# ADR-002: Date cursor deduplication strategy

**Date:** 2026-04-21
**Status:** Accepted
**Supersedes:** dedup design note in `context/2026-04-21-1200-client-design.md`

---

## Context

`DateCursorResultPage<T>` wraps BaseLinker endpoints paginated by a time cursor,
specifically `getOrders` which takes `date_confirmed_from` (inclusive, Unix seconds).
The BaseLinker docs instruct clients to advance the cursor by
`max(date_confirmed_in_batch) + 1 second` between calls.

With a well-behaved server the `+1 second` trick eliminates overlap: every item in
batch *N+1* has `date_confirmed >= previousMax + 1`, strictly greater than any date in
batch *N*. No duplicates can occur.

The initial implementation (PR #2, first pass) added a defensive `HashSet<Long>` of
emitted ids to guard against the unlikely case where the server returned overlapping
items across a batch boundary. The set grew unbounded for the lifetime of the stream,
with a measured ~70–100 MB overhead per 1 M orders — raised as IMPORTANT by the
performance reviewer.

## Decision

Replace the id-based dedup set with a **cursor-based filter**: when iterating a fetched
batch, emit an item only if `extractDateConfirmed(item) >= currentCursor.epochSecond`.
Items returned out of contract by a misbehaving server are silently skipped.

This requires:
- dropping the `ToLongFunction<T> extractId` constructor parameter (no longer needed)
- keeping the existing `ToLongFunction<T> extractDateConfirmed`
- keeping the defensive "cursor failed to advance → stop" guard for the same-second
  pathological case
- replacing `HashSet<Long> seenIds` with O(1) filter logic

## Consequences

**Positive:**
- Memory cost drops from O(total emitted items) to O(1)
- One fewer constructor parameter — simpler `OrdersService.list(...)` glue
- Behavior is now formally "trust BaseLinker's +1s cursor contract, skip anything the
  server returns below the current cursor"

**Negative / residual risk:**
- Pathological scenario 4 (server returns the **same item id** in consecutive batches
  with a **modified** `date_confirmed` that still satisfies `>= cursor`) would result
  in a duplicate emission. This requires the server to rewrite timestamps between
  calls and is considered out of scope. A caller that needs guaranteed dedup can
  wrap the stream with `.distinct()` using a comparator on the id field.

**Documentation impact:**
- `context/2026-04-21-1200-api-base-sdk.md` previously claimed "Dedup by `order_id`
  across batch boundaries". Keep the dedup guarantee but restate the mechanism as
  "the SDK filters items below the current cursor", which is stronger than id dedup
  for the common misbehavior (returning old items) and weaker only for the
  scenario-4 corner case above.
- `context/2026-04-21-1200-client-design.md` proposed "dedup by order_id" in the
  algorithm. This ADR overrides that choice.
