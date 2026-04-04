# ADR-001: MVP — API method scope for v1

**Date:** 2026-03-30
**Status:** Accepted

---

## Context

This project is the first and only typed Java client for the BaseLinker API. The API has 130+ methods.
The project owner needs specific operations to automate processes:

- fetching and creating orders (online sales + internal delivery to physical store)
- reading and updating inventory stock levels
- receiving goods from a supplier via inventory document

Both sales systems (NoviCloud, BaseLinker) have limited warehouse management modules.
Delivery to the physical store is modeled as an Order (to be collected), not an inventory document —
because the inventory document process in BL is too limited for this purpose.

## Decision

In v1 we implement only the methods needed for the above use cases.
Remaining methods will be added in subsequent iterations as needed.

## Scope v1 — 14 methods

### Orders
| Method | Purpose |
|--------|---------|
| `getOrders` | fetching orders / sales history |
| `addOrder` | creating a delivery order for physical store |
| `setOrderStatus` | changing order status |
| `getOrderStatusList` | list of available statuses (context) |

### Products and inventory stock (Inventory)
| Method | Purpose |
|--------|---------|
| `getInventoryProductsList` | product list |
| `getInventoryProductsData` | detailed product data |
| `getInventoryProductsStock` | reading stock levels |
| `updateInventoryProductsStock` | updating stock levels |
| `getInventoryProductLogs` | stock change logs (sales history) |
| `getInventories` | catalog ID — required by most inventory calls |
| `getInventoryWarehouses` | warehouse list — required for stock operations |

### Inventory documents
| Method | Purpose |
|--------|---------|
| `addInventoryDocument` | creating a goods receipt document (GRN) |
| `addInventoryDocumentItems` | adding items to a document |
| `setInventoryDocumentStatusConfirmed` | confirming the document (updates stock levels) |
| `getInventoryDocumentSeries` | document series — required when creating a document |

## Consequences

- Scope is closed and achievable in a short time
- Methods outside scope have no `docs/methods/<method>.md` or schemas in OpenAPI
- When adding new methods in the future, this ADR should be updated or a new one created
