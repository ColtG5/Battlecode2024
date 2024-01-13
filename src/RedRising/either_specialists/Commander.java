package RedRising.either_specialists;

import RedRising.Movement;
import RedRising.Utility;
import battlecode.common.RobotController;

public class Commander {
    RobotController rc;
    Movement movement;
    Utility utility;

    public Commander(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() {

    }
}

