# PrintXpress Low-Fidelity Text Wireframes

**Planning aid only. These are not screenshots of an implemented application.** Labels in square brackets are placeholders; content, prices and states must come from the real app later. The diagrams show hierarchy, not exact device dimensions.

## 1. Login

    +----------------------------------------+
    |              PRINTXPRESS               |
    |         Your printing, simplified      |
    |                                        |
    | Email                                  |
    | [_______________________________]        |
    | Password                               |
    | [_________________________] [Show]     |
    | [field error appears here]             |
    |                                        |
    |             [ LOGIN ]                  |
    |         New customer? Register         |
    +----------------------------------------+

## 2. Register

    +----------------------------------------+
    | < Back            Create account       |
    | Full name  [____________________]       |
    | Email      [____________________]       |
    | Phone      [____________________]       |
    | Password   [____________] [Show]       |
    | Confirm    [____________] [Show]       |
    | [field-specific error if needed]       |
    |                                        |
    |            [ REGISTER ]                |
    |         Already registered? Login      |
    +----------------------------------------+

## 3. Home

    +----------------------------------------+
    | PRINTXPRESS             [Notifications]|
    | Hello, [customer name]                 |
    | [ Search print products          ]     |
    | +------------------------------------+ |
    | | [Promotion card - display only]    | |
    | +------------------------------------+ |
    | Categories  [See all]                  |
    | [Cards] [Flyers] [Posters] [More]       |
    | Featured products [scrolling cards]    |
    | [Sample designs] [Print guidelines]    |
    |----------------------------------------|
    | Home | Orders | Notifications | Profile |
    +----------------------------------------+

## 4. Product List

    +----------------------------------------+
    | < Back      [Category name]            |
    | +------------------------------------+ |
    | | [Image]  [Product name]             | |
    | |          [Short description]        | |
    | |          From LKR [base price]      | |
    | |          [View Details]             | |
    | +------------------------------------+ |
    | | [Next product card]                | |
    | +------------------------------------+ |
    +----------------------------------------+

## 5. Product Details

    +----------------------------------------+
    | < Back      Product details            |
    | [        Product image/placeholder    ] |
    | [Product name]                         |
    | [Description and specifications]       |
    | Base price: LKR [amount]               |
    | Available: [sizes/materials/options]   |
    |                                        |
    |       [ CUSTOMIZE / CONTINUE ]         |
    +----------------------------------------+

## 6. Customize Print

    +----------------------------------------+
    | < Back      Customize print            |
    | [Product name and thumbnail]           |
    | Quantity       [  1  ]                 |
    | Size           [Select option v]       |
    | Material       [Select option v]       |
    | [Other relevant options from database] |
    | Custom text    [__________________]    |
    | Artwork        [Choose file]           |
    | [Validation near missing option]       |
    | Current total: LKR [amount]            |
    |              [ CONTINUE ]              |
    +----------------------------------------+

## 7. Order Summary

    +----------------------------------------+
    | < Back      Order summary              |
    | [Product name] x [quantity]            |
    | Options: [selected values]             |
    | Artwork: [file name / none]            |
    | Text: [short excerpt / none]           |
    | Unit price: LKR [amount]               |
    | Subtotal:   LKR [amount]               |
    |----------------------------------------|
    | Total:      LKR [amount]               |
    | [Edit]       [CONTINUE TO DELIVERY]    |
    +----------------------------------------+

## 8. Pickup / Delivery

    +----------------------------------------+
    | < Back      Pickup or delivery         |
    | ( ) Pickup       ( ) Home Delivery     |
    |                                        |
    | If Home Delivery:                      |
    | [Select saved address v] [Add address] |
    | [Address error appears here]           |
    |                                        |
    | Scheduled date [Choose date]           |
    | Scheduled time [Choose time]           |
    |              [ PLACE ORDER ]           |
    +----------------------------------------+

Pickup hides address controls. Home Delivery requires an address before Place Order can succeed.

## 9. My Orders

    +----------------------------------------+
    | My Orders                              |
    | +------------------------------------+ |
    | | Order #[ID]    [Status badge]      | |
    | | [Date]          LKR [total]        | |
    | | [View Details]                     | |
    | +------------------------------------+ |
    | [More order cards, if any]             |
    |                                        |
    | Empty: No orders yet. [Browse Products]|
    |----------------------------------------|
    | Home | Orders | Notifications | Profile |
    +----------------------------------------+

The order-card state and empty state are alternatives, not simultaneous content.

## 10. Order Details / Tracking

    +----------------------------------------+
    | < Back      Order #[ID]                |
    | Status: [Current status text]           |
    | [Processing] -> [Printing] -> [Ready/  |
    |  Out for Delivery] -> [Completed]      |
    | Scheduled: [date and time]             |
    | Type: [Pickup / Home Delivery]         |
    | Items: [products and selected options] |
    | Total: LKR [amount]                    |
    |                                        |
    | If Processing: [Reschedule] [Cancel]   |
    +----------------------------------------+

The Ready for Pickup and Out for Delivery steps are alternative paths. Cancelled is a terminal exception.

## 11. Profile

    +----------------------------------------+
    | Profile                                |
    | [Customer name]                        |
    | [Email]  [Phone]                       |
    | [Edit Profile]                         |
    |----------------------------------------|
    | > Manage Addresses                     |
    | > Saved Designs                        |
    | > Print Guidelines                     |
    | > FAQ / Support                        |
    | > Logout                               |
    |----------------------------------------|
    | Home | Orders | Notifications | Profile |
    +----------------------------------------+
