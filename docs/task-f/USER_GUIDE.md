# PrintXpress User Guide

**Version checked:** 8 October 2026. The full customer order flow was checked on a Pixel 6 Android API 35 emulator; registration, login, logout and restart persistence were also checked on Nexus 5X API 24 and API 25 emulators. PrintXpress is a local academic printing-service app for Sri Lanka. Orders, notices and account data are stored on the device; no print shop, payment service or delivery server receives them.

## Start and account

Open PrintXpress. The splash screen sends a signed-out user to Login and a signed-in user to Home. Use **Create an account** to register with full name, valid email, phone number and a password of at least eight characters containing a letter and a number. Confirm the same password. Registration returns to Login; enter the new credentials to sign in. The app keeps a local session until you choose **Logout** in Profile.

## Browse and customize

On Home, search by product name or description or tap **Browse categories**. The catalogue has Business Cards, Flyers, Posters, Banners, Stickers, T-Shirts and Mugs. Open a product to see its starting LKR price and available choices, then tap **Customize print**. Select one value for each displayed option type, enter a quantity of at least one, and supply custom text or artwork. The estimated total updates when quantity or choices change. Prices are shown as `LKR 1,500.00`.

**Select or change artwork** opens Android's document picker for an image or PDF. The selected filename is shown; you can change or remove it on the Artwork Selection screen. **Save this design** stores a named local reference that appears under Profile → Saved designs. The file remains on the device; the app does not upload it.

## Checkout

Tap **Review order** and check the product, options, quantity, text/artwork and total. Promotions are informational and do not reduce the amount. Continue to **Pickup or delivery**:

- **Pickup:** choose a future Sri Lankan date/time; no address is needed.
- **Home Delivery:** choose a saved address and a future date/time. Use **Add address** if the list is empty.

Tap **Place order** only when the summary and schedule are correct. A successful save opens Order Confirmation with a real local order number. A failure stays in checkout and does not show confirmation.

## Orders and tracking

Use the **Orders** tab to see history, then **View details** to see the current status, selected item choices, schedule and total. Statuses are Processing, Printing, Ready for Pickup, Out for Delivery, Completed and Cancelled. **Reschedule** and **Cancel order** are available only while the current status is Processing. The app checks the saved status again before accepting either action. The tracking display is a local order-status view, not live courier tracking.

## Profile and information

The **Notices** tab lists local order notices and display-only promotions. **Profile** shows your account information and links to Edit profile, Manage addresses, Saved designs, and Print guidelines & FAQ. Address changes do not rewrite the address snapshot on an older order. Choose **Logout** to remove the local sign-in state; logging in again restores the same local records.

## Limits of this version

Only one local device database is used. There is no payment integration, remote artwork upload, push notification, print-shop connection, live order fulfillment or in-app support messaging. Keep selected artwork available in the Android document provider if you want to reuse its saved URI.
