package beastmode;

import battlecode.common.*;

import static beastmode.RobotPlayer.*;

public class Utility {
    RobotController rc;
    int localID;

    public Utility(RobotController rc) {
        this.rc = rc;
    }

    public void setLocalID(int localID) {
        this.localID = localID;
    }

    public int makeLocalID(int assigningLocalIDIndex) throws GameActionException {
        // for each duck on round one, give it a random local ID
        // (random localID is just for indexing into shared array pretty much)
        int currentID = rc.readSharedArray(assigningLocalIDIndex);
        int localID = ++currentID;
        rc.writeSharedArray(assigningLocalIDIndex, localID); // change the shared array ID so next duck gets incremented ID
        if (localID == 50) {
            rc.writeSharedArray(assigningLocalIDIndex, 0);
        }
        return localID;
    }

    public CoolRobotInfo[] readAllBotsInfoFromSharedArray(CoolRobotInfo[] coolRobotInfoArray) throws GameActionException {
        if (rc.getRoundNum() > 1) { // not all bots have written their stuff into their index until round 2 starts
            for (int i = 1; i <= 50; i++) {
                int coolRobotInfoInt = rc.readSharedArray(i);
                coolRobotInfoArray[i - 1] = new CoolRobotInfo(i, coolRobotInfoInt);
            }
        }
        return coolRobotInfoArray;
    }


    public void writeMyInfoToSharedArray() throws GameActionException {
        MapLocation locationToStore;
        if (rc.isSpawned()) locationToStore = rc.getLocation();
        else locationToStore = NONELOCATION; // NONELOCATION (-1, -1) represents not spawned rn (no location)
        int coolRobotInfoInt = convertRobotInfoToInt(locationToStore, rc.hasFlag());
//      if (rc.getRoundNum() == 1) System.out.println("id: " + localID + "coolRobotInfoInt: " + Integer.toBinaryString(coolRobotInfoInt));
        rc.writeSharedArray(localID, coolRobotInfoInt);
    }

    /**
     * Spawns the bots evenly distributed among the three spawn zones using localID (for start of game)
     *
     * @throws GameActionException if spawning fails
     */

    public MapLocation[] findCentersOfSpawnZones() {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();

        int spawnCentersTracker = 0;
        MapLocation[] spawnAreaCenters = new MapLocation[3];

        for (MapLocation loc : spawnLocs) {
            if (spawnCentersTracker == 3) break;
            // build each tile that would be surrounding this loc if it was a valid spawn area center. after each maploc you build,
            // check if it exists in the spawnLocs array. if all tiles that we build are in the spawnLocs array, then this is a valid spawn area center
            MapLocation[] potentialSpawnAreaSquare = new MapLocation[2];
            potentialSpawnAreaSquare[0] = loc.add(Direction.NORTHWEST);
            potentialSpawnAreaSquare[1] = loc.add(Direction.SOUTHEAST);

            boolean chillinRelaxinAllCool = true;
            for (MapLocation mapLoc : potentialSpawnAreaSquare) {
                boolean thisMapLocIsChillinRelaxinAllCool = false;
                for (MapLocation spawnLoc : spawnLocs) {
                    if (mapLoc.equals(spawnLoc)) {
                        thisMapLocIsChillinRelaxinAllCool = true;
                        break;
                    }
                }
                if (!thisMapLocIsChillinRelaxinAllCool) {
                    chillinRelaxinAllCool = false;
                    break;
                }
            }

            if (chillinRelaxinAllCool) {
                switch (spawnCentersTracker) {
                    case 0:
                        spawnAreaCenters[0] = loc;
                        break;
                    case 1:
                        spawnAreaCenters[1] = loc;
                        break;
                    case 2:
                        spawnAreaCenters[2] = loc;
                        break;
                }
                spawnCentersTracker++;
            }
        }

//        if (rc.getRoundNum() == 1) System.out.println("id:" + localID + " bytecode used after: " + Clock.getBytecodeNum());
//
//        rc.setIndicatorDot(spawnAreaCenters[0], 255, 0, 255);
//        rc.setIndicatorDot(spawnAreaCenters[1], 255, 0, 255);
//        rc.setIndicatorDot(spawnAreaCenters[2], 255, 0, 255);

        return spawnAreaCenters;
    }

