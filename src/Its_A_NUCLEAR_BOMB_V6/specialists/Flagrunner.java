package Its_A_NUCLEAR_BOMB_V6.specialists;

import Its_A_NUCLEAR_BOMB_V6.*;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static Its_A_NUCLEAR_BOMB_V6.RobotPlayer.NONELOCATION;

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
    ArrayList<MapLocation> oppFlags = null;
    int oppFlagsIndex;
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
//        if (!isBuilderSet) {
//            isBuilderSet = true;
//            isBuilder = utility.amIABuilder();
//        }
        if (!leftySet) {
            leftySet = true;
            movement.setLefty(utility.getMyFlagrunnerGroup() % 2 == 0);
        }

        if (oppFlags == null || oppFlags.isEmpty() || oppFlags.size() > 3) senseFlagsAroundMe();

        boolean isLeader = utility.amIAGroupLeader();
        if (isLeader) {
            locationForFlagrunnerGroup = setLocationForGroup(); // decide where the group will go (including you)
        } else {
            locationForFlagrunnerGroup = utility.readLocationFromFlagrunnerGroupIndex();
        }

        if (rc.getID() == 13974) System.out.println(locationForFlagrunnerGroup);

//        if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS - 10 && rc.getRoundNum() > GameConstants.SETUP_ROUNDS - 40) { // sit by dam, and trap around there if u can
////            MapLocation mid = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
////            useBannedMovement(mid);
////            return;
//            MapInfo[] damStuff = rc.senseNearbyMapInfos();
//            for (MapInfo location : damStuff) {
//                if (location.isDam() && rc.getLocation().isAdjacentTo(location.getMapLocation())) {
//                    return;
//                }
//            }
//        }
//
//        if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS - 4) { // sit by dam, and trap around there if u can
//            MapInfo[] damStuff = rc.senseNearbyMapInfos();
//            for (MapInfo location : damStuff) {
//                if (location.isDam() && rc.getLocation().isAdjacentTo(location.getMapLocation())) {
//                    utility.placeTrapNearEnemy(rc.getLocation());
//                    return;
//                }
//            }
//        }

//        if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS - 4) { // sit by dam, and trap around there if u can
//            MapInfo[] damStuff = rc.senseNearbyMapInfos();
//            for (MapInfo location : damStuff) {
//                RobotInfo[] someEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//                boolean anyEnemyCloseEnoughToWarrantPlacingATrap = someEnemies.length > 0;
//                if (location.isDam() && rc.getLocation().isAdjacentTo(location.getMapLocation()) && anyEnemyCloseEnoughToWarrantPlacingATrap) {
//                    MapLocation closestEnemy = closestEnemyToMe(someEnemies);
//                    System.out.println("tryna place trap");
//                    utility.placeTrapNearEnemy(closestEnemy);
//                    break;
//                }
//            }
//        }
//
//        /*
//         * If its the range of rounds to do the dam shenanigans, do that, otherwise do a normal flagrunner turn
//         */
//        int turnsToWait = 14;
//        if (!goBeserk && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS - 3 && rc.getRoundNum() <= GameConstants.SETUP_ROUNDS + turnsToWait) {
//
//            someDamStrategy(turnsToWait);
//
//        }





        int numOfEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length;
        boolean atLeastOneEnemy = numOfEnemies > 0;

        tryPickUpFlag();

        // determine if still need help bringing the flag home
        if (rc.hasFlag()) {
            MapLocation closetSpawnAreaCenter = utility.getClosetSpawnAreaCenter();
            if (rc.canSenseLocation(closetSpawnAreaCenter) && !atLeastOneEnemy) { // close enough to home, don't need defending anymore
                utility.writeToFlagrunnerGroupIndex(NONELOCATION);
            } else {
                utility.writeToFlagrunnerGroupIndex(rc.getLocation());
            }
            bugNav.moveTo(closetSpawnAreaCenter);
            return;
        }


        attack();
//        attackLowestHealth();
        if (!doMicro()) {
//            rc.setIndicatorString("No need to play it safe!! attackable enemies: " + rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent()).length);
            moveToTarget();
            utility.placeTrapNearEnemies(rc.senseNearbyRobots(10, rc.getTeam().opponent()));
        } else {
            utility.placeTrapNearEnemies(rc.senseNearbyRobots(10, rc.getTeam().opponent()));
        }
