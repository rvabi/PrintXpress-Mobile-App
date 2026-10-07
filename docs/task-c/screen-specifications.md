# PrintXpress Screen Specifications

**Status:** Planned interface only. Nineteen destinations are specified below; no Android screen has been implemented or captured. The screen names in this document are the naming reference for Task C.

## Shared rules

The four main bottom-navigation destinations are Home, My Orders, Notifications / Offers, and Profile. Secondary destinations use a toolbar back action. Forms keep entered data when validation fails. All displayed monetary amounts are LKR with two decimal places. Product choices come from product_options; customer cancellation and rescheduling appear only for Processing orders. Promotions show percentage offers but never change checkout totals in this version.

## 1. Splash Screen

**Purpose:** Brief branding while deciding where to route the customer.

- Show PrintXpress name/logo placeholder and a short tagline; a small loading indicator is optional.
- Route to Login when no valid local session exists, or Home when a valid session exists. This does not imply automatic login after registration.
- Keep the screen brief and accessible; do not require a tap to continue.

## 2. Login Screen

**Purpose:** Authenticate an existing customer.

- PrintXpress title, labelled email and password fields, show/hide password control, Login button and Register link.
- Show required-field and email-format errors beside fields; show invalid-credentials feedback without exposing account details.
- Successful login would open Home; the password entry should be concealed by default.

## 3. Register Screen

**Purpose:** Create a customer account.

- Labelled full name, email, phone, password and confirm-password fields; Register button and Login link.
- Show errors for missing/invalid values, password requirements, mismatch and duplicate email.
- Successful registration is planned to show confirmation and open Login, with no automatic login assumed.

## 4. Home Screen

**Purpose:** Give a quick route to products and current information.

- Greeting, local catalogue search entry, one informational promotion card, category shortcuts, featured/popular product cards, an embedded sample-design section and print-guidelines shortcut.
- Bottom navigation: Home, Orders, Notifications, Profile. Product search would filter/navigate to the local product list; no network search is implied.
- Keep the first view focused; longer content scrolls.

## 5. Product Categories Screen

**Purpose:** Browse all seven categories.

- RecyclerView/grid-style cards for Business Cards, Flyers, Posters, Banners, Stickers, T-Shirts and Mugs.
- Each card shows a clear name and simple icon/image placeholder; selection opens Product List for that category.
- An empty/error state is planned if local seed data cannot be read.

## 6. Product List Screen

**Purpose:** Compare products within the chosen category.

- Category title and scrollable product cards with image/placeholder, product name, short description, starting LKR price and View Details action.
- Show a useful empty state if no products exist in that category.
- The starting price is the base price; selected options can change the later unit price.

## 7. Product Details Screen

**Purpose:** Explain one product before customization.

- Product image/placeholder, name, description, base price, available option overview and relevant size/material/specification information.
- Customize / Continue is the main action; back returns to the product list.
- Do not display unavailable option values as selectable.

## 8. Customize Print Screen

**Purpose:** Collect an order item's print choices.

- Product summary, quantity input, relevant choice controls populated logically from product_options, optional/required custom-text field, artwork-selection action, and current calculated LKR price.
- Clearly mark which options or content are required for the selected product; quantity must be greater than zero.
- Continue is enabled only after validation. The planned Java price calculation uses BigDecimal and two-decimal rounding.

## 9. Artwork Selection Screen

**Purpose:** Let the customer choose and review an artwork reference.

- States: no artwork selected, artwork selected with a readable file name, and remove/change selection.
- The future implementation will use Android's file picker. Display file-type and quality guidance only after picker filters and supported handling are implemented and tested.
- Do not imply upload to a remote print shop; this version stores a local content reference.

## 10. Order Summary Screen

**Purpose:** Let the customer check the intended purchase before fulfillment.

- Selected product, options, quantity, custom-text excerpt/artwork name, unit price, subtotal, total, and Continue to Delivery action.
- Provide Edit customization navigation. Show LKR with two decimal places.
- Informational promotions may be viewed elsewhere but are not deducted from totals.

## 11. Pickup / Delivery Screen

**Purpose:** Select fulfillment and schedule the order.

- Radio choice for Pickup or Home Delivery, plus scheduled date/time selection.
- Home Delivery reveals saved-address selection and Add Address; an address is required before continuing.
- Pickup hides address fields and does not require an address. Validate the selected schedule before placing the order.

## 12. Order Confirmation Screen

**Purpose:** Confirm a successfully saved order.

- Success state, order ID, order type, scheduled date/time and LKR total; View My Orders and Back to Home actions.
- Display only after the order, items and selected options have been saved together successfully.
- A failed save must show an error instead of a success screen.

## 13. My Orders Screen

**Purpose:** Browse the customer's own order history.

- Scrollable order cards showing order ID, date, total, current status and View Details.
- If no orders exist, show a plain explanation and Browse Products action; never show fake order data.
- Orders is one of the four bottom-navigation destinations.

## 14. Order Details / Tracking Screen

**Purpose:** Show the order contents and current local workflow status.

- Order ID/date/type, items and selected options, total, scheduled time, current status and a simple labelled status progression.
- Pickup path: Processing → Printing → Ready for Pickup → Completed. Delivery path: Processing → Printing → Out for Delivery → Completed. Cancelled is a terminal exception rather than a completed step.
- Show Reschedule and Cancel only while the status is Processing. Recheck status before saving either change. This is local status display, not live courier tracking.

## 15. Notifications / Offers Screen

**Purpose:** Present order notices and general promotions.

- Separate cards/sections for order notifications and promotional offers; useful empty states.
- Promotions display a percentage value, such as 10% for promotions.discount = 10.0, and remain informational only.
- Notifications is one of the four bottom-navigation destinations.

## 16. Profile Screen

**Purpose:** Give access to customer data and support destinations.

- Name, email, phone, Edit Profile, Manage Addresses, Saved Designs, Print Guidelines, FAQ / Support and Logout.
- Editing can occur in this screen or a small dialog without adding another planned destination.
- Validate changed fields before saving and confirm logout navigation to Login.

## 17. Manage Addresses Screen

**Purpose:** Maintain reusable delivery locations.

- Address list with Add, Edit and Delete actions; labelled address line, city, district and postal-code inputs.
- Validate required address information and confirm deletion; do not remove an address silently.
- Changes to a saved address must not rewrite the delivery-address snapshot in past orders.

## 18. Saved Designs Screen

**Purpose:** Show artwork references previously saved by the customer.

- Saved design name and file reference, with View/Open and Delete actions where the stored file remains accessible.
- Show an unavailable-file message if a reference can no longer be opened; do not promise a preview before it is implemented.
- Provide an empty state that explains designs can be saved during customization.

## 19. Print Guidelines / FAQ Screen

**Purpose:** Provide concise printing and support information.

- Sections for general artwork quality, colour and bleed-margin guidance, followed by common questions and support information.
- Label guidance as general advice. Exact accepted formats, colour profiles and bleed measurements must be confirmed before being presented as print-shop requirements.
- Keep content scrollable, readable and easy to return from.
