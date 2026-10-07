# PrintXpress — Project Plan

**Module:** CSE5011 Mobile Application Development  
**Submission deadline:** 16 October 2026  
**Project workspace:** C:\Users\hp\Desktop\mad

## Goal and working rules

Build an original, working native Android application for a Sri Lankan digital printing service. Prioritize correct order flow, SQLite integration, validation, a consistent UI, testing, and clear assignment evidence. Work in small, buildable phases. Use Java for application code and XML for layouts. Do not use Kotlin, Flutter, React Native, Ionic, or a web application. No deployment or hosting is required.

The existing reference PDF, DOCX, and ZIP in the workspace are source material only. Do not copy another student's text, code, diagrams, or screenshots. Do not claim a feature passes until it has been executed and checked.

## Agreed feature scope

- Account: registration, login, profile editing, logout, and delivery address management.
- Catalogue: browse the seven product categories, products, prices, sizes, materials, and specifications.
- Customization: select print options and quantity; enter custom text; choose artwork with Android's file picker; optionally save a design.
- Checkout: review prices, choose pickup or home delivery, validate the address when needed, place an order, and save the order and items to SQLite.
- Orders: view history and details, track status, and cancel or reschedule before printing begins.
- Information: local notifications, promotional offers, sample designs, print guidelines, and FAQ/support information.

**Categories:** Business Cards, Flyers, Posters, Banners, Stickers, T-Shirts, and Mugs.

The first implementation may use local sample catalogue content and local order status changes. Promotions are informational and display-only: promotions.discount is a percentage value (10.0 means 10%), and no promotion discount is applied to checkout totals. It must not imply that a print shop or delivery service is connected.

## Screen and navigation plan

1. Splash Screen
2. Login Screen
3. Register Screen
4. Home Screen
5. Product Categories Screen
6. Product List Screen
7. Product Details Screen
8. Customize Print Screen
9. Artwork Selection Screen
10. Order Summary Screen
11. Pickup / Delivery Screen
12. Order Confirmation Screen
13. My Orders Screen
14. Order Details / Tracking Screen
15. Notifications / Offers Screen
16. Profile Screen
17. Manage Addresses Screen
18. Saved Designs Screen
19. Print Guidelines / FAQ Screen

Navigation: Splash → Login/Register → Home → Product Categories → Product List → Product Details → Customize Print → Artwork Selection (when needed) → Order Summary → Pickup / Delivery → Order Confirmation → My Orders → Order Details / Tracking.

Home, My Orders, Notifications / Offers and Profile are the four main bottom-navigation destinations. Profile links to Manage Addresses, Saved Designs and Print Guidelines / FAQ; logout returns to Login. See docs/task-c/screen-specifications.md for the nineteen planned destinations. Activities and fragments will be chosen for reliable navigation after the Android project scaffold is created.

## Database plan

Use a local SQLite database named **PrintXpressDB**. Enable foreign keys, use integer primary keys, and keep data access in DatabaseHelper. Store dates and times consistently as text. Seed the seven categories and an original starter catalogue once. The approved relational design contains eleven tables:

| Table | Columns | Relationships |
| --- | --- | --- |
| users | user_id, full_name, email, phone, password | Parent of addresses, orders, saved_designs, notifications |
| addresses | address_id, user_id, address_line, city, district, postal_code | user_id → users |
| categories | category_id, category_name | Parent of products |
| products | product_id, category_id, product_name, description, base_price, image_name | category_id → categories |
| product_options | option_id, product_id, option_type, option_value, price_adjustment | product_id → products |
| orders | order_id, user_id, order_date, order_type, delivery_address, scheduled_for, rescheduled_at, total_amount, status | user_id → users |
| order_items | order_item_id, order_id, product_id, quantity, custom_text, artwork_path, unit_price, subtotal | order_id → orders; product_id → products |
| order_item_options | order_item_option_id, order_item_id, option_id, price_adjustment | order_item_id → order_items; option_id → product_options |
| saved_designs | design_id, user_id, design_name, file_path | user_id → users |
| notifications | notification_id, user_id, title, message, created_at, is_read | user_id → users |
| promotions | promotion_id, title, description, discount, start_date, end_date | Independent offer records |

The orders.scheduled_for TEXT NOT NULL field records the customer's current selected pickup or delivery date/time. The nullable orders.rescheduled_at TEXT field records when the most recent rescheduling action occurred. Store both as ISO 8601 UTC text (yyyy-MM-ddTHH:mm:ssZ); convert a customer-selected Sri Lankan local time to UTC for storage and back for display. Rescheduling replaces scheduled_for with the newly selected time and updates rescheduled_at. The planned statuses are Processing, Printing, Ready for Pickup, Out for Delivery, Completed, and Cancelled. The customer may cancel or reschedule **only** when status is Processing; all five other statuses reject both actions.

Product choices are rows in product_options: option_id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER NOT NULL, option_type TEXT NOT NULL, option_value TEXT NOT NULL, and price_adjustment REAL NOT NULL DEFAULT 0, with a foreign key to products. Example types are Size, Material, Print Side, Finish, Colour, and Paper Type; example values include A4, A3, Matte, Glossy, Front Only, and Front and Back.

Selected choices are rows in order_item_options: order_item_option_id INTEGER PRIMARY KEY AUTOINCREMENT, order_item_id INTEGER NOT NULL, option_id INTEGER NOT NULL, and price_adjustment REAL NOT NULL DEFAULT 0, with foreign keys to order_items and product_options. Its adjustment is a checkout-time price snapshot. DatabaseHelper/application logic must verify that each selected product_options.product_id equals the related order_items.product_id before saving; foreign keys alone do not enforce this. order_items has no fixed size or material columns; it holds order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, product_id INTEGER NOT NULL, quantity INTEGER NOT NULL, custom_text TEXT, artwork_path TEXT, unit_price REAL NOT NULL, and subtotal REAL NOT NULL, with foreign keys to orders and products.

