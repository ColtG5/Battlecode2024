package beastmode.either_specialists;

import battlecode.common.*;
import beastmode.Movement;
import beastmode.Utility;

public class Defender {
    RobotController rc;
    Movement movement;
    Utility utility;

    public Defender(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() throws GameActionException {

    }
}
