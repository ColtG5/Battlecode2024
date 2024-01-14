package GoldenSon.after_specialists;

import battlecode.common.*;
import GoldenSon.Movement;
import GoldenSon.Utility;

import java.util.ArrayList;

import static GoldenSon.RobotPlayer.*;

public class Flagrunner {
    RobotController rc;
    Movement movement;
    Utility util;
    MapLocation locationForFlagrunnerGroup;

    public Flagrunner(RobotController rc, Movement movement, Utility util) {
        this.rc = rc;
        this.movement = movement;
        this.util = util;
    }

    public void run() throws GameActionException {
        if (util.amIAGroupLeader()) locationForFlagrunnerGroup = setLocationForGroup(); // decide where the group will go (including you)
        else locationForFlagrunnerGroup = getLocationForGroup();

//        rc.setIndicatorDot(locationForFlagrunnerGroup, 0, 0, 255);

        boolean isLeader = util.amIAGroupLeader();
        if (rc.getRoundNum() == 201) System.out.println(isLeader);

        if (isLeader) {
            if (tooFewGroupMembersAround(8)) { // if leader don't got a lotta homies, maybe just sit and wait for the gang?
                attackMicroWithMoveAvailable();
                // have the option of choosing to sit in place to wait for dudes to pull up on you
                // attackMicroWithNoMoveAvailable();
//                rc.setIndicatorDot(rc.getLocation(), 255, 255, 0);
            } else { // if u got homies, gameplan as usual.
                attackMicroWithMoveAvailable();
            }
            if (rc.getRoundNum() == 201) System.out.println("gang gang");
            rc.setIndicatorDot(rc.getLocation(), 0, 0, 125);
        } else { // a follower
            if (isDistanceToGroupLeaderMoreThan(10)) { // if too far from group leader, use ur movement to get back to them!!
                movement.hardMove(util.getLocationOfMyGroupLeader());
                attackMicroWithNoMoveAvailable();
//                rc.setIndicatorDot(util.getLocationOfMyGroupLeader(), 0, 255, 0);
            } else { // if ur close enough, u can use ur movement in ur micro
                attackMicroWithMoveAvailable();
            }
        }
    }

    // ---------------------------------------------------------------------------------
    //                               helper funcs below
    // ---------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------
    //                              buddy checking funcs
    // ---------------------------------------------------------------------------------

    public boolean tooFewGroupMembersAround(int lessThanThis) throws GameActionException {
        RobotInfo[] robotInfos = rc.senseNearbyRobots(-1, rc.getTeam());
        return robotInfos.length < lessThanThis;
    }

