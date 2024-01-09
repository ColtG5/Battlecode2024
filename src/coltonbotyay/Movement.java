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
        Direction originalDir = rc.getLocation().directionTo(mapLocation);
        MapInfo mapInfo=rc.senseMapInfo(rc.getLocation().add(originalDir));
        if(mapInfo.isPassable() && originalDir != priorLocation.directionTo(rc.getLocation()).opposite()){
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
