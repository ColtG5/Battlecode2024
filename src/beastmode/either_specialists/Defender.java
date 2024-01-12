package beastmode.either_specialists;

import battlecode.common.*;
import beastmode.Movement;
import beastmode.Utility;

import java.util.Arrays;
import java.util.Comparator;

public class Defender {
    RobotController rc;
    Movement movement;
    Utility utility;

    static final int breadLocOneIndex = 51;
    static final int breadLocTwoIndex = 52;
    static final int breadLocThreeIndex = 53;

    static MapInfo[] aroundBread = null;
    static MapLocation myFlag = null;
    static boolean arraySorted = false;
    static int moveToIndex = 0;

    public Defender(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        moveAroundBread();
        if (enemies.length != 0) tryToPlaceBomb();
    }

    private void moveAroundBread() throws GameActionException {
        MapLocation me = rc.getLocation();
        // Create a array that holds the flags
        MapLocation[] flags = new MapLocation[]{
                utility.intToLocation(rc.readSharedArray(breadLocOneIndex)),
                utility.intToLocation(rc.readSharedArray(breadLocTwoIndex)),
                utility.intToLocation(rc.readSharedArray(breadLocThreeIndex))
        };
        if (aroundBread == null) aroundBread = rc.senseNearbyMapInfos(2);

        // Set flag for the defender to the flag they spawned on
        if (myFlag == null)
            for (MapLocation flag : flags)
                if (me.equals(flag)) {
                    myFlag = flag;
                    break;
                }

        // Sort array in a circular motion if it isn't already sorted
        if (!arraySorted) {
            Arrays.sort(aroundBread, Comparator.comparingDouble(location -> {
                double coordX = location.getMapLocation().x - myFlag.x;
                double coordY = location.getMapLocation().y - myFlag.y;
                return Math.atan2(coordY, coordX);
            }));
            arraySorted = true;
        }

        // Try to move to next location around bread
        MapLocation moveTo = aroundBread[moveToIndex].getMapLocation();
        if (rc.canMove(me.directionTo(moveTo)))
            rc.move(me.directionTo(moveTo));

        // Calculate next location to move to next turn
        moveToIndex = (moveToIndex + 1) % aroundBread.length;
    }

    private void tryToPlaceBomb() throws GameActionException {
        if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation()))
            rc.build(TrapType.EXPLOSIVE, rc.getLocation());
    }
}
