/**
 * Predicts wait times using observed queue lengths
 * and actual boarding wait times.
 */
public class WaitTimePredictor {
    private int count;
    private double sumX;
    private double sumY;
    private double sumXX;
    private double sumXY;

    /**
     * Records one completed wait.
     */
    public void record(int peopleAhead, int actualWait) {
        if (peopleAhead < 0 || actualWait < 0) {
            throw new IllegalArgumentException(
                "Queue length and wait must be nonnegative.");
        }

        count++;
        sumX += peopleAhead;
        sumY += actualWait;
        sumXX += (double) peopleAhead * peopleAhead;
        sumXY += (double) peopleAhead * actualWait;
    }

    /**
     * Returns a predicted wait time. Uses the existing
     * estimate if there is insufficient regression data
     */
    public int predict(int peopleAhead, int fallback) {
        if (peopleAhead < 0) {
            throw new IllegalArgumentException(
                "Queue length cannot be negative.");
        }

        // -1 means no wait estimate is available.
        if (fallback < 0) {
            return -1;
        }

        if (count < 2) {
            return fallback;
        }

        double denominator = count * sumXX - sumX * sumX;

        if (denominator == 0) {
            return fallback;
        }

        double slope =
            (count * sumXY - sumX * sumY) / denominator;

        double intercept = (sumY - slope * sumX) / count;

        double prediction = intercept + slope * peopleAhead;

        return (int) Math.max(0, Math.round(prediction));
    }
}
