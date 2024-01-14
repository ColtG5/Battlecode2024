package OGOG_Simpson.either_specialists;

import battlecode.common.RobotController;
import OGOG_Simpson.Movement;
import OGOG_Simpson.Utility;

/**
 * Just a base strategy class, if a bot doesn't specialize in any strategy (not entirely sure if needed, but just for now)
 */
public class Unspecialized {
    RobotController rc;
    Movement movement;
    Utility utility;

    public Unspecialized(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() {

    }
}
