package franklin;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Stack;

public strictfp class Movement {
    RobotController rc;
    boolean lefty;
    public Movement(RobotController rc, boolean lefty) {
        this.rc = rc;
        this.lefty = lefty;
    }

    public void setLefty(boolean lefty){
        this.lefty = lefty;
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

    public boolean smallMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else if (rc.canMove(dir.rotateLeft())) {
            rc.move(dir.rotateLeft());
            return true;
        } else if (rc.canMove(dir.rotateRight())) {
            rc.move(dir.rotateRight());
            return true;
        }
        return false;

    }
    static Stack<Direction> MovementStack = new Stack<Direction>();

    public Direction rotateOnce(boolean lefty, Direction hype) {
        if (lefty) return hype.rotateLeft();
        else return hype.rotateRight();
    }

    /**
     *
     * @param hype Direction of failed move
     * @return failed to move or not
     * @throws GameActionException I dont fucking know
     */
    public boolean tryNewDirection(Direction hype) throws GameActionException {
        Direction ogDir = hype;
        while(true){
            hype = rotateOnce(lefty, hype);
            if(smallMove(hype)) {
                return true;
            }
            else{
                MapLocation potentialLocation = rc.getLocation().add(hype);
                if (rc.onTheMap(potentialLocation)) MovementStack.push(hype);
                else {
                    MovementStack.clear();
                    return false;
                }
            }
            if(ogDir == hype){
                MovementStack.clear();
//                lefty = !lefty;
                return false;
            }
        }
    }


    public boolean hardMove(MapLocation mapLocation) throws GameActionException {
        rc.setIndicatorString("am i lefty?? " + lefty);
        if(MovementStack.empty()){
            //HYPE is the direction
            Direction hype =rc.getLocation().directionTo(mapLocation);
            if(smallMove(hype)) {
                return true;
            }

            else{
                MovementStack.push(hype);
                if(!tryNewDirection(hype)){
                    return false;
                }
                else{
                    return true;
                }

            }
        }
        else{
            Direction topOfStack = MovementStack.peek();
            if(rc.canMove(topOfStack)){
                Direction last = topOfStack;
                while(rc.canMove(topOfStack)){
                    MovementStack.pop();
                    last = topOfStack;
                    if(MovementStack.empty()) break;
                    topOfStack = MovementStack.peek();
                }
                rc.move(last);
                return true;
            }
            else{
                if(tryNewDirection(topOfStack)){
                    return true;
                }
                else{
                    return false;
                }
            }

        }
    }
}
