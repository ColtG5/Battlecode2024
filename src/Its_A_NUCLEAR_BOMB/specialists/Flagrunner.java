package Its_A_NUCLEAR_BOMB.specialists;

import Its_A_NUCLEAR_BOMB.*;
import battlecode.common.*;

import java.util.ArrayList;

public class Flagrunner {
    int localID;
    RobotController rc;
    Movement movement;
    BugNav bugNav;
    Utility utility;
    Symmetry symmetry;
    MapLocation locationForFlagrunnerGroup;
    boolean iAmStrategicallyWaiting = false;
    Direction dirAwayFromDam = null;
    boolean goBeserk = false;
    int turnsToGoBeserkFor = 10;
    ArrayList<MapLocation> stunTrapsLastRound = null;
    boolean isBuilder;
    boolean isBuilderSet = false;
    boolean leftySet = false;
    boolean broadcastReached = false;

    Utility.CoolRobotInfo[] coolRobotInfoArray;
    MapLocation[] spawnAreaCenters;
    MapLocation myRandomLocation;

    public Flagrunner(RobotController rc, Movement movement, Utility utility, BugNav bugNav, Symmetry symmetry) {
        this.rc = rc;
        this.movement = movement;
        this.utility = utility;
        this.bugNav = bugNav;
        this.symmetry = symmetry;
    }

    public void setLocalID(int localID) {
        this.localID = localID;
    }

    public void setSpawnAreaCenters(MapLocation[] spawnAreaCenters) {
        this.spawnAreaCenters = spawnAreaCenters;
    }

    public void setCoolRobotInfoArray(Utility.CoolRobotInfo[] coolRobotInfoArray) {
        this.coolRobotInfoArray = coolRobotInfoArray;
    }

    public void run() throws GameActionException {
        if (!isBuilderSet) {
            isBuilderSet = true;
            isBuilder = utility.amIABuilder();
        }
        if (!leftySet) {
            leftySet = true;
            movement.setLefty(utility.getMyFlagrunnerGroup() % 2 == 0);
        }

        boolean isLeader = utility.amIAGroupLeader();
        if (isLeader) {
            locationForFlagrunnerGroup = setLocationForGroup(); // decide where the group will go (including you)
        } else {
            locationForFlagrunnerGroup = utility.readLocationFromFlagrunnerGroupIndex();
//            rc.setIndicatorDot(locationForFlagrunnerGroup, 0, 255, 0);
        }

        if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS - 4) { // sit by dam, and trap around there if u can
            MapInfo[] damStuff = rc.senseNearbyMapInfos();
            for (MapInfo location : damStuff) {
                if (location.isDam() && rc.getLocation().isAdjacentTo(location.getMapLocation())) {
                    utility.placeTrapNearEnemy(rc.getLocation());
                    return;
                }
            }
        }
//        else if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) { // 4 rounds before divider drops, move away from dam, to try to kite into our stuns
//            bugNav.moveTo(utility.getClosetSpawnAreaCenter());
//        }


        // below here is an attempt at a dam strategy that (somehow) doesn't win more games sadly

//        /**
//         * If its the range of rounds to do the dam shenanigans, do that, otherwise do a normal flagrunner turn
//         */
//        int turnsToWait = 14;
//        if (!goBeserk && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS - 3 && rc.getRoundNum() <= GameConstants.SETUP_ROUNDS + turnsToWait) {
//
//            someDamStrategy(turnsToWait);
//
//        } else if (goBeserk) {
//
////            berserkMode();
//            attackMicroWithMoveAvailable();
//
//        } else {
//
//            if (rc.hasFlag()) {
//                utility.writeToFlagrunnerGroupIndex(rc.getLocation());
//                MapLocation closetSpawnAreaCenter = utility.getClosetSpawnAreaCenter();
//                bugNav.moveTo(closetSpawnAreaCenter);
//            }
//
//            // see if there are any flags on the ground around you, and go and try to grab them
//            if (rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) senseFlagsAroundMe();
//
//            attackMicroWithMoveAvailable();
//
//        }


        if (rc.hasFlag()) {
            utility.writeToFlagrunnerGroupIndex(rc.getLocation());
            MapLocation closetSpawnAreaCenter = utility.getClosetSpawnAreaCenter();
            bugNav.moveTo(closetSpawnAreaCenter);
        }

