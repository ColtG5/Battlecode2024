package GoldenSon.after_specialists;

import GoldenSon.Movement;
import GoldenSon.Utility;
import battlecode.common.*;

import java.util.Map;

import static GoldenSon.RobotPlayer.coolRobotInfoArray;

/**
 * Just a base strategy class, if a bot doesn't specialize in any strategy (not entirely sure if needed, but just for now)
 */
public class Flagrunner {
    RobotController rc;
    Movement movement;
    Utility utility;
    MapLocation locationForFlagrunnerGroup;

    public Flagrunner(RobotController rc, Movement movement, Utility utility, boolean lefty) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() throws GameActionException {
        if (utility.amIAGroupLeader()) locationForFlagrunnerGroup = setLocationForGroup();
        else locationForFlagrunnerGroup = getLocationForGroup();




    }

    public MapLocation setLocationForGroup() {
        // decide what location the group gonna have
        MapLocation locForGroup = new MapLocation(42, 42);

        // write this locForGroup into the spot in the shared array for this group
        int groupNumber = utility.getMyFlagrunnerGroup();
        int indexInSharedArray = utility.getFlagrunnerGroupIndex(groupNumber);

        return locForGroup;
    }

    public MapLocation getLocationForGroup() {

    }

}