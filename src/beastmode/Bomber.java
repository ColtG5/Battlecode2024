package beastmode;

import battlecode.common.*;

public strictfp class Bomber {
    RobotController rc;
    Movement movement;

    public Bomber(RobotController rc, boolean lefty) {
        this.rc = rc;
        this.movement = new Movement(rc, lefty);
    }

    /**
     * Attempt to place a bomb at the given location.
     * @param location MapLocation to build the bomb
     */
    public void tryToPlaceBomb(MapLocation location) throws GameActionException {
        if (rc.canBuild(TrapType.EXPLOSIVE, location))
            rc.build(TrapType.EXPLOSIVE, location);
    }

    /**
     * Go dive into the enemies to place a bomb.
     */
    public void kamikaze() throws GameActionException {
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