        // if there is an enemy carrying one of our flags, head straight to them
        goToEnemyFlagrunners();

        // see if there are any flags on the ground around you, and go and try to grab them
        if (rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) senseFlagsAroundMe();

        attackMicroWithMoveAvailable();


        stunTrapsLastRound = stunTrapsNearMe();


        // after every round whether spawned or not, convert your info to an int and write it to the shared array
        utility.writeMyInfoToSharedArray(false);
    }

    // ---------------------------------------------------------------------------------
    //                               builder funcs below
    // ---------------------------------------------------------------------------------

    public MapLocation closestEnemyToMe(RobotInfo[] nearbyEnemies) {
        RobotInfo closestEnemy = null;

        for (RobotInfo enemy : nearbyEnemies) {
            if (closestEnemy == null) closestEnemy = enemy;
            else if (rc.getLocation().distanceSquaredTo(enemy.getLocation()) < rc.getLocation().distanceSquaredTo(closestEnemy.getLocation()))
                closestEnemy = enemy;
        }
        if (closestEnemy != null) return closestEnemy.getLocation();
        return null;
    }

    public MapLocation locationClosestToEnemy(ArrayList<MapLocation> locations, MapLocation closestEnemy) {
        MapLocation closestLocation = null;

        for (MapLocation location : locations) {
            if (closestLocation == null) closestLocation = location;
            else if (location.distanceSquaredTo(closestEnemy) < closestLocation.distanceSquaredTo(closestEnemy))
                closestLocation = location;
        }

        return closestLocation;
    }

    public ArrayList<MapLocation> stunTrapsNearMe() throws GameActionException {
        MapInfo[] tilesAroundMe = rc.senseNearbyMapInfos(rc.getLocation(), -1);
        ArrayList<MapLocation> stunTraps = new ArrayList<>();
        for (MapInfo tile : tilesAroundMe) {
            if (tile.getTrapType() == TrapType.STUN) stunTraps.add(tile.getMapLocation());
        }
        return stunTraps;
    }

    public MapLocation getClosestStunTrapToMe() throws GameActionException {
        ArrayList<MapLocation> stunTraps = stunTrapsNearMe();
        if (stunTraps.isEmpty()) return null;
        MapLocation closestStunTrap = stunTraps.get(0);
        for (MapLocation stunTrap : stunTraps) {
            if (rc.getLocation().distanceSquaredTo(stunTrap) < rc.getLocation().distanceSquaredTo(closestStunTrap)) {
                closestStunTrap = stunTrap;
            }
        }
        return closestStunTrap;
    }

    public MapLocation getClosestDamLoc() {
        MapInfo[] damStuff = rc.senseNearbyMapInfos();
        MapLocation closestDam = null;
        for (MapInfo location : damStuff) {
            if (location.isDam()) {
                if (closestDam == null) closestDam = location.getMapLocation();
                else if (rc.getLocation().distanceSquaredTo(location.getMapLocation()) < rc.getLocation().distanceSquaredTo(closestDam)) {
                    closestDam = location.getMapLocation();
                }
            }
        }
        return closestDam;
    }

    // ---------------------------------------------------------------------------------
    //                              buddy checking funcs
    // ---------------------------------------------------------------------------------

    public boolean tooFewGroupMembersAround(int lessThanThis) throws GameActionException {
        RobotInfo[] robotInfos = rc.senseNearbyRobots(-1, rc.getTeam());
        return robotInfos.length < lessThanThis;
    }

    public boolean isDistanceToGroupLeaderMoreThan(int distance) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        MapLocation groupLeaderLocation = utility.readLocationFromFlagrunnerGroupIndex();
        return myLocation.distanceSquaredTo(groupLeaderLocation) > distance;
    }

    public MapLocation setLocationForGroup() throws GameActionException {
        MapLocation locForGroup = null;

        // see if we can sense any enemy flags
        FlagInfo[] enemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        // filter out flags that are picked up by us (this group only going to capture a flag rn. if a flag is picked up by us,
        // lets assume for now that he has his own group to be backup
        ArrayList<FlagInfo> enemyFlagsNotPickedUp = new ArrayList<>();
        for (FlagInfo enemyFlag : enemyFlags) {
            if (!enemyFlag.isPickedUp()) enemyFlagsNotPickedUp.add(enemyFlag);
        }

        MapLocation[] allDroppedFlags = rc.senseBroadcastFlagLocations();

        if (!enemyFlagsNotPickedUp.isEmpty()) { // if we can see a flag to conquer
            // get the closest flag to us
            MapLocation closestFlag = enemyFlags[0].getLocation();
            for (FlagInfo enemyFlag : enemyFlags) {
                if (rc.getLocation().distanceSquaredTo(enemyFlag.getLocation()) < rc.getLocation().distanceSquaredTo(closestFlag)) {
                    closestFlag = enemyFlag.getLocation();
                }
            }
            locForGroup = closestFlag;
        } else if (allDroppedFlags.length != 0) { // there is still a flag left for us to conquer
            // get the closest flag to us that is on the ground
            MapLocation closestFlag = allDroppedFlags[0];
            for (MapLocation droppedFlag : allDroppedFlags) {
                if (rc.getLocation().distanceSquaredTo(droppedFlag) < rc.getLocation().distanceSquaredTo(closestFlag)) {
                    closestFlag = droppedFlag;
                }
            }
            locForGroup = closestFlag;
        } else { // none on ground means at least one dude is hauling a flag back rn!!! go help him!!!
            // see if anyone on our team is carrying a flag
            for (Utility.CoolRobotInfo coolRobotInfo : coolRobotInfoArray) {
                if (coolRobotInfo.getHasFlag()) {
                    locForGroup = coolRobotInfo.getCurLocation();
                    break;
                }
            }
        }
        if (locForGroup == null)
            locForGroup = utility.getClosetSpawnAreaCenter(); // by my logic, this should never happen, but hey

        // write this locForGroup into the spot in the shared array for this group
//        rc.setIndicatorDot(locForGroup, 0, 0, 255);
        utility.writeToFlagrunnerGroupIndex(locForGroup);
        return locForGroup;
    }

    // ---------------------------------------------------------------------------------
    //                        attacking/healing micro helper funcs
    // ---------------------------------------------------------------------------------

    public MapLocation getAttackableEnemyWithLowestHealth(RobotInfo[] enemyRobots) {
        ArrayList<RobotInfo> attackableEnemies = new ArrayList<>();
        for (RobotInfo enemyRobot : enemyRobots) {
            if (rc.canAttack(enemyRobot.location)) attackableEnemies.add(enemyRobot);
        }
        if (attackableEnemies.isEmpty()) return null;

        RobotInfo lowestHealthEnemy = attackableEnemies.get(0);
        for (RobotInfo enemyRobot : attackableEnemies) {
            if (enemyRobot.health < lowestHealthEnemy.health) lowestHealthEnemy = enemyRobot;
        }
//        rc.setIndicatorString(lowestHealthEnemy.location.toString());
        return lowestHealthEnemy.location;
    }

    /**
     * Returns a pair of a boolean and a MapLocation. The boolean is true if the enemy can attack me next turn, and
     * false if the enemy cannot attack me next turn. The MapLocation is the location of the enemy that can attack me
     *
     * @param enemyRobots all enemy robots
     * @return bool of if we could be attacked next turn, and the location of that rapscallion
     */
    public Utility.MyPair<Boolean, MapLocation> canEnemyAttackMeNextTurn(RobotInfo[] enemyRobots) {
        boolean canEnemyAttackMeNextTurn = false;
        MapLocation locationOfEnemyThatCanAttackMe = null;
        for (RobotInfo enemyRobot : enemyRobots) {
            int distanceToEnemy = rc.getLocation().distanceSquaredTo(enemyRobot.location);
            if (5 <= distanceToEnemy && distanceToEnemy <= 10) { // I think this is the right range
                canEnemyAttackMeNextTurn = true;
                locationOfEnemyThatCanAttackMe = enemyRobot.location;
                break;
            }
        }
        return new Utility.MyPair<>(canEnemyAttackMeNextTurn, locationOfEnemyThatCanAttackMe);
    }

    public void tryToHeal() throws GameActionException {
        RobotInfo[] friendliesInRangeToHeal = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());
        if (friendliesInRangeToHeal.length == 0) return;
        RobotInfo lowestHealthFriendly = friendliesInRangeToHeal[0];
        for (RobotInfo friendly : friendliesInRangeToHeal) {
            if (friendly.hasFlag) {
                lowestHealthFriendly = friendly;
                break;
            }
            if (friendly.buildLevel > 20) {
                lowestHealthFriendly = friendly;
                break;
            }
            if (friendly.health < lowestHealthFriendly.health) lowestHealthFriendly = friendly;
        }

        if (rc.canHeal(lowestHealthFriendly.location)) rc.heal(lowestHealthFriendly.location);
