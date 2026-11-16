import junit.framework.TestCase;

/**
 * Tests the coaster state holder.
 *
 * @author CoasterQ team
 * @version 2026.11.09
 */
public class CoasterTest extends TestCase
{
    private Coaster plain;
    private Coaster vip;
    private Guest guest;

    /**
     * Creates fresh coasters before each test.
     */
    public void setUp()
    {
        plain = new Coaster(5, "Gemini", 4, 48, 78, 300, 6, 2,
            0.5, false, 2);
        vip = new Coaster(1, "Steel Vengeance", 6, 52, 78, 350, 8, 3,
            0.9, true, 4);
        guest = new Guest("Ryan", 60, 150, 1065);
    }

    /**
     * Tests the fixed configuration values.
     */
    public void testConfiguration()
    {
        assertEquals(5, plain.getId());
        assertEquals("Gemini", plain.getName());
        assertEquals(4, plain.getSeatCapacity());
        assertEquals(48, plain.getMinHeight());
        assertEquals(78, plain.getMaxHeight());
        assertEquals(300, plain.getMaxWeight());
        assertEquals(6, plain.getDispatchInterval());
        assertEquals(2, plain.getRideDuration());
        assertEquals(0.5, plain.getArrivalProbability(), 0.0001);
        assertFalse(plain.hasVip());
    }

    /**
     * Tests the initial simulation state.
     */
    public void testInitialState()
    {
        assertTrue(plain.getStandardLine().isEmpty());
        assertTrue(plain.getStandardWaiting().isEmpty());
        assertTrue(plain.getCurrentRiders().isEmpty());
        assertEquals(0, plain.getNextDispatchMinute());
        assertEquals(-1, plain.getCompletionMinute());
        assertFalse(plain.isRunning());
    }

    /**
     * Tests that a ride without VIP has no VIP line.
     */
    public void testNoVipLine()
    {
        assertNull(plain.getVipLine());
        assertNull(plain.getVipWaiting());
        assertTrue(plain.isLineFull(true));
    }

    /**
     * Tests that a VIP ride has an empty VIP line.
     */
    public void testVipLine()
    {
        assertTrue(vip.hasVip());
        assertNotNull(vip.getVipLine());
        assertNotNull(vip.getVipWaiting());
        assertTrue(vip.getVipLine().isEmpty());
        assertTrue(vip.getVipWaiting().isEmpty());
        assertFalse(vip.isLineFull(true));
    }

    /**
     * Tests the dispatch and completion setters.
     */
    public void testSetters()
    {
        plain.setNextDispatchMinute(603);
        plain.setCompletionMinute(605);

        assertEquals(603, plain.getNextDispatchMinute());
        assertEquals(605, plain.getCompletionMinute());
        assertTrue(plain.isRunning());

        plain.setCompletionMinute(-1);

        assertFalse(plain.isRunning());
    }

    /**
     * Tests that the regular line reports full at capacity.
     */
    public void testStandardLineFull()
    {
        assertFalse(plain.isLineFull(false));

        plain.getStandardLine().enqueue(new QueueEntry(guest, 600, 1L));
        assertFalse(plain.isLineFull(false));

        plain.getStandardLine().enqueue(new QueueEntry(guest, 600, 2L));
        assertTrue(plain.isLineFull(false));
    }

    /**
     * Tests that abandoned entries still count toward capacity.
     */
    public void testAbandonedEntryStillCounts()
    {
        QueueEntry first = new QueueEntry(guest, 600, 1L);
        plain.getStandardLine().enqueue(first);
        plain.getStandardLine().enqueue(new QueueEntry(guest, 600, 2L));
        first.abandon();

        assertTrue(plain.isLineFull(false));
    }

    /**
     * Tests that the VIP line holds half the regular capacity.
     */
    public void testVipLineCapacity()
    {
        vip.getVipLine().enqueue(new QueueEntry(guest, 600, 1L));
        assertFalse(vip.isLineFull(true));

        vip.getVipLine().enqueue(new QueueEntry(guest, 600, 2L));
        assertTrue(vip.isLineFull(true));
        assertFalse(vip.isLineFull(false));
    }

    /**
     * Tests that the VIP line has room for one guest at minimum.
     */
    public void testVipLineCapacityMinimum()
    {
        Coaster tiny = new Coaster(2, "Tiny", 4, 40, 80, 300, 5, 2,
            0.5, true, 1);

        assertFalse(tiny.isLineFull(true));

        tiny.getVipLine().enqueue(new QueueEntry(guest, 600, 1L));

        assertTrue(tiny.isLineFull(true));
    }

    /**
     * Tests boundary values that are allowed.
     */
    public void testBoundaryValuesAccepted()
    {
        Coaster edge = new Coaster(1, "Edge", 1, 10, 10, 1, 5, 5,
            0.0, false, 1);
        Coaster certain = new Coaster(2, "Certain", 1, 10, 20, 1, 5, 2,
            1.0, false, 1);

        assertEquals(5, edge.getRideDuration());
        assertEquals(1.0, certain.getArrivalProbability(), 0.0001);
    }

    /**
     * Tests invalid names.
     */
    public void testInvalidName()
    {
        try
        {
            new Coaster(1, null, 4, 40, 80, 300, 5, 2, 0.5, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            new Coaster(1, "  ", 4, 40, 80, 300, 5, 2, 0.5, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests a nonpositive seat capacity.
     */
    public void testInvalidSeatCapacity()
    {
        try
        {
            new Coaster(1, "A", 0, 40, 80, 300, 5, 2, 0.5, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests invalid height limits.
     */
    public void testInvalidHeights()
    {
        try
        {
            new Coaster(1, "A", 4, 0, 80, 300, 5, 2, 0.5, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            new Coaster(1, "A", 4, 50, 49, 300, 5, 2, 0.5, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests a nonpositive maximum weight.
     */
    public void testInvalidMaxWeight()
    {
        try
        {
            new Coaster(1, "A", 4, 40, 80, 0, 5, 2, 0.5, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests invalid dispatch intervals and ride durations.
     */
    public void testInvalidTiming()
    {
        try
        {
            new Coaster(1, "A", 4, 40, 80, 300, 0, 2, 0.5, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            new Coaster(1, "A", 4, 40, 80, 300, 5, 0, 0.5, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            new Coaster(1, "A", 4, 40, 80, 300, 5, 6, 0.5, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests probabilities outside zero to one.
     */
    public void testInvalidProbability()
    {
        try
        {
            new Coaster(1, "A", 4, 40, 80, 300, 5, 2, -0.1, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            new Coaster(1, "A", 4, 40, 80, 300, 5, 2, 1.1, false, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests a nonpositive queue capacity.
     */
    public void testInvalidQueueCapacity()
    {
        try
        {
            new Coaster(1, "A", 4, 40, 80, 300, 5, 2, 0.5, false, 0);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }
}