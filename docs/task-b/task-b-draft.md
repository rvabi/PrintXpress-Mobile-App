# Task B – System and Database Design

**Status:** Draft design explanation. The Android application and SQLite database have not yet been implemented.

## 1. Introduction

PrintXpress is planned as a customer-facing native Android application for a Sri Lankan printing service. Its design connects product browsing, customization, pickup or delivery, and order tracking. The UML diagrams and relational schema describe these requirements before Java implementation.

## 2. Use Case Design

The Customer is the only actor; no administrator interface is in scope. Use cases cover accounts, products and designs, orders, and support information. Place Order includes Customize Product, Review Order Summary and Select Pickup or Home Delivery because checkout requires all three. Customize Product includes Select Product Options. Upload Artwork and Enter Custom Text extend customization conditionally; neither applies to every product. Reschedule Order and Cancel Order extend tracking only while status is Processing. Login is treated as a prerequisite for personal actions rather than repeated as an included use case.

## 3. Class Diagram

The domain classes correspond to the eleven planned tables. User is associated with Address, Order, SavedDesign and Notification. Category groups Product; Product offers ProductOption choices and can occur in many OrderItem records. Each Order contains at least one OrderItem, whose selected choices are represented by OrderItemOption. Promotion remains independent because only general offers are planned. Java money values will use BigDecimal, although SQLite columns remain REAL. DatabaseHelper shows representative responsibilities for schema creation, authentication, queries and transactional order changes, without dominating the domain model.

## 4. Activity Diagrams

Registration validates required fields, email, phone, password and confirmation. Errors or an existing email return the customer to correction. A valid user would be saved with a password hash, then directed to Login; automatic login is not assumed. Order placement validates quantity, required options and customer content before price calculation and summary review. Home Delivery requires an address; Pickup does not. A scheduled time is mandatory. One SQLite transaction would create the Order, its OrderItem rows and selected options, rolling back if any save fails. The separate management activity begins with status viewing. It offers rescheduling, cancellation or no change only for Processing orders and rechecks status immediately before an update.

## 5. ER Diagram

Users have one-to-many links to addresses, orders, saved designs and notifications. Categories contain products; products offer options and may appear in many order items. An order has one or more items. Each item may select multiple options through order_item_options. Foreign keys protect referenced rows, but cannot ensure that a selected option belongs to the item's product; application logic must check this. Promotions remain independent, display-only percentage offers without order-level redemption.

## 6. Database Normalization

The worked example begins with an order containing repeated product entries and multi-valued options. First Normal Form gives each option an atomic value and its own row. Second Normal Form separates facts dependent on the order or its line from a composite order-line-option record. Third Normal Form moves category and catalogue option descriptions behind their own keys. Reusable addresses are separate from users. Delivery address and checkout prices remain deliberate snapshots so later profile or catalogue changes do not alter order history.

## 7. Relational Schema

The eleven-table proposal is listed in normalization.md and specified in schema.md. Integer primary keys identify rows; foreign keys connect dependent records. Email and category name have the stated uniqueness rules. scheduled_for is NOT NULL, while rescheduled_at is nullable. Foreign keys must be enabled, and an order with at least one item must be saved transactionally. These are design requirements, not evidence of a built database.

## 8. Design Decisions and Justification

The customer-only scope matches the requested mobile workflows. ProductOption allows different products to offer sizes, finishes and materials without fixed columns in OrderItem. OrderItemOption records each selection and its checkout-time price adjustment. A separate Address table supports several saved locations; the order keeps the chosen delivery address as a historical snapshot. SQLite provides local relational storage for an offline academic demonstration. Only a Processing order may be cancelled or rescheduled; the latter updates scheduled_for and records the latest action in rescheduled_at. Option ownership validation prevents choices from another product being attached to an item. LKR totals will use BigDecimal and two-decimal rounding.

## 9. Conclusion

The diagrams and schema form a consistent plan for PrintXpress interactions and data. They keep validation, option selection and order-status rules explicit. Implementation, rendered-diagram review and testing are still required before functionality can be reported as complete.
