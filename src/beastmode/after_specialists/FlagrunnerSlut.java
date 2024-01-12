package beastmode.after_specialists;

import battlecode.common.*;
import beastmode.Movement;
import beastmode.Utility;
import beastmode.after_specialists.Flagrunner;
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
            RobotInfo[] robotEnemyInfo = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            RobotInfo[] robotInfo = rc.senseNearbyRobots(-1, rc.getTeam());
            if(KILLMODE){
                if (robotEnemyInfo.length == 0) KILLMODE = false;
                else {
                    rc.setIndicatorString("ITS KILLMODE TIME");
                    flagrunner.wipeThemOut(robotEnemyInfo);
                    return;
                }
            }
            if (robotEnemyInfo.length == 0 && !rc.hasFlag()) {
                if (rc.getHealth() <= TrapType.EXPLOSIVE.enterDamage) {
                    if (rc.canHeal(rc.getLocation())) {
                        rc.heal(rc.getLocation());
                        return;
                    }
                }
                for (RobotInfo info : robotInfo) {
                    if (info.getHealth() < GameConstants.DEFAULT_HEALTH) {
                        if (rc.canHeal(info.getLocation())) {
                            rc.heal(info.getLocation());
                            return;
                        }
                    }
                }
            }
            for (RobotInfo info : robotInfo) {
                if (info.hasFlag) {
                    rc.setIndicatorString("I am a flagrunner and I am following the flag");
                    flagrunner.followFlag(info.getLocation());
                    flagrunner.attackTheLocals();
                    return;
                }
            }
            if (robotEnemyInfo.length >= 6) {
                KILLMODE = true;
                flagrunner.wipeThemOut(robotEnemyInfo);
                return;
            }

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
