# Project3Group65CoasterQ
# CoasterQ — Project 3, Group 65
CS 2114 · Deliverable 3 (First Prototype)

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
one completed ride and the correct wait statistics.

To run the other tests, run `LinkedQueueTest.java`, `QueueEntryTest.java`,
and `ParkControllerTest.java` as JUnit tests in Eclipse.

Sample session: enter a name, height, and weight → `status` (view rides) →
`join 3` (join ride 3 if eligible) → `advance` until the ride is complete →
`status` (view statistics) → `quit` (exit).

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

## Reflection

### What changed from the specification?

- The spec called for a unique token for each queue visit. We used a `long`
  and passed it into `QueueEntry(Guest, int, long)` from the model. This gives
  a guest a separate entry each time they join a line.
- We kept the validation split from the spec. The controller checks input
  format, the model checks park rules, and the queue enforces its capacity.
  We also added checks for a null guest and a negative join time in
  `QueueEntry` so invalid visits are rejected when they are created.
- We added a test for abandoning one entry and creating a new entry for
  the same guest. This checks that leaving a line does not mark a later
  visit as abandoned.
-3 New fields were added to ParkModel: activeVip, activeWait, nextVisitToken. Wait is 
recorded at dispatch, counted only when ride completes.
-Extra constructor: Parkmodel(long, Coaster[]) to help with test cases.

### Did the model stand alone?

`ParkModelTest` calls `ParkModel` directly. In the console version,
`ParkController` reads commands and calls the model, and `ConsoleView`
displays the resulting `ParkSnapshot`.

We verified that the model runs on its own by registering a player,
joining a VIP line, and advancing time through direct calls to
`ParkModel`. The resulting snapshot showed one completed ride, a
7-minute wait, and one VIP pass remaining. In the current code,
`ParkModel` handles queue and ride rules, while `ConsoleView` handles
console output.

### What happened to our unknowns?

We figured out how to use a linked queue for each line, how to represent
separate visits for the same guest, and how to pass park state to the
console through a snapshot.

We figured out how to advance the simulation and track wait times.
The console advances five minutes per command, and the model records
the wait when the player boards. The ride counts toward the player's
statistics when it finishes. The current wait estimate uses the
queue size and ride timing. The regression predictor is planned
for Deliverable 4.

### What doesn't work yet?

The player is assigned ID `0`, and simulated guests currently reuse
that same ID. Simulated guests still need unique IDs.


## Next Steps

### What we need to finish

1. Verify that a player can register, join a line, board, finish a ride,
   and see the updated statistics.
2. Check that the model skips an abandoned visit when a player leaves
   and rejoins. Verify full lines, ride restrictions, and VIP pass usage.
3. Check that the console handles invalid numbers, invalid ride IDs,
   incomplete commands, and end of input.
4. Implement a linear regression class for a more accurate WaitTme Calculation, possibly
with a python wrapper.

### Change feature


We will add a regression model that predicts how long a player will
wait for a coaster. The model will record how many guests are ahead
when a player joins and how many minutes the player waits before
boarding. Once there are enough observations, the player can enter
`predict <ride ID>` to see an estimated wait for that coaster's
regular line. Until then, the console will say there is not enough
data to make a prediction.

We expect `ParkModel.joinLine()` to record the starting queue length
and `advanceTime()` to record the actual wait when the player boards.
We plan to add `ParkModel.predictWait(int rideId)` and a
`WaitTimePredictor` class to store observations and fit the regression.
`ParkController.handleCommand()` will handle the new command, and
`ConsoleView.displayMenu()` and a new `displayPrediction()` method
will show the option and result. We expect the existing `LinkedQueue`
methods, `Guest`, and `Main.main()` to work as written.

### Stretch goals

We will attempt regression-based wait estimates as our change feature.
We are dropping Swing animation and wait-dependent abandonment for
Deliverable 4.

### New tests needed

- Check that boarding records the actual wait and abandoned visits
  do not become training observations.
- Test predictions with known data, too few observations, and queue
  lengths that are all the same.
- Check that invalid ride IDs and malformed `predict` commands are
  handled and that the console displays either the estimate or the
  message about needing more data.
### Who does what

| Item | Owner |
| --- | --- |
| Finish and test ride dispatch, completion, and statistics | Mikey |
| Verify full queues, FIFO order, and abandoned visits | Ryan and Mikey |
| Finish console integration and test invalid input | Eyal |
| Record queue lengths and actual waits, and implement the regression predictor | Mikey |
| Test the predictor with known data, too few observations, and identical queue lengths. Verify that abandoned visits are excluded. | Ryan |
| Add the `predict <ride ID>` command, display its result, and test invalid commands | Eyal ||
| Verify queue order after reopening | Ryan |
| Run the full prototype together | Everyone |

## Attribution


| File | Who | Contribution |
| --- | --- | --- |
| `LinkedQueue.java` | Ryan | Bounded FIFO queue |
| `LinkedQueueTest.java` | Ryan | FIFO, capacity, and empty queue tests |
| `QueueEntry.java` | Ryan | Visit and abandonment tracking |
| `QueueEntryTest.java` | Ryan | Visit and abandonment tests |
| `Guest.java` | Mikey | Guest information, including name, height, and weight |
| `Coaster.java` | Mikey | Ride settings, regular and VIP queues, and ride state |
| `ParkModel.java` | Mikey | Registration, line rules, time advancement, ride dispatch, and statistics |
| `ParkSnapshot.java` | Mikey | Read-only park, ride, and player information for the console |
| `ParkModelTest.java` | Mikey | Tests for joining lines, advancing time, and completing rides |
| `ConsoleView.java` | Eyal | Console prompts, menus, ride information, and statistics display |
| `ParkController.java` | Eyal | Command parsing, input validation, and calls to the park model |
| `ParkControllerTest.java` | Eyal | Tests for valid commands, invalid input, quitting, and end of input |
| `Main.java` | Eyal | Connects the model, controller, and view to start the program |
| `CoasterTest.java` | Mikey | Tests ride configuration, regular and VIP queues, and queue capacity. |
| `GuestTest.java` | Mikey | Tests guest information and rejects invalid names, heights, and weights. |
| `ParkSnapshotTest.java` | Mikey | Tests snapshot fields and verifies that snapshot lists are copied and cannot be modified. |

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

ChatGPT was also used to help debug issues with Node references and
implementing JUnit 4 to make test cases.
