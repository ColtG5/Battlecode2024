package Its_A_Gun_V5.specialists;

import battlecode.common.*;
import Its_A_Gun_V5.Movement;
import Its_A_Gun_V5.Utility;

import java.util.ArrayList;

public class Defender {
    RobotController rc;
    Movement movement;
    Utility utility;
    Utility.CoolRobotInfo[] coolRobotInfoArray;
    int localID;
    MapLocation[] spawnAreaCenters;
    static MapLocation myBreadIDefendForMyLife = null;
    static MapLocation[] spawnZonesOfMyBread = new MapLocation[9];
    static boolean isMyBreadSet = false;
    static boolean returnToFlag = true;
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
    public void setCoolRobotInfoArray(Utility.CoolRobotInfo[] coolRobotInfoArray) {
        this.coolRobotInfoArray = coolRobotInfoArray;
    }

    public void run() throws GameActionException {
        if (returnToFlag) movement.hardMove(myBreadIDefendForMyLife);

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo enemy : nearbyEnemies) {
            if (enemy.hasFlag()) {
                returnToFlag = false;
                movement.hardMove(enemy.getLocation());
                MapLocation closestEnemy = closestEnemyToMe(nearbyEnemies);
                if (rc.canAttack(closestEnemy)) rc.attack(closestEnemy);
            }
        }

        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam());
        if (flags.length == 0) returnToFlag = true;
        for (FlagInfo flag : flags) {
            if (flag.getLocation().equals(myBreadIDefendForMyLife)) returnToFlag = true;
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        MapLocation closestEnemy = closestEnemyToMe(enemies);
//        if (enemies.length != 0 && !returnToFlag) tryToPlaceBomb(closestEnemy);
//        if (closestEnemy != null && rc.canAttack(closestEnemy)) rc.attack(closestEnemy);

        if (closestEnemy != null) {
            if (rc.getLocation().distanceSquaredTo(closestEnemy) <= GameConstants.ATTACK_RADIUS_SQUARED) {
                if (rc.canAttack(closestEnemy)) rc.attack(closestEnemy);
                movement.smallMove(rc.getLocation().directionTo(closestEnemy).opposite());
            } else if (rc.getLocation().distanceSquaredTo(closestEnemy) <= 10) {
                utility.placeTrapNearEnemy(closestEnemy);
                movement.smallMove(rc.getLocation().directionTo(closestEnemy).opposite());
            }
        }
        boolean myBreadIsAtHome = false;
        for (FlagInfo flag : flags) {
            if (flag.getLocation().equals(myBreadIDefendForMyLife)) myBreadIsAtHome = true;
        }
        if (enemies.length == 0 && myBreadIsAtHome) placeTrapsAroundBread();

        RobotInfo[] defenders = rc.senseNearbyRobots(-1, rc.getTeam());
        boolean isUnderAttack = defenders.length - 2 <= enemies.length;

        // after every round whether spawned or not, convert your info to an int and write it to the shared array
        utility.writeMyInfoToSharedArray(isUnderAttack);
    }

    /**
     * Attempt to place a bomb at current location
     */
    private void tryToPlaceBomb(MapLocation closestEnemy) throws GameActionException {

        MapInfo[] infoAround = rc.senseNearbyMapInfos(GameConstants.INTERACT_RADIUS_SQUARED);
        ArrayList<MapLocation> possiblePlacements = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            possiblePlacements.clear();

            for (MapInfo info : infoAround) {
                if (rc.canBuild(TrapType.EXPLOSIVE, info.getMapLocation()))
                    possiblePlacements.add(info.getMapLocation());
            }

            MapLocation bestPlacement = locationClosestToEnemy(possiblePlacements, closestEnemy);

            if (bestPlacement != null) {
                if (rc.canBuild(TrapType.EXPLOSIVE, bestPlacement)) rc.build(TrapType.EXPLOSIVE, bestPlacement);
            }
        }
    }

    private void placeTrapsAroundBread() throws GameActionException {
        for (MapLocation spawnZoneLoc : spawnZonesOfMyBread) {
            if (rc.canBuild(TrapType.STUN, spawnZoneLoc)) {
                rc.build(TrapType.STUN, spawnZoneLoc);
            }
        }
    }

    /**
     * Try to spawn the defender on or around the flag when he tries to respawn
     * @throws GameActionException why?????????????????
     */
    public void tryToSpawnOnMyFlag() throws GameActionException {
        // If we can spawn on our flag loc, spawn there
        if (!isMyBreadSet) {
            int whichBread = (localID % 3) + 1;
            myBreadIDefendForMyLife = spawnAreaCenters[whichBread-1];

            spawnZonesOfMyBread[0] = myBreadIDefendForMyLife.add(Direction.NORTH);
            spawnZonesOfMyBread[1] = myBreadIDefendForMyLife.add(Direction.NORTHEAST);
            spawnZonesOfMyBread[2] = myBreadIDefendForMyLife.add(Direction.EAST);
            spawnZonesOfMyBread[3] = myBreadIDefendForMyLife.add(Direction.SOUTHEAST);
            spawnZonesOfMyBread[4] = myBreadIDefendForMyLife.add(Direction.SOUTH);
            spawnZonesOfMyBread[5] = myBreadIDefendForMyLife.add(Direction.SOUTHWEST);
            spawnZonesOfMyBread[6] = myBreadIDefendForMyLife.add(Direction.WEST);
            spawnZonesOfMyBread[7] = myBreadIDefendForMyLife.add(Direction.NORTHWEST);
            spawnZonesOfMyBread[8] = myBreadIDefendForMyLife.add(Direction.CENTER);

            isMyBreadSet = true;
        }
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