package beastmode.after_specialists;

import battlecode.common.*;
import beastmode.Movement;
import beastmode.Utility;

public class Bomber {
    RobotController rc;
    Movement movement;
    Utility utility;

    public Bomber(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() {

    }

    /**
     * Attempt to place a bomb at the given location.
     * @param location MapLocation to build the bomb
     */
    private void tryToPlaceBomb(MapLocation location) throws GameActionException {
        if (rc.canBuild(TrapType.EXPLOSIVE, location))
            rc.build(TrapType.EXPLOSIVE, location);
    }

    /**
     * Go dive into the enemies to place a bomb.
     */
    private void kamikaze() throws GameActionException {
        // Move to opponents with approximate flag locations
        MapLocation[] potentialFlags = rc.senseBroadcastFlagLocations();
        for (MapLocation flag : potentialFlags) {
            movement.simpleMove(flag);
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length >= 5 && !(rc.getRoundNum() <= GameConstants.SETUP_ROUNDS))
            tryToPlaceBomb(rc.getLocation());
    }
}
