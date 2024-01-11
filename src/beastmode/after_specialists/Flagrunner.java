package beastmode.after_specialists;

import battlecode.common.*;
import beastmode.Movement;
import beastmode.Utility;

/**
 * Just a base strategy class, if a bot doesn't specialize in any strategy (not entirely sure if needed, but just for now)
 */
public class Flagrunner {
    RobotController rc;
    Movement movement;
    Utility utility;

    public Flagrunner(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() throws GameActionException {
        if (rc.hasFlag()) backToSpawn();
        else fetchFlag();
    }

    private void backToSpawn() throws GameActionException {
        // This array will never be null
        MapLocation[] spawnLocations = rc.getAllySpawnLocations();
        MapLocation closestSpawn = spawnLocations[0];
        for (MapLocation spawn : spawnLocations) {
            if (rc.getLocation().distanceSquaredTo(spawn) < rc.getLocation().distanceSquaredTo(closestSpawn))
                closestSpawn = spawn;
        }
        movement.hardMove(closestSpawn);
    }

    private void fetchFlag() throws GameActionException {
        movement.moveTowardsEnemyFlags();

        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        MapLocation closestFlag = null;
        for (FlagInfo flag : flags) {
            if (closestFlag == null) closestFlag = flag.getLocation();
            else if (rc.getLocation().distanceSquaredTo(flag.getLocation()) <
                    rc.getLocation().distanceSquaredTo(closestFlag)) {
                closestFlag = flag.getLocation();
            }
        }
        if (closestFlag != null) movement.simpleMove(closestFlag);
        if (closestFlag != null && rc.getLocation().isAdjacentTo(closestFlag) && rc.canPickupFlag(closestFlag))
            rc.pickupFlag(closestFlag);
    }
}
