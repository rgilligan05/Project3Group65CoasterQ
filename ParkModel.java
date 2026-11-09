import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Owns the park simulation: rides, the player, the clock, random arrivals,
 * abandonment, boarding, and statistics. Never reads input or prints output.
 *
 * @author CoasterQ team
 * @version 2026.11.09
 */
public class ParkModel
{
    //~ Fields ................................................................

    private enum PlayerState
    {
        NOT_REGISTERED, FREE, QUEUED, RIDING, CLOSED
    }

    private static final int OPEN_MINUTE = 600;
    private static final int CLOSE_MINUTE = 1200;
    private static final int STARTING_PASSES = 2;
    private static final int MAX_HEIGHT_INCHES = 96;
    private static final int MAX_WEIGHT_POUNDS = 700;
    private static final double ABANDON_CHANCE = 0.01;
    private static final double VIP_ARRIVAL_SHARE = 0.15;

    private final Coaster[] coasters;
    private final Random random;
    private final int[] rideCounts;

    private Guest player;
    private int currentMinute;
    private int fastPasses;
    private PlayerState playerState;
    private QueueEntry activeEntry;
    private int activeRideId;
    private boolean activeVip;
    private int activeWait;
    private int totalRides;
    private int totalWaitMinutes;
    private int lastWaitMinutes;
    private long nextVisitToken;

    //~ Constructors ..........................................................

    /**
     * Creates the park with its eight rides.
     *
     * @param seed seed for the random generator; any value is accepted
     */
    public ParkModel(long seed)
    {
        this(seed, createCoasters());
    }


    /**
     * Creates a park with supplied rides, for test fixtures.
     *
     * @param seed seed for the random generator
     * @param rides the rides, with IDs 1..n matching array positions
     * @throws IllegalArgumentException if rides is null, empty, or has a
     *         null element
     */
    ParkModel(long seed, Coaster[] rides)
    {
        if (rides == null || rides.length == 0)
        {
            throw new IllegalArgumentException("Rides are required.");
        }

        for (Coaster ride : rides)
        {
            if (ride == null)
            {
                throw new IllegalArgumentException("Rides are required.");
            }
        }

        coasters = rides.clone();
        rideCounts = new int[coasters.length];
        random = new Random(seed);
        currentMinute = OPEN_MINUTE;
        playerState = PlayerState.NOT_REGISTERED;
        nextVisitToken = 1;
    }

    //~Public  Methods ........................................................

    /**
     * Registers the player and grants two VIP passes.
     *
     * @param name the player's name
     * @param height height in inches
     * @param weight weight in pounds
     * @throws IllegalArgumentException if the name is null or blank, or a
     *         measurement is nonpositive or implausibly high
     * @throws IllegalStateException if a player is already registered
     */
    public void registerPlayer(String name, int height, int weight)
    {
        if (playerState != PlayerState.NOT_REGISTERED)
        {
            throw new IllegalStateException("Player is already registered.");
        }

        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("Name cannot be blank.");
        }

        if (height <= 0 || height > MAX_HEIGHT_INCHES)
        {
            throw new IllegalArgumentException("Height must be between 1 and "
                + MAX_HEIGHT_INCHES + " inches.");
        }

        if (weight <= 0 || weight > MAX_WEIGHT_POUNDS)
        {
            throw new IllegalArgumentException("Weight must be between 1 and "
                + MAX_WEIGHT_POUNDS + " pounds.");
        }

