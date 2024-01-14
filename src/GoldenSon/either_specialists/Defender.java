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
    static boolean isMyBreadSet = false;
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
        rc.setIndicatorString("I am defender");
        // yes its hardcoded, i dont care !!!!!
        if (!isMyBreadSet) {
            int whichBread = (localID % 3) + 1;
            myBreadIDefendForMyLife = spawnAreaCenters[whichBread-1];
            isMyBreadSet = true;
            System.out.println("My bread ::" + myBreadIDefendForMyLife);
        }
        movement.hardMove(myBreadIDefendForMyLife);
//        if (rc.getExperience(SkillType.BUILD) < 10 && rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) {
//            farmEXP();
//        } else {
//            movement.hardMove(myBreadIDefendForMyLife);
//        }
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length != 0) tryToPlaceBomb(enemies);
//        moveAroundBread();
        RobotInfo[] enemies2 = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies2.length != 0) {
            MapLocation enemy = utility.enemyWithLowestHP(enemies2);
            if (rc.canAttack(enemy)) rc.attack(enemy);
//            tryToPlaceBomb(enemies2);
        }
    }

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