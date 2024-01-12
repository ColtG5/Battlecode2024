package beastmode.after_specialists;

import battlecode.common.*;
import beastmode.Movement;
import beastmode.Utility;

/**
 * Just a base strategy class, if a bot doesn't specialize in any strategy (not entirely sure if needed, but just for now)
 */
public class Flagrunner {
    RobotController rc;
    Movement movement;
    Utility utility;
    boolean lefty;
    private static final int MAXINT = 2147483647;
    static FlagInfo flag = null;
    static int closestFlagDistance = MAXINT;
    static boolean KILLMODE = false;
    static MapLocation lastFlagFollowerLocation = null;

    public Flagrunner(RobotController rc, Movement movement, Utility utility, boolean lefty) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
        this.lefty = lefty;
    }

    public void run() throws GameActionException {
        rc.setIndicatorString("I am a flagrunner");
        RobotInfo[] robotEnemyInfo = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, rc.getTeam());
        if (KILLMODE) {
            if (robotEnemyInfo.length == 0) KILLMODE = false;
            else {
                rc.setIndicatorString("ITS KILLMODE TIME");
                wipeThemOut(robotEnemyInfo);
                return;
            }

        }
        if (robotEnemyInfo.length == 0) {
            if (rc.getHealth() < 1000) {
                if (rc.canHeal(rc.getLocation())) {
                    rc.heal(rc.getLocation());
                    return;
                }
            }
            for (RobotInfo info : robotInfo) {
                if (info.getHealth() < 2000) {
                    if (rc.canHeal(info.getLocation())) {
                        rc.heal(info.getLocation());
                    }
                }
            }
        }

        for (RobotInfo info : robotInfo) {
            if (info.hasFlag) {
                rc.setIndicatorString("I am a flagrunner and I am following the flag");
                followFlag(info.getLocation());
                attackTheLocals();
                return;
            }
        }
        lastFlagFollowerLocation = null;

        if (robotEnemyInfo.length > 5) {
            wipeThemOut(robotEnemyInfo);
            KILLMODE = true;
            return;
        }

        if (rc.hasFlag()) backToSpawn();
        else {
            fetchFlag();
            attackTheLocals();
        }
    }

    private void wipeThemOut(RobotInfo[] robotEnemyInfo) throws GameActionException {
        int maxHP = GameConstants.DEFAULT_HEALTH;
        MapLocation target = robotEnemyInfo[0].getLocation();
        for (RobotInfo info : robotEnemyInfo) {
            if (maxHP >= info.getHealth()) {
                maxHP = info.getHealth();
                target = info.getLocation();
            }
        }
        MapInfo[] mapInfo = rc.senseNearbyMapInfos();
        int count = 0;
        for (MapInfo info : mapInfo) {
            if ((info.getTrapType() == TrapType.STUN) || (info.getTrapType() == TrapType.EXPLOSIVE))
                count++;
        }
        if (count >= 3)
            target = rc.getLocation().add(rc.getLocation().directionTo(target).opposite());

        movement.hardMove(target);
        cheekyBomb(target);
        attackTheLocals();
    }


    private void cheekyBomb(MapLocation mapLocation) throws GameActionException {
        MapInfo[] mapInfo = rc.senseNearbyMapInfos();
        int numberOfStuns = 0;
        int numberOfExplosives = 0;
        for (MapInfo info : mapInfo) {
            if (info.getTrapType() == TrapType.STUN) numberOfStuns++;
            if (info.getTrapType() == TrapType.EXPLOSIVE) numberOfExplosives++;
        }
        if (numberOfExplosives < 2) {
            if (mapLocation.isWithinDistanceSquared(rc.getLocation(), 6)) {
                if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation().add(rc.getLocation().directionTo(mapLocation)))) {
                    rc.setIndicatorString("I am a flagrunner and I am building a explosive trap");
                    rc.build(TrapType.EXPLOSIVE, rc.getLocation().add(rc.getLocation().directionTo(mapLocation)));
                } else if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateRight()))) {
                    rc.setIndicatorString("I am a flagrunner and I am building a explosive trap");
                    rc.build(TrapType.EXPLOSIVE, rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateRight()));
                } else if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateLeft()))) {
                    rc.setIndicatorString("I am a flagrunner and I am building a explosive trap");
                    rc.build(TrapType.EXPLOSIVE, rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateLeft()));
                }
            }
        }
        if (numberOfStuns < 1) {
            if (mapLocation.isWithinDistanceSquared(rc.getLocation(), 6)) {
                if (rc.canBuild(TrapType.STUN, rc.getLocation().add(rc.getLocation().directionTo(mapLocation)))) {
                    rc.setIndicatorString("I am a flagrunner and I am building a stun trap");
                    rc.build(TrapType.STUN, rc.getLocation().add(rc.getLocation().directionTo(mapLocation)));
                } else if (rc.canBuild(TrapType.STUN, rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateRight()))) {
                    rc.setIndicatorString("I am a flagrunner and I am building a stun trap");
                    rc.build(TrapType.STUN, rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateRight()));
                } else if (rc.canBuild(TrapType.STUN, rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateLeft()))) {
                    rc.setIndicatorString("I am a flagrunner and I am building a stun trap");
                    rc.build(TrapType.STUN, rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateLeft()));
                }
            }
        }


    }


    private void followFlag(MapLocation mapLocOfFlagRunner) throws GameActionException {
        if (lastFlagFollowerLocation == null) {
            flag = null;
            closestFlagDistance = MAXINT;
            MapLocation[] spawnLocations = rc.getAllySpawnLocations();
            MapLocation closestSpawn = spawnLocations[0];
            for (MapLocation spawn : spawnLocations) {
                if (rc.getLocation().distanceSquaredTo(spawn) < rc.getLocation().distanceSquaredTo(closestSpawn))
                    closestSpawn = spawn;
            }
            Direction opDir = rc.getLocation().directionTo(closestSpawn).opposite();
            movement.hardMove(mapLocOfFlagRunner.add(opDir));
        } else {
            if (mapLocOfFlagRunner.add(mapLocOfFlagRunner.directionTo(lastFlagFollowerLocation)).isWithinDistanceSquared(rc.getLocation(), 2)) {
                MapLocation theSpotOfOpposite = mapLocOfFlagRunner.add(mapLocOfFlagRunner.directionTo(lastFlagFollowerLocation));
                movement.hardMove(rc.getLocation().add(rc.getLocation().directionTo(theSpotOfOpposite).opposite()));
            } else movement.hardMove(lastFlagFollowerLocation);
        }
        lastFlagFollowerLocation = mapLocOfFlagRunner;
    }

    private void attackTheLocals() throws GameActionException {
        RobotInfo[] robotInfo = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
        MapLocation target = null;
        int maxHealth = GameConstants.DEFAULT_HEALTH;
        for (RobotInfo info : robotInfo) {
            if (maxHealth >= info.getHealth()) {
                maxHealth = info.getHealth();
                target = info.getLocation();
            }
            if (info.hasFlag) {
                target = info.getLocation();
                break;
            }
        }
        if (target != null) {
            if (rc.canAttack(target)) rc.attack(target);
        }
    }

    private void backToSpawn() throws GameActionException {
        // This array will never be null
        MapLocation[] spawnLocations = rc.getAllySpawnLocations();
        MapLocation closestSpawn = spawnLocations[0];
        for (MapLocation spawn : spawnLocations) {
            if (rc.getLocation().distanceSquaredTo(spawn) < rc.getLocation().distanceSquaredTo(closestSpawn))
                closestSpawn = spawn;
        }
        movement.hardMove(closestSpawn);
    }

    private void fetchFlag() throws GameActionException {
        FlagInfo[] flagInfo = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo info : flagInfo) {
            if (info.isPickedUp() && info.equals(flag)) {
                flag = null;
                closestFlagDistance = MAXINT;
                continue;
            }
            if (info.getLocation().distanceSquaredTo(rc.getLocation()) < closestFlagDistance) {
                flag = info;
                closestFlagDistance = info.getLocation().distanceSquaredTo(rc.getLocation());
                if (rc.canPickupFlag(flag.getLocation())) {
                    rc.pickupFlag(flag.getLocation());
                }
            }
        }

        if (flag != null) {
            rc.setIndicatorString("I am a flagrunner and flag is not null");
            if (rc.getLocation().equals(flag.getLocation())) {
                rc.setIndicatorString("I am a flagrunner and I am at the flag");
                flag = null;
            } else movement.hardMove(flag.getLocation());
        } else {
            rc.setIndicatorString("I am moving to broadcast flag locations");
            MapLocation[] flagLocations = rc.senseBroadcastFlagLocations();
            if (flagLocations.length == 0) {
                backToSpawn();
                return;
            }
            MapLocation closestFlag = flagLocations[0];
            for (MapLocation flag : flagLocations) {
                if (rc.getLocation().distanceSquaredTo(flag) < rc.getLocation().distanceSquaredTo(closestFlag)) {
                    if (rc.getLocation().equals(closestFlag)) {
                        rc.setIndicatorString("I made it to the flag and it it is not there");
                        continue;
                    }
                    closestFlag = flag;
                }
            }
            MapLocation loc = rc.getLocation().add(rc.getLocation().directionTo(closestFlag));
            if (rc.senseMapInfo(loc).isWater()) {
                rc.setIndicatorString("I am a flagrunner and I am filling water");
                if (rc.canFill(loc)) rc.fill(loc);
            }
            movement.hardMove(closestFlag);
        }
    }
}
