package RedRising.before_specialists;


import RedRising.Movement;
import RedRising.Utility;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;


public class Scout {
    RobotController rc;
    Movement movement;
    Utility utility;
    public Scout(RobotController rc, Movement movement, Utility utility) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
    }

    public void run() throws GameActionException {
        scoutRandomDirection();
    }

    static MapLocation locationGoal = null;
    boolean crumbLocated = false;
    MapLocation getRandomDirection() throws GameActionException {
        MapInfo[] mapInfos =rc.senseNearbyMapInfos();
        MapInfo mapPlace =mapInfos[(int)(Math.random()*mapInfos.length)];
        while(!mapPlace.isPassable()) {
            mapPlace =mapInfos[(int)(Math.random()*mapInfos.length)];
        }
        if(mapPlace.isPassable()) {
            return mapPlace.getMapLocation()
            ;}
        return rc.getLocation();
    }
    private void scoutRandomDirection() throws GameActionException {
        // Choose a random direction, and move that way if possible
//        String ind ="I am a scout and I at location: "+rc.getLocation()+" and my goal is: "+locationGoal;
        if(rc.getLocation().equals( locationGoal)) {
            if(crumbLocated){
                crumbLocated = false;
            }
            locationGoal = null;
        }
//        rc.setIndicatorString(ind);
        MapInfo[] mapInfos =rc.senseNearbyMapInfos();
        if(locationGoal!=null) {
            if(rc.senseMapInfo(rc.getLocation().add(rc.getLocation().directionTo(locationGoal))).isWater()){
                if(rc.canFill(rc.getLocation().add(rc.getLocation().directionTo(locationGoal)))) {
                    rc.fill(rc.getLocation().add(rc.getLocation().directionTo(locationGoal)));
                    if(rc.canMove(rc.getLocation().directionTo(locationGoal))){
                    rc.move(rc.getLocation().directionTo(locationGoal));
                    return;
                    }
                }
            }

            if(!crumbLocated) {
                MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
                if (crumbs.length > 0) {
                    crumbLocated = true;
                    locationGoal = crumbs[0];
                    movement.hardMove(locationGoal);
                    return;
                }
            }
            movement.hardMove(locationGoal);
            if(rc.getLocation().equals(locationGoal)) {
                rc.setIndicatorString("HEELLLLP");
                if(crumbLocated){
                    crumbLocated = false;
                }
                locationGoal = getRandomDirection();

            }
        }else{
            MapLocation[] crumbs= rc.senseNearbyCrumbs(-1);
            if(crumbs.length>0) {
                crumbLocated = true;
                locationGoal = crumbs[0];
                movement.hardMove(locationGoal);
                return;
            }
            else if (mapInfos.length > 0) {
                locationGoal = getRandomDirection();

            }

        }


    }



}
