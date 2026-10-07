# PrintXpress Database Normalization

**Status:** Original Task B design explanation. This is a proposed schema, not evidence of an implemented database.

## Unnormalized order record

Imagine one PrintXpress order form stored as a single record:

    Order 42; customer: N. Perera, n@example.com; delivery: Colombo;
    item 1: 100 Business Cards, [Size=Standard, Finish=Matte], artwork URI;
    item 2: 2 Posters, [Size=A3, Material=Glossy], custom text;
    status: Processing

The item list and each item's option list are repeating groups. A single cell containing several items or comma-separated choices is hard to query and update. Customer contact data, product descriptions, and category names would also be copied into every order form.

## First Normal Form (1NF)

Give each stored value one meaning and separate repeating groups into rows. For illustration, an early flat selection table could have one row per option chosen for an order line, identified by (order_id, line_number, option_number). It would contain order date, customer details, product details, item quantity, option type/value, and prices as separate columns.

This removes comma-separated items and options, but repeats order and item facts across several option rows. A product with two selected options creates two rows for its one ordered item. Use a stable order_item_id in the eventual schema so two lines for the same product remain distinct.

## Second Normal Form (2NF)

In the illustrative 1NF key (order_id, line_number, option_number):

- order_date, user_id, status, and delivery choice depend on order_id alone;
- product_id, quantity, custom text, and artwork depend on (order_id, line_number);
- the selected option depends on the whole selection key.

Move order facts to orders, item facts to order_items, and chosen option facts to order_item_options. Each new table has a primary key; its non-key facts describe that row's entity. Customer addresses and saved designs are separate reusable records rather than repeated order-line fields.

## Third Normal Form (3NF)

Remove facts that depend on other non-key facts:

- user_id determines a user's name, email, phone, and password hash, so those belong in users rather than orders;
- category_id determines category_name, so category data belongs in categories rather than products;
- product_id determines product name, description, base price, and category link, so these belong in products rather than order_items;
- option_id determines its product, type, value, and current catalogue adjustment, so these belong in product_options rather than being repeated as free text for every order item.

addresses, saved_designs, and notifications each depend on their own primary key and reference users. promotions is independent. Foreign keys represent the relationships without duplicating the parent record's descriptive fields.

## Final relational schema

- users(user_id PK, full_name, email, phone, password)
- addresses(address_id PK, user_id FK, address_line, city, district, postal_code)
- categories(category_id PK, category_name)
- products(product_id PK, category_id FK, product_name, description, base_price, image_name)
- product_options(option_id PK, product_id FK, option_type, option_value, price_adjustment)
- orders(order_id PK, user_id FK, order_date, order_type, delivery_address, scheduled_for, rescheduled_at, total_amount, status)
- order_items(order_item_id PK, order_id FK, product_id FK, quantity, custom_text, artwork_path, unit_price, subtotal)
- order_item_options(order_item_option_id PK, order_item_id FK, option_id FK, price_adjustment)
- saved_designs(design_id PK, user_id FK, design_name, file_path)
- notifications(notification_id PK, user_id FK, title, message, created_at, is_read)
- promotions(promotion_id PK, title, description, discount, start_date, end_date)

The master data and relationship tables avoid repeating groups and partial/transitive dependencies. A few order fields are deliberate historical snapshots rather than a claim of perfect mathematical 3NF: orders.delivery_address preserves the address used at checkout; order_items.unit_price and order_item_options.price_adjustment preserve agreed prices; item subtotal and order total preserve displayed totals. Store and update these values together in one transaction, then test that they remain consistent. The selected options are normalized through order_item_options; there are no fixed size or material columns in order_items.

See schema.md for field types and all foreign keys.
