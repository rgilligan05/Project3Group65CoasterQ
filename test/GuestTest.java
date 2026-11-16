import junit.framework.TestCase;

/**
 * Tests the guest holder.
 *
 * @author CoasterQ team
 * @version 2026.11.09
 */
public class GuestTest extends TestCase
{
    private Guest guest;

    /**
     * Creates a fresh guest before each test.
     */
    public void setUp()
    {
        guest = new Guest("Ryan", 60, 150, 1065);
    }

    /**
     * Tests the stored values.
     */
    public void testGetters()
    {
        assertEquals("Ryan", guest.getName());
        assertEquals(60, guest.getHeightInches());
        assertEquals(150, guest.getWeightPounds());
    }

    /**
     * Tests that surrounding whitespace is removed from the name.
     */
    public void testNameIsTrimmed()
    {
        Guest padded = new Guest("  Mikey  ", 70, 180, 1065);

        assertEquals("Mikey", padded.getName());
    }

    /**
     * Tests the smallest positive measurements.
     */
    public void testMinimumMeasurements()
    {
        Guest small = new Guest("A", 1, 1, 1065);

        assertEquals(1, small.getHeightInches());
        assertEquals(1, small.getWeightPounds());
    }

    /**
     * Tests a null name.
     */
    public void testNullName()
    {
        try
        {
            new Guest(null, 60, 150, 1065);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests empty and blank names.
     */
    public void testBlankName()
    {
        try
        {
            new Guest("", 60, 150, 1066);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            new Guest("   ", 60, 150, 1065);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests a zero height.
     */
    public void testZeroHeight()
    {
        try
        {
            new Guest("Ryan", 0, 150, 1065);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests a negative height.
     */
    public void testNegativeHeight()
    {
        try
        {
            new Guest("Ryan", -5, 150, 1065);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests a zero weight.
     */
    public void testZeroWeight()
    {
        try
        {
            new Guest("Ryan", 60, 0, 1065);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }

    /**
     * Tests a negative weight.
     */
    public void testNegativeWeight()
    {
        try
        {
            new Guest("Ryan", 60, -1, 1065);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }
}