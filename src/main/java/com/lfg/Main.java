package com.lfg;

import java.util.Scanner;
import static com.lfg.TextFormatter.*;

/**
 * Main class for the Dungeon Queue System.
 * This class handles user input, initializes the DungeonManager, and controls the overall flow of the application.
 * It provides a command-line interface for users to interact with the system.
 */
public class Main {
    /**
     * The entry point of the application.
     * Collects user input, validates it, initializes the DungeonManager, and starts the dungeon queue process.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Get max instances
            System.out.println("-Welcome " + formatGuildMaster("Guild Master") + "! Please enter the following values-\n");
            int maxInstances = getValidatedIntInput(scanner, "How many " + formatDungeon("dungeons") + " should we clear today? (n): ", 1, Integer.MAX_VALUE, "dungeon");
            
            // Get player counts
            int tankCount = getValidatedIntInput(scanner, "How many " + formatRole("tanks") + " do we have? ", 1, Integer.MAX_VALUE, "tank");
            int healerCount = getValidatedIntInput(scanner, "How many " + formatRole("healers") + " do we have? ", 1, Integer.MAX_VALUE, "healer");
            int dpsCount = getValidatedIntInput(scanner, "How many " + formatRole("DPS") + " do we have? ", 3, Integer.MAX_VALUE, "dps");
            
            // Get time values
            int minTime = getValidatedIntInput(scanner, "How many minimum " + formatDungeon("dungeon") + " time (t1) in seconds? ", 1, 15, "mintime");
            int maxTime = getValidatedIntInput(scanner, "How many maximum " + formatDungeon("dungeon") + " time (t2) in seconds? ", minTime, 15, "maxtime");
            
            System.out.println("\n" + formatGuildMaster("Initializing Dungeon Subjugation Quest...") + "\n");
            
            // Create table
            int tableWidth = 40;
            System.out.println(horizontalLine(tableWidth));
            System.out.println(tableRow("Dungeons to Clear", String.valueOf(maxInstances)));
            System.out.println(horizontalLine(tableWidth));
            System.out.println(tableRow("Tanks", String.valueOf(tankCount)));
            System.out.println(tableRow("Healers", String.valueOf(healerCount)));
            System.out.println(tableRow("DPS", String.valueOf(dpsCount)));
            System.out.println(horizontalLine(tableWidth));
            System.out.println(tableRow("Clear Time (Min)", minTime + " seconds"));
            System.out.println(tableRow("Clear Time (Max)", maxTime + " seconds"));
            System.out.println(horizontalLine(tableWidth) + "\n");
            
            pressAnyKeyToContinue();
            clearScreen();

            // Create dungeon manager
            DungeonManager manager = new DungeonManager(maxInstances, minTime, maxTime);
            
            // Queue players
            manager.queuePlayers(tankCount, healerCount, dpsCount);
            
            // Form parties and start instances
            manager.formParties();
            
            // Wait for all instances to complete
            manager.waitForCompletion();
            
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Gets a validated integer input from the user within the specified range.
     * 
     * @param scanner The scanner to read input from
     * @param prompt The prompt to display to the user
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (inclusive)
     * @return The validated integer input
     */
    private static int getValidatedIntInput(Scanner scanner, String prompt, int min, int max, String type) {
        int value;
        while (true) {
            System.out.print(prompt);
            try {
                if (scanner.hasNextInt()) {
                    value = scanner.nextInt();
                    if (value >= min && value <= max) {
                        scanner.nextLine(); // Consume the newline
                        return value;
                    } else {
                        switch (type) {
                            case "tank":
                                System.out.println("Error: Minimum value is 1");
                                break;
                            case "healer":
                            System.out.println("Error: Minimum value is 1");
                            break;
                            case "dps":
                            System.out.println("Error: Minimum value is 3");
                            break;
                            case "dungeon":
                                System.out.println("Error: Minimum value is 1");
                                break;
                            case "minTime":
                                System.out.println("Error: Value must be between " + min + " and " + max + ". Please try again.");
                                break;
                            case "maxTime":
                            System.out.println("Error: Value must be between " + min + " and " + max + ". Please try again.");
                            break;
                            default:
                                System.out.println("Error: Value must be between " + min + " and " + max + ". Please try again.");
                                break;
                        }
                    }
                } else {
                    System.out.println("Error: Please enter a valid integer.");
                    scanner.nextLine(); // Consume the invalid input
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                scanner.nextLine(); // Consume the invalid input
            }
        }
    }
}
