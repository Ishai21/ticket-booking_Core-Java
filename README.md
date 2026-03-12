# TrainBooking - Java Core Concepts

Project overview

This is a small console-based Java application that simulates a simplified train booking system (IRCTC-like) to showcase core Java concepts: OOP, collections, file-based persistence (JSON), exception handling, and simple service layering.

Key features implemented

- User signup (creates and stores users in a JSON file)
- User login (simple credential check)
- Search trains by source and destination
- View available trains and seat layout
- Book a seat (creates a Ticket and stores booking details)
- Fetch user's bookings
- Cancel a booking
- Basic console-based menu and flow control

High-level architecture

Packages

- ticket_booking.entities: Domain model classes (User, Train, Ticket).
- ticket_booking.services: Application services that contain business logic (TrainService, UserBookingService).
- ticket_booking.util: Utility classes for user/service helpers (UserServiceUtil).
- ticket_booking.localDb: JSON files representing persistent data (trains.json, users.json).
- ticket_booking: `Main` class that contains the console UI and coordinates user input with services.

Data flow (concise)

1. Main reads user input from console and routes commands to service layer.
2. Services (TrainService, UserBookingService) perform business logic and update in-memory models.
3. Services persist changes by writing JSON to files in `src/main/java/ticket_booking/localDb` or the runtime `localDb` directory using Jackson's ObjectMapper.
4. Entities are simple POJOs with getters/setters and are serialized/deserialized to JSON.

Why this structure

- Keeps core logic in services so `Main` remains focused on UI/input.
- Entities remain POJOs so they are easy to serialize and unit test.
- Local JSON files remove the need for a DB and keep the project self-contained for demonstration.

Where to look first in the codebase

- `src/main/java/ticket_booking/Main.java` — Console UI and program entry point.
- `src/main/java/ticket_booking/services` — Business logic for trains and bookings.
- `src/main/java/ticket_booking/entities` — Domain classes (Train, Ticket, User).
- `src/main/java/ticket_booking/localDb` — sample JSON files used as persistent storage.

Run and build instructions

Prerequisites: JDK 11+ (or the version used to build the project) and Gradle wrapper (included).

From the project root (where `gradlew` is):

- Build the project:

  ./gradlew clean build

- Install distribution and run the included script (recommended for a packaged run):

  ./gradlew installDist
  ./build/install/IRCTC/bin/IRCTC

- Or run the jar directly (if available in build/libs):

  java -jar build/libs/IRCTC-1.0-SNAPSHOT.jar

Notes about running

- The app uses JSON files in `localDb/` (top-level) for runtime persistence; sample data exists in `src/main/java/ticket_booking/localDb` and a copy may be created/updated under `localDb/` while running.
- When testing flows (signup, login, book, cancel), watch the terminal output for confirmation messages. If you don't see expected messages, check the console UI code in `Main.java` for missing prints.

Developer notes & tips

- Generating getters/setters in IntelliJ: Use Code -> Generate... (macOS default shortcut is Cmd+N or Cmd+Insert). If Cmd+N isn't working, try right-click -> Generate or check Keymap preferences.
- Persistence: Services write JSON via Jackson's ObjectMapper. The helper method `saveUserListToFile()` is private because it's an internal implementation detail of the service — callers should interact through public service methods (e.g., signup, updateUser) rather than writing files directly.
- Common debug points:
  - If search or menu options don't behave as expected, check how `Main` parses user input and which `switch` cases map to service calls.
  - If booking/cancellation doesn't show updated results, inspect the booking service methods for proper list mutation and persistence calls.

Suggested small follow-ups (low-risk improvements)

- Add unit tests for service layer (search, booking, cancel) — JUnit tests to demonstrate correctness.
- Improve console UI messages for success/failure (signup/login/book/cancel) and ensure Ticket ID is printed when booking completes.
- Add more sample trains into `src/main/java/ticket_booking/localDb/trains.json` with varied sources/destinations.

Contribution

This repository is intended as a personal showcase of core Java skills. If you want to contribute:

- Fork the repo, add tests for the changed features, and open a PR explaining the change.


Contact

- For questions about the code, open an issue or contact the project owner(irfhan.shaikk@gmail.com).

Requirements coverage

- README.md created: Done
- Basic architecture explained: Done
- Suggested repository name: irctc-core-java

Thank you — let me know if you want the README expanded with screenshots, sample console session logs, or a CONTRIBUTING guide.
