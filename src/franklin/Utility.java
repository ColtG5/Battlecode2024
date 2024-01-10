package franklin;

import battlecode.common.*;

public strictfp class Utility {
    RobotController rc;
    public Utility(RobotController rc) {
        this.rc = rc;
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

    public void trySpawning() throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        for (MapLocation loc : spawnLocs) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                break;
            }
        }
    }
}
