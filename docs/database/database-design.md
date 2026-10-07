# PrintXpress Database Design

**Status:** Approved design for a future Android SQLite implementation. No database file or Android code exists yet.
**Database name:** PrintXpressDB

## Design decisions

The design has eleven relational tables: users, addresses, categories, products, product_options, orders, order_items, order_item_options, saved_designs, notifications, and promotions. Primary keys are integer identifiers. Foreign keys must be enabled on every SQLite connection and checked during order creation. A single database transaction must save an order, its items, and their chosen options together.

### Orders and scheduling

Orders contains the original order_date, the current scheduled_for TEXT pickup/delivery time, and nullable rescheduled_at TEXT for the latest rescheduling timestamp. The application will require scheduled_for at checkout and store all date/time values in one consistent format. Rescheduling replaces scheduled_for and updates rescheduled_at. The existing order_type distinguishes Pickup from Home Delivery. delivery_address is a snapshot for Home Delivery and can be empty for Pickup.

Only Processing orders can be cancelled or rescheduled by the customer. Printing, Ready for Pickup, Out for Delivery, Completed, and Cancelled are locked against both actions. The application must re-read the status before committing an update so that a stale screen cannot change an ineligible order. This is a local academic workflow; no print-shop backend is implied.

### Flexible product options

product_options defines choices available for a product. Each row has option_id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER NOT NULL, option_type TEXT NOT NULL, option_value TEXT NOT NULL, and price_adjustment REAL NOT NULL DEFAULT 0. Its product_id references products(product_id). Type examples: Size, Material, Print Side, Finish, Colour, Paper Type. Value examples: A4, A3, Matte, Glossy, Front Only, Front and Back.

order_item_options records the selected choices for an item. Each row has order_item_option_id INTEGER PRIMARY KEY AUTOINCREMENT, order_item_id INTEGER NOT NULL, option_id INTEGER NOT NULL, and price_adjustment REAL NOT NULL DEFAULT 0. Its two foreign keys reference order_items(order_item_id) and product_options(option_id). The adjustment is copied when the order is placed, preserving its price even if the catalogue changes. Fixed size and material columns are removed from order_items.

order_items has order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER NOT NULL, product_id INTEGER NOT NULL, quantity INTEGER NOT NULL, custom_text TEXT, artwork_path TEXT, unit_price REAL NOT NULL, and subtotal REAL NOT NULL. Its foreign keys reference orders(order_id) and products(product_id). A proposed price rule is unit_price = product base_price + sum of selected option price_adjustment values; subtotal = unit_price × quantity; total_amount = sum of item subtotals. Validate price and round for display consistently before saving.

### Integrity and operational rules

- Validate that each selected option belongs to the same product as its order item. The listed foreign keys alone do not enforce this cross-table rule.
- Validate one selection per single-choice option type when applicable. Option cardinality may vary by product and needs application rules or additional metadata later.
- Preserve catalogue rows referenced by historical orders. Restrict deletion or use an explicit archive policy; do not silently cascade-delete past orders.
- Validate quantity greater than zero, nonnegative prices, delivery address for Home Delivery, and required customer content before the transaction.
- Treat artwork_path and saved design file_path as persisted content URIs when using Android's Storage Access Framework. URI access must be retained when needed.
- Store a password hash in users.password, not a plain password. Enforce unique email in the database as well as in form validation.
- total_amount, unit_price, subtotal, the delivery address, and selected option adjustments are intentional order-time snapshots for stable history.
- SQLite REAL is required by this approved schema but can have floating-point rounding effects. Use a consistent Java calculation and rounding policy, and verify totals in tests.
- The schema does not yet have a delivery-fee or rescheduling-history table. If either becomes required, add it through a documented migration before implementing that behavior.

See schema.md for all fields and relationships, and normalization.md for the 1NF–3NF explanation.
