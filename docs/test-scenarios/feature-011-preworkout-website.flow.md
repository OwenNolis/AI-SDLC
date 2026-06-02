## Flow Tests (Markdown)

### Scenario: User completes main flow

#### FLOW-001: Successful Product Purchase

*   **Preconditions:**
    *   User is not logged in.
    *   The product catalog contains at least one product with available stock.
*   **Steps:**
    1.  **Actor:** user
        **Action:** Navigates to the homepage.
        **Expected:** The homepage is displayed with a hero section and product highlights.
        **Observability:** Log INFO: "Homepage loaded".
    2.  **Actor:** user
        **Action:** Clicks on the "Shop Now" button in the hero section.
        **Expected:** The user is redirected to the product listing page.
        **API Calls:** GET /api/products
        **Observability:** Log INFO: "Navigated to product listing page".
    3.  **Actor:** user
        **Action:** Selects a product from the listing page.
        **Expected:** The user is redirected to the product detail page for the selected product.
        **API Calls:** GET /api/products/{productId}
        **Observability:** Log INFO: "Navigated to product detail page for product {productId}".
    4.  **Actor:** user
        **Action:** Clicks the "Add to Cart" button on the product detail page.
        **Expected:** The product is added to the shopping cart, and a confirmation message is displayed.
        **API Calls:** POST /api/cart/items (productId, quantity=1)
        **Observability:** Log INFO: "Product {productId} added to cart".
    5.  **Actor:** user
        **Action:** Navigates to the shopping cart page.
        **Expected:** The shopping cart page displays the added product with its quantity and price.
        **API Calls:** GET /api/cart
        **Observability:** Log INFO: "Navigated to shopping cart page".
    6.  **Actor:** user
        **Action:** Clicks the "Proceed to Checkout" button.
        **Expected:** The user is redirected to the checkout page.
        **Observability:** Log INFO: "Navigated to checkout page".
    7.  **Actor:** user
        **Action:** Fills in the shipping address and selects a payment method (e.g., Credit Card).
        **Expected:** The checkout form is valid, and the order summary is updated.
        **Observability:** Log INFO: "Shipping address and payment method entered".
    8.  **Actor:** user
        **Action:** Clicks the "Place Order" button.
        **Expected:** The order is placed successfully, and the user is redirected to an order confirmation page.
        **API Calls:** POST /api/orders (cart items, shipping address, payment method)
        **Observability:** Log INFO: "Order placed successfully. Order ID: {orderId}".
*   **Variants:**
    *   **Type:** alternate
        **Name:** User registers during checkout
        **Steps:**
        1.  **Actor:** user
            **Action:** On the checkout page, clicks "Login/Register".
            **Expected:** A modal or section for registration appears.
            **Observability:** Log INFO: "User initiated registration during checkout".
        2.  **Actor:** user
            **Action:** Fills in registration details (first name, last name, email, password) and submits.
            **Expected:** User account is created, and they are logged in.
            **API Calls:** POST /api/auth/register
            **Observability:** Log INFO: "User registered successfully during checkout".
        3.  **Actor:** user
            **Action:** Continues with checkout process.
            **Expected:** The order is placed using the newly created account.
            **API Calls:** POST /api/orders
            **Observability:** Log INFO: "Order placed with newly registered user. Order ID: {orderId}".
    *   **Type:** data
        **Name:** Different payment methods
        **Steps:**
        1.  **Actor:** user
            **Action:** Selects "Bancontact" as the payment method on the checkout page.
            **Expected:** The payment form adapts for Bancontact.
            **Observability:** Log INFO: "Selected Bancontact payment method".
        2.  **Actor:** user
            **Action:** Completes the order with Bancontact.
            **Expected:** Order is placed successfully.
            **API Calls:** POST /api/orders
            **Observability:** Log INFO: "Order placed successfully with Bancontact. Order ID: {orderId}".
        3.  **Actor:** user
            **Action:** Selects "Credit Card" as the payment method on the checkout page.
            **Expected:** The payment form adapts for Credit Card.
            **Observability:** Log INFO: "Selected Credit Card payment method".
        4.  **Actor:** user
            **Action:** Completes the order with Credit Card.
            **Expected:** Order is placed successfully.
            **API Calls:** POST /api/orders
            **Observability:** Log INFO: "Order placed successfully with Credit Card. Order ID: {orderId}".
    *   **Type:** negative
        **Name:** Empty shopping cart during checkout
        **Steps:**
        1.  **Actor:** user
            **Action:** Navigates to the shopping cart page.
            **Expected:** The shopping cart is empty.
            **API Calls:** GET /api/cart
            **Observability:** Log INFO: "Shopping cart is empty".
        2.  **Actor:** user
            **Action:** Attempts to navigate to the checkout page.
            **Expected:** The user is shown a message indicating the cart is empty and cannot proceed to checkout.
            **Observability:** Log WARN: "User attempted to checkout with an empty cart".

