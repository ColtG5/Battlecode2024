package GoldenSon.either_specialists;

import GoldenSon.Movement;
import GoldenSon.Utility;
import battlecode.common.*;

import java.util.ArrayList;

public class Builder {
    RobotController rc;
    Movement movement;
    Utility utility;

    public Builder(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() throws GameActionException {
        // Don't move when farming exp
        if ((rc.getExperience(SkillType.BUILD) < 30 && rc.getRoundNum() < GameConstants.SETUP_ROUNDS) ||
                (rc.getExperience(SkillType.BUILD) < 20 && rc.getRoundNum() > GameConstants.SETUP_ROUNDS)) {
            farmEXP();
            return;
        }

        // FOr testing only
        MapLocation[] enemyFlags = rc.senseBroadcastFlagLocations();
        for (MapLocation flag : enemyFlags) {
            movement.hardMove(flag);
        }

        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length > 3 && rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
            placeBombs();
        }
    }

    private void farmEXP() throws GameActionException {
        MapInfo[] infos = rc.senseNearbyMapInfos(-1);
        for (MapInfo info : infos) {
            if (info.isWater()) {
                if (rc.canFill(info.getMapLocation()))
                    rc.fill(info.getMapLocation());
            } else {
                if (rc.canDig(info.getMapLocation()))
                    rc.dig(info.getMapLocation());
            }
        }
    }

    private void placeBombs() throws GameActionException {
//       MapInfo[] infoAround = rc.senseNearbyMapInfos(GameConstants.INTERACT_RADIUS_SQUARED);
//       for (MapInfo info : infoAround) {
//           if (rc.canBuild(TrapType.EXPLOSIVE, info.getMapLocation()))
//               rc.build(TrapType.EXPLOSIVE, info.getMapLocation());
//       }
        MapLocation me = rc.getLocation();
        ArrayList<MapLocation> checkerBoardPlacement = new ArrayList<>();
        checkerBoardPlacement.add(me);
        checkerBoardPlacement.add(me.add(Direction.NORTHEAST));
        checkerBoardPlacement.add(me.add(Direction.NORTHWEST));
        checkerBoardPlacement.add(me.add(Direction.SOUTHWEST));
        checkerBoardPlacement.add(me.add(Direction.SOUTHEAST));

        for (MapLocation location : checkerBoardPlacement) {
            if (rc.canBuild(TrapType.EXPLOSIVE, location))
               rc.build(TrapType.EXPLOSIVE, location);
        }
    }
}
