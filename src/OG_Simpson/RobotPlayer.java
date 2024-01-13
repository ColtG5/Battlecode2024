package OG_Simpson;

import battlecode.common.*;
import OG_Simpson.before_specialists.*;
import OG_Simpson.after_specialists.*;
import OG_Simpson.either_specialists.*;

import java.util.ArrayList;
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
    static MapLocation[] spawnAreaCenters;
    static MapLocation spawnAreaCenter1;
    static MapLocation spawnAreaCenter2;
    static MapLocation spawnAreaCenter3;
    static boolean lefty = true; // do u pathfind favouring left first or right first

    // before divider drop
    static boolean isScout = false;
    static boolean isDefender = false;

    // after divider drop
    static boolean isBomber = false;
    static boolean isFlagrunner = false;
    static int whichFlagrunnerGroup;

    // either
    static boolean isCommander = false;
    public static Utility.CoolRobotInfo[] coolRobotInfoArray = new Utility.CoolRobotInfo[50];

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
     * [0,          1-50,          51,  52,  53,             54,    55,    56]
     * id     id's of all ducks    3 bread loc's      each flagRunner group move loc
     *
     */

    static final int assigningLocalIDIndex = 0;
    static final int breadLocOneIndex = 51;
    static final int breadLocTwoIndex = 52;
    static final int breadLocThreeIndex = 53;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws Exception {
        // Create objects for the other files
        Movement movement = new Movement(rc, lefty);
        Utility util = new Utility(rc);

        // before strategies
        Scout scout = new Scout(rc, movement, util);

        // after strategies
        Bomber bomber = new Bomber(rc, movement, util);
        Flagrunner flagrunner = new Flagrunner(rc, movement, util, lefty);
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
                    util.setLocalID(localID);

                    MapLocation[] spawnAreaCentersLocal = util.findCentersOfSpawnZones();
                    spawnAreaCenters = spawnAreaCentersLocal;
                    spawnAreaCenter1 = spawnAreaCentersLocal[0];
                    spawnAreaCenter2 = spawnAreaCentersLocal[1];
                    spawnAreaCenter3 = spawnAreaCentersLocal[2];

//                    util.setInitialGroupLeaders();
//
//                    whichFlagrunnerGroup = util.getMyFlagrunnerGroup();
                }

                if (localID == 1) {
                    if (rc.getRoundNum() == GameConstants.GLOBAL_UPGRADE_ROUNDS && rc.canBuyGlobal(GlobalUpgrade.ACTION)) rc.buyGlobal(GlobalUpgrade.ACTION);
                    if (rc.getRoundNum() == GameConstants.GLOBAL_UPGRADE_ROUNDS * 2 && rc.canBuyGlobal(GlobalUpgrade.HEALING)) rc.buyGlobal(GlobalUpgrade.HEALING);
                }

                // read every other robots info from the shared array, store it in coolRobotInfoArray
                coolRobotInfoArray = util.readAllBotsInfoFromSharedArray(coolRobotInfoArray);

                if (!rc.isSpawned()) {
                    if (rc.getRoundNum() != 1 && isDefender) defender.tryToSpawnOnFlag();
                    util.trySpawningEvenly(spawnAreaCenters);
                }
                if (rc.isSpawned()) {

                    // ----------------------------------------
                    // start of turn logic
                    // ----------------------------------------

                    // writes the first 3 bread locations into the shared array, and also checks if this bot is a defender
//                    if (rc.getRoundNum() == 1) {
//                    }
                    FlagInfo[] myFlags = rc.senseNearbyFlags(2, rc.getTeam());
                    if (myFlags.length != 0) {
                        if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation()))
                            rc.build(TrapType.EXPLOSIVE, rc.getLocation());
                    }

                    // ----------------------------------------
                    // logic for who will specialize to what (subject to change idrk what im doing ong no cap on 4nem)
                    // ----------------------------------------

                    if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS - 20) {
                        if (!isDefender) isScout = true;
                    } else {
                        if (isScout) isFlagrunner = true;
                        isScout = false;
                    }

//                    if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS) {
//                        if (isScout) isFlagrunner = true; // change all scouts to flagrunners
//
//                        // set all the before divider specializations to false just to make sure no one is running them
//                        isScout = false;
//                    }
//
//                    if (rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
//                        // setting specializations after the setup rounds
//                    }

                    // ----------------------------------------
                    // big switch statement thing for what strategy the robot will run for this turn
                    // ----------------------------------------

                    if (rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) { // before divider drop strategies
                        if (rc.getRoundNum() == 1) continue; // dont let bots move on the first turn
                        else if (isCommander) commander.run();  // no commanders rn
                        else if (isScout) scout.run(); // rn we make 30 scouts
                        else if (isDefender) defender.run();
                        else if (isFlagrunner) flagrunner.run();
                        else unspecialized.run(); // none unspecialized rn (all taken up to be scouts)
                    } else { // after divider drop strategies
                        if (isCommander) commander.run();
                        else if (isBomber) bomber.run(); // none rn
                        else if (isFlagrunner) flagrunner.run(); // so 30 bots switch from scout to unspecialized
                        else if (isDefender) defender.run();
                        else unspecialized.run();
                    }

                    // ----------------------------------------
                    // end of turn stuff
                    // ----------------------------------------

                }

                // after every round whether spawned or not, convert your info to an int and write it to the shared array
                util.writeMyInfoToSharedArray();

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
}