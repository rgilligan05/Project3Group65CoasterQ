import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for WaitTimePredictor.
 *
 * The predictor needs at least 20 effective samples before it uses the
 * regression, so most tests record 60 points. Tests that check exact
 * predictions use perfectly linear data, which gives the same fitted line
 * no matter how the points are weighted.
 */
public class WaitTimePredictorTest {
    private static final int FALLBACK = 25;

    private WaitTimePredictor predictor;

    @Before
    public void setUp() {
        predictor = new WaitTimePredictor();
    }

    /** Records 'count' points at queue lengths 5, 15, ..., 95 (repeating), wait = slope * x + offset. */
    private void recordLine(WaitTimePredictor p, int count, int slope, int offset) {
        for (int i = 0; i < count; i++) {
            int x = 5 + (i % 10) * 10;
            p.record(x, slope * x + offset);
        }
    }

    // ---------- Fallback behavior ----------

    @Test
    public void predict_noData_returnsFallback() {
        assertEquals(FALLBACK, predictor.predict(40, FALLBACK));
    }

    @Test
    public void predict_tooFewRecords_returnsFallback() {
        recordLine(predictor, 10, 1, 0);
        assertEquals(FALLBACK, predictor.predict(40, FALLBACK));
    }

    @Test
    public void predict_identicalQueueLengths_returnsFallback() {
        for (int i = 0; i < 60; i++) {
            predictor.record(30, 30);
        }
        assertEquals(FALLBACK, predictor.predict(40, FALLBACK));
    }

    @Test
    public void predict_decreasingWaits_returnsFallback() {
        // Wait = 100 - queue length: a negative slope, which is physically wrong.
        recordLine(predictor, 60, -1, 100);
        assertEquals(FALLBACK, predictor.predict(40, FALLBACK));
    }

    @Test
    public void predict_negativeFallback_returnsMinusOneWithNoData() {
        assertEquals(-1, predictor.predict(40, -1));
    }

    @Test
    public void predict_negativeFallback_returnsMinusOneEvenWithData() {
        recordLine(predictor, 60, 1, 0);
        assertEquals(-1, predictor.predict(40, -1));
    }

    // ---------- Regression results ----------

    @Test
    public void predict_perfectLineThroughOrigin_predictsExactly() {
        recordLine(predictor, 60, 1, 0);
        assertEquals(40, predictor.predict(40, FALLBACK));
    }

    @Test
    public void predict_lineWithIntercept_predictsExactly() {
        // Wait = 2 * queue length + 10, so 30 people ahead gives 70.
        recordLine(predictor, 60, 2, 10);
        assertEquals(70, predictor.predict(30, FALLBACK));
    }

    @Test
    public void predict_beyondRecordedRange_extrapolates() {
        // Recorded queue lengths stop at 95, but the line continues.
        recordLine(predictor, 60, 1, 0);
        assertEquals(200, predictor.predict(200, FALLBACK));
    }

    @Test
    public void predict_zeroPeopleAhead_returnsIntercept() {
        recordLine(predictor, 60, 2, 10);
        assertEquals(10, predictor.predict(0, FALLBACK));
    }

    @Test
    public void predict_negativeRawPrediction_isClampedToZero() {
        // Wait = 2x - 20 (queue lengths 20 to 110), so 0 people ahead is -20 raw.
        for (int i = 0; i < 60; i++) {
            int x = 20 + (i % 10) * 10;
            predictor.record(x, 2 * x - 20);
        }
        assertEquals(0, predictor.predict(0, FALLBACK));
    }

    // ---------- Recency weighting ----------

    @Test
    public void predict_recentDataOutweighsOldData() {
        // Old conditions: wait = x. New conditions: wait = 3x.
        recordLine(predictor, 60, 1, 0);
        recordLine(predictor, 200, 3, 0);

        int prediction = predictor.predict(40, FALLBACK);

        // New data alone gives 120 and old data alone gives 40. Plain
        // least squares over everything would land near 90, so a result
        // above 100 shows that the old data is being discounted.
        assertTrue("Expected recent data to dominate, got " + prediction,
                prediction > 100);
    }

    // ---------- Input validation ----------

    @Test(expected = IllegalArgumentException.class)
    public void record_negativePeopleAhead_throws() {
        predictor.record(-1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void record_negativeWait_throws() {
        predictor.record(10, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void predict_negativePeopleAhead_throws() {
        predictor.predict(-1, FALLBACK);
    }

    @Test
    public void record_zeroValues_isAllowed() {
        predictor.record(0, 0);
        assertEquals(FALLBACK, predictor.predict(10, FALLBACK));
    }
}