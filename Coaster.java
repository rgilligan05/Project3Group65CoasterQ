import java.util.ArrayList;
import java.util.List;

/**
 * State holder for one ride: fixed configuration, its lines, and its
 * current train. Only ParkModel changes this state.
 *
 * @author mlkuhl
 * @version 2026.11.09
 */
public class Coaster
{
    private final int id;
    private final String name;
    private final int seatCapacity;
    private final int minHeight;
    private final int maxHeight;
    private final int maxWeight;
    private final int dispatchInterval;
    private final int rideDuration;
    private final double arrivalProbability;
    private final boolean hasVip;
    private final int standardCapacity;
    private final int vipCapacity;

    private final LinkedQueue<QueueEntry> standardLine;
    private final LinkedQueue<QueueEntry> vipLine;

    private final List<QueueEntry> standardWaiting;
    private final List<QueueEntry> vipWaiting;

    private final List<QueueEntry> currentRiders;
    private int nextDispatchMinute;
    private int completionMinute;

    /**
     * Creates a coaster with empty lines and no train running.
     *
     * @param id ride ID
     * @param name ride name
     * @param seatCapacity seats per train
     * @param minHeight minimum height in inches (inclusive)
     * @param maxHeight maximum height in inches (inclusive)
     * @param maxWeight maximum weight in pounds (inclusive)
     * @param dispatchInterval minutes between dispatches
     * @param rideDuration minutes a train is running
     * @param arrivalProbability chance per minute of a guest arriving
     * @param hasVip whether the ride has a VIP line
     * @param queueCapacity capacity of the regular line
     * @throws IllegalArgumentException if any value is invalid
     */
    Coaster(int id, String name, int seatCapacity, int minHeight,
        int maxHeight, int maxWeight, int dispatchInterval, int rideDuration,
        double arrivalProbability, boolean hasVip, int queueCapacity)
    {
        if (name == null || name.trim().isEmpty() || seatCapacity <= 0
            || minHeight <= 0 || maxHeight < minHeight || maxWeight <= 0
            || dispatchInterval <= 0 || rideDuration <= 0
            || rideDuration > dispatchInterval
            || arrivalProbability < 0.0 || arrivalProbability > 1.0
            || queueCapacity <= 0)
        {
            throw new IllegalArgumentException(
                "Invalid coaster configuration.");
        }

        this.id = id;
        this.name = name;
        this.seatCapacity = seatCapacity;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.maxWeight = maxWeight;
        this.dispatchInterval = dispatchInterval;
        this.rideDuration = rideDuration;
        this.arrivalProbability = arrivalProbability;
        this.hasVip = hasVip;
        standardCapacity = queueCapacity;
        vipCapacity = Math.max(1, queueCapacity / 2);

        standardLine = new LinkedQueue<QueueEntry>(standardCapacity);
        standardWaiting = new ArrayList<QueueEntry>();
        currentRiders = new ArrayList<QueueEntry>();

        if (hasVip)
        {
            vipLine = new LinkedQueue<QueueEntry>(vipCapacity);
            vipWaiting = new ArrayList<QueueEntry>();
        }
        else
        {
            vipLine = null;
            vipWaiting = null;
        }

        nextDispatchMinute = 0;
        completionMinute = -1;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getSeatCapacity()
    {
        return seatCapacity;
    }

    public int getMinHeight()
    {
        return minHeight;
    }

    public int getMaxHeight()
    {
        return maxHeight;
    }

    public int getMaxWeight()
    {
        return maxWeight;
    }

    public int getDispatchInterval()
    {
        return dispatchInterval;
    }

    public int getRideDuration()
    {
        return rideDuration;
    }

    public double getArrivalProbability()
    {
        return arrivalProbability;
    }

    public boolean hasVip()
    {
        return hasVip;
    }

    public LinkedQueue<QueueEntry> getStandardLine()
    {
        return standardLine;
    }

    /**
     * Returns the VIP line.
     *
     * @return the VIP line, or null if this ride has none
     */
    public LinkedQueue<QueueEntry> getVipLine()
    {
        return vipLine;
    }

    public List<QueueEntry> getStandardWaiting()
    {
        return standardWaiting;
    }

    /**
     * Returns the active VIP entries.
     *
     * @return the list, or null if this ride has no VIP line
     */
    public List<QueueEntry> getVipWaiting()
    {
        return vipWaiting;
    }

    public List<QueueEntry> getCurrentRiders()
    {
        return currentRiders;
    }

    public int getNextDispatchMinute()
    {
        return nextDispatchMinute;
    }

    public void setNextDispatchMinute(int minute)
    {
        nextDispatchMinute = minute;
    }

    public int getCompletionMinute()
    {
        return completionMinute;
    }

    public void setCompletionMinute(int minute)
    {
        completionMinute = minute;
    }

    /**
     * Checks whether a train is running.
     *
     * @return true if a train has been dispatched and not yet finished
     */
    public boolean isRunning()
    {
        return completionMinute >= 0;
    }

    /**
     * Checks whether a line has reached capacity. Abandoned entries still
     * stored in the queue count toward capacity.
     *
     * @param vip true for the VIP line
     * @return true if the line is full
     */
    public boolean isLineFull(boolean vip)
    {
        if (vip)
        {
            return vipLine == null || vipLine.size() >= vipCapacity;
        }

        return standardLine.size() >= standardCapacity;
    }
}