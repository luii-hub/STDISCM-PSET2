package com.lfg;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static com.lfg.TextFormatter.*;

/**
 * DungeonManager class for managing dungeon instances and party formation.
 * This class is responsible for:
 * - Managing dungeon instances (creation, completion, tracking)
 * - Forming parties from available players
 * - Tracking player counts by role (tank, healer, DPS)
 * - Synchronizing access to shared resources
 * - Providing status updates on dungeon instances
 */
public class DungeonManager {
    private final int maxInstances;
    private final int minTime;
    private final int maxTime;
    
    private int tankCount;
    private int healerCount;
    private int dpsCount;
    
    private boolean[] instanceActive;
    private int[] instancePartiesServed;
    private int[] instanceTotalTime;
    private int[] currentCompletionTimes;
    
    private CountDownLatch completionLatch;
    private final Object lock = new Object();
    private Random random = new Random();
    private boolean isFormingParties = false;
    
    /**
     * Constructs a new DungeonManager with the specified parameters.
     * Initializes the instance tracking arrays and player counts.
     * 
     * @param maxInstances Maximum number of concurrent dungeon instances
     * @param minTime Minimum time (in seconds) for a dungeon to complete
     * @param maxTime Maximum time (in seconds) for a dungeon to complete
     */
    public DungeonManager(int maxInstances, int minTime, int maxTime) {
        this.maxInstances = maxInstances;
        this.minTime = minTime;
        this.maxTime = maxTime;
        
        this.instanceActive = new boolean[maxInstances];
        this.instancePartiesServed = new int[maxInstances];
        this.instanceTotalTime = new int[maxInstances];
        this.currentCompletionTimes = new int[maxInstances];
        
        this.tankCount = 0;
        this.healerCount = 0;
        this.dpsCount = 0;
    }
    
    /**
     * Adds players to the queue.
     * 
     * @param tanks Number of tanks to add
     * @param healers Number of healers to add
     * @param dps Number of DPS to add
     */
    public synchronized void queuePlayers(int tanks, int healers, int dps) {
        synchronized (lock) {
            // Store initial counts
            this.tankCount = tanks;
            this.healerCount = healers;
            this.dpsCount = dps;
            
            // Calculate the maximum number of parties that can be formed
            int maxParties = calculateMaxParties();
            
            // Initialize the completion latch with the maximum number of parties
            this.completionLatch = new CountDownLatch(maxParties);
        
            // Print the initial queue
            System.out.println("\nInitial queue:");
            System.out.println(formatRole("Tanks") + ": " + tankCount);
            System.out.println(formatRole("Healers") + ": " + healerCount);
            System.out.println(formatRole("DPS") + ": " + dpsCount);
            System.out.println("Maximum parties possible: " + maxParties);
            System.out.println("Limiting factor: " + getPartyLimitingFactor());
        }
    }
    
    /**
     * Calculates the maximum number of parties that can be formed with the current resources.
     * 
     * @return The maximum number of parties that can be formed
     */
    private int calculateMaxParties() {
        // A party requires 1 tank, 1 healer, and 3 DPS
        int maxPartiesFromTanks = tankCount;
        int maxPartiesFromHealers = healerCount;
        int maxPartiesFromDPS = dpsCount / 3;
        
        // The limiting factor is the minimum of the three
        return Math.min(Math.min(maxPartiesFromTanks, maxPartiesFromHealers), maxPartiesFromDPS);
    }
    
    /**
     * Gets the limiting factor for party formation.
     * 
     * @return A string describing which role is limiting party formation
     */
    private String getPartyLimitingFactor() {
        int maxPartiesFromTanks = tankCount;
        int maxPartiesFromHealers = healerCount;
        int maxPartiesFromDPS = dpsCount / 3;
        
        int minParties = Math.min(Math.min(maxPartiesFromTanks, maxPartiesFromHealers), maxPartiesFromDPS);
        
        if (minParties == maxPartiesFromTanks) {
            return formatRole("Tanks") + " (1 per party)";
        } else if (minParties == maxPartiesFromHealers) {
            return formatRole("Healers") + " (1 per party)";
        } else {
            return formatRole("DPS") + " (3 per party)";
        }
    }
    
