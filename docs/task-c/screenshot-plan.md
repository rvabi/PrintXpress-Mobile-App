# PrintXpress Task C Screenshot Evidence Plan

**Status:** 28 real screenshots captured with `adb shell screencap -p` on 8 October 2026: 25 original Pixel 6 API 35 captures plus one authentication capture each on API 24, API 25 and API 35. Files are stored under `docs/screenshots/`. Each file shows a distinct running-app state. The order confirmation, history and tracking captures were taken before the later UTC-to-Sri-Lanka schedule display fix, so their timestamps show the older UTC presentation.

| ID | Screen | Purpose / evidence | Feature demonstrated | Capture status |
| --- | --- | --- | --- | --- |
| TC-UI-01 | Login | Show account entry | Email/password controls and Register navigation | `01-login.png` |
| TC-UI-02 | Registration | Show customer form | Name, email, phone, password and confirmation | `02-register.png` |
| TC-UI-03 | Home | Show main visual hierarchy | Greeting, search, categories, offer and four tabs | `03-home.png` |
| TC-UI-04 | Categories | Show category browsing | Scrollable seeded categories | `04-categories.png` |
| TC-UI-05 | Product List | Show a category product | Name, description and starting LKR price | `05-product-list.png` |
| TC-UI-06 | Product Details | Show product information | Description, base price, options and Customize action | `06-product-details.png` |
| TC-UI-07 | Customize Product | Show print selection form | Options, text and calculated price | `07-customization-priced.png` |
| TC-UI-08 | Artwork Selected | Show selected local file | File name and change/remove actions | `08-artwork-selected.png` |
| TC-UI-09 | Order Summary | Show pre-checkout review | Selected options and LKR total | `09-order-summary.png` |
| TC-UI-10 | Delivery/Pickup | Show conditional fields | Pickup and Home Delivery address state | `10-pickup-delivery.png`, `10-home-delivery.png` |
| TC-UI-11 | Order Confirmation | Show database save result | Order #1 and total | `11-order-confirmation.png` |
| TC-UI-12 | My Orders | Show history | Real order card and status | `12-my-orders.png` |
| TC-UI-13 | Order Tracking | Show order details | Items, selected options and Processing actions | `13-order-tracking.png` |
| TC-UI-14 | Notifications/Offers | Show information | Real order notices and display-only offer | `14-notifications.png` |
| TC-UI-15 | Profile | Show account links | Customer details and support entries | `15-profile.png` |

## Additional captured states

| File | Evidence shown |
| --- | --- |
| `07-customization.png` | Default customization choices before priced selections |
| `validation-required-registration.png` | Required registration field error |
| `validation-invalid-email.png` | Invalid email field error |
| `validation-short-password.png` | Short password rejected on registration |
| `validation-duplicate-email.png` | Duplicate email registration attempt |
| `validation-invalid-login.png` | Incorrect login rejected |
| `validation-schedule-required.png` | Checkout without a schedule rejected |
| `validation-missing-content.png` | Customization without text or artwork rejected |
| `validation-quantity-zero.png` | Quantity zero rejected |
| `api25-auth-home.png` | API 25 Home after registering and signing in as API25 Tester, recaptured after an unrelated emulator Launcher dialog had appeared |
| `api35-regression-home.png` | API 35 Home after registering and signing in as API35 Regression |
| `api24-auth-home.png` | API 24 Home after registering, signing in and restarting as API24 Tester |

The profile capture shows the original Test User before the later profile edit; the edit result was checked in the emulator and SQLite, not in that PNG. The Login capture shows the destination after splash routing, not the transient splash itself. No image is reused as evidence for a different screen.
