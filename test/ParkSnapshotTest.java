import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests the read-only park snapshot.
 *
 * @author mlkuhl
 * @version 2026.11.09
 */
public class ParkSnapshotTest extends TestCase
{
    private List<Integer> counts;
    private List<ParkSnapshot.RideInfo> rides;
    private ParkSnapshot snapshot;

    /**
     * Creates a fresh snapshot before each test.
     */
    public void setUp()
    {
        counts = new ArrayList<Integer>();
        counts.add(1);
        counts.add(0);

        rides = new ArrayList<ParkSnapshot.RideInfo>();
        rides.add(new ParkSnapshot.RideInfo(1, "Alpha", true, true,
            4, 2, 10, 3, true));
        rides.add(new ParkSnapshot.RideInfo(2, "Beta", false, false,
            0, 0, -1, -1, false));

        snapshot = new ParkSnapshot(630, "Ryan", "QUEUED", 1, 3, 4, 5.5,
            counts, rides);
    }

    /**
     * Tests the stored park and player values.
     */
    public void testFields()
    {
        assertEquals(630, snapshot.currentMinute);
        assertEquals("Ryan", snapshot.playerName);
        assertEquals("QUEUED", snapshot.playerState);
        assertEquals(1, snapshot.fastPasses);
        assertEquals(3, snapshot.totalRides);
        assertEquals(4, snapshot.lastWaitMinutes);
        assertEquals(5.5, snapshot.averageWaitMinutes, 0.0001);
    }

    /**
     * Tests the stored ride counts.
     */
    public void testRideCounts()
    {
        assertEquals(2, snapshot.rideCounts.size());
        assertEquals(Integer.valueOf(1), snapshot.rideCounts.get(0));
        assertEquals(Integer.valueOf(0), snapshot.rideCounts.get(1));
    }

    /**
     * Tests the display values of the first ride.
     */
    public void testFirstRideInfo()
    {
        ParkSnapshot.RideInfo ride = snapshot.rides.get(0);

        assertEquals(1, ride.id);
        assertEquals("Alpha", ride.name);
        assertTrue(ride.eligible);
        assertTrue(ride.hasVip);
        assertEquals(4, ride.standardCount);
        assertEquals(2, ride.vipCount);
        assertEquals(10, ride.standardWait);
        assertEquals(3, ride.vipWait);
        assertTrue(ride.running);
    }

    /**
     * Tests the display values of the second ride.
     */
    public void testSecondRideInfo()
    {
        ParkSnapshot.RideInfo ride = snapshot.rides.get(1);

        assertEquals(2, ride.id);
        assertEquals("Beta", ride.name);
        assertFalse(ride.eligible);
        assertFalse(ride.hasVip);
        assertEquals(0, ride.standardCount);
        assertEquals(0, ride.vipCount);
        assertEquals(-1, ride.standardWait);
        assertEquals(-1, ride.vipWait);
        assertFalse(ride.running);
    }

    /**
     * Tests that ride order is preserved.
     */
    public void testRideOrder()
    {
        assertEquals(2, snapshot.rides.size());
        assertEquals("Alpha", snapshot.rides.get(0).name);
        assertEquals("Beta", snapshot.rides.get(1).name);
    }

    /**
     * Tests that a null player name becomes an empty string.
     */
    public void testNullPlayerName()
    {
        ParkSnapshot unregistered = new ParkSnapshot(600, null,
            "NOT_REGISTERED", 0, 0, 0, 0.0, counts, rides);

        assertEquals("", unregistered.playerName);
    }

    /**
     * Tests that changing the source lists does not change the snapshot.
     */
    public void testListsAreCopied()
    {
        counts.set(0, 99);
        counts.add(7);
        rides.clear();

        assertEquals(2, snapshot.rideCounts.size());
        assertEquals(Integer.valueOf(1), snapshot.rideCounts.get(0));
        assertEquals(2, snapshot.rides.size());
    }

    /**
     * Tests that the ride count list cannot be modified.
     */
    public void testRideCountsUnmodifiable()
    {
        try
        {
            snapshot.rideCounts.add(5);
            fail("Expected UnsupportedOperationException.");
        }
        catch (UnsupportedOperationException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            snapshot.rideCounts.set(0, 5);
            fail("Expected UnsupportedOperationException.");
        }
        catch (UnsupportedOperationException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(Integer.valueOf(1), snapshot.rideCounts.get(0));
    }

    /**
     * Tests that the ride list cannot be modified.
     */
    public void testRidesUnmodifiable()
    {
        try
        {
            snapshot.rides.add(rides.get(0));
            fail("Expected UnsupportedOperationException.");
        }
        catch (UnsupportedOperationException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            snapshot.rides.remove(0);
            fail("Expected UnsupportedOperationException.");
        }
        catch (UnsupportedOperationException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(2, snapshot.rides.size());
    }

    /**
     * Tests an empty snapshot.
     */
    public void testEmptyLists()
    {
        ParkSnapshot empty = new ParkSnapshot(600, "", "FREE", 0, 0, 0,
            0.0, new ArrayList<Integer>(),
            new ArrayList<ParkSnapshot.RideInfo>());

        assertTrue(empty.rideCounts.isEmpty());
        assertTrue(empty.rides.isEmpty());
    }
}