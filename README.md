# PrintXpress Mobile Application

PrintXpress is a native Android application for a digital printing service in Sri Lanka. Customers can browse print products, customize an order, choose pickup or home delivery, and follow locally stored order history and status.

This project is for **CSE5011 – Mobile Application Development**. The submission deadline is **16 October 2026**.

## Technology stack

| Area | Choice |
| --- | --- |
| Platform and IDE | Native Android; Android Studio |
| Application code | Java |
| User interface | XML layouts |
| Local relational data | SQLite (PrintXpressDB) |
| Notifications | Local in-app SQLite notices |
| Artwork selection | Android Storage Access Framework / file picker |
| Version control | Git and GitHub |

The current implementation uses one Java Activity, XML View containers and SQLite. Catalogue and order cards are generated inside XML-defined scrolling screens. No deployment or hosting is planned.

## Planned main features

- Registration, login, profile, and delivery addresses
- Seven product categories: Business Cards, Flyers, Posters, Banners, Stickers, T-Shirts, and Mugs
- Product information, print options, quantity, custom text, and artwork selection
- Order summary, pickup or home delivery, and SQLite order storage
- Order history, tracking, and cancellation or rescheduling before printing
- Notifications, promotions, saved designs, print guidelines, and FAQ/support

See [PROJECT_PLAN.md](PROJECT_PLAN.md) for the full scope, screens, database design, proposed project structure, and phased implementation plan. Assignment material will be organized under [docs/](docs/).

## Current status

**Implementation verified on API 35; authentication also verified on API 24 and API 25.** On 8 October 2026 the repository gained its first Android app module, Java/XML customer flow, eleven-table SQLite database, Gradle wrapper, automated tests and real emulator screenshots. The full order flow and 35-case Task E matrix were exercised on API 35; registration/login and password compatibility passed on API 24, API 25 and API 35. See [IMPLEMENTATION_LOG.md](docs/development/IMPLEMENTATION_LOG.md) and [TEST_PLAN.md](docs/task-e/TEST_PLAN.md) for exact results and remaining limits. GitHub has not been pushed from this PC.

## Running the project in Android Studio

1. Install Android Studio with Android SDK Platform 35 and a compatible JDK (the verified build used JDK 21).
2. Open this repository folder in Android Studio and let Gradle sync. `local.properties` is machine-local and ignored by Git.
3. Select an Android API 35 emulator or device for the fully verified order flow and run `app`. Authentication was also verified on API 24 and API 25. The code declares API 24 minimum and uses the compatible PBKDF2 fallback there.
4. From a terminal with `JAVA_HOME` and `ANDROID_HOME` set, run `gradlew.bat assembleDebug`, `gradlew.bat test`, and `gradlew.bat lint`.

The verified configuration is AGP 8.9.1, Gradle 8.11.1, `compileSdk`/`targetSdk` 35, and `minSdk` 24. See the [technical guide](docs/task-f/TECHNICAL_GUIDE.md) for the architecture and test command.

## Academic evidence

The docs folders contain Tasks A–F, diagrams, database notes, and real emulator screenshots. The 35 Task E cases were executed; PASS is used only for checks actually performed. Broader device and accessibility review remains. All writing and diagrams must be original to PrintXpress.
