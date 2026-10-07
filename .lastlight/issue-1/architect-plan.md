# Architect Plan — Issue #1: Add Product Update and Delete

## Problem Statement

`ProductController` (line 1–30) exposes only three endpoints — two GETs and a POST — with no validation and no 404 handling. `ProductService` (line 1–24) delegates straight to the repository with no guards: `saveProduct` never checks for a duplicate `(ean, storeId)` pair, both GET methods silently return empty when no record matches (yielding a 200 with an empty body/array), and there is no update or delete operation at all. The `Product` model carries no validation annotations, so an empty EAN or a negative price is accepted without complaint. No HTTP error-mapping layer exists: exceptions propagate unhandled and produce 500s rather than structured 400/404/409 responses.

---

## Summary of Changes

1. **Add `spring-boot-starter-validation`** to `pom.xml` so `@Valid` on `@RequestBody` triggers Hibernate Validator.
2. **Annotate `Product` fields** with `@NotBlank` / `@Positive` constraints.
3. **Create two custom exception classes** (`ProductNotFoundException`, `ProductConflictException`).
4. **Create `GlobalExceptionHandler`** (`@RestControllerAdvice`) mapping those exceptions + `WebExchangeBindException` to 404 / 409 / 400 with a structured `ErrorResponse` body.
5. **Extend `ProductService`**: add `updateProduct` and `deleteProduct`, add conflict check to `saveProduct`, and add `switchIfEmpty → error` to both GET methods.
6. **Extend `ProductController`**: add `@Valid`, add `PUT /{ean}/store/{storeId}` (200) and `DELETE /{ean}/store/{storeId}` (204), propagate 404/409 via reactive error signals.
7. **Expand both test files** to cover all new operations and all error paths.
8. **Update `README.md`** with the full endpoint table.

---

## Files to Modify — Exhaustive Manifest

