import junit.framework.TestCase;

/**
 * Tests the park simulation model.
 *
 * Most simulation tests use a single-ride fixture with no random arrivals,
 * whose next dispatch is at minute 603 and whose ride lasts two minutes.
 *
 * @author mlkuhl
 * @version 2026.11.09
 */
public class ParkModelTest extends TestCase
{
    private ParkModel park;
    private Coaster fixture;
    private ParkModel model;

    /**
     * Creates a full park and a fixture park before each test.
     */
    public void setUp()
    {
        park = new ParkModel(1L);
        fixture = new Coaster(1, "Fixture", 4, 40, 80, 300, 5, 2,
            0.0, false, 10);
        fixture.setNextDispatchMinute(603);
        model = new ParkModel(1L, new Coaster[] {fixture});
    }

    /**
     * Returns one ride's display values.
     *
     * @param target the model to read
     * @param id the ride ID
     * @return the ride's display values
     */
    private ParkSnapshot.RideInfo ride(ParkModel target, int id)
    {
        return target.getSnapshot().rides.get(id - 1);
    }

    /**
     * Creates a model with one supplied ride and a registered player.
     *
     * @param coaster the ride
     * @return the model
     */
    private ParkModel registeredModel(Coaster coaster)
    {
        ParkModel result = new ParkModel(1L, new Coaster[] {coaster});
        result.registerPlayer("Ryan", 60, 150);
        return result;
    }

    // ~ Construction .......................................................

    /**
     * Tests the initial park state.
     */
    public void testInitialState()
    {
        ParkSnapshot snap = park.getSnapshot();

        assertEquals(600, snap.currentMinute);
        assertEquals("", snap.playerName);
        assertEquals("NOT_REGISTERED", snap.playerState);
        assertEquals(0, snap.fastPasses);
        assertEquals(0, snap.totalRides);
        assertEquals(0, snap.lastWaitMinutes);
        assertEquals(0.0, snap.averageWaitMinutes, 0.0001);
        assertEquals(8, snap.rideCounts.size());
        assertEquals(8, snap.rides.size());

        for (int i = 0; i < 8; i++)
        {
            ParkSnapshot.RideInfo info = snap.rides.get(i);

            assertEquals(i + 1, info.id);
            assertEquals(Integer.valueOf(0), snap.rideCounts.get(i));
            assertEquals(0, info.standardCount);
            assertEquals(0, info.vipCount);
            assertTrue(info.eligible);
            assertFalse(info.running);
        }
    }

    /**
     * Tests the configured rides and their initial wait estimates.
     */
    public void testInitialRides()
    {
        ParkSnapshot.RideInfo first = ride(park, 1);
        ParkSnapshot.RideInfo gemini = ride(park, 5);
        ParkSnapshot.RideInfo last = ride(park, 8);

        assertEquals("Steel Vengeance", first.name);
        assertTrue(first.hasVip);
        assertEquals(8, first.standardWait);
        assertEquals(8, first.vipWait);

        assertEquals("Gemini", gemini.name);
        assertFalse(gemini.hasVip);
        assertEquals(-1, gemini.vipWait);

        assertEquals("Woodstock Express", last.name);
        assertFalse(last.hasVip);
        assertEquals(5, last.standardWait);
    }

    /**
     * Tests extreme seeds.
     */
    public void testSeedBoundaries()
    {
        ParkModel low = new ParkModel(Long.MIN_VALUE);
        ParkModel high = new ParkModel(Long.MAX_VALUE);

        assertEquals(600, low.getSnapshot().currentMinute);
        assertEquals(8, low.getSnapshot().rides.size());
        assertEquals(600, high.getSnapshot().currentMinute);
        assertEquals(8, high.getSnapshot().rides.size());
    }

    /**
     * Tests that equal seeds and commands give equal results.
     */
    public void testSameSeedSameResults()
    {
        ParkModel first = new ParkModel(99L);
        ParkModel second = new ParkModel(99L);

        first.registerPlayer("Ryan", 60, 150);
        second.registerPlayer("Ryan", 60, 150);
        first.joinLine(2, false);
        second.joinLine(2, false);

        for (int step = 0; step < 120; step++)
        {
            first.advanceTime(5);
            second.advanceTime(5);
            assertSameSnapshot(first.getSnapshot(), second.getSnapshot());

            if (first.getSnapshot().playerState.equals("FREE"))
            {
                first.joinLine(2, false);
                second.joinLine(2, false);
            }
        }
    }

