package beastmode;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static beastmode.RobotPlayer.NONELOCATION;

public class Utility {
    RobotController rc;
    public Utility(RobotController rc) {
        this.rc = rc;
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

    public boolean trySpawning() throws GameActionException {
        if (rc.isSpawned()) return false;
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        for (MapLocation loc : spawnLocs) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                return true;
            }
        }
        return false;
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
    }

    /**
     * call this one when u are converting ur own into to an int
     * @param curLocation robots current location
     * @param hasFlag have flag
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
     * @param coolRobotInfo coolRobotInfo to convert
     * @return an int of the robots info that can be stored into the array
     */
    public int convertRobotInfoToInt(CoolRobotInfo coolRobotInfo) {
        return convertRobotInfoToInt(coolRobotInfo.curLocation, coolRobotInfo.hasFlag);
    }

    /**
     * puts a map location to an integer (last 12 bits are x and y, with y the last six bits
     * @param location location to convert
     * @return integer representation of the location
     */
    public int locationToInt(MapLocation location) {
        if (location.equals(NONELOCATION)) return 4095; // 4095 is all 1's, representing no location
        return (location.x << 6) + location.y;
    }

    /**
     * puts an integer to a map location
     * @param integerLocation int to convert to map location
     * @return map location representation of the int
     */
    public MapLocation intToLocation(int integerLocation) {
        if (integerLocation == 4095) return NONELOCATION; // 4095 is all 1's, representing a null location
        return new MapLocation(integerLocation >> 6, integerLocation & 63);
    }

//    public void writeWhereYouAreToArray(int localID) throws GameActionException {
//        int locToStore = locationToInt(rc.getLocation());
//        if (rc.canWriteSharedArray(localID, locToStore)) {
//            rc.writeSharedArray(localID, locToStore);
//        }
//    }
//
//    public MapLocation readABotsLocationFromArray(int localID) throws GameActionException {
//        int locToRead = rc.readSharedArray(localID);
//        return intToLocation(locToRead);
//    }


}
