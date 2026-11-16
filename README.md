# Project3Group65CoasterQ
# CoasterQ — Project 3, Group 65
CS 2114 · Deliverable 4 (Second Prototype)

A console application where a player visits a roller coaster park. The player
enters their name, height, and weight, joins a regular or VIP line, advances
time, and views ride statistics. Each line uses a `LinkedQueue<QueueEntry>`
to keep guests in the order they joined.

## How to compile and run

Run `Main.java` as a Java Application in Eclipse, then interact with the
program in the console.

To run the model by itself, right-click `ParkModelTest.java` in Eclipse
and select Run As: JUnit Test. `testAdvanceCompletesRide()` registers
a player, joins a ride, advances time, and checks that the model records
one completed ride and the correct wait statistics. The new prediction
tests check that wait estimates are calculated and invalid ride IDs
are handled correctly.

To run the other tests, run `LinkedQueueTest.java`, `QueueEntryTest.java`,
`GuestTest.java`, `CoasterTest.java`, `ParkSnapshotTest.java`,
`ParkControllerTest.java`, `ConsoleViewTest.java`, and
`WaitTimePredictorTest.java` as JUnit tests in Eclipse.

Sample session: enter a name, height, and weight → `status` (view rides) →
`predict 3` (view estimated wait time) → `join 3` (join ride 3) →
`advance` (advance time) → `status` (view updated statistics) →
`quit` (exit).

## Demonstration: it works
We ran `Main.java` and entered `Gus`, `abc`, and `180`. The program
rejected the height because it must be a whole number. We entered
`Gus`, `50`, and `180` on the second try.

