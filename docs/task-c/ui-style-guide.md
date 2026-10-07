# PrintXpress UI Style Guide

**Status:** Planned native Android Views design. No XML layouts or application screenshots exist yet.

## 1. Design direction

PrintXpress should feel clear, professional and friendly without requiring a complex commercial interface. A restrained blue and gold palette suggests reliability and printed colour, while white cards keep product details readable. Screens should present one primary action at a time, use consistent labels and support small and common large phone displays through scrolling and flexible widths.

## 2. Colour palette

| Role | Hex | Intended use |
| --- | --- | --- |
| Primary brand | #123B63 | Toolbar, primary buttons, selected navigation |
| Secondary/accent | #E7A12B | Highlights, promotion accents, selected chips |
| Background | #F5F7FA | Screen background |
| Surface/card | #FFFFFF | Cards, forms, dialogs |
| Primary text | #1B2633 | Main text on light surfaces |
| Secondary text | #51606F | Captions and supporting text |
| Success | #1F7A4C | Confirmation and completed status |
| Warning | #A15C00 | Caution and in-progress status |
| Error | #B42318 | Field errors and cancellation notices |

Use white text on the primary, success and error colours. Use primary dark text on the gold accent; white text on gold would be hard to read. Approximate contrast checks: white on primary 11.47:1, dark text on accent 6.95:1, secondary text on white 6.46:1, white on success 5.32:1, and white on error 6.57:1. Keep colour as a supporting cue, never the only way to communicate order status or an error.

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
- **Text fields:** Persistent visible labels, clear examples, a comfortable input height, and field-specific errors directly below. Password fields offer show/hide. Required fields are explained before submission.
- **Cards:** White surface, consistent padding, subtle outline/elevation, and a clear title/action hierarchy. Avoid deep nested cards.
- **Product cards:** Image or neutral placeholder, name, short description, starting LKR price, and View Details. Placeholder art must not imply a real uploaded product image.
- **Category cards:** Simple icon/illustration placeholder plus category name; use a responsive grid when width allows and avoid tiny tap areas.
- **Status badges:** Short text plus colour. Processing uses blue, Printing/Out for Delivery amber, Ready for Pickup/Completed green, and Cancelled red. The exact status text is always visible.
- **Toolbar:** Consistent title, back action on secondary screens, and restrained overflow actions. Home can use the brand name and greeting.
- **Bottom navigation:** Four destinations only: Home, Orders, Notifications, Profile. Show icon and text; highlight the selected destination. Do not add Categories as a fifth tab.
- **Dialogs:** Use concise confirmation for cancellation and destructive address/design deletion. Name the affected item and give Cancel/Confirm actions.
- **Empty states:** Short explanation plus one useful action, such as Browse Products when My Orders is empty. Do not show fake order cards.
- **Validation messages:** Place near the relevant field or option, use plain language, preserve entered values, and return focus to the first problem. Do not rely only on a toast.

## 6. Accessibility checks

Aim for readable contrast and at least 48dp touch targets for interactive controls. Use clear action labels and useful content descriptions for meaningful images and icons; mark decorative images as decorative. Keep status and errors understandable without colour. Ensure forms scroll with the keyboard open, errors are near their fields, focus order follows the visual order, and text remains usable when system font size increases. Verify these choices later on an emulator and a physical device.
