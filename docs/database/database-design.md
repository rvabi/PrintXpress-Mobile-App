# PrintXpress Database Design

**Status:** Approved design, implemented in `DatabaseHelper.java` on 8 October 2026. The eleven tables, seed data, order transaction and relationships were verified on an API 35 emulator; see Task E for exact checks.
**Database name:** PrintXpressDB

## Design decisions

The design has eleven relational tables: users, addresses, categories, products, product_options, orders, order_items, order_item_options, saved_designs, notifications, and promotions. Primary keys are integer identifiers. `DatabaseHelper.onConfigure` enables foreign keys, and the API 35 data passed `PRAGMA foreign_key_check`. A single database transaction saves an order, its items, and their chosen options together.

### Promotions

promotions.discount is a percentage value for display: 10.0 means 10%. Promotions are independent informational offers in the current version. Do not subtract this percentage from product prices, order item subtotals, delivery amounts, or checkout totals unless discount redemption is explicitly approved later.

### Orders and scheduling

Orders contains the original order_date, scheduled_for TEXT NOT NULL for the current pickup/delivery time, and nullable rescheduled_at TEXT for when the most recent rescheduling action occurred. Store date/time values as ISO 8601 UTC text in yyyy-MM-ddTHH:mm:ssZ form (for example, 2026-10-10T09:30:00Z). Convert a customer-selected Sri Lankan local time to UTC before storage and back to local time for display. Rescheduling replaces scheduled_for with the newly selected time and updates rescheduled_at to the action timestamp. The existing order_type distinguishes Pickup from Home Delivery. delivery_address is a snapshot for Home Delivery and can be empty for Pickup.

Only Processing orders can be cancelled or rescheduled by the customer. Printing, Ready for Pickup, Out for Delivery, Completed, and Cancelled are locked against both actions. The application must re-read the status before committing an update so that a stale screen cannot change an ineligible order. This is a local academic workflow; no print-shop backend is implied.

### Flexible product options

product_options defines choices available for a product. Each row has option_id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER NOT NULL, option_type TEXT NOT NULL, option_value TEXT NOT NULL, and price_adjustment REAL NOT NULL DEFAULT 0. Its product_id references products(product_id). Type examples: Size, Material, Print Side, Finish, Colour, Paper Type. Value examples: A4, A3, Matte, Glossy, Front Only, Front and Back.

order_item_options records the selected choices for an item. Each row has order_item_option_id INTEGER PRIMARY KEY AUTOINCREMENT, order_item_id INTEGER NOT NULL, option_id INTEGER NOT NULL, and price_adjustment REAL NOT NULL DEFAULT 0. Its two foreign keys reference order_items(order_item_id) and product_options(option_id). The adjustment is copied when the order is placed, preserving its price even if the catalogue changes. Fixed size and material columns are removed from order_items.

order_items has order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, product_id INTEGER NOT NULL, quantity INTEGER NOT NULL, custom_text TEXT, artwork_path TEXT, unit_price REAL NOT NULL, and subtotal REAL NOT NULL. Its foreign keys reference orders(order_id) and products(product_id). Currency is Sri Lankan Rupees (LKR). The implemented price rule is unit_price = product base_price + sum of selected option price_adjustment values; subtotal = unit_price × quantity; total_amount = sum of item subtotals. Java uses BigDecimal for these calculations, avoiding direct floating-point totals. Monetary results are rounded to two decimal places before SQLite persistence and UI display.

### Integrity and operational rules

- Application/DatabaseHelper logic must verify that product_options.product_id matches the related order_items.product_id for every selected option before saving. The listed foreign keys alone do not enforce this cross-table rule.
- Validate one selection per single-choice option type when applicable. Option cardinality may vary by product and needs application rules or additional metadata later.
- Preserve catalogue rows referenced by historical orders. Restrict deletion or use an explicit archive policy; do not silently cascade-delete past orders.
- Validate quantity greater than zero, nonnegative prices, delivery address for Home Delivery, and required customer content before the transaction.
- Treat artwork_path and saved design file_path as persisted content URIs when using Android's Storage Access Framework. URI access must be retained when needed.
- Store a versioned, randomly salted password hash in `users.password`, never plaintext. API 26+ creates PBKDF2-HMAC-SHA256 records; API 24–25 creates PBKDF2-HMAC-SHA1 records. The format identifies the algorithm, so the same TEXT column verifies either format and no schema migration was required. Enforce case-insensitive unique email in SQLite as well as form validation.
- total_amount, unit_price, subtotal, the delivery address, and selected option adjustments are intentional order-time snapshots for stable history.
- Monetary SQLite columns remain REAL for this academic project. Read them into BigDecimal using a safe decimal conversion, calculate with BigDecimal, and use a documented two-decimal rounding mode (HALF_UP) before persistence and display. Verify unit, item, and order totals in tests; never sum monetary double values directly.
- The schema does not yet have a delivery-fee or rescheduling-history table. If either becomes required, add it through a documented migration before implementing that behavior.

See schema.md for all fields and relationships, and normalization.md for the 1NF–3NF explanation.
