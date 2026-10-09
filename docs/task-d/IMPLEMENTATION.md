# Task D — PrintXpress Implementation

**Status (8 October 2026):** Implemented in the existing repository and exercised on a Pixel 6 API 35 emulator. This document describes the code that exists; test outcomes and gaps are in [Task E](../task-e/TEST_PLAN.md).

## Android Studio and build environment

The first clone on this PC contained only planning documents, diagrams and `.gitignore`; it had no Android module or Gradle wrapper. The native app module was added inside that checkout. The verified setup used Android Studio installed at `C:\Program Files\Android\Android Studio`, Oracle JDK 21.0.10 for Gradle, AGP 8.9.1, Gradle 8.11.1, Android SDK Platform 35, `compileSdk` 35, `targetSdk` 35 and a declared `minSdk` 24. The package and application ID are `com.printxpress.app`. `local.properties` is ignored and was not committed. Runtime checks used an API 35 Pixel 6, an API 25 Nexus 5X Google APIs emulator, and an API 24 Nexus 5X AOSP x86 emulator.

Run from the repository root with a compatible JDK and SDK configured:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
.\gradlew.bat lint
```

## Java/XML architecture and package structure

The app uses AndroidX AppCompat, Java application source, Android XML Views and direct SQLite APIs. `app/src/main/res/layout/activity_main.xml` defines the title, scrolling content container and four-destination navigation container. `view_card.xml` and XML drawables provide reusable rounded cards and buttons. `colors.xml` contains the approved mint/coral palette. `MainActivity.java` renders the nineteen planned destinations into those XML View containers and inflates cards for catalogue and order rows.

There is currently **one Activity** (`MainActivity`) and **no Fragments, RecyclerView adapters or separate model classes**. `DatabaseHelper.OrderLine` is the order-input model. The small lists use native `ScrollView` and `LinearLayout` cards; RecyclerView was not needed for the seeded seven-product catalogue and is not included as a dependency. The earlier `docs/task-c/android-ui-mapping.md` proposed fragments/adapters before coding and is retained as a historical design proposal. Product and order information is read through `Cursor` queries and shown in Java-created View controls within the XML-defined screen.

| Package/file | Responsibility |
| --- | --- |
| `MainActivity.java` | Navigation, forms, validation, product choices, checkout, profile, notices and informational screens |
| `database/DatabaseContract.java` | Database name, version and eleven table names |
| `database/DatabaseHelper.java` | Schema, one-time seeds, queries, CRUD, transactions, order status updates and local notices |
| `security/PasswordHasher.java` | Versioned, salted PBKDF2 password derivation and verification across API levels |
| `pricing/Money.java` | BigDecimal rounding and `LKR 1,500.00` formatting |
| `res/layout`, `res/drawable`, `res/values` | XML View containers, rounded surfaces, launcher icon, strings and approved colors |

## SQLite and data behavior

`PrintXpressDB` is created by `DatabaseHelper` with foreign keys enabled in `onConfigure`. Version 1 creates `users`, `addresses`, `categories`, `products`, `product_options`, `orders`, `order_items`, `order_item_options`, `saved_designs`, `notifications`, and `promotions`. It seeds seven categories, seven products, 34 dynamic choices and one display-only promotion in `onCreate`, so reopening the database does not seed duplicates. Real emulator checks before and after restart returned the same seed counts. `PRAGMA foreign_key_check` returned no violations after two orders.

Registration stores a randomly salted PBKDF2 value in `users.password` with 120,000 iterations, a 16-byte salt and a 256-bit derived key; it does not store plaintext. The existing four-part TEXT format records `pbkdf2_sha256` or `pbkdf2_sha1`, iteration count, salt and hash, so **no database schema change or migration** was needed. New accounts use Android's `PBKDF2WithHmacSHA256` on API 26+ and `PBKDF2WithHmacSHA1` on API 24–25. Verification reads the stored algorithm rather than choosing by the current device API. A compact PBKDF2-HMAC-SHA256 implementation using Android's available `HmacSHA256` MAC verifies older SHA256 records on API 24–25. [Android documents the factory algorithm availability](https://developer.android.com/reference/javax/crypto/SecretKeyFactory) and [HmacSHA256 MAC availability](https://developer.android.com/reference/javax/crypto/Mac). A case-insensitive unique email constraint rejects duplicates. Login saves only `loggedInUserId` in `SharedPreferences`; logout removes that key.

On API 24 and API 25, new accounts registered, logged in, rejected wrong passwords and logged in again after force-stop; SQLite showed `pbkdf2_sha1` records. API 24 also retained the signed-in session after another restart, and its direct instrumentation suite returned `OK (9 tests)`, including old SHA256 ASCII/Unicode fixtures. On API 35, a new account registered and logged in, wrong credentials were rejected, and login/session survived restart; the prior SHA256 user row and new SHA256 row remained in SQLite. These results verify authentication on all three tested API levels while the full order flow remains verified on API 35.

Product choices are rows in `product_options` and are grouped dynamically by `option_type` in customization. One Spinner choice is made for each type. The calculation is `unit = base_price + selected adjustments`, `subtotal = unit × quantity`, and `total = sum of subtotals`. Java uses `BigDecimal` and HALF_UP rounding to two decimal places. The approved SQLite schema keeps monetary columns as REAL, so rounded Java amounts are stored as numeric snapshots. Promotions remain informational and are never subtracted from checkout totals.

`createOrder` validates the user, content, quantity, fulfillment type, address and selected option ownership. For each selected option it queries `product_options` using both `option_id` and the order item's `product_id`; a mismatch throws before order persistence. One SQLite transaction inserts the order header, item rows, selected option rows and an in-app notification. A required failure rolls back all of them. The isolated Android database test passed both an invalid cross-product option rollback and a valid order save.

Order statuses are `Processing`, `Printing`, `Ready for Pickup`, `Out for Delivery`, `Completed`, and `Cancelled`. Only `Processing` shows customer Cancel and Reschedule actions. `DatabaseHelper` also re-reads the current status inside a transaction and updates only a row whose status is still `Processing`. Reschedule writes both `scheduled_for` and `rescheduled_at`; Cancel writes `Cancelled`. Real stale-screen tests changed a visible order to `Printing` through SQLite before tapping each action: both operations were rejected without changing the schedule or adding a notice.

Schedule values are stored as ISO 8601 UTC text and displayed in Sri Lankan local time. Home Delivery keeps a snapshot of the selected saved address in the order. Addresses, saved designs and profile data are stored locally and survived app restart in emulator checks.

## Artwork and notifications

Artwork selection uses Android `ACTION_OPEN_DOCUMENT` with image/PDF MIME filters. The app reads the displayed filename and requests a persisted read grant for the returned content URI. It can choose, change or remove the current artwork reference. A saved design stores the URI and name for later reuse, including after app restart. The app does not upload artwork or send orders to a server.

Notifications are **in-app SQLite records**, inserted on order creation, rescheduling and cancellation and listed beside a display-only promotion. There is no operating-system notification channel, remote push service, print-shop backend or live courier tracking in this academic local workflow.

## Validation and tested scope

Registration checks required fields, email format, a 9–15 character phone pattern, password length plus letter/number, confirmation match and duplicate email. Login checks required fields, email format and credentials. Customization requires quantity greater than zero and text or artwork; delivery requires a future schedule and an address only for Home Delivery. Field errors appear on affected inputs and checkout failures do not show confirmation.

Real API 35 emulator testing covered pickup and delivery, profile/address editing, saved designs, search, artwork change/removal, validation, notices, logout, restart persistence and Processing-only order changes. API 24, API 25 and API 35 authentication regressions passed through the native UI. Direct instrumentation passed nine tests on each emulator, including five password-format cases and all five locked order statuses. The Gradle Unified Test Platform runner on this PC failed to collect results before reporting test cases, so direct instrumentation was used. A smaller API 25 display at 1.3 font scale showed readable Home content and a scrollable Register form; broader device and accessibility polish remains.
