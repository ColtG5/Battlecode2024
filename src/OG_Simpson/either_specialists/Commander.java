package OG_Simpson.either_specialists;

import battlecode.common.RobotController;
import OG_Simpson.Movement;
import OG_Simpson.Utility;

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

