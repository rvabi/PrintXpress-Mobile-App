# Task C — Final User Interface Design

**Status (10 October 2026):** Fifteen approved screens are implemented in the existing Java/XML Android app. The final screen list and real emulator evidence are in screen-specifications.md and screenshot-plan.md. The design_reference PNGs guided the implementation and are not runtime evidence.

## Design approach

PrintXpress uses the approved mint, coral and warm-white palette, rounded cards and controls, tropical decorative imagery and readable dark text. The visual language stays consistent from Splash and authentication through shopping, checkout, orders, profile and support. Product imagery and decorations are reusable approved elements; customer interactions use native Android Views.

## Navigation

Splash checks the local session and opens Login or Home. Registration returns to Login. Home leads to local search, SQLite categories, products, orders, notifications and profile. The order path runs through product detail, dynamic customization, optional artwork selection, one-item order summary, delivery/pickup checkout and confirmation. Order history opens tracking. Profile opens edit profile, addresses, saved designs, notifications and help. Help links to FAQs, printing guidelines and order tracking.

## Interaction and data

The visible Register design has four fields and a phone-number dialog. Login and Register preserve local SQLite authentication and masked passwords with visibility controls. Categories, products and product options use real seeded SQLite rows. Artwork uses Android's document picker and a local persisted URI. Order totals use LKR and BigDecimal; promotions are display-only. Home Delivery requires an address, Store Pickup does not, and both require scheduling. Confirmation, My Orders and notifications display customer-facing #PX order numbers derived from real order IDs.

The 15 main visual screens coexist with functional destinations such as Customize Print, Order Details, Manage Addresses, Saved Designs, Edit Profile and Guidelines & FAQ. No payment gateway, social authentication, cloud upload, push service or live chat is represented as working.

## Verification and limits

All 15 final UI captures exist as non-empty, distinct 1080×2400 API 35 PNGs. Task E records the separate functional regression and test history. Smaller-device and font-scale checks were limited; screen-reader, measured contrast and a broad hardware/device review remain outstanding. The approved screens are frozen for final assignment QA.
