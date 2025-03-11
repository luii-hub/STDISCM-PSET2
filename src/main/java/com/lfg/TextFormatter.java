package com.lfg;

/**
 * Helper class for text formatting and console operations.
 * Provides methods for colored text output and console clearing.
 */
public class TextFormatter {
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // Role colors
    public static final String TANK_COLOR = BLUE;
    public static final String HEALER_COLOR = GREEN;
    public static final String DPS_COLOR = RED;
    
    // Other game element colors
    public static final String DUNGEON_COLOR = PURPLE;
    public static final String TIME_COLOR = YELLOW;
    public static final String GUILD_MASTER_COLOR = CYAN;
    
    /**
     * Formats text for tank role.
     * 
     * @param text The text to format
     * @return Formatted text with tank color
     */
    public static String formatTank(String text) {
        return TANK_COLOR + text + RESET;
    }
    
    /**
     * Formats text for healer role.
     * 
     * @param text The text to format
     * @return Formatted text with healer color
     */
    public static String formatHealer(String text) {
        return HEALER_COLOR + text + RESET;
    }
    
    /**
     * Formats text for DPS role.
     * 
     * @param text The text to format
     * @return Formatted text with DPS color
     */
    public static String formatDPS(String text) {
        return DPS_COLOR + text + RESET;
    }
    
    /**
     * Formats text for dungeon references.
     * 
     * @param text The text to format
     * @return Formatted text with dungeon color
     */
    public static String formatDungeon(String text) {
        return DUNGEON_COLOR + text + RESET;
    }
    
    /**
     * Formats text for time values.
     * 
     * @param text The text to format
     * @return Formatted text with time color
     */
    public static String formatTime(String text) {
        return TIME_COLOR + text + RESET;
    }
    
    /**
     * Formats text for Guild Master references.
     * 
     * @param text The text to format
     * @return Formatted text with Guild Master color
     */
    public static String formatGuildMaster(String text) {
        return GUILD_MASTER_COLOR + text + RESET;
    }
    
    /**
     * Formats a role name with appropriate color.
     * 
     * @param role The role name to format
     * @return Formatted role name
     */
    public static String formatRole(String role) {
        if (role.toLowerCase().contains("tank")) {
            return formatTank(role);
        } else if (role.toLowerCase().contains("heal")) {
            return formatHealer(role);
        } else if (role.toLowerCase().contains("dps")) {
            return formatDPS(role);
        } else {
            return YELLOW + role + RESET;
        }
    }
    
    /**
     * Formats a status message with appropriate color.
     * 
     * @param status The status message to format
     * @return Formatted status message
     */
    public static String formatStatus(String status) {
        if (status.toLowerCase().equals("active")) {
            return GREEN + status + RESET;
        } else if (status.toLowerCase().equals("empty")) {
            return RED + status + RESET;
        }
        return status;
    }
    
    /**
     * Formats a header with a consistent style.
     * 
     * @param text The header text to format
     * @return Formatted header text
     */
    public static String formatHeader(String text) {
        return CYAN + "== " + text + " ==" + RESET;
    }
    
    /**
     * Formats a line with consistent indentation and styling.
     * 
     * @param text The line text to format
     * @return Formatted line text
     */
    public static String formatLine(String text) {
        return "  " + text;
    }
    
    /**
     * Clears the console screen.
     * Works for both Windows and Unix-based systems.
     */
    public static void clearScreen() {
        try {
            final String os = System.getProperty("os.name");
            
            if (os.contains("Windows")) {
                // For Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // For Unix/Linux/MacOS
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback if clearing fails
            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        }
    }

    public static void pressAnyKeyToContinue() {
        System.out.println("\nPress any key to continue...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Fallback if reading fails
        }
    }
    
    /**
     * Creates a horizontal line for tables.
     * 
     * @param length The length of the line
     * @return A formatted horizontal line
     */
    public static String horizontalLine(int length) {
        return YELLOW + "+" + "-".repeat(length) + "+" + RESET;
    }

    /**
     * Formats a table row with consistent spacing.
     * 
     * @param label The label for the row
     * @param value The value for the row
     * @return A formatted table row
     */
    public static String tableRow(String label, String value) {
        return YELLOW + "| " + RESET + String.format("%-20s", label) + 
               YELLOW + " | " + RESET + String.format("%-15s", value) + 
               YELLOW + " |" + RESET;
    }
}
