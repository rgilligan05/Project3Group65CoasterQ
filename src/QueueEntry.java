/**
 * Stores information about one guest's visit to a queue.
 *
 * @author Ryan Gilligan
 * @version 2026.11.08
 */
class QueueEntry
{
    private final Guest guest;
    private final int joinedMinute;
    private final long visitToken;
    private boolean abandoned;

    /**
     * Creates a queue entry.
     *
     * @param guest guest joining the queue
     * @param joinedMinute simulation minute of joining
     * @param visitToken unique token supplied by the model
     * @throws IllegalArgumentException if guest is null
     *         or joinedMinute is negative
     */
    QueueEntry(Guest guest, int joinedMinute, long visitToken)
    {
        if (guest == null)
        {
            throw new IllegalArgumentException(
                "Guest cannot be null.");
        }

        if (joinedMinute < 0)
        {
            throw new IllegalArgumentException(
                "Join time cannot be negative.");
        }

        this.guest = guest;
        this.joinedMinute = joinedMinute;
        this.visitToken = visitToken;
        abandoned = false;
    }

    /**
     * Returns the guest.
     *
     * @return guest associated with this visit
     */
    Guest getGuest()
    {
        return guest;
    }

    /**
     * Returns the join time.
     *
     * @return simulation minute of joining
     */
    int getJoinedMinute()
    {
        return joinedMinute;
    }

    /**
     * Returns the visit token.
     *
     * @return token identifying this visit
     */
    long getVisitToken()
    {
        return visitToken;
    }

    /**
     * Checks whether this visit was abandoned.
     *
     * @return true if abandoned
     */
    boolean isAbandoned()
    {
        return abandoned;
    }

    /**
     * Marks this visit as abandoned.
     */
    void abandon()
    {
        abandoned = true;
    }
}
