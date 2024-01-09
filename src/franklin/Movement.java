package franklin;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

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


}
