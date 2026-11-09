import java.util.Scanner;

/**
 * Starts the CoasterQ console application.
 *
 * @author Eyal
 * @version 2026.11.09
 */
public class Main
{
    /**
     * Creates the model, view, and controller and starts the program.
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        ParkModel model = new ParkModel(System.currentTimeMillis());

        try (Scanner input = new Scanner(System.in))
        {
            ConsoleView view = new ConsoleView(input, System.out);
            ParkController controller = new ParkController(model, view);
            controller.run();
        }
    }
}