    /**
     * Compares two snapshots field by field.
     *
     * @param a the first snapshot
     * @param b the second snapshot
     */
    private void assertSameSnapshot(ParkSnapshot a, ParkSnapshot b)
    {
        assertEquals(a.currentMinute, b.currentMinute);
        assertEquals(a.playerState, b.playerState);
        assertEquals(a.totalRides, b.totalRides);
        assertEquals(a.lastWaitMinutes, b.lastWaitMinutes);
        assertEquals(a.fastPasses, b.fastPasses);

        for (int i = 0; i < a.rides.size(); i++)
        {
            assertEquals(a.rides.get(i).standardCount,
                b.rides.get(i).standardCount);
            assertEquals(a.rides.get(i).vipCount,
                b.rides.get(i).vipCount);
            assertEquals(a.rides.get(i).standardWait,
                b.rides.get(i).standardWait);
        }
    }

    /**
     * Tests rejection of invalid fixture rides.
     */
    public void testInvalidFixtureRides()
    {
        try
        {
            new ParkModel(1L, null);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            new ParkModel(1L, new Coaster[0]);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            new ParkModel(1L, new Coaster[] {fixture, null});
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    // ~ registerPlayer .....................................................

    /**
     * Tests a valid registration.
     */
    public void testRegisterPlayer()
    {
        park.registerPlayer("Ryan", 60, 150);

        ParkSnapshot snap = park.getSnapshot();

        assertEquals("Ryan", snap.playerName);
        assertEquals("FREE", snap.playerState);
        assertEquals(2, snap.fastPasses);
        assertEquals(0, snap.totalRides);
    }

    /**
     * Tests that the name is trimmed.
     */
    public void testRegisterTrimsName()
    {
        park.registerPlayer("  Ryan  ", 60, 150);

        assertEquals("Ryan", park.getSnapshot().playerName);
    }

    /**
     * Tests the accepted profile boundaries.
     */
    public void testRegisterBoundaries()
    {
        ParkModel small = new ParkModel(1L);
        ParkModel large = new ParkModel(1L);

        small.registerPlayer("A", 1, 1);
        large.registerPlayer("B", 96, 700);

        assertEquals("FREE", small.getSnapshot().playerState);
        assertEquals("FREE", large.getSnapshot().playerState);
    }

    /**
     * Tests null and blank names.
     */
    public void testRegisterInvalidName()
    {
        try
        {
            park.registerPlayer(null, 60, 150);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            park.registerPlayer("", 60, 150);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            park.registerPlayer("   ", 60, 150);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("NOT_REGISTERED", park.getSnapshot().playerState);
        assertEquals(0, park.getSnapshot().fastPasses);
    }

    /**
     * Tests invalid heights.
     */
    public void testRegisterInvalidHeight()
    {
        try
        {
            park.registerPlayer("Ryan", 0, 150);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            park.registerPlayer("Ryan", -60, 150);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            park.registerPlayer("Ryan", 97, 150);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("NOT_REGISTERED", park.getSnapshot().playerState);
    }

    /**
     * Tests invalid weights.
     */
    public void testRegisterInvalidWeight()
    {
        try
        {
            park.registerPlayer("Ryan", 60, 0);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            park.registerPlayer("Ryan", 60, -150);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            park.registerPlayer("Ryan", 60, 701);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("NOT_REGISTERED", park.getSnapshot().playerState);
    }

    /**
     * Tests that a rejected registration allows a later valid one.
     */
    public void testRegisterAfterFailure()
    {
        try
        {
            park.registerPlayer("Ryan", 0, 150);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        park.registerPlayer("Ryan", 60, 150);

        assertEquals("FREE", park.getSnapshot().playerState);
    }

    /**
     * Tests that a second registration is rejected and changes nothing.
     */
    public void testRegisterTwice()
    {
        park.registerPlayer("Ryan", 60, 150);

        try
        {
            park.registerPlayer("Mikey", 70, 180);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("Ryan", park.getSnapshot().playerName);
        assertEquals(2, park.getSnapshot().fastPasses);
    }

    // ~ joinLine ...........................................................

    /**
     * Tests a valid join.
     */
    public void testJoinLine()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.joinLine(3, false);

        ParkSnapshot snap = park.getSnapshot();

        assertEquals("QUEUED", snap.playerState);
        assertEquals(1, snap.rides.get(2).standardCount);
        assertEquals(0, snap.rides.get(0).standardCount);
        assertEquals(0, snap.rides.get(3).standardCount);
        assertEquals(2, snap.fastPasses);
    }

    /**
     * Tests ride IDs that do not exist.
     */
    public void testJoinInvalidId()
    {
        park.registerPlayer("Ryan", 60, 150);

        try
        {
            park.joinLine(0, false);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            park.joinLine(-1, false);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            park.joinLine(9, false);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("FREE", park.getSnapshot().playerState);
    }

    /**
     * Tests that an invalid ID is reported even before registration.
     */
    public void testJoinInvalidIdUnregistered()
    {
        try
        {
            park.joinLine(9, false);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests joining before registering.
     */
    public void testJoinUnregistered()
    {
        try
        {
            park.joinLine(1, false);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("NOT_REGISTERED", park.getSnapshot().playerState);
        assertEquals(0, ride(park, 1).standardCount);
    }

    /**
     * Tests that a queued player cannot join another line.
     */
    public void testJoinWhileQueued()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.joinLine(3, false);

        try
        {
            park.joinLine(4, false);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            park.joinLine(3, true);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(1, ride(park, 3).standardCount);
        assertEquals(0, ride(park, 3).vipCount);
        assertEquals(0, ride(park, 4).standardCount);
        assertEquals(2, park.getSnapshot().fastPasses);
    }

    /**
     * Tests that a riding player cannot join a line.
     */
    public void testJoinWhileRiding()
    {
        model.registerPlayer("Ryan", 60, 150);
        model.joinLine(1, false);
        model.advanceTime(3);

        assertEquals("RIDING", model.getSnapshot().playerState);

        try
        {
            model.joinLine(1, false);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(0, ride(model, 1).standardCount);
        assertEquals("RIDING", model.getSnapshot().playerState);
    }

    /**
     * Tests that nobody can join after the park closes.
     */
    public void testJoinWhenClosed()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.advanceTime(1000);

        assertEquals("CLOSED", park.getSnapshot().playerState);

        try
        {
            park.joinLine(1, false);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(2, park.getSnapshot().fastPasses);
    }

    /**
     * Tests a player who is too short.
     */
    public void testJoinTooShort()
    {
        park.registerPlayer("Ryan", 45, 150);

        assertFalse(ride(park, 1).eligible);
        assertTrue(ride(park, 7).eligible);

        try
        {
            park.joinLine(1, false);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("FREE", park.getSnapshot().playerState);
        assertEquals(0, ride(park, 1).standardCount);
    }

    /**
     * Tests that the minimum height is inclusive.
     */
    public void testJoinAtMinimumHeight()
    {
        park.registerPlayer("Ryan", 42, 150);
        park.joinLine(7, false);

        assertEquals("QUEUED", park.getSnapshot().playerState);
    }

    /**
     * Tests a player one inch under the minimum height.
     */
    public void testJoinBelowMinimumHeight()
    {
        park.registerPlayer("Ryan", 41, 150);

        try
        {
            park.joinLine(7, false);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("FREE", park.getSnapshot().playerState);
    }

    /**
     * Tests that the maximum height is inclusive.
     */
    public void testMaximumHeight()
    {
        park.registerPlayer("Ryan", 84, 150);

        assertFalse(ride(park, 1).eligible);
        assertTrue(ride(park, 7).eligible);

        park.joinLine(8, false);

        assertEquals("QUEUED", park.getSnapshot().playerState);
    }

    /**
     * Tests a player taller than every ride allows.
     */
    public void testJoinTooTall()
    {
        park.registerPlayer("Ryan", 85, 150);

        for (int id = 1; id <= 8; id++)
        {
            assertFalse(ride(park, id).eligible);
        }

        try
        {
            park.joinLine(8, false);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("FREE", park.getSnapshot().playerState);
    }

    /**
     * Tests the weight limits, including the inclusive maximum.
     */
    public void testWeightLimits()
    {
        park.registerPlayer("Ryan", 60, 300);

        assertTrue(ride(park, 1).eligible);

        ParkModel heavy = new ParkModel(1L);
        heavy.registerPlayer("Mikey", 60, 301);

        assertFalse(ride(heavy, 1).eligible);
        assertTrue(ride(heavy, 4).eligible);

        try
        {
            heavy.joinLine(1, false);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        ParkModel heaviest = new ParkModel(1L);
        heaviest.registerPlayer("Eyal", 60, 401);

        assertFalse(ride(heaviest, 8).eligible);
    }

    /**
     * Tests a VIP join, which uses one pass.
     */
    public void testJoinVip()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.joinLine(1, true);

        assertEquals("QUEUED", park.getSnapshot().playerState);
        assertEquals(1, park.getSnapshot().fastPasses);
        assertEquals(1, ride(park, 1).vipCount);
        assertEquals(0, ride(park, 1).standardCount);
    }

    /**
     * Tests the VIP line of a ride that has none.
     */
    public void testJoinUnsupportedVip()
    {
        park.registerPlayer("Ryan", 60, 150);

        try
        {
            park.joinLine(5, true);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("FREE", park.getSnapshot().playerState);
        assertEquals(2, park.getSnapshot().fastPasses);
        assertEquals(0, ride(park, 5).standardCount);
    }

    /**
     * Tests joining the VIP line with no passes left.
     */
    public void testJoinVipWithoutPasses()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.joinLine(1, true);
        park.leaveLine();
        park.joinLine(1, true);
        park.leaveLine();

        assertEquals(0, park.getSnapshot().fastPasses);

        try
        {
            park.joinLine(1, true);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("FREE", park.getSnapshot().playerState);
        assertEquals(0, park.getSnapshot().fastPasses);
        assertEquals(0, ride(park, 1).vipCount);

        park.joinLine(1, false);

        assertEquals("QUEUED", park.getSnapshot().playerState);
    }

    /**
     * Tests that an abandoned entry still occupies a full line.
     */
    public void testJoinFullLineWithAbandonedEntry()
    {
        Coaster tiny = new Coaster(1, "Tiny", 4, 40, 80, 300, 5, 2,
            0.0, false, 1);
        tiny.setNextDispatchMinute(603);
        ParkModel tinyModel = registeredModel(tiny);

        tinyModel.joinLine(1, false);
        tinyModel.leaveLine();

        try
        {
            tinyModel.joinLine(1, false);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("FREE", tinyModel.getSnapshot().playerState);
        assertEquals(0, ride(tinyModel, 1).standardCount);
    }

    /**
     * Tests a line filled by random arrivals.
     */
    public void testJoinFullLineFromArrivals()
    {
        Coaster busy = new Coaster(1, "Busy", 4, 40, 80, 300, 5, 2,
            1.0, false, 2);
        busy.setNextDispatchMinute(700);
        ParkModel busyModel = registeredModel(busy);

        busyModel.advanceTime(5);

        try
        {
            busyModel.joinLine(1, false);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("FREE", busyModel.getSnapshot().playerState);
    }

    /**
     * Tests the ineligible flag before registration.
     */
    public void testEligibleBeforeRegistration()
    {
        for (int id = 1; id <= 8; id++)
        {
            assertTrue(ride(park, id).eligible);
        }
    }

    // ~ leaveLine ..........................................................

    /**
     * Tests leaving a line.
     */
    public void testLeaveLine()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.joinLine(2, false);
        park.leaveLine();

        assertEquals("FREE", park.getSnapshot().playerState);
        assertEquals(0, ride(park, 2).standardCount);
    }

    /**
     * Tests leaving a VIP line, which does not refund the pass.
     */
    public void testLeaveVipLine()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.joinLine(2, true);
        park.leaveLine();

        assertEquals("FREE", park.getSnapshot().playerState);
        assertEquals(0, ride(park, 2).vipCount);
        assertEquals(1, park.getSnapshot().fastPasses);
    }

    /**
     * Tests that the player can join another line after leaving.
     */
    public void testJoinAfterLeaving()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.joinLine(2, false);
        park.leaveLine();
        park.joinLine(3, false);

        assertEquals("QUEUED", park.getSnapshot().playerState);
        assertEquals(0, ride(park, 2).standardCount);
        assertEquals(1, ride(park, 3).standardCount);
    }

    /**
     * Tests leaving while free.
     */
    public void testLeaveWhileFree()
    {
        park.registerPlayer("Ryan", 60, 150);

        try
        {
            park.leaveLine();
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("FREE", park.getSnapshot().playerState);
    }

    /**
     * Tests leaving before registration.
     */
    public void testLeaveUnregistered()
    {
        try
        {
            park.leaveLine();
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("NOT_REGISTERED", park.getSnapshot().playerState);
    }

    /**
     * Tests leaving while riding.
     */
    public void testLeaveWhileRiding()
    {
        model.registerPlayer("Ryan", 60, 150);
        model.joinLine(1, false);
        model.advanceTime(3);

        try
        {
            model.leaveLine();
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals("RIDING", model.getSnapshot().playerState);
    }

    /**
     * Tests leaving after the park closes.
     */
    public void testLeaveWhenClosed()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.advanceTime(1000);

        try
        {
            park.leaveLine();
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests that an abandoned entry does not take a seat from a rejoin.
     */
    public void testLeaveAndRejoinSameMinute()
    {
        Coaster single = new Coaster(1, "One Seat", 1, 40, 80, 300, 5, 2,
            0.0, false, 10);
        single.setNextDispatchMinute(603);
        ParkModel singleModel = registeredModel(single);

        singleModel.joinLine(1, false);
        singleModel.leaveLine();
        singleModel.joinLine(1, false);
        singleModel.advanceTime(5);

        ParkSnapshot snap = singleModel.getSnapshot();

        assertEquals("FREE", snap.playerState);
        assertEquals(1, snap.totalRides);
        assertEquals(3, snap.lastWaitMinutes);
        assertEquals(0, snap.rides.get(0).standardCount);
    }

    /**
     * Tests that a rejoin is timed from the new join, not the old one.
     */
    public void testLeaveAndRejoinLater()
    {
        model.registerPlayer("Ryan", 60, 150);
        model.joinLine(1, false);
        model.leaveLine();
        model.advanceTime(1);
        model.joinLine(1, false);
        model.advanceTime(4);

        ParkSnapshot snap = model.getSnapshot();

        assertEquals(605, snap.currentMinute);
        assertEquals(1, snap.totalRides);
        assertEquals(2, snap.lastWaitMinutes);
        assertEquals(2.0, snap.averageWaitMinutes, 0.0001);
    }

    // ~ advanceTime ........................................................

    /**
     * Tests a ride that completes within five minutes.
     */
    public void testAdvanceCompletesRide()
    {
        model.registerPlayer("Ryan", 60, 150);
        model.joinLine(1, false);
        model.advanceTime(5);

        ParkSnapshot snap = model.getSnapshot();

        assertEquals(605, snap.currentMinute);
        assertEquals("FREE", snap.playerState);
        assertEquals(1, snap.totalRides);
        assertEquals(3, snap.lastWaitMinutes);
        assertEquals(3.0, snap.averageWaitMinutes, 0.0001);
        assertEquals(Integer.valueOf(1), snap.rideCounts.get(0));
        assertFalse(snap.rides.get(0).running);
    }

    /**
     * Tests the riding state and that a ride is not counted until it ends.
     */
    public void testRidingBeforeCompletion()
    {
        model.registerPlayer("Ryan", 60, 150);
        model.joinLine(1, false);
        model.advanceTime(3);

        ParkSnapshot snap = model.getSnapshot();

        assertEquals(603, snap.currentMinute);
        assertEquals("RIDING", snap.playerState);
        assertEquals(0, snap.totalRides);
        assertEquals(0, snap.lastWaitMinutes);
        assertEquals(0.0, snap.averageWaitMinutes, 0.0001);
        assertTrue(snap.rides.get(0).running);
        assertEquals(0, snap.rides.get(0).standardCount);

        model.advanceTime(2);

        assertEquals("FREE", model.getSnapshot().playerState);
        assertEquals(1, model.getSnapshot().totalRides);
    }

    /**
     * Tests the average over two rides.
     */
    public void testAverageOverTwoRides()
    {
        model.registerPlayer("Ryan", 60, 150);
        model.joinLine(1, false);
        model.advanceTime(5);
        model.advanceTime(1);
        model.joinLine(1, false);
        model.advanceTime(4);

        ParkSnapshot snap = model.getSnapshot();

        assertEquals(610, snap.currentMinute);
        assertEquals(2, snap.totalRides);
        assertEquals(2, snap.lastWaitMinutes);
        assertEquals(2.5, snap.averageWaitMinutes, 0.0001);
        assertEquals(Integer.valueOf(2), snap.rideCounts.get(0));
    }

    /**
     * Tests a VIP ride.
     */
    public void testVipRideCompletes()
    {
        Coaster vipRide = new Coaster(1, "Vip", 4, 40, 80, 300, 5, 2,
            0.0, true, 10);
        vipRide.setNextDispatchMinute(603);
        ParkModel vipModel = registeredModel(vipRide);

        vipModel.joinLine(1, true);
        vipModel.advanceTime(5);

        ParkSnapshot snap = vipModel.getSnapshot();

        assertEquals(1, snap.totalRides);
        assertEquals(3, snap.lastWaitMinutes);
        assertEquals(1, snap.fastPasses);
        assertEquals(0, snap.rides.get(0).vipCount);
    }

    /**
     * Tests that advancing without joining a line records nothing.
     */
    public void testAdvanceWhileFree()
    {
        model.registerPlayer("Ryan", 60, 150);
        model.advanceTime(5);

        ParkSnapshot snap = model.getSnapshot();

        assertEquals(605, snap.currentMinute);
        assertEquals("FREE", snap.playerState);
        assertEquals(0, snap.totalRides);
        assertEquals(0.0, snap.averageWaitMinutes, 0.0001);
    }

    /**
     * Tests advancing by a single minute.
     */
    public void testAdvanceOneMinute()
    {
        model.registerPlayer("Ryan", 60, 150);
        model.advanceTime(1);

        assertEquals(601, model.getSnapshot().currentMinute);
    }

    /**
     * Tests nonpositive step counts.
     */
    public void testAdvanceNonPositive()
    {
        model.registerPlayer("Ryan", 60, 150);

        try
        {
            model.advanceTime(0);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            model.advanceTime(-5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(600, model.getSnapshot().currentMinute);
    }

    /**
     * Tests that a nonpositive count is reported before registration.
     */
    public void testAdvanceNonPositiveUnregistered()
    {
        try
        {
            park.advanceTime(0);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests advancing before registration.
     */
    public void testAdvanceUnregistered()
    {
        try
        {
            park.advanceTime(5);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(600, park.getSnapshot().currentMinute);
    }

    /**
     * Tests that time stops at closing.
     */
    public void testAdvanceStopsAtClosing()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.advanceTime(10000);

        ParkSnapshot snap = park.getSnapshot();

        assertEquals(1200, snap.currentMinute);
        assertEquals("CLOSED", snap.playerState);
    }

    /**
     * Tests the last minute before closing and the closing step.
     */
    public void testClosingBoundary()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.advanceTime(599);

        assertEquals(1199, park.getSnapshot().currentMinute);
        assertEquals("FREE", park.getSnapshot().playerState);

        park.advanceTime(5);

        assertEquals(1200, park.getSnapshot().currentMinute);
        assertEquals("CLOSED", park.getSnapshot().playerState);
    }

    /**
     * Tests the park closing after exactly 120 five-minute steps.
     */
    public void testFiveMinuteStepsToClosing()
    {
        park.registerPlayer("Ryan", 60, 150);

        for (int step = 0; step < 120; step++)
        {
            assertEquals("FREE", park.getSnapshot().playerState);
            park.advanceTime(5);
        }

        assertEquals(1200, park.getSnapshot().currentMinute);
        assertEquals("CLOSED", park.getSnapshot().playerState);
    }

    /**
     * Tests advancing after closing.
     */
    public void testAdvanceWhenClosed()
    {
        park.registerPlayer("Ryan", 60, 150);
        park.advanceTime(10000);

        try
        {
            park.advanceTime(5);
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(1200, park.getSnapshot().currentMinute);
    }

    /**
     * Tests that a queued player at closing completes no ride.
     */
    public void testClosingWhileQueued()
    {
        Coaster never = new Coaster(1, "Never", 4, 40, 80, 300, 5, 2,
            0.0, false, 10);
        never.setNextDispatchMinute(5000);
        ParkModel neverModel = registeredModel(never);

        neverModel.joinLine(1, false);
        neverModel.advanceTime(10000);

        ParkSnapshot snap = neverModel.getSnapshot();

        assertEquals("CLOSED", snap.playerState);
        assertEquals(0, snap.totalRides);
    }

    /**
     * Tests a ride that finishes exactly at closing.
     */
    public void testRideEndingAtClosing()
    {
        Coaster late = new Coaster(1, "Late", 4, 40, 80, 300, 5, 2,
            0.0, false, 10);
        late.setNextDispatchMinute(1198);
        ParkModel lateModel = registeredModel(late);

        lateModel.joinLine(1, false);
        lateModel.advanceTime(10000);

        ParkSnapshot snap = lateModel.getSnapshot();

        assertEquals("CLOSED", snap.playerState);
        assertEquals(1, snap.totalRides);
        assertEquals(598, snap.lastWaitMinutes);
    }

    /**
     * Tests that a ride still running at closing is finished and counted.
     */
    public void testRideInProgressAtClosing()
    {
        Coaster late = new Coaster(1, "Late", 4, 40, 80, 300, 5, 3,
            0.0, false, 10);
        late.setNextDispatchMinute(1198);
        ParkModel lateModel = registeredModel(late);

        lateModel.joinLine(1, false);
        lateModel.advanceTime(10000);

        ParkSnapshot snap = lateModel.getSnapshot();

        assertEquals("CLOSED", snap.playerState);
        assertEquals(1, snap.totalRides);
        assertEquals(598, snap.lastWaitMinutes);
        assertFalse(snap.rides.get(0).running);
    }

    // ~ boarding and arrivals ..............................................

    /**
     * Tests that arrivals after the player do not board before the player.
     */
    public void testLaterArrivalsBoardAfterPlayer()
    {
        Coaster busy = new Coaster(1, "Busy", 1, 40, 80, 300, 5, 2,
            1.0, false, 50);
        busy.setNextDispatchMinute(603);
        ParkModel busyModel = registeredModel(busy);

        busyModel.joinLine(1, false);
        busyModel.advanceTime(3);

        ParkSnapshot snap = busyModel.getSnapshot();

        assertEquals("RIDING", snap.playerState);
        assertTrue(snap.rides.get(0).running);
        assertTrue(snap.rides.get(0).standardCount <= 3);
    }

    /**
     * Tests that guests already in line board before the player.
     */
    public void testEarlierArrivalsBoardBeforePlayer()
    {
        Coaster busy = new Coaster(1, "Busy", 1, 40, 80, 300, 5, 2,
            1.0, false, 50);
        busy.setNextDispatchMinute(603);
        ParkModel busyModel = registeredModel(busy);

        busyModel.advanceTime(2);
        busyModel.joinLine(1, false);
        busyModel.advanceTime(1);

        ParkSnapshot snap = busyModel.getSnapshot();

        assertEquals("QUEUED", snap.playerState);
        assertTrue(snap.rides.get(0).running);
    }

    /**
     * Tests that the player is not abandoned by the random abandonment.
     */
    public void testPlayerNeverAbandonsAutomatically()
    {
        Coaster slow = new Coaster(1, "Slow", 4, 40, 80, 300, 5, 2,
            1.0, false, 100);
        slow.setNextDispatchMinute(5000);
        ParkModel slowModel = registeredModel(slow);

        slowModel.joinLine(1, false);
        slowModel.advanceTime(300);

        int before = ride(slowModel, 1).standardCount;

        assertEquals("QUEUED", slowModel.getSnapshot().playerState);
        assertTrue(before >= 1);

        slowModel.leaveLine();

        assertEquals(before - 1, ride(slowModel, 1).standardCount);
    }

    /**
     * Tests that random arrivals build a line.
     */
    public void testArrivalsFillLine()
    {
        Coaster busy = new Coaster(1, "Busy", 4, 40, 80, 300, 5, 2,
            1.0, false, 50);
        busy.setNextDispatchMinute(5000);
        ParkModel busyModel = registeredModel(busy);

        busyModel.advanceTime(5);

        int count = ride(busyModel, 1).standardCount;

        assertTrue(count >= 1);
        assertTrue(count <= 5);
    }

    /**
     * Tests that no arrivals happen at probability zero.
     */
    public void testNoArrivals()
    {
        model.registerPlayer("Ryan", 60, 150);
        model.advanceTime(100);

        assertEquals(0, ride(model, 1).standardCount);
    }

    // ~ wait estimates .....................................................

    /**
     * Tests the wait estimate with an empty line.
     */
    public void testWaitEstimateEmptyLine()
    {
        assertEquals(3, ride(model, 1).standardWait);
        assertEquals(-1, ride(model, 1).vipWait);
    }

    /**
     * Tests that the estimate counts the trains ahead of a new guest.
     */
    public void testWaitEstimateGrowsWithLine()
    {
        Coaster single = new Coaster(1, "One Seat", 1, 40, 80, 300, 5, 2,
            0.0, false, 10);
        single.setNextDispatchMinute(603);
        ParkModel singleModel = registeredModel(single);

        assertEquals(3, ride(singleModel, 1).standardWait);

        singleModel.joinLine(1, false);

        assertEquals(8, ride(singleModel, 1).standardWait);
    }

    /**
     * Tests that a wait ending after closing is unavailable.
     */
    public void testWaitUnavailableNearClosing()
    {
        model.registerPlayer("Ryan", 60, 150);
        model.advanceTime(599);

        assertEquals(1199, model.getSnapshot().currentMinute);
        assertEquals(-1, ride(model, 1).standardWait);
    }

    // ~ snapshots ..........................................................

    /**
     * Tests that an earlier snapshot is not changed by later commands.
     */
    public void testSnapshotIsIndependent()
    {
        park.registerPlayer("Ryan", 60, 150);

        ParkSnapshot before = park.getSnapshot();

        park.joinLine(3, false);
        park.advanceTime(5);

        assertEquals(600, before.currentMinute);
        assertEquals("FREE", before.playerState);
        assertEquals(0, before.rides.get(2).standardCount);
    }

    /**
     * Tests that snapshot lists cannot be modified.
     */
    public void testSnapshotListsUnmodifiable()
    {
        ParkSnapshot snap = park.getSnapshot();

        try
        {
            snap.rides.add(snap.rides.get(0));
            fail("Expected UnsupportedOperationException.");
        }
        catch (UnsupportedOperationException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            snap.rideCounts.set(0, 5);
            fail("Expected UnsupportedOperationException.");
        }
        catch (UnsupportedOperationException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(Integer.valueOf(0), park.getSnapshot().rideCounts.get(0));
    }

    /**
     * Tests the eligibility flags of a mid-sized player.
     */
    public void testEligibilityFlags()
    {
        park.registerPlayer("Ryan", 45, 150);

        assertFalse(ride(park, 1).eligible);
        assertFalse(ride(park, 3).eligible);
        assertFalse(ride(park, 6).eligible);
        assertTrue(ride(park, 7).eligible);
        assertTrue(ride(park, 8).eligible);
    }
}