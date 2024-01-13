package RedRising.either_specialists;

import RedRising.Movement;
import RedRising.Utility;
import battlecode.common.RobotController;

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
