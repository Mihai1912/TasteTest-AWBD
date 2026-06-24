# Șabloane de proiectare (Design Patterns)

Acest document acoperă Partea a II-a §8 din cerințele proiectului: implementarea și documentarea a cel puțin unui șablon de proiectare specific sistemelor distribuite.

## Șablon: Saga (Orchestrare)

Șablonul Saga gestionează o tranzacție de business de lungă durată care se întinde pe mai multe servicii, fără a folosi o tranzacție ACID globală. Fiecare pas se finalizează (commit) local, iar dacă un pas ulterior eșuează, orchestratorul rulează acțiuni de compensare (în ordine inversă) pentru a anula pașii deja finalizați.

### De ce se potrivește acest șablon pentru TasteTest

Crearea unui restaurant este o mică tranzacție distribuită:

1. **Persistă** restaurantul în baza de date Postgres a `restaurant-service`.
2. **Notifică** proprietarul prin `notification-service` (apel REST rutat prin load balancing-ul Eureka).

Pasul 1 este o scriere locală în baza de date; pasul 2 traversează o graniță de serviciu. O singură tranzacție JPA nu poate înfășura atomic ambele operațiuni. Dacă apelul de notificare eșuează după ce rândul a fost comis, sistemul ar rămâne altfel cu un restaurant "live" al cărui proprietar nu a fost niciodată notificat — un succes parțial silențios. Saga face acest eșec explicit și inversează pasul 1.

### Implementare

| Componentă | Fișier |
|---|---|
| Orchestrator | [restaurant-service/.../saga/AddRestaurantSaga.java](../restaurant-service/src/main/java/com/example/restaurant/saga/AddRestaurantSaga.java) |
| Client REST de ieșire | [restaurant-service/.../saga/NotificationClient.java](../restaurant-service/src/main/java/com/example/restaurant/saga/NotificationClient.java) |
| RestTemplate cu load-balancing + transmitere JWT | [restaurant-service/.../config/RestClientConfig.java](../restaurant-service/src/main/java/com/example/restaurant/config/RestClientConfig.java) |
| Punctul de intrare al saga | `RestaurantService.addRestaurant` în [RestaurantService.java](../restaurant-service/src/main/java/com/example/restaurant/service/RestaurantService.java) |

### Tabel Pas / Compensare

| # | Pas | Serviciu | Compensare |
|---|---|---|---|
| 1 | `stepPersistRestaurant` — `INSERT` în `project.restaurants` | restaurant-service (BD locală) | `compensateDeleteRestaurant` — `DELETE` după id |
| 2 | `stepNotifyOwner` — `POST /api/notifications` | notification-service (remote, prin Eureka) | — (pas terminal) |

### Granițele tranzacțiilor

Saga rulează deliberat **în afara** oricărei tranzacții JPA înglobante:

- `RestaurantService.addRestaurant` este adnotată `@Transactional(NOT_SUPPORTED)`, suspendând tranzacția de la nivel de clasă.
- Saga folosește un `TransactionTemplate` configurat cu `PROPAGATION_REQUIRES_NEW`, astfel încât fiecare pas de persistare / compensare își deschide, comite și închide propria tranzacție în mod independent.

Tocmai acest lucru face ca compensarea să aibă sens — fără el, o scriere necomisă în baza de date ar face pur și simplu rollback prin JPA și nu ar exista nimic de "compensat".

### Apelul inter-servicii și securitatea

`NotificationClient` emite un `POST` către `http://tastetest-notification-service/api/notifications`. Segmentul de host este id-ul logic al serviciului Eureka (care corespunde cu `spring.application.name` din `notification-service`), rezolvat de Spring Cloud LoadBalancer la o instanță concretă.

Ambele servicii sunt protejate de autentificare JWT (Partea a II-a §6). Interceptorul din `RestClientConfig` copiază antetul `Authorization` de intrare din cererea curentă pe cererea de ieșire, astfel încât apelul de notificare este autentificat ca același utilizator final care a creat restaurantul. Nu este nevoie de credențiale serviciu-la-serviciu.

### Scenarii de eșec

| Punctul de eșec | Rezultat |
|---|---|
| Pasul 1 aruncă excepție (BD picată, validare etc.) | Nu este nevoie de compensare — nimic nu a fost comis. Saga aruncă din nou ca `SagaFailedException`. |
| Pasul 2 aruncă excepție (notification-service picat, 5xx, rețea) | Compensarea rulează: rândul restaurantului este șters. Apelantul vede `SagaFailedException`. |
| Compensarea însăși eșuează | Logat la nivel ERROR; saga aruncă totuși din nou excepția originală. Sistemul este acum într-o stare inconsistentă și ar necesita reconciliere manuală — aceasta este limita binecunoscută a saga-urilor bazate pe compensare. |

### Compromisuri

- **Pro:** Fără two-phase commit, fără coordonator de tranzacții distribuite, fără BD partajată.
- **Pro:** Fiecare pas este un apel HTTP/JPA normal, observabil — ușor de logat și de urmărit.
- **Contra:** Compensările sunt best-effort; dacă eșuează, ai nevoie de un job de reconciliere în afara fluxului.
- **Contra:** Sistemul este consistent *în cele din urmă* (eventually consistent) în fereastra dintre commit-ul pasului 1 și finalizarea pasului 2. Un cititor care interoghează restaurantele în acea fereastră poate vedea un rând pe care saga îl compensează ulterior.

### De ce orchestrare, nu coreografie

Cele două alternative sunt:

- **Coreografie** — serviciile reacționează la evenimentele celorlalte pe un broker (Kafka/RabbitMQ). Fără coordonator central.
- **Orchestrare** — un serviciu conduce saga și invocă explicit celelalte servicii.

S-a ales orchestrarea deoarece (a) proiectul folosește deja REST sincron + Eureka, fără niciun broker de mesaje provizionat, și (b) fluxul are doar doi pași cu o singură compensare, ceea ce este prea puțin pentru a justifica costul operațional al unui broker. Dacă saga ar crește peste ~3 pași sau ar avea nevoie de reîncercări cu backoff, coreografia pe un broker ar deveni opțiunea mai potrivită.