//        utility.placeTrapNearEnemies(rc.senseNearbyRobots(10, rc.getTeam().opponent()));
        attack();
//        attackLowestHealth();
        tryToHeal();


        stunTrapsLastRound = stunTrapsNearMe();
        // after every round whether spawned or not, convert your info to an int and write it to the shared array
        utility.writeMyInfoToSharedArray(false);
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

//        MapLocation target = getPriorityEnemy(rc.senseNearbyRobots(-1, rc.getTeam().opponent()));
//        if (target != null) return target;

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
        if (rc.getRoundNum() == 140) System.out.println("locForGroup: " + locForGroup);
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
        if (!rc.isActionReady()) return;
        RobotInfo[] friendliesInRangeToHeal = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());
        if (friendliesInRangeToHeal.length == 0) return;
        RobotInfo lowestHealthFriendly = friendliesInRangeToHeal[0];
        for (RobotInfo friendly : friendliesInRangeToHeal) {
            if (friendly.hasFlag) {
                lowestHealthFriendly = friendly;
                break;
            }
            if (friendly.health < lowestHealthFriendly.health) lowestHealthFriendly = friendly;
        }
        if (lowestHealthFriendly.health > (GameConstants.DEFAULT_HEALTH - (rc.getHealAmount()) / 2)) return; // dont bother healing if ur heal does less than half their missing health
        if (rc.canHeal(lowestHealthFriendly.location)) rc.heal(lowestHealthFriendly.location);
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

    void tryPickUpFlag() throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo info : flags) {
            if (!info.isPickedUp() && !utility.readAmIToDefend()) {
                if (rc.canPickupFlag(info.getLocation())) {
                    rc.pickupFlag(info.getLocation());
                    utility.writeToFlagrunnerGroupIndex(rc.getLocation());
                    MapLocation closetSpawnAreaCenter = utility.getClosetSpawnAreaCenter();
                    useBannedMovement(closetSpawnAreaCenter);
                } else {
                    utility.writeToFlagrunnerGroupIndex(info.getLocation());
//                    useBannedMovement(info.getLocation());
                }
                break;
            }
        }
    }

    private void senseFlagsAroundMe() throws GameActionException {
        if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS - 60) return;
        // Get symmetry locs
        MapLocation[] symmetryLocs = symmetry.getPossibleFlagLocations();
        if ((symmetry.isSymmetryValid() && symmetryLocs.length != 3)) {
            symmetry.updateSymmetry();
            symmetryLocs = symmetry.getPossibleFlagLocations();
        }

        if (oppFlags == null || oppFlags.size() > 3) {
            oppFlags = new ArrayList<>(Arrays.asList(symmetryLocs));
            oppFlags.sort(Comparator.comparingInt(flag -> rc.getLocation().distanceSquaredTo(flag)));
            oppFlagsIndex = 0;

//            rc.writeSharedArray(51, utility.locationToInt(oppFlags.get(0)));
//            rc.writeSharedArray(52, utility.locationToInt(oppFlags.get(1)));
//            rc.writeSharedArray(53, utility.locationToInt(oppFlags.get(2)));
        }
        tryPickUpFlag();
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

