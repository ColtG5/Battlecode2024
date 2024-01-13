package beastmode.either_specialists;

import battlecode.common.*;
import beastmode.Movement;
import beastmode.Utility;

import java.util.ArrayList;

public class Defender {
    RobotController rc;
    Movement movement;
    Utility utility;
    int localID;

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

    public void setLocalID(int localID) {
        this.localID = localID;
    }

    public void run() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length != 0) tryToPlaceBomb(enemies);
        moveAroundBread();
        RobotInfo[] enemies2 = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies2.length != 0) {
            MapLocation enemy = utility.enemyWithLowestHP(enemies2);
            if (rc.canAttack(enemy)) rc.attack(enemy);
            tryToPlaceBomb(enemies2);
        }
    }

    /**
     * Make the defender move in circles around the flag
     * @throws GameActionException
     */
    private void moveAroundBread() throws GameActionException {
        MapLocation me = rc.getLocation();
        // Create a array that holds the flags
        MapLocation[] flags = new MapLocation[]{
                utility.intToLocation(rc.readSharedArray(breadLocOneIndex)),
                utility.intToLocation(rc.readSharedArray(breadLocTwoIndex)),
                utility.intToLocation(rc.readSharedArray(breadLocThreeIndex))
        };

//        // Set flag for the defender to the flag they spawned on
//        if (myFlag == null) {
//            for (MapLocation flag : flags) {
//                if (me.equals(flag)) {
//                    myFlag = flag;
//                    break;
//                }
//            }
//        }
        if (myFlag == null) {
            myFlag = utility.intToLocation(rc.readSharedArray(localID + 3));
        }

        if (rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
            FlagInfo[] nearbyFlags = rc.senseNearbyFlags(2, rc.getTeam());
            for (FlagInfo flag : nearbyFlags) {
                MapLocation stolenFlag = flag.getLocation();
                if (!stolenFlag.equals(myFlag)) {
                    rc.setIndicatorString("TRYING TO GET FLAG BACK");
                    movement.hardMove(stolenFlag);
                    if (rc.canAttack(stolenFlag)) rc.attack(stolenFlag);
                    // If killed the duck, then try and pick up the flag
                    if (rc.canPickupFlag(stolenFlag)) rc.pickupFlag(stolenFlag);
                }
            }
        }

        // If we found a flag, add the 8 locations around it
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

    /**
     * Attempt to place a bomb at current location
     */
    private void tryToPlaceBomb(RobotInfo[] enemies) throws GameActionException {
        MapLocation closestEnemy = closestEnemyToFlag(enemies);
        Direction dir = null;
        if (closestEnemy != null) dir = myFlag.directionTo(closestEnemy);

        if (dir != null && rc.canBuild(TrapType.EXPLOSIVE, myFlag.add(dir)))
            rc.build(TrapType.EXPLOSIVE, myFlag.add(dir));

        rc.setIndicatorString("Tried to place bomb");
    }


    /**
     * Try to spawn the defender on or around the flag when he tries to respawn
     * @throws GameActionException
     */
    public void tryToSpawnOnFlag() throws GameActionException {
        // If we can spawn on flag, spawn there
        if (rc.canSpawn(myFlag)) rc.spawn(myFlag);
        // Otherwise we try to spawn around the flag
        else {
            for (MapLocation location : aroundBread) {
                if (rc.canSpawn(location)) rc.spawn(location);
            }
        }
    }

    public MapLocation closestEnemyToFlag(RobotInfo[] nearbyEnemies) {
        RobotInfo closestEnemy = null;

        for (RobotInfo enemy : nearbyEnemies) {
            if (closestEnemy == null) closestEnemy = enemy;
            else if (myFlag.distanceSquaredTo(enemy.getLocation()) < myFlag.distanceSquaredTo(closestEnemy.getLocation()))
                closestEnemy = enemy;
        }
        if (closestEnemy != null) return closestEnemy.getLocation();
        return null;
    }
}
