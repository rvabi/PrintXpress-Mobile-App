# PrintXpress Proposed Relational Schema

**Database:** PrintXpressDB (SQLite)
**Status:** Design documentation; tables have not been created or tested.

The type and nullability choices below are proposed for implementation. PK means primary key; FK means foreign key. Enable SQLite foreign-key enforcement. Use integer identifiers and store times as consistently formatted TEXT. The required approved option and order-item columns are retained exactly.

## users

**Purpose:** Stores one customer account and login identity.

| Column | Proposed type / constraint | Key |
| --- | --- | --- |
| user_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| full_name | TEXT NOT NULL | |
| email | TEXT NOT NULL UNIQUE | |
| phone | TEXT NOT NULL | |
| password | TEXT NOT NULL; contains a password hash | |

**Relationships:** One user can have many addresses, orders, saved designs, and notifications.

## addresses

**Purpose:** Stores reusable customer delivery addresses.

| Column | Proposed type / constraint | Key |
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

| Column | Proposed type / constraint | Key |
| --- | --- | --- |
| category_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| category_name | TEXT NOT NULL UNIQUE | |

**Relationships:** One category has many products.

## products

**Purpose:** Stores catalogue products and base prices.

| Column | Proposed type / constraint | Key |
| --- | --- | --- |
| product_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| category_id | INTEGER NOT NULL | FK → categories.category_id |
| product_name | TEXT NOT NULL | |
| description | TEXT | |
| base_price | REAL NOT NULL | |
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

**Relationships:** Many options belong to one product. One option may be selected in many order_item_options. A uniqueness rule on product_id + option_type + option_value is proposed to prevent duplicate catalogue choices.

## orders

**Purpose:** Stores an order header, fulfillment choice, current schedule, total, and lifecycle status.

| Column | Proposed type / constraint | Key |
| --- | --- | --- |
| order_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| user_id | INTEGER NOT NULL | FK → users.user_id |
| order_date | TEXT NOT NULL | |
| order_type | TEXT NOT NULL; Pickup or Home Delivery | |
| delivery_address | TEXT; required by app for Home Delivery | |
| scheduled_for | TEXT; required by app at checkout | |
| rescheduled_at | TEXT NULL | |
| total_amount | REAL NOT NULL | |
| status | TEXT NOT NULL; initial value Processing | |

**Relationships:** Many orders belong to one user. One order has one or more order_items. Customer cancellation/rescheduling is allowed only in Processing status. Other statuses: Printing, Ready for Pickup, Out for Delivery, Completed, Cancelled.

## order_items

**Purpose:** Stores each ordered product, customer content, quantity, and checkout-time unit/subtotal prices.

| Column | Approved type / constraint | Key |
| --- | --- | --- |
| order_item_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| order_id | INTEGER NOT NULL | FK → orders.order_id |
| product_id | INTEGER NOT NULL | FK → products.product_id |
| quantity | INTEGER NOT NULL | |
| custom_text | TEXT | |
| artwork_path | TEXT | |
| unit_price | REAL NOT NULL | |
| subtotal | REAL NOT NULL | |

**Relationships:** Many items belong to one order; each item refers to one product and can have multiple order_item_options. Size and material are represented by selected option rows, not columns here.

## order_item_options

**Purpose:** Stores choices selected for an order item and the price adjustment at checkout.

| Column | Approved type / constraint | Key |
| --- | --- | --- |
| order_item_option_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| order_item_id | INTEGER NOT NULL | FK → order_items.order_item_id |
| option_id | INTEGER NOT NULL | FK → product_options.option_id |
| price_adjustment | REAL NOT NULL DEFAULT 0 | |

**Relationships:** Each row belongs to one order item and one catalogue option. A unique pair of order_item_id + option_id is proposed to prevent recording an identical choice twice. The application must verify that the option belongs to the order item's product.

## saved_designs

**Purpose:** Stores a user's named design reference for reuse.

| Column | Proposed type / constraint | Key |
| --- | --- | --- |
| design_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| user_id | INTEGER NOT NULL | FK → users.user_id |
| design_name | TEXT NOT NULL | |
| file_path | TEXT NOT NULL; likely a persisted content URI | |

**Relationships:** Many saved designs belong to one user.

## notifications

**Purpose:** Stores in-app customer notices and read state.

| Column | Proposed type / constraint | Key |
| --- | --- | --- |
| notification_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| user_id | INTEGER NOT NULL | FK → users.user_id |
| title | TEXT NOT NULL | |
| message | TEXT NOT NULL | |
| created_at | TEXT NOT NULL | |
| is_read | INTEGER NOT NULL DEFAULT 0 | |

**Relationships:** Many notifications belong to one user. is_read uses 0/1 in SQLite.

## promotions

**Purpose:** Stores offers displayed to customers.

| Column | Proposed type / constraint | Key |
| --- | --- | --- |
| promotion_id | INTEGER PRIMARY KEY AUTOINCREMENT | PK |
| title | TEXT NOT NULL | |
| description | TEXT | |
| discount | REAL NOT NULL | |
| start_date | TEXT NOT NULL | |
| end_date | TEXT NOT NULL | |

**Relationships:** Independent catalogue content; it currently has no FK to orders or products. Whether discount means percent or fixed Sri Lankan rupees must be decided before redemption logic is added.

## Relationship summary

- users 1 → many addresses, orders, saved_designs, notifications
- categories 1 → many products
- products 1 → many product_options and order_items
- orders 1 → many order_items
- order_items 1 → many order_item_options
- product_options 1 → many order_item_options
- promotions is independent

The order_items ↔ product_options many-to-many selection is resolved by order_item_options. Do not delete referenced catalogue choices or products without an explicit history-preserving policy.