Currency is Sri Lankan Rupees (LKR). Monetary database columns remain REAL for this academic project, but Java price calculations must use BigDecimal, never direct floating-point totals. Round unit prices, item subtotals, option adjustments, and order totals to exactly two decimal places before SQLite persistence and UI display. Keep a delivery address snapshot in orders so history remains understandable after profile edits. Database operations will cover account lookup/create/update, address CRUD, catalogue and option reads, saved design CRUD, order creation in a transaction, order/history/status reads, permitted order updates, notifications, and promotions. Passwords must never be stored as plain text even though the required column is named password; the exact student-friendly hashing approach will be documented before implementation. See docs/database/database-design.md, schema.md, and normalization.md.

## Validation and business rules
- Require a name, valid email and phone number, a suitable password, and matching confirmation.
- Reject duplicate email addresses; never save an invalid registration.
- Require all product options needed by the selected product and quantity greater than zero.
- Require artwork or custom text when the selected print product needs customer content.
- Require a saved or entered address for Home Delivery; pickup needs no delivery address.
- Calculate the order total from item data and save order and item rows together.
- Permit cancellation or rescheduling only when status is Processing; reject both actions for Printing, Ready for Pickup, Out for Delivery, Completed, and Cancelled.

Exact input limits and the detailed pricing formula will be recorded during implementation; the approved LKR BigDecimal/two-decimal policy applies throughout.

## Proposed initial Android project structure

This is a proposal only. **No Android application code or Gradle files have been created yet.**

    mad/
    ├── .gitignore
    ├── README.md
    ├── PROJECT_PLAN.md
    ├── settings.gradle
    ├── build.gradle
    ├── gradle.properties
    ├── gradlew / gradlew.bat
    ├── gradle/wrapper/
    ├── app/
    │   ├── build.gradle
    │   └── src/
    │       ├── main/
    │       │   ├── AndroidManifest.xml
    │       │   ├── java/com/printxpress/app/
    │       │   │   ├── activities/
    │       │   │   ├── fragments/
    │       │   │   ├── models/
    │       │   │   ├── adapters/
    │       │   │   ├── database/
    │       │   │   └── utils/
    │       │   └── res/
    │       │       ├── layout/
    │       │       ├── drawable/
    │       │       ├── mipmap-*/
    │       │       └── values/
    │       ├── test/java/com/printxpress/app/
    │       └── androidTest/java/com/printxpress/app/
    └── docs/
        ├── task-a/  task-b/  task-c/  task-d/  task-e/  task-f/
        ├── diagrams/
        ├── database/
        └── screenshots/

The app package will be com.printxpress.app. Keep screen, model, adapter, database, and utility classes small enough to explain in a viva. Use Android SDK components and add a dependency such as RecyclerView or Material Components only when it serves an implemented screen. Select compatible Android Gradle Plugin, SDK, and Java versions after checking the Android Studio environment.

## Development phases

| Phase | Deliverable and check |
| --- | --- |
| 1. Foundation | Inspect workspace; create plan, README, docs, Git ignore; agree Android scaffold. |
| 2. Database and models | Create schema, models, seeds, and database methods; verify relationships and queries. |
| 3. Authentication | Register, login, profile, addresses, and form validation. |
| 4. Product browsing | Home, categories, product list/details, and adapters. |
| 5. Print customization | Options, quantity, text, artwork picker, and price calculation. |
| 6. Orders | Summary, pickup/delivery, transactional save, history, tracking, cancellation/rescheduling. |
| 7. Additional features | Notifications, promotions, saved designs, guidelines, FAQ/support. |
| 8. UI polish | Consistent layout, spacing, navigation, empty states, and error messages. |
| 9. Testing | Execute positive and negative cases, fix defects, record real outcomes. |
| 10. Documentation | Finalize Tasks A–F, diagrams, evidence, user/technical guides, and README. |

Keep Git commits tied to actual completed work. Do not commit build output, local SDK paths, credentials, or fabricated evidence.

## Assignment evidence plan

- **Task A:** Original critical comparison of Android/iOS, Android Studio/Xcode, Java/Kotlin/Swift, native/cross-platform, and database choices; justify Android + Java + XML + SQLite.
- **Task B:** Use case, class, activity, and ER diagrams; explain 1NF, 2NF, 3NF and final schema/design choices.
- **Task C:** Capture attractive UI designs and real completed screens.
- **Task D:** Show relevant Java, XML, SQLite, CRUD, validation, catalogue, customization, artwork, orders, tracking, notifications, and profile evidence.
- **Task E:** Prepare about 20 useful positive and negative test cases with ID, objective, data, steps, expected result, actual result, and status. Leave actual result and status blank until tested.
- **Task F:** Write user and technical documentation, including requirements, installation/run steps, navigation, architecture, database, structure, important classes, validation, and testing.

## Current status and next gate

Phase 1 planning files and documentation directories are prepared. The eleven-table database design is approved and documented for Task B, with PlantUML source diagrams prepared. The application has not been scaffolded, built, executed, or tested. Tomorrow, create the native Java/XML Android Studio project on the laptop with Android Studio, using the installed compatible SDK and Gradle defaults; then verify a clean sync and launch before implementing database code.
