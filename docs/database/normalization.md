# PrintXpress Database Normalization

**Status:** Task B normalization explanation. The worked order 42 example remains hypothetical; the described eleven-table SQLite database is now implemented and was exercised with real emulator orders.

## Worked example: unnormalized form (UNF)

Suppose order 42 belongs to customer 7 and contains 100 Business Cards and two Posters. A single order-form record might have OrderID, CustomerID, CustomerName, CustomerEmail, CustomerPhone, DeliveryAddress, OrderTotal, and a repeating collection of products. Each product entry holds ProductID, ProductName, CategoryName, Quantity, UnitPrice, Subtotal, and SelectedOptions. The business cards might contain [Size: Standard, Finish: Matte]; the posters might contain [Size: A3, Material: Glossy].

This is unnormalized because product entries repeat inside one order record and SelectedOptions holds several values. Searching for all A3 orders or changing a category name becomes unreliable when values are embedded in lists. Repeating customer and product descriptions across orders also creates update anomalies.

## First Normal Form (1NF)

Make every value atomic and give each row a key. For this illustration, flatten the order into one row per selected option, with composite key (order_id, line_no, option_no). An option is split into option_type and option_value rather than stored as a comma-separated list. Product IDs 101 and 202 below are hypothetical.

| order_id | line_no | option_no | product_id | option_type | option_value |
| --- | --- | --- | --- | --- | --- |
| 42 | 1 | 1 | 101 | Size | Standard |
| 42 | 1 | 2 | 101 | Finish | Matte |
| 42 | 2 | 1 | 202 | Size | A3 |
| 42 | 2 | 2 | 202 | Material | Glossy |

The omitted order, customer, item-price and product-description columns would still repeat across these rows. The example items each have options; in the final schema an item with no selected options has zero order_item_options rows. line_no distinguishes two lines for the same product. The final design uses order_item_id and order_item_option_id instead of these illustrative line and option numbers.

## Second Normal Form (2NF)

In that 1NF composite key, order_date, customer_id, delivery choice and OrderTotal depend only on order_id. ProductID, Quantity, custom content, UnitPrice and Subtotal depend on (order_id, line_no). The chosen option depends on the whole selection key. These partial dependencies mean the flat table is not in 2NF.

Separate the order header into orders, ordered lines into order_items, and selections into order_item_options. Product descriptions and base prices are moved to products, while customer name, email and phone are moved to users; they must not be copied into each selection row. The order item retains transactional quantity, custom text, artwork reference, agreed unit price and subtotal. This decomposition also prepares the further transitive-dependency checks in 3NF.

## Third Normal Form (3NF)

A non-key field must not determine another non-key field in its table. For example, if CategoryName were kept in products, product_id would lead through category_id to CategoryName. Put CategoryName in categories and retain category_id as a foreign key in products. If an option's type, value and current price adjustment were copied into order_item_options, option_id would determine those catalogue facts. Store them once in product_options and retain option_id in the selected-option table.

A user can have several reusable addresses, so addresses is a separate table linked to users rather than multiple address columns in users. saved_designs and notifications also depend on their own identifiers and link to users. promotions is independent. Order delivery_address deliberately records the address used at checkout; changing a saved address must not rewrite historical orders. Similarly, unit_price, subtotal, total_amount and the selected price_adjustment are controlled order-time price snapshots. These are intentional historical redundancies, so the final design should not be presented as perfectly redundancy-free 3NF.

## Final normalized relational schema

Uppercase table labels below are presentation style; the implemented SQLite identifiers in schema.md use lowercase. PK means primary key, FK means foreign key. The complete NOT NULL, UNIQUE and CHECK constraints are specified in schema.md and `DatabaseHelper.onCreate`.

    USERS(
      user_id PK,
      full_name NOT NULL,
      email NOT NULL UNIQUE,
      phone NOT NULL,
      password NOT NULL
    )

    ADDRESSES(
      address_id PK,
      user_id NOT NULL FK → USERS.user_id,
      address_line NOT NULL,
      city NOT NULL,
      district NOT NULL,
      postal_code
    )

    CATEGORIES(
      category_id PK,
      category_name NOT NULL UNIQUE
    )

    PRODUCTS(
      product_id PK,
      category_id NOT NULL FK → CATEGORIES.category_id,
      product_name NOT NULL,
      description,
      base_price NOT NULL,
      image_name
    )

    PRODUCT_OPTIONS(
      option_id PK,
      product_id NOT NULL FK → PRODUCTS.product_id,
      option_type NOT NULL,
      option_value NOT NULL,
      price_adjustment NOT NULL DEFAULT 0
    )

    ORDERS(
      order_id PK,
      user_id NOT NULL FK → USERS.user_id,
      order_date NOT NULL,
      order_type NOT NULL,
      delivery_address,
      scheduled_for NOT NULL,
      rescheduled_at NULL,
      total_amount NOT NULL,
      status NOT NULL
    )

    ORDER_ITEMS(
      order_item_id PK,
      order_id NOT NULL FK → ORDERS.order_id,
      product_id NOT NULL FK → PRODUCTS.product_id,
      quantity NOT NULL,
      custom_text,
      artwork_path,
      unit_price NOT NULL,
      subtotal NOT NULL
    )

    ORDER_ITEM_OPTIONS(
      order_item_option_id PK,
      order_item_id NOT NULL FK → ORDER_ITEMS.order_item_id,
      option_id NOT NULL FK → PRODUCT_OPTIONS.option_id,
      price_adjustment NOT NULL DEFAULT 0
    )

    SAVED_DESIGNS(
      design_id PK,
      user_id NOT NULL FK → USERS.user_id,
      design_name NOT NULL,
      file_path NOT NULL
    )

    NOTIFICATIONS(
      notification_id PK,
      user_id NOT NULL FK → USERS.user_id,
      title NOT NULL,
      message NOT NULL,
      created_at NOT NULL,
      is_read NOT NULL DEFAULT 0
    )

    PROMOTIONS(
      promotion_id PK,
      title NOT NULL,
      description,
      discount NOT NULL,
      start_date NOT NULL,
      end_date NOT NULL
    )

Every order is created with at least one order item in one transaction; a foreign key alone cannot enforce that minimum. `DatabaseHelper` also verifies that each selected `product_options.product_id` equals its `order_items.product_id`. Customer cancellation and rescheduling are permitted only while the current status is Processing. `scheduled_for` is mandatory, while `rescheduled_at` remains nullable and records the most recent reschedule action. Monetary values are LKR; Java calculates with BigDecimal and rounds to two decimal places before writing the approved SQLite REAL fields or displaying values.
