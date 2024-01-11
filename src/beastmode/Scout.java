package beastmode;


import battlecode.common.*;


public class Scout {
    RobotController rc;
    Movement movement;
    public Scout(RobotController rc, boolean lefty) {
        this.rc = rc;
        this.movement = new Movement(rc, lefty);
    }
    static MapLocation locationGoal = null;
    boolean crumbLocated = false;
    public void scoutRandomDirection() throws GameActionException {
        // Choose a random direction, and move that way if possible
        String ind ="I am a scout and I at location: "+rc.getLocation()+" and my goal is: "+locationGoal;
        rc.setIndicatorString(ind);
        MapInfo[] mapInfos =rc.senseNearbyMapInfos();
        if(locationGoal!=null) {
            if(rc.senseMapInfo(rc.getLocation().add(rc.getLocation().directionTo(locationGoal))).isWater()){
                if(rc.canFill(rc.getLocation().add(rc.getLocation().directionTo(locationGoal)))) {
                    rc.fill(rc.getLocation().add(rc.getLocation().directionTo(locationGoal)));
                    rc.move(rc.getLocation().directionTo(locationGoal));
                    return;
                }
            }
            if(rc.getLocation() == locationGoal) {
                locationGoal = null;

            }

            MapLocation[] crumbs= rc.senseNearbyCrumbs(-1);
            if(crumbs.length>0) {
                locationGoal = crumbs[0];
                movement.hardMove(locationGoal);
                return;
            }
            movement.hardMove(locationGoal);
        }else{
            MapLocation[] crumbs= rc.senseNearbyCrumbs(-1);
            if(crumbs.length>0) {
                locationGoal = crumbs[0];
                movement.hardMove(locationGoal);
                return;
            }
            if (mapInfos.length > 0) {
                MapInfo mapPlace =mapInfos[(int)(Math.random()*mapInfos.length)];
                while(!mapPlace.isPassable()) {
                    mapPlace =mapInfos[(int)(Math.random()*mapInfos.length)];
                }
                if(mapPlace.isPassable()) {
                    locationGoal = mapPlace.getMapLocation();
                    movement.hardMove(locationGoal);
                    return;
                }

            }

        }


    }



}
