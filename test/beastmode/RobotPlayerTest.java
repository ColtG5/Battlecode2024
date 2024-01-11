package beastmode;

import battlecode.common.MapLocation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import beastmode.Utility;

public class RobotPlayerTest {

	@Test
	public void testLocToInt() {
		Utility util = new Utility(null);
		MapLocation loc = new MapLocation(12, 35);
		int fortnite = util.locationToInt(loc);
		assertEquals(803, fortnite);
	}

	@Test
	public void testIntToLoc() {
		Utility util = new Utility(null);
		MapLocation loc = new MapLocation(12, 35);
		int fortnite = util.locationToInt(loc);
		MapLocation fortniteLoc = util.intToLocation(fortnite);
		assertEquals(loc, fortniteLoc);
	}

}