    /**
     * Forms parties from available players and assigns them to instances.
     */
    public void formParties() {
        boolean acquired = false;
        try {
            // Use a timeout when acquiring the lock to prevent deadlock
            synchronized (lock) {
                // If another thread is already forming parties, return
                if (isFormingParties) {
                    return;
                }
                isFormingParties = true;
                acquired = true;
            }
            
            boolean morePartiesToForm = true;
            
            while (morePartiesToForm) {
                int availableInstance = -1;
                boolean canFormParty = false;
                
                synchronized (lock) {
                    // Check if we can form more parties
                    if (tankCount <= 0 || healerCount <= 0 || dpsCount < 3) {
                        // Print a message if we can't form parties due to lack of resources
                        if (tankCount > 0 || healerCount > 0 || dpsCount > 0) {
                            System.out.println("\nCannot form more parties due to:");
                            if (tankCount <= 0) System.out.println("- Not enough " + formatRole("Tanks"));
                            if (healerCount <= 0) System.out.println("- Not enough " + formatRole("Healers"));
                            if (dpsCount < 3) System.out.println("- Not enough " + formatRole("DPS") + " (need at least 3)");
                        }
                        morePartiesToForm = false;
                        continue;
                    }
                    
                    // Find an available instance
                    for (int i = 0; i < maxInstances; i++) {
                        if (!instanceActive[i]) {
                            availableInstance = i;
                            canFormParty = true;
                            break;
                        }
                    }
                    
                    // If we can form a party and have an available instance
                    if (canFormParty) {
                        // Create a party
                        tankCount--;
                        healerCount--;
                        dpsCount -= 3;
                        
                        // Start the instance
                        startInstance(availableInstance);
                    } else {
                        // No available instances, wait for one to become available
                        System.out.println("\nAll instances are currently active. Waiting for one to become available...");
                        morePartiesToForm = false;
                    }
                }
                
                // If no instances are available but we have resources, wait a bit before checking again
                // This prevents CPU spinning and allows other threads to make progress
                if (!morePartiesToForm && tankCount > 0 && healerCount > 0 && dpsCount >= 3) {
                    try {
                        // Release the forming parties flag temporarily to prevent starvation
                        synchronized (lock) {
                            isFormingParties = false;
                            acquired = false;
                        }
                        
                        // Wait a bit before checking for available instances again
                        Thread.sleep(500);
                        
                        // Reacquire the forming parties flag
                        synchronized (lock) {
                            if (isFormingParties) {
                                // Another thread is now forming parties, so we can return
                                return;
                            }
                            isFormingParties = true;
                            acquired = true;
                        }
                        
                        // Check again if we can form more parties
                        synchronized (lock) {
                            for (int i = 0; i < maxInstances; i++) {
                                if (!instanceActive[i]) {
                                    morePartiesToForm = true;
                                    break;
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } finally {
            // Make sure we always release the forming parties flag if we acquired it
            if (acquired) {
                synchronized (lock) {
                    isFormingParties = false;
                }
            }
        }
    }
    
    /**
     * Starts a dungeon instance with the given ID.
     * 
     * @param instanceId The ID of the instance to start
     */
    private void startInstance(int instanceId) {
        // Calculate random completion time
        final int completionTime = minTime + random.nextInt(maxTime - minTime + 1);
        
        synchronized (lock) {
            instanceActive[instanceId] = true;
            currentCompletionTimes[instanceId] = completionTime;
            
            System.out.println("\nStarting " + formatDungeon("instance") + " " + (instanceId + 1) + " with completion time: " + TIME_COLOR + completionTime + " seconds" + RESET);
            printInstanceStatus(); // Always print status when starting an instance
            
            // Print remaining queue after starting an instance
            System.out.println("\nRemaining in queue:");
            System.out.println(formatRole("Tanks") + ": " + tankCount);
            System.out.println(formatRole("Healers") + ": " + healerCount);
            System.out.println(formatRole("DPS") + ": " + dpsCount);
        }
        
        // Start a thread to simulate the dungeon run
        new Thread(() -> {
            try {
                // Sleep for the completion time (in milliseconds)
                Thread.sleep(completionTime * 1000);
                
                synchronized (lock) {
                    // Only proceed if the instance is still active
                    if (instanceActive[instanceId]) {
                        // Update instance statistics
                        instanceActive[instanceId] = false;
                        instancePartiesServed[instanceId]++;
                        instanceTotalTime[instanceId] += completionTime;
                        currentCompletionTimes[instanceId] = 0; // Reset current completion time
                        
                        System.out.println("\n" + formatDungeon("Instance") + " " + (instanceId + 1) + " completed after " + TIME_COLOR + completionTime + " seconds" + RESET);
                        printInstanceStatus(); // Always print status when an instance completes
                        
                        // Signal that this party has completed
                        completionLatch.countDown();
                    }
                }
                
                // Add a small delay before trying to form more parties
                // This helps prevent race conditions with very short completion times
                Thread.sleep(100);
                
                // Try to form more parties if possible, but only if we're not the last party
                synchronized (lock) {
                    if (tankCount > 0 && healerCount > 0 && dpsCount >= 3) {
                        // We need to run this in a separate thread to avoid deadlocks
                        new Thread(() -> formParties()).start();
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * Waits for all instances to complete.
     */
    public void waitForCompletion() {
        try {
            // Check if we can form any more parties
            boolean canFormMoreParties = true;
            
            while (canFormMoreParties) {
                boolean instancesActive = false;
                
                synchronized (lock) {
                    // Check if we have the resources to form more parties
                    canFormMoreParties = (tankCount > 0 && healerCount > 0 && dpsCount >= 3);
                    
                    // If we can't form more parties, but there are still active instances,
                    // we need to wait for them to complete
                    if (!canFormMoreParties) {
                        for (int i = 0; i < maxInstances; i++) {
                            if (instanceActive[i]) {
                                instancesActive = true;
                                break;
                            }
                        }
                        
                        if (!instancesActive) {
                            // No more parties can be formed and no instances are active
                            break;
                        }
                    }
                }
                
                // If we can't form more parties but instances are still active,
                // wait a bit before checking again
                if (!canFormMoreParties && instancesActive) {
                    Thread.sleep(500);
                    continue;
                }
                
                // Wait a bit before checking again - this prevents CPU spinning
                Thread.sleep(500);
            }
            
            // Make sure all instances are complete
            boolean allDone = false;
            int checkCount = 0;
            
            while (!allDone) {
                synchronized (lock) {
                    allDone = true;
                    for (int i = 0; i < maxInstances; i++) {
                        if (instanceActive[i]) {
                            allDone = false;
                            break;
                        }
                    }
                }
                
                if (!allDone) {
                    // If instances are still active, wait a bit before checking again
                    Thread.sleep(500);
                    
                    // Add a safety timeout to prevent infinite waiting
                    checkCount++;
                    if (checkCount > 60) { // 30 seconds timeout
                        System.out.println("\nWARNING: Timeout waiting for instances to complete. Some instances may still be active.");
                        break;
                    }
                }
            }
            
            // Make sure all console output is complete
            Thread.sleep(1000);
            
            System.out.println("\n== All Dungeons Completed ==");
            printInstanceStatus();
            
            pressAnyKeyToContinue();
            printSummary(); // Print summary only after all instances are done
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Prints the current status of all instances.
     */
    public void printInstanceStatus() {
        System.out.println("\nCurrent " + formatDungeon("Instance") + " Status:");
        for (int i = 0; i < maxInstances; i++) {
            if (instanceActive[i]) {
                System.out.println(formatDungeon("Dungeon") + " " + (i + 1) + ": " + formatStatus("active") + " | Time to subjugate: " + TIME_COLOR + currentCompletionTimes[i] + " seconds" + RESET);
            } else {
                System.out.println(formatDungeon("Dungeon") + " " + (i + 1) + ": " + formatStatus("empty"));
            }
        }
    }
    
    /**
     * Prints a summary of the dungeon quest.
     */
    public void printSummary() {
        System.out.println("\n" + formatHeader("Dungeon Quest Summary"));
        for (int i = 0; i < maxInstances; i++) {
            System.out.println(formatDungeon("Dungeon") + " " + (i + 1) + ": " +
                    "Parties served: " + instancePartiesServed[i] +
                    ", Total time: " + TIME_COLOR + instanceTotalTime[i] + " seconds" + RESET);
        }
        
        // Print unused players
        System.out.println("\n" + formatRole("Idle Adventurers") + ":");
        System.out.println(formatRole("Tanks") + ": " + tankCount);
        System.out.println(formatRole("Healers") + ": " + healerCount);
        System.out.println(formatRole("DPS") + ": " + dpsCount);
        
        // Identify the limiting role
        if (tankCount > 0 || healerCount > 0 || dpsCount > 0) {
            System.out.println("\n" + formatHeader("The conquest had ended! Cannot generate more parties because of lack of:"));
            
            if (tankCount == 0 && (healerCount > 0 || dpsCount >= 3)) {
                System.out.println("- " + formatRole("Tanks"));
            }
            
            if (healerCount == 0 && (tankCount > 0 || dpsCount >= 3)) {
                System.out.println("- " + formatRole("Healers"));
            }
            
            if (dpsCount < 3 && (tankCount > 0 || healerCount > 0)) {
                System.out.println("- " + formatRole("DPS") + " (need at least 3)");
            }
            
            if (tankCount > 0 && healerCount > 0 && dpsCount >= 3) {
                System.out.println("- Available " + formatDungeon("instances") + " (all instances are in use)");
            }
        } else {
            System.out.println("\nAll " + formatRole("Adventurers") + " were assigned to parties.");
        }
    }
}
