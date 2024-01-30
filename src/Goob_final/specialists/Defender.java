package Goob_final.specialists;

import Goob_final.*;
import battlecode.common.*;
import java.util.ArrayList;
import static Goob_final.RobotPlayer.NONELOCATION;

public class Defender {
    RobotController rc;
    Movement movement;
    BugNav bugnav;
    Utility utility;
    Flagrunner flagrunner;
    Utility.CoolRobotInfo[] coolRobotInfoArray;
    int localID;
    MapLocation[] spawnAreaCenters;
    static MapLocation myBreadIDefendForMyLife = null;
    static MapLocation[] cornerStunsOnSpawnZones = new MapLocation[5];
    static MapLocation[] edgeBombsOnSpawnZones = new MapLocation[4];
    static boolean isMyBreadSet = false;
    static boolean returnToFlag = true;
    public Defender(RobotController rc, Movement movement, BugNav bugnav, Utility utility, Flagrunner flagrunner) {
        this.rc = rc;
        this.movement = movement;
        this.bugnav = bugnav;
        this.utility = utility;
        this.flagrunner = flagrunner;
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
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam());
        if (flags.length == 0) returnToFlag = false;
        if (rc.canSenseLocation(myBreadIDefendForMyLife)) {
            for (FlagInfo flag : flags) {
                if (flag.getLocation().equals(myBreadIDefendForMyLife)) {
                    returnToFlag = true;
                    break;
                }
            }
        }

        if (!returnToFlag) {
            flagrunner.run();
            return;
        }

        bugnav.moveTo(myBreadIDefendForMyLife);

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo enemy : nearbyEnemies) {
            if (enemy.hasFlag()) {
                returnToFlag = false;
                bugnav.moveTo(enemy.getLocation());
                MapLocation closestEnemy = closestEnemyToMe(nearbyEnemies);
                if (rc.canAttack(closestEnemy)) rc.attack(closestEnemy);
            }
        }

        if (flags.length == 0) returnToFlag = true;
        for (FlagInfo flag : flags) {
            if (flag.getLocation().equals(myBreadIDefendForMyLife)) returnToFlag = true;
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        MapLocation closestEnemy = closestEnemyToMe(enemies);

        if (closestEnemy != null) {
            if (rc.getLocation().distanceSquaredTo(closestEnemy) <= GameConstants.ATTACK_RADIUS_SQUARED) {
                if (rc.canAttack(closestEnemy)) rc.attack(closestEnemy);
                movement.smallMove(rc.getLocation().directionTo(closestEnemy).opposite());
            } else if (rc.getLocation().distanceSquaredTo(closestEnemy) <= 10) {
                bugnav.moveTo(closestEnemy);
                if (rc.canAttack(closestEnemy)) rc.attack(closestEnemy);
            }
        }
        boolean myBreadIsAtHome = false;
        for (FlagInfo flag : flags) {
            if (flag.getLocation().equals(myBreadIDefendForMyLife)) myBreadIsAtHome = true;
        }
        if (rc.getCrumbs() > 300 && myBreadIsAtHome && rc.getRoundNum() > GameConstants.SETUP_ROUNDS) placeTrapsAroundBread();

        RobotInfo[] defenders = rc.senseNearbyRobots(-1, rc.getTeam());
        boolean isUnderAttack = defenders.length < enemies.length;

        tryToHeal();

        // after every round whether spawned or not, convert your info to an int and write it to the shared array
        utility.writeMyInfoToSharedArray(isUnderAttack);
    }

    public void tryToHeal() throws GameActionException {
        if (!rc.isActionReady()) return;
        RobotInfo[] friendliesInRangeToHeal = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());
        if (friendliesInRangeToHeal.length == 0) return;
        RobotInfo lowestHealthFriendly = friendliesInRangeToHeal[0];
        for (RobotInfo friendly : friendliesInRangeToHeal) {
            if (friendly.hasFlag) {
                lowestHealthFriendly = friendly;
                break;
            }
            if (friendly.health < lowestHealthFriendly.health) lowestHealthFriendly = friendly;
        }
        if (rc.canHeal(lowestHealthFriendly.location)) rc.heal(lowestHealthFriendly.location);
    }

    private void placeTrapsAroundBread() throws GameActionException {
        for (MapLocation spawnZoneLoc : cornerStunsOnSpawnZones) {
            if (rc.canBuild(TrapType.STUN, spawnZoneLoc)) {
                rc.build(TrapType.STUN, spawnZoneLoc);
            }
        }
        if (rc.getCrumbs() > 2000) {
            for (MapLocation spawnZoneLoc : edgeBombsOnSpawnZones) {
                if (rc.canBuild(TrapType.EXPLOSIVE, spawnZoneLoc)) {
                    rc.build(TrapType.EXPLOSIVE, spawnZoneLoc);
                }
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

            cornerStunsOnSpawnZones[0] = myBreadIDefendForMyLife.add(Direction.NORTHEAST);
            cornerStunsOnSpawnZones[1] = myBreadIDefendForMyLife.add(Direction.SOUTHEAST);
            cornerStunsOnSpawnZones[2] = myBreadIDefendForMyLife.add(Direction.SOUTHWEST);
            cornerStunsOnSpawnZones[3] = myBreadIDefendForMyLife.add(Direction.NORTHWEST);
            cornerStunsOnSpawnZones[4] = myBreadIDefendForMyLife.add(Direction.CENTER);

            edgeBombsOnSpawnZones[0] = myBreadIDefendForMyLife.add(Direction.NORTH);
            edgeBombsOnSpawnZones[1] = myBreadIDefendForMyLife.add(Direction.EAST);
            edgeBombsOnSpawnZones[2] = myBreadIDefendForMyLife.add(Direction.SOUTH);
            edgeBombsOnSpawnZones[3] = myBreadIDefendForMyLife.add(Direction.WEST);

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