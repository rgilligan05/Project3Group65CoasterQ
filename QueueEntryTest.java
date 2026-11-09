import student.TestCase;

/**
 * Tests queue visit data and abandonment.
 *
 * @author Ryan Gilligan
 * @version 2026.11.08
 */
public class QueueEntryTest extends TestCase
{
    private Guest guest;
    private QueueEntry entry;

    /**
     * Creates a fresh guest and visit.
     */
    public void setUp()
    {
        guest = new Guest("Ryan", 60, 150);
        entry = new QueueEntry(guest, 600, 1L);
    }

    /**
     * Tests the initial field values.
     */
    public void testInitialValues()
    {
        assertSame(guest, entry.getGuest());
        assertEquals(600, entry.getJoinedMinute());
        assertEquals(1L, entry.getVisitToken());
        assertFalse(entry.isAbandoned());
    }

    /**
     * Tests abandonment without changing immutable values.
     */
    public void testAbandon()
    {
        entry.abandon();

        assertTrue(entry.isAbandoned());
        assertSame(guest, entry.getGuest());
        assertEquals(600, entry.getJoinedMinute());
        assertEquals(1L, entry.getVisitToken());
    }

    /**
     * Tests repeated abandonment.
     */
    public void testRepeatedAbandon()
    {
        entry.abandon();
        entry.abandon();

        assertTrue(entry.isAbandoned());
    }

    /**
     * Tests that abandoning an old visit leaves a new one active.
     */
    public void testIndependentVisits()
    {
        QueueEntry later = new QueueEntry(guest, 605, 2L);

        entry.abandon();

        assertTrue(entry.isAbandoned());
        assertFalse(later.isAbandoned());
        assertSame(guest, later.getGuest());
        assertEquals(605, later.getJoinedMinute());
        assertEquals(2L, later.getVisitToken());
    }

    /**
     * Tests that abandoned entries remain stored until removed.
     */
    public void testStoredAbandonedEntries()
    {
        LinkedQueue<QueueEntry> visits =
            new LinkedQueue<QueueEntry>(2);

        QueueEntry later = new QueueEntry(guest, 605, 2L);

        visits.enqueue(entry);
        visits.enqueue(later);

        entry.abandon();

        assertEquals(2, visits.size());
        assertSame(entry, visits.peek());
        assertSame(entry, visits.dequeue());
        assertSame(later, visits.dequeue());
        assertFalse(later.isAbandoned());
        assertTrue(visits.isEmpty());
    }

    /**
     * Tests rejection of a null guest.
     */
    public void testNullGuest()
    {
        try
        {
            new QueueEntry(null, 600, 3L);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        assertFalse(entry.isAbandoned());
    }

    /**
     * Tests rejection of a negative join time.
     */
    public void testNegativeMinute()
    {
        try
        {
            new QueueEntry(guest, -1, 3L);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(600, entry.getJoinedMinute());
    }

    /**
     * Tests a join time and token of zero.
     */
    public void testZeroMinute()
    {
        QueueEntry zero = new QueueEntry(guest, 0, 0L);

        assertEquals(0, zero.getJoinedMinute());
        assertEquals(0L, zero.getVisitToken());
        assertFalse(zero.isAbandoned());
    }

    /**
     * Tests preservation of a large token.
     */
    public void testTokenBoundary()
    {
        QueueEntry boundary =
            new QueueEntry(guest, 600, Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE, boundary.getVisitToken());
    }
}
