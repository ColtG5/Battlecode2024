package franklin;

import battlecode.common.*;

import java.util.Random;

public strictfp class Bomber {
    static final Random rng = new Random(6147);
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
        if (potentialFlags.length != 0) {
            MapLocation flag = potentialFlags[rng.nextInt(potentialFlags.length)];
            if (flag != null) {
                movement.hardMove(flag);
            }
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        FlagInfo[] enemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        if (enemies.length >= 5 && enemyFlags.length != 0) tryToPlaceBomb(rc.getLocation());
    }
}
