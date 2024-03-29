package GoldenSon.after_specialists;

import GoldenSon.Movement;
import GoldenSon.Utility;
import battlecode.common.*;

public class Bomber {
    RobotController rc;
    Movement movement;
    Utility utility;

    public Bomber(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() throws GameActionException {
        kamikaze();
    }

    /**
     * Go dive into the enemies to place a bomb.
     */
    private void kamikaze() throws GameActionException {
        movement.moveTowardsEnemyFlags();

        // Place bomb when near flag and have enemies around
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        FlagInfo[] enemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        if (enemies.length >= 5 && enemyFlags.length != 0) tryToPlaceBomb();
    }

    /**
     * Attempt to place a bomb at current location
     */
    private void tryToPlaceBomb() throws GameActionException {
        if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation()))
            rc.build(TrapType.EXPLOSIVE, rc.getLocation());
    }
}
