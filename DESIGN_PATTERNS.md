# Design Patterns

This document covers Part II §8 of the project requirements: implement and document at least one distributed-systems design pattern.

## Pattern: Saga (Orchestration)

The Saga pattern manages a long-running business transaction that spans multiple services without using a global ACID transaction. Each step commits locally, and if a later step fails the orchestrator runs compensating actions (in reverse order) to undo the committed steps.

### Why this pattern fits TasteTest

Creating a restaurant is a small distributed transaction:

1. **Persist** the restaurant in `restaurant-service`'s Postgres database.
2. **Notify** the owner via `notification-service` (REST call routed through Eureka load balancing).

Step 1 is a local DB write; step 2 crosses a service boundary. A single JPA transaction cannot atomically wrap both. If the notification call fails after the row is committed, the system would otherwise be left with a "live" restaurant whose owner was never notified — a silent partial success. The saga makes that failure explicit and reverses step 1.

### Implementation

| Component | File |
|---|---|
| Orchestrator | [restaurant-service/.../saga/AddRestaurantSaga.java](restaurant-service/src/main/java/com/example/restaurant/saga/AddRestaurantSaga.java) |
| Outbound REST client | [restaurant-service/.../saga/NotificationClient.java](restaurant-service/src/main/java/com/example/restaurant/saga/NotificationClient.java) |
| Load-balanced RestTemplate + JWT forwarding | [restaurant-service/.../config/RestClientConfig.java](restaurant-service/src/main/java/com/example/restaurant/config/RestClientConfig.java) |
| Saga entry point | `RestaurantService.addRestaurant` in [RestaurantService.java](restaurant-service/src/main/java/com/example/restaurant/service/RestaurantService.java) |

### Step / Compensation table

| # | Step | Service | Compensation |
|---|---|---|---|
| 1 | `stepPersistRestaurant` — `INSERT` into `project.restaurants` | restaurant-service (local DB) | `compensateDeleteRestaurant` — `DELETE` by id |
| 2 | `stepNotifyOwner` — `POST /api/notifications` | notification-service (remote, via Eureka) | — (terminal step) |

### Transaction boundaries

The saga deliberately runs **outside** any enclosing JPA transaction:

- `RestaurantService.addRestaurant` is annotated `@Transactional(NOT_SUPPORTED)`, suspending the class-level transaction.
- The saga uses a `TransactionTemplate` configured with `PROPAGATION_REQUIRES_NEW`, so each persist / compensation step opens, commits, and closes its own transaction independently.

This is what makes compensation meaningful — without it, an uncommitted DB write would simply roll back via JPA, and there would be nothing to "compensate."

### Inter-service call & security

`NotificationClient` issues a `POST` to `http://notification-service/api/notifications`. The host segment is a logical Eureka service id, resolved by Spring Cloud LoadBalancer to a concrete instance.

Both services sit behind JWT authentication (Part II §6). The `RestClientConfig` interceptor copies the inbound `Authorization` header from the current request onto the outbound request, so the notification call is authenticated as the same end user that created the restaurant. No service-to-service credential is needed.

### Failure scenarios

| Failure point | Outcome |
|---|---|
| Step 1 throws (DB down, validation, etc.) | No compensation needed — nothing was committed. Saga rethrows as `SagaFailedException`. |
| Step 2 throws (notification-service down, 5xx, network) | Compensation runs: the restaurant row is deleted. Caller sees `SagaFailedException`. |
| Compensation itself fails | Logged at ERROR; saga still rethrows the original exception. The system is now in an inconsistent state and would require manual reconciliation — this is the well-known limit of compensation-based sagas. |

### Trade-offs

- **Pro:** No 2-phase commit, no distributed transaction coordinator, no shared DB.
- **Pro:** Each step is a normal, observable HTTP/JPA call — easy to log and trace.
- **Con:** Compensations are best-effort; if they fail you need an out-of-band reconciliation job.
- **Con:** The system is *eventually* consistent during the window between step 1 commit and step 2 completion. A reader querying restaurants in that window may see a row that the saga later compensates away.

### Why orchestration, not choreography

The two alternatives are:

- **Choreography** — services react to each other's events on a broker (Kafka/RabbitMQ). No central coordinator.
- **Orchestration** — one service drives the saga and explicitly invokes the others.

Orchestration was chosen because (a) the project already uses synchronous REST + Eureka, with no message broker provisioned, and (b) the flow has only two steps with one compensation, which is too small to justify the operational cost of a broker. If the saga grew beyond ~3 steps or needed retries with backoff, choreography on a broker would become the better fit.