### A. `pom.xml`
- **Location:** root, `<dependencies>` block (after line 38 / the existing `webflux` dependency).
- **Change:** Add `spring-boot-starter-validation` dependency:
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  ```

### B. `src/main/java/com/mahmoud/firstChallenge/model/Product.java`
- **Location:** field declarations (lines 11–15).
- **Change:** Add `jakarta.validation.constraints` imports and annotate fields:
  - `ean` → `@NotBlank(message = "EAN must not be blank")`
  - `storeId` → `@NotBlank(message = "Store ID must not be blank")`
  - `name` → `@NotBlank(message = "Name must not be blank")`
  - `price` → `@Positive(message = "Price must be positive")`
  - Add imports: `import jakarta.validation.constraints.NotBlank;` and `import jakarta.validation.constraints.Positive;`

### C. NEW — `src/main/java/com/mahmoud/firstChallenge/exception/ProductNotFoundException.java`
- Full new file. `RuntimeException` subclass with a single `String message` constructor.
  ```java
  package com.mahmoud.firstChallenge.exception;
  public class ProductNotFoundException extends RuntimeException {
      public ProductNotFoundException(String message) { super(message); }
  }
  ```

### D. NEW — `src/main/java/com/mahmoud/firstChallenge/exception/ProductConflictException.java`
- Full new file. `RuntimeException` subclass with a single `String message` constructor.
  ```java
  package com.mahmoud.firstChallenge.exception;
  public class ProductConflictException extends RuntimeException {
      public ProductConflictException(String message) { super(message); }
  }
  ```

### E. NEW — `src/main/java/com/mahmoud/firstChallenge/exception/GlobalExceptionHandler.java`
- Full new file annotated `@RestControllerAdvice`. Contains:
  - Inner `record ErrorResponse(String error, String message, Map<String, String> fieldErrors)`
  - `@ExceptionHandler(ProductNotFoundException.class)` → `@ResponseStatus(NOT_FOUND)`, body `ErrorResponse("NOT_FOUND", ex.getMessage(), null)`
  - `@ExceptionHandler(ProductConflictException.class)` → `@ResponseStatus(CONFLICT)`, body `ErrorResponse("CONFLICT", ex.getMessage(), null)`
  - `@ExceptionHandler(WebExchangeBindException.class)` → `@ResponseStatus(BAD_REQUEST)`, collects `BindingResult.getFieldErrors()` into `Map<field, defaultMessage>`, body `ErrorResponse("VALIDATION_ERROR", "Validation failed", fieldErrors)`
  - Imports: `org.springframework.web.bind.support.WebExchangeBindException`, `org.springframework.validation.FieldError`, `org.springframework.http.HttpStatus`, `reactor.core.publisher.Mono`, `java.util.Map`, `java.util.stream.Collectors`

### F. `src/main/java/com/mahmoud/firstChallenge/service/ProductService.java`
- **Imports to add:** `ProductNotFoundException`, `ProductConflictException` from `.exception` package.
- **`getProductsByEan` (line 16–18):** append `.switchIfEmpty(Flux.error(new ProductNotFoundException("No product found with EAN: " + ean)))`.
- **`getProductByEanAndStore` (line 20–22):** append `.switchIfEmpty(Mono.error(new ProductNotFoundException("No product found with EAN: " + ean + " and storeId: " + storeId)))`.
- **`saveProduct` (line 24–26):** Replace body with: check for existing `(product.getEan(), product.getStoreId())` via `productRepository.findByEanAndStoreId`; if found → `Mono.error(new ProductConflictException(...))`, otherwise (via `switchIfEmpty`) → `productRepository.save(product)`.
- **ADD `updateProduct(String ean, String storeId, Product updated)`:**
  1. `productRepository.findByEanAndStoreId(ean, storeId)` → if empty: `Mono.error(new ProductNotFoundException(...))`.
  2. `.flatMap(existing -> ...)`:
     - If `updated.getEan()` or `updated.getStoreId()` differ from path params: check `productRepository.findByEanAndStoreId(updated.getEan(), updated.getStoreId())` → if found: `Mono.error(new ProductConflictException(...))`.
     - Else: set all fields on `existing` and call `productRepository.save(existing)`.
  3. Returns `Mono<Product>`.
- **ADD `deleteProduct(String ean, String storeId)`:**
  1. `productRepository.findByEanAndStoreId(ean, storeId)` → if empty: `Mono.error(new ProductNotFoundException(...))`.
  2. `.flatMap(p -> productRepository.deleteById(p.getId()))`.
  3. Returns `Mono<Void>`.

### G. `src/main/java/com/mahmoud/firstChallenge/controller/ProductController.java`
- **Imports to add:** `jakarta.validation.Valid`, `org.springframework.http.HttpStatus`, `org.springframework.web.bind.annotation.ResponseStatus`, `org.springframework.web.bind.annotation.PutMapping`, `org.springframework.web.bind.annotation.DeleteMapping`.
- **`getProductsByEan` (line 18–20):** No signature change; 404 is now raised by the service layer.
- **`getProductByEanAndStore` (line 22–24):** No signature change; 404 raised by service.
- **`addProduct` (line 26–28):** Add `@Valid` before `@RequestBody Product product`. Add `@ResponseStatus(HttpStatus.CREATED)` on the method to return 201 instead of 200 on success.
- **ADD `updateProduct`:**
  ```java
  @PutMapping("/{ean}/store/{storeId}")
  public Mono<Product> updateProduct(
          @PathVariable String ean,
          @PathVariable String storeId,
          @Valid @RequestBody Product product) {
      return productService.updateProduct(ean, storeId, product);
  }
  ```
- **ADD `deleteProduct`:**
  ```java
  @DeleteMapping("/{ean}/store/{storeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteProduct(
          @PathVariable String ean,
          @PathVariable String storeId) {
      return productService.deleteProduct(ean, storeId);
  }
  ```

### H. `src/test/java/com/mahmoud/firstChallenge/ProductServiceTest.java`
Replace/extend the existing two tests and add the following (all using `Mockito.mock(ProductRepository.class)` + `StepVerifier`):
- **`testGetProductsByEan`** — existing, keep as-is.
- **`testGetProductsByEan_notFound`** — mock `findByEan` returns `Flux.empty()`; `StepVerifier` expects `ProductNotFoundException`.
- **`testGetProductByEanAndStore`** — existing, keep as-is.
- **`testGetProductByEanAndStore_notFound`** — mock `findByEanAndStoreId` returns `Mono.empty()`; `StepVerifier` expects `ProductNotFoundException`.
- **`testSaveProduct_success`** — mock `findByEanAndStoreId` returns `Mono.empty()`, `save` returns product; `StepVerifier` expects product.
- **`testSaveProduct_conflict`** — mock `findByEanAndStoreId` returns existing product; `StepVerifier` expects `ProductConflictException`.
- **`testUpdateProduct_success`** — mock `findByEanAndStoreId(ean, storeId)` returns existing product, `save` returns updated; `StepVerifier` expects result.
- **`testUpdateProduct_notFound`** — mock `findByEanAndStoreId` returns `Mono.empty()`; `StepVerifier` expects `ProductNotFoundException`.
- **`testUpdateProduct_conflict`** — mock first `findByEanAndStoreId(ean, storeId)` returns existing; mock second `findByEanAndStoreId(newEan, newStoreId)` returns conflicting product; `StepVerifier` expects `ProductConflictException`.
- **`testDeleteProduct_success`** — mock `findByEanAndStoreId` returns product, `deleteById` returns `Mono.empty()`; `StepVerifier` verifies complete.
- **`testDeleteProduct_notFound`** — mock `findByEanAndStoreId` returns `Mono.empty()`; `StepVerifier` expects `ProductNotFoundException`.

Imports to add: `com.mahmoud.firstChallenge.exception.ProductNotFoundException`, `com.mahmoud.firstChallenge.exception.ProductConflictException`.

### I. `src/test/java/com/mahmoud/firstChallenge/ProductControllerTest.java`
Keep the two existing tests. Add using `WebTestClient.bindToController(new ProductController(productService)).build()` plus `RouterFunctions` / `ExceptionHandlerExceptionResolver` if needed (see Risk below). All new tests:
- **`testGetProductsByEan_notFound`** — service returns `Flux.error(new ProductNotFoundException(...))`; expect 404.
- **`testGetProductByEanAndStore_notFound`** — service returns `Mono.error(new ProductNotFoundException(...))`; expect 404.
- **`testAddProduct_valid`** — valid body, service returns product; expect 201.
- **`testAddProduct_blankEan`** — body with `ean=""`, service NOT called; expect 400 with `fieldErrors.ean` present.
- **`testAddProduct_negativePrice`** — body with `price=-1.0`; expect 400 with `fieldErrors.price` present.
- **`testAddProduct_conflict`** — service returns `Mono.error(new ProductConflictException(...))`; expect 409.
- **`testUpdateProduct_success`** — service returns updated product; expect 200.
- **`testUpdateProduct_notFound`** — service returns `Mono.error(new ProductNotFoundException(...))`; expect 404.
- **`testUpdateProduct_conflict`** — service returns `Mono.error(new ProductConflictException(...))`; expect 409.
- **`testUpdateProduct_invalid`** — blank EAN in body; expect 400.
- **`testDeleteProduct_success`** — service returns `Mono.empty()`; expect 204.
- **`testDeleteProduct_notFound`** — service returns `Mono.error(new ProductNotFoundException(...))`; expect 404.

**Important binding note for controller tests:** `WebTestClient.bindToController(...)` does NOT wire `@RestControllerAdvice` beans automatically. To make exception-handler tests work, use `WebTestClient.bindToControllerAdvice(new GlobalExceptionHandler()).bindToController(new ProductController(productService))` — or use `WebTestClient.bindToRouterFunction` + a manual `DispatcherHandler` setup. The simplest correct approach: use the `controllerAdvice` method that `WebTestClient` exposes. Exact call: `WebTestClient.bindToController(new ProductController(productService)).controllerAdvice(new GlobalExceptionHandler()).build()`.

### J. `README.md`
Replace/augment the **Endpoints** section with:

```markdown
## Endpoints

