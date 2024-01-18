package BAMFF;

import BAMFF.specialists.Defender;
import BAMFF.specialists.Flagrunner;
import BAMFF.specialists.Scout;
import battlecode.common.*;

import java.util.Arrays;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {
    static int turnCount = 0;
    static final MapLocation NONELOCATION = new MapLocation(-1, -1);

    static int localID;
    public static MapLocation[] spawnAreaCenters;
    public static MapLocation spawnAreaCenter1;
    public static MapLocation spawnAreaCenter2;
    public static MapLocation spawnAreaCenter3;
    static boolean lefty = true; // do u pathfind favouring left first or right first
    static boolean isScout = false;
    static boolean isDefender = false;
    static boolean isFlagrunner = false;
    public static int FLAGRUNNERS_IN_GROUP_1 = 16;
//    public static final int INITIAL_GROUP_1_LEADER_ID = 1;
//    public static final int FLAGRUNNER_BUILDER_GROUP_1_ID = INITIAL_GROUP_1_LEADER_ID + 1;

    public static int FLAGRUNNERS_IN_GROUP_2 = 16;
//    public static final int INITIAL_GROUP_2_LEADER_ID = FLAGRUNNERS_IN_GROUP_1 + 1;
//    public static final int FLAGRUNNER_BUILDER_GROUP_2_ID = INITIAL_GROUP_2_LEADER_ID + 1;

    public static int FLAGRUNNERS_IN_GROUP_3 = 15;
//    public static final int INITIAL_GROUP_3_LEADER_ID = FLAGRUNNERS_IN_GROUP_1 + FLAGRUNNERS_IN_GROUP_2 + 1;
//    public static final int FLAGRUNNER_BUILDER_GROUP_3_ID = INITIAL_GROUP_3_LEADER_ID + 1;

    public static final int AMOUNT_OF_FLAGRUNNERS = FLAGRUNNERS_IN_GROUP_1 + FLAGRUNNERS_IN_GROUP_2 + FLAGRUNNERS_IN_GROUP_3; // must be divisible by FLAGRUNNERS_PER_GROUP | must add up to all groups added together

    public static Utility.CoolRobotInfo[] coolRobotInfoArray = new Utility.CoolRobotInfo[50];

    /**
     * SHARED ARRAY
     * [0,          1-50,          51,  52,  53,                      54, 55   56, 57   58, 59]
     * id     id's of all ducks    3 bread loc's   flag runner group: loc,id   loc,id   loc,id
     *
     */

    static final int assigningLocalIDIndex = 0;
    static final int breadLocOneIndex = 51;
    static final int breadLocTwoIndex = 52;
    static final int breadLocThreeIndex = 53;
//    static final int flagRunnerGroupIndexingStart = 53;
    static final int flagRunnerGroupOneLocIndex = 54;
    static final int flagRunnerGroupOneIDIndex = 55;
    static final int flagRunnerGroupTwoLocIndex = 56;
    static final int flagRunnerGroupTwoIDIndex = 57;
    static final int flagRunnerGroupThreeLocIndex = 58;
    static final int flagRunnerGroupThreeIDIndex = 59;

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

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws Exception {
        // Create objects for the other files
        Movement movement = new Movement(rc, lefty);
        Utility utility = new Utility(rc);

        // before strategies
        Scout scout = new Scout(rc, movement, utility);

        // after strategies
        Flagrunner flagrunner = new Flagrunner(rc, movement, utility);
        Defender defender = new Defender(rc, movement, utility);

        // either strategies

        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            try {
                if (rc.getRoundNum() == 1) {
                    localID = utility.makeLocalID(assigningLocalIDIndex);
                    movement.setLefty((localID % 2) == 1);
                    utility.setLocalID(localID);
                    defender.setLocalID(localID);
                    flagrunner.setLocalID(localID);
                    scout.setLocalID(localID);

                    MapLocation[] spawnAreaCentersLocal = utility.findCentersOfSpawnZones();
                    spawnAreaCenters = spawnAreaCentersLocal;
                    spawnAreaCenter1 = spawnAreaCentersLocal[0];
                    spawnAreaCenter2 = spawnAreaCentersLocal[1];
                    spawnAreaCenter3 = spawnAreaCentersLocal[2];
                    defender.setSpawnAreaCenters(spawnAreaCentersLocal);
                    flagrunner.setSpawnAreaCenters(spawnAreaCentersLocal);

                    if (48 <= localID && localID <= 50) isDefender = true;
                }

                if (localID == 1) {
                    if (rc.getRoundNum() == 1) utility.setInitialGroupLeaders();

                    if (rc.getRoundNum() == GameConstants.GLOBAL_UPGRADE_ROUNDS && rc.canBuyGlobal(GlobalUpgrade.ATTACK)) rc.buyGlobal(GlobalUpgrade.ATTACK);
                    if (rc.getRoundNum() == GameConstants.GLOBAL_UPGRADE_ROUNDS * 2 && rc.canBuyGlobal(GlobalUpgrade.HEALING)) rc.buyGlobal(GlobalUpgrade.HEALING);
                    if (rc.getRoundNum() == GameConstants.GLOBAL_UPGRADE_ROUNDS * 3 && rc.canBuyGlobal(GlobalUpgrade.CAPTURING)) rc.buyGlobal(GlobalUpgrade.CAPTURING);
                }

                // read every other robots info from the shared array, store it in coolRobotInfoArray
                coolRobotInfoArray = utility.readAllBotsInfoFromSharedArray(coolRobotInfoArray);
                utility.setCoolRobotInfoArray(coolRobotInfoArray);
                flagrunner.setCoolRobotInfoArray(coolRobotInfoArray);
                defender.setCoolRobotInfoArray(coolRobotInfoArray);


                if (!rc.isSpawned()) {
                    if (isDefender) defender.tryToSpawnOnMyFlag();
                    else if (rc.getRoundNum() > 200) {
                        utility.spawnDefend();
                        utility.trySpawningEvenly(spawnAreaCenters);
                    }
                    else utility.trySpawningEvenly(spawnAreaCenters);
                    utility.writeMyInfoToSharedArray(false);
                }
                if (rc.isSpawned()) {

                    // ----------------------------------------
                    // start of turn logic
                    // ----------------------------------------

                    // writes the first 3 bread locations into the shared array, and also checks if this bot is a defender
                    if (rc.getRoundNum() == 1) utility.didISpawnOnFlag(utility);

                    // ----------------------------------------
                    // logic for who will specialize to what (subject to change idrk what im doing ong no cap on 4nem)
                    // ----------------------------------------

                    if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS - 40) {
                        if (!isDefender) isScout = true; // set the proper scouts
                    } else if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
                        if (localID <= AMOUNT_OF_FLAGRUNNERS) isFlagrunner = true; // set the proper flagrunners

                        // set all the before divider specializations to false just to make sure no one is running them
                        isScout = false;
                    }

                    // ----------------------------------------
                    // big switch statement thing for what strategy the robot will run for this turn
                    // ----------------------------------------

                    if (rc.getRoundNum() <= GameConstants.SETUP_ROUNDS) { // before divider drop strategies
                        if (rc.getRoundNum() == 1) continue; // don't let bots move on the first turn (I forget why but there's prob a reason)
                        else if (isDefender) defender.run();
                        else if (isScout) scout.run(); // rn we make 30 scouts
                        else if (isFlagrunner) flagrunner.run();
                    } else { // after divider drop strategies
                        if (isFlagrunner) {
                            utility.handleIfGroupLeaderDied(); // switches group leaders if they died, to the bot checking
                            flagrunner.run();
                        }
                        else if (isDefender) defender.run();
                    }

                    // ----------------------------------------
                    // end of turn stuff
                    // ----------------------------------------

//                    if (rc.getRoundNum() == 410) rc.resign();

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

    public static Utility.CoolRobotInfo[] getCoolRobotInfoArray() {
        return coolRobotInfoArray;
    }
}