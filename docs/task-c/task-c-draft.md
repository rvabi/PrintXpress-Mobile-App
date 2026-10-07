# TASK C – User Interface Design

**Status:** Design draft. The screens described here are planned; no Android interface or screenshots have been produced.

## 1. Introduction

PrintXpress will provide a customer interface for browsing printing products, preparing artwork and placing pickup or delivery orders. The design will support the assessment's functional flow while remaining achievable with native Android XML Views. Nineteen planned destinations cover the main customer tasks.

## 2. Design Approach

The interface will use a clean hierarchy with one clear primary action on each screen. Dark blue will identify the brand and important actions, while white cards and a pale background will keep product details readable. A gold accent will draw attention to offers without making promotions appear to reduce the checkout total. Repeated labels, card patterns and spacing will help customers recognize actions as they move from browsing to ordering.

## 3. Navigation Design

Splash will route customers to Login when no valid session exists or to Home when a session is available. Registration will return to Login after success; automatic login is not assumed. Home will provide entry to categories, featured products, sample designs and print guidance. The central flow will move from category and product details through customization, optional artwork selection, order summary, pickup or delivery, and confirmation. Four bottom-navigation destinations—Home, Orders, Notifications and Profile—will cover frequent return paths without overcrowding the bar. Profile will link to addresses, saved designs, guidance, support and logout.

## 4. Key Interface Screens

Home will combine a greeting, local product search, category shortcuts, a single informational promotion card and featured products. Product Categories will use a grid, while Product List will use scrollable cards with starting LKR prices and View Details actions. Product Details will present specifications and available option types before the customer continues. Customize Print will show only options relevant to the selected product, using the planned product_options data. Quantity, custom text and artwork actions will sit near a recalculated price. The summary will repeat the selected choices, artwork name, unit price, subtotal and total so the customer can correct mistakes before checkout.

Pickup / Delivery will reveal address selection only for Home Delivery and require a scheduled date and time for either choice. Order Confirmation will appear only after a successful database save. My Orders will show compact cards, while Order Details will use text-labelled status progression. Reschedule and Cancel actions will appear only for Processing orders. Promotions in Notifications / Offers will show percentage values for information and will not change the order total.

## 5. Validation and User Feedback

Errors will appear next to the affected input or option, preserving entries for correction. Registration will explain invalid email, phone, password and duplicate email; customization will identify missing options or quantity below one. Delivery will explain when an address or schedule is missing. A saved order will lead to a confirmation containing its ID and total, while a failed save will not display success. Empty product, order, notification and design lists will use short explanations and useful next actions rather than sample records presented as real data. Order status will always be shown in words as well as colour.

## 6. Accessibility and Responsiveness

Text will use system fonts and scalable sp sizes, with primary body text around 16sp. Dark text on white cards and white text on the dark blue primary colour will support contrast. Buttons and icon actions will aim for at least 48dp touch targets and clear labels or content descriptions. Longer forms and product information will scroll, and layouts will use flexible widths so common Android phone sizes and increased font settings remain usable. These decisions will require device checks after implementation.

## 7. Design Consistency

The style guide defines a small palette, typography roles and a 4dp-based spacing scale. Product and order cards will reuse padding, rounded corners, title placement and action treatment. Primary buttons will share colour and size; secondary actions will use an outline or text style. Toolbars and the four-item bottom navigation will maintain predictable orientation across destinations. Validation messages and status badges will follow the same patterns throughout the application.

## 8. Conclusion

The interface prioritizes clear ordering, prices and status. Its restrained design should be practical to implement with Java and XML Views. Actual screenshots and claims about usability will be added only after the running Android application is built and tested.
