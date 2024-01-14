package GoldenSon.either_specialists;

import GoldenSon.Movement;
import GoldenSon.Utility;
import battlecode.common.*;

import java.util.ArrayList;

public class Builder {
    RobotController rc;
    Movement movement;
    Utility utility;

    public Builder(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() throws GameActionException {
        // Don't move when farming exp
        if ((rc.getExperience(SkillType.BUILD) < 30 && rc.getRoundNum() < GameConstants.SETUP_ROUNDS) ||
                (rc.getExperience(SkillType.BUILD) < 20 && rc.getRoundNum() > GameConstants.SETUP_ROUNDS)) {
            farmEXP();
        }

        // FOr testing only
        MapLocation[] enemyFlags = rc.senseBroadcastFlagLocations();
        for (MapLocation flag : enemyFlags) {
            movement.hardMove(flag);
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length > 4 && rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
            placeBombs();
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

    private void placeBombs() throws GameActionException {
       MapInfo[] infoAround = rc.senseNearbyMapInfos(GameConstants.INTERACT_RADIUS_SQUARED);
       ArrayList<MapLocation> possiblePlacements = new ArrayList<>();
       for (MapInfo info : infoAround) {
           if (rc.canBuild(TrapType.EXPLOSIVE, info.getMapLocation())) possiblePlacements.add(info.getMapLocation());
       }

        MapLocation closestEnemy = closestEnemyToMe(rc.senseNearbyRobots(-1, rc.getTeam().opponent()));
        MapLocation bestPlacement = locationClosestToEnemy(possiblePlacements, closestEnemy);

        if (bestPlacement != null) {
            if (rc.canBuild(TrapType.EXPLOSIVE, bestPlacement)) rc.build(TrapType.EXPLOSIVE, bestPlacement);
        } else {
            if (rc.canAttack(closestEnemy)) rc.attack(closestEnemy);
        }
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
