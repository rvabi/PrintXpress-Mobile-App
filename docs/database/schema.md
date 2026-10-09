# PrintXpress Implemented Relational Schema

**Database:** PrintXpressDB (SQLite)
**Status:** Design documentation implemented in `DatabaseHelper.java`. All eleven tables were created on the API 35 emulator; `PRAGMA foreign_key_check` returned no violations after two orders.

The types and constraints below match `DatabaseHelper.onCreate`. PK means primary key; FK means foreign key. SQLite foreign-key enforcement is enabled in `onConfigure`. Integer identifiers are used and times are stored as consistently formatted TEXT. The password algorithm is encoded in the existing `users.password` value, so the API 24–25 compatibility fix did not change this schema.

## users

**Purpose:** Stores one customer account and login identity.

| Column | SQLite type / constraint | Key |
| --- | --- | --- |
| user_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| full_name | TEXT NOT NULL | |
| email | TEXT NOT NULL COLLATE NOCASE UNIQUE | |
| phone | TEXT NOT NULL | |
| password | TEXT NOT NULL; versioned salted password hash | |

**Relationships:** One user can have many addresses, orders, saved designs, and notifications.

## addresses

**Purpose:** Stores reusable customer delivery addresses.

| Column | SQLite type / constraint | Key |
| --- | --- | --- |
| address_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| user_id | INTEGER NOT NULL | FK → users.user_id |
| address_line | TEXT NOT NULL | |
| city | TEXT NOT NULL | |
| district | TEXT NOT NULL | |
| postal_code | TEXT | |

**Relationships:** Many addresses belong to one user. Orders keep a delivery-address snapshot rather than depending on a mutable address row.

## categories

**Purpose:** Groups print products into the seven agreed categories.

| Column | SQLite type / constraint | Key |
| --- | --- | --- |
| category_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| category_name | TEXT NOT NULL UNIQUE | |

**Relationships:** One category has many products.

## products

**Purpose:** Stores catalogue products and base prices.

| Column | SQLite type / constraint | Key |
| --- | --- | --- |
| product_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| category_id | INTEGER NOT NULL | FK → categories.category_id |
| product_name | TEXT NOT NULL | |
| description | TEXT | |
| base_price | REAL NOT NULL CHECK(base_price >= 0) | |
| image_name | TEXT | |

**Relationships:** One product has many available product_options and can appear in many order_items.

## product_options

**Purpose:** Defines a product's selectable size, material, print side, finish, colour, paper type, or other choice.

| Column | Approved type / constraint | Key |
| --- | --- | --- |
| option_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| product_id | INTEGER NOT NULL | FK → products.product_id |
| option_type | TEXT NOT NULL | |
| option_value | TEXT NOT NULL | |
| price_adjustment | REAL NOT NULL DEFAULT 0 | |

**Relationships:** Many options belong to one product. One option may be selected in many order_item_options. `UNIQUE(product_id, option_type, option_value)` prevents duplicate catalogue choices.

## orders

**Purpose:** Stores an order header, fulfillment choice, current schedule, total, and lifecycle status.

| Column | SQLite type / constraint | Key |
| --- | --- | --- |
| order_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| user_id | INTEGER NOT NULL | FK → users.user_id |
| order_date | TEXT NOT NULL | |
| order_type | TEXT NOT NULL CHECK(order_type IN ('Pickup','Home Delivery')) | |
| delivery_address | TEXT; required by app for Home Delivery | |
| scheduled_for | TEXT NOT NULL | |
| rescheduled_at | TEXT NULL | |
| total_amount | REAL NOT NULL CHECK(total_amount >= 0) | |
| status | TEXT NOT NULL CHECK(approved six statuses); initial value Processing | |

