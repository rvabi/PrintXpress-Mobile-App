# PrintXpress Final Screen Specifications

**Status (10 October 2026):** The following 15 approved visual screens are implemented. They map to the matching screen_01 through screen_15 files in design_reference/screens. Real implementation captures are in docs/screenshots/final-ui. Additional functional destinations appear after these entry screens.

| No. | Final screen | Actual content and behavior |
| --- | --- | --- |
| 01 | Splash | Mint brand composition; routes to Login or Home according to the local session. |
| 02 | Login | Email/password, validation, password visibility, Sign In and Sign Up. Forgot Password is informational; social controls do not authenticate. |
| 03 | Register | Name, email, password and confirmation fields; phone dialog; validation, duplicate-email rejection and return to Login after success. |
| 04 | Home | Brand header, local search, seven seeded category shortcuts, product cards, informational offer and navigation. |
| 05 | Categories | Two-column cards for the seven actual SQLite categories; selection opens the product listing. |
| 06 | Product Listing | SQLite products, category filter chips, LKR starting prices and product navigation. Ratings and discounts are not fabricated. |
| 07 | Product Detail | Product imagery, description, base price, available option overview and Customize Now. |
| 08 | Upload Artwork | Selected filename, Android document picker, change/remove and Save Design controls; local URI reference, not remote upload. |
| 09 | Cart / Order Summary | One draft order item with actual options, quantity control, subtotal and BigDecimal LKR total. Promotions do not alter the total. |
| 10 | Checkout / Fulfilment | Home Delivery or Store Pickup, conditional saved address, scheduling and Place Order. The stepper does not represent a payment gateway. |
| 11 | Order Confirmation | Success state, full generated #PX order number, Track Order and Continue Shopping. |
| 12 | My Orders | Actual SQLite order cards, full #PX numbers, status labels and order-detail navigation. |
| 13 | Notifications | Local order notices with full #PX numbers and display-only promotion information. |
| 14 | Profile | Logged-in name/email, edit profile, saved designs, addresses, orders, notifications, support and logout. |
| 15 | Help & Support | Searchable/support menu to FAQs, printing guidelines and order tracking. Live Chat is marked unavailable; no support backend is claimed. |

## Functional destinations beyond the 15 visual reference screens

Customize Print collects dynamic SQLite options, quantity and custom text and leads to artwork selection and summary. Order Details / Tracking displays persisted items, schedule, fulfillment and status. Cancel and Reschedule are offered only when the current SQLite status is Processing; the database rechecks status before changing it. Manage Addresses, Saved Designs, Edit Profile, and Guidelines & FAQ are additional native View destinations. The app contains one Activity and no Fragment-based navigation.

The navigation path is Splash → Login/Register → Home → Categories/Search → Product Listing → Product Detail → Customize Print → optional Artwork → Cart / Order Summary → Checkout → Confirmation → Tracking or Home. Home also opens Orders, Notifications and Profile; Profile opens the account and support destinations. Registration returns to Login and does not automatically sign in.
