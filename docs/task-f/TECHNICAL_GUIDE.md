# PrintXpress Technical Guide

**Repository state checked:** 8 October 2026 on the new Windows PC. The clone originally contained planning and Tasks A–C only; the Android module was added in the same checkout. See [Task D](../task-d/IMPLEMENTATION.md) for implementation detail and [Task E](../task-e/TEST_PLAN.md) for executed tests.

## Requirements and build

| Item | Verified value |
| --- | --- |
| Platform | Native Android, Java application source, XML Views, AndroidX |
| Package / application ID | `com.printxpress.app` |
| IDE | Android Studio at `C:\Program Files\Android\Android Studio` |
| JDK used for Gradle | Oracle JDK 21.0.10 (`C:\Program Files\Java\jdk-21.0.10`) |
| Android Gradle Plugin / Gradle | 8.9.1 / 8.11.1 |
| SDK | `compileSdk` 35, `targetSdk` 35, `minSdk` 24 |
| SDK path on verified PC | `C:\Users\SHALINI\AppData\Local\Android\Sdk` |
| Test devices | Pixel 6 Google APIs x86_64, API 35, `emulator-5554`; Nexus 5X Google APIs x86_64, API 25, `emulator-5556`; Nexus 5X AOSP x86, API 24, `emulator-5558` |

Open the repository root in Android Studio and let it sync. Install Android SDK Platform 35. Authentication was verified on API 24, API 25 and API 35 emulators; the full order flow was exercised on API 35. Use a JDK compatible with AGP 8.9.1; the verified command-line build used JDK 21. Do not add `local.properties`, `.gradle/`, `build/`, `app/build/`, emulator data or credentials to Git.

From PowerShell in the repository root:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.10'
$env:ANDROID_HOME = 'C:\Users\SHALINI\AppData\Local\Android\Sdk'
.\gradlew.bat assembleDebug
.\gradlew.bat test
.\gradlew.bat lint
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Use Android Studio Run or `adb install -r` and `adb shell am start -n com.printxpress.app/.MainActivity` to launch. The first launch creates and seeds `PrintXpressDB`.

For API 24 verification, install the stable `system-images;android-24;default;x86` package in SDK Manager, create a Nexus 5X AVD named `PrintXpress_API24`, and boot it. Check `adb devices`, then run `adb -s emulator-5558 shell getprop ro.build.version.sdk` on this PC; it returned `24`. Use the serial shown by your own `adb devices` output on another PC. The app does not depend on Google APIs for authentication.

## Code map

| Location | Purpose |
| --- | --- |
| `app/src/main/java/com/printxpress/app/MainActivity.java` | One Activity with nineteen customer destinations, forms and navigation |
| `.../database/DatabaseContract.java` | Database/table identifiers |
| `.../database/DatabaseHelper.java` | SQLite schema, seeds, reads, CRUD, transactional order creation, status checks |
| `.../security/PasswordHasher.java` | Secure salted password derivation and verification |
| `.../pricing/Money.java` | BigDecimal rounding and LKR formatting |
| `app/src/main/res/layout/` | XML screen container and reusable card View |
| `app/src/main/res/values/` | Mint/coral colors, theme and strings |
| `app/src/test/` | JVM Money tests |
| `app/src/androidTest/` | Isolated Android SQLite/security/order tests |

`MainActivity` renders View controls into the XML layout's scrolling container. It inflates XML cards for small lists. There are no Fragments, RecyclerView adapters, Kotlin application files or Compose code. The earlier Task C mapping of multiple Activities and Fragments remains a design proposal, not a description of built classes.

## Database and rules

`DatabaseHelper` creates `PrintXpressDB` version 1 with foreign keys enabled and eleven tables: users, addresses, categories, products, product_options, orders, order_items, order_item_options, saved_designs, notifications and promotions. Seed data is inserted in `onCreate` only. Never delete or recreate a production database as a migration; `onUpgrade` intentionally requires a non-destructive migration design before any schema version increase.