| Method | Path | Success | Error codes |
|--------|------|---------|-------------|
| GET    | `/products/ean/{ean}` | 200 | 404 (not found) |
| GET    | `/products/ean/{ean}/store/{storeId}` | 200 | 404 (not found) |
| POST   | `/products/ean` | 201 | 400 (validation), 409 (conflict) |
| PUT    | `/products/ean/{ean}/store/{storeId}` | 200 | 400 (validation), 404 (not found), 409 (conflict) |
| DELETE | `/products/ean/{ean}/store/{storeId}` | 204 | 404 (not found) |
```

---

## Commands

From the guardrails report — use these exactly:

```bash
# Full test suite (type-check + tests)
mvn test -B

# Compile only (fast type-check)
mvn test-compile -B
```

Gate script (`mvn test -B`) must exit 0 before the branch is merged.

---

## Implementation Approach (Step-by-Step)

1. **`pom.xml`** — add `spring-boot-starter-validation`. Run `mvn test-compile -B` to confirm dependencies resolve.
2. **Exception classes** — create `exception/ProductNotFoundException.java` and `exception/ProductConflictException.java`. These have no external dependencies; write them first.
3. **`GlobalExceptionHandler`** — create with the three `@ExceptionHandler` methods and the `ErrorResponse` record. It compiles independently of the service changes.
4. **`Product` model** — add `@NotBlank` / `@Positive` annotations. Confirm `mvn test-compile -B` still passes.
5. **`ProductService`** — extend all five methods as specified. Compile to verify.
6. **`ProductController`** — add `@Valid`, new endpoints. Compile to verify.
7. **`ProductServiceTest`** — expand with all new unit tests. Run `mvn test -B`.
8. **`ProductControllerTest`** — expand with all new controller/integration tests. Adjust `WebTestClient` builder to include `controllerAdvice(new GlobalExceptionHandler())`. Run `mvn test -B`.
9. **`README.md`** — update the endpoint table.
10. Verify `mvn test -B` exits 0. Commit.

---

## Risks and Edge Cases

### R1 — `WebTestClient.bindToController` and `@RestControllerAdvice`
`bindToController` does not auto-discover advice beans from the application context. Without explicitly passing `new GlobalExceptionHandler()` via `.controllerAdvice(...)`, the controller tests for 404/409/400 from exception handlers will see 500 instead of the expected status. **Mitigation:** use `WebTestClient.bindToController(...).controllerAdvice(new GlobalExceptionHandler()).build()` as specified in File I.

### R2 — Flux 404: status already committed
If any element is emitted before the stream errors, the 200 header is already written and a 404 handler cannot change it. This is only safe because `switchIfEmpty` triggers before any element is emitted (when the source is truly empty). Do not use `doOnError` or late-stage error injection. **Mitigation:** always apply `switchIfEmpty(Flux.error(...))` immediately on the repository call, before any downstream operator.

### R3 — `@Positive` vs `@PositiveOrZero` on price
`@Positive` rejects zero (`price = 0.0`). The issue says "negative or zero price" → 400, so `@Positive` (strictly > 0) is correct. If a zero-price product is a legitimate business case, this would need to be `@PositiveOrZero`. **For this plan, `@Positive` is used** consistent with the issue wording.

### R4 — `updateProduct` conflict check for same-key update (no EAN/storeId change)
When the EAN and storeId in the request body match the path params exactly, the conflict check is skipped (no second DB query). This is intentional: updating name/price on the same product is not a conflict. If the body ean/storeId differ from path params, an extra `findByEanAndStoreId` query is issued before saving. **No silent drop** — if the conflict check query fails (DB error), the error propagates to the caller as a 500.

### R5 — `saveProduct` conflict check atomicity
There is no atomic "find-or-insert" (MongoDB `upsert` with `setOnInsert`) in the reactive repo at this layer. Two concurrent POSTs with the same (ean, storeId) could both pass the check and both save, resulting in duplicate documents. For the scope of this issue this race condition is accepted. **Warn-and-surface:** if it occurs, the second save simply inserts a duplicate; no silent drop, but no 409 either. A truly atomic solution would require a unique MongoDB index and handling `DuplicateKeyException`. This is out of scope per the issue acceptance criteria.

### R6 — DELETE on a product with multiple EAN matches
`findByEanAndStoreId` returns the single product for the exact (ean, storeId) key. `deleteById` then removes only that document. No other products are affected. This is safe by design.

### R7 — Validation on `storeId` in the Product model
`storeId` is annotated `@NotBlank`. For the POST body, `storeId` must be supplied. For PUT, the path params carry the identity; the body's `storeId` is used only if it differs (triggers conflict check). **Warn-and-surface:** if the PUT body has a blank `storeId`, validation rejects it with 400 and a field error for `storeId`. This is the correct behaviour — it makes a malformed request visible.

---

## Test Strategy

- **Unit tests (`ProductServiceTest`):** pure Mockito mocks of `ProductRepository`. No Spring context, no MongoDB. Each method under test is exercised for the success path and every error path (not-found, conflict). Use `StepVerifier` for reactive assertions.
- **Controller tests (`ProductControllerTest`):** `WebTestClient` bound to controller class with mocked `ProductService` and `GlobalExceptionHandler` wired as advice. Tests verify HTTP status codes and, for 400 responses, the presence of `fieldErrors` in the JSON body. No Spring application context or MongoDB required.
- **`FirstChallengeApplicationTests`:** existing Spring Boot context test — it loads the context; keep as-is (it fails to connect to MongoDB at runtime but Spring Boot's test slice still loads the context with Flapdoodle/Embedded or accepts the connection failure gracefully, as proven by the guardrails run passing with `Tests run: 1`).

---

## Estimated Complexity

**Medium** — the reactive patterns (`switchIfEmpty`, chained `flatMap`) require care, and the `WebTestClient` advice-wiring quirk in tests is a known gotcha. No new infrastructure (no new DB schema, no new Spring profile, no configuration files). All changes are in well-contained Java classes.
