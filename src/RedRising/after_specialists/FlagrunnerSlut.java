package RedRising.after_specialists;

import RedRising.Movement;
import RedRising.Utility;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class FlagrunnerSlut {
    RobotController rc;
    Movement movement;
    Utility utility;
    Flagrunner flagrunner;
    public FlagrunnerSlut(RobotController rc, Movement movement, Utility utility, Flagrunner flagrunner) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
        this.flagrunner = flagrunner;
    }

    static boolean KILLMODE = false;

    public void run() throws GameActionException {
        if (rc.hasFlag()) flagrunner.backToSpawn();
        //Assumptions: - they have been assigned a local ID and are not picked as a Flagrunner leader
        MapLocation leaderLoc = utility.getLocationOfMyGroupLeader();

        //read global array for the local ID of the Flagrunner leader
        //if no leader nearby, become leader and write to array
        boolean inRangeOfLeader = false;
        RobotInfo[] mapInfos =rc.senseNearbyRobots(-1,rc.getTeam());
        for(RobotInfo mapInfo:mapInfos){
            if(mapInfo.getLocation().equals(leaderLoc)){
                inRangeOfLeader = true;
                break;
            }
        }
        if(inRangeOfLeader){
           flagrunner.run();

        }else{
            KILLMODE = false;
            movement.hardMove(leaderLoc);
            flagrunner.attackTheLocals();
        }

        //check where leader is and if not in sensor range, move towards them while attacking and avoiding enemies

        //else they are in sensor range, so they will scan number of enemies and allies in sensor range

        //check if KILLMODE is on, if so, kill enemies as the method below describes

        //if there are 6 or more enemies, enter killmode and kill enemies (HOWEVER WE NEED THEM TO NOT LEAVE THE FLAGRUNNER LEADER)
        //This will require a new whipe them out function that acts the same but gets the lowest health enemy to the leader and moves towards them
        //cheeky bomb can be used here as well

        //otherwise, heal eachother and the leader if no enemies around

        //move towards the leader if KILLMODE off, while attacking other bots

        //if flag within range than just run flagrunner code by using flagrunner.run()

    }
}
