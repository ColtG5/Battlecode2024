package beastmode;

import battlecode.common.*;
import beastmode.before_specialists.*;
import beastmode.after_specialists.*;
import beastmode.either_specialists.*;

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
    static boolean lefty = true; // do u pathfind favouring left first or right first
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
     * [0,          1-50,          51]
     * id     id's of all ducks
     *
     */

    static final int assigningLocalIDIndex = 0;
    static final int duckOneIndex = 1;
    static final int duckTwoIndex = 2;
    static final int duckThreeIndex = 3;
    static final int duckFourIndex = 4;
    static final int duckFiveIndex = 5;
    static final int duckSixIndex = 6;
    static final int duckSevenIndex = 7;
    static final int duckEightIndex = 8;
    static final int duckNineIndex = 9;
    static final int duckTenIndex = 10;
    static final int duckElevenIndex = 11;
    static final int duckTwelveIndex = 12;
    static final int duckThirteenIndex = 13;
    static final int duckFourteenIndex = 14;
    static final int duckFifteenIndex = 15;
    static final int duckSixteenIndex = 16;
    static final int duckSeventeenIndex = 17;
    static final int duckEighteenIndex = 18;
    static final int duckNineteenIndex = 19;
    static final int duckTwentyIndex = 20;
    static final int duckTwentyOneIndex = 21;
    static final int duckTwentyTwoIndex = 22;
    static final int duckTwentyThreeIndex = 23;
    static final int duckTwentyFourIndex = 24;
    static final int duckTwentyFiveIndex = 25;
    static final int duckTwentySixIndex = 26;
    static final int duckTwentySevenIndex = 27;
    static final int duckTwentyEightIndex = 28;
    static final int duckTwentyNineIndex = 29;
    static final int duckThirtyIndex = 30;
    static final int duckThirtyOneIndex = 31;
    static final int duckThirtyTwoIndex = 32;
    static final int duckThirtyThreeIndex = 33;
    static final int duckThirtyFourIndex = 34;
    static final int duckThirtyFiveIndex = 35;
    static final int duckThirtySixIndex = 36;
    static final int duckThirtySevenIndex = 37;
    static final int duckThirtyEightIndex = 38;
    static final int duckThirtyNineIndex = 39;
    static final int duckFortyIndex = 40;
    static final int duckFortyOneIndex = 41;
    static final int duckFortyTwoIndex = 42;
    static final int duckFortyThreeIndex = 43;
    static final int duckFortyFourIndex = 44;
    static final int duckFortyFiveIndex = 45;
    static final int duckFortySixIndex = 46;
    static final int duckFortySevenIndex = 47;
    static final int duckFortyEightIndex = 48;
    static final int duckFortyNineIndex = 49;
    static final int duckFiftyIndex = 50;
    static final int spawnLocCenterOneIndex = 51;
    static final int spawnLocCenterTwoIndex = 52;
    static final int spawnLocCenterThreeIndex = 53;
    static final int numberOfScoutsIndex = 54;
    static final int numberOfBuildersIndex = 55;

    static final int maxNumberOfScout = 30;
    static final int maxNumberOfBuilder = 20;

    static boolean isScout = false;
    static boolean isBomber = false;
    static boolean isBuilder = false;
    static boolean isCommander = false;
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Create objects for the other files
        Movement movement = new Movement(rc, lefty);
        Utility util = new Utility(rc);

        // before strategies
        Scout scout = new Scout(rc, movement, util);

        // after strategies
        Bomber bomber = new Bomber(rc, movement, util);

        // either strategies
        Unspecialized unspecialized = new Unspecialized(rc, movement, util);
        Builder builder = new Builder(rc, movement, util);
        Commander commander = new Commander(rc, movement, util);

        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            try {
                if (rc.getRoundNum() == 1) {
                    localID = util.makeLocalID(assigningLocalIDIndex);
                    movement.setLefty((localID % 2) == 1);
                }

                if (turnCount == 1) {
                    rc.setIndicatorString("I am robot #" + localID);
                }

                if (!rc.isSpawned()) {
                    boolean didSpawn = util.trySpawning();
                } else {
                    // ----------------------------------------
                    // logic for who will specialize to what (subject to change idrk what im doing)
                    // ----------------------------------------

                    //Scouters
                    if (rc.getRoundNum() < 200) {
                        int amountOfScouts = rc.readSharedArray(numberOfScoutsIndex);
                        int amountOfBuilders = rc.readSharedArray(numberOfBuildersIndex);
                        if (amountOfScouts < maxNumberOfScout) {
                            isScout = true;
                            rc.writeSharedArray(numberOfScoutsIndex, amountOfScouts + 1);
                        } else if (amountOfBuilders < maxNumberOfBuilder) {
                            isBuilder = false;
                            rc.writeSharedArray(numberOfBuildersIndex, amountOfBuilders + 1);
                        }
                    }

                    if (rc.getRoundNum() >= 200) {
                        // set all the before divider specializations to false
                        isScout = false;
                    }

                    // ----------------------------------------
                    // big switch statement thing for what strategy the robot will run for this turn
                    // ----------------------------------------

                    if (rc.getRoundNum() <= 200) { // before divider drop strategies
                        if (isCommander) { // no commanders rn
                            commander.run();
                        } else if (isScout) { // rn we make 30 scouts
                            scout.run();
                        } else if (isBuilder) { // and 20 builders, so all 50 ducks get specialized, so none hit the default case
                            builder.run();
                        } else { // none unspecialized rn (all taken up to be scouts or builders)
                            unspecialized.run();
                        }
                    } else { // after divider drop strategies
                        if (isCommander) {
                            commander.run();
                        } else if (isBomber) { // none rn
                            bomber.run();
                        } else if (isBuilder) { // carry over of 20 builders assigned before divider drop
                            builder.run();
                        } else { // so 30 bots switch from scout to unspecialized
                            unspecialized.run();
                        }
                    }

                    util.writeWhereYouAreToArray(localID);
                }

            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                Clock.yield();
            }
        }
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
