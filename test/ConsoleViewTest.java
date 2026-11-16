import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ConsoleView: the constructor, readLine, displayMenu, and
 * displayMessage. Tests for displayPark need ParkSnapshot objects and
 * are not included here.
 *
 * Output is captured in memory. Expected text uses the system line
 * separator because println does.
 */
public class ConsoleViewTest
{
    private static final String NL = System.lineSeparator();

    private ByteArrayOutputStream buffer;
    private PrintStream output;

    @Before
    public void setUp()
    {
        buffer = new ByteArrayOutputStream();
        output = new PrintStream(buffer, true);
    }

    /** Creates a view that reads from the given text and writes to the buffer. */
    private ConsoleView viewWithInput(String text)
    {
        return new ConsoleView(new Scanner(text), output);
    }

    /** Everything written to the output so far. */
    private String printed()
    {
        return buffer.toString();
    }

    // ---------- Constructor ----------

    @Test(expected = IllegalArgumentException.class)
    public void constructor_nullInput_throws()
    {
        new ConsoleView(null, output);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_nullOutput_throws()
    {
        new ConsoleView(new Scanner(""), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_bothNull_throws()
    {
        new ConsoleView(null, null);
    }

    @Test
    public void constructor_validArguments_printsNothing()
    {
        viewWithInput("");
        assertEquals("", printed());
    }

    // ---------- readLine ----------

    @Test
    public void readLine_returnsTheInputLine()
    {
        ConsoleView view = viewWithInput("join 1" + NL);
        assertEquals("join 1", view.readLine("> "));
    }

    @Test
    public void readLine_printsPromptWithoutNewline()
    {
        ConsoleView view = viewWithInput("join 1" + NL);
        view.readLine("> ");
        assertEquals("> ", printed());
    }

    @Test
    public void readLine_endOfInput_returnsNull()
    {
        ConsoleView view = viewWithInput("");
        assertNull(view.readLine("> "));
    }

    @Test
    public void readLine_endOfInput_stillPrintsPrompt()
    {
        ConsoleView view = viewWithInput("");
        view.readLine("> ");
        assertEquals("> ", printed());
    }

    @Test
    public void readLine_emptyLine_returnsEmptyString()
    {
        ConsoleView view = viewWithInput(NL);
        assertEquals("", view.readLine("> "));
    }

    @Test
    public void readLine_noTrailingNewline_returnsLastLine()
    {
        ConsoleView view = viewWithInput("quit");
        assertEquals("quit", view.readLine("> "));
    }

    @Test
    public void readLine_preservesWhitespaceAndCase()
    {
        ConsoleView view = viewWithInput("  JOIN 1  " + NL);
        assertEquals("  JOIN 1  ", view.readLine("> "));
    }

    @Test
    public void readLine_readsLinesInOrderThenNull()
    {
        ConsoleView view = viewWithInput("first" + NL + "second" + NL);

        assertEquals("first", view.readLine("> "));
        assertEquals("second", view.readLine("> "));
        assertNull(view.readLine("> "));
    }

    @Test
    public void readLine_printsPromptOncePerCall()
    {
        ConsoleView view = viewWithInput("a" + NL + "b" + NL);

        view.readLine("1> ");
        view.readLine("2> ");

        assertEquals("1> 2> ", printed());
    }

    @Test
    public void readLine_emptyPrompt_printsNothing()
    {
        ConsoleView view = viewWithInput("a" + NL);
        view.readLine("");
        assertEquals("", printed());
    }

    // ---------- displayMessage ----------

    @Test
    public void displayMessage_printsTextWithNewline()
    {
        ConsoleView view = viewWithInput("");
        view.displayMessage("Joined the line.");
        assertEquals("Joined the line." + NL, printed());
    }

    @Test
    public void displayMessage_emptyMessage_printsBlankLine()
    {
        ConsoleView view = viewWithInput("");
        view.displayMessage("");
        assertEquals(NL, printed());
    }

    @Test
    public void displayMessage_calledTwice_printsBothInOrder()
    {
        ConsoleView view = viewWithInput("");
        view.displayMessage("one");
        view.displayMessage("two");
        assertEquals("one" + NL + "two" + NL, printed());
    }

    @Test
    public void displayMessage_doesNotConsumeInput()
    {
        ConsoleView view = viewWithInput("join 1" + NL);
        view.displayMessage("hello");
        assertEquals("join 1", view.readLine(""));
    }

    // ---------- displayMenu ----------

    @Test
    public void displayMenu_printsBlankLineThenSixCommands()
    {
        ConsoleView view = viewWithInput("");
        view.displayMenu();

        String[] lines = printed().split(Pattern.quote(NL));

        assertEquals(8, lines.length);
        assertEquals("", lines[0]);
    }

    @Test
    public void displayMenu_listsCommandsInOrderWithDescriptions()
    {
        ConsoleView view = viewWithInput("");
        view.displayMenu();

        String[] lines = printed().split(Pattern.quote(NL));

        assertTrue(lines[1].startsWith("join <ride ID>"));
        assertFalse(lines[1].contains("vip"));
        assertTrue(lines[1].contains("regular"));

        assertTrue(lines[2].startsWith("join <ride ID> vip"));
        assertTrue(lines[2].contains("VIP"));

        assertTrue(lines[3].startsWith("leave"));
        assertTrue(lines[3].contains("current line"));

        assertTrue(lines[4].startsWith("advance"));
        assertTrue(lines[4].contains("five minutes"));

        assertTrue(lines[5].startsWith("status"));
        assertTrue(lines[5].contains("stats"));

        assertTrue(lines[6].startsWith("quit"));
        assertTrue(lines[6].contains("Exit"));
    }

    @Test
    public void displayMenu_doesNotConsumeInput()
    {
        ConsoleView view = viewWithInput("status" + NL);
        view.displayMenu();
        assertEquals("status", view.readLine(""));
    }

    @Test
    public void displayMenu_calledTwice_printsTheMenuTwice()
    {
        ConsoleView view = viewWithInput("");
        view.displayMenu();
        String once = printed();

        view.displayMenu();

        assertEquals(once + once, printed());
    }
    /**
     * Tests that the prediction command appears in the menu.
     */
    @Test
    public void testPredictionInMenu()
    {
        ConsoleView view = viewWithInput("");

        view.displayMenu();

        assertTrue(printed().contains("predict <ride ID>"));
    }
}
