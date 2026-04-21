# ADR-003: BaselinkerException as unchecked RuntimeException

**Date:** 2026-04-21
**Status:** Accepted

---

## Context

`BaselinkerException` (and its subclass `BaselinkerApiException`) were introduced in the MVP
transport layer as checked exceptions extending `java.lang.Exception`. At the time the SDK
was essentially `client.execute(method, params, Class<T>)` — one synchronous call per method
and a caller that already knows it might fail.

Post-MVP the SDK adds service facades that wrap paginated calls behind
`Stream<T>` / `Iterable<T>`:

```java
client.orders().list(request).stream().forEach(this::handleOrder);
```

Java functional interfaces (`Function`, `Consumer`, `Predicate`, ...) do not declare checked
exceptions. The moment a paginated fetch is driven from inside `Stream.forEach()` or
`Stream.iterator()`, any checked exception thrown by the underlying `client.execute(...)`
must be caught in the lambda and rewrapped. This leaks mechanical `try/catch` into every
integration point or forces a separate `BaselinkerUncheckedException` duplicate type.

## Decision

Promote `BaselinkerException` (and transitively `BaselinkerApiException`) to extend
`java.lang.RuntimeException`. They remain part of the public API and may still be declared
in `throws` clauses for documentation value, but the compiler no longer forces callers to
handle or declare them.

## Consequences

**Positive:**
- `client.orders().list(request).stream().forEach(...)` can surface API errors through
  terminal operations exactly as promised in `context/2026-04-21-1200-api-base-sdk.md`.
- No `BaselinkerUncheckedException` wrapper duplicate.
- Aligns with contemporary SDK conventions (AWS SDK v2, Anthropic SDK, Stripe,
  Spring ecosystem) where transport exceptions are unchecked.
- Simpler user code at every call site: no mandatory `try/catch`.

**Negative / residual risk:**
- Existing code that declared `throws BaselinkerException` keeps compiling but the compiler
  no longer enforces handling. Callers who had been relying on that compiler prompt now
  need to remember it at the doc level (methods in `OrdersService` retain their `throws`
  declarations to document the contract even though Java does not require them).
- Existing unit tests that used `assertThrows(BaselinkerException.class, ...)` continue to
  work since `RuntimeException` branch does not affect instance checks.
- Package-private or third-party code that extended `BaselinkerException` via
  `extends Exception`-assumptions would need adjusting — no such code exists in this repo.

## Alternatives considered

1. **Custom throwing functional interfaces in the pagination package.** Rejected — forces
   callers of services (not just internal paginators) to import a non-standard interface
   just to interop with `Stream`, defeating the ergonomic goal.
2. **Catch checked exception inside each service lambda and rethrow as
   `BaselinkerUncheckedException`.** Rejected — two parallel exception hierarchies add
   burden without value, and the "unchecked" variant would be the one callers actually
   catch, reducing the checked flavor to ceremony.
3. **Leave as checked; services return `Try<T>` / `Result<T>` wrappers.** Rejected — SDK is
   deliberately minimal-dependency (no Vavr etc.) and monadic wrappers are un-idiomatic in
   Java 17 without library support.
