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
        // yes its hardcoded, i dont care !!!!!
        if (!isMyBreadSet) {
            int whichBread = (localID % 3) + 1;
            myBreadIDefendForMyLife = spawnAreaCenters[whichBread-1];
            isMyBreadSet = true;
        }
        movement.hardMove(myBreadIDefendForMyLife);
//        if (rc.getExperience(SkillType.BUILD) < 10 && rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) {
//            farmEXP();
//        } else movement.hardMove(myBreadIDefendForMyLife);

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length != 0) tryToPlaceBomb();
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
    private void tryToPlaceBomb() throws GameActionException {

        MapInfo[] infoAround = rc.senseNearbyMapInfos(GameConstants.INTERACT_RADIUS_SQUARED);
        ArrayList<MapLocation> possiblePlacements = new ArrayList<>();

        MapLocation closestEnemy = closestEnemyToMe(rc.senseNearbyRobots(-1, rc.getTeam().opponent()));

        for (int i = 0; i < 10; i++) {
            possiblePlacements.clear();

            for (MapInfo info : infoAround) {
                if (rc.canBuild(TrapType.EXPLOSIVE, info.getMapLocation()))
                    possiblePlacements.add(info.getMapLocation());
            }

            MapLocation bestPlacement = locationClosestToEnemy(possiblePlacements, closestEnemy);

            if (rc.getRoundNum() > 190 && rc.getRoundNum() < 210 && bestPlacement != null) {
                rc.setIndicatorString(bestPlacement.toString());
            }


            if (bestPlacement != null) {
                if (rc.canBuild(TrapType.EXPLOSIVE, bestPlacement)) rc.build(TrapType.EXPLOSIVE, bestPlacement);
            }
        }

        if (rc.canAttack(closestEnemy)) rc.attack(closestEnemy);
    }


    /**
     * Try to spawn the defender on or around the flag when he tries to respawn
     * @throws GameActionException
     */
    public void tryToSpawnOnMyFlag() throws GameActionException {
        // If we can spawn on our flag loc, spawn there
        if (rc.canSpawn(myBreadIDefendForMyLife)) rc.spawn(myBreadIDefendForMyLife);
    }

    public MapLocation closestEnemyToMe(RobotInfo[] nearbyEnemies) {
        RobotInfo closestEnemy = null;

        for (RobotInfo enemy : nearbyEnemies) {
            if (closestEnemy == null) closestEnemy = enemy;
            else if (rc.getLocation().distanceSquaredTo(enemy.getLocation()) < rc.getLocation().distanceSquaredTo(closestEnemy.getLocation()))
                closestEnemy = enemy;
        }
        if (closestEnemy != null) return closestEnemy.getLocation();
        return null;
    }

    public MapLocation locationClosestToEnemy(ArrayList<MapLocation> locations, MapLocation closestEnemy) {
        MapLocation closestLocation = null;

        for (MapLocation location : locations) {
            if (closestLocation == null) closestLocation = location;
            else if (location.distanceSquaredTo(closestEnemy) < closestLocation.distanceSquaredTo(closestEnemy))
                closestLocation = location;
        }

        return closestLocation;
    }
}