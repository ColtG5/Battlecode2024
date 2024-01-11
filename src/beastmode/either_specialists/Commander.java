package beastmode.either_specialists;

import battlecode.common.RobotController;
import beastmode.Movement;
import beastmode.Utility;

public strictfp class Commander {
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