Passwords are stored in the existing `users.password` TEXT column as `algorithm$iterations$salt$hash`, with a random 16-byte salt, 120,000 PBKDF2 iterations and a 256-bit derived value. API 26+ creates `pbkdf2_sha256` records using `PBKDF2WithHmacSHA256`; API 24–25 creates `pbkdf2_sha1` records using `PBKDF2WithHmacSHA1`. Verification reads the algorithm tag, so existing SHA256 rows remain valid. Because [Android's SHA256 factory starts at API 26](https://developer.android.com/reference/javax/crypto/SecretKeyFactory), API 24–25 verify older SHA256 rows with a small PBKDF2-HMAC-SHA256 loop built on [Android's available `HmacSHA256` MAC](https://developer.android.com/reference/javax/crypto/Mac). No schema migration or extra cryptography dependency was added. SharedPreferences contains only `loggedInUserId`; email uniqueness is case-insensitive in SQLite.

The API 24 and API 25 emulators each registered and authenticated a new user, rejected a wrong password, and retained the account across app restarts. API 24 also retained its signed-in session after restart; SQLite held one `pbkdf2_sha1` user and 7/7/34 seeded category/product/option rows, with no foreign-key violations. API 35 registered and authenticated another user, rejected a wrong password, and retained its session and existing user rows. Direct instrumentation verified fixed ASCII and Unicode legacy SHA256 records on all three APIs.

Money calculations use `BigDecimal`, HALF_UP rounding to two decimals, and LKR formatting. The academic schema stores already-rounded price snapshots in REAL columns. Product choices are normalized rows; an order item's selected option is accepted only when its `product_id` matches the item's product. `createOrder` saves the order, item(s), selected choices and notice in one transaction and rolls back on failure. Promotions never affect the calculated total.

Schedules are stored as UTC ISO 8601 text (`yyyy-MM-ddTHH:mm:ssZ`) and displayed in Asia/Colombo local time. Reschedule updates `scheduled_for` and `rescheduled_at`. Only the current SQLite status `Processing` permits Cancel or Reschedule; the helper re-reads status inside a transaction before updating. A delivery order stores a snapshot of the selected address.

Artwork is selected with `ACTION_OPEN_DOCUMENT`, using image/PDF types and a persisted URI read grant. The app saves the URI/reference and readable filename; it does not upload the file. Notifications are SQLite in-app notices, not OS push notifications.

## Verification

On this PC, `assembleDebug`, `test` and `lint` succeeded. The JVM suite has two Money tests per debug/release variant. The isolated Android database suite passed four tests via:

```powershell
.\gradlew.bat assembleDebugAndroidTest
adb -s emulator-5554 install -r app\build\outputs\apk\debug\app-debug.apk
adb -s emulator-5554 install -r app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
adb -s emulator-5554 shell am instrument -w com.printxpress.app.test/androidx.test.runner.AndroidJUnitRunner
```

Repeat the three `adb` commands with `emulator-5556` for API 25 or `emulator-5558` for API 24. Direct `am instrument` executed the test APK and returned `OK (9 tests)` on **each** emulator. Four database tests use `PrintXpressTestDB` and leave customer data untouched; five password tests check both stored formats, including a Unicode SHA256 fixture. Earlier `gradlew.bat connectedDebugAndroidTest` attempts failed when the local Unified Test Platform did not collect results (0 tests reported). A later 10 October 2026 run on the separate `PrintXpress_API35` AVD completed **9/9 tests with BUILD SUCCESSFUL**. The earlier failures remain historical testing evidence, not the current connected-test result.

Manual API 35 emulator tests created two orders, verified order rows and option rows through SQLite, checked foreign keys, exercised stale-screen status rejection, address editing/deletion, artwork change/removal and invalid drafts, and confirmed customer data and seed counts after restart. The API 25 smaller display was checked at default and 1.3 font scale; Home remained readable and Register could scroll to its submit button. Real screenshots are indexed in [Task C's screenshot plan](../task-c/screenshot-plan.md). Wider device and screen-reader checks remain. GitHub was not pushed automatically.
