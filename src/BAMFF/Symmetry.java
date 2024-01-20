package BAMFF;

import battlecode.common.*;

public class Symmetry {
    RobotController rc;
    Utility utility;
    boolean isHorizontal = true;
    boolean isVertical = true;
    boolean isRotational = true;
    int W, H;
    int symX, symY;
    Integer flagStolenIndex = null;
    boolean sym = false;
    MapLocation[] possibleFlagLocations;
    MapLocation spawn1;
    MapLocation spawn2;
    MapLocation spawn3;
    MapLocation flagStolen = null;


    Symmetry(RobotController rc, Utility utility) {
        this.rc = rc;
        this.utility = utility;
        H = rc.getMapHeight();
        W = rc.getMapWidth();
    }

    public boolean getSymmetry() {
        return sym;
    }
    public MapLocation[] getPossibleFlagLocations() {
        return possibleFlagLocations;
    }

    public void updatePossibleFlagLocations(boolean iStoleFlag) throws GameActionException {
        if (iStoleFlag) {
            for (int i = 51; i <= 53; i++) {
                if (flagStolen.equals(utility.intToLocation(rc.readSharedArray(i)))) {
                    rc.writeSharedArray(i, 0);
                    rc.writeSharedArray(61 + flagStolenIndex, 0);
                }
            }
        }

        int flagsLeft = 0;
        for (int i = 51; i <= 53; i++) {
            if (rc.readSharedArray(i) != 0) flagsLeft++;
        }

        if (flagsLeft == 1) {
            possibleFlagLocations = new MapLocation[1];
            for (int i = 51; i <= 53; i++) {
                if (rc.readSharedArray(i) != 0)
                    possibleFlagLocations[0] = utility.intToLocation(rc.readSharedArray(i));
            }
        }

        if (flagsLeft == 2) {
            possibleFlagLocations = new MapLocation[2];
            int j = 0;
            for (int i = 51; i <= 53; i++) {
                if (rc.readSharedArray(i) != 0) {
                    possibleFlagLocations[j] = utility.intToLocation(rc.readSharedArray(i));
                    j++;
                }
            }
        }

        for (int i = 0; i < possibleFlagLocations.length; i++) {
            MapLocation flag = possibleFlagLocations[i];

            if (rc.canSenseLocation(flag) && rc.hasFlag()) {
                flagStolenIndex = i;
                flagStolen = possibleFlagLocations[flagStolenIndex];
                break;
            }

            int combined = rc.readSharedArray(61 + i);
            if (combined != 0 && flagStolenIndex == null) {
                int flagDroppedLocation = combined >> 2;
                MapLocation prevFlagLoc = utility.intToLocation(flagDroppedLocation);
                flagStolenIndex = combined & 0b11;

                if (rc.canSenseLocation(prevFlagLoc)) {
                    flagStolen = possibleFlagLocations[flagStolenIndex];
                    break;
                }
            }
        }

        if (rc.hasFlag()) {
            int locationToInt = utility.locationToInt(rc.getLocation());
            int combined = (locationToInt << 2) | flagStolenIndex;
            rc.writeSharedArray(61 + flagStolenIndex, combined);
            System.out.println("RC FLAG INDEX: " + flagStolenIndex);
        }
    }

    void updateSymmetry() throws GameActionException {
        if (isHorizontal && !isVertical && !isRotational) {
            sym = true;
            possibleFlagLocations = new MapLocation[3];
            possibleFlagLocations[0] = new MapLocation(W - spawn1.x - 1, spawn1.y);
            possibleFlagLocations[1] = new MapLocation(W - spawn2.x - 1, spawn2.y);
            possibleFlagLocations[2] = new MapLocation(W - spawn3.x - 1, spawn3.y);
            rc.writeSharedArray(51, utility.locationToInt(possibleFlagLocations[0]));
            rc.writeSharedArray(52, utility.locationToInt(possibleFlagLocations[1]));
            rc.writeSharedArray(53, utility.locationToInt(possibleFlagLocations[2]));
            return;
        }
        if (!isHorizontal && isVertical && !isRotational) {
            sym = true;
            possibleFlagLocations = new MapLocation[3];
            possibleFlagLocations[0] = new MapLocation(spawn1.x, H - spawn1.y - 1);
            possibleFlagLocations[1] = new MapLocation(spawn2.x, H - spawn2.y - 1);
            possibleFlagLocations[2] = new MapLocation(spawn3.x, H - spawn3.y - 1);
            rc.writeSharedArray(51, utility.locationToInt(possibleFlagLocations[0]));
            rc.writeSharedArray(52, utility.locationToInt(possibleFlagLocations[1]));
            rc.writeSharedArray(53, utility.locationToInt(possibleFlagLocations[2]));
            return;
        }
        if (!isHorizontal && !isVertical && isRotational) {
            sym = true;
            possibleFlagLocations = new MapLocation[3];
            possibleFlagLocations[0] = new MapLocation(W - spawn1.x - 1, H - spawn1.y - 1);
            possibleFlagLocations[1] = new MapLocation(W - spawn2.x - 1, H - spawn2.y - 1);
            possibleFlagLocations[2] = new MapLocation(W - spawn3.x - 1, H - spawn3.y - 1);
            rc.writeSharedArray(51, utility.locationToInt(possibleFlagLocations[0]));
            rc.writeSharedArray(52, utility.locationToInt(possibleFlagLocations[1]));
            rc.writeSharedArray(53, utility.locationToInt(possibleFlagLocations[2]));
        }
    }

