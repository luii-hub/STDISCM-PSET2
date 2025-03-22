# STDISCM S12 Problem Set 2:Dungeon Queue System Synchronization

A Java-based simulation of a dungeon queuing system for an MMORPG, managing party formation and dungeon instance completion. Created within IntelliJ IDE.

## Developer
- RANA, Luis Miguel | S12 | 121...24

## Overview

As the guild master of a guild in a MMORPG, you are responsible for managing the dungeon queuing system. Your guild has a fixed number of tanks, healers, and DPS players, and you need to form parties of 5 players to complete dungeons. 

## Features

- Manages dungeon instances and party formation
- Handles multiple concurrent dungeon runs
- Tracks player counts by role (Tank, Healer, DPS)
- Provides real-time status updates
- Generates summary reports after dungeon completion
- Prevents deadlock and starvation in resource allocation
- Concurrency control using CountDownLatch and Object lock 

## Requirements

- Java Development Kit (JDK) 17 or higher

## Input 

- n: Number of Dungeons
- t: Number of Tanks
- h: Number of Healers
- d: Number of DPS
- t1: Minimum time (in seconds) for a dungeon to complete
- t2: Maximum time (in seconds) for a dungeon to complete

## Output

- Summary report after all dungeons are completed


## How to Build and Run  the Project
1. Clone the repository from GitHub
2. Open the project directory
3. Build and Run the project
4. Configure the input parameters
5. Run Program and enter the party formation
6. Wait for the program to finish
