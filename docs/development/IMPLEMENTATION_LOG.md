# PrintXpress Implementation Log

## 8 October 2026 — new-PC repository inspection and first implementation

### Actual checkout before edits

| Check | Initial finding |
| --- | --- |
| Repository root | `C:\Users\SHALINI\OneDrive\Desktop\MAD\PrintXpress-Mobile-App` (nested inside the supplied `MAD` workspace) |
| Branch / remote | `main`, tracking `origin/main`; `origin` was `https://github.com/rvabi/PrintXpress-Mobile-App.git` |
| Git status | Clean (`## main...origin/main`) |
| Last commits | Only three existed: `3d37a5e Complete PrintXpress planning for Tasks A-C`; `85d4b29 docs: lock PrintXpress database design and Task B diagrams`; `f95992b chore: establish PrintXpress project plan and docs structure` |
| App module / Gradle | No `app/`, Gradle files or wrapper; initial `gradlew.bat assembleDebug`, `test` and `lint` each failed because the wrapper did not exist |
| Source / layouts / database / tests | No Java source, XML app layouts, SQLite implementation or test source existed; Tasks A–C and database/diagram designs were documentation only |
| `docs/development/IMPLEMENTATION_LOG.md` | Absent |
| Task D/E/F guides | Absent, apart from `.gitkeep` placeholders |
| Initial milestone | Planning and approved eleven-table design only; implementation had not begun |
| Initial `git diff --check` | No errors |

Initial `compileSdk`, `targetSdk`, `minSdk`, AGP and Gradle versions were **not defined in the repository**. They were selected only for this first Android module. Java on PATH was Oracle 21.0.10; Android Studio's bundled JBR reported 25.0.3, so Gradle used the compatible installed JDK 21. The SDK was at `C:\Users\SHALINI\AppData\Local\Android\Sdk`. Initially only platform `android-37.0` (stable metadata, but not resolvable by cached AGP 8.9.1) and no system image were installed. Existing AVD `Pixel_6_Pro` referred to a missing API 36 Google APIs image. `adb devices` initially listed no devices. Local C: free space was about 144 GB before the emulator-image install.

### Work performed

- Added the first **PrintXpress** Android module inside the existing repository, without deleting planning documents or creating an unrelated project. Selected AGP 8.9.1 and Gradle 8.11.1 from local caches; generated the wrapper. Installed stable SDK Platform/Build Tools 35 to resolve the initial `android-37` build failure. The resulting configuration is `compileSdk` 35, `targetSdk` 35 and `minSdk` 24. This is an initial choice, not an upgrade from a prior app version. `local.properties` was not created or committed.
- Added Java/XML View navigation for the nineteen planned customer destinations, the approved mint/coral resources, rounded cards/buttons and a vector launcher icon.
- Added `DatabaseContract`, eleven SQLite tables, seed catalogue/promotions, CRUD, PBKDF2 password hashing, minimal SharedPreferences session, dynamic options, BigDecimal pricing, SAF artwork selection, saved designs, address selection, transactional order creation, notices, and guarded cancel/reschedule.
- Installed the API 35 Google APIs x86_64 image after checking local disk space, created a Pixel 6 AVD, started it headlessly, and confirmed `emulator-5554 device` and `sys.boot_completed=1`.
- Added `MoneyTest` JVM tests and four isolated `DatabaseHelperTest` Android tests, then updated README, project plan, Task C design/evidence notes, database notes and Tasks D–F.
- Captured real emulator screenshots under `docs/screenshots/`, including Login, Register, Home, Categories, Product List/Details, Customization, Artwork Selected, Summary, Pickup/Delivery, Confirmation, Orders/Tracking, Notifications and Profile. The selected artwork for tests was an image file on the emulator; no server upload was claimed.

### Build and test history

1. Initial requested wrapper commands failed because the clone contained no wrapper or app code.
2. The first scaffold compile against the installed `android-37.0` failed because AGP 8.9.1 could not find target `android-37`; stable API 35 was installed and selected. An offline build then needed missing transitive AndroidX dependencies; the normal Gradle build resolved them and assembled the APK.
3. `gradlew.bat assembleDebug`: **PASS** after the API 35 setup and again after implementation changes.
4. `gradlew.bat test`: **PASS**; two `MoneyTest` cases ran in both debug and release variants.
5. First `gradlew.bat lint` found one `WrongConstant` error in `MainActivity`; replaced the numeric typeface value with `Typeface.BOLD`. Subsequent `gradlew.bat lint`: **PASS** (warnings remain, chiefly resource/localization polish and dependency version notices).
6. `gradlew.bat connectedDebugAndroidTest`: **not a PASS**. The local Unified Test Platform process terminated before returning results; its report showed 0 tests. After fixing the isolated test database setup, installing the same test APK and running `adb shell am instrument -w com.printxpress.app.test/androidx.test.runner.AndroidJUnitRunner` returned **OK (3 tests)**. The test database is `PrintXpressTestDB`, separate from customer data.

