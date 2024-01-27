package Its_A_NUCLEAR_BOMB_V6.specialists;

import Its_A_NUCLEAR_BOMB_V6.*;
import battlecode.common.*;

public class Scout {
    int localID;
    RobotController rc;
    Movement movement;
    Utility utility;
    BugNav bugnav;
    boolean isBuilder;
    boolean isBuilderSet = false;

    public Scout(RobotController rc, Movement movement, BugNav bugnav, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.bugnav = bugnav;
        this.utility = utility;
    }

    public void setLocalID(int localID) {
        this.localID = localID;
    }

    public void run() throws GameActionException {
        if (!isBuilderSet) {
            isBuilderSet = true;
            isBuilder = utility.amIABuilder();
        }

//        if (isBuilder && rc.getExperience(SkillType.BUILD) < 30) {
//            utility.farmBuildEXP();
//        }
        scoutRandomDirection();

        // after every round whether spawned or not, convert your info to an int and write it to the shared array
        utility.writeMyInfoToSharedArray(false);
    }

    static MapLocation locationGoal = null;
    boolean crumbLocated = false;

    MapLocation getRandomDirection() {
        MapInfo[] mapInfos = rc.senseNearbyMapInfos();
        MapInfo mapPlace = mapInfos[(int) (Math.random() * mapInfos.length)];
        while (!mapPlace.isPassable()) {
            mapPlace = mapInfos[(int) (Math.random() * mapInfos.length)];
        }
        if (mapPlace.isPassable()) return mapPlace.getMapLocation();
        return rc.getLocation();
    }

    private void scoutRandomDirection() throws GameActionException {
        // Choose a random direction, and move that way if possible
        if (rc.getLocation().equals(locationGoal)) {
            if (crumbLocated) crumbLocated = false;
            locationGoal = null;
        }

        MapInfo[] mapInfos = rc.senseNearbyMapInfos();
        if (locationGoal != null) {
            if (!crumbLocated) {
                MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
                if (crumbs.length > 0) {
                    crumbLocated = true;
                    locationGoal = crumbs[0];
                    bugnav.moveTo(locationGoal);
                    return;
                }
            }
            bugnav.moveTo(locationGoal);
            if (rc.getLocation().equals(locationGoal)) {
                if (crumbLocated) crumbLocated = false;
                locationGoal = getRandomDirection();
            }
        } else {
            MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
            if (crumbs.length > 0) {
                crumbLocated = true;
                locationGoal = crumbs[0];
                bugnav.moveTo(locationGoal);
            } else if (mapInfos.length > 0) locationGoal = getRandomDirection();
        }
    }
}
