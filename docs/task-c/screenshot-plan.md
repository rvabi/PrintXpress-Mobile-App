# Task C — Final UI Screenshot Evidence

**Audit date:** 10 October 2026. These 15 PNGs are real API 35 emulator captures of the running app, stored in docs/screenshots/final-ui. Each is non-empty, 1080×2400 and has a distinct SHA-256 hash. None has the same hash as a design_reference screen PNG. The design references are comparison material only.

| Screen | Actual emulator evidence |
| --- | --- |
| 01 Splash | screen_01_splash_actual.png |
| 02 Login | screen_02_login_actual.png |
| 03 Register | screen_03_register_actual.png |
| 04 Home | screen_04_home_actual.png |
| 05 Categories | screen_05_categories_actual.png |
| 06 Product Listing | screen_06_product_listing_actual.png |
| 07 Product Detail | screen_07_product_detail_actual.png |
| 08 Upload Artwork | screen_08_upload_artwork_actual.png |
| 09 Cart / Order Summary | screen_09_cart_actual.png |
| 10 Checkout / Fulfilment | screen_10_checkout_actual.png |
| 11 Order Confirmation | screen_11_order_confirmed_actual.png |
| 12 My Orders | screen_12_my_orders_actual.png |
| 13 Notifications | screen_13_notifications_actual.png |
| 14 Profile | screen_14_profile_actual.png |
| 15 Help & Support | screen_15_help_support_actual.png |

The Order Confirmation capture shows #PX000006 in full. My Orders shows #PX000006, #PX000005 and #PX000004 in full. Notifications shows full formatted order IDs. These captures document their own observed app state; a screenshot alone does not prove every action or database rule. Functional test evidence is in Task E.

The earlier 25-screen evidence set and three API-specific authentication captures remain in docs/screenshots as historical verification. They are separate from this final UI series; some earlier captures predate later UI and time-format fixes. No older image is substituted for a final UI capture.