### Real emulator and SQLite results

Registration, invalid email, short password, duplicate email, invalid and valid login, session restart, browsing, search, dynamic price, text, artwork picker/change/removal, saved design, summary, pickup, home delivery, address creation/selection/edit/deletion, scheduling, order creation, history, details, notices, promotions, profile edit, FAQ, logout and login again were executed. Invalid content/quantity and all non-Processing status guards were also exercised. See Task E for individual outcomes.

Order #1: Pickup, quantity 2 Classic Business Cards with Glossy, Front and Back, Large; unit LKR 2,400.00 and total LKR 4,800.00. Rescheduling changed `scheduled_for` and populated `rescheduled_at`; cancellation changed status to Cancelled. Order #2: Home Delivery to the saved Colombo address, quantity 1 Promotional Flyers with Premium, Front and Back, A4 and a real persisted content URI; total LKR 2,150.00. SQLite contained two orders, two items and six option rows, and `PRAGMA foreign_key_check` returned no rows. To test stale-screen protection, order #2's status was changed externally to Printing while its Activity still showed Processing. Both Cancel and Reschedule were rejected; no schedule or notice changed.

After an app restart, counts were unchanged at one user, seven categories, seven products, 34 product options, one address, two orders, two items, six order-item options, four notices and one promotion. Saved designs also persisted; after deleting one duplicate test design, one remained. The test account's password value had a `pbkdf2_sha256` prefix, length 90 and contained no plaintext test password. Session login remained after a signed-in restart; logout stayed signed out after restart.

### Defects found and fixed

- **Option reset after artwork:** Reproduced by selecting priced options, opening Artwork Selection, then returning; the total fell from LKR 4,800.00 to LKR 3,000.00. The transition had saved text/quantity but not option IDs. Fixed by preserving option IDs and unit price before the artwork route. Rebuilt and reran the same flow; all selections and LKR 4,800.00 remained.
- **Draft leaked into another product:** Reproduced by starting Promotional Flyers after the first order; it inherited quantity 2 and `Hello`. Fixed by clearing the completed draft and resetting when product ID changes. Rebuilt and verified Flyers opened with quantity 1 and empty text.
- **Saved design reuse after process restart:** The design reference would have been cleared while selecting the first product. Added a pending-design handoff through Categories. Rebuilt, restarted and verified the saved design name appeared in customization.
- **Schedule display:** Persisted UTC values were initially shown raw in order screens. Added conversion back to Asia/Colombo display time; reran Orders and observed `2026-10-11 16:30 (Sri Lanka)` for the stored `2026-10-11T11:00:00Z` value.

### Files changed

Gradle root/wrapper files; `app/build.gradle`; `app/src/main/AndroidManifest.xml`; Java source in `com.printxpress.app`, `database`, `security`, `pricing`; XML layouts, drawables and values; JVM and Android tests; README and project plan; database/Task B and Task C status/theme/evidence documentation; Task D/E/F documents; real screenshots. Build outputs, SDK files, AVD data, `.gradle/`, `.idea/` state and credentials are not included in Git changes.

### Pending work

- Check larger font sizes, smaller screens, accessibility, and visual polish on more than the Pixel 6 API 35 emulator.
- Investigate the host's Gradle Unified Test Platform result-collection failure if a `connectedDebugAndroidTest` Gradle report is required; direct instrumentation already executes and passes the tests.
- Review original Task A–C drafts/diagrams for final assignment wording and actual architecture; the historical multiple-Activity/Fragment mapping differs from the implemented single-Activity structure.
- No commit or push was made. Before any later push, inspect the exact commit contents, final build/test state and obtain explicit user approval.

### Final quality gate on this PC