//        if (rc.canHeal(rc.getLocation())) rc.heal(rc.getLocation());

    }

    public void moveAwayFromEnemyIJustAttacked(MapLocation enemyLocation) throws GameActionException {
        Direction directionAwayFromEnemy = rc.getLocation().directionTo(enemyLocation).opposite();
        movement.smallMove(directionAwayFromEnemy);
    }

    // ---------------------------------------------------------------------------------
    //                            helper functions
    // ---------------------------------------------------------------------------------
    MapLocation getRandomDirection() {
        Direction[] allDirs = new Direction[]{
                Direction.NORTH,
                Direction.NORTHEAST,
                Direction.EAST,
                Direction.SOUTHEAST,
                Direction.SOUTH,
                Direction.SOUTHWEST,
                Direction.WEST,
                Direction.NORTHWEST
        };

        MapLocation goScoutHere;
//        Direction randomDir = allDirs[(int) (Math.random() * allDirs.length)];
        do {
            goScoutHere = locationForFlagrunnerGroup;
            Direction randomDir = allDirs[(int) (Math.random() * allDirs.length)];
            for (int i = 0; i < 6; i++) {
                goScoutHere = goScoutHere.add(randomDir);
                if (!rc.onTheMap(goScoutHere)) break;
            }
        } while (!rc.onTheMap(goScoutHere));

        return goScoutHere;
    }

    private void senseFlagsAroundMe() throws GameActionException {
        // Get symmetry locs
        MapLocation[] symmetryLocs = symmetry.getPossibleFlagLocations();
        if (symmetry.isSymmetryValid()) symmetry.updateSymmetry();

        // Stop searching when one of the flags gets picked up
        for (MapLocation symmetryFlags : symmetryLocs) {
            if (locationForFlagrunnerGroup.isAdjacentTo(symmetryFlags)) {
                broadcastReached = false;
                break;
            }
        }

        FlagInfo[] flagInfo = rc.senseNearbyFlags(-1, rc.getTeam().opponent());

        if (flagInfo.length != 0) broadcastReached = false;
        if (broadcastReached) {
//            rc.setIndicatorString("My location: " + myRandomLocation);
            bugNav.moveTo(myRandomLocation);
            if (rc.getLocation().isAdjacentTo(myRandomLocation)) broadcastReached = false;
        } else if (flagInfo.length == 0 && rc.getLocation().isAdjacentTo(locationForFlagrunnerGroup)) {
            broadcastReached = true;
            myRandomLocation = getRandomDirection();
            bugNav.moveTo(myRandomLocation);
        }

        for (FlagInfo info : flagInfo) {
            if (!info.isPickedUp()) {
//                if (rc.canPickupFlag(info.getLocation()) && !isBuilder) {
                if (rc.canPickupFlag(info.getLocation())) {
                    rc.pickupFlag(info.getLocation());
                    utility.writeToFlagrunnerGroupIndex(rc.getLocation());
                    MapLocation closetSpawnAreaCenter = utility.getClosetSpawnAreaCenter();
//                    useBannedMovement(closetSpawnAreaCenter);
//                    movement.hardMove(closetSpawnAreaCenter);
//                    return closetSpawnAreaCenter;
                    bugNav.moveTo(closetSpawnAreaCenter);
                } else {
                    utility.writeToFlagrunnerGroupIndex(info.getLocation());
                    useBannedMovement(info.getLocation());
//                    movement.hardMove(info.getLocation());
//                    return info.getLocation();
//                    bugNav.moveTo(info.getLocation());
                }
                break;
            }
        }
//        return null;
    }

    private void goToEnemyFlagrunners() throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo enemyRobot : enemyRobots) {
            if (enemyRobot.hasFlag()) {
                bugNav.moveTo(enemyRobot.getLocation());
                rc.setIndicatorString("AW HELL NAH WHERE U THINK UR GOING");
            }
        }
    }

    private void useBannedMovement(MapLocation locationForFlagrunnerGroup) throws GameActionException {
        RobotInfo[] friendlies = rc.senseNearbyRobots(-1, rc.getTeam());
        ArrayList<MapLocation> bannedPlaces = new ArrayList<>();
        for (RobotInfo robot : friendlies) {
//            if (mapInfo.getMapLocation().isAdjacentTo(utility.getLocationOfMyGroupLeader())) {
//                bannedPlaces.add(mapInfo.getMapLocation());
//            }
            if (robot.hasFlag()) {
                bannedPlaces.add(robot.getLocation());

                // add every location around the flag carrier to the banned places
                for (Direction dir : Direction.allDirections()) {
                    MapLocation locationAroundFlagCarrier = robot.getLocation().add(dir);
                    bannedPlaces.add(locationAroundFlagCarrier);
                }
            }
        }
//        movement.hardMove(utility.getLocationOfMyGroupLeader(), bannedPlaces);
//        movement.hardMove(locationForFlagrunnerGroup, bannedPlaces);

        if (bannedPlaces.isEmpty()) bugNav.moveTo(locationForFlagrunnerGroup);
        else movement.hardMove(locationForFlagrunnerGroup, bannedPlaces);
    }

