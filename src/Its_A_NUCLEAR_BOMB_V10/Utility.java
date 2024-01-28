package Its_A_NUCLEAR_BOMB_V10;

import Its_A_NUCLEAR_BOMB_V10.specialists.Flagrunner;
import battlecode.common.*;

import java.util.ArrayList;
import static Its_A_NUCLEAR_BOMB_V10.RobotPlayer.*;


public class Utility {
    RobotController rc;
    int localID;
    CoolRobotInfo[] coolRobotInfoArray = null;

    public Utility(RobotController rc) {
        this.rc = rc;
    }

    public void setLocalID(int localID) {
        this.localID = localID;
    }
    Direction[] directions = Direction.values();

    public void setCoolRobotInfoArray(CoolRobotInfo[] coolRobotInfoArray) {
        this.coolRobotInfoArray = coolRobotInfoArray;
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

    public void writeMyInfoToSharedArray(boolean isUnderAttack) throws GameActionException {
        MapLocation locationToStore;
        if (rc.isSpawned()) locationToStore = rc.getLocation();
        else locationToStore = NONELOCATION; // NONELOCATION (-1, -1) represents not spawned rn (no location)
        int coolRobotInfoInt = convertRobotInfoToInt(locationToStore, rc.hasFlag(), isUnderAttack);
//      if (rc.getRoundNum() == 1) System.out.println("id: " + localID + "coolRobotInfoInt: " + Integer.toBinaryString(coolRobotInfoInt));
        rc.writeSharedArray(localID, coolRobotInfoInt);
    }

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
        return spawnAreaCenters;
    }