`gradlew.bat clean assembleDebug`, `gradlew.bat test`, `gradlew.bat lint`, and `git diff --check` all completed successfully after the documentation update. `adb devices` still reported `emulator-5554 device`. Git status contained only the intended tracked-document edits and untracked source/evidence files; `.gradle/` and `app/build/` were ignored, and `local.properties` was absent. The expanded direct Android instrumentation run returned `OK (4 tests)`; the separate Gradle UTP limitation above remains pending.

### Additional executed edge cases

After the first quality gate, the four remaining Task E cases were executed. An address changed from 12 Main Street to 24 Lake Road and then was deleted; order #2 retained its original 12 Main Street delivery snapshot. Artwork was changed from one picked image to another, then removed; the filename and empty state changed accordingly. Two invalid customization drafts (missing text/artwork and quantity zero) stayed on the form. The fourth isolated Android test simulated Printing, Ready for Pickup, Out for Delivery, Completed and Cancelled and confirmed both customer actions were rejected for every status, with schedule and notice count unchanged. Direct instrumentation returned `OK (4 tests)`.

The final repeated gate after these cases passed: `gradlew.bat clean assembleDebug`, `gradlew.bat test`, `gradlew.bat lint`, direct Android instrumentation (`OK (4 tests)`), and `git diff --check`. Task E contains 35 executed PASS cases and the screenshot directory contains 25 nonempty real PNG captures. `adb devices` still reported `emulator-5554 device`. No commit or push was made.

## 8 October 2026 — final pre-commit review

The shell initially lacked `ANDROID_HOME`; the first `clean assembleDebug` attempt stopped at SDK discovery. Setting `ANDROID_HOME` only for the build process to `C:\Users\SHALINI\AppData\Local\Android\Sdk` resolved it without creating `local.properties`. The repeated `gradlew.bat clean assembleDebug`, `gradlew.bat test`, `gradlew.bat lint` and `git diff --check` all passed. Two `MoneyTest` methods ran in each debug/release JVM variant. Direct instrumentation was rebuilt and rerun on `emulator-5554`: `OK (4 tests)`. Re-running `gradlew.bat connectedDebugAndroidTest` still failed with `Failed to receive the UTP test results`; its HTML report lists **0 tests**, so it is not reported as a test pass.

The 35 Task E PASS entries were compared with the prior emulator execution record, screenshots, SQLite observations and direct instrumentation; no entry required a downgrade. The 25 PNGs are distinct, nonempty 1080×2400 emulator captures. The screenshot index now accounts for all 25 files and clarifies that some order timestamp captures predate the UTC-to-local display fix, the Profile capture predates the name edit, and Login shows the destination after splash. No screenshot is reused for an unrelated screen. No files are staged. The tracked and unignored file list contains no `local.properties`, generated builds, `.gradle`, Android Studio state, emulator data, credentials or unrelated reference PDF/DOCX/ZIP files.

