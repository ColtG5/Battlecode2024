package OGOG_Simpson.either_specialists;

import battlecode.common.RobotController;
import OGOG_Simpson.Movement;
import OGOG_Simpson.Utility;

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

