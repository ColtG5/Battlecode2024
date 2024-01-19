package BAMFF;

import battlecode.common.*;

import java.util.ArrayList;

public class Symmetry {
    RobotController rc;
    Utility utility;
    boolean isHorizontal = true;
    boolean isVertical = true;
    boolean isRotational = true;
    int W, H;
    int symX, symY;
    MapLocation middleOfMap;
    MapLocation[] broadcastFlags;


    Symmetry(RobotController rc, Utility utility) {
        this.rc = rc;
        this.utility = utility;
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        middleOfMap = new MapLocation(W / 2, H / 2);
        broadcastFlags = rc.senseBroadcastFlagLocations();
    }

    void checkSymmetry(MapLocation[] spawnCenters) throws GameActionException {
        if (rc.getRoundNum() == 10) {
            rc.writeSharedArray(60, 1);
            rc.writeSharedArray(61, 1);
            rc.writeSharedArray(62, 1);
        }

        if (isHorizontal && !isVertical && !isRotational) {
            System.out.println("HORIZONTAL SYMMETRY");
            return;
        }
        if (!isHorizontal && isVertical && !isRotational) {
            System.out.println("VERTICAL SYMMETRY");
            return;
        }
        if (!isHorizontal && !isVertical && isRotational) {
            System.out.println("ROTATIONAL SYMMETRY");
            return;
        }


        isHorizontal = rc.readSharedArray(60) != 0;
        isVertical = rc.readSharedArray(61) != 0;
        isRotational = rc.readSharedArray(62) != 0;

        rc.setIndicatorDot(broadcastFlags[0], 0, 0, 0);
        rc.setIndicatorDot(broadcastFlags[1], 0, 0, 0);
        rc.setIndicatorDot(broadcastFlags[2], 0, 0, 0);

        for (MapLocation spawn : spawnCenters) {
            if (isVertical) {
                symX = spawn.x;
                symY = H - spawn.y - 1;
                MapLocation loc = new MapLocation(symX, symY);
                rc.setIndicatorDot(loc, 255, 0, 0);

                if (rc.canSenseLocation(loc)) {
                    FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
                    if (flags.length == 0) eliminateVertical();
                    for (FlagInfo flag : flags) {
                        if (!flag.getLocation().equals(loc)) eliminateVertical();
                    }
                }
            }

            if (isHorizontal) {
                symX = W - spawn.x - 1;
                symY = spawn.y;
                MapLocation loc = new MapLocation(symX, symY);
                rc.setIndicatorDot(loc, 0, 255, 0);

                if (rc.canSenseLocation(loc)) {
                    FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
                    if (flags.length == 0) eliminateHorizontal();
                    for (FlagInfo flag : flags) {
                        if (!flag.getLocation().equals(loc)) eliminateHorizontal();
                    }
                }
            }

            if (isRotational) {
                symX = W - spawn.x - 1;
                symY = H - spawn.y - 1;
                MapLocation loc = new MapLocation(symX, symY);
                rc.setIndicatorDot(loc, 0, 0, 255);

                if (rc.canSenseLocation(loc)) {
                    FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
                    if (flags.length == 0) eliminateRotational();
                    for (FlagInfo flag : flags) {
                        if (!flag.getLocation().equals(loc)) eliminateRotational();
                    }
                }
            }
        }
    }

    void eliminateHorizontal() throws GameActionException {
        if (!isHorizontal) return;
        isHorizontal = false;
        rc.writeSharedArray(60, 0);
    }

    void eliminateVertical() throws GameActionException {
        if (!isVertical) return;
        isVertical = false;
        rc.writeSharedArray(61, 0);
    }

    void eliminateRotational() throws GameActionException {
        if (!isRotational) return;
        isRotational = false;
        rc.writeSharedArray(62, 0);
    }
}
