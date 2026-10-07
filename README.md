# PrintXpress Mobile Application

PrintXpress is a planned native Android application for a digital printing service in Sri Lanka. Customers will be able to browse print products, customize an order, choose pickup or home delivery, and follow their order history and status.

This project is for **CSE5011 – Mobile Application Development**. The submission deadline is **16 October 2026**.

## Technology stack

| Area | Choice |
| --- | --- |
| Platform and IDE | Native Android; Android Studio |
| Application code | Java |
| User interface | XML layouts |
| Local relational data | SQLite (PrintXpressDB) |
| Notifications | Android Notification API |
| Artwork selection | Android Storage Access Framework / file picker |
| Version control | Git and GitHub |

RecyclerView, Material UI components, or Glide will be added only if an implemented feature needs them. No deployment or hosting is planned.

## Planned main features

- Registration, login, profile, and delivery addresses
- Seven product categories: Business Cards, Flyers, Posters, Banners, Stickers, T-Shirts, and Mugs
- Product information, print options, quantity, custom text, and artwork selection
- Order summary, pickup or home delivery, and SQLite order storage
- Order history, tracking, and cancellation or rescheduling before printing
- Notifications, promotions, sample designs, saved designs, print guidelines, and FAQ/support

See [PROJECT_PLAN.md](PROJECT_PLAN.md) for the full scope, screens, database design, proposed project structure, and phased implementation plan. Assignment material will be organized under [docs/](docs/).

## Current status

**Planning and repository setup only.** The workspace originally contained a PDF, a DOCX, and a ZIP reference file. No Android application code, Gradle project, APK, executed test results, or screenshots have been produced yet. The proposed Android project structure is awaiting review.

## Running the project later in Android Studio

When the Android project scaffold has been created:

1. Install a compatible Android Studio and Android SDK on the development laptop.
2. Open this folder as an Android Studio project.
3. Let Gradle sync, using the SDK path configured on that laptop. Keep local.properties out of Git.
4. Select an Android emulator or connect an Android device with USB debugging enabled.
5. Run the app configuration from Android Studio. Use **Build > Build APK(s)** when an APK is needed.

The required Gradle and SDK versions will be documented after the project is scaffolded and checked in Android Studio. These steps are instructions for future use; they have not yet been verified in this workspace.

## Academic evidence

The docs folders are reserved for Tasks A–F, diagrams, database notes, and real screenshots. Test cases will record actual results and PASS/FAIL only after the app has been run and checked. All writing and diagrams must be original to PrintXpress.
