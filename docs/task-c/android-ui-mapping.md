# PrintXpress Android UI Mapping

**Status:** Proposed file and component mapping for the later Android Studio project. The names below describe planned Java classes and XML layouts; none has been created yet. The project will use native Android Views, Java, XML and SQLite.

## Main navigation structure

`MainActivity` would host a `BottomNavigationView` and four fragments: `HomeFragment`, `OrdersFragment`, `NotificationsFragment` and `ProfileFragment`. This keeps the four primary destinations in one activity. Secondary screens would use activities with a toolbar back action. A single `DatabaseHelper` would provide local data through clear Java methods; adapters would bind list data to `RecyclerView` items.

| Planned screen | Likely Java screen | Likely XML layout | Main Views / behavior |
| --- | --- | --- | --- |
| Splash Screen | `SplashActivity.java` | `activity_splash.xml` | `ImageView`/text branding, optional `ProgressBar`; route from local session state. |
| Login Screen | `LoginActivity.java` | `activity_login.xml` | Labelled `EditText` fields, password visibility control, `Button`, register link and field errors. |
| Register Screen | `RegisterActivity.java` | `activity_register.xml` | `ScrollView`, five labelled `EditText` fields, register button and inline validation. |
| Home Screen | `HomeFragment.java` in `MainActivity.java` | `fragment_home.xml` in `activity_main.xml` | Greeting, search field, promotion card, category/featured product lists and embedded sample designs. |
| Product Categories Screen | `CategoriesActivity.java` | `activity_categories.xml` | Grid-style `RecyclerView` with seven category cards. |
| Product List Screen | `ProductListActivity.java` | `activity_product_list.xml` | Category toolbar, `RecyclerView` product cards, empty-state `TextView`. |
| Product Details Screen | `ProductDetailsActivity.java` | `activity_product_details.xml` | Scrollable image, description, starting price, options overview and customize button. |
| Customize Print Screen | `CustomizeActivity.java` | `activity_customize.xml` | Quantity `EditText`, option controls generated from `product_options`, custom text, artwork action and live LKR total. |
| Artwork Selection Screen | `ArtworkSelectionActivity.java` | `activity_artwork_selection.xml` | Selected-file state, choose/change/remove buttons; launches the Android Storage Access Framework picker. |
| Order Summary Screen | `OrderSummaryActivity.java` | `activity_order_summary.xml` | Item/options summary, quantity, artwork/text summary, unit price, subtotal, total and edit/continue actions. |
| Pickup / Delivery Screen | `DeliveryActivity.java` | `activity_delivery.xml` | `RadioGroup`, conditional saved-address list/add action, date/time pickers, validation and place-order button. |
| Order Confirmation Screen | `OrderConfirmationActivity.java` | `activity_order_confirmation.xml` | Success icon/text, persisted order ID, type, schedule and total; Orders/Home buttons. |
| My Orders Screen | `OrdersFragment.java` in `MainActivity.java` | `fragment_orders.xml` | `RecyclerView` order cards, status labels, empty state and details action. |
| Order Details / Tracking Screen | `OrderDetailsActivity.java` | `activity_order_details.xml` | Order information, item/options list, status progression and Processing-only cancel/reschedule actions. |
| Notifications / Offers Screen | `NotificationsFragment.java` in `MainActivity.java` | `fragment_notifications.xml` | Notification and promotion sections/cards, empty states and percentage offer labels. |
| Profile Screen | `ProfileFragment.java` in `MainActivity.java` | `fragment_profile.xml` | Customer details, edit action, links to addresses/designs/help and logout. |
| Manage Addresses Screen | `ManageAddressesActivity.java` | `activity_manage_addresses.xml` | `RecyclerView`, add/edit form or dialog, delete confirmation and address validation. |
| Saved Designs Screen | `SavedDesignsActivity.java` | `activity_saved_designs.xml` | `RecyclerView` of design names/file references, open/delete actions where feasible, empty state. |
| Print Guidelines / FAQ Screen | `InfoActivity.java` | `activity_info.xml` | Scrollable guidance and FAQ sections with concise headings and answers. |

## Shared UI pieces and data behavior

- `RecyclerView` adapters would serve the product, category, order, address, notification and saved-design lists as needed. Small static sections can use ordinary XML Views instead.
- The product option controls would be built from each product's available `product_options` rows. Selected option IDs, quantities, text and artwork reference would move through the checkout flow without treating a sample design as a saved customer design.
- Java price calculations would use `BigDecimal`, round to two decimal places and display `LKR`. The SQLite monetary columns remain `REAL` as approved. Promotion percentages would be displayed only and excluded from checkout calculations.
- The artwork picker would return a local content URI/reference. Later implementation must confirm which file types can actually be opened and how the reference remains available for an order or saved design.
- Home Delivery would reveal an address selector and require a chosen address; Pickup would not require one. Both choices would require a scheduled date/time before an order can be saved.
- Cancellation and rescheduling controls would be visible only for `Processing` orders and the database helper would recheck status before making either change.

The mapping can be adjusted during Android Studio setup if a smaller screen is better presented as a dialog or section, while preserving the documented customer flow and data rules.
