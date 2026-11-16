import java.util.NoSuchElementException;
import junit.framework.TestCase;
/**
 * Tests the bounded linked queue.
 *
 * @author Ryan Gilligan
 * @version 2026.11.08
 */
public class LinkedQueueTest extends TestCase
{
    private LinkedQueue<String> queue;

    /**
     * Creates a fresh queue before each test.
     */
    public void setUp()
    {
        queue = new LinkedQueue<String>(2);
    }

    /**
     * Tests the initial state.
     */
    public void testInitialState()
    {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    /**
     * Tests a zero capacity.
     */
    public void testZeroCapacity()
    {
        try
        {
            new LinkedQueue<String>(0);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests a negative capacity.
     */
    public void testNegativeCapacity()
    {
        try
        {
            new LinkedQueue<String>(-1);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests adding elements.
     */
    public void testEnqueue()
    {
        queue.enqueue("A");

        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
        assertEquals("A", queue.peek());

        queue.enqueue("B");

        assertEquals(2, queue.size());
        assertEquals("A", queue.peek());
    }

    /**
     * Tests rejection of null without changing the queue.
     */
    public void testEnqueueNull()
    {
        queue.enqueue("A");

        try
        {
            queue.enqueue(null);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(1, queue.size());
        assertEquals("A", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    /**
     * Tests rejection when full without changing the contents.
     */
    public void testEnqueueFull()
    {
        queue.enqueue("A");
        queue.enqueue("B");

        try
        {
            queue.enqueue("C");
            fail("Expected IllegalStateException.");
        }
        catch (IllegalStateException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(2, queue.size());
        assertEquals("A", queue.dequeue());
        assertEquals("B", queue.dequeue());
    }

    /**
     * Tests null rejection when the queue is also full.
     */
    public void testEnqueueNullWhenFull()
    {
        queue.enqueue("A");
        queue.enqueue("B");

        try
        {
            queue.enqueue(null);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(2, queue.size());
        assertEquals("A", queue.dequeue());
        assertEquals("B", queue.dequeue());
    }

    /**
     * Tests FIFO removal and size changes.
     */
    public void testDequeue()
    {
        queue.enqueue("A");
        queue.enqueue("B");

        assertEquals("A", queue.dequeue());
        assertEquals(1, queue.size());
        assertEquals("B", queue.peek());

        assertEquals("B", queue.dequeue());
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    /**
     * Tests removal from an empty queue.
     */
    public void testDequeueEmpty()
    {
        try
        {
            queue.dequeue();
            fail("Expected NoSuchElementException.");
        }
        catch (NoSuchElementException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());

        queue.enqueue("A");
        assertEquals("A", queue.dequeue());
    }

    /**
     * Tests removal after the final element was removed.
     */
    public void testDequeueAfterDraining()
    {
        queue.enqueue("A");
        queue.dequeue();

        try
        {
            queue.dequeue();
            fail("Expected NoSuchElementException.");
        }
        catch (NoSuchElementException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    /**
     * Tests that peeking does not remove anything.
     */
    public void testPeek()
    {
        queue.enqueue("A");
        queue.enqueue("B");

        assertEquals("A", queue.peek());
        assertEquals("A", queue.peek());
        assertEquals(2, queue.size());

        assertEquals("A", queue.dequeue());
        assertEquals("B", queue.peek());
        assertEquals(1, queue.size());
    }

    /**
     * Tests peeking at an empty queue.
     */
    public void testPeekEmpty()
    {
        try
        {
            queue.peek();
            fail("Expected NoSuchElementException.");
        }
        catch (NoSuchElementException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    /**
     * Tests peeking after the final element was removed.
     */
    public void testPeekAfterDraining()
    {
        queue.enqueue("A");
        queue.dequeue();

        try
        {
            queue.peek();
            fail("Expected NoSuchElementException.");
        }
        catch (NoSuchElementException exception)
        {
            assertNotNull(exception);
        }

        assertEquals(0, queue.size());
    }

    /**
     * Tests reuse after completely emptying the queue.
     */
    public void testReuseAfterEmptying()
    {
        queue.enqueue("A");
        assertEquals("A", queue.dequeue());

        queue.enqueue("B");
        queue.enqueue("C");

        assertEquals("B", queue.dequeue());
        assertEquals("C", queue.dequeue());
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    /**
     * Tests alternating additions and removals.
     */
    public void testInterleavedOperations()
    {
        queue.enqueue("A");
        queue.enqueue("B");

        assertEquals("A", queue.dequeue());

        queue.enqueue("C");

        assertEquals(2, queue.size());
        assertEquals("B", queue.dequeue());
        assertEquals("C", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    /**
     * Tests a capacity-one queue with a different element type.
     */
    public void testCapacityOne()
    {
        LinkedQueue<Integer> single =
            new LinkedQueue<Integer>(1);

        single.enqueue(7);

        assertEquals(Integer.valueOf(7), single.peek());
        assertEquals(Integer.valueOf(7), single.dequeue());
        assertTrue(single.isEmpty());

        single.enqueue(9);

        assertEquals(Integer.valueOf(9), single.dequeue());
        assertEquals(0, single.size());
    }

    /**
     * Tests that duplicate values are stored separately.
     */
    public void testDuplicateValues()
    {
        queue.enqueue("A");
        queue.enqueue("A");

        assertEquals("A", queue.dequeue());
        assertEquals(1, queue.size());

        assertEquals("A", queue.dequeue());
        assertTrue(queue.isEmpty());
    }
}
