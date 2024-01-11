package beastmode;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Utility {
    RobotController rc;
    public Utility(RobotController rc) {
        this.rc = rc;
    }

    private class ThingsAboutRobotFromArray() {
        MapLocation curLocation;
        boolean hasFlag;

        public ThingsAboutRobotFromArray(MapLocation curLocation) throws GameActionException rc {
            this.curLocation = curLocation;
            this.hasFlag = rc.readBroadcast(0);

        }
    }

    public int makeLocalID(int assigningLocalIDIndex) throws GameActionException {
        int localID;
        // for each duck on round one, give it a random local ID
        // (random localID is just for indexing into shared array pretty much)
        int currentID = rc.readSharedArray(assigningLocalIDIndex);
        localID = ++currentID;
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
     * puts a map location to an integer (last 12 bits are x and y, with y the last six bits
     * @param location location to convert
     * @return integer representation of the location
     */
    public int locationToInt(MapLocation location) {
        if (location == null) return 0;
        return (location.x << 6) + location.y;
    }

    /**
     * puts an integer to a map location
     * @param integerLocation int to convert to map location
     * @return map location representation of the int
     */
    public MapLocation intToLocation(int integerLocation) {
        if (integerLocation == 0) return null;
        return new MapLocation(integerLocation >> 6, integerLocation & 63);
    }

    public void writeWhereYouAreToArray(int localID) throws GameActionException {
        int locToStore = locationToInt(rc.getLocation());
        if (rc.canWriteSharedArray(localID, locToStore)) {
            rc.writeSharedArray(localID, locToStore);
        }
    }

    public MapLocation readABotsLocationFromArray(int localID) throws GameActionException {
        int locToRead = rc.readSharedArray(localID);
        return intToLocation(locToRead);
    }


}
