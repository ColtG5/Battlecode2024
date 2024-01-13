package GoldenSon.after_specialists;

import GoldenSon.Movement;
import GoldenSon.Utility;
import battlecode.common.*;

import static GoldenSon.RobotPlayer.coolRobotInfoArray;

/**
 * Just a base strategy class, if a bot doesn't specialize in any strategy (not entirely sure if needed, but just for now)
 */
public class Flagrunner {
    RobotController rc;
    Movement movement;
    Utility utility;

    public Flagrunner(RobotController rc, Movement movement, Utility utility, boolean lefty) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() throws GameActionException {

    }
}