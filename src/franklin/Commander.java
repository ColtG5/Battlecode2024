package franklin;

import battlecode.common.*;

public strictfp class Commander {
    RobotController rc;
    public Commander(RobotController rc) {
        this.rc = rc;
    }

    public void calculateNewFlagSpots() {
        MapLocation middleOFMap = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    }
}