    public void trySpawningEvenly(MapLocation[] spawnAreaCenters) throws GameActionException {
        if (rc.isSpawned()) return;

        int whichSpawnArea = localID % 3;
        MapLocation chosenCenter = spawnAreaCenters[whichSpawnArea];
        // build a 3x3 square of maplocations with chosenCenter being the center of the square
        MapLocation[] chosenSpawnArea = new MapLocation[9];
        int tracker = 0;
        for (int x = chosenCenter.x - 1; x <= chosenCenter.x + 1; x++) {
            for (int y = chosenCenter.y - 1; y <= chosenCenter.y + 1; y++) {
                chosenSpawnArea[tracker] = new MapLocation(x, y);
                tracker++;
            }
        }
//        System.out.println("here ong fr");
        for (MapLocation loc : chosenSpawnArea) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                break; // Spawn at the first valid location
            }
        }

    }

    /**
     * this class stores info about a bot, as it is stored in the shared array. create an object of this class
     * for each robot's stuff u read from the shared array
     */
    public class CoolRobotInfo {
        int localID;
        MapLocation curLocation; // highest 12 bits
        boolean hasFlag; // 13th bit

        // lower 3 bits are free rn

        public CoolRobotInfo(int localID, int intOfRobotFromArray) throws GameActionException {
            this.localID = localID;
            // highest 12 bits will be the location, so take highest 12 bits, convert that int into a location, and store the MapLocation into curLocation
            curLocation = intToLocation(intOfRobotFromArray >> 4);
            // 13th bit will be if the robot has a flag, so take the 13th bit, and store it into hasFlag
            hasFlag = ((intOfRobotFromArray >> 3) & 1) == 1;
        }

        public boolean getHasFlag() {
            return hasFlag;
        }

        public MapLocation getCurLocation() {
            return curLocation;
        }
    }


    /**
     * call this one when u are converting ur own into to an int
     *
     * @param curLocation robots current location
     * @param hasFlag     have flag
     * @return all a robots info converted to a convenient int
     */
    public int convertRobotInfoToInt(MapLocation curLocation, boolean hasFlag) {
        int intToReturn = 0;
        intToReturn += locationToInt(curLocation) << 4;
        if (hasFlag) {
            intToReturn += 1 << 3;
        }
        return intToReturn;
    }

    /**
     * call this one when u are converting a coolRobotInfo into an int
     *
     * @param coolRobotInfo coolRobotInfo to convert
     * @return an int of the robots info that can be stored into the array
     */
    public int convertRobotInfoToInt(CoolRobotInfo coolRobotInfo) {
        return convertRobotInfoToInt(coolRobotInfo.curLocation, coolRobotInfo.hasFlag);
    }

    /**
     * puts a map location to an integer (last 12 bits are x and y, with y the last six bits
     *
     * @param location location to convert
     * @return integer representation of the location
     */
    public int locationToInt(MapLocation location) {
        if (location.equals(NONELOCATION)) return 4095; // 4095 is all 1's, representing no location
        return (location.x << 6) + location.y;
    }

    /**
     * puts an integer to a map location
     *
     * @param integerLocation int to convert to map location
     * @return map location representation of the int
     */
    public MapLocation intToLocation(int integerLocation) {
        if (integerLocation == 4095) return NONELOCATION; // 4095 is all 1's, representing a null location
        return new MapLocation(integerLocation >> 6, integerLocation & 63);
    }

    public void didISpawnOnFlag(Utility util) throws GameActionException {
        MapLocation me = rc.getLocation();
        FlagInfo[] flags = rc.senseNearbyFlags(1, rc.getTeam());
        if (flags.length > 0) {
            MapLocation flagLoc = flags[0].getLocation();
            if (me.x == flagLoc.x && me.y == flagLoc.y) {
                if (rc.readSharedArray(breadLocOneIndex) == 0) {
                    rc.writeSharedArray(breadLocOneIndex, util.locationToInt(me));
                } else if (rc.readSharedArray(breadLocTwoIndex) == 0) {
                    rc.writeSharedArray(breadLocTwoIndex, util.locationToInt(me));
                } else if (rc.readSharedArray(breadLocThreeIndex) == 0) {
                    rc.writeSharedArray(breadLocThreeIndex, util.locationToInt(me));
                }
            }
        }
    }

    /**
     * set leader of each group as the lowest ID duck in each group
     *
     * @throws GameActionException if cannot write to shared array
     */
    public void setInitialGroupLeaders() throws GameActionException {
        rc.writeSharedArray(flagRunnerGroupOneLocIndex, 1);
        rc.writeSharedArray(flagRunnerGroupTwoLocIndex, 11);
        rc.writeSharedArray(flagRunnerGroupThreeLocIndex, 21);
    }

    /**
     * splits all flagrunners into the three groups. if a non flagrunner calls this func, they will get a group of 0.
     * dont let that happen unless you intend it !!! !!! !!!
     *
     * @return int representing which group the bot is in (1, 2, or 3 (0 for none))
     */
    public int getMyFlagrunnerGroup() {
        // the max number of flagrunner groups is 3. the max amount of flag runners is stored in the constant in robotcontroller AMOUNT_OF_FLAGRUNNERS
        // divide the first 1/3 of flagrunners into group 1, the second 1/3 into group 2, and the last 1/3 into group 3
        if (localID <= AMOUNT_OF_FLAGRUNNERS / 3) {
            return 1;
        } else if (localID <= 2 * AMOUNT_OF_FLAGRUNNERS / 3) {
            return 2;
        } else if (localID <= AMOUNT_OF_FLAGRUNNERS) {
            return 3;
        } else {
            return 0; // this is bad if it returns 0 !
        }

    }

    /**
     * gives a bot their current group leader
     *
     * @return the localID of the group leader
     */
    public int whoIsMyGroupLeader() throws GameActionException {
        int whatIndexToReadFrom = flagRunnerGroupIndexingStart + whichFlagrunnerGroup;
        return rc.readSharedArray(whatIndexToReadFrom);
    }

    public MapLocation getLocationOfMyGroupLeader() throws GameActionException {
        int arrayIndexToReadFrom = flagRunnerGroupIndexingStart + whichFlagrunnerGroup;

//        // print the entire shared array
//        for (int i = 0; i < 64; i++) {
//            System.out.println("index: " + i + "value: " + rc.readSharedArray(i));
//        }

//        System.out.println("localID: " + localID);
//        System.out.println("index reading: " + arrayIndexToReadFrom);
        int localIDOfGroupLeader = rc.readSharedArray(arrayIndexToReadFrom);
//        System.out.println(localIDOfGroupLeader);

        CoolRobotInfo groupLeaderInfo = coolRobotInfoArray[localIDOfGroupLeader - 1];
        return groupLeaderInfo.curLocation;
    }

    /**
     * checks if that ducks group leader is dead, and if so, makes the duck checking, the new group leader
     *
     * @throws GameActionException if the duck cannot read/write to array
     */
    public void handleIfGroupLeaderDied() throws GameActionException {
        int arrayIndexToReadFrom = flagRunnerGroupIndexingStart + whichFlagrunnerGroup;
        int localIDOfGroupLeader = rc.readSharedArray(arrayIndexToReadFrom);
        CoolRobotInfo groupLeaderInfo = coolRobotInfoArray[localIDOfGroupLeader - 1];
        if (groupLeaderInfo.curLocation == NONELOCATION) {
            // if group leader is dead, make ourselves the new group leader
            rc.writeSharedArray(arrayIndexToReadFrom, localID);
        }
    }

    /**
     * @param nearbyEnemies Array of robots
     * @return Enemy with lowest HP or null if no enemies around
     */
    public MapLocation enemyWithLowestHP(RobotInfo[] nearbyEnemies) {
        RobotInfo enemyWithLowestHP = null;

        for (RobotInfo enemy : nearbyEnemies) {
            if (enemyWithLowestHP == null) enemyWithLowestHP = enemy;
            else if (enemy.getHealth() < enemyWithLowestHP.getHealth()) enemyWithLowestHP = enemy;
        }
        if (enemyWithLowestHP != null) return enemyWithLowestHP.getLocation();
        return null;
    }
}
