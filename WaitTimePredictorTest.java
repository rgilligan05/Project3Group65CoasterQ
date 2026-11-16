import student.TestCase;

/**
 * Tests regression-based wait time predictions.
 *
 * @author Ryan Gilligan
 * @version 2026.11.16
 */
public class WaitTimePredictorTest extends TestCase
{
    private WaitTimePredictor predictor;

    /**
     * Creates a fresh predictor before each test.
     */
    public void setUp()
    {
        predictor = new WaitTimePredictor();
    }


    /**
     * Tests using the fallback when no observations exist.
     */
    public void testNoData()
    {
        assertEquals(10, predictor.predict(3, 10));
    }


    /**
     * Tests using the fallback with only one observation.
     */
    public void testOneObservation()
    {
        predictor.record(2, 7);

        assertEquals(10, predictor.predict(3, 10));
    }


    /**
     * Tests predicting wait times using linear regression.
     */
    public void testRegressionPrediction()
    {
        predictor.record(0, 5);
        predictor.record(2, 11);

        assertEquals(8, predictor.predict(1, 10));
    }


    /**
     * Tests using the fallback when queue lengths are identical.
     */
    public void testIdenticalQueueLengths()
    {
        predictor.record(2, 5);
        predictor.record(2, 10);

        assertEquals(7, predictor.predict(3, 7));
    }


    /**
     * Tests rejection of a negative queue length.
     */
    public void testNegativeQueueLength()
    {
        try
        {
            predictor.predict(-1, 10);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }


    /**
     * Tests rejection of a negative recorded queue length.
     */
    public void testNegativeRecordedQueueLength()
    {
        try
        {
            predictor.record(-1, 5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }


    /**
     * Tests rejection of a negative recorded wait.
     */
    public void testNegativeRecordedWait()
    {
        try
        {
            predictor.record(3, -5);
            fail("Expected IllegalArgumentException.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }


    /**
     * Tests unavailable wait estimates.
     */
    public void testUnavailablePrediction()
    {
        assertEquals(-1, predictor.predict(3, -1));
    }


    /**
     * Tests that predicted wait times cannot be negative.
     */
    public void testNonnegativePrediction()
    {
        predictor.record(0, 10);
        predictor.record(2, 2);

        assertTrue(predictor.predict(5, 10) >= 0);
    }
}