| Command | Result |
| --- | --- |
| `join 99` | Rejected the ride ID because valid IDs are 1 through 8. |
| `join 1` | Rejected the rider because Steel Vengeance requires at least 52 inches. |
| `join 5 vip` | Rejected the VIP request because Gemini has no VIP line. |
| `join 5` | Joined Gemini's regular line. |
| `join 3` | Rejected the second join because the player was already in line. |
| `leave` | Left Gemini's line. |
| `join 3 vip` | Joined Magnum XL-200's VIP line and used one pass. |
| `advance` twice | Reached 10:10 AM with one completed ride and a 7-minute wait. |
| `status | Saw we Rrode 1 ride, waited 7 minutes, and had an average wait time of 7 minutes |
| `quit` | Exited the program. |

For Deliverable 4, we also added the `predict <ride ID>` command.
Entering `predict 3` displays an estimated wait time for Magnum XL-200.
Invalid inputs like `predict abc`, `predict 99`, and `predict` are
handled without crashing the program.
## Reflection

### How did our design handle the change?

For Deliverable 4, we added the regression-based wait-time prediction
feature that we planned in Deliverable 3. We updated `ParkModel`,
`WaitTimePredictor`, `ParkController`, and `ConsoleView`. The model now
records how many people were ahead of a guest when they joined a line
and how long they actually waited before boarding. `WaitTimePredictor`
uses these observations to estimate wait times for each coaster's
regular line.

Most of the changes matched what we predicted in Deliverable 3. We
expected to update the model, controller, and view while keeping classes
like `LinkedQueue`, `Guest`, and `Main` unchanged. One difference was
that we recorded the actual wait time during boarding rather than
directly in `advanceTime()`. We also used the existing `displayMessage()`
method instead of creating a separate `displayPrediction()` method.

### Did the change cross the model/view line?

The separation between our model, controller, and view stayed the same.
When the user enters `predict <ride ID>`, the controller reads the
command and calls `ParkModel.predictWait()`. The model calculates the
prediction, and the view displays the result.

The view does not calculate wait times or store regression data. This
made the feature easier to add because we could change the prediction
logic without changing how the rest of the program displays information.

For future changes, organizing the commands into smaller controller
methods could make adding new features easier without affecting the
other parts of the program.

### What changed from our Deliverable 3 plan?

Our original plan was to display a message saying there was not enough
data until the regression model had enough observations. Instead, we
decided to use the existing wait-time estimate as a fallback. This
allows the user to receive an estimate before enough data has been
collected for regression.

We also kept the prediction calculations entirely in Java rather than
using the Python wrapper we had considered in Deliverable 3. This
kept the feature within our existing program and avoided adding
another dependency.

We also updated `ParkModel` so simulated guests receive unique IDs
instead of reusing the player's ID.

### What doesn't work yet?

The main simulation and prediction features have been implemented, and
we have added and run the tests. We do not have any additional MVP
features planned at this point. Our remaining work is to complete the
final integration checks, address any issues that come up, and make
sure the full program is ready for the final demonstration. If time allows,
we might implement a Swing GUI to display a video of a rollercoaster when your status
says riding.

## Next Steps

### What we need to finish

1. Run the full test suite and verify that all model, queue, and
   prediction tests pass.
2. Check that ride dispatch, completion, abandonment, and VIP passes
   continue working correctly with the new prediction feature.
3. Verify that invalid commands, incomplete inputs, and invalid ride
   IDs do not crash the program.
4. Implement video with java Swing.

### Stretch goals

We completed the regression-based wait-time predictor that we planned
as our Deliverable 4 change feature. We are not committing to any
additional stretch goals for the final build.

We are continuing to leave out Swing animation and wait-dependent
abandonment so we can focus on finishing and testing the current
program.

### Three-minute demonstration

| Time | What we will show |
| --- | --- |
| 0:00–0:30 | Run the program, register a player, and use `status` to view the rides. |
| 0:30–1:00 | Enter `predict 3` to demonstrate the new wait-time prediction feature. |
| 1:00–1:45 | Join a ride, advance time, and show the updated ride statistics. |
| 1:45–2:20 | Enter invalid inputs such as `predict abc` and `join 99` to show error handling. |
| 2:20–2:50 | Run the JUnit tests, including the queue and prediction tests. |
| 2:50–3:00 | Briefly explain how the controller connects the view and model. |

### Who does what

| Item | Owner |
| --- | --- |
| Final testing of ride dispatch, completion, and statistics | Mikey |
| Verify full queues, FIFO order, and abandoned visits | Mikey |
| Final testing of the wait-time predictor | Ryan |
| Verify console integration and invalid input | Eyal |
| Check the prediction command and its output | Ryan and Eyal |
| Final review of queue behavior and tests | Ryan |
| Run the complete prototype and prepare the demo | Everyone |

## Attribution


| File | Who | Contribution |
| --- | --- | --- |
| `LinkedQueue.java` | Ryan | Wrote the bounded FIFO queue |
| `LinkedQueueTest.java` | Ryan | Wrote FIFO, capacity, empty queue, and interleaved operation tests |
| `QueueEntry.java` | Ryan | Wrote queue visit and abandonment tracking |
| `QueueEntryTest.java` | Ryan | Wrote visit and abandonment tests |
| `Guest.java` | Mikey | Wrote guest information and validation |
| `GuestTest.java` | Mikey | Wrote tests for guest information and invalid inputs |
| `Coaster.java` | Mikey | Wrote ride configuration, queues, and ride state |
| `CoasterTest.java` | Mikey | Wrote tests for ride configuration and capacity |
| `ParkModel.java` | Mikey, Ryan | Mikey wrote the main simulation; Ryan revised it for wait-time prediction |
| `ParkModelTest.java` | Mikey, Ryan | Tested model functionality and added prediction tests |
| `ParkSnapshot.java` | Mikey | Wrote the read-only park and player snapshot |
| `ParkSnapshotTest.java` | Mikey | Wrote tests for snapshot fields and copied lists |
| `WaitTimePredictor.java` | Ryan | Developed the regression-based wait-time prediction feature |
| `WaitTimePredictorTest.java` | Ryan | Added regression, fallback, and invalid-input tests|
| `ConsoleView.java` | Eyal, Ryan | Eyal wrote the console display; Ryan helped add the prediction menu option |
| `ConsoleViewTest.java` | Eyal, Ryan | Tested console output and the prediction menu |
| `ParkController.java` | Eyal | Eyal wrote command handling and helped integrate the prediction command |
| `ParkControllerTest.java` | Eyal | Tested commands, invalid inputs, and prediction handling |
| `Main.java` | Eyal | Connected the model, controller, and view to start the program |

### GenAI note

We used ChatGPT to help plan our test cases, especially edge cases like
empty or full queues, invalid ride IDs, and leaving and rejoining a line.
We also used it to think through how the simulation clock advances and
how that interacts with rides boarding and dispatching. It helped us understand how the
classes work together, including how the controller calls the model and
how the view displays information from a snapshot. ChatGPT also helped
draft some of the queue and console code and tests. For Deliverable 4,
we asked which classes would need to change to add the regression-based
wait time predictor.

For Deliverable 4, we used ChatGPT to help implement and test the
regression-based wait-time predictor, identify which classes needed
changes, troubleshoot errors, and review our tests against the project
requirements.

ChatGPT was also used to help debug issues with Node references and
implementing JUnit 4 to make test cases.