        player = new Guest(name, height, weight, player.getId());
        fastPasses = STARTING_PASSES;
        playerState = PlayerState.FREE;
    }


    /**
     * Adds the player to a ride's line.
     *
     * @param rideId the ride ID
     * @param vip true to join the VIP line
     * @throws IllegalArgumentException if the ride ID does not exist
     * @throws IllegalStateException if the player is unregistered, queued,
     *         riding, or the park is closed; the player is ineligible; the
     *         VIP line is unavailable or no passes remain; or the line is full
     */
    public void joinLine(int rideId, boolean vip)
    {
        if (rideId < 1 || rideId > coasters.length)
        {
            throw new IllegalArgumentException("Ride ID must be between 1 and "
                + coasters.length + ".");
        }

        switch (playerState)
        {
            case NOT_REGISTERED:
                throw new IllegalStateException(
                    "Register before joining a line.");
            case CLOSED:
                throw new IllegalStateException("The park is closed.");
            case QUEUED:
                throw new IllegalStateException(
                    "You are already in a line. Leave it first.");
            case RIDING:
                throw new IllegalStateException(
                    "You cannot join a line while riding.");
            default:
                break;
        }

        Coaster coaster = coasters[rideId - 1];

        if (!isEligible(coaster))
        {
            throw new IllegalStateException("You do not meet the requirements"
                + " for " + coaster.getName() + " (height "
                + coaster.getMinHeight() + "-" + coaster.getMaxHeight()
                + " in, max weight " + coaster.getMaxWeight() + " lb).");
        }

        if (vip && !coaster.hasVip())
        {
            throw new IllegalStateException(
                coaster.getName() + " has no VIP line.");
        }

        if (vip && fastPasses <= 0)
        {
            throw new IllegalStateException("You have no VIP passes left.");
        }

        if (coaster.isLineFull(vip))
        {
            throw new IllegalStateException("That line is full.");
        }

        QueueEntry entry = new QueueEntry(player, currentMinute,
            nextVisitToken++);

        if (vip)
        {
            coaster.getVipLine().enqueue(entry);
            coaster.getVipWaiting().add(entry);
            fastPasses--;
        }
        else
        {
            coaster.getStandardLine().enqueue(entry);
            coaster.getStandardWaiting().add(entry);
        }

        activeEntry = entry;
        activeRideId = rideId;
        activeVip = vip;
        playerState = PlayerState.QUEUED;
    }


    /**
     * Removes the player from the current line. The entry is marked
     * abandoned and skipped at boarding. Passes are not refunded.
     *
     * @throws IllegalStateException unless the player is queued
     */
    public void leaveLine()
    {
        if (playerState != PlayerState.QUEUED)
        {
            throw new IllegalStateException("You are not in a line.");
        }

        Coaster coaster = coasters[activeRideId - 1];
        activeEntry.abandon();

        if (activeVip)
        {
            coaster.getVipWaiting().remove(activeEntry);
        }
        else
        {
            coaster.getStandardWaiting().remove(activeEntry);
        }

        activeEntry = null;
        activeRideId = 0;
        activeVip = false;
        playerState = PlayerState.FREE;
    }


    /**
     * Advances the simulation one minute at a time, stopping at closing.
     *
     * @param minutes number of minutes to advance
     * @throws IllegalArgumentException if minutes is not positive
     * @throws IllegalStateException if unregistered or the park is closed
     */
    public void advanceTime(int minutes)
    {
        if (minutes <= 0)
        {
            throw new IllegalArgumentException(
                "Minutes must be positive.");
        }

        if (playerState == PlayerState.NOT_REGISTERED)
        {
            throw new IllegalStateException(
                "Register before advancing time.");
        }

        if (playerState == PlayerState.CLOSED)
        {
            throw new IllegalStateException("The park is closed.");
        }

        int steps = Math.min(minutes, CLOSE_MINUTE - currentMinute);

        for (int i = 0; i < steps; i++)
        {
            tick();
        }
    }


    /**
     * Returns a read-only copy of the current display values.
     *
     * @return the snapshot
     */
    public ParkSnapshot getSnapshot()
    {
        List<Integer> counts = new ArrayList<Integer>();
        for (int count : rideCounts)
        {
            counts.add(count);
        }

        List<ParkSnapshot.RideInfo> rides =
            new ArrayList<ParkSnapshot.RideInfo>();

        for (Coaster c : coasters)
        {
            boolean vip = c.hasVip();
            rides.add(new ParkSnapshot.RideInfo(
                c.getId(),
                c.getName(),
                isEligible(c),
                vip,
                c.getStandardWaiting().size(),
                vip ? c.getVipWaiting().size() : 0,
                estimateWait(c, false),
                vip ? estimateWait(c, true) : -1,
                c.isRunning()));
        }

        double average = totalRides == 0
            ? 0.0 : (double) totalWaitMinutes / totalRides;

        return new ParkSnapshot(currentMinute,
            player == null ? "" : player.getName(),
            playerState.name(), fastPasses, totalRides, lastWaitMinutes,
            average, counts, rides);
    }

    //~ Private helpers .......................................................

    /**
     * Advances the clock one minute and processes that minute's events.
     */
    private void tick()
    {
        currentMinute++;

        for (Coaster c : coasters)
        {
            if (c.isRunning() && currentMinute >= c.getCompletionMinute())
            {
                finishTrain(c);
            }
        }

        for (Coaster c : coasters)
        {
            addArrival(c);
            abandonRandomly(c.getStandardWaiting());
            if (c.hasVip())
            {
                abandonRandomly(c.getVipWaiting());
            }
        }

        if (currentMinute < CLOSE_MINUTE)
        {
            for (Coaster c : coasters)
            {
                if (!c.isRunning()
                    && currentMinute >= c.getNextDispatchMinute())
                {
                    dispatch(c);
                }
            }
        }
        else
        {
            for (Coaster c : coasters)
            {
                if (c.isRunning())
                {
                    finishTrain(c);
                }
            }

            playerState = PlayerState.CLOSED;
        }
    }


    /**
     * Possibly adds one random guest to a ride's lines.
     *
     * @param c the ride
     */
    private void addArrival(Coaster c)
    {
        if (random.nextDouble() >= c.getArrivalProbability())
        {
            return;
        }

        boolean vip = c.hasVip() && random.nextDouble() < VIP_ARRIVAL_SHARE;

        if (c.isLineFull(vip))
        {
            return;
        }

        Guest guest = new Guest("Guest", 65, 160, player.getId());
        QueueEntry entry = new QueueEntry(guest, currentMinute,
            nextVisitToken++);

        if (vip)
        {
            c.getVipLine().enqueue(entry);
            c.getVipWaiting().add(entry);
        }
        else
        {
            c.getStandardLine().enqueue(entry);
            c.getStandardWaiting().add(entry);
        }
    }


    /**
     * Marks some waiting guests as abandoned. The player never abandons
     * automatically.
     *
     * @param waiting the active entries of one line
     */
    private void abandonRandomly(List<QueueEntry> waiting)
    {
        Iterator<QueueEntry> iterator = waiting.iterator();

        while (iterator.hasNext())
        {
            QueueEntry entry = iterator.next();

            if (entry != activeEntry && random.nextDouble() < ABANDON_CHANCE)
            {
                entry.abandon();
                iterator.remove();
            }
        }
    }


    /**
     * Loads and dispatches a train. The VIP line fills at most half of
     * the seats; the regular line fills the rest.
     *
     * @param c the ride
     */
    private void dispatch(Coaster c)
    {
        List<QueueEntry> riders = c.getCurrentRiders();
        int vipBoarded = 0;

        if (c.hasVip())
        {
            vipBoarded = board(c.getVipLine(), c.getVipWaiting(),
                c.getSeatCapacity() / 2, riders);
        }

        board(c.getStandardLine(), c.getStandardWaiting(),
            c.getSeatCapacity() - vipBoarded, riders);

        c.setNextDispatchMinute(
            c.getNextDispatchMinute() + c.getDispatchInterval());

        if (riders.isEmpty())
        {
            return;
        }

        c.setCompletionMinute(currentMinute + c.getRideDuration());

        if (activeEntry != null && riders.contains(activeEntry))
        {
            activeWait = currentMinute - activeEntry.getJoinedMinute();
            playerState = PlayerState.RIDING;
        }
    }


    /**
     * Moves up to limit non-abandoned entries from a line onto a train,
     * discarding abandoned entries reached along the way.
     *
     * @param line the line to board from
     * @param waiting the line's active entries
     * @param limit maximum riders to board
     * @param riders the train's riders
     * @return the number boarded
     */
    private int board(LinkedQueue<QueueEntry> line,
        List<QueueEntry> waiting, int limit, List<QueueEntry> riders)
    {
        int boarded = 0;

        while (boarded < limit && !line.isEmpty())
        {
            QueueEntry entry = line.dequeue();

            if (!entry.isAbandoned())
            {
                waiting.remove(entry);
                riders.add(entry);
                boarded++;
            }
        }

        return boarded;
    }


    /**
     * Ends a ride, recording statistics if the player was aboard.
     *
     * @param c the ride
     */
    private void finishTrain(Coaster c)
    {
        if (playerState == PlayerState.RIDING && activeEntry != null
            && c.getCurrentRiders().contains(activeEntry))
        {
            totalRides++;
            totalWaitMinutes += activeWait;
            lastWaitMinutes = activeWait;
            rideCounts[activeRideId - 1]++;

            activeEntry = null;
            activeRideId = 0;
            activeVip = false;
            playerState = PlayerState.FREE;
        }

        c.getCurrentRiders().clear();
        c.setCompletionMinute(-1);
    }


    /**
     * Checks the player's height and weight against a ride's limits.
     * Before registration every ride counts as eligible.
     *
     * @param c the ride
     * @return true if the player may ride
     */
    private boolean isEligible(Coaster c)
    {
        if (player == null)
        {
            return true;
        }

        return player.getHeightInches() >= c.getMinHeight()
            && player.getHeightInches() <= c.getMaxHeight()
            && player.getWeightPounds() <= c.getMaxWeight();
    }


    /**
     * Estimates the minutes until a guest joining now would board.
     *
     * @param c the ride
     * @param vip true for the VIP line
     * @return minutes, or -1 if unavailable or after closing
     */
    private int estimateWait(Coaster c, boolean vip)
    {
        int seats;
        int ahead;

        if (vip)
        {
            if (!c.hasVip())
            {
                return -1;
            }

            seats = c.getSeatCapacity() / 2;
            ahead = c.getVipWaiting().size();
        }
        else
        {
            seats = c.getSeatCapacity();
            if (c.hasVip())
            {
                seats -= Math.min(c.getSeatCapacity() / 2,
                    c.getVipWaiting().size());
            }
            ahead = c.getStandardWaiting().size();
        }

        if (seats <= 0)
        {
            return -1;
        }

        int nextBoard = c.getNextDispatchMinute();
        if (c.isRunning())
        {
            nextBoard = Math.max(nextBoard, c.getCompletionMinute());
        }
        nextBoard = Math.max(nextBoard, currentMinute);

        int boardMinute = nextBoard
            + (ahead / seats) * c.getDispatchInterval();

        if (boardMinute >= CLOSE_MINUTE)
        {
            return -1;
        }

        return boardMinute - currentMinute;
    }


    /**
     * Builds the eight park rides.
     *
     * @return the rides, with IDs 1 through 8
     */
    private static Coaster[] createCoasters()
    {
        Coaster[] rides = new Coaster[] {
            // id, name, seats, minH, maxH, maxW, interval, duration,
            // arrival probability, VIP, line capacity
            new Coaster(1, "Steel Vengeance", 6, 52, 78, 300, 8, 3,
                0.90, true, 60),
            new Coaster(2, "Millennium Force", 6, 48, 78, 300, 8, 3,
                0.85, true, 60),
            new Coaster(3, "Magnum XL-200", 6, 48, 78, 300, 7, 3,
                0.80, true, 60),
            new Coaster(4, "Maverick", 5, 52, 78, 350, 6, 2,
                0.65, true, 45),
            new Coaster(5, "Gemini", 5, 48, 78, 350, 6, 2,
                0.60, false, 45),
            new Coaster(6, "Blue Streak", 5, 48, 78, 350, 6, 2,
                0.55, false, 45),
            new Coaster(7, "Cedar Creek Mine Ride", 4, 42, 84, 400, 5, 2,
                0.40, false, 30),
            new Coaster(8, "Woodstock Express", 4, 36, 84, 400, 5, 2,
                0.35, false, 30)
        };

        for (Coaster ride : rides)
        {
            ride.setNextDispatchMinute(
                OPEN_MINUTE + ride.getDispatchInterval());
        }

        return rides;
    }
}