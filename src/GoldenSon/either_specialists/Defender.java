package GoldenSon.either_specialists;

import battlecode.common.*;
import GoldenSon.Movement;
import GoldenSon.Utility;
import GoldenSon.RobotPlayer.*;

import java.util.ArrayList;

public class Defender {
    RobotController rc;
    Movement movement;
    Utility utility;
    int localID;
    MapLocation[] spawnAreaCenters;
    static MapLocation myBreadIDefendForMyLife = null;

    static final int breadLocOneIndex = 51;
    static final int breadLocTwoIndex = 52;
    static final int breadLocThreeIndex = 53;

    ArrayList<MapLocation> aroundBread = new ArrayList<>();
    static int moveToIndex = 0;

    public Defender(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void setSpawnAreaCenters(MapLocation[] spawnAreaCenters) {
        this.spawnAreaCenters = spawnAreaCenters;
    }

    public void setLocalID(int localID) {
        this.localID = localID;
    }

    public void run() throws GameActionException {
        // yes its hardcoded, i dont care !!!!!
        if (rc.getRoundNum() == 2) {
            int whichBread = (localID % 3) + 1;
            myBreadIDefendForMyLife = spawnAreaCenters[whichBread-1];
            System.out.println("MA BREAD::: " + myBreadIDefendForMyLife);
        }

        if (rc.getExperience(SkillType.BUILD) < 30 && rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) {
            farmEXP();
        } else {
            movement.hardMove(myBreadIDefendForMyLife);
        }
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length != 0) tryToPlaceBomb(enemies);
//        moveAroundBread();
        RobotInfo[] enemies2 = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies2.length != 0) {
            MapLocation enemy = utility.enemyWithLowestHP(enemies2);
            if (rc.canAttack(enemy)) rc.attack(enemy);
            tryToPlaceBomb(enemies2);
        }
        System.out.println(myBreadIDefendForMyLife);
    }

    /**
     * Make the defender move in circles around the flag
     * @throws GameActionException
     */
//    private void moveAroundBread() throws GameActionException {
//        MapLocation me = rc.getLocation();
//        // Create a array that holds the flags
//        MapLocation[] flags = new MapLocation[]{
//                utility.intToLocation(rc.readSharedArray(breadLocOneIndex)),
//                utility.intToLocation(rc.readSharedArray(breadLocTwoIndex)),
//                utility.intToLocation(rc.readSharedArray(breadLocThreeIndex))
//        };
//
//        // Set flag for the defender to the flag they spawned on
//        if (myFlag == null) {
//            for (MapLocation flag : flags) {
//                if (me.equals(flag)) {
//                    myFlag = flag;
//                    break;
//                }
//            }
//        }
//
////        if (rc.getRoundNum() == 169) { // dont run this code before u are around ur designated flag, or else u will think one of our other flags is a stolen flag !!!
////            FlagInfo[] nearbyFlags = rc.senseNearbyFlags(2, rc.getTeam());
////            for (FlagInfo flag : nearbyFlags) {
////                MapLocation stolenFlag = flag.getLocation();
////                if (!stolenFlag.equals(myFlag)) {
////                    rc.setIndicatorString("TRYING TO GET FLAG BACK");
////                    movement.hardMove(stolenFlag);
////                    if (rc.canAttack(stolenFlag)) rc.attack(stolenFlag);
////                    // If killed the duck, then try and pick up the flag
////                    if (rc.canPickupFlag(stolenFlag)) rc.pickupFlag(stolenFlag);
////                }
////            }
////        }
//
//        // If we found a flag, add the 8 locations around it
//        if (myFlag != null) {
//            aroundBread.add(myFlag.add(Direction.NORTH));
//            aroundBread.add(myFlag.add(Direction.NORTHEAST));
//            aroundBread.add(myFlag.add(Direction.EAST));
//            aroundBread.add(myFlag.add(Direction.SOUTHEAST));
//            aroundBread.add(myFlag.add(Direction.SOUTH));
//            aroundBread.add(myFlag.add(Direction.SOUTHWEST));
//            aroundBread.add(myFlag.add(Direction.WEST));
//            aroundBread.add(myFlag.add(Direction.NORTHWEST));
//        }
//
//        // Try to move to next location around bread
//        MapLocation moveTo = aroundBread.get(moveToIndex);
//        if (rc.canMove(me.directionTo(moveTo)))
//            rc.move(me.directionTo(moveTo));
//
//        // Calculate next location to move to next turn
//        moveToIndex = (moveToIndex + 1) % aroundBread.size();
//    }

    private void farmEXP() throws GameActionException {
        MapInfo[] infos = rc.senseNearbyMapInfos(-1);
        for (MapInfo info : infos) {
            if (info.isWater()) {
                if (rc.canFill(info.getMapLocation()))
                    rc.fill(info.getMapLocation());
            } else {
                if (rc.canDig(info.getMapLocation()))
                    rc.dig(info.getMapLocation());
            }
        }
    }

    /**
     * Attempt to place a bomb at current location
     */
    private void tryToPlaceBomb(RobotInfo[] enemies) throws GameActionException {
        MapLocation closestEnemy = closestEnemyToFlag(enemies);
        Direction dir = null;
        if (closestEnemy != null) {
            dir = myBreadIDefendForMyLife.directionTo(closestEnemy);
        }

        if (dir != null && rc.canBuild(TrapType.EXPLOSIVE, myBreadIDefendForMyLife.add(dir)))
            rc.build(TrapType.EXPLOSIVE, myBreadIDefendForMyLife.add(dir));

        rc.setIndicatorString("Tried to place bomb");
    }


    /**
     * Try to spawn the defender on or around the flag when he tries to respawn
     * @throws GameActionException
     */
    public void tryToSpawnOnMyFlag() throws GameActionException {
        // If we can spawn on our flag loc, spawn there
        if (rc.canSpawn(myBreadIDefendForMyLife)) rc.spawn(myBreadIDefendForMyLife);
    }

    public MapLocation closestEnemyToFlag(RobotInfo[] nearbyEnemies) {
        RobotInfo closestEnemy = null;

        for (RobotInfo enemy : nearbyEnemies) {
            if (closestEnemy == null) closestEnemy = enemy;
            else if (myBreadIDefendForMyLife.distanceSquaredTo(enemy.getLocation()) < myBreadIDefendForMyLife.distanceSquaredTo(closestEnemy.getLocation()))
                closestEnemy = enemy;
        }
        if (closestEnemy != null) return closestEnemy.getLocation();
        return null;
    }
}