//    public void someDamStrategy(int turnsToWait) throws GameActionException {
//        if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS - 3 || rc.getRoundNum() == GameConstants.SETUP_ROUNDS - 2) {
//            MapInfo[] damStuff = rc.senseNearbyMapInfos();
//            boolean byDam = false;
//            for (MapInfo location : damStuff) {
//                if (location.isDam() && rc.getLocation().isAdjacentTo(location.getMapLocation())) {
//                    byDam = true;
//                }
//            }
//            if (byDam && rc.canBuild(TrapType.STUN, rc.getLocation()))
//                utility.placeTrapNearEnemySingleLoc(rc.getLocation());
//
////            RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
////            if (enemies.length > 0) {
////                iAmStrategicallyWaiting = true; // if we can sense an enemy over the dam
////                rc.setIndicatorString("I will be waiting.");
////                MapLocation closestEnemy = closestEnemyToMe(enemies);
//////                if (rc.canMove(rc.getLocation().directionTo(closestEnemy).opposite())) {
//////                    movement.smallMove(rc.getLocation().directionTo(closestEnemy).opposite());
//////                }
////            }
//            iAmStrategicallyWaiting = true;
//            rc.setIndicatorString("I will be waiting.");
//        }
//        if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS - 1) { // see all the traps that have been laid down
//            stunTrapsLastRound = stunTrapsNearMe();
//            // get the absolute closest dam location around me
//            MapLocation closestDamLoc = getClosestDamLoc();
//            if (closestDamLoc != null) dirAwayFromDam = rc.getLocation().directionTo(closestDamLoc).opposite();
//            else dirAwayFromDam = rc.getLocation().directionTo(utility.getClosetSpawnAreaCenter());
//        }
//        ArrayList<MapLocation> stunTrapsNearMe = stunTrapsNearMe();
//        if (iAmStrategicallyWaiting) {
//            if (!stunTrapsNearMe.isEmpty()) { // if we are waiting for the enemy behind our stun traps
//                if (rc.getRoundNum() >= GameConstants.SETUP_ROUNDS && rc.getRoundNum() <= GameConstants.SETUP_ROUNDS + turnsToWait) { // wait for the enemy for 8(?) turns
//                    RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
////                if (enemies.length == 0) {
////                    rc.setIndicatorString("No enemies neaby man!!");
////                    iAmStrategicallyWaiting = false; // where'd they go??? nobody to wait for anymore so give up waiting
////                } else { // there are still enemies potentially tripping our traps!
//                    if (!stunTrapsLastRound.isEmpty()) {
//                        // check if any enemy position coincides with being adjacent to a stun trap that was placed last round. if so, we know they triggered it on their last move!!!
//                        outer:
//                        for (RobotInfo enemy : enemies) {
//                            for (MapLocation stunTrap : stunTrapsLastRound) {
//                                if (enemy.getLocation().isAdjacentTo(stunTrap)) { // an enemy triggered a stun trap! GO CRAZY!!!
//                                    iAmStrategicallyWaiting = false;
//                                    goBeserk = true;
//                                    rc.setIndicatorString("THEY TRIPPED A STUN RAHHHHHHH");
//                                    break outer; // break from the nested loop
//                                }
//                            }
//                        }
//                    }
//                    // make sure you have a stun trap in between you and the closest enemy
//                    MapLocation closestEnemy = closestEnemyToMe(enemies);
//                    // get the maplocation of the spot behind the closest stuntrap to us, where behind means that being in thgat spot places the stun trap in between us and the enemy
//                    MapLocation closestStunTrap = getClosestStunTrapToMe();
//
//                    // go sit behind the closest stun trap so you are safe from the closest enemy
////                    MapLocation spotBehindClosestStunTrap = closestStunTrap.add(closestEnemy.directionTo(closestStunTrap)); // hopefully this works man
////                    rc.setIndicatorString("This is my safe place: " + spotBehindClosestStunTrap.toString());
////                    if (!rc.getLocation().isAdjacentTo(spotBehindClosestStunTrap)) movement.smallMove(rc.getLocation().directionTo(spotBehindClosestStunTrap));
//
////                    MapLocation spotToWait = getClosestDamLoc().add(dirAwayFromDam);
////                    rc.setIndicatorString("This is my safe place: " + spotToWait.toString());
////                    if (!rc.getLocation().equals(spotToWait)) movement.smallMove(rc.getLocation().directionTo(spotToWait));
//
//                    MapLocation spotToWait = closestStunTrap.add(dirAwayFromDam).add(dirAwayFromDam);
////                rc.setIndicatorString("This is my safe place: " + spotToWait.toString());
//                    if (!rc.getLocation().equals(spotToWait))
//                        movement.smallMove(rc.getLocation().directionTo(spotToWait));
//
////                }
//                }
//            } else {
//                attackMicroWithMoveAvailable();
//            }
//            if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS + turnsToWait + 1) { // if we waited for the enemy for 8 turns, and they didn't come, go back to normal
//                iAmStrategicallyWaiting = false;
//            }
//        }
//    }
//
//    public void berserkMode() throws GameActionException {
//        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//        MapLocation closestAttackableEnemy = getAttackableEnemyWithLowestHealth(enemies);
//        if (closestAttackableEnemy != null) {
//            if (rc.canAttack(closestAttackableEnemy)) rc.attack(closestAttackableEnemy);
//            movement.smallMove(rc.getLocation().directionTo(closestAttackableEnemy).opposite());
//        }
//        if (rc.isActionReady()) {
//            MapLocation closestEnemy = closestEnemyToMe(enemies);
//            if (closestEnemy != null) {
//                if (rc.canMove(rc.getLocation().directionTo(closestEnemy))) {
//                    movement.smallMove(rc.getLocation().directionTo(closestEnemy));
//                    closestAttackableEnemy = getAttackableEnemyWithLowestHealth(enemies);
//                    if (closestAttackableEnemy != null && rc.canAttack(closestAttackableEnemy)) rc.attack(closestEnemy);
//                }
//            } else {
//                tryToHeal();
//            }
//        }
//
//        if (turnsToGoBeserkFor == 0) {
//            goBeserk = false;
//            turnsToGoBeserkFor = 10;
//        } else {
//            turnsToGoBeserkFor--;
//        }
//    }

    /**
     * Use this attacking/healing micro when you do NOT want to move in the micro! (you want to beeline to a location and
     * not waste your movement in the micro (getting to the place is super urgent!!!!!)
     *
     * @throws GameActionException could not attack or heal smthn we tried to I think
     */
