import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable, read-only copy of the park's display values.
 * Exposes plain data only, never model objects.
 *
 * @author CoasterQ team
 * @version 2026.11.09
 */
public final class ParkSnapshot
{
    /** Minutes since midnight. */
    public final int currentMinute;

    /** The player's name, or an empty string before registration. */
    public final String playerName;

    /** The player's state, such as FREE or QUEUED. */
    public final String playerState;

    /** Remaining VIP passes. */
    public final int fastPasses;

    /** Completed rides. */
    public final int totalRides;

    /** Wait of the most recent completed ride, or 0 if none. */
    public final int lastWaitMinutes;

    /** Average wait over completed rides, or 0.0 if none. */
    public final double averageWaitMinutes;

    /** Completed-ride count per ride; index 0 is ride 1. */
    public final List<Integer> rideCounts;

    /** Display values for each ride, in ride ID order. */
    public final List<RideInfo> rides;

    /**
     * Creates a snapshot. Lists are copied and made unmodifiable.
     *
     * @param currentMinute minutes since midnight
     * @param playerName player name, or null if not registered
     * @param playerState player state name
     * @param fastPasses remaining VIP passes
     * @param totalRides completed rides
     * @param lastWaitMinutes last recorded wait
     * @param averageWaitMinutes average recorded wait
     * @param rideCounts completed rides per coaster
     * @param rides display values per coaster
     */
    ParkSnapshot(int currentMinute, String playerName, String playerState,
        int fastPasses, int totalRides, int lastWaitMinutes,
        double averageWaitMinutes, List<Integer> rideCounts,
        List<RideInfo> rides)
    {
        this.currentMinute = currentMinute;
        this.playerName = playerName == null ? "" : playerName;
        this.playerState = playerState;
        this.fastPasses = fastPasses;
        this.totalRides = totalRides;
        this.lastWaitMinutes = lastWaitMinutes;
        this.averageWaitMinutes = averageWaitMinutes;
        this.rideCounts = Collections.unmodifiableList(
            new ArrayList<Integer>(rideCounts));
        this.rides = Collections.unmodifiableList(
            new ArrayList<RideInfo>(rides));
    }


    /**
     * Immutable display values for one ride.
     */
    public static final class RideInfo
    {
        /** Ride ID, starting at 1. */
        public final int id;

        /** Ride name. */
        public final String name;

        /** Whether the player meets the ride's restrictions. */
        public final boolean eligible;

        /** Whether the ride has a VIP line. */
        public final boolean hasVip;

        /** Guests waiting in the regular line (abandoned excluded). */
        public final int standardCount;

        /** Guests waiting in the VIP line (abandoned excluded). */
        public final int vipCount;

        /** Estimated regular wait in minutes, or -1 if unavailable. */
        public final int standardWait;

        /** Estimated VIP wait in minutes, or -1 if unavailable. */
        public final int vipWait;

        /** Whether a train is currently running. */
        public final boolean running;

        /**
         * Creates ride display values.
         *
         * @param id ride ID
         * @param name ride name
         * @param eligible whether the player may ride
         * @param hasVip whether a VIP line exists
         * @param standardCount regular line size
         * @param vipCount VIP line size
         * @param standardWait regular wait estimate
         * @param vipWait VIP wait estimate
         * @param running whether a train is running
         */
        RideInfo(int id, String name, boolean eligible, boolean hasVip,
            int standardCount, int vipCount, int standardWait, int vipWait,
            boolean running)
        {
            this.id = id;
            this.name = name;
            this.eligible = eligible;
            this.hasVip = hasVip;
            this.standardCount = standardCount;
            this.vipCount = vipCount;
            this.standardWait = standardWait;
            this.vipWait = vipWait;
            this.running = running;
        }
    }
}