    public MapLocation getClosetSpawnAreaCenter() {
        int closetDistence = spawnAreaCenters[0].distanceSquaredTo(rc.getLocation());
        MapLocation closetSpawnAreaCenter = spawnAreaCenters[0];
        for (MapLocation spawnAreaCenter : spawnAreaCenters) {
            if (spawnAreaCenter.distanceSquaredTo(rc.getLocation()) < closetDistence) {
                closetDistence = spawnAreaCenter.distanceSquaredTo(rc.getLocation());
                closetSpawnAreaCenter = spawnAreaCenter;
            }
        }
        return closetSpawnAreaCenter;
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
        boolean isUnderAttack; // 14th bit

        // lower 3 bits are free rn

        public CoolRobotInfo(int localID, int intOfRobotFromArray) throws GameActionException {
            this.localID = localID;
            // highest 12 bits will be the location, so take highest 12 bits, convert that int into a location, and store the MapLocation into curLocation
            curLocation = intToLocation(intOfRobotFromArray >> 4);
            // 13th bit will be if the robot has a flag, so take the 13th bit, and store it into hasFlag
            hasFlag = ((intOfRobotFromArray >> 3) & 1) == 1;
            isUnderAttack = ((intOfRobotFromArray >> 2) & 1) == 1;
        }

        public boolean getHasFlag() {
            return hasFlag;
        }
        public boolean getIsUnderAttack() {
            return isUnderAttack;
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
    public int convertRobotInfoToInt(MapLocation curLocation, boolean hasFlag, boolean isUnderAttack) {
        int intToReturn = 0;
        intToReturn += locationToInt(curLocation) << 4;
        if (hasFlag) intToReturn += 1 << 3;
        if (isUnderAttack) intToReturn += 1 << 2;
        return intToReturn;
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

//    public int convertFakeIDToLocalID(int fakeID) {
//        int whatGroupAmI = getMyFlagrunnerGroup();
//        if (whatGroupAmI == 1) {
//            return fakeID + 1;
//        } else if (whatGroupAmI == 2) {
//            return fakeID + FLAGRUNNERS_IN_GROUP_1 + 1;
//        } else if (whatGroupAmI == 3) {
//            return fakeID + FLAGRUNNERS_IN_GROUP_1 + FLAGRUNNERS_IN_GROUP_2 + 1;
//        } else {
//            System.out.println("\n\n\n NOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO CONVERTING FAKEID TO LOCALID FAILED \n\n\n");
//            return -9000;
//        }
//    }

//    public int convertLocalIDToFakeID(int localID) {
//        int whatGroupAmI = getMyFlagrunnerGroup();
//        if (whatGroupAmI == 1) {
//            return localID - 1;
//        } else if (whatGroupAmI == 2) {
//            return localID - FLAGRUNNERS_IN_GROUP_1 - 1;
//        } else if (whatGroupAmI == 3) {
//            return localID - FLAGRUNNERS_IN_GROUP_1 - FLAGRUNNERS_IN_GROUP_2 - 1;
//        } else {
//            System.out.println("\n\n\n NOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO CONVERTING LOCALID TO FAKEID FAILED \n\n\n");
//            return -9000;
//        }
//    }

    /**
     * set leader of each group as the lowest ID duck in each group
     *
     * @throws GameActionException if cannot write to shared array
     */
    public void setInitialGroupLeaders() throws GameActionException {
        writeJustLocalIDToFlagrunnerGroupIndex(1, flagRunnerGroupOneIDIndex);
        if ((FLAGRUNNERS_IN_GROUP_1 + 1) <= 47) writeJustLocalIDToFlagrunnerGroupIndex(FLAGRUNNERS_IN_GROUP_1 + 1, flagRunnerGroupTwoIDIndex);
        if ((FLAGRUNNERS_IN_GROUP_1 + FLAGRUNNERS_IN_GROUP_2 + 1) <= 47) writeJustLocalIDToFlagrunnerGroupIndex(FLAGRUNNERS_IN_GROUP_1 + FLAGRUNNERS_IN_GROUP_2 + 1, flagRunnerGroupThreeIDIndex);
//        System.out.println("\t\t\tAHAHAHAH reading the array directly: " + rc.readSharedArray(flagRunnerGroupTwoLocIndex));
    }

    /**
     * checks if you are the leader of a group. since the localID of the group leader is stored as a funky number of
     * 1-16 due to space constraints, we need to convert the localID of the group leader back to the actual localID
     * @return if you are the group leader
     * @throws GameActionException why????????????????????
     */
    public boolean amIAGroupLeader() throws GameActionException {
        return localID == readLocalIDOfGroupLeaderFromFlagrunnerGroupIndex();
    }

    public boolean amIABuilder() {
        if (FLAGRUNNERS_IN_GROUP_1 == 47) { // only one group
            return ((localID == 2) || (localID == 3) || (localID == 4));
        } else if (FLAGRUNNERS_IN_GROUP_1 + FLAGRUNNERS_IN_GROUP_2 == 47) {
            return (localID == 2) || (localID == FLAGRUNNERS_IN_GROUP_1 + 2); // rn we only make two builders for 2 groups, can change
        } else {
            return (localID == 2) || (localID == FLAGRUNNERS_IN_GROUP_1 + 2) || (localID == FLAGRUNNERS_IN_GROUP_1 + FLAGRUNNERS_IN_GROUP_2 + 2);
        }
    }

    public int getMyFlagrunnerGroup() {
        if (localID <= FLAGRUNNERS_IN_GROUP_1) return 1;
        else if (localID <= FLAGRUNNERS_IN_GROUP_1 + FLAGRUNNERS_IN_GROUP_2) return 2;
        else if (localID <= FLAGRUNNERS_IN_GROUP_1 + FLAGRUNNERS_IN_GROUP_2 + FLAGRUNNERS_IN_GROUP_3) return 3;
        else {
            System.out.println("\n\n\n NOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO GETFLAGRUNNERGROUP FAILED \n\n\n");
            return -9000;
        }
    }

    public void writeLocationToDefend(MapLocation loc, int groupOfLeader) throws GameActionException {
        int combined = locationToInt(loc) << 4;
        combined |= 1;
        rc.writeSharedArray(flagRunnerGroupOneLocIndex + (groupOfLeader - 1) * 2, combined);
    }

    public boolean readAmIToDefend() throws GameActionException {
        int combined = rc.readSharedArray(flagRunnerGroupOneLocIndex + (getMyFlagrunnerGroup() - 1) * 2);
        return (combined & 1) == 1;
    }

    public boolean readAmIToDefend(int groupId) throws GameActionException {
        int combined = rc.readSharedArray(flagRunnerGroupOneLocIndex + (groupId - 1) * 2);
        return (combined & 1) == 1;
    }

    public MapLocation getLocationOfMyGroupLeader() throws GameActionException {
        int LocalIDOfGroupLeader = readLocalIDOfGroupLeaderFromFlagrunnerGroupIndex();

        CoolRobotInfo groupLeaderInfo = coolRobotInfoArray[LocalIDOfGroupLeader - 1];
        return groupLeaderInfo.getCurLocation();
    }

    /**
     * checks if that ducks group leader is dead, and if so, makes the duck checking, the new group leader
     *
     * @throws GameActionException if the duck cannot read/write to array
     */
    public void handleIfGroupLeaderDied() throws GameActionException {
        int localIDOfGroupLeader = readLocalIDOfGroupLeaderFromFlagrunnerGroupIndex();
        CoolRobotInfo groupLeaderInfo = coolRobotInfoArray[localIDOfGroupLeader - 1];
        if (groupLeaderInfo.getCurLocation() == NONELOCATION) {
            // if group leader is dead, make ourselves the new group leader
            writeJustLocalIDToFlagrunnerGroupIndex(localID);
//            System.out.println("A GROUP LEADER DIED, REPLACING WITH ME (" + localID + ")");
        }
    }

    /**
     * writes the location of the flagrunner group leader to the shared array, as well as the ID of the duck calling this.
     * so, you make yourself the group leader by calling this
     * @param locationForFlagrunnerGroup loc for the flagrunner group to follow, attack, etc
     */
    public void writeToFlagrunnerGroupIndex(MapLocation locationForFlagrunnerGroup) throws GameActionException {
        // high 12 bits: location
        // low 4 bits: localID of group leader, but this num is only between 1-14, as each group has exactly 14 ducks
//        int intToWrite = locationToInt(locationForFlagrunnerGroup) << 4; // loc in int form, in high 12 bits
//        intToWrite += convertLocalIDToFakeID(localID); // fakeID in lower 4 bits
//        int arrayIndexToWriteTo = flagRunnerGroupIndexingStart + getMyFlagrunnerGroup();
//        rc.writeSharedArray(arrayIndexToWriteTo, intToWrite);

        int locToWrite = locationToInt(locationForFlagrunnerGroup) << 4;
        int arrayIndexToWriteTo = flagRunnerGroupOneLocIndex + ((getMyFlagrunnerGroup() - 1) * 2);
        rc.writeSharedArray(arrayIndexToWriteTo, locToWrite);

        arrayIndexToWriteTo = flagRunnerGroupOneLocIndex + ((getMyFlagrunnerGroup() - 1) * 2) + 1;
        rc.writeSharedArray(arrayIndexToWriteTo, localID);
    }

    /**
     * change the location set for a specific flagrunner group to a new location, without changing the leader
     * @param locationForFlagrunnerGroup new location for the flagrunner group to see
     * @param whichFlagrunnerGroup which group to change the location for
     * @throws GameActionException if cannot write to shared array
     */
    public void writeJustLocationToFlagrunnerGroupIndex(MapLocation locationForFlagrunnerGroup, int whichFlagrunnerGroup) throws GameActionException {
//        int intToWrite = locationToInt(locationForFlagrunnerGroup) << 4; // loc in int form, in high 12 bits
//        // don't overwrite the lower 4 bits of the groupIndex in the shared array, just the higher 12
//        int arrayIndexToWriteTo = flagRunnerGroupIndexingStart + whichFlagrunnerGroup;
//        int currentFakeIDInArray = rc.readSharedArray(arrayIndexToWriteTo) & 15; // 15 is all 1's except the first 4 bits, so this will preserve the lower 4 bits
//        intToWrite += currentFakeIDInArray; // fakeID in lower 4 bits
//        rc.writeSharedArray(arrayIndexToWriteTo, intToWrite);

        int locToWrite = locationToInt(locationForFlagrunnerGroup) << 4;
        int arrayIndexToWriteTo = flagRunnerGroupOneLocIndex + ((getMyFlagrunnerGroup() - 1) * 2);
        rc.writeSharedArray(arrayIndexToWriteTo, locToWrite);
    }

    public void writeJustLocalIDToFlagrunnerGroupIndex(int localIDOfNewLeader) throws GameActionException {
//        int arrayIndexToWriteTo = flagRunnerGroupIndexingStart + getMyFlagrunnerGroup();
//        int intToWrite = rc.readSharedArray(arrayIndexToWriteTo); // this int holds location in high 12 bits, and "fake" localID in low 4 bits. we want to preserve the location, but overwrite the 4 bits of localID
//        intToWrite = intToWrite & 4080; // 4080 is all 1's except the last 4 bits, so this will preserve the location, but make the localID 0
//        intToWrite += convertLocalIDToFakeID(localIDOfNewLeader); // converting the leader localID to a "flagrunner group localID" (1-14)
//        rc.writeSharedArray(arrayIndexToWriteTo, intToWrite);

        int arrayIndexToWriteTo = flagRunnerGroupOneLocIndex + ((getMyFlagrunnerGroup() - 1) * 2) + 1;
        rc.writeSharedArray(arrayIndexToWriteTo, localIDOfNewLeader);
    }

    /**
     * initially, set each group up with a group leader (ONLY time this should be called)
     * @param localIDOfNewLeader localID of new leader
     * @param hardCodedWhatArrIndex hard coded array index
     * @throws GameActionException why????????????????????
     */
    public void writeJustLocalIDToFlagrunnerGroupIndex(int localIDOfNewLeader, int hardCodedWhatArrIndex) throws GameActionException {
//        int intToWrite = rc.readSharedArray(hardCodedWhatArrIndex); // this int holds location in high 12 bits, and "fake" localID in low 4 bits. we want to preserve the location, but overwrite the 4 bits of localID
//        intToWrite = intToWrite & 4080; // 4080 is all 1's except the last 4 bits, so this will preserve the location, but make the localID 0
//        intToWrite += convertLocalIDToFakeID(localIDOfNewLeader); // converting the leader localID to a "flagrunner group localID" (1-14)
//        rc.writeSharedArray(hardCodedWhatArrIndex, intToWrite);
        rc.writeSharedArray(hardCodedWhatArrIndex, localIDOfNewLeader);
    }

    public MapLocation readLocationFromFlagrunnerGroupIndex() throws GameActionException {
//        int arrayIndexToReadFrom = flagRunnerGroupIndexingStart + getMyFlagrunnerGroup();
//        int intToRead = rc.readSharedArray(arrayIndexToReadFrom);
//        return intToLocation(intToRead >> 4);

        int arrayIndexToReadFrom = flagRunnerGroupOneLocIndex + ((getMyFlagrunnerGroup() - 1) * 2);
        return intToLocation(rc.readSharedArray(arrayIndexToReadFrom) >> 4);
    }

    public int[] readAllLocalIDsOfGroupLeaders() throws GameActionException {
        int[] groupLeaderIDs = new int[3];
        int group1 = 1;
        int group2 = 2;
        int group3 = 3;

        groupLeaderIDs[0] = rc.readSharedArray(flagRunnerGroupOneLocIndex + ((group1 - 1) * 2) + 1);
        groupLeaderIDs[1] = rc.readSharedArray(flagRunnerGroupOneLocIndex + ((group2 - 1) * 2) + 1);
        groupLeaderIDs[2] = rc.readSharedArray(flagRunnerGroupOneLocIndex + ((group3 - 1) * 2) + 1);

        return groupLeaderIDs;
    }

    public int readLocalIDOfGroupLeaderFromFlagrunnerGroupIndex() throws GameActionException {
//        int arrayIndexToReadFrom = flagRunnerGroupIndexingStart + getMyFlagrunnerGroup();
//        int fakeLocalIDFromGroupIndex = rc.readSharedArray(arrayIndexToReadFrom) & 15;
//        return convertFakeIDToLocalID(fakeLocalIDFromGroupIndex);

        int arrayIndexToReadFrom = flagRunnerGroupOneLocIndex + ((getMyFlagrunnerGroup() - 1) * 2) + 1;
        return rc.readSharedArray(arrayIndexToReadFrom);
    }

    public void farmBuildEXP() throws GameActionException {
        MapInfo[] infos = rc.senseNearbyMapInfos(-1);
        for (MapInfo info : infos) {
            if (info.isWater()) {
                if (rc.canFill(info.getMapLocation()))
                    rc.fill(info.getMapLocation());
            } else {
                if (rc.canDig(info.getMapLocation()))
                    rc.dig(info.getMapLocation());
            }
        }
    }

    public void spawnDefend() throws GameActionException {
        for (int duckID = 48; duckID <= 50; duckID++) {
            CoolRobotInfo duckDefender = coolRobotInfoArray[duckID - 1];
            if (duckDefender.getIsUnderAttack()) {
                MapLocation closestSpawn = spawnAreaCenters[0];
                for (MapLocation spawn : spawnAreaCenters) {
                    if (duckDefender.getCurLocation().distanceSquaredTo(spawn)
                            < duckDefender.getCurLocation().distanceSquaredTo(closestSpawn))
                        closestSpawn = spawn;
                }
                if (rc.canSpawn(closestSpawn)) {
                    rc.spawn(closestSpawn);
                    break;
                }
                for (Direction dir : directions) {
                    if (rc.canSpawn(closestSpawn.add(dir))) {
                        rc.spawn(closestSpawn.add(dir));
                        break;
                    }
                }
            }
        }
    }

    /**
     * rn, only used to place on your own location when u are at the dam!!! (use the func below instead for flagrunners)
     * @param closestEnemy
     * @throws GameActionException
     */
    public void placeTrapNearEnemy(MapLocation closestEnemy) throws GameActionException {
        MapInfo[] possibleTrapBuildingLocs = rc.senseNearbyMapInfos(GameConstants.INTERACT_RADIUS_SQUARED);
        ArrayList<MapLocation> validPlacements = new ArrayList<>();

        for (MapInfo info : possibleTrapBuildingLocs) {
            boolean noAdjacentTraps = true;
            MapLocation infoLoc = info.getMapLocation();
            if (rc.canBuild(TrapType.STUN, infoLoc)) {
                MapInfo[] adjacentTiles = rc.senseNearbyMapInfos(infoLoc, 2);
                for (MapInfo adjacentTile : adjacentTiles) {
                    if (adjacentTile.getTrapType() != TrapType.NONE) {
                        noAdjacentTraps = false;
                        break;
                    }
                }
                if (noAdjacentTraps) validPlacements.add(infoLoc);
            }
        }

        if (rc.getRoundNum() == 141) {
            System.out.println("validPlacements: " + validPlacements);
        }

        Direction dirToEnemy = rc.getLocation().directionTo(closestEnemy);

        for (MapLocation validPlacement : validPlacements) {
            if (dirToEnemy == rc.getLocation().directionTo(validPlacement)) {
                if (rc.getRoundNum() == 141) System.out.println("TRYNA BUILD HEREEEE straight " + validPlacement);
                if (rc.canBuild(TrapType.STUN, validPlacement)) rc.build(TrapType.STUN, validPlacement);
                return;
            }
        }

        for (MapLocation validPlacement : validPlacements) {
            if ((dirToEnemy.rotateRight() == rc.getLocation().directionTo(validPlacement)) || (dirToEnemy.rotateLeft() == rc.getLocation().directionTo(validPlacement)) ) {
                if (rc.getRoundNum() == 141) System.out.println("TRYNA BUILD HEREEEE rotate once " + validPlacement);
                if (rc.canBuild(TrapType.STUN, validPlacement)) rc.build(TrapType.STUN, validPlacement);
                return;
            }
        }

        boolean isMyLocAValidPlacement = validPlacements.contains(rc.getLocation());
        if (isMyLocAValidPlacement) {
            if (rc.getRoundNum() == 141) System.out.println("TRYNA BUILD HEREEEE me " + rc.getLocation());
            if (rc.canBuild(TrapType.STUN, rc.getLocation())) rc.build(TrapType.STUN, rc.getLocation());
            return;
        }

        for (MapLocation validPlacement : validPlacements) {
            if ((dirToEnemy.rotateRight().rotateRight() == rc.getLocation().directionTo(validPlacement)) || (dirToEnemy.rotateLeft().rotateLeft() == rc.getLocation().directionTo(validPlacement)) ) {
                if (rc.getRoundNum() == 141) System.out.println("TRYNA BUILD HEREEEE rotate twice " + validPlacement);
                if (rc.canBuild(TrapType.STUN, validPlacement)) rc.build(TrapType.STUN, validPlacement);
                return;
            }
        }

    }

    public void placeTrapNearEnemySingleLoc(MapLocation potentialSpotForTrap) throws GameActionException {
        boolean noAdjacentTraps = true;
        if (rc.canBuild(TrapType.STUN, potentialSpotForTrap)) {
            MapInfo[] adjacentTiles = rc.senseNearbyMapInfos(potentialSpotForTrap, 2);
            for (MapInfo adjacentTile : adjacentTiles) {
                if (adjacentTile.getTrapType() != TrapType.NONE) {
                    noAdjacentTraps = false;
                    break;
                }
            }
        }
        if (!noAdjacentTraps) return;
        rc.build(TrapType.STUN, potentialSpotForTrap);
    }

    Direction[] dirs = Direction.values();
    MapLocation currentLoc = null;
    public void placeBestTrap(RobotInfo[] enemies) throws GameActionException {
        if (!rc.isActionReady()) return;
        if (rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) return;
        if (enemies.length < 3) return;

        MapInfo[] allStunsAroundMe = rc.senseNearbyMapInfos(8);

        for (MapInfo info : allStunsAroundMe) {
            if (info.getTrapType() == TrapType.STUN) return;
        }

        TrapInfo[] trapInfo = new TrapInfo[9];
        for (int i = 0; i < 9; i++) trapInfo[i] = new TrapInfo(dirs[i]);
        RobotInfo[] enemies1 = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo robot : enemies1) {
            currentLoc = robot.location;
            trapInfo[0].update();
            trapInfo[1].update();
            trapInfo[2].update();
            trapInfo[3].update();
            trapInfo[4].update();
            trapInfo[5].update();
            trapInfo[6].update();
            trapInfo[7].update();
            trapInfo[8].update();
        }

        TrapInfo bestTrap = trapInfo[8];
        for (int i = 0; i < 8; ++i) {
            if (trapInfo[i].isBetter(bestTrap)) bestTrap = trapInfo[i];
//            if (rc.getRoundNum() == 243 && rc.getID() == 10087)System.out.println();
        }

        if (rc.canBuild(TrapType.STUN, bestTrap.location))
            rc.build(TrapType.STUN, bestTrap.location);
    }

    class TrapInfo {
        Direction dir;
        MapLocation location;
        int enemiesInRange = 0;

        public TrapInfo(Direction dir) {
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
        }

        void update() {
            if (!rc.canBuild(TrapType.STUN, location)) return;
            int dist = location.distanceSquaredTo(currentLoc);
            if (dist <= 13) enemiesInRange++;
        }

        boolean isBetter(TrapInfo T) {
            return enemiesInRange > T.enemiesInRange;
        }
    }

    public void placeTrapNearEnemies(RobotInfo[] closestEnemiesToTrap) throws GameActionException {
        if (!rc.isActionReady()) return;
        if (rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) return;
        if (closestEnemiesToTrap.length < 3) return;

        MapInfo[] possibleTrapBuildingLocs = rc.senseNearbyMapInfos(GameConstants.INTERACT_RADIUS_SQUARED);
        MapInfo[] allStunsAroundMe = rc.senseNearbyMapInfos(8);
//        ArrayList<MapLocation> validPlacements = new ArrayList<>();

        for (MapInfo info : allStunsAroundMe) {
            if (info.getTrapType() == TrapType.STUN) return;
        }

//        for (MapInfo info : possibleTrapBuildingLocs) {
//            MapLocation infoLoc = info.getMapLocation();
//            validPlacements.add(infoLoc);
//        }

        int minDist = 1000000;
        Direction bestDir = null;
        for (RobotInfo enemy : closestEnemiesToTrap) {
            MapLocation enemyLoc = enemy.getLocation();

            for (MapInfo info : possibleTrapBuildingLocs) {
                MapLocation loc = info.getMapLocation();
                Direction dir = loc.directionTo(enemyLoc);
                boolean canBuild = rc.canBuild(TrapType.STUN, loc);
                int dist = loc.distanceSquaredTo(enemyLoc);

                if (bestDir == null || (dist < minDist && canBuild)) {
                    bestDir = dir;
                    minDist = dist;
                }
            }
        }

        if (bestDir != null && rc.canBuild(TrapType.STUN, rc.getLocation().add(bestDir))) {
            rc.canBuild(TrapType.STUN, rc.getLocation().add(bestDir));
        } else {
            if (rc.canBuild(TrapType.STUN, rc.getLocation()))
                rc.build(TrapType.STUN, rc.getLocation());
        }

        // maybe try rotating left or right before building on own loc?


//            for (MapLocation validPlacement : validPlacements) {
//                if (dirToEnemy == rc.getLocation().directionTo(validPlacement)) {
//                    if (rc.canBuild(TrapType.STUN, validPlacement)) {
//                        rc.build(TrapType.STUN, validPlacement);
//                        return;
//                    }
//                }
//            }

//            for (MapLocation validPlacement : validPlacements) {
//                if ((dirToEnemy.rotateRight() == rc.getLocation().directionTo(validPlacement)) || (dirToEnemy.rotateLeft() == rc.getLocation().directionTo(validPlacement))) {
//                    if (rc.canBuild(TrapType.STUN, validPlacement)) {
//                        rc.build(TrapType.STUN, validPlacement);
//                        return;
//                    }
//                }
//            }
//        }
    }

    /**
     * used to return two things from a func (idk a better way rn sorry)
     * @param <T>
     */
    public static class MyPair<T, U> {
        private final T pair_first;
        private final U pair_second;

        public MyPair(T first, U second) {
            pair_first = first;
            pair_second = second;
        }

        public T first() {
            return pair_first;
        }

        public U second() {
            return pair_second;
        }
    }
}