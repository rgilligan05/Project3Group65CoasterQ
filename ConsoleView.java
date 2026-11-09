import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;

/**
 * Reads console input and displays park information.
 *
 * @author Eyal
 * @version 2026.11.09
 */
public class ConsoleView
{
    private final Scanner input;
    private final PrintStream output;

    /**
     * Creates a console view.
     *
     * @param input source of player input
     * @param output destination for console text
     */
    public ConsoleView(Scanner input, PrintStream output)
    {
        if (input == null || output == null)
        {
            throw new IllegalArgumentException("Input and output are required.");
        }

        this.input = input;
        this.output = output;
    }


    /**
     * Prompts for and reads one line of input.
     *
     * @param prompt text displayed before reading
     * @return the raw input line, or null at end of input
     */
    public String readLine(String prompt)
    {
        output.print(prompt);
        output.flush();

        if (input.hasNextLine())
        {
            return input.nextLine();
        }

        return null;
    }


    /**
     * Displays the available commands.
     */
    public void displayMenu()
    {
        output.println();
        output.println("join <ride ID>       Join the regular line");
        output.println("join <ride ID> vip   Join the VIP line");
        output.println("leave                Leave your current line");
        output.println("advance              Advance time by five minutes");
        output.println("status               View park and player stats");
        output.println("quit                 Exit CoasterQ");
    }


    /**
     * Displays a message to the player.
     *
     * @param message the text to display
     */
    public void displayMessage(String message)
    {
        output.println(message);
    }


    /**
     * Displays the current park and player information.
     *
     * @param snapshot the model's read-only park information
     */
    public void displayPark(ParkSnapshot snapshot)
    {
        output.println();
        output.println("=== CoasterQ | " + formatTime(snapshot.currentMinute)
            + " ===");

        if (snapshot.playerName != null && !snapshot.playerName.isEmpty())
        {
            output.println("Player: " + snapshot.playerName
                + " | State: " + snapshot.playerState
                + " | VIP passes: " + snapshot.fastPasses);

            output.printf(Locale.US,
                "Completed rides: %d | Last wait: %d min"
                    + " | Average wait: %.1f min%n",
                snapshot.totalRides, snapshot.lastWaitMinutes,
                snapshot.averageWaitMinutes);
        }

        for (ParkSnapshot.RideInfo ride : snapshot.rides)
        {
            StringBuilder line = new StringBuilder();
            line.append(ride.id).append(". ").append(ride.name);
            line.append(" | Regular: ").append(ride.standardCount);
            line.append(" in line, ");
            line.append(formatWait(ride.standardWait));

            if (ride.hasVip)
            {
                line.append(" | VIP: ").append(ride.vipCount);
                line.append(" in line, ");
                line.append(formatWait(ride.vipWait));
            }

            if (ride.running)
            {
                line.append(" | Train running");
            }

            if (!ride.eligible)
            {
                line.append(" | Not eligible");
            }

            int index = ride.id - 1;
            if (index >= 0 && index < snapshot.rideCounts.size())
            {
                line.append(" | Ridden: ");
                line.append(snapshot.rideCounts.get(index));
            }

            output.println(line);
        }
    }


    /**
     * Formats minutes since midnight as a clock time.
     *
     * @param minute minutes since midnight
     * @return formatted time
     */
    private String formatTime(int minute)
    {
        int hour = minute / 60;
        int displayHour = hour % 12;

        if (displayHour == 0)
        {
            displayHour = 12;
        }

        String period = hour < 12 ? "AM" : "PM";
        return String.format(Locale.US, "%d:%02d %s",
            displayHour, minute % 60, period);
    }


    /**
     * Formats a wait estimate for display.
     *
     * @param wait the model's wait estimate
     * @return formatted estimate
     */
    private String formatWait(int wait)
    {
        if (wait < 0)
        {
            return "wait unavailable";
        }

        return wait + " min wait";
    }
}
