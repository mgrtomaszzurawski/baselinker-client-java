# BaseLinker SDK — demo app

Live test harness that exercises every v1 MVP method against the real BaseLinker
API.

## Requirements

- JDK 17+
- Maven
- A BaseLinker API token (account panel → My Account → API)

## Running

READ-only run (safe, no data created):

```bash
export BASELINKER_TOKEN=xxxxxxxxxxxxxxxxxxxx
mvn -pl demo-app -am compile
mvn -pl demo-app exec:java
```

Include WRITE methods (creates a demo order and a draft inventory document in your
account — use a throwaway test catalog, nothing is auto-deleted):

```bash
export BASELINKER_TOKEN=xxxxxxxxxxxxxxxxxxxx
export BASELINKER_DEMO_WRITES=true
mvn -pl demo-app exec:java
```

## What it exercises

READ:
- `orders().statusList()`
- `orders().list(...)` (cursor pagination, sampled)
- `inventory().catalogs()`
- `inventory().warehouses()`
- `inventory().products().list(...)` (page pagination, sampled)
- `inventory().products().stock(...)`
- `inventory().products().data(...)`
- `inventory().products().logs(...)`
- `inventoryDocuments().series()`

WRITE (when `BASELINKER_DEMO_WRITES=true`):
- `orders().add(...)` + `orders().setStatus(...)`
- `inventory().products().updateStock(...)` — idempotent re-assign of existing
  stock value (zero delta)
- `inventoryDocuments().add(...)` + `addItems(...)` — stops before `confirm()` to
  avoid moving stock

## Exit codes

- `0` — all calls succeeded
- `1` — `BASELINKER_TOKEN` missing
- `2` — BaseLinker API error (bad token, rate limit, validation)
- `3` — transport / serialization error