**Relationships:** Many orders belong to one user. One order has one or more order_items. Customer cancellation/rescheduling is allowed only in Processing status. Other statuses: Printing, Ready for Pickup, Out for Delivery, Completed, Cancelled. scheduled_for is updated to the newly selected time on rescheduling; nullable rescheduled_at records when the most recent action happened. Store both in ISO 8601 UTC text (yyyy-MM-ddTHH:mm:ssZ) and convert selected Sri Lankan local times at the UI boundary.

## order_items

**Purpose:** Stores each ordered product, customer content, quantity, and checkout-time unit/subtotal prices.

| Column | Approved type / constraint | Key |
| --- | --- | --- |
| order_item_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| order_id | INTEGER NOT NULL | FK → orders.order_id |
| product_id | INTEGER NOT NULL | FK → products.product_id |
| quantity | INTEGER NOT NULL CHECK(quantity > 0) | |
| custom_text | TEXT | |
| artwork_path | TEXT | |
| unit_price | REAL NOT NULL CHECK(unit_price >= 0) | |
| subtotal | REAL NOT NULL CHECK(subtotal >= 0) | |

**Relationships:** Many items belong to one order; each item refers to one product and can have multiple order_item_options. Size and material are represented by selected option rows, not columns here.

## order_item_options

**Purpose:** Stores choices selected for an order item and the price adjustment at checkout.

| Column | Approved type / constraint | Key |
| --- | --- | --- |
| order_item_option_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| order_item_id | INTEGER NOT NULL | FK → order_items.order_item_id |
| option_id | INTEGER NOT NULL | FK → product_options.option_id |
| price_adjustment | REAL NOT NULL DEFAULT 0 | |

**Relationships:** Each row belongs to one order item and one catalogue option. `UNIQUE(order_item_id, option_id)` prevents recording an identical choice twice. `DatabaseHelper` verifies that `product_options.product_id` matches the related `order_items.product_id` before saving; the two foreign keys do not enforce this cross-table equality.

## saved_designs

**Purpose:** Stores a user's named design reference for reuse.

| Column | SQLite type / constraint | Key |
| --- | --- | --- |
| design_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| user_id | INTEGER NOT NULL | FK → users.user_id |
| design_name | TEXT NOT NULL | |
| file_path | TEXT NOT NULL; likely a persisted content URI | |

**Relationships:** Many saved designs belong to one user.

## notifications

**Purpose:** Stores in-app customer notices and read state.

| Column | SQLite type / constraint | Key |
| --- | --- | --- |
| notification_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| user_id | INTEGER NOT NULL | FK → users.user_id |
| title | TEXT NOT NULL | |
| message | TEXT NOT NULL | |
| created_at | TEXT NOT NULL | |
| is_read | INTEGER NOT NULL DEFAULT 0 CHECK(is_read IN (0,1)) | |

**Relationships:** Many notifications belong to one user. is_read uses 0/1 in SQLite.

## promotions

**Purpose:** Stores informational offers displayed to customers. The discount value is a percentage, not a money amount.

| Column | SQLite type / constraint | Key |
| --- | --- | --- |
| promotion_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| title | TEXT NOT NULL | |
| description | TEXT | |
| discount | REAL NOT NULL; percentage value (10.0 means 10%) | |
| start_date | TEXT NOT NULL | |
| end_date | TEXT NOT NULL | |

**Relationships:** Independent catalogue content with no FK to orders or products. Promotions are display-only in this version; their percentage values do not alter checkout calculations.

## Relationship summary

- users 1 → many addresses, orders, saved_designs, notifications
- categories 1 → many products
- products 1 → many product_options and order_items
- orders 1 → many order_items
- order_items 1 → many order_item_options
- product_options 1 → many order_item_options
- promotions is independent

The order_items ↔ product_options many-to-many selection is resolved by order_item_options. Do not delete referenced catalogue choices or products without an explicit history-preserving policy. Monetary values are in Sri Lankan Rupees (LKR). REAL columns remain for the academic schema, but Java calculations must use BigDecimal and HALF_UP rounding to two decimal places before database persistence and UI display; totals must not be calculated directly with floating-point arithmetic.
