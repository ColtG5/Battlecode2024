package GoldenSon;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class Movement {
    static final Random rng = new Random(6147);
    RobotController rc;
    boolean lefty;
    public Movement(RobotController rc, boolean lefty) {
        this.rc = rc;
        this.lefty = lefty;
    }

    public void setLefty(boolean lefty){
        this.lefty = lefty;
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

    public boolean smallMove(Direction dir, ArrayList<MapLocation> mapLocations) throws GameActionException {
        if (rc.canMove(dir) && !mapLocations.contains(rc.getLocation().add(dir))) {
            rc.move(dir);
            return true;
        } else if (rc.canMove(dir.rotateLeft()) && !mapLocations.contains(rc.getLocation().add(dir.rotateLeft()))) {
            rc.move(dir.rotateLeft());
            return true;
        } else if (rc.canMove(dir.rotateRight()) && !mapLocations.contains(rc.getLocation().add(dir.rotateRight()))) {
            rc.move(dir.rotateRight());
            return true;
        }
        return false;

    }
    static Stack<Direction> MovementStack = new Stack<>();

    public Direction rotateOnce(boolean lefty, Direction hype) {
        if (lefty) return hype.rotateLeft();
        else return hype.rotateRight();
    }

    /**
     * @param hype Direction of failed move
     * @return failed to move or not
     * @throws GameActionException I dont fucking know
     */
    public boolean tryNewDirection(Direction hype) throws GameActionException {
        Direction ogDir = hype;
        while (true) {
            hype = rotateOnce(lefty, hype);
            if(smallMove(hype)) {
                return true;
            } else {
                MapLocation potentialLocation = rc.getLocation().add(hype);
                if (rc.onTheMap(potentialLocation)) MovementStack.push(hype);
                else {
                    MovementStack.clear();
                    return false;
                }
            }
            if(ogDir == hype) {
                MovementStack.clear();
                return false;
            }
        }
    }
    public boolean tryNewDirection(Direction hype, ArrayList<MapLocation> mapLocations) throws GameActionException {
        Direction ogDir = hype;
        while (true) {
            hype = rotateOnce(lefty, hype);
            if(!mapLocations.contains(rc.getLocation().add(hype))){
                if(smallMove(hype)) {
                    return true;
                }
            }
             else {
                MapLocation potentialLocation = rc.getLocation().add(hype);
                if (rc.onTheMap(potentialLocation)) MovementStack.push(hype);
                else {
                    MovementStack.clear();
                    return false;
                }
            }
            if(ogDir == hype) {
                MovementStack.clear();
                return false;
            }
        }
    }
    /**
     * @param mapLocation Location to move to
     * @return failed to move or not
     * @throws GameActionException I dont fucking know
     */
    public boolean hardMove(MapLocation mapLocation) throws GameActionException {
        if (!rc.isMovementReady()) return false;

        if (MovementStack.empty()) {
            //HYPE is the direction
            Direction hype = rc.getLocation().directionTo(mapLocation);

            if (rc.canFill(rc.getLocation().add(hype))) rc.fill(rc.getLocation().add(hype));

            if (smallMove(hype)) return true;
            else {
                MovementStack.push(hype);
                return tryNewDirection(hype);
            }
        } else {
            Direction topOfStack = MovementStack.peek();
            if (rc.canMove(topOfStack)){
                Direction last = topOfStack;
                while (rc.canMove(topOfStack)) {
                    MovementStack.pop();
                    last = topOfStack;
                    if (MovementStack.empty()) break;
                    topOfStack = MovementStack.peek();
                }
                rc.move(last);
                return true;
            }
            else return tryNewDirection(topOfStack);
        }
    }

    public boolean hardMove(MapLocation mapLocation, ArrayList<MapLocation> arrayList) throws GameActionException {
        if (!rc.isMovementReady()) return false;

        if (MovementStack.empty()) {
            //HYPE is the direction
            Direction hype = rc.getLocation().directionTo(mapLocation);
            if(!arrayList.contains(rc.getLocation().add(hype))){
                if (smallMove(hype, arrayList)) return true;
                else {
                    MovementStack.push(hype);
                    return tryNewDirection(hype, arrayList);
                }
            }
            else {
                MovementStack.push(hype);
                return tryNewDirection(hype, arrayList);
            }
        } else {
            Direction topOfStack = MovementStack.peek();
            if (rc.canMove(topOfStack) && !arrayList.contains(rc.getLocation().add(topOfStack))){
                Direction last = topOfStack;
                while (rc.canMove(topOfStack) &&!arrayList.contains(rc.getLocation().add(topOfStack))) {
                    MovementStack.pop();
                    last = topOfStack;
                    if (MovementStack.empty()) break;
                    topOfStack = MovementStack.peek();
                }
                rc.move(last);
                return true;
            }
            else return tryNewDirection(topOfStack);
        }
    }

    public void moveTowardsEnemyFlags() throws GameActionException {
        MapLocation[] potentialFlags = rc.senseBroadcastFlagLocations();
        if (potentialFlags.length != 0) {
            MapLocation flag = potentialFlags[rng.nextInt(potentialFlags.length)];
            if (flag != null) hardMove(flag);
        }
    }
}
