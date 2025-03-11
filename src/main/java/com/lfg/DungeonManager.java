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
            
            // Calculate how many complete parties we can potentially form
            int possibleParties = Math.min(maxInstances,
                    Math.min(tanks, Math.min(healers, dps / 3)));
            
            // Create latch
            this.completionLatch = new CountDownLatch(possibleParties);
            
            System.out.println("\nInitial queue:");
            System.out.println(formatRole("Tanks") + ": " + tankCount);
            System.out.println(formatRole("Healers") + ": " + healerCount);
            System.out.println(formatRole("DPS") + ": " + dpsCount);
            pressAnyKeyToContinue();
            clearScreen();
        }
    }
    
    /**
     * Forms parties from available players and assigns them to instances.
     */
    public void formParties() {
        synchronized (lock) {
            // If another thread is already forming parties, return
            if (isFormingParties) {
                return;
            }
            isFormingParties = true;
        }
        
        try {
            boolean morePartiesToForm = true;
            boolean statusPrinted = false;
            
            while (morePartiesToForm) {
                synchronized (lock) {
                    // Check if we can form more parties
                    if (tankCount <= 0 || healerCount <= 0 || dpsCount < 3) {
                        morePartiesToForm = false;
                        continue;
                    }
                    
                    // Find an available instance
                    int instanceId = findAvailableInstance();
                    
                    if (instanceId >= 0) {
                        // Create a party
                        tankCount--;
                        healerCount--;
                        dpsCount -= 3;
                        
                        // Start the instance
                        startInstance(instanceId);
                        
                        // Only print queue status once per batch of party formations
                        if (!statusPrinted) {
                            System.out.println("\nRemaining in queue:");
                            System.out.println(formatRole("Tanks") + ": " + tankCount);
                            System.out.println(formatRole("Healers") + ": " + healerCount);
                            System.out.println(formatRole("DPS") + ": " + dpsCount);
                            
                            // Print instance status
                            printInstanceStatus();
                            statusPrinted = true;
                        }
                    } else {
                        // No available instances, wait for one to become available
                        morePartiesToForm = false;
                    }
                }
                
                // If no instances are available, wait a bit before checking again
                if (!morePartiesToForm) {
                    try {
                        Thread.sleep(500);
                        
                        // Check again if we can form more parties
                        synchronized (lock) {
                            if (tankCount > 0 && healerCount > 0 && dpsCount >= 3 && findAvailableInstance() >= 0) {
                                morePartiesToForm = true;
                                statusPrinted = false; // Reset for next batch
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } finally {
            synchronized (lock) {
                isFormingParties = false;
            }
        }
    }
    
    /**
     * Finds an available instance.
     * 
     * @return The index of the available instance, or -1 if none are available
     */
    private int findAvailableInstance() {
        for (int i = 0; i < maxInstances; i++) {
            if (!instanceActive[i]) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Starts a dungeon instance with the given ID.
     * 
     * @param instanceId The ID of the instance to start
     */
    private void startInstance(int instanceId) {
        instanceActive[instanceId] = true;
        
        // Calculate random completion time
        final int completionTime = minTime + random.nextInt(maxTime - minTime + 1);
        currentCompletionTimes[instanceId] = completionTime;
        
        System.out.println("\nStarting " + formatDungeon("instance") + " " + (instanceId + 1) + " with completion time: " + TIME_COLOR + completionTime + " seconds" + RESET);
        printInstanceStatus(); // Always print status when starting an instance

        
        // Start a thread to simulate the dungeon run
        new Thread(() -> {
            try {
                // Sleep for the completion time (in milliseconds)
                Thread.sleep(completionTime * 1000);
                
                synchronized (lock) {
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
                
                // Try to form more parties if possible, but only if we're not the last party
                if (completionLatch.getCount() > 0) {
                    formParties();
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
            completionLatch.await(); // Wait for all parties to complete
            Thread.sleep(500); // Small delay to ensure all completion messages are printed
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
