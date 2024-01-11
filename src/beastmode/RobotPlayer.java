package beastmode;

import battlecode.common.*;
import beastmode.before_specialists.*;
import beastmode.after_specialists.*;
import beastmode.either_specialists.*;
import javafx.util.Pair;
import scala.Tuple1;

import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {
    static int turnCount = 0;
    static final MapLocation NONELOCATION = new MapLocation(-1, -1);

    static int localID;
    static boolean lefty = true; // do u pathfind favouring left first or right first

    // before divider drop
    static boolean isScout = false;
    static boolean isDefender = false;

    // after divider drop
    static boolean isBomber = false;
    static boolean isFlagrunner = false;

    // either
    static boolean isCommander = false;
    static Utility.CoolRobotInfo[] coolRobotInfoArray = new Utility.CoolRobotInfo[50];

    static final Random rng = new Random(6147);

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
     * [0,          1-50,          51,  52,  53]
     * id     id's of all ducks    3 spawn loc's
     *
     */

    static final int assigningLocalIDIndex = 0;
    static final int breadLocOneIndex = 51;
    static final int breadLocTwoIndex = 52;
    static final int breadLocThreeIndex = 53;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Create objects for the other files
        Movement movement = new Movement(rc, lefty);
        Utility util = new Utility(rc);

        // before strategies
        Scout scout = new Scout(rc, movement, util);

        // after strategies
        Bomber bomber = new Bomber(rc, movement, util);
        Flagrunner flagrunner = new Flagrunner(rc, movement, util);
        Defender defender = new Defender(rc, movement, util);

        // either strategies
        Unspecialized unspecialized = new Unspecialized(rc, movement, util);
        Commander commander = new Commander(rc, movement, util);

        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            try {
                if (rc.getRoundNum() == 1) {
                    localID = util.makeLocalID(assigningLocalIDIndex);
                    movement.setLefty((localID % 2) == 1);
                }

                if (!rc.isSpawned()) {
                    util.trySpawning();
                }
                if (rc.isSpawned()) {

                    // ----------------------------------------
                    // start of turn logic
                    // ----------------------------------------

                    // read every other robots info from the shared array, store it in coolRobotInfoArray
                    if (rc.getRoundNum() > 1) { // not all bots have written their stuff into their index until round 2 starts
                        for (int i = 1; i <= 50; i++) {
                            int coolRobotInfoInt = rc.readSharedArray(i);
                            coolRobotInfoArray[i-1] = util.new CoolRobotInfo(i, coolRobotInfoInt);
                        }
                    }

                    if (rc.getRoundNum() == 1) {
                        MapLocation me = rc.getLocation();
                        FlagInfo[] flags = rc.senseNearbyFlags(1, rc.getTeam());
                        if (flags.length > 0) {
                            MapLocation flagLoc = flags[0].getLocation();
                            if (me.x == flagLoc.x && me.y == flagLoc.y) {
                                if (rc.readSharedArray(breadLocOneIndex) == 0) {
                                    rc.writeSharedArray(breadLocOneIndex, util.locationToInt(me));
                                } else if (rc.readSharedArray(breadLocTwoIndex) == 0) {
                                    rc.writeSharedArray(breadLocTwoIndex, util.locationToInt(me));
                                } else if (rc.readSharedArray(breadLocThreeIndex) == 0) {
                                    rc.writeSharedArray(breadLocThreeIndex, util.locationToInt(me));
                                }
                            }
                        }
                    }

                    // ----------------------------------------
                    // logic for who will specialize to what (subject to change idrk what im doing ong no cap on 4nem)
                    // ----------------------------------------

                    if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
                        if (!isDefender) isScout = true;
                    }

                    if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS) {
                        if (isScout) isFlagrunner = true; // change all scouts to flagrunners

                        // set all the before divider specializations to false just to make sure no one is running them
                        isScout = false;
//                        isBuilder = false;
                    }

                    if (rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
                        // setting specializations after the setup rounds
                    }

                    // ----------------------------------------
                    // big switch statement thing for what strategy the robot will run for this turn
                    // ----------------------------------------

                    if (rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) { // before divider drop strategies
                        if (rc.getRoundNum() == 1) { // dont let bots move on the first turn

                        } else if (isCommander) { // no commanders rn
                            commander.run();
                        } else if (isScout) { // rn we make 30 scouts
                            scout.run();
                        } else { // none unspecialized rn (all taken up to be scouts)
                            unspecialized.run();
                        }
                    } else { // after divider drop strategies
                        if (isCommander) {
                            commander.run();
                        } else if (isBomber) { // none rn
                            bomber.run();
                        } else if (isFlagrunner){ // so 30 bots switch from scout to unspecialized
                            flagrunner.run();
                        } else {
                            unspecialized.run();
                        }
                    }

                    // ----------------------------------------
                    // end of turn stuff
                    // ----------------------------------------

                }

                // after every round whether spawned or not, convert your info to an int and write it to the shared array
                MapLocation locationToStore;
                if (rc.isSpawned()) locationToStore = rc.getLocation();
                else locationToStore = NONELOCATION; // NONELOCATION (-1, -1) represents not spawned rn (no location)
                int coolRobotInfoInt = util.convertRobotInfoToInt(locationToStore, false);
//                if (rc.getRoundNum() == 1) System.out.println("id: " + localID + "coolRobotInfoInt: " + Integer.toBinaryString(coolRobotInfoInt));
                rc.writeSharedArray(localID, coolRobotInfoInt);

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
            //rc.setIndicatorString("There are nearby enemy robots! Scary!");
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
