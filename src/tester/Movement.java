package tester;

import battlecode.common.*;

public strictfp class Movement {
    RobotController rc;
    MapLocation prevLoc;
    public Movement(RobotController rc) {
        this.rc = rc;
    }

    private boolean simpleMoveHelper(Direction dir) throws GameActionException {
        boolean moved = false;
        // get the maplocation that moving this dir would yield us
        MapLocation loc = rc.getLocation().add(dir);
        if (!prevLoc.equals(loc)) { // only move if u aren't moving onto the previous spot
            rc.move(dir);
            moved = true;
            prevLoc = rc.getLocation();
        }
        return moved;
    }

    public boolean simpleMove(MapLocation mapLocation) throws GameActionException {
        boolean moved = false;
        System.out.println(rc.getLocation());
        System.out.println(mapLocation);
        Direction dir = rc.getLocation().directionTo(mapLocation);
        if (rc.canMove(dir)) {
            // get the map loc that moving this way would yield us
            MapLocation loc = rc.getLocation().add(dir);
            if (!prevLoc.equals(loc)) { // only move if u aren't moving onto the previous spot
                rc.move(dir);
                moved = simpleMoveHelper(dir);
            }
        } else if (rc.canMove(dir.rotateLeft())) {
            // get the map loc that moving this way would yield us
            MapLocation loc = rc.getLocation().add(dir.rotateLeft());
            if (!prevLoc.equals(loc)) { // only move if u aren't moving onto the previous spot
                rc.move(dir.rotateLeft());
                moved = simpleMoveHelper(dir.rotateLeft());
            }
        } else if (rc.canMove(dir.rotateRight())) {
            // get the map loc that moving this way would yield us
            MapLocation loc = rc.getLocation().add(dir.rotateRight());
            if (!prevLoc.equals(loc)) { // only move if u aren't moving onto the previous spot
                rc.move(dir.rotateRight());
                moved = simpleMoveHelper(dir.rotateRight());
            }
        } else if (rc.canMove(dir.rotateLeft().rotateLeft())) {
            // get the map loc that moving this way would yield us
            MapLocation loc = rc.getLocation().add(dir.rotateLeft().rotateLeft());
            if (!prevLoc.equals(loc)) { // only move if u aren't moving onto the previous spot
                rc.move(dir.rotateLeft().rotateLeft());
                moved = simpleMoveHelper(dir.rotateLeft().rotateLeft());
            }
        } else if (rc.canMove(dir.rotateRight().rotateRight())) {
            // get the map loc that moving this way would yield us
            MapLocation loc = rc.getLocation().add(dir.rotateRight().rotateRight());
            if (!prevLoc.equals(loc)) { // only move if u aren't moving onto the previous spot
                rc.move(dir.rotateRight().rotateRight());
                moved = simpleMoveHelper(dir.rotateRight().rotateRight());
            }
        } else if (rc.canMove(dir.rotateLeft().rotateLeft().rotateLeft())){
            // get the map loc that moving this way would yield us
            MapLocation loc = rc.getLocation().add(dir.rotateLeft().rotateLeft().rotateLeft());
            if (!prevLoc.equals(loc)) { // only move if u aren't moving onto the previous spot
                rc.move(dir.rotateLeft().rotateLeft().rotateLeft());
                moved = simpleMoveHelper(dir.rotateLeft().rotateLeft().rotateLeft());
            }
        } else if(rc.canMove(dir.rotateRight().rotateRight().rotateRight())){
            // get the map loc that moving this way would yield us
            MapLocation loc = rc.getLocation().add(dir.rotateRight().rotateRight().rotateRight());
            if (!prevLoc.equals(loc)) { // only move if u aren't moving onto the previous spot
                rc.move(dir.rotateRight().rotateRight().rotateRight());
                moved = simpleMoveHelper(dir.rotateRight().rotateRight().rotateRight());
            }
        }
        rc.setIndicatorString("moved: " + moved);
        return moved;
    }
    static MapLocation priorLocation;


    public boolean hardMove(MapLocation mapLocation) throws GameActionException {
        // direction we want to move to, to get to end goal
        Direction originalDir = rc.getLocation().directionTo(mapLocation);
        // get map info of the location in the direction to end goal
        MapInfo mapInfo=rc.senseMapInfo(rc.getLocation().add(originalDir));
        // if the loc we tryna move to is passable, and we arent moving backwards, then move there
        if(mapInfo.isPassable() && originalDir != priorLocation.directionTo(rc.getLocation()).opposite()){
            // if fuzzy move is able to move to the location, set priorLocation to current location
            if(simpleMove(mapInfo.getMapLocation())){
                priorLocation=mapInfo.getMapLocation();
                if(rc.getLocation() == mapLocation){
                    priorLocation=null;
                }
                return true;
            }
            else{
                return false;
            }

        }
        else{
            //make rc follow impassable terrain
            Direction direction =priorLocation.directionTo(rc.getLocation());


        }
        return false;
    }
}
