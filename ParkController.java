import java.util.Locale;

/**
 * Connects console commands to the CoasterQ model.
 *
 * @author Eyal
 * @version 2026.11.09
 */
public class ParkController
{
    private static final int STEP_MINUTES = 5;

    private final ParkModel model;
    private final ConsoleView view;
    private boolean running;

    /**
     * Creates a controller with the supplied model and view.
     *
     * @param model park simulation
     * @param view console interface
     * @throws IllegalArgumentException if either dependency is null
     */
    public ParkController(ParkModel model, ConsoleView view)
    {
        if (model == null || view == null)
        {
            throw new IllegalArgumentException("Model and view are required.");
        }

        this.model = model;
        this.view = view;
        running = true;
    }


    /**
     * Runs registration and accepts commands until the session ends.
     */
    public void run()
    {
        view.displayMessage("Welcome to CoasterQ!");

        ParkSnapshot snapshot = model.getSnapshot();

        if ("NOT_REGISTERED".equalsIgnoreCase(snapshot.playerState))
        {
            if (!registerPlayer())
            {
                return;
            }

            snapshot = model.getSnapshot();
        }

        view.displayPark(snapshot);
        stopIfClosed(snapshot);

        while (running)
        {
            view.displayMenu();
            String command = view.readLine("Command: ");

            if (command == null)
            {
                running = false;
            }
            else
            {
                handleCommand(command);
            }
        }
    }


    /**
     * Processes a single console command.
     *
     * @param command raw command text
     */
    public void handleCommand(String command)
    {
        if (!running)
        {
            return;
        }

        if (command == null || command.trim().isEmpty())
        {
            view.displayMessage("Enter a command.");
            view.displayPark(model.getSnapshot());
            return;
        }

        String[] parts = command.trim().split("\\s+");
        String action = parts[0].toLowerCase(Locale.ROOT);

        try
        {
            switch (action)
            {
                case "join":
                    joinLine(parts);
                    break;

                case "leave":
                    requireNoArguments(parts);
                    model.leaveLine();
                    view.displayMessage("You left the line.");
                    break;

                case "advance":
                    requireNoArguments(parts);
                    model.advanceTime(STEP_MINUTES);
                    break;

                case "status":
                    requireNoArguments(parts);
                    break;

                case "quit":
                    requireNoArguments(parts);
                    running = false;
                    view.displayMessage("Thanks for visiting CoasterQ!");
                    return;

                default:
                    throw new IllegalArgumentException(
                        "Unknown command. Choose an option from the menu.");
            }
        }
        catch (IllegalArgumentException | IllegalStateException exception)
        {
            view.displayMessage(exception.getMessage());
        }

        ParkSnapshot snapshot = model.getSnapshot();
        view.displayPark(snapshot);
        stopIfClosed(snapshot);
    }


    /**
     * Prompts for a player profile until valid input or end of input.
     *
     * @return true if registration succeeded
     */
    private boolean registerPlayer()
    {
        while (running)
        {
            String name = view.readLine("Name: ");
            if (name == null)
            {
                break;
            }

            String height = view.readLine("Height in inches: ");
            if (height == null)
            {
                break;
            }

            String weight = view.readLine("Weight in pounds: ");
            if (weight == null)
            {
                break;
            }

            try
            {
                model.registerPlayer(name.trim(),
                    parseWholeNumber(height, "Height"),
                    parseWholeNumber(weight, "Weight"));
                return true;
            }
            catch (IllegalArgumentException exception)
            {
                view.displayMessage(exception.getMessage());
            }
            catch (IllegalStateException exception)
            {
                view.displayMessage(exception.getMessage());
                running = false;
            }
        }

        running = false;
        return false;
    }


    /**
     * Parses the requested ride and line before calling the model.
     *
     * @param parts the command and its arguments
     */
    private void joinLine(String[] parts)
    {
        if (parts.length < 2 || parts.length > 3
            || (parts.length == 3 && !"vip".equalsIgnoreCase(parts[2])))
        {
            throw new IllegalArgumentException(
                "Use join <ride ID> or join <ride ID> vip.");
        }

        int rideId = parseWholeNumber(parts[1], "Ride ID");
        boolean vip = parts.length == 3;
        model.joinLine(rideId, vip);
        view.displayMessage("You joined the line for ride " + rideId + ".");
    }


    /**
     * Rejects extra input for a command that takes no arguments.
     *
     * @param parts the command and its arguments
     */
    private void requireNoArguments(String[] parts)
    {
        if (parts.length != 1)
        {
            throw new IllegalArgumentException(
                "Use " + parts[0] + " by itself.");
        }
    }


    /**
     * Converts text to an integer with a field-specific error.
     *
     * @param text the text to convert
     * @param label the field name shown in an error
     * @return the parsed integer
     */
    private int parseWholeNumber(String text, String label)
    {
        try
        {
            return Integer.parseInt(text.trim());
        }
        catch (NumberFormatException exception)
        {
            throw new IllegalArgumentException(
                label + " must be a whole number.");
        }
    }


    /**
     * Ends the session when the model reports the park has closed.
     *
     * @param snapshot the current park information
     */
    private void stopIfClosed(ParkSnapshot snapshot)
    {
        if ("CLOSED".equalsIgnoreCase(snapshot.playerState))
        {
            running = false;
            view.displayMessage("The park is closed. Thanks for visiting!");
        }
    }
}
