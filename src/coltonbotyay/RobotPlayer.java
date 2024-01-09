package coltonbotyay;

import battlecode.common.*;

import java.lang.reflect.Array;
import java.util.Arrays;
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
     * id
     *
     */

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        System.out.println("I'm alive");
        rc.setIndicatorString("Hello world!");

        Movement movement = new Movement(rc);

        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            try {
                if (rc.getRoundNum() == 1) {
                    // for each duck on round one, give it a random local ID
                    // (random localID is just for indexing into shared array pretty much)
                    int currentID = rc.readSharedArray(0);
                    localID = ++currentID;
                    rc.writeSharedArray(0, localID); // change the shared array ID so next duck gets incremented ID
                    rc.writeSharedArray(localID, rc.getID()); // write your actual ID into array index of your

                    if (localID == 50) {
                        int[] arr = new int[50];
                        for (int i = 0; i < 50; i++) {
//                            System.out.println(Arrays.toString(arr));
                            arr[i] = rc.readSharedArray(i+1);
                        }
                        System.out.println(Arrays.toString(arr));

                        rc.writeSharedArray(0, 0);
                    }
                }

                if (rc.getRoundNum() == 2 || rc.getRoundNum() == 3 || rc.getRoundNum() == 50 || rc.getRoundNum() == 900) {
                    int indexZeroNum = rc.readSharedArray(0);
                    indexZeroNum++;
                    rc.writeSharedArray(0, indexZeroNum);
                    rc.writeSharedArray(localID, indexZeroNum);

                    if (localID == 50) {
                        int[] arr = new int[50];
                        for (int i = 0; i < 50; i++) {
//                            System.out.println(Arrays.toString(arr));
                            arr[i] = rc.readSharedArray(i+1);
                        }
                        System.out.println(Arrays.toString(arr));

                        rc.writeSharedArray(0, 0);
                    }
                }

                if (turnCount == 1) {
                    rc.setIndicatorString("I am robot #" + localID);
                }

                if (!rc.isSpawned()) {
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    for (MapLocation loc : spawnLocs) {
                        if (rc.canSpawn(loc)) {
                            rc.spawn(loc);
//                            int currentID = rc.readSharedArray(0);
//                            localID = ++currentID;
//                            rc.setIndicatorString("I am robot #" + localID);
//                            rc.writeSharedArray(0, currentID);
                            break;
                        }
                    }
                }
                else {
                    // try moving to the closest enemy and attacking closest enemy
                    MapLocation closestEnemyLoc = findClosestEnemy(rc);
                    if (closestEnemyLoc != null) {
//                        rc.setIndicatorString("there is a closest enemy");
                        // try moving closer to the enemy duck

                        movement.simpleMove(closestEnemyLoc);
                        // try attacking the closest duck to you
                        while (rc.canAttack(closestEnemyLoc)) {
                            rc.attack(closestEnemyLoc);
//                            System.out.println("smacked that lil bih" + closestEnemyLoc.toString());
                            rc.setIndicatorString("smacked a lil bih" + closestEnemyLoc.toString());
                        }
                    }

                    // try to grab a close crumb
                    MapLocation[] potentialCrumbs = rc.senseNearbyCrumbs(-1);
                    if (potentialCrumbs.length != 0) {
                        MapLocation closestCrumb = null;
                        for (MapLocation crumb : potentialCrumbs) {
                            if (closestCrumb == null) {
                                closestCrumb = crumb;
                            } else if (rc.getLocation().distanceSquaredTo(crumb) < rc.getLocation().distanceSquaredTo(closestCrumb)) {
                                closestCrumb = crumb;
                            }
                        }
                        if (closestCrumb != null) {
                            movement.simpleMove(closestCrumb);
                        }
                    }

                    // if can move at end of turn, just move randomly (for now!!!)
                    Direction dir = directions[rng.nextInt(directions.length)];
                    movement.simpleMove(rc.getLocation().add(dir));

//                    // Rarely attempt placing random traps
//                    MapLocation prevLoc = rc.getLocation().subtract(dir);
//                    if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextInt() % 37 == 1)
//                        rc.build(TrapType.EXPLOSIVE, prevLoc);
                }

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
}
