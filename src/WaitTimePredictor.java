/**
 * Predicts wait times using observed queue lengths
 * and actual boarding wait times.
 */
public class WaitTimePredictor {
    public static final double DECAY = 0.98;
    public static final double MIN_SAMPLES = 20.0;
    private static final double MIN_X_VARIANCE = 1.0;
    
    
    private double weight;
    private double count;
    private double sumX;
    private double sumY;
    private double sumXX;
    private double sumXY;

    /**
     * Records one completed wait.
     * @param peopleAhead number of people ahead in queue.
     * @param actualWait the actual wait time
     */
    public void record(int peopleAhead, int actualWait) {
        if (peopleAhead < 0 || actualWait < 0) {
            throw new IllegalArgumentException(
                "Queue length and wait must be nonnegative.");
        }

        count = count * DECAY + 1;
        sumX = sumX * DECAY + peopleAhead;
        sumY = sumY * DECAY + actualWait;
        sumXX = sumXX * DECAY + (double) peopleAhead * peopleAhead;
        sumXY = sumXY * DECAY + (double) peopleAhead * actualWait;
    }

    /**
     * Returns a predicted wait time. Uses the existing
     * estimate if there is insufficient regression data
     * @param peopleAhead 
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
 
        if (count < MIN_SAMPLES) {
            return fallback;
        }
 
        double meanX = sumX / count;
        double meanY = sumY / count;
        double varX = sumXX / count - meanX * meanX;
 
        // Queue lengths are all nearly the same, so no slope can be fitted.
        if (varX < MIN_X_VARIANCE) {
            return fallback;
        }
 
        double slope = (sumXY / count - meanX * meanY) / varX;
 
        // More people ahead should never mean a shorter wait.
        if (slope <= 0) {
            return fallback;
        }
 
        double intercept = meanY - slope * meanX;
        double prediction = intercept + slope * peopleAhead;
 
        return (int) Math.max(0, Math.round(prediction));
    }
}
