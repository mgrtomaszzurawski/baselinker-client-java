# baselinker-client-java

Unofficial, hand-written Java 17 SDK for the BaseLinker REST API
(<https://baselinker.com>). This project is not affiliated with or endorsed by
BaseLinker.

## Status

**v1 MVP — 14 methods wrapped** (per `ADR/ADR-001-mvp-method-scope.md`):

- **Orders** — `getOrders`, `addOrder`, `setOrderStatus`, `getOrderStatusList`
- **Inventory** — `getInventories`, `getInventoryWarehouses`,
  `getInventoryProductsList`, `getInventoryProductsData`,
  `getInventoryProductsStock`, `updateInventoryProductsStock`,
  `getInventoryProductLogs`
- **Inventory documents (GRN)** — `addInventoryDocument`,
  `addInventoryDocumentItems`, `setInventoryDocumentStatusConfirmed`,
  `getInventoryDocumentSeries`

Every other BaseLinker method (~130 in total) is reachable via the
`client.execute(method, params, ResponseClass)` escape hatch.

## Quickstart

```java
BaselinkerClient client = BaselinkerClient.builder()
        .apiToken(System.getenv("BASELINKER_TOKEN"))
        .build();

// Pull orders confirmed in the last 24 hours — auto-paginated
client.orders()
      .list(GetOrdersRequest.builder()
              .dateConfirmedFrom(Instant.now().minus(24, ChronoUnit.HOURS))
              .build())
      .stream()
      .forEach(order -> System.out.println(order.orderId()));
```

Full user guide: [`docs/guide.md`](docs/guide.md).
Copy-paste snippets: [`examples/`](examples/).
Live test harness: [`demo-app/`](demo-app/).

## Modules

- `baselinker-client/` — the SDK library
- `demo-app/` — exercises every v1 method against the real BaseLinker API
- `examples/` — illustrative snippets per domain

## Build

```bash
mvn install
```

JDK 17+, Maven 3.8+. Jackson is the only runtime dependency.

## Key design decisions

- `ResultPage<T>` abstracts the two BaseLinker pagination styles (time-cursor
  for `getOrders`, page-number elsewhere) behind a `Stream<T>` interface.
- `Instant` everywhere on-the-wire Unix epoch seconds are used — conversion is
  handled by a dedicated Jackson module.
- `BaselinkerException` / `BaselinkerApiException` are unchecked so API errors
  surface through `Stream<T>` terminal operations (see `ADR-003`).
- Public API types are Java `record`s; hand-written for ergonomic control,
  independent of the generated OpenAPI models (which remain available for the
  escape hatch).

See `ADR/` for the full set of decisions.

## License

GPL-3.0