    void checkSymmetry(MapLocation[] spawnCenters) throws GameActionException {
        if (rc.getRoundNum() == 10) {
            int combined = ((isHorizontal ? 1 : 0) << 2 | (isVertical ? 1 : 0) << 1 | (isRotational ? 1 : 0));
            rc.writeSharedArray(60, combined);
            spawn1 = spawnCenters[0];
            spawn2 = spawnCenters[1];
            spawn3 = spawnCenters[2];
        }

        if (isHorizontal) isHorizontal = ((rc.readSharedArray(60) >> 2) & 1) == 1;
        if (isVertical) isVertical = ((rc.readSharedArray(60) >> 1) & 1) == 1;
        if (isRotational) isRotational = (rc.readSharedArray(60) & 1) == 1;

        if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS - 40) {
            possibleFlagLocations = new MapLocation[6];

            // Rotational will always be a possible location
            possibleFlagLocations[0] = new MapLocation(W - spawn1.x - 1, H - spawn1.y - 1);
            possibleFlagLocations[1] = new MapLocation(W - spawn2.x - 1, H - spawn2.y - 1);
            possibleFlagLocations[2] = new MapLocation(W - spawn3.x - 1, H - spawn3.y - 1);

            if (isHorizontal) {
                possibleFlagLocations[3] = new MapLocation(W - spawn1.x - 1, spawn1.y);
                possibleFlagLocations[4] = new MapLocation(W - spawn2.x - 1, spawn2.y);
                possibleFlagLocations[5] = new MapLocation(W - spawn3.x - 1, spawn3.y);
            } else if (isVertical) {
                possibleFlagLocations[3] = new MapLocation(spawn1.x, H - spawn1.y - 1);
                possibleFlagLocations[4] = new MapLocation(spawn2.x, H - spawn2.y - 1);
                possibleFlagLocations[5] = new MapLocation(spawn3.x, H - spawn3.y - 1);
            }
        }

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
                        if (flag.getLocation().equals(loc)) {
                            eliminateHorizontal();
                            eliminateRotational();
                        }
                    }
                }
                updateSymmetry();
                if (sym) return;
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
                        if (flag.getLocation().equals(loc)) {
                            eliminateRotational();
                            eliminateVertical();
                        }
                    }
                }
                updateSymmetry();
                if (sym) return;
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
                        if (flag.getLocation().equals(loc)) {
                            eliminateVertical();
                            eliminateHorizontal();
                        }
                    }
                }
                updateSymmetry();
                if (sym) return;
            }
        }
    }

    void eliminateHorizontal() throws GameActionException {
        if (!isHorizontal) return;
        isHorizontal = false;
        int combined = ((isVertical ? 1 : 0) << 1 | (isRotational ? 1 : 0));
        rc.writeSharedArray(60, combined);
    }

    void eliminateVertical() throws GameActionException {
        if (!isVertical) return;
        isVertical = false;
        int combined = ((isHorizontal ? 1 : 0) << 2 | (isRotational ? 1 : 0));
        rc.writeSharedArray(60, combined);
    }

    void eliminateRotational() throws GameActionException {
        if (!isRotational) return;
        isRotational = false;
        int combined = (isHorizontal ? 1 : 0) << 2 | (isVertical ? 1 : 0) << 1;
        rc.writeSharedArray(60, combined);
    }
}
