package acooltester;

import battlecode.common.*;

import java.util.Map;
import java.util.Stack;

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
    static Stack<Direction> MovementStack = new Stack<Direction>();

    public Direction rotateOnce(boolean lefty, Direction hype) {
        if (lefty) return hype.rotateLeft();
        else return hype.rotateRight();
    }

    /**
     *
     * @param lefty
     * @param hype Direction of failed move
     * @return failed to move or not
     * @throws GameActionException I dont fucking know
     */
    public boolean tryNewDirection(boolean lefty,Direction hype) throws GameActionException {

        while(true){
            hype = rotateOnce(lefty, hype);
            if(rc.canMove(hype)) {
                rc.move(hype);
                return true;
            }

            else{
                MovementStack.push(hype);
            }
            if(MovementStack.size() == 8){
                MovementStack.clear();
                return false;
            }
        }
    }


    public boolean hardMove(boolean lefty,MapLocation mapLocation) throws GameActionException {
        if(MovementStack.empty()){
            //HYPE is the direction
            Direction hype =rc.getLocation().directionTo(mapLocation);
            if(rc.canMove(hype)){
                rc.move(hype);
                return true;
            }

            else{
                MovementStack.push(hype);
                if(!tryNewDirection(lefty,hype)){
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
                    topOfStack = MovementStack.peek();
                }
                rc.move(last);
                return true;
            }
            else{
                if(tryNewDirection(lefty,topOfStack)){
                    return true;
                }
                else{
                    return false;
                }
            }

        }
    }
}