#### FLOW-002: Admin Product Management

*   **Preconditions:**
    *   An admin user is logged in.
    *   The product catalog is accessible.
*   **Steps:**
    1.  **Actor:** user
        **Action:** Navigates to the admin dashboard.
        **Expected:** The admin dashboard is displayed with navigation options.
        **Observability:** Log INFO: "Admin dashboard loaded".
    2.  **Actor:** user
        **Action:** Clicks on "Manage Products".
        **Expected:** The admin product list page is displayed, showing existing products.
        **API Calls:** GET /api/admin/products
        **Observability:** Log INFO: "Navigated to admin product management page".
    3.  **Actor:** user
        **Action:** Clicks the "Add New Product" button.
        **Expected:** The form for adding a new product is displayed.
        **Observability:** Log INFO: "Opened product creation form".
    4.  **Actor:** user
        **Action:** Fills in the product details (name, description, price, stock, etc.) and clicks "Save".
        **Expected:** The new product is created successfully, and the admin product list is updated.
        **API Calls:** POST /api/admin/products
        **Observability:** Log INFO: "New product created. Product ID: {newProductId}".
    5.  **Actor:** user
        **Action:** Selects an existing product from the list and clicks "Edit".
        **Expected:** The form for editing the selected product is displayed with its current details.
        **API Calls:** GET /api/admin/products/{productId}
        **Observability:** Log INFO: "Opened product edit form for product {productId}".
    6.  **Actor:** user
        **Action:** Modifies product details (e.g., price, stock) and clicks "Save".
        **Expected:** The product is updated successfully, and the admin product list reflects the changes.
        **API Calls:** PUT /api/admin/products/{productId}
        **Observability:** Log INFO: "Product {productId} updated successfully".
*   **Variants:**
    *   **Type:** negative
        **Name:** Admin attempts to create a product with invalid data
        **Steps:**
        1.  **Actor:** user
            **Action:** Opens the product creation form.
            **Expected:** The form is displayed.
            **Observability:** Log INFO: "Opened product creation form".
        2.  **Actor:** user
            **Action:** Submits the form with a missing required field (e.g., name).
            **Expected:** An error message is displayed indicating the missing field, and the product is not created.
            **API Calls:** POST /api/admin/products (with invalid data)
            **Observability:** Log WARN: "Attempted to create product with missing required field".
    *   **Type:** negative
        **Name:** Non-admin user attempts to access admin product management
        **Steps:**
        1.  **Actor:** user
            **Action:** Attempts to navigate to the admin product management URL.
            **Expected:** The user is redirected to a login page or shown an "Access Denied" error.
            **API Calls:** (Implicitly attempts to access admin endpoints)
            **Observability:** Log ERROR: "Unauthorized access attempt to admin product management".

#### FLOW-003: User Login and Order History

*   **Preconditions:**
    *   A user account exists with a registered email and password.
    *   The user has previously placed at least one order.
*   **Steps:**
    1.  **Actor:** user
        **Action:** Navigates to the login page.
        **Expected:** The login form is displayed with fields for email and password.
        **Observability:** Log INFO: "Navigated to login page".
    2.  **Actor:** user
        **Action:** Enters valid email and password and clicks "Login".
        **Expected:** The user is logged in and redirected to the homepage or a dashboard.
        **API Calls:** POST /api/auth/login
        **Observability:** Log INFO: "User logged in successfully. User ID: {userId}".
    3.  **Actor:** user
        **Action:** Navigates to the "Order History" page.
        **Expected:** The order history page displays a list of the user's past orders.
        **API Calls:** GET /api/orders/me
        **Observability:** Log INFO: "Navigated to order history page".