//    private void smartMovement(MapLocation location) throws GameActionException {
////        if (rc.canSenseLocation(location)) {
////            RobotInfo flagCarrier = rc.senseRobotAtLocation(location);
////            if (flagCarrier != null && flagCarrier.hasFlag() && flagCarrier.getTeam() == rc.getTeam()) {
////                useBannedMovement();
////                return;
////            }
////        }
////        if (coolRobotInfoArray[utility.readLocalIDOfGroupLeaderFromFlagrunnerGroupIndex() - 1].getHasFlag()) {
////            useBannedMovement();
////        }
//        RobotInfo[] friendlies = rc.senseNearbyRobots(-1, rc.getTeam());
//
//
//        movement.hardMove(location);
////        bugNav.moveTo(location);
//    }

    // ---------------------------------------------------------------------------------
    //                            attacking/healing micro
    // ---------------------------------------------------------------------------------

    public void someDamStrategy(int turnsToWait) throws GameActionException {
        if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS - 3 || rc.getRoundNum() == GameConstants.SETUP_ROUNDS - 2) {
            MapInfo[] damStuff = rc.senseNearbyMapInfos();
            boolean byDam = false;
            for (MapInfo location : damStuff) {
                if (location.isDam() && rc.getLocation().isAdjacentTo(location.getMapLocation())) {
                    byDam = true;
                }
            }
            if (byDam && rc.canBuild(TrapType.STUN, rc.getLocation()))
                utility.placeTrapNearEnemySingleLoc(rc.getLocation());

//            RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//            if (enemies.length > 0) {
//                iAmStrategicallyWaiting = true; // if we can sense an enemy over the dam
//                rc.setIndicatorString("I will be waiting.");
//                MapLocation closestEnemy = closestEnemyToMe(enemies);
////                if (rc.canMove(rc.getLocation().directionTo(closestEnemy).opposite())) {
////                    movement.smallMove(rc.getLocation().directionTo(closestEnemy).opposite());
////                }
//            }
            iAmStrategicallyWaiting = true;
            rc.setIndicatorString("I will be waiting.");
        }
        if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS - 1) { // see all the traps that have been laid down
            stunTrapsLastRound = stunTrapsNearMe();
            // get the absolute closest dam location around me
            MapLocation closestDamLoc = getClosestDamLoc();
            if (closestDamLoc != null) dirAwayFromDam = rc.getLocation().directionTo(closestDamLoc).opposite();
            else dirAwayFromDam = rc.getLocation().directionTo(utility.getClosetSpawnAreaCenter());
        }
        ArrayList<MapLocation> stunTrapsNearMe = stunTrapsNearMe();
        if (iAmStrategicallyWaiting) {
            if (!stunTrapsNearMe.isEmpty()) { // if we are waiting for the enemy behind our stun traps
                if (rc.getRoundNum() >= GameConstants.SETUP_ROUNDS && rc.getRoundNum() <= GameConstants.SETUP_ROUNDS + turnsToWait) { // wait for the enemy for 8(?) turns
                    RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//                if (enemies.length == 0) {
//                    rc.setIndicatorString("No enemies neaby man!!");
//                    iAmStrategicallyWaiting = false; // where'd they go??? nobody to wait for anymore so give up waiting
//                } else { // there are still enemies potentially tripping our traps!
                    if (!stunTrapsLastRound.isEmpty()) {
                        // check if any enemy position coincides with being adjacent to a stun trap that was placed last round. if so, we know they triggered it on their last move!!!
                        outer:
                        for (RobotInfo enemy : enemies) {
                            for (MapLocation stunTrap : stunTrapsLastRound) {
                                if (enemy.getLocation().isAdjacentTo(stunTrap)) { // an enemy triggered a stun trap! GO CRAZY!!!
                                    iAmStrategicallyWaiting = false;
                                    goBeserk = true;
                                    rc.setIndicatorString("THEY TRIPPED A STUN RAHHHHHHH");
                                    break outer; // break from the nested loop
                                }
                            }
                        }
                    }
                    // make sure you have a stun trap in between you and the closest enemy
                    MapLocation closestEnemy = closestEnemyToMe(enemies);
                    // get the maplocation of the spot behind the closest stuntrap to us, where behind means that being in thgat spot places the stun trap in between us and the enemy
                    MapLocation closestStunTrap = getClosestStunTrapToMe();

                    // go sit behind the closest stun trap so you are safe from the closest enemy
//                    MapLocation spotBehindClosestStunTrap = closestStunTrap.add(closestEnemy.directionTo(closestStunTrap)); // hopefully this works man
//                    rc.setIndicatorString("This is my safe place: " + spotBehindClosestStunTrap.toString());
//                    if (!rc.getLocation().isAdjacentTo(spotBehindClosestStunTrap)) movement.smallMove(rc.getLocation().directionTo(spotBehindClosestStunTrap));

//                    MapLocation spotToWait = getClosestDamLoc().add(dirAwayFromDam);
//                    rc.setIndicatorString("This is my safe place: " + spotToWait.toString());
//                    if (!rc.getLocation().equals(spotToWait)) movement.smallMove(rc.getLocation().directionTo(spotToWait));

                    MapLocation spotToWait = closestStunTrap.add(dirAwayFromDam).add(dirAwayFromDam);
//                rc.setIndicatorString("This is my safe place: " + spotToWait.toString());
                    if (!rc.getLocation().equals(spotToWait))
                        movement.smallMove(rc.getLocation().directionTo(spotToWait));

//                }
                }
            } else {
                attackMicroWithMoveAvailable();
            }
            if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS + turnsToWait + 1) { // if we waited for the enemy for 8 turns, and they didn't come, go back to normal
                iAmStrategicallyWaiting = false;
            }
        }
    }

    public void berserkMode() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        MapLocation closestAttackableEnemy = getAttackableEnemyWithLowestHealth(enemies);
        if (closestAttackableEnemy != null) {
            if (rc.canAttack(closestAttackableEnemy)) rc.attack(closestAttackableEnemy);
            movement.smallMove(rc.getLocation().directionTo(closestAttackableEnemy).opposite());
        }
        if (rc.isActionReady()) {
            MapLocation closestEnemy = closestEnemyToMe(enemies);
            if (closestEnemy != null) {
                if (rc.canMove(rc.getLocation().directionTo(closestEnemy))) {
                    movement.smallMove(rc.getLocation().directionTo(closestEnemy));
                    closestAttackableEnemy = getAttackableEnemyWithLowestHealth(enemies);
                    if (closestAttackableEnemy != null && rc.canAttack(closestAttackableEnemy)) rc.attack(closestEnemy);
                }
            } else {
                tryToHeal();
            }
        }

        if (turnsToGoBeserkFor == 0) {
            goBeserk = false;
            turnsToGoBeserkFor = 10;
        } else {
            turnsToGoBeserkFor--;
        }
    }

    /**
     * Use this attacking/healing micro when you do NOT want to move in the micro! (you want to beeline to a location and
     * not waste your movement in the micro (getting to the place is super urgent!!!!!)
     *
     * @throws GameActionException could not attack or heal smthn we tried to I think
     */
    public void attackMicroWithNoMoveAvailable() throws GameActionException {
        // find the closest enemy to me. if I can attack it, attack it. if it is one movement away from being
        // able to attack me, do nothing (save attack for next turn when they are maybe closer). else (the enemy is
        // too far to care about), heal yourself, or others
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        if (enemyRobots.length != 0) {
            MapLocation attackableEnemyLocation = getAttackableEnemyWithLowestHealth(enemyRobots);
            Utility.MyPair<Boolean, MapLocation> canIBeAttackedNextTurn = canEnemyAttackMeNextTurn(enemyRobots);
            if (attackableEnemyLocation != null) { // if there's an opp I can attack, best believe we running his fade
                if (rc.canAttack(attackableEnemyLocation)) rc.attack(attackableEnemyLocation);
            } else if (canIBeAttackedNextTurn.first()) { // if we can be attacked next turn, not much we can really do
                // without a movement, so just save attack cooldown for next turn to attack them maybe
                // do nothing
            } else { // if we can't be attacked next turn, heal up the brothas
                tryToHeal();
            }
        } else { // no worries in the world, just heal up the gang
            tryToHeal();
        }
    }

    /**
     * Use this attacking/healing micro when you DO want to move in the micro! (for most cases. Essentially make your
     * journey longer in exchange for being able to move during your attack/healing micro)
     *
     * @throws GameActionException if we cant move, attack, or heal I think???
     */
    public void attackMicroWithMoveAvailable() throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        if (enemyRobots.length != 0) {
            MapLocation attackableEnemyLocation = getAttackableEnemyWithLowestHealth(enemyRobots);
            Utility.MyPair<Boolean, MapLocation> canIBeAttackedNextTurn = canEnemyAttackMeNextTurn(enemyRobots);
            if (attackableEnemyLocation != null) { // somebody can attack us! hit them and run away
                if (rc.canAttack(attackableEnemyLocation)) rc.attack(attackableEnemyLocation);
//                if (movement.MovementStack.empty()) moveAwayFromEnemyIJustAttacked(attackableEnemyLocation);
                moveAwayFromEnemyIJustAttacked(attackableEnemyLocation);
            } else if (canIBeAttackedNextTurn.first()) { // someone can potentially attack us next turn if they move to us. go smack them
                if (rc.getHealth() > 150) {
                    if (rc.isActionReady()) { // we can fight back, so go smack them
                        movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()));
//                        if (movement.MovementStack.empty()) movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()));
                        attackableEnemyLocation = getAttackableEnemyWithLowestHealth(enemyRobots);
                        if (attackableEnemyLocation != null && rc.canAttack(attackableEnemyLocation))
                            rc.attack(attackableEnemyLocation);
                    } else { // we cannot fight back, try to run
                        movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()).opposite());
