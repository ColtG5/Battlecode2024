package coltonbotyay;

import battlecode.common.*;

public strictfp class Movement {
    RobotController rc;
    public Movement(RobotController rc) {
        this.rc = rc;
    }

    public boolean simpleMove(MapLocation mapLocation) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(mapLocation);
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else if (rc.canMove(dir.rotateLeft())) {
            rc.move(dir.rotateLeft());
            return true;
        } else if (rc.canMove(dir.rotateRight())) {
            rc.move(dir.rotateRight());
            return true;
        } else if (rc.canMove(dir.rotateLeft().rotateLeft())) {
            rc.move(dir.rotateLeft().rotateLeft());
            return true;
        } else if (rc.canMove(dir.rotateRight().rotateRight())) {
            rc.move(dir.rotateRight().rotateRight());
            return true;
        }
        else if(rc.canMove(dir.rotateLeft().rotateLeft().rotateLeft())){
            rc.move(dir.rotateLeft().rotateLeft().rotateLeft());
            return true;
        }
        else if(rc.canMove(dir.rotateRight().rotateRight().rotateRight())){
            rc.move(dir.rotateRight().rotateRight().rotateRight());
            return true;
        }
        else if(rc.canMove(dir.opposite())){
            rc.move(dir.opposite());
            return true;
        }
        return false;
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
