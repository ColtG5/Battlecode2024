package GoldenSon.before_specialists;


import GoldenSon.Movement;
import GoldenSon.Utility;
import battlecode.common.*;

import static GoldenSon.RobotPlayer.*;


public class Scout {
    int localID;
    RobotController rc;
    Movement movement;
    Utility utility;
    boolean isBuilder;
    boolean isBuilderSet = false;

    public Scout(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void setLocalID(int localID) {
        this.localID = localID;
    }

    public void run() throws GameActionException {
        if (!isBuilderSet) {
            isBuilderSet = true;
            isBuilder = localID == FLAGRUNNER_BUILDER_GROUP_1_ID || localID == FLAGRUNNER_BUILDER_GROUP_2_ID || localID == FLAGRUNNER_BUILDER_GROUP_3_ID;
        }

        if (isBuilder && rc.getExperience(SkillType.BUILD) < 30) {
            utility.farmBuildEXP();
        }
        scoutRandomDirection();
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
            Direction dirToNextLocation = rc.getLocation().directionTo(locationGoal);
            MapLocation nextLocation = rc.getLocation().add(dirToNextLocation);
            if (rc.senseMapInfo(nextLocation).isWater()) {
                if (rc.canFill(nextLocation)) {
                    rc.fill(nextLocation);
                    if (rc.canMove(dirToNextLocation)) {
                        rc.move(dirToNextLocation);
                        return;
                    }
                }
            }

            if (!crumbLocated) {
                MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
                if (crumbs.length > 0) {
                    crumbLocated = true;
                    locationGoal = crumbs[0];
                    movement.hardMove(locationGoal);
                    return;
                }
            }
            movement.hardMove(locationGoal);
            if (rc.getLocation().equals(locationGoal)) {
                if (crumbLocated) crumbLocated = false;
                locationGoal = getRandomDirection();
            }
        } else {
            MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
            if (crumbs.length > 0) {
                crumbLocated = true;
                locationGoal = crumbs[0];
                movement.hardMove(locationGoal);
            } else if (mapInfos.length > 0) locationGoal = getRandomDirection();
        }
    }
}
