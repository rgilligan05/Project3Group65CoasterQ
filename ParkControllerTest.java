import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import student.TestCase;

/**
 * Tests console input handling and its effect on the park model.
 *
 * @author Eyal
 * @version 2026.11.09
 */
public class ParkControllerTest extends TestCase
{
    private ParkModel model;
    private ByteArrayOutputStream output;

    /**
     * Creates a fresh park and output buffer before each test.
     */
    @Override
    public void setUp()
    {
        model = new ParkModel(65L);
        output = new ByteArrayOutputStream();
    }


    /**
     * Tests registration, time advancement, and quitting.
     */
    public void testRegistrationAdvanceAndQuit()
    {
        ParkController controller = makeController(
            "Eyal Cohen\n60\n150\nadvance\nquit\nadvance\n");

        controller.run();

        assertEquals("Eyal Cohen", model.getSnapshot().playerName);
        assertEquals(605, model.getSnapshot().currentMinute);
        assertTrue(output.toString().contains("Thanks for visiting"));
    }


    /**
     * Tests retrying registration after an invalid number.
     */
    public void testRegistrationRetriesInvalidHeight()
    {
        ParkController controller = makeController(
            "Eyal\ntall\n150\nEyal\n60\n150\nquit\n");

        controller.run();

        assertTrue(output.toString().contains("Height must be a whole number"));
        assertEquals("Eyal", model.getSnapshot().playerName);
        assertEquals(600, model.getSnapshot().currentMinute);
    }


    /**
     * Tests end of input before registration is complete.
     */
    public void testEndOfInputDuringRegistration()
    {
        ParkController controller = makeController("Eyal\n60\n");

        controller.run();

        assertTrue("NOT_REGISTERED".equalsIgnoreCase(
            model.getSnapshot().playerState));
        assertEquals(600, model.getSnapshot().currentMinute);
    }


    /**
     * Tests end of input after registration.
     */
    public void testEndOfInputAtMenu()
    {
        ParkController controller = makeController("Eyal\n60\n150\n");

        controller.run();

        assertEquals("Eyal", model.getSnapshot().playerName);
        assertEquals(600, model.getSnapshot().currentMinute);
    }


    /**
     * Tests that empty commands are rejected.
     */
    public void testBlankAndNullCommands()
    {
        model.registerPlayer("Eyal", 60, 150);
        ParkController controller = makeController("");

        controller.handleCommand("   ");
        controller.handleCommand(null);

        assertTrue(output.toString().contains("Enter a command"));
        assertEquals(600, model.getSnapshot().currentMinute);
    }


    /**
     * Tests that an unknown command leaves the model unchanged.
     */
    public void testUnknownCommand()
    {
        model.registerPlayer("Eyal", 60, 150);
        ParkController controller = makeController("");

        controller.handleCommand("teleport");

        assertTrue(output.toString().contains("Unknown command"));
        assertEquals(600, model.getSnapshot().currentMinute);
    }


    /**
     * Tests rejection of a nonnumeric ride ID.
     */
    public void testNonNumericRideId()
    {
        model.registerPlayer("Eyal", 60, 150);
        ParkController controller = makeController("");

        controller.handleCommand("join coaster");

        assertTrue(output.toString().contains("Ride ID must be a whole number"));
        assertTrue("FREE".equalsIgnoreCase(model.getSnapshot().playerState));
        assertEquals(2, model.getSnapshot().fastPasses);
    }


    /**
     * Tests rejection of extra arguments on a time command.
     */
    public void testAdvanceWithExtraArgument()
    {
        model.registerPlayer("Eyal", 60, 150);
        ParkController controller = makeController("");

        controller.handleCommand("advance 10");

        assertTrue(output.toString().contains("Use advance by itself"));
        assertEquals(600, model.getSnapshot().currentMinute);
    }


    /**
     * Tests handling an invalid join format.
     */
    public void testInvalidJoinFormat()
    {
        model.registerPlayer("Eyal", 60, 150);
        ParkController controller = makeController("");

        controller.handleCommand("join 3 fast");

        assertTrue(output.toString().contains("Use join <ride ID>"));
        assertTrue("FREE".equalsIgnoreCase(model.getSnapshot().playerState));
        assertEquals(2, model.getSnapshot().fastPasses);
    }


    /**
     * Tests that the controller handles a model rejection and keeps running.
     */
    public void testModelRejectionAllowsNextCommand()
    {
        ParkController controller = makeController(
            "Eyal\n60\n150\njoin 99\nadvance\nquit\n");

        controller.run();

        assertEquals(605, model.getSnapshot().currentMinute);
        assertEquals(2, model.getSnapshot().fastPasses);
        assertTrue(output.toString().contains("Thanks for visiting"));
    }


    /**
     * Tests joining and leaving a regular line through console commands.
     */
    public void testJoinAndLeave()
    {
        model.registerPlayer("Eyal", 60, 150);
        ParkController controller = makeController("");

        controller.handleCommand("join 3");
        assertTrue("QUEUED".equalsIgnoreCase(model.getSnapshot().playerState));

        controller.handleCommand("leave");
        assertTrue("FREE".equalsIgnoreCase(model.getSnapshot().playerState));
        assertEquals(600, model.getSnapshot().currentMinute);
    }


    /**
     * Tests that the session stops when the park closes.
     */
    public void testParkCloses()
    {
        model.registerPlayer("Eyal", 60, 150);
        model.advanceTime(595);
        ParkController controller = makeController("");

        controller.handleCommand("advance");
        controller.handleCommand("advance");

        assertEquals(1200, model.getSnapshot().currentMinute);
        assertTrue(output.toString().contains("The park is closed"));
    }


    /**
     * Tests the controller constructor's null checks.
     */
    public void testNullControllerDependencies()
    {
        ConsoleView view = new ConsoleView(
            new Scanner(""), new PrintStream(output));

        try
        {
            new ParkController(null, view);
            fail("Expected IllegalArgumentException for null model.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }

        try
        {
            new ParkController(model, null);
            fail("Expected IllegalArgumentException for null view.");
        }
        catch (IllegalArgumentException exception)
        {
            assertNotNull(exception);
        }
    }


    /**
     * Creates a controller with simulated keyboard input.
     *
     * @param input the lines to supply to the view
     * @return controller connected to the test model
     */
    private ParkController makeController(String input)
    {
        ConsoleView view = new ConsoleView(
            new Scanner(input), new PrintStream(output));
        return new ParkController(model, view);
    }
}