    public boolean isDistanceToGroupLeaderMoreThan(int distance) throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        MapLocation groupLeaderLocation = util.readLocationFromFlagrunnerGroupIndex();
        return myLocation.distanceSquaredTo(groupLeaderLocation) > distance;
    }

    public MapLocation setLocationForGroup() throws GameActionException {
        MapLocation locForGroup = null;

        // see if we can sense any enemy flags
        FlagInfo[] enemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        // filter out flags that are picked up by us (this group only going to capture a flag rn. if a flag is picked up by us,
        // lets assume for now that he has his own group to be backup
        ArrayList<FlagInfo> enemyFlagsNotPickedUp = new ArrayList<FlagInfo>();
        for (FlagInfo enemyFlag : enemyFlags) {
            if (!enemyFlag.isPickedUp()) enemyFlagsNotPickedUp.add(enemyFlag);
        }

        MapLocation[] allDroppedFlags = rc.senseBroadcastFlagLocations();

        if (enemyFlagsNotPickedUp.size() != 0) { // if we can see a flag to conquer
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

            int indexOfFirstTeammate = 1 + (FLAGRUNNERS_PER_GROUP * (util.getMyFlagrunnerGroup()-1)); // 1, 15, 29
            int indexOfLastTeammate = util.getMyFlagrunnerGroup() * FLAGRUNNERS_PER_GROUP; // 14, 28, 42

            for (int i = indexOfFirstTeammate-1; i <= indexOfLastTeammate-1; i++) {
                Utility.CoolRobotInfo coolRobotInfo = coolRobotInfoArray[i];
                if (coolRobotInfo.getHasFlag()) {
                    locForGroup = coolRobotInfo.getCurLocation();
                    break;
                }
            }
        }
        if (locForGroup == null) locForGroup = rc.getLocation(); // literally no flags in play (should never happen right

//        rc.setIndicatorDot(locForGroup, 255, 0, 255);

        // write this locForGroup into the spot in the shared array for this group
        util.writeToFlagrunnerGroupIndex(locForGroup);
        return locForGroup;
    }

    public MapLocation getLocationForGroup() throws GameActionException {
        return util.readLocationFromFlagrunnerGroupIndex();
    }

    // ---------------------------------------------------------------------------------
    //                        attacking/healing micro helper funcs
    // ---------------------------------------------------------------------------------

    public MapLocation getAttackableEnemyWithLowestHealth(RobotInfo[] enemyRobots) {
        ArrayList<RobotInfo> attackableEnemies = new ArrayList<RobotInfo>();
        for (RobotInfo enemyRobot : enemyRobots) {
            if (rc.canAttack(enemyRobot.location)) attackableEnemies.add(enemyRobot);
        }
        if (attackableEnemies.size() == 0) return null;

        RobotInfo lowestHealthEnemy = attackableEnemies.get(0);
        for (RobotInfo enemyRobot : attackableEnemies) {
            if (enemyRobot.health < lowestHealthEnemy.health) lowestHealthEnemy = enemyRobot;
        }
        return lowestHealthEnemy.location;
    }

    /**
     * Returns a pair of a boolean and a MapLocation. The boolean is true if the enemy can attack me next turn, and
     * false if the enemy cannot attack me next turn. The MapLocation is the location of the enemy that can attack me
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
        return new Utility.MyPair<Boolean, MapLocation>(canEnemyAttackMeNextTurn, locationOfEnemyThatCanAttackMe);
    }

    public void tryToHeal() throws GameActionException {
        if (rc.canHeal(rc.getLocation())) {
            rc.heal(rc.getLocation());
            return;
        }

        RobotInfo[] friendliesInRangeToHeal = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());
        if (friendliesInRangeToHeal.length == 0) return;
        RobotInfo lowestHealthFriendly = friendliesInRangeToHeal[0];
        for (RobotInfo friendly : friendliesInRangeToHeal) {
            if (friendly.health < lowestHealthFriendly.health) lowestHealthFriendly = friendly;
        }

        if (rc.canHeal(lowestHealthFriendly.location)) rc.heal(lowestHealthFriendly.location);
    }

    public void moveAwayFromEnemyIJustAttacked(MapLocation enemyLocation) throws GameActionException {
        Direction directionAwayFromEnemy = rc.getLocation().directionTo(enemyLocation).opposite();
        if (rc.canMove(directionAwayFromEnemy)) rc.move(directionAwayFromEnemy);
    }

    // ---------------------------------------------------------------------------------
    //                            attacking/healing micro
    // ---------------------------------------------------------------------------------

    /**
     * Use this attacking/healing micro when you do NOT want to move in the micro! (you want to beeline to a location and
     * not waste your movement in the micro (getting to the place is super urgent!!!!!)
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
     * @throws GameActionException if we cant move, attack, or heal I think???
     */
    public void attackMicroWithMoveAvailable() throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        if (enemyRobots.length != 0) {
            MapLocation attackableEnemyLocation = getAttackableEnemyWithLowestHealth(enemyRobots);
            Utility.MyPair<Boolean, MapLocation> canIBeAttackedNextTurn = canEnemyAttackMeNextTurn(enemyRobots);
            if (attackableEnemyLocation != null) { // somebody can attack us! hit them and run away
                if (rc.canAttack(attackableEnemyLocation)) rc.attack(attackableEnemyLocation);
                moveAwayFromEnemyIJustAttacked(attackableEnemyLocation);
            } else if (canIBeAttackedNextTurn.first()) { // someone can potentially attack us next turn if they move to us. go smack them
                if (rc.isActionReady()) { // we can fight back, so go smack them
                    movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()));
                    if (rc.canAttack(canIBeAttackedNextTurn.second())) rc.attack(canIBeAttackedNextTurn.second());
                } else { // we cannot fight back, try to run
                    movement.smallMove(rc.getLocation().directionTo(canIBeAttackedNextTurn.second()).opposite());
                }
            } else { // there's an enemy, but they cant attack us next turn. save our action cooldown, and just move to our goal
                // two strats here: either stay put and try to heal,
                tryToHeal();
                // or move to the goal and don't heal
                movement.hardMove(locationForFlagrunnerGroup);
            }
        } else { // zero enemies on our radar, so walk to spot we gotta go, and heal up
            movement.hardMove(locationForFlagrunnerGroup);
            tryToHeal();
        }
    }
}