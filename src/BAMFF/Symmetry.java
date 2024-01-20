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
    int[] possibleFlagLocations;
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
    public int[] getPossibleFlagLocations() throws GameActionException {
        if (getSymmetry()) {
            possibleFlagLocations = new int[3];
            for (int i = 51; i <= 53; i++) {
                int combined = rc.readSharedArray(i);
                possibleFlagLocations[i - 51] = combined;
//                MapLocation possibleFlag = utility.intToLocation(combined >> 1);
//                boolean isStolen = (combined & 1) == 1;
//                System.out.println("FLAG LOC: " + possibleFlag);
//                System.out.println("IS IT STOLEN: " + isStolen);
            }
        }
//        System.out.println("[");
//        for (int combined : possibleFlagLocations) {
//            MapLocation possibleFlag = utility.intToLocation(combined >> 1);
//            System.out.println(possibleFlag + ", ");
//        }
//        System.out.println("]");
        return possibleFlagLocations;
    }

    public void updatePossibleFlagLocations(boolean iStoleFlag) throws GameActionException {
        // Flag has been fully captured, so we set the boolean bit to true
        if (iStoleFlag) {
            for (int i = 51; i <= 53; i++) {
                if (flagStolen.equals(utility.intToLocation(rc.readSharedArray(i) >> 1))) {
//                    System.out.println("I FULLY CAPTURED: " + utility.intToLocation(rc.readSharedArray(i) >> 1));
                    possibleFlagLocations[flagStolenIndex] |= 1;
                    rc.writeSharedArray(i, possibleFlagLocations[flagStolenIndex]);
                    rc.writeSharedArray(61 + flagStolenIndex, 0);
                }
            }
        }

        if (flagStolen == null) {
            for (int i = 0; i < possibleFlagLocations.length; i++) {
                MapLocation flag = utility.intToLocation(possibleFlagLocations[i] >> 1);

                int combined = rc.readSharedArray(61 + i);
//                System.out.println("COMBINED VALUE: " + combined);
                boolean isStolen = (possibleFlagLocations[i] & 1) == 1;
//                System.out.println("IS THIS FLAG STOLEN: " + isStolen);
                if (combined != 0 && !isStolen) {
                    int flagDroppedLocation = combined >> 2;
                    MapLocation prevFlagLoc = utility.intToLocation(flagDroppedLocation);

                    if (rc.canSenseLocation(prevFlagLoc)) {
                        flagStolenIndex = combined & 0b11;
                        flagStolen = utility.intToLocation(possibleFlagLocations[flagStolenIndex] >> 1);
//                        System.out.println("I AM PICKING UP THE DROPPED FLAG AT: " + prevFlagLoc);
//                        System.out.println("IT HAS THE INDEX OF: " + flagStolenIndex);
//                        System.out.println("FLAG IS: " + flagStolen);
                        break;
                    }
                }

                if (rc.canSenseLocation(flag) && rc.hasFlag() && !isStolen) {
                    flagStolenIndex = i;
                    flagStolen = flag;
                    break;
                }
            }
        }

        if (rc.hasFlag()) {
            // If a duck is carrying the flag set the boolean bit to true
            // so ducks don't get stuck going for already stolen flags
//            System.out.println("I AM CARRYING: " + (utility.intToLocation(possibleFlagLocations[flagStolenIndex] >> 1)));
//            System.out.println("HAS INDEX: " + flagStolenIndex);
            possibleFlagLocations[flagStolenIndex] |= 1;
            if (rc.getHealth() < 750) {
                possibleFlagLocations[flagStolenIndex] &= ~1;
//                System.out.println("I AM PROBABLY GOING TO DIE SO STEALING: " + (utility.intToLocation(possibleFlagLocations[flagStolenIndex] >> 1)));
            }
            rc.writeSharedArray(51 + flagStolenIndex, possibleFlagLocations[flagStolenIndex]);


            // Info for if the duck dies and someone else picks up the flag
            // before it gets returned to enemy spawn
            int locationToInt = utility.locationToInt(rc.getLocation());
            int combined = (locationToInt << 2) | flagStolenIndex;
            rc.writeSharedArray(61 + flagStolenIndex, combined);
        }
    }

    void updateSymmetry() throws GameActionException {
        if (isHorizontal && !isVertical && !isRotational) {
            sym = true;
            possibleFlagLocations = new int[3];
            int combined = (utility.locationToInt(new MapLocation(W - spawn1.x - 1, spawn1.y)) << 1);
            int combined1 = (utility.locationToInt(new MapLocation(W - spawn2.x - 1, spawn2.y)) << 1);
            int combined2 = (utility.locationToInt(new MapLocation(W - spawn3.x - 1, spawn3.y)) << 1);
            possibleFlagLocations[0] = combined;
            possibleFlagLocations[1] = combined1;
            possibleFlagLocations[2] = combined2;
            rc.writeSharedArray(51, combined);
            rc.writeSharedArray(52, combined1);
            rc.writeSharedArray(53, combined2);
            return;
        }
        if (!isHorizontal && isVertical && !isRotational) {
            sym = true;
            possibleFlagLocations = new int[3];
            int combined = (utility.locationToInt(new MapLocation(spawn1.x, H - spawn1.y - 1)) << 1);
            int combined1 = (utility.locationToInt(new MapLocation(spawn2.x, H - spawn2.y - 1)) << 1);
            int combined2 = (utility.locationToInt(new MapLocation(spawn3.x, H - spawn3.y - 1)) << 1);
            possibleFlagLocations[0] = combined;
            possibleFlagLocations[1] = combined1;
            possibleFlagLocations[2] = combined2;
            rc.writeSharedArray(51, combined);
            rc.writeSharedArray(52, combined1);
            rc.writeSharedArray(53, combined2);
            return;
        }
        if (!isHorizontal && !isVertical && isRotational) {
            sym = true;
            possibleFlagLocations = new int[3];
            int combined = (utility.locationToInt(new MapLocation(W - spawn1.x - 1, H - spawn1.y - 1)) << 1);
            int combined1 = (utility.locationToInt(new MapLocation(W - spawn2.x - 1, H - spawn2.y - 1)) << 1);
            int combined2 = (utility.locationToInt(new MapLocation(W - spawn3.x - 1, H - spawn3.y - 1)) << 1);
            possibleFlagLocations[0] = combined;
            possibleFlagLocations[1] = combined1;
            possibleFlagLocations[2] = combined2;
            rc.writeSharedArray(51, combined);
            rc.writeSharedArray(52, combined1);
            rc.writeSharedArray(53, combined2);
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
            possibleFlagLocations = new int[6];

            // Rotational will always be a possible location
            possibleFlagLocations[0] = utility.locationToInt(new MapLocation(W - spawn1.x - 1, H - spawn1.y - 1)) << 1;
            possibleFlagLocations[1] = utility.locationToInt(new MapLocation(W - spawn2.x - 1, H - spawn2.y - 1)) << 1;
            possibleFlagLocations[2] = utility.locationToInt(new MapLocation(W - spawn3.x - 1, H - spawn3.y - 1)) << 1;

            if (isHorizontal) {
                possibleFlagLocations[3] = utility.locationToInt(new MapLocation(W - spawn1.x - 1, spawn1.y)) << 1;
                possibleFlagLocations[4] = utility.locationToInt(new MapLocation(W - spawn2.x - 1, spawn2.y)) << 1;
                possibleFlagLocations[5] = utility.locationToInt(new MapLocation(W - spawn3.x - 1, spawn3.y)) << 1;
            } else if (isVertical) {
                possibleFlagLocations[3] = utility.locationToInt(new MapLocation(spawn1.x, H - spawn1.y - 1)) << 1;
                possibleFlagLocations[4] = utility.locationToInt(new MapLocation(spawn2.x, H - spawn2.y - 1)) << 1;
                possibleFlagLocations[5] = utility.locationToInt(new MapLocation(spawn3.x, H - spawn3.y - 1)) << 1;
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