**Compatibility finding:** The Gradle configuration declares `minSdk 24`, but `PasswordHasher` requests `PBKDF2WithHmacSHA256` from Android `SecretKeyFactory` without a fallback. [Android lists this algorithm as supported from API 26](https://developer.android.com/reference/javax/crypto/SecretKeyFactory). API 35 registration/login passed; API 24–25 authentication has not been executed and is likely unavailable with the current provider. This is a code follow-up before claiming API 24 runtime support. The pre-commit review requested documentation-only corrections, so application code and declared SDK versions were left as they were.

Documentation corrected in this review: `README.md`, `docs/task-c/screenshot-plan.md`, `docs/task-e/TEST_PLAN.md`, `docs/task-d/IMPLEMENTATION.md`, `docs/task-f/TECHNICAL_GUIDE.md`, and this log. Task E remains 35 PASS, 0 PENDING and 0 FAIL for the API 35 test matrix; API 24–25 compatibility is an uncovered pending item outside those 35 rows. No commit or push was made.

## 8 October 2026 — API 25 compatibility and assignment consistency continuation

`PasswordHasher` now creates PBKDF2-HMAC-SHA256 records on API 26+ and PBKDF2-HMAC-SHA1 records on API 24–25. Both use 120,000 iterations, a random 16-byte salt and a 256-bit derived key. The existing `algorithm$iterations$salt$hash` TEXT value is sufficient to distinguish records, so **no SQLite schema version or table change** was made. Verification follows the stored algorithm. On API 24–25 a short PBKDF2-HMAC-SHA256 loop using Android's available `HmacSHA256` MAC checks older SHA256 records; a fixed old-format record matched on both tested APIs. [Android's factory table](https://developer.android.com/reference/javax/crypto/SecretKeyFactory) lists PBKDF2-HMAC-SHA1 from API 10 and SHA256 from API 26; the [MAC table](https://developer.android.com/reference/javax/crypto/Mac) lists HmacSHA256 from API 1.

The API 25 Google APIs image and `PrintXpress_API25` Nexus 5X AVD were available for testing, with sufficient local C: space. `adb -s emulator-5556 shell getprop ro.build.version.sdk` returned 25. The current debug APK installed and launched. A new API25 Tester registered, logged in, logged out, received a rejection for a wrong password, stayed logged out after force-stop/relaunch, then logged in again with the correct password. A signed-in force-stop/relaunch returned to Home. SQLite showed the new user's `pbkdf2_sha1` marker and unchanged 7/7/34 seeded category/product/option counts. `api25-auth-home.png` is a real app capture after that flow. The first attempted capture accidentally showed an unrelated Pixel Launcher crash dialog; it was replaced only after the UI dump and resumed Activity confirmed PrintXpress Home was visible.

On API 35 (`emulator-5554`), a new API35 Regression account registered, logged in, logged out, rejected a wrong password, then logged in again after a signed-out restart. A signed-in restart returned to Home. SQLite retained both the prior user and new user's `pbkdf2_sha256` records plus the original two orders, two items, six selected options and four notices; `PRAGMA foreign_key_check` remained empty. `api35-regression-home.png` captures the new signed-in account. The older user's plaintext password was not available, so this review did not claim a manual re-login to that particular account; the fixed legacy SHA256 record was verified by instrumentation on both APIs.

Four new `PasswordHasherTest` instrumentation cases check deterministic same-salt verification, wrong-password rejection, distinct salts, legacy SHA256 verification and the API-selected new-record marker. The first API 25 direct run found an outdated `DatabaseHelperTest` assertion that assumed every new record was SHA256; the code worked, but the assertion failed. The assertion now checks the API-selected marker. The repeated direct suite returned **OK (8 tests)** on API 25 and **OK (8 tests)** on API 35. The tests use a separate `PrintXpressTestDB`; customer databases were not reset.

Task A official reference URLs/titles were checked, and Task B wording, conceptual class method labels, ER constraints, use-case artwork wording, Task C navigation and screenshot index were aligned with the implemented app. The historical sample-design gallery and featured Home cards are identified as proposals rather than built features. A small accessibility improvement sets text-link touch areas to at least 48dp. On the smaller API 25 display at 1.3 font scale, Home remained readable and the Register button could be reached by scrolling; the font setting was restored to 1.0 afterward. Screen-reader behavior, additional device sizes and API 24 runtime remain untested. The known `startActivityForResult`/`onActivityResult` deprecation is a build warning; changing the working artwork flow was not justified during this final verification.

This continuation changed `PasswordHasher.java`, `MainActivity.java`, `DatabaseHelperTest.java`, added `PasswordHasherTest.java`, added two real emulator captures, and updated factual README, plan, Task A–F, database, diagram and development documents. No generated SDK/AVD files, build outputs, credentials, commit or push were added to Git.

### Final assignment and quality gate

The six PlantUML sources rendered to six PNGs under `docs/diagrams/`; each rendered diagram was visually inspected. Task B links to these deliverables. The screenshot index contains 27 nonempty PNGs (26 at 1080×2400 and the API 25 capture at 1080×1920), with 27 distinct SHA-256 file hashes. The 35 Task E matrix rows remain 35 PASS, 0 FAIL, 0 PENDING; the separate API 25 and API 35 authentication observations also passed. API 24 itself remains a pending runtime check outside that matrix.

One additional password regression fixture checks a legacy SHA256 hash made from a non-ASCII password. It verified on both emulator APIs, increasing the direct instrumentation suite to **OK (9 tests)** on API 25 and **OK (9 tests)** on API 35: four isolated database cases and five password cases. The first eight-test results earlier in this entry are historical intermediate runs. The customer data was not reset by these isolated tests.

The final `gradlew.bat clean assembleDebug` completed successfully, with the known `MainActivity` deprecated artwork-result API compile note. `gradlew.bat test` completed successfully: two `MoneyTest` cases in each debug and release variant, zero failures. `gradlew.bat lint` completed successfully with 16 warnings and no errors in `lint-results-debug.xml`; warnings cover dependency updates, backup rules, unused theme resources and text localization. `git diff --check` passed. A fresh `gradlew.bat connectedDebugAndroidTest` on the selected API 35 emulator **failed** with `Failed to receive the UTP test results`; its UTP log contains a fatal runner message, and the Gradle report has no reported test cases. This is a local result-collection limitation, not a claimed Gradle pass. Direct instrumentation independently executed all nine tests on both emulators.

The API 25 Nexus 5X at 1.3 font scale showed readable Home content and a Register form whose submit control remained reachable by scrolling; text links now have at least 48dp height. Screen-reader, automated contrast, keyboard and broader physical-device checks remain open. The Task C style guide records this limited result. No files were staged or committed, and no push occurred. `local.properties` exists as a machine-local SDK pointer and is ignored by Git; generated build folders, SDK/AVD files, credentials and unrelated reference archives remain outside the intended commit. The exact candidate list is in `PRE_COMMIT_FILE_LIST.md`. Final status has 16 modified tracked files and 64 untracked deliverable files, with zero staged files.

## 8 October 2026 — API 24 closeout

About 128 GB of local C: space was available before setup. The preferred Google APIs x86_64 API 24 image was available but its official 1.12 GB download transferred very slowly, so a smaller official **AOSP x86 API 24 revision 8** image was used. [Google's system-image metadata](https://dl.google.com/android/repository/sys-img/android/sys-img2-3.xml) lists its 313,489,224-byte archive and SHA-1 `c1cae7634b0216c0b5990f2c144eb8ca948e3511`. The archive was downloaded from a public mirror, then matched against both official values before extraction to the local SDK; SDK Manager recognized `system-images;android-24;default;x86`. The Nexus 5X `PrintXpress_API24` AVD booted as `emulator-5558`, and `getprop ro.build.version.sdk` returned `24` with `sys.boot_completed=1`. No SDK image, AVD data or downloaded archive was added to the repository.

The current debug APK installed and launched. A new API24 Tester account registered and returned to Login; correct credentials opened Home. After logout, a wrong password stayed on Login. A signed-out force-stop/relaunch stayed on Login; the correct password then opened Home again, and a signed-in force-stop/relaunch returned to Home. SQLite held one user with a `pbkdf2_sha1` marker, seven categories, seven products and 34 options; `integrity_check` returned `ok` and `foreign_key_check` returned no rows. Direct instrumentation on API 24 returned **OK (9 tests)**, including the legacy SHA256 ASCII and Unicode fixtures. `api24-auth-home.png` is a real emulator capture of the signed-in Home state. The Task E 35-case matrix remains 35 PASS, 0 FAIL, 0 PENDING; API 24 is recorded as a separate executed authentication regression.

Documentation updated for these verified results: README, project plan, Task C screenshot/design notes, Task D, Task E, Task F user/technical guides, and this log. The full order workflow was not re-executed on API 24; its existing detailed runtime evidence remains API 35. The Gradle UTP result-collection issue and broader screen-reader/physical-device accessibility checks remain open.

## 9 October 2026 — final closeout gate

After the API 24 documentation changes, `gradlew.bat clean assembleDebug`, `gradlew.bat test`, `gradlew.bat lint` and `gradlew.bat assembleDebugAndroidTest` all completed successfully. The JVM reports contain two `MoneyTest` cases in debug and two in release, with no failures. Lint has 16 warnings and no errors. Direct instrumentation returned **OK (9 tests)** on API 24, API 25 and API 35. During a long host pause in the parallel reinstall, one API 35 APK install timed out at Android integrity verification; a single-device retry installed successfully, and a fresh API 35 direct instrumentation run again returned **OK (9 tests)**. The pause extended two suite wall times but did not cause reported test failures.

`gradlew.bat connectedDebugAndroidTest` was retried with API 35 selected and still failed with `Failed to receive the UTP test results`; the Gradle task is **not** marked as passed. `git diff --check` passed. The 35 Task E matrix cases are 35 PASS, 0 FAIL and 0 PENDING; API 24, API 25 and API 35 authentication checks are separate supplementary PASS rows. The screenshot index accounts for 28 nonempty real PNGs with 28 distinct hashes. Git status shows 16 modified tracked files, 65 untracked intended deliverables and zero staged files. `PRE_COMMIT_FILE_LIST.md` enumerates all 81 candidates in three proposed staging groups. `local.properties` and generated folders are ignored; no forbidden file is tracked or proposed. No commit, push or final PDF was created.
