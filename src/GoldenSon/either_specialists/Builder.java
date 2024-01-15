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

    public void run(MapLocation groupLocToMoveTo) throws GameActionException {
        // Don't move when farming exp
        if (rc.getExperience(SkillType.BUILD) < 30) {
            farmEXP();
        }

//        // FOr testing only
//        MapLocation[] enemyFlags = rc.senseBroadcastFlagLocations();
//        for (MapLocation flag : enemyFlags) {
//            movement.hardMove(flag);
//        }

        if (isDistanceToGroupLeaderMoreThan(10)) {
            movement.hardMove(utility.getLocationOfMyGroupLeader());
        }

        movement.hardMove(groupLocToMoveTo);

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//        System.out.println("might place bombs if conditon met");
        if (enemies.length > 3 && rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
            tryToPlaceBomb();
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
//        System.out.println("TRIED !!!");
        MapInfo[] infoAround = rc.senseNearbyMapInfos(-1);
        ArrayList<MapLocation> possiblePlacements = new ArrayList<>();

        int countNumberOfTrapsAround = 0;

        for (MapInfo info : infoAround) {
            if (info.getTrapType() != TrapType.NONE) {
                countNumberOfTrapsAround++;
            }
        }

        if (countNumberOfTrapsAround > 3) return;

        for (MapInfo info : infoAround) {
            if (rc.canBuild(TrapType.EXPLOSIVE, info.getMapLocation())) possiblePlacements.add(info.getMapLocation());
        }

        if (rc.getRoundNum() > 190 && rc.getRoundNum() < 210) {
            System.out.println(possiblePlacements);
        }

        MapLocation closestEnemy = closestEnemyToMe(rc.senseNearbyRobots(-1, rc.getTeam().opponent()));
        MapLocation bestPlacement = locationClosestToEnemy(possiblePlacements, closestEnemy);

        if (bestPlacement != null && rc.getCrumbs() >= TrapType.EXPLOSIVE.buildCost) {
            if (rc.canBuild(TrapType.EXPLOSIVE, bestPlacement)) rc.build(TrapType.EXPLOSIVE, bestPlacement);
        } else {
            if (rc.canAttack(closestEnemy)) rc.attack(closestEnemy);
        }
    }

    private void tryToPlaceBomb() throws GameActionException {

        MapInfo[] infoAround = rc.senseNearbyMapInfos(10);
        ArrayList<MapLocation> possiblePlacements = new ArrayList<>();

        int countNumberOfTrapsAround = 0;

        for (MapInfo info : infoAround) {
            if (info.getTrapType() != TrapType.NONE) {
                countNumberOfTrapsAround++;
            }
        }

        if (countNumberOfTrapsAround > 3) return;

        MapLocation closestEnemy = closestEnemyToMe(rc.senseNearbyRobots(-1, rc.getTeam().opponent()));

        for (int i = 0; i < 2; i++) {
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

    public boolean isDistanceToGroupLeaderMoreThan(int distance) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        MapLocation groupLeaderLocation = utility.readLocationFromFlagrunnerGroupIndex();
        return myLocation.distanceSquaredTo(groupLeaderLocation) > distance;
    }
}
