package com.lfg;

/**
 * Party class representing a group of players for a dungeon.
 * A party consists of specific numbers of tanks, healers, and DPS players.
 * In this system, a standard party composition is 1 tank, 1 healer, and 3 DPS.
 */
public class Party {
    /**
     * The number of tank players in the party.
     */
    private final int tanks;
    
    /**
     * The number of healer players in the party.
     */
    private final int healers;
    
    /**
     * The number of DPS (Damage Per Second) players in the party.
     */
    private final int dps;

    /**
     * Constructs a new Party with the specified numbers of tanks, healers, and DPS players.
     * 
     * @param tanks  the number of tank players in the party
     * @param healers  the number of healer players in the party
     * @param dps  the number of DPS players in the party
     */
    public Party(int tanks, int healers, int dps) {
        this.tanks = tanks;
        this.healers = healers;
        this.dps = dps;
    }

    /**
     * Returns a string representation of the party, including the numbers of tanks, healers, and DPS players.
     * 
     * @return a string representation of the party
     */
    @Override
    public String toString() {
        return "Party: " + tanks + " tank(s), " + healers + " healer(s), " + dps + " DPS";
    }
}
