package Goob_final;

import battlecode.common.*;

import java.util.ArrayList;

public class BugNav {
    RobotController rc;
    int H, W;

    BugNav(RobotController rc) {
        this.rc = rc;
        this.H = rc.getMapHeight();
        this.W = rc.getMapWidth();
        states = new int[W][];
    }

    final int MIN_DISTANCE_RESET = 3;
    final int MAX_TURNS_MOVING_TO_OBSTACLE = 2;
    final int INFINITY = 1000000000;

    Direction[] directions = Direction.values();
    boolean[] canMoveArray;
    MapLocation prevTarget = null;
    MapLocation lastObstacle = null;
    MapLocation minLocationToTarget = null;
    MapLocation myLoc;
    ArrayList<MapLocation> blacklistLocs;
    int minDistToTarget = INFINITY;
    int turnsMovingToObstacle = 0;
    Boolean shouldIRotateRight = null;

    int[][] states;
    int bugPathIndex = 0;
    int stateIndex = 0;
    boolean isReady(){
        return stateIndex >= W;
    }

    void fill() {
        while(stateIndex < W) {
            if (Clock.getBytecodesLeft() < 1000) return;
            states[stateIndex++] = new int[H];
        }
    }

    void update() {
        if (!rc.isMovementReady()) return;
        myLoc = rc.getLocation();
        generateArray();
    }

    void generateArray() {
        canMoveArray = new boolean[9];
        for (Direction dir : directions) {
            canMoveArray[dir.ordinal()] = rc.canMove(dir);
        }

        if (blacklistLocs == null || blacklistLocs.isEmpty()) return;
        for (MapLocation loc : blacklistLocs) {
            Direction dir = myLoc.directionTo(loc);
            canMoveArray[dir.ordinal()] = false;
        }
    }

    public void moveTo(MapLocation target) throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (target == null) target = rc.getLocation();

        // Fill water
        MapInfo[] mapInfo = rc.senseNearbyMapInfos(GameConstants.INTERACT_RADIUS_SQUARED);
        for (MapInfo info : mapInfo) {
            MapLocation loc = info.getMapLocation();
            if ((loc.x + loc.y) % 2 != 0 && info.isWater()) {
                if (rc.canFill(loc)) rc.fill(loc);
            }
        }

        update();

        if (prevTarget == null) {
            resetPathfinding();
            shouldIRotateRight = null;
        } else {
            int distance = target.distanceSquaredTo(prevTarget);
            if (distance > 0) {
                if (distance >= MIN_DISTANCE_RESET) {
                    shouldIRotateRight = null;
                    resetPathfinding();
                } else softReset(target);
            }
        }

        prevTarget = target;

        checkState();
        myLoc = rc.getLocation();

        int distance = myLoc.distanceSquaredTo(target);
        if (distance == 0) return;

        if (distance < minDistToTarget) {
            resetPathfinding();
            minDistToTarget = distance;
            minLocationToTarget = myLoc;
        }

        Direction dir = myLoc.directionTo(target);
        // If no obstacle around, just move to target
        if (lastObstacle == null) {
            if (tryGreedyMove()) {
                resetPathfinding();
                return;
            }
        } else dir = myLoc.directionTo(lastObstacle);

        if (canMoveArray[dir.ordinal()]) {
            move(dir);
            if (lastObstacle != null) {
                ++turnsMovingToObstacle;
                lastObstacle = rc.getLocation().add(dir);
                if (turnsMovingToObstacle >= MAX_TURNS_MOVING_TO_OBSTACLE){
                    resetPathfinding();
                } else if (!rc.onTheMap(lastObstacle)){
                    resetPathfinding();
                }
            }
            return;
        } else turnsMovingToObstacle = 0;

        checkRotate(dir);

        int i = 16;
        while (i-- > 0) {
            if (canMoveArray[dir.ordinal()]) {
                move(dir);
                return;
            }
            MapLocation newLoc = myLoc.add(dir);
            if (!rc.onTheMap(newLoc)) shouldIRotateRight = !shouldIRotateRight;
                //If I could not go in that direction, and it was not outside the map, then this is the latest obstacle found
            else lastObstacle = newLoc;
            if (shouldIRotateRight) dir = dir.rotateRight();
            else dir = dir.rotateLeft();
        }

        if (canMoveArray[dir.ordinal()]) move(dir);
    }

    public void moveTo(MapLocation target, ArrayList<MapLocation> blacklist) throws GameActionException {
        blacklistLocs = blacklist;
        moveTo(target);
    }


    boolean tryGreedyMove() throws GameActionException{
        MapLocation myLoc = rc.getLocation();
        Direction dir = myLoc.directionTo(prevTarget);
        if (canMoveArray[dir.ordinal()]) {
            move(dir);
            return true;
        }

        int dist = myLoc.distanceSquaredTo(prevTarget);
        int dist1 = INFINITY, dist2 = INFINITY;
        Direction dir1 = dir.rotateRight();
        MapLocation newLoc = myLoc.add(dir1);

        if (canMoveArray[dir1.ordinal()]) dist1 = newLoc.distanceSquaredTo(prevTarget);

        Direction dir2 = dir.rotateLeft();
        newLoc = myLoc.add(dir2);

        if (canMoveArray[dir2.ordinal()]) dist2 = newLoc.distanceSquaredTo(prevTarget);
        if (dist1 < dist && dist1 < dist2) {
            move(dir1);
            return true;
        }
        if (dist2 < dist && dist2 < dist1) {
            move(dir2);
            return true;
        }
        return false;
    }

    void checkRotate(Direction dir){
        if (shouldIRotateRight != null) return;
        Direction dirLeft = dir;
        Direction dirRight = dir;
        int i = 8;
        while (--i >= 0) {
            if (!canMoveArray[dirLeft.ordinal()]) dirLeft = dirLeft.rotateLeft();
            else break;
        }
        i = 8;
        while (--i >= 0){
            if (!canMoveArray[dirRight.ordinal()]) dirRight = dirRight.rotateRight();
            else break;
        }
        int distLeft = myLoc.add(dirLeft).distanceSquaredTo(prevTarget), distRight = myLoc.add(dirRight).distanceSquaredTo(prevTarget);
        shouldIRotateRight = distRight <= distLeft;
    }

    void move(Direction dir) throws GameActionException {
        if (dir == Direction.CENTER) return;

        rc.move(dir);
    }

    void resetPathfinding() {
        lastObstacle = null;
        minDistToTarget = INFINITY;
        ++bugPathIndex;
        turnsMovingToObstacle = 0;
    }

    void softReset(MapLocation target) {
        if (minLocationToTarget != null) minDistToTarget = minLocationToTarget.distanceSquaredTo(target);
        else resetPathfinding();
    }


    void checkState(){
        if (!isReady()) return;
        if (lastObstacle == null) return;
        int state = (bugPathIndex << 14) | (lastObstacle.x << 8) |  (lastObstacle.y << 2);
        if (shouldIRotateRight != null) {
            if (shouldIRotateRight) state |= 1;
            else state |= 2;
        }
        if (states[myLoc.x][myLoc.y] == state) resetPathfinding();
        states[myLoc.x][myLoc.y] = state;
    }
}
