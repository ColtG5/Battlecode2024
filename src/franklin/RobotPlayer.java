package franklin;

import battlecode.common.*;

import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    static int localID;
    static boolean firstRoundFlagBearer = false;

    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * SHARED ARRAY
     * [0,  1,  2,  3,  4,  5,  6,  7,  8, ...]
     * id  hq1 hq2 hq3
     *
     */

    static final int assigningLocalIDIndex = 0;
    static final int spawnLocCenterOneIndex = 1;
    static final int spawnLocCenterTwoIndex = 2;
    static final int spawnLocCenterThreeIndex = 3;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Movement movement = new Movement(rc);
        Utility util = new Utility(rc);
        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            try {
                if (rc.getRoundNum() == 1) {
                    localID = util.makeLocalID(assigningLocalIDIndex);

                    // if spawned on top of flag, set flag bearer to true, and grab that thang
                    if (rc.canPickupFlag(rc.getLocation())) {
                        firstRoundFlagBearer = true;
                        rc.pickupFlag(rc.getLocation());
                    }
                }

                if (turnCount == 1) {
                    rc.setIndicatorString("I am robot #" + localID);
                }

                if (!rc.isSpawned()) {
                    boolean didSpawn = util.trySpawning();
                }

                // try moving to the closest enemy and attacking closest enemy
                MapLocation closestEnemy = findClosestEnemy(rc);
                if (closestEnemy != null) {
                    // if have enough health to attack, attack, otherwise move away and try to heal
                    if (rc.getHealth() >= 500) {
                        // try moving closer to the enemy duck
                        movement.simpleMove(closestEnemy);
                        // try attacking the closest duck to you
                        while (rc.canAttack(closestEnemy)) {
                            rc.attack(closestEnemy);
                            rc.setIndicatorString("smacked a lil bih" + closestEnemy.toString());
                        }
                    } else {
                        // try moving away from the enemy duck
                        movement.simpleMove(rc.getLocation().subtract(rc.getLocation().directionTo(closestEnemy)));
                        // try healing
                        if (rc.canHeal(rc.getLocation())) {
                            rc.heal(rc.getLocation());
                        }
                    }
                }

                MapInfo[] info = rc.senseNearbyMapInfos(1);

                // Fill water if possible to grab crumbs on water
                if (rc.getExperience(SkillType.BUILD) <= 30) {
                    for (MapInfo location : info) {
                        if (location.isWater()) {
                            MapLocation waterLocation = location.getMapLocation();
                            if (rc.getLocation().isAdjacentTo(waterLocation)) {
                                if (rc.canFill(waterLocation)) rc.fill(waterLocation);
                            }
                        } else {
                            MapLocation digLocation = info[rng.nextInt(info.length)].getMapLocation();
                            if (rc.getLocation().isAdjacentTo(digLocation)) {
                                if (rc.canDig(digLocation)) rc.dig(digLocation);
                            }
                        }
                    }
                }

                // try to grab a close crumb
                MapLocation[] potentialCrumbs = rc.senseNearbyCrumbs(-1);

                if (potentialCrumbs.length != 0) {
                    MapLocation closestCrumb = null;
                    for (MapLocation crumb : potentialCrumbs) {
                        if (closestCrumb == null) closestCrumb = crumb;
                        else if (rc.getLocation().distanceSquaredTo(crumb) < rc.getLocation().distanceSquaredTo(closestCrumb)) {
                            closestCrumb = crumb;
                        }
                    }
                    if (closestCrumb != null) {
                        movement.simpleMove(closestCrumb);
                    }
                }

                if (rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    MapLocation spawn = spawnLocs[rng.nextInt(27)];
                    movement.simpleMove(spawn);
                }

                // Move to spawn if duck has flag
//                    if (rc.hasFlag()) {
//                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
//                        rc.setIndicatorString("Going to " + spawnLocs[0] + " with flag.");
//                        movement.simpleMove(spawnLocs[0]);
//                    }
//                    // Move to flags after setup rounds
//                    else if (rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
//                        // Find approximate location of flags
//                        MapLocation[] potentialFlags = rc.senseBroadcastFlagLocations();
//                        for (MapLocation flag : potentialFlags) {
//                            movement.simpleMove(flag);
//                        }
//                        // Check if duck is in range of a flag
//                        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
//                        MapLocation closestFlag = null;
//                        if (flags.length != 0) {
//                            for (FlagInfo flag : flags) {
//                                if (closestFlag == null) closestFlag = flag.getLocation();
//                                else if (rc.getLocation().distanceSquaredTo(flag.getLocation()) <
//                                        rc.getLocation().distanceSquaredTo(flag.getLocation())) {
//                                    closestFlag = flag.getLocation();
//                                }
//                            }
//                            if (closestFlag != null) {
//                                movement.simpleMove(closestFlag);
//                            }
//
//                            if (rc.getLocation().isAdjacentTo(closestFlag) && rc.canPickupFlag(closestFlag)) {
//                                rc.pickupFlag(closestFlag);
//                            }
//                        }
//                    }

                // if can move at end of turn, just move randomly (for now!!!)
                Direction dir = directions[rng.nextInt(directions.length)];
                movement.simpleMove(rc.getLocation().add(dir));

                // if have action at end of turn, and not full health, why not heal
                tryToHeal(rc);

                // Rarely attempt placing random traps
                MapLocation prevLoc = rc.getLocation().subtract(dir);
                if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextInt() % 42 == 1)
                    rc.build(TrapType.EXPLOSIVE, prevLoc);

                // debugging
//                    rc.setIndicatorString(rc.getActionCooldownTurns() + " ||| " + rc.canDig(rc.getLocation().add(Direction.NORTH)) + " | " + rc.canFill(rc.getLocation().add(Direction.NORTH)));

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }
        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    public static MapLocation[] findEnemies(RobotController rc) throws GameActionException{
    	RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        MapLocation[] enemyLocations;
        if (enemies.length != 0) {
    		rc.setIndicatorString("There are nearby enemy robots! Scary!");
    		enemyLocations = new MapLocation[enemies.length];
    		for (int i = 0; i < enemies.length; i++) {
    			enemyLocations[i] = enemies[i].getLocation();
    		}
            return enemyLocations;
    	}
        return null;
    }

    public static MapLocation findClosestEnemy(RobotController rc) throws GameActionException {
    	RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        MapLocation closestEnemy = null;
        if (enemies.length != 0) {
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            for (RobotInfo enemy : enemies) {
                MapLocation enemyLoc = enemy.getLocation();
                if (closestEnemy == null) {
                    closestEnemy = enemyLoc;
                }
                else if (rc.getLocation().distanceSquaredTo(enemyLoc) < rc.getLocation().distanceSquaredTo(closestEnemy)) {
                    closestEnemy = enemyLoc;
                }
            }
        }
        return closestEnemy;
    }

    public static void tryToHeal(RobotController rc) throws GameActionException {
        if (rc.getActionCooldownTurns() < GameConstants.COOLDOWN_LIMIT) {
            if (rc.getHealth() < 1000 && rc.canHeal(rc.getLocation())) rc.heal(rc.getLocation());
            else {
                RobotInfo[] teammies = rc.senseNearbyRobots(-1, rc.getTeam());
                for (RobotInfo teammie : teammies) {
                    if (rc.getHealth() < 1000 && rc.canHeal(teammie.getLocation())) rc.heal(teammie.getLocation());
                }
            }
        }
    }
}
