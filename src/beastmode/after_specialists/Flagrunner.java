package beastmode.after_specialists;

import battlecode.common.*;
import beastmode.Movement;
import beastmode.Utility;

import java.util.Arrays;

/**
 * Just a base strategy class, if a bot doesn't specialize in any strategy (not entirely sure if needed, but just for now)
 */
public class Flagrunner {
    private static final int MAXINT = 2147483647;
    RobotController rc;
    Movement movement;
    Utility utility;

    public Flagrunner(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }
    static boolean KILLMODE = false;

    public void run() throws GameActionException {
        rc.setIndicatorString( "I am a flagrunner");
        RobotInfo[] robotEnemyInfo = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if(KILLMODE){

            if(robotEnemyInfo.length == 0){
                KILLMODE = false;
            }
            else{
                rc.setIndicatorString("ITS KILLMODE TIME");
                wipeThemOut(robotEnemyInfo);
            }
        }
        RobotInfo[] roboInfo = rc.senseNearbyRobots(-1, rc.getTeam());
        for(RobotInfo info: roboInfo){
            if(info.hasFlag){
            followFlag(info.getLocation());
            attackTheLocals();
            return;
            }
        }

        if(robotEnemyInfo.length > 5){
            wipeThemOut(robotEnemyInfo);
            KILLMODE= true;
            return;
        }

        if (rc.hasFlag()) backToSpawn();
        else {fetchFlag();attackTheLocals();}
    }

    private void wipeThemOut(RobotInfo[] robotEnemyInfo) throws GameActionException {
        int closestDistenceToEnemy = robotEnemyInfo[0].getLocation().distanceSquaredTo(rc.getLocation());
        MapLocation target = robotEnemyInfo[0].getLocation();
        for(RobotInfo info:  robotEnemyInfo){
            if(closestDistenceToEnemy > info.getLocation().distanceSquaredTo(rc.getLocation())){
                closestDistenceToEnemy = info.getLocation().distanceSquaredTo(rc.getLocation());
                target = info.getLocation();
            }
        }
        movement.hardMove(target);
        cheekyBomb(target);
        attackTheLocals();
    }


    private void cheekyBomb(MapLocation mapLocation) throws GameActionException{
        MapInfo[] mapInfo = rc.senseNearbyMapInfos();
        int numberOfStuns = 0;
        for(MapInfo info: mapInfo){
            if(info.getTrapType() == TrapType.STUN){
                numberOfStuns++;

            }

        }
        if(numberOfStuns <2){
            if(mapLocation.isWithinDistanceSquared(rc.getLocation(), 2)){
                if(rc.canBuild(TrapType.STUN, rc.getLocation().add(rc.getLocation().directionTo(mapLocation)))){
                    rc.setIndicatorString("I am a flagrunner and I am building a stun trap");
                    rc.build(TrapType.STUN,  rc.getLocation().add(rc.getLocation().directionTo(mapLocation)));
                }else if(rc.canBuild(TrapType.STUN, rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateRight()))){
                    rc.setIndicatorString("I am a flagrunner and I am building a stun trap");
                    rc.build(TrapType.STUN,  rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateRight()));
                }
                else if(rc.canBuild(TrapType.STUN, rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateLeft()))){
                    rc.setIndicatorString("I am a flagrunner and I am building a stun trap");
                    rc.build(TrapType.STUN,  rc.getLocation().add(rc.getLocation().directionTo(mapLocation).rotateLeft()));
                }
            }
        }

    }
    private void followFlag(MapLocation mapLocOfFlagRunner) throws GameActionException{
        flag = null;
        closestFlagDistence = MAXINT;
        MapLocation[] spawnLocations = rc.getAllySpawnLocations();
        MapLocation closestSpawn = spawnLocations[0];
        for (MapLocation spawn : spawnLocations) {
            if (rc.getLocation().distanceSquaredTo(spawn) < rc.getLocation().distanceSquaredTo(closestSpawn))
                closestSpawn = spawn;
        }
        Direction opDir = rc.getLocation().directionTo(closestSpawn).opposite();
        movement.hardMove(mapLocOfFlagRunner.add(opDir));

    }
    private void attackTheLocals() throws GameActionException{
        RobotInfo[] roboInfo = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        MapLocation target = null;
        int maxHealth = 2001;
        for( RobotInfo info: roboInfo){
            if(maxHealth > info.getHealth()){
                maxHealth = info.getHealth();
                target = info.getLocation();
            }
            if(info.hasFlag){
                target = info.getLocation();
                break;
            }
        }
        if(target != null){
            if(rc.canAttack(target)){
                rc.attack(target);
            }
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

    static FlagInfo flag = null;
    static int  closestFlagDistence = MAXINT ;

    private void fetchFlag() throws GameActionException {
        FlagInfo[] flagInfo = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for(FlagInfo info: flagInfo){
            if(info.isPickedUp() && info.equals(flag)){
                flag = null;
                closestFlagDistence = MAXINT;
                continue;
            }
            if(info.getLocation().distanceSquaredTo(rc.getLocation())<closestFlagDistence){
                flag = info;
                closestFlagDistence = info.getLocation().distanceSquaredTo(rc.getLocation());
                if(rc.canPickupFlag(flag.getLocation())){
                    rc.pickupFlag(flag.getLocation());
                }
            }

        }
        if(flag != null){
            movement.hardMove(flag.getLocation());
        }
        else{
            MapLocation[] flagLocations = rc.senseBroadcastFlagLocations();
            if(flagLocations.length == 0){
                backToSpawn();
                return;
            }
            MapLocation closestFlag= flagLocations[0];
            for (MapLocation flag : flagLocations) {
                if (rc.getLocation().distanceSquaredTo(flag) < rc.getLocation().distanceSquaredTo(closestFlag))
                    closestFlag = flag;
            }
            movement.hardMove(closestFlag);
        }

    }
}