*   **Variants:**
    *   **Type:** negative
        **Name:** Invalid login credentials
        **Steps:**
        1.  **Actor:** user
            **Action:** Navigates to the login page.
            **Expected:** The login form is displayed.
            **Observability:** Log INFO: "Navigated to login page".
        2.  **Actor:** user
            **Action:** Enters an invalid email or password and clicks "Login".
            **Expected:** An error message is displayed indicating invalid credentials, and the user remains on the login page.
            **API Calls:** POST /api/auth/login (with invalid credentials)
            **Observability:** Log WARN: "Login attempt failed due to invalid credentials".
    *   **Type:** negative
        **Name:** User with no orders attempts to view history
        **Steps:**
        1.  **Actor:** user
            **Action:** Logs in successfully.
            **Expected:** User is logged in.
            **API Calls:** POST /api/auth/login
            **Observability:** Log INFO: "User logged in successfully. User ID: {userId}".
        2.  **Actor:** user
            **Action:** Navigates to the "Order History" page.
            **Expected:** The order history page displays a message indicating that no orders have been placed yet.
            **API Calls:** GET /api/orders/me
            **Observability:** Log INFO: "Navigated to order history page for user with no orders".

---

## Flow Tests (JSON)

```json
{
  "meta": {
    "featureId": "feature-011-preworkout-website",
    "version": "1.0.0"
  },
  "feature": "feature-011-preworkout-website",
  "scenarios": [
    {
      "id": "happy_path_purchase",
      "title": "User completes main flow",
      "type": "happy-path"
    },
    {
      "id": "admin_product_management",
      "title": "Admin Product Management",
      "type": "happy-path"
    },
    {
      "id": "user_login_order_history",
      "title": "User Login and Order History",
      "type": "happy-path"
    }
  ],
  "flows": [
    {
      "id": "FLOW-001",
      "name": "Successful Product Purchase",
      "preconditions": [
        "User is not logged in.",
        "The product catalog contains at least one product with available stock."
      ],
      "steps": [
        {
          "actor": "user",
          "action": "Navigates to the homepage.",
          "expected": "The homepage is displayed with a hero section and product highlights.",
          "observability": [
            "Log INFO: \"Homepage loaded\""
          ]
        },
        {
          "actor": "user",
          "action": "Clicks on the \"Shop Now\" button in the hero section.",
          "expected": "The user is redirected to the product listing page.",
          "apiCalls": [
            "GET /api/products"
          ],
          "observability": [
            "Log INFO: \"Navigated to product listing page\""
          ]
        },
        {
          "actor": "user",
          "action": "Selects a product from the listing page.",
          "expected": "The user is redirected to the product detail page for the selected product.",
          "apiCalls": [
            "GET /api/products/{productId}"
          ],
          "observability": [
            "Log INFO: \"Navigated to product detail page for product {productId}\""
          ]
        },
        {
          "actor": "user",
          "action": "Clicks the \"Add to Cart\" button on the product detail page.",
          "expected": "The product is added to the shopping cart, and a confirmation message is displayed.",
          "apiCalls": [
            "POST /api/cart/items (productId, quantity=1)"
          ],
          "observability": [
            "Log INFO: \"Product {productId} added to cart\""
          ]
        },
        {
          "actor": "user",
          "action": "Navigates to the shopping cart page.",
          "expected": "The shopping cart page displays the added product with its quantity and price.",
          "apiCalls": [
            "GET /api/cart"
          ],
          "observability": [
            "Log INFO: \"Navigated to shopping cart page\""
          ]
        },
        {
          "actor": "user",
          "action": "Clicks the \"Proceed to Checkout\" button.",
          "expected": "The user is redirected to the checkout page.",
          "observability": [
            "Log INFO: \"Navigated to checkout page\""
          ]
        },
        {
          "actor": "user",
          "action": "Fills in the shipping address and selects a payment method (e.g., Credit Card).",
          "expected": "The checkout form is valid, and the order summary is updated.",
          "observability": [
            "Log INFO: \"Shipping address and payment method entered\""
          ]
        },
        {
          "actor": "user",
          "action": "Clicks the \"Place Order\" button.",
          "expected": "The order is placed successfully, and the user is redirected to an order confirmation page.",
          "apiCalls": [
            "POST /api/orders (cart items, shipping address, payment method)"
          ],
          "observability": [
            "Log INFO: \"Order placed successfully. Order ID: {orderId}\""
          ]
        }
      ],
      "variants": [
        {
          "type": "alternate",
          "name": "User registers during checkout",
          "steps": [
            {
              "actor": "user",
              "action": "On the checkout page, clicks \"Login/Register\".",
              "expected": "A modal or section for registration appears.",
              "observability": [
                "Log INFO: \"User initiated registration during checkout\""
              ]
            },
            {
              "actor": "user",
              "action": "Fills in registration details (first name, last name, email, password) and submits.",
              "expected": "User account is created, and they are logged in.",
              "apiCalls": [
                "POST /api/auth/register"
              ],
              "observability": [
                "Log INFO: \"User registered successfully during checkout\""
              ]
            },
            {
              "actor": "user",
              "action": "Continues with checkout process.",
              "expected": "The order is placed using the newly created account.",
              "apiCalls": [
                "POST /api/orders"
              ],
              "observability": [
                "Log INFO: \"Order placed with newly registered user. Order ID: {orderId}\""
              ]
            }
          ]
        },
        {
          "type": "data",
          "name": "Different payment methods",
          "steps": [
            {
              "actor": "user",
              "action": "Selects \"Bancontact\" as the payment method on the checkout page.",
              "expected": "The payment form adapts for Bancontact.",
              "observability": [
                "Log INFO: \"Selected Bancontact payment method\""
              ]
            },
            {
              "actor": "user",
              "action": "Completes the order with Bancontact.",
              "expected": "Order is placed successfully.",
              "apiCalls": [
                "POST /api/orders"
              ],
              "observability": [
                "Log INFO: \"Order placed successfully with Bancontact. Order ID: {orderId}\""
              ]
            },
            {
              "actor": "user",
              "action": "Selects \"Credit Card\" as the payment method on the checkout page.",
              "expected": "The payment form adapts for Credit Card.",
              "observability": [
                "Log INFO: \"Selected Credit Card payment method\""
              ]
            },
            {
              "actor": "user",
              "action": "Completes the order with Credit Card.",
              "expected": "Order is placed successfully.",
              "apiCalls": [
                "POST /api/orders"
              ],
              "observability": [
                "Log INFO: \"Order placed successfully with Credit Card. Order ID: {orderId}\""
              ]
            }
          ]
        },
        {
          "type": "negative",
          "name": "Empty shopping cart during checkout",
          "steps": [
            {
              "actor": "user",
              "action": "Navigates to the shopping cart page.",
              "expected": "The shopping cart is empty.",
              "apiCalls": [
                "GET /api/cart"
              ],
              "observability": [
                "Log INFO: \"Shopping cart is empty\""
              ]
            },
            {
              "actor": "user",
              "action": "Attempts to navigate to the checkout page.",
              "expected": "The user is shown a message indicating the cart is empty and cannot proceed to checkout.",
              "observability": [
                "Log WARN: \"User attempted to checkout with an empty cart\""
              ]
            }
          ]
        }
      ]
    },
    {
      "id": "FLOW-002",
      "name": "Admin Product Management",
      "preconditions": [
        "An admin user is logged in.",
        "The product catalog is accessible."
      ],
      "steps": [
        {
          "actor": "user",
          "action": "Navigates to the admin dashboard.",
          "expected": "The admin dashboard is displayed with navigation options.",
          "observability": [
            "Log INFO: \"Admin dashboard loaded\""
          ]
        },
        {
          "actor": "user",
          "action": "Clicks on \"Manage Products\".",
          "expected": "The admin product list page is displayed, showing existing products.",
          "apiCalls": [
            "GET /api/admin/products"
          ],
          "observability": [
            "Log INFO: \"Navigated to admin product management page\""
          ]
        },
        {
          "actor": "user",
          "action": "Clicks the \"Add New Product\" button.",
          "expected": "The form for adding a new product is displayed.",
          "observability": [
            "Log INFO: \"Opened product creation form\""
          ]
        },
        {
          "actor": "user",
          "action": "Fills in the product details (name, description, price, stock, etc.) and clicks \"Save\".",
          "expected": "The new product is created successfully, and the admin product list is updated.",
          "apiCalls": [
            "POST /api/admin/products"
          ],
          "observability": [
            "Log INFO: \"New product created. Product ID: {newProductId}\""
          ]
        },
        {
          "actor": "user",
          "action": "Selects an existing product from the list and clicks \"Edit\".",
          "expected": "The form for editing the selected product is displayed with its current details.",
          "apiCalls": [
            "GET /api/admin/products/{productId}"
          ],
          "observability": [
            "Log INFO: \"Opened product edit form for product {productId}\""
          ]
        },
        {
          "actor": "user",
          "action": "Modifies product details (e.g., price, stock) and clicks \"Save\".",
          "expected": "The product is updated successfully, and the admin product list reflects the changes.",
          "apiCalls": [
            "PUT /api/admin/products/{productId}"
          ],
          "observability": [
            "Log INFO: \"Product {productId} updated successfully\""
          ]
        }
      ],
      "variants": [
        {
          "type": "negative",
          "name": "Admin attempts to create a product with invalid data",
          "steps": [
            {
              "actor": "user",
              "action": "Opens the product creation form.",
              "expected": "The form is displayed.",
              "observability": [
                "Log INFO: \"Opened product creation form\""
              ]
            },
            {
              "actor": "user",
              "action": "Submits the form with a missing required field (e.g., name).",
              "expected": "An error message is displayed indicating the missing field, and the product is not created.",
              "apiCalls": [
                "POST /api/admin/products (with invalid data)"
              ],
              "observability": [
                "Log WARN: \"Attempted to create product with missing required field\""
              ]
            }
          ]
        },
        {
          "type": "negative",
          "name": "Non-admin user attempts to access admin product management",
          "steps": [
            {
              "actor": "user",
              "action": "Attempts to navigate to the admin product management URL.",
              "expected": "The user is redirected to a login page or shown an \"Access Denied\" error.",
              "apiCalls": [],
              "observability": [
                "Log ERROR: \"Unauthorized access attempt to admin product management\""
              ]
            }
          ]
        }
      ]
    },
    {
      "id": "FLOW-003",
      "name": "User Login and Order History",
      "preconditions": [
        "A user account exists with a registered email and password.",
        "The user has previously placed at least one order."
      ],
      "steps": [
        {
          "actor": "user",
          "action": "Navigates to the login page.",
          "expected": "The login form is displayed with fields for email and password.",
          "observability": [
            "Log INFO: \"Navigated to login page\""
          ]
        },
        {
          "actor": "user",
          "action": "Enters valid email and password and clicks \"Login\".",
          "expected": "The user is logged in and redirected to the homepage or a dashboard.",
          "apiCalls": [
            "POST /api/auth/login"
          ],
          "observability": [
            "Log INFO: \"User logged in successfully. User ID: {userId}\""
          ]
        },
        {
          "actor": "user",
          "action": "Navigates to the \"Order History\" page.",
          "expected": "The order history page displays a list of the user's past orders.",
          "apiCalls": [
            "GET /api/orders/me"
          ],
          "observability": [
            "Log INFO: \"Navigated to order history page\""
          ]
        }
      ],
      "variants": [
        {
          "type": "negative",
          "name": "Invalid login credentials",
          "steps": [
            {
              "actor": "user",
              "action": "Navigates to the login page.",
              "expected": "The login form is displayed.",
              "observability": [
                "Log INFO: \"Navigated to login page\""
              ]
            },
            {
              "actor": "user",
              "action": "Enters an invalid email or password and clicks \"Login\".",
              "expected": "An error message is displayed indicating invalid credentials, and the user remains on the login page.",
              "apiCalls": [
                "POST /api/auth/login (with invalid credentials)"
              ],
              "observability": [
                "Log WARN: \"Login attempt failed due to invalid credentials\""
              ]
            }
          ]
        },
        {
          "type": "negative",
          "name": "User with no orders attempts to view history",
          "steps": [
            {
              "actor": "user",
              "action": "Logs in successfully.",
              "expected": "User is logged in.",
              "apiCalls": [
                "POST /api/auth/login"
              ],
              "observability": [
                "Log INFO: \"User logged in successfully. User ID: {userId}\""
              ]
            },
            {
              "actor": "user",
              "action": "Navigates to the \"Order History\" page.",
              "expected": "The order history page displays a message indicating that no orders have been placed yet.",
              "apiCalls": [
                "GET /api/orders/me"
              ],
              "observability": [
                "Log INFO: \"Navigated to order history page for user with no orders\""
              ]
            }
          ]
        }
      ]
    }
  ]
}
```