//    public void attackMicroWithNoMoveAvailable() throws GameActionException {
//        // find the closest enemy to me. if I can attack it, attack it. if it is one movement away from being
//        // able to attack me, do nothing (save attack for next turn when they are maybe closer). else (the enemy is
//        // too far to care about), heal yourself, or others
//        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//
//        if (enemyRobots.length != 0) {
//            MapLocation attackableEnemyLocation = getAttackableEnemyWithLowestHealth(enemyRobots);
//            Utility.MyPair<Boolean, MapLocation> canIBeAttackedNextTurn = canEnemyAttackMeNextTurn(enemyRobots);
//            if (attackableEnemyLocation != null) { // if there's an opp I can attack, best believe we running his fade
//                if (rc.canAttack(attackableEnemyLocation)) rc.attack(attackableEnemyLocation);
//            } else if (canIBeAttackedNextTurn.first()) { // if we can be attacked next turn, not much we can really do
//                // without a movement, so just save attack cooldown for next turn to attack them maybe
//                // do nothing
//            } else { // if we can't be attacked next turn, heal up the brothas
//                tryToHeal();
//            }
//        } else { // no worries in the world, just heal up the gang
//            tryToHeal();
//        }
//    }

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
                doMicro();
            } else if (canIBeAttackedNextTurn.first()) { // someone can potentially attack us next turn if they move to us. go smack them
                if (rc.getHealth() >= 600) {
                    if (rc.isActionReady()) { // we can fight back, so go smack them
                        movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()));
//                        if (movement.MovementStack.empty()) movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()));
                        attackableEnemyLocation = getAttackableEnemyWithLowestHealth(enemyRobots);
                        if (attackableEnemyLocation != null && rc.canAttack(attackableEnemyLocation))
                            rc.attack(attackableEnemyLocation);
                    } else { // we cannot fight back, try to run
                        movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()).opposite());
                    }
                } else {
                    doMicro();
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


    public void someDamStrategy(int turnsToWait) throws GameActionException {
        if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS - 3 || rc.getRoundNum() == GameConstants.SETUP_ROUNDS - 2) {
            MapInfo[] damStuff = rc.senseNearbyMapInfos();
            boolean byDam = false;
            for (MapInfo location : damStuff) {
                if (location.isDam() && rc.getLocation().isAdjacentTo(location.getMapLocation())) {
                    byDam = true;
                }
            }
            RobotInfo[] someEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            boolean anyEnemyCloseEnoughToWarrantPlacingATrap = someEnemies.length > 0;
            if (byDam && anyEnemyCloseEnoughToWarrantPlacingATrap) {
                MapLocation closestEnemy = closestEnemyToMe(someEnemies);
//                if (rc.canBuild(TrapType.STUN, rc.getLocation()))
//                    utility.placeTrapNearEnemySingleLoc(rc.getLocation());
                System.out.println("tryna place trap");
                utility.placeTrapNearEnemy(closestEnemy);
            }

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
//                attackMicroWithMoveAvailable();
            }
            if (rc.getRoundNum() == GameConstants.SETUP_ROUNDS + turnsToWait + 1) { // if we waited for the enemy for enough turns, and they didn't come, go back to normal
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


    // isn't it important to get the lowest health enemy if there are multiple close enough to attack??
    MapLocation getPriorityEnemy(RobotInfo[] robots) {
        if (robots.length == 0) return null;
        MapLocation targetLoc = null;
        int minDist = INF;
        for (RobotInfo robot : robots) {
            int dist = rc.getLocation().distanceSquaredTo(robot.location);
            if (robot.health <= rc.getAttackDamage())
                return robot.location;
            if (targetLoc == null || dist < minDist) {
                targetLoc = robot.location;
                minDist = dist;
            }
        }
        return targetLoc;
    }

    MapLocation getClosestEnemy(RobotInfo[] robots) {
        if (robots.length == 0) return null;
        MapLocation targetLoc = null;
        int minDist = INF;
        for (RobotInfo robot : robots) {
            int dist = rc.getLocation().distanceSquaredTo(robot.location);
            if (targetLoc == null || dist < minDist) {
                targetLoc = robot.location;
                minDist = dist;
            }
        }
        return targetLoc;
    }

    /*------------------------------------------------------------------------------------------------------------*/
    final int INF = 1000000;
    final Direction[] dirs = Direction.values();
    MapLocation currentLoc;
    boolean shouldPlaySafe = false;
    boolean alwaysInRange = false;
    boolean canAttack;

    boolean doMicro() throws GameActionException {
        if (!rc.isMovementReady()) return false;
        shouldPlaySafe = false;
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (robots.length == 0) return false;
        canAttack = rc.isActionReady();
        currentLoc = getClosestEnemy(robots);

//        shouldPlaySafe = true;


        if (rc.getHealth() < 600 && rc.getLocation().distanceSquaredTo(currentLoc) <= 15) {
//            rc.setIndicatorString("we are low health and someone is in vision radius, play safe!!! (doMicro)");
            shouldPlaySafe = true;
        }

        if (currentLoc != null && rc.getLocation().distanceSquaredTo(currentLoc) <= GameConstants.ATTACK_RADIUS_SQUARED) {
//            rc.setIndicatorString("someone is in attack range, play safe!!! (doMicro)");
            shouldPlaySafe = true;
        }
//
//        if (rc.getLocation().distanceSquaredTo(currentLoc) <= 15) {
//            rc.setIndicatorString("we are low health and someone is in vision radius, play safe!!! (doMicro)");
//            shouldPlaySafe = true;
//        }

        if (!shouldPlaySafe) return false;
        alwaysInRange = !canAttack;

        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 0; i < 9; i++) microInfo[i] = new MicroInfo(dirs[i]);

        for (RobotInfo robot : robots) {
            currentLoc = robot.getLocation();
            microInfo[0].updateEnemy();
            microInfo[1].updateEnemy();
            microInfo[2].updateEnemy();
            microInfo[3].updateEnemy();
            microInfo[4].updateEnemy();
            microInfo[5].updateEnemy();
            microInfo[6].updateEnemy();
            microInfo[7].updateEnemy();
            microInfo[8].updateEnemy();
        }

        robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for (RobotInfo robot : robots) {
            currentLoc = robot.getLocation();
            microInfo[0].updateAlly();
            microInfo[1].updateAlly();
            microInfo[2].updateAlly();
            microInfo[3].updateAlly();
            microInfo[4].updateAlly();
            microInfo[5].updateAlly();
            microInfo[6].updateAlly();
            microInfo[7].updateAlly();
            microInfo[8].updateAlly();
        }

        MicroInfo bestMicro = microInfo[8];
        for (int i = 0; i < 8; ++i) {
            if (microInfo[i].isBetter(bestMicro)) bestMicro = microInfo[i];
            if (rc.getRoundNum() == 243 && rc.getID() == 10087)System.out.println();
        }

        return apply(bestMicro);
    }

    boolean apply(MicroInfo bestMicro) throws GameActionException {
        if (bestMicro.dir == Direction.CENTER) return true;

        rc.setIndicatorDot(rc.getLocation().add(bestMicro.dir), 0, 255, 0);

        if (rc.canMove(bestMicro.dir)) {
            rc.move(bestMicro.dir);
            return true;
        }
        return false;
    }

    void attack() throws GameActionException {
        if (!rc.isActionReady()) return;
        RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
        MapLocation target = getPriorityEnemy(robots);
        if (target != null) {
            if (rc.canAttack(target)) rc.attack(target);
        }
    }

    void attackLowestHealth() throws GameActionException {
        if (!rc.isActionReady()) return;
        RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
        MapLocation target = null;
        for (RobotInfo enemy : robots) {
            if (target == null || enemy.health < rc.senseRobotAtLocation(target).health) {
                target = enemy.location;
            }
        }
        if (target != null) {
            if (rc.canAttack(target)) rc.attack(target);
        }
    }


    void moveToTarget() throws GameActionException {
        if (!rc.isMovementReady()) return;

        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        MapLocation target = getClosestEnemy(robots);
        if (target != null) {
            useBannedMovement(target);
            return;
        }
        if (tryMoveToOppFlag()) return;

        // Rarely will call this
        useBannedMovement(locationForFlagrunnerGroup);
    }

    boolean tryMoveToOppFlag() throws GameActionException {
        if (oppFlags == null || oppFlags.isEmpty()) return false;

        if (symmetry.isSymmetryValid()) {
            oppFlags.clear();

            if (rc.readSharedArray(51) != 0)
                oppFlags.add(utility.intToLocation(rc.readSharedArray(51)));
            if (rc.readSharedArray(52) != 0)
                oppFlags.add(utility.intToLocation(rc.readSharedArray(52)));
            if (rc.readSharedArray(53) != 0)
                oppFlags.add(utility.intToLocation(rc.readSharedArray(53)));

            if (oppFlags.isEmpty()) return false;
        }

        if (rc.getLocation().isAdjacentTo(oppFlags.get(oppFlagsIndex)) && !rc.canPickupFlag(oppFlags.get(oppFlagsIndex))) {
            if (symmetry.isSymmetryValid()) {
                if (utility.intToLocation(rc.readSharedArray(51)).equals(oppFlags.get(oppFlagsIndex)))
                    rc.writeSharedArray(51, 0);
                if (utility.intToLocation(rc.readSharedArray(52)).equals(oppFlags.get(oppFlagsIndex)))
                    rc.writeSharedArray(52, 0);
                if (utility.intToLocation(rc.readSharedArray(53)).equals(oppFlags.get(oppFlagsIndex)))
                    rc.writeSharedArray(53, 0);
            }
            oppFlags.remove(oppFlagsIndex);

            if (oppFlags.isEmpty()) return false;
            oppFlagsIndex %= oppFlags.size();
        }

        FlagInfo[] flagInfo = rc.senseNearbyFlags(-1, rc.getTeam().opponent());

        if (rc.canSenseLocation(oppFlags.get(oppFlagsIndex)) && flagInfo.length == 0) {
            if (symmetry.isSymmetryValid()) {
                if (utility.intToLocation(rc.readSharedArray(51)).equals(oppFlags.get(oppFlagsIndex)))
                    rc.writeSharedArray(51, 0);
                if (utility.intToLocation(rc.readSharedArray(52)).equals(oppFlags.get(oppFlagsIndex)))
                    rc.writeSharedArray(52, 0);
                if (utility.intToLocation(rc.readSharedArray(53)).equals(oppFlags.get(oppFlagsIndex)))
                    rc.writeSharedArray(53, 0);
            }

            oppFlags.remove(oppFlagsIndex);

            if (oppFlags.isEmpty()) return false;
            oppFlagsIndex %= oppFlags.size();
        }

        // For flags that are moved
        for (FlagInfo flag : flagInfo) {
            if (!oppFlags.contains(flag.getLocation())) {
                useBannedMovement(flag.getLocation());
                if (rc.canPickupFlag(flag.getLocation())) rc.pickupFlag(flag.getLocation());
                return true;
            }
        }
        useBannedMovement(oppFlags.get(oppFlagsIndex));
        return true;
    }

    class MicroInfo {
        Direction dir;
        MapLocation location;
        int minDistanceToEnemy = INF;
        int canLandHit = 0;
        int enemiesInAttackRange = 0;
        int enemiesInVisionRange = 0;
        int minDistToAlly = INF;

        MapLocation target = null;

        boolean canMove = true;

        public MicroInfo(Direction dir) {
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            if (dir != Direction.CENTER && !rc.canMove(dir)) canMove = false;
        }

        void updateEnemy() {
            if (!canMove) return;
            int dist = location.distanceSquaredTo(currentLoc);
            if (dist < minDistanceToEnemy) minDistanceToEnemy = dist;
            if (dist <= GameConstants.ATTACK_RADIUS_SQUARED) enemiesInAttackRange++;
            if (dist <= GameConstants.VISION_RADIUS_SQUARED) enemiesInVisionRange++;

            if (dist <= GameConstants.ATTACK_RADIUS_SQUARED && canAttack) {
                canLandHit = 1;
                target = currentLoc;
            }
        }

        void updateAlly() {
            if (!canMove) return;
            int dist = location.distanceSquaredTo(currentLoc);
            if (dist < minDistToAlly) minDistToAlly = dist;
        }

        boolean inRange() {
            if (alwaysInRange) return true;
            return minDistanceToEnemy <= GameConstants.ATTACK_RADIUS_SQUARED;
        }

        boolean isBetter(MicroInfo M) {
            // M is current best micro

//            if (rc.getRoundNum() == 243 && rc.getID() == 10087) System.out.println("best micro: " + M.dir + " | micro we are checking: " + dir);


            // if best micro cannot move and this one can, then switch it
            if (canMove && !M.canMove) return true;
            // and if best micro can move and this one can't, don't switch it
            if (!canMove && M.canMove) return false;

//            if (rc.getRoundNum() == 243 && rc.getID() == 10087) {
//                System.out.println("enemies in attack range: " + enemiesInAttackRange + " best micro enemies in attack range: " + M.enemiesInAttackRange);
//            }

            // FIRST CHECK: whichever micro puts u in spot with less enemies in attack range, do that one
            if (enemiesInAttackRange - canLandHit < M.enemiesInAttackRange - M.canLandHit) return true;
            if (enemiesInAttackRange - canLandHit > M.enemiesInAttackRange - M.canLandHit) return false;

//            if (rc.getRoundNum() == 243 && rc.getID() == 10087) System.out.println("enemies in vision range: " + enemiesInVisionRange + " best micro enemies in vision range: " + M.enemiesInVisionRange);
//
//
//            if (enemiesInVisionRange - canLandHit < M.enemiesInVisionRange - M.canLandHit) return true;
//            if (enemiesInVisionRange - canLandHit > M.enemiesInVisionRange - M.canLandHit) return false;

//            if (rc.getRoundNum() == 243 && rc.getID() == 10087) System.out.println("can land hit: " + canLandHit + " best micro can land hit: " + M.canLandHit);

            // SECOND CHECK: if above checks say that placement in between enemies is equal, do whatever micro hits someone
            if (canLandHit > M.canLandHit) return true;
            if (canLandHit < M.canLandHit) return false;

//            if (rc.getRoundNum() == 243 && rc.getID() == 10087) System.out.println("min dist to ally: " + minDistToAlly + " best micro min dist to ally: " + M.minDistToAlly);

            // THIRD CHECK: if everything is god damn equal, do the one that puts you closer to more allies
            if (minDistToAlly < M.minDistToAlly) return true;
            if (minDistToAlly > M.minDistToAlly) return false;

//            System.out.println("HEREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");

//            if (rc.getRoundNum() == 243 && rc.getID() == 10087) System.out.println("min dist to enemy: " + minDistanceToEnemy + " best micro min dist to enemy: " + M.minDistanceToEnemy);

            // FOURTH CHECK: if still undecided, HUH
            if (inRange()) return minDistanceToEnemy >= M.minDistanceToEnemy;
            else return minDistanceToEnemy <= M.minDistanceToEnemy;
        }
    }
}
