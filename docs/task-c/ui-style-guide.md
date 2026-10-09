# PrintXpress UI Style Guide

**Status:** Approved mint/coral native Android Views design. XML layouts and real API 24/API 25/API 35 emulator captures are in `app/src/main/res/layout/` and `docs/screenshots/`. An initial smaller-device and 1.3 font-scale review was completed on API 25; a screen-reader and broader device review remain open.

## 1. Design direction

PrintXpress should feel clear, professional and friendly. Soft mint surfaces and coral accents identify actions while white cards keep product details readable. Screens should present one primary action at a time, use consistent labels and support common phone sizes through scrolling and flexible widths.

## 2. Colour palette

| Role | Hex | Intended use |
| --- | --- | --- |
| Primary mint | #A8DDD7 | Navigation and soft accents |
| Dark mint | #73C7BF | Primary buttons |
| Coral accent | #F47F7A | Secondary links and highlights |
| Soft coral | #F3A6A6 | Optional gentle accents |
| Light peach | #F8C9C3 | Optional promotional accents |
| Background | #FFFDFC | Screen background |
| Surface/card | #FFFFFF | Cards, forms, dialogs |
| Light surface | #F5F7F7 | Optional alternate surface |
| Primary text | #2F2F2F | Main text on light surfaces |
| Secondary text | #6B7280 | Captions and supporting text |
| Border | #DDE5E5 | Card outlines |
| Success | #57B894 | Confirmation |
| Warning | #F2B84B | Caution |
| Error | #E85D5D | Validation and cancellation |

Use dark text on the light mint buttons; use text labels for status and errors. The previous blue/gold contrast figures are obsolete. The API 25 font-scale check found Home readable and Register scrollable; automated contrast measurement and screen-reader behavior have not been verified.

## 3. Typography

Use Android's system sans-serif family; no custom font file is required. Sizes are starting values in sp and may adjust after device testing.

| Role | Approximate size and weight |
| --- | --- |
| Screen title | 24sp, bold |
| Section heading | 20sp, semibold |
| Product title | 18sp, semibold |
| Body text and input | 16sp, regular |
| Caption or supporting label | 14sp, regular |
| Price | 20sp, bold |
| Button text | 16sp, semibold |

Support user font scaling. Avoid putting important instructions in tiny captions. Prices should show LKR and two decimal places, such as LKR 1,250.00.

## 4. Spacing and layout

Use a 4dp base scale: 4dp for tight icon gaps, 8dp for related labels, 12dp for compact card content, 16dp for normal screen padding, 24dp between sections, and 32dp for major separation. Cards may use about 12dp corner radius and subtle elevation or a light border. Avoid fixed full-screen heights; place long forms in a ScrollView and let lists scroll independently. Keep key actions visible near the end of each task.

## 5. Reusable components

- **Buttons:** One filled primary action per screen. Secondary actions use an outlined button; low-priority links use text. Disabled actions look disabled and cannot be tapped. Use descriptive labels such as Continue to Delivery rather than Next.
- **Text fields:** Persistent visible labels, clear examples, a comfortable input height, and field-specific errors. Passwords are concealed by default; a show/hide control is still pending.
- **Cards:** White surface, consistent padding, subtle outline/elevation, and a clear title/action hierarchy. Avoid deep nested cards.
- **Product cards:** Image or neutral placeholder, name, short description, starting LKR price, and View Details. Placeholder art must not imply a real uploaded product image.
- **Category cards:** White rounded cards currently list category names vertically. A responsive grid can be considered during polish.
- **Status:** The current status is always written in text. Coloured badges remain a polish task.
- **Toolbar:** Consistent title, back action on secondary screens, and restrained overflow actions. Home can use the brand name and greeting.
- **Bottom navigation:** Four text-labelled destinations: Home, Orders, Notices, Profile. Selected-state styling and icons remain polish tasks.
- **Dialogs:** Use concise confirmation for cancellation and destructive address/design deletion. Name the affected item and give Cancel/Confirm actions.
- **Empty states:** Short explanation plus one useful action, such as Browse Products when My Orders is empty. Do not show fake order cards.
- **Validation messages:** Place near the relevant field or option, use plain language, preserve entered values, and return focus to the first problem. Do not rely only on a toast.

## 6. Accessibility checks

Aim for readable contrast and at least 48dp touch targets for interactive controls. Use clear action labels and useful content descriptions for meaningful images and icons; mark decorative images as decorative. Keep status and errors understandable without colour. Ensure forms scroll with the keyboard open, errors are near their fields, focus order follows the visual order, and text remains usable when system font size increases. Text links have a 48dp minimum height, and Home/Register were checked at 1.3 font scale on the API 25 emulator. Keyboard, screen-reader, contrast and physical-device checks remain future accessibility work.
