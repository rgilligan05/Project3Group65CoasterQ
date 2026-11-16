/**
 * Immutable holder for a guest's name, height, and weight.
 * Contains no console behavior and no eligibility rules.
 *
 * @author CoasterQ team
 * @version 2026.11.09
 */
public class Guest
{
    //~ Fields ................................................................

    private final String name;
    private final int heightInches;
    private final int weightPounds;
    private final int id;

    //~ Constructors ..........................................................

    /**
     * Creates a guest.
     *
     * @param name the guest's name
     * @param heightInches height in inches
     * @param weightPounds weight in pounds
     * @param id ID for the guest
     * @throws IllegalArgumentException if the name is null or blank, or
     *         either measurement is not positive
     */
    public Guest(String name, int heightInches, int weightPounds, int id)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("Name cannot be blank.");
        }

        if (heightInches <= 0 || weightPounds <= 0)
        {
            throw new IllegalArgumentException(
                "Height and weight must be positive.");
        }

        this.name = name.trim();
        this.heightInches = heightInches;
        this.weightPounds = weightPounds;
        this.id = id;
    }

    //~Public  Methods ........................................................

    /**
     * Returns the guest's name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the guest's height.
     *
     * @return height in inches
     */
    public int getHeightInches()
    {
        return heightInches;
    }
    public int getId()
    {
        return id;
    }

    /**
     * Returns the guest's weight.
     *
     * @return weight in pounds
     */
    public int getWeightPounds()
    {
        return weightPounds;
    }
}