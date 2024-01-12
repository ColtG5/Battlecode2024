package beastmode.either_specialists;

import battlecode.common.*;
import beastmode.Movement;
import beastmode.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Defender {
    RobotController rc;
    Movement movement;
    Utility utility;

    static final int breadLocOneIndex = 51;
    static final int breadLocTwoIndex = 52;
    static final int breadLocThreeIndex = 53;

    ArrayList<MapLocation> aroundBread = new ArrayList<>();
    static MapLocation myFlag = null;
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

        // Set flag for the defender to the flag they spawned on
        if (myFlag == null)
            for (MapLocation flag : flags)
                if (me.equals(flag)) {
                    myFlag = flag;
                    break;
                }
        if (myFlag != null) {
            aroundBread.add(myFlag.add(Direction.NORTH));
            aroundBread.add(myFlag.add(Direction.NORTHEAST));
            aroundBread.add(myFlag.add(Direction.EAST));
            aroundBread.add(myFlag.add(Direction.SOUTHEAST));
            aroundBread.add(myFlag.add(Direction.SOUTH));
            aroundBread.add(myFlag.add(Direction.SOUTHWEST));
            aroundBread.add(myFlag.add(Direction.WEST));
            aroundBread.add(myFlag.add(Direction.NORTHWEST));
        }

        // Try to move to next location around bread
        MapLocation moveTo = aroundBread.get(moveToIndex);
        if (rc.canMove(me.directionTo(moveTo)))
            rc.move(me.directionTo(moveTo));

        // Calculate next location to move to next turn
        moveToIndex = (moveToIndex + 1) % aroundBread.size();
    }

    private void tryToPlaceBomb() throws GameActionException {
        if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation()))
            rc.build(TrapType.EXPLOSIVE, rc.getLocation());
    }
}
