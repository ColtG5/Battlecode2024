package tester;

import battlecode.common.*;

import java.util.Map;
import java.util.Random;

public strictfp class RobotPlayer {

    static int turnCount = 0;
    static int localID = 0;
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
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Movement mv = new Movement(rc);
        Utility util = new Utility(rc);
        while (true) {
            turnCount += 1;  // We have now been alive for one more turn!
            try {
                // only let first bot spawn for testing purposes
                if (rc.getRoundNum() == 1 && rc.readSharedArray(0) == 0) {
                    rc.writeSharedArray(0, 1);
                    localID = 5;
                    rc.spawn(new MapLocation(4, 22));
                }
                if (localID != 5) Clock.yield();

//                util.trySpawning();

                if (!rc.isSpawned()) Clock.yield();

                rc.setIndicatorString("" + rc.getLocation());

                mv.simpleMove(new MapLocation(17, 22));

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
    }
}
