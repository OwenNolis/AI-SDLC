# Feature-008: Extra Context — Order Management

## Background

This feature was initiated after user research showed that 34% of drop-offs in the checkout
flow happen because the order button is hard to find or click on mobile devices.
The button currently renders at 80px wide — well below the WCAG 2.1 minimum target size of 44x44px.

## Related Existing Endpoints

The following endpoints already exist and must be reused — do NOT redesign them:

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/products | Returns paginated product list with stock count |
| GET | /api/customers/{id}/address | Returns saved delivery addresses for pre-fill |
| POST | /api/orders | Creates a new order (to be implemented) |
| GET | /api/orders?customerId={id} | Returns order history (to be implemented) |
| DELETE | /api/orders/{id} | Cancels an order (to be implemented) |

## Domain Model (existing)

```
Customer (id, email, name)
    └── Order (id, orderNumber, customerId, status, totalAmount, createdAt)
            └── OrderItem (id, orderId, productId, quantity, unitPrice)
Product (id, name, stock, price)
```

## Known Technical Constraints

- The `sequences` table already exists with `entity_type` and `year` columns — reuse it for orderNumber generation
- PostgreSQL sequence function: `nextval('order_seq')` — already configured
- Frontend Axios instance is at `src/api/client.ts` — all API calls must go through it
- The `useQuery` / `useMutation` hooks from React Query are the only allowed data fetching mechanism

## Design System Notes

- Primary button color: `#2563EB` (blue-600)
- Button min-width for primary actions: `200px`
- Loading spinner: use existing `<Spinner />` component from `src/components/Spinner.tsx`
- Form wizard: use existing `<StepIndicator />` component — it accepts `steps` and `currentStep` props

## Open Questions from Previous Sprint

1. Should cancelled orders still appear in the history list? (decision: yes, with CANCELLED badge)
2. What happens to stock when order is cancelled? (decision: stock is restored immediately on cancellation)
3. Is there a maximum number of saved addresses per customer? (decision: max 5, oldest overwritten)
