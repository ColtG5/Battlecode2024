package beastmode;

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
        // Create objects for the other files
        Movement movement = new Movement(rc, lefty);
        Bomber bomber = new Bomber(rc, lefty);
        Utility util = new Utility(rc);


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