//                        if (movement.MovementStack.empty()) movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()).opposite());

//                        placeModestBombsSpaced(enemyRobots);

                    }
                } else {
//                    movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()).opposite());
//                    if (movement.MovementStack.empty()) movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()).opposite());

//                    movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()).opposite());
//                    placeModestBombsSpaced(enemyRobots);
//                    utility.placeTrapNearEnemies(rc.senseNearbyRobots(10, rc.getTeam().opponent()));

                }
//                if (!movement.MovementStack.empty()) movement.hardMove(locationForFlagrunnerGroup);


//                if (coolRobotInfoArray[utility.readLocalIDOfGroupLeaderFromFlagrunnerGroupIndex() - 1].getHasFlag()) {
//                    useBannedMovement();
//                } else {
//                    movement.hardMove(locationForFlagrunnerGroup);
//                }

//                smartMovement(locationForFlagrunnerGroup);
                useBannedMovement(locationForFlagrunnerGroup);

                utility.placeTrapNearEnemies(rc.senseNearbyRobots(10, rc.getTeam().opponent()));

//                bugNav.moveTo(locationForFlagrunnerGroup);
//                tryToHeal();

            } else { // there's an enemy, but they cant attack us next turn. save our action cooldown, and just move to our goal
                // two strats here: either stay put and try to heal,
                tryToHeal();
                // or move to the goal and don't heal
//                movement.hardMove(locationForFlagrunnerGroup);
//                smartMovement(locationForFlagrunnerGroup);
                useBannedMovement(locationForFlagrunnerGroup);
                tryToHeal();
            }
        } else { // zero enemies on our radar, so walk to spot we gotta go, and heal up
//            movement.hardMove(locationForFlagrunnerGroup);
            tryToHeal();
//            smartMovement(locationForFlagrunnerGroup);
            useBannedMovement(locationForFlagrunnerGroup);
            tryToHeal();
        }
    }
}