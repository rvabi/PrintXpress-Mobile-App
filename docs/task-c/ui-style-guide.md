# PrintXpress UI Style Guide

**Final UI status (10 October 2026):** Fifteen approved customer-facing screens are implemented with native Android Java and XML Views. Their real API 35 captures are indexed in the screenshot plan. The supplied design_reference PNGs are visual specifications, not implementation evidence.

## Visual language

The app uses soft mint headers, coral primary actions, warm white backgrounds, rounded white cards, subtle shadows and tropical decorative imagery. Decorative leaves and product illustrations come from approved reusable assets. Buttons, fields, chips, navigation and order cards remain Android Views.

| Role | Approved colour |
| --- | --- |
| Primary mint | #A8DDD7 |
| Dark mint | #73C7BF |
| Coral accent | #F47F7A |
| Soft coral | #F3A6A6 |
| Light peach | #F8C9C3 |
| Background | #FFFDFC |
| Surface | #FFFFFF |
| Light surface | #F5F7F7 |
| Primary text | #2F2F2F |
| Secondary text | #6B7280 |
| Border | #DDE5E5 |
| Success | #57B894 |
| Warning | #F2B84B |
| Error | #E85D5D |

The approved screens use a clear heading, supporting text, generous spacing and a prominent coral action where a task needs one. LKR prices and order statuses are written as text. Order numbers use the customer-facing #PX six-digit format on confirmation, My Orders and notifications.

## Implemented components and layout

- Smooth mint-to-white header shapes, branded wordmark, tropical leaves and approved product artwork provide a consistent screen family.
- Login and Register use rounded native EditTexts. Passwords start concealed and have working visibility controls. Registration collects the phone number in a second dialog after its four visible fields.
- Categories and products use rounded image cards populated from the local SQLite catalogue. Product options in customization come from product_options.
- The order summary has a native quantity control and a BigDecimal-based LKR total. Promotions are informational only.
- Checkout uses native delivery/pickup controls, saved addresses and Android date/time pickers. Confirmation displays the generated order number.
- Orders, notifications, profile and support use scrollable cards with text labels. Social icons, live chat, payment and remote upload are not working services.

Layouts use ScrollView or scrollable content where needed; images retain their aspect ratio. The final 15 screen captures were made on API 35 at 1080×2400. API 24 and API 25 launch, authentication and basic catalogue navigation were also checked. A previous smaller-screen and 1.3 font-scale review covered Home and Register; screen-reader, automated contrast and broad device-matrix checks remain open. No general accessibility compliance claim is made.
