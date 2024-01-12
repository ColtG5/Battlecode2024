package beastmode.after_specialists;

import battlecode.common.*;
import battlecode.schema.GameplayConstants;
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

    /**
     * MAIN LOGIC LOOP
     * @throws GameActionException
     */
    public void run() throws GameActionException {
        rc.setIndicatorString("I am a flagrunner");
        RobotInfo[] robotEnemyInfo = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, rc.getTeam());
        //CHECKS IF KILL MODE HAS BEEN SET FROM PREVIOUS ROUND
        //IF SO, CHECKS IF THERE ARE ANY ENEMIES LEFT, IF ALL DEAD, KILL MODE IS SET TO FALSE
        //ELSE, KEEP ON KILLLLLING
        if (KILLMODE) {
            if (robotEnemyInfo.length == 0) KILLMODE = false;
            else {
                rc.setIndicatorString("ITS KILLMODE TIME");
                wipeThemOut(robotEnemyInfo);
                return;
            }

        }
        //--------------------------------------
        //FROM HERE ON, THEY WILL NOT HUNT DOWN ENEMIES
        //--------------------------------------

        //HEALING LOGIC
        //IF NO ENEMIES AROUND, HEAL SELF IF IN RANGE OF DYING FROM LAND MINE
        //ELSE, HEAL OTHERS IF IN RANGE OF DYING FROM LAND MINE BUT DON'S STOP MOVING
        if (robotEnemyInfo.length == 0 && !rc.hasFlag()) {
            if (rc.getHealth() <= TrapType.EXPLOSIVE.enterDamage) {
                if (rc.canHeal(rc.getLocation())) {
                    rc.heal(rc.getLocation());
                    return;
                }
            }
            for (RobotInfo info : robotInfo) {
                if (info.getHealth() < GameConstants.DEFAULT_HEALTH) {
                    if (rc.canHeal(info.getLocation())) {
                        rc.heal(info.getLocation());
                    }
                }
            }
        }
        //IF ALLY ROBOT HAS FLAG, FOLLOW THEM USING FOLLOW LOGIC
        //REMEMBER TO ATTACK AS YOU ESCORT
        for (RobotInfo info : robotInfo) {
            if (info.hasFlag) {
                rc.setIndicatorString("I am a flagrunner and I am following the flag");
                followFlag(info.getLocation());
                attackTheLocals();
                return;
            }
        }
        //LINE IS HERE TO CLEAR THE LAST FLAG LOCATION IF NO ALLY HAS FLAG IN RANGE AS THERE WAS A BUG WITHOUT IT
        lastFlagFollowerLocation = null;
        //IF THERE ARE ENEMIES AROUND, KILL THEM ALL!!!!! BUT ONLY IF THERE ARE 6 OR MORE, OTHERWISE KEEP ON MOVING
        if (robotEnemyInfo.length > 5 && !rc.hasFlag()) {
            wipeThemOut(robotEnemyInfo);
            KILLMODE = true;
            return;
        }
        //IF YOU HAVE THE FLAG, GO BACK TO SPAWN
        //ELSE, FETCH THE FLAG AND KILL PEOPLE AS YOU GO

        if (rc.hasFlag()) backToSpawn();
        else {
            fetchFlag();
            attackTheLocals();
        }
    }

    /**
     * MAIN BATTLE LOGIC
     * @param robotEnemyInfo array of enemy robots
     * @throws GameActionException
     */
    private void wipeThemOut(RobotInfo[] robotEnemyInfo) throws GameActionException {
        //THIS FINDS THE LOWEST HEALTH ENEMY AND TARGETS THEM AS MOVEMENT CHOICE
        int maxHP = GameConstants.DEFAULT_HEALTH;
        MapLocation target = robotEnemyInfo[0].getLocation();
        for (RobotInfo info : robotEnemyInfo) {
            if (maxHP >= info.getHealth()) {
                maxHP = info.getHealth();
                target = info.getLocation();
            }
        }
        //THIS CHECKS IF THERE ARE 3 OR MORE TRAPS AROUND YOU, IF SO, RUN AWAY TO DRAG ENEMIES INTO THEM
        MapInfo[] mapInfo = rc.senseNearbyMapInfos();
        int count = 0;
        for (MapInfo info : mapInfo) {
            if ((info.getTrapType() == TrapType.STUN) || (info.getTrapType() == TrapType.EXPLOSIVE))
                count++;
        }
        if (count >= 3)
            target = rc.getLocation().add(rc.getLocation().directionTo(target).opposite());
        // MOVE TOWARDS TARGET AND PLACE TRAPS AS YOU GO
        movement.hardMove(target);
        cheekyBomb(target);
        //ATTACK ENEMIES AROUND YOU
        attackTheLocals();
    }

    /**
     * PLACES TRAPS AS YOU GO
     * @param mapLocation location to place traps
     * @throws GameActionException
     */
    private void cheekyBomb(MapLocation mapLocation) throws GameActionException {
        //CHECKS ALL SURROUNDING AREA, IF THERE ARE LESS THAN 2 EXPLOSIVE TRAPS, TRY TO PLACE ONE IN DIRECTION OF ENEMY
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
        //CHECKS ALL SURROUNDING AREA, IF THERE ARE LESS THAN 1 STUN TRAPS, TRY TO PLACE ONE IN DIRECTION OF ENEMY
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

    /**
     * FOLLOWS THE ALLY WITH THE FLAG
     * @param mapLocOfFlagRunner location of ally with flag
     * @throws GameActionException
     */
    private void followFlag(MapLocation mapLocOfFlagRunner) throws GameActionException {
        //IF AND ELSE ARE USED FOR DETERMINING IF YOU HAVE BEEN FOLLOWING A BOT WITH A FLAG OR JUST FOUND BOT WITH FLAG
        //IF YOU JUST FOUND A BOT WITH FLAG THEN MOVE OPPOSITE DIRECTION OF CLOSEST FLAG TO AS FLAG BOT MOVES TO CLOSEST ONE. THIS WAY YOU GET BEHIND THE BOT WITH FLAG
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
        }
        //IF YOU HAVE BEEN FOLLOWING A BOT WITH A FLAG, THEN MOVE IN THE DIRECTION OF THE LAST LOCATION OF THE BOT WITH FLAG BUT ONE EXTRA TILE AWAY TO MAKE SURE YOU ARE NOT NEAR THE FLAG RUNNER AND CLOG HIS MOVEMENT
        else {
            if (mapLocOfFlagRunner.add(mapLocOfFlagRunner.directionTo(lastFlagFollowerLocation)).isWithinDistanceSquared(rc.getLocation(), 2)) {
                MapLocation theSpotOfOpposite = mapLocOfFlagRunner.add(mapLocOfFlagRunner.directionTo(lastFlagFollowerLocation));
                movement.hardMove(rc.getLocation().add(rc.getLocation().directionTo(theSpotOfOpposite).opposite()));
            } else movement.hardMove(lastFlagFollowerLocation.add(mapLocOfFlagRunner.directionTo(lastFlagFollowerLocation)));
        }
        //UPDATES LAST LOCATION OF BOT WITH FLAG
        lastFlagFollowerLocation = mapLocOfFlagRunner;
    }
    /**
     * ATTACKS ENEMIES AROUND YOU
     * @throws GameActionException
     */
    private void attackTheLocals() throws GameActionException {
        RobotInfo[] robotInfo = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
        MapLocation target = null;
        //FIND LOWEST HEALTH ENEMY AND TARGET THEM
        int maxHealth = GameConstants.DEFAULT_HEALTH;
        for (RobotInfo info : robotInfo) {
            if (maxHealth >= info.getHealth()) {
                maxHealth = info.getHealth();
                target = info.getLocation();
            }
            //IF THEY HAVE THE FLAG, TARGET THEM AND KILL THEM FIRST
            if (info.hasFlag) {
                target = info.getLocation();
                break;
            }
        }
        //ATTACK TARGET IF YOU CAN
        if (target != null) {
            if (rc.canAttack(target)) rc.attack(target);
        }
    }
    /**
     * GOES BACK TO SPAWN
     * @throws GameActionException
     */
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
