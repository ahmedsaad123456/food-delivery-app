# Food Delivery App Backend

A food-delivery REST backend built with Spring Boot. It covers restaurants and menus, search, a cart with pricing rules, orders with a guarded state machine, Stripe payments with webhooks, stock control under concurrent load, driver assignment, order tracking and reviews, and reliable event delivery through a transactional outbox.

## Tech stack

| Area | Technology |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 4.1.1 (Spring Framework 7.0.9) |
| Persistence | PostgreSQL 16, Spring Data JPA (Hibernate 7.4.5), Flyway 12 migrations |
| Cache / key-value | Redis |
| Documents | MongoDB |
| Security | Spring Security, JWT, role and authority based access control |
| Payments | Stripe Checkout (test mode) and Stripe webhooks |
| Mapping / boilerplate | MapStruct, Lombok |
| API docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers 2.0.5 |
| Infrastructure | Docker Compose |

## Features

### API foundation
- Uniform response envelope on every endpoint.
- Central error handling with `@RestControllerAdvice`, consistent error codes, no stack traces leaked.
- Correlation id filter that puts the id in the logging MDC so every log line of a request can be traced.
- Classic layered architecture (Controller → Service → Repository) with feature-based packages.
- DTOs at every API boundary (entities are never exposed) and MapStruct for mapping.
- Jakarta Bean Validation, including a custom Egyptian mobile-number constraint.
- Schema fully owned by Flyway migrations; API documented with Swagger UI.

### Restaurants, categories and menus
- Full CRUD for restaurants, categories and menu items.
- Lazy JPA relations with foreign keys enforced in the database.
- Menu data cached in Redis, evicted when a menu changes.

### Search and pagination
- Dynamic search with JPA Specifications: optional filters for name, cuisine, price range, rating and availability.
- Sorting and pagination with a maximum page size.
- Menus grouped by category with Java Streams.
- DTO projections instead of full entities.
- N+1 queries found and removed with entity graphs.

### Authentication and authorization
- Registration and login with BCrypt-hashed passwords and short-lived JWT access tokens.
- Custom authentication filter in the Spring Security filter chain that builds the security context.
- Custom entry-point and access-denied handlers returning the same JSON error format.
- RBAC: roles and permissions stored in the database and enforced with method security (`@PreAuthorize`).
- Ownership checks, for example only a restaurant's owner can edit it.
- JWT secret supplied through the environment.

### Cart and pricing
- Cart stored in Redis.
- `Money` value object (immutable, built on `BigDecimal`).
- Discounts built with the Strategy pattern (percentage, fixed amount, free delivery).
- All discount strategies follow one behavioural contract, so any of them can replace another (Liskov Substitution Principle).
- Taxes and service fees layered on with the Decorator pattern.

### Orders
- Domain-driven design: `Order` is an aggregate root that owns its items and enforces its own invariants.
- Guarded state machine: only legal status transitions are allowed, otherwise a domain exception is thrown.
- Order items keep a snapshot of name and price at order time, so later menu edits never change past orders.
- Placing an order runs in a single database transaction.
- Every status change publishes an application event; a listener records it on the order timeline.

### Payments with Stripe
- Checkout Sessions created behind small, focused interfaces (Interface Segregation Principle) implemented by a Stripe adapter (Adapter pattern), so the domain never depends on Stripe types.
- Idempotency keys on session creation.
- Webhooks are the source of truth for payment: signature verified on the raw request body, endpoint protected by the signature instead of JWT.
- Idempotent webhook consumer: processed events are recorded in the same transaction as the work, so duplicates are ignored.
- One handler per event type, resolved through a handler map.
- Successful payment moves the order to `PAID` and raises a domain event for follow-up work.
- Refunds supported.
- Stripe keys and webhook secret live only in environment variables.

### Inventory and concurrency
- Stock is checked at checkout; no stock is reserved for unpaid carts.
- Stock is deducted after payment with a single atomic conditional `UPDATE`, so overselling is impossible even with many simultaneous buyers and across several application instances.
- If an item sells out between checkout and payment, the order is cancelled and the payment is refunded automatically.
- Optimistic locking (`@Version` with retry), pessimistic locking and the atomic update were compared under a 50-thread race test before choosing the atomic update.

### Asynchronous processing
- Dedicated `ThreadPoolTaskExecutor` with explicit core, max and queue sizing.
- `@Async` and `CompletableFuture` composition for post-payment work.
- `TaskDecorator` that carries the security context and MDC to worker threads and cleans them up afterwards.
- Available drivers tracked in memory with `ConcurrentHashMap` and atomic compute operations.

### Notifications (simulated)
- Small sender interfaces for email, SMS and push (Interface Segregation Principle).
- Template Method base class for the build, deliver and log steps.
- Factory that picks the sender by type.
- Shared contract so every sender accepts any valid message (Liskov Substitution Principle).
- Triggered by application events.
- Delivery is simulated: a sender only writes a log line, and no email or SMS provider is connected.

### Delivery and driver assignment
- Drivers have availability states and their own CRUD.
- Assignment uses the Strategy pattern with two interchangeable policies: nearest driver and least busy driver.
- The active policy is chosen by configuration.
- Both policies are verified by one shared contract test, so either can be swapped without touching the caller.
- Booking a driver is an atomic database operation, so two orders can never get the same driver.
- The surrounding `OrderFulfillmentService` (ETA estimation and driver assignment after payment) is simulated: it runs the flow, but the ETA is a placeholder value, not a real routing computation.

### Order tracking and reviews
- Order timeline and customer reviews stored in MongoDB, since they are flexible, append-heavy and not business-critical.
- Average rating per restaurant computed with a MongoDB aggregation pipeline.
- Money, stock and orders stay in PostgreSQL for ACID guarantees.

### Event-driven reliability (Transactional Outbox)
- Events that must not be lost, such as payment succeeded, are written to an outbox table in the same transaction as the business change, which removes the dual-write problem.
- Scheduled poller claims events with `FOR UPDATE SKIP LOCKED`, so several instances can poll safely.
- Retry with exponential back-off and a maximum attempt count; after that the event is marked `FAILED`.
- At-least-once delivery, with idempotent consumers through a processed-events record per consumer.
- Payment fulfillment and the confirmation email are separate outbox events, so a failure in one never blocks the other.
- All outbox SQL sits behind repositories.

## Testing
  - Unit tests for the outbox processor and its payment handlers (Mockito).
  - Integration test on a real PostgreSQL (Testcontainers) where 50 threads compete for 5 units and exactly 5 purchases must succeed, repeated to catch flaky races.

## Skills

**Programming Languages:** Java

**Backend & Frameworks:** Spring Boot, Spring Data JPA, Hibernate, Spring Security, Spring Events, Bean Validation, MapStruct, Java Streams, Multithreading

**Databases:** PostgreSQL, MongoDB, Redis, Flyway migrations

**DevOps & Tools:** Docker, Docker Compose, Stripe CLI

**Core Computer Science & Architecture:** Object-Oriented Programming (OOP), SOLID Principles, Design Patterns, Domain-Driven Design (DDD), Event-Driven Architecture, Outbox Pattern, Idempotency, Concurrency Control (optimistic, pessimistic, atomic updates)

**Security & Authentication:** JWT Authentication, Role-Based Access Control (RBAC), Webhook Signature Verification, API Security Best Practices

**API Design:** RESTful API Design, Pagination, Filtering, Sorting, Consistent Response Structures, HTTP Status Codes, OpenAPI / Swagger

**Payments:** Stripe Checkout, Webhooks, Refunds

**Testing:** JUnit 5, Mockito, Testcontainers.
