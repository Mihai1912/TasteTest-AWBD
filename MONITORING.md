# Monitorizare si metrici

Stack-ul de observability din proiect este format din:

- Spring Boot Actuator pe fiecare serviciu
- Prometheus pentru colectarea metricilor
- Grafana pentru dashboard
- Zipkin pentru distributed tracing

## Unde le accesezi

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- Zipkin: http://localhost:9411

Login Grafana:

- user: `admin`
- parola: `admin`

## Ce verifici in demo

### 1. Health checks

Fiecare serviciu expune endpoint-ul:

- `/actuator/health`

Exemple de servicii:

- `http://localhost:8090/actuator/health` pentru backend
- `http://localhost:8091/actuator/health` pentru auth
- `http://localhost:8092/actuator/health` pentru users
- `http://localhost:8093/actuator/health` pentru restaurants
- `http://localhost:8888/actuator/health` pentru config-server
- `http://localhost:8761/actuator/health` pentru discovery-server
- `http://localhost:8082/actuator/health` pentru notification-service

### 2. Metrici in Prometheus

In Prometheus mergi la tab-ul `Graph` si poti rula query-uri precum:

```promql
up
```

```promql
process_cpu_usage
```

```promql
jvm_memory_used_bytes
```

```promql
rate(http_server_requests_seconds_count[5m])
```

Daca vrei sa verifici daca toate serviciile sunt scrapped corect, deschide:

- Status -> Targets

Ar trebui sa vezi job-urile pentru toate serviciile Spring si sa fie `UP`.

### 3. Dashboard in Grafana

In Grafana gasesti dashboard-ul:

- `TasteTest Observability`

Acesta afiseaza:

- CPU usage
- JVM heap memory
- HTTP requests / second

Daca dashboard-ul nu apare imediat, intra la `Dashboards` si cauta folderul `TasteTest`.

### 4. Distributed tracing

In Zipkin:

- genereaza cateva request-uri in aplicatie
- apoi deschide Zipkin si cauta trace-uri dupa serviciu

Daca nu vezi nimic, inseamna de obicei ca:

- nu a fost trafic suficient
- sau serviciul respectiv nu a generat spans inca

## Workflow rapid pentru prezentare

1. Deschide `/actuator/health` pe un serviciu si arata ca este `UP`.
2. Deschide Prometheus si verifica `Status -> Targets`.
3. Deschide Grafana si arata dashboard-ul cu CPU, memorie si request rate.
4. Deschide Zipkin si arata trace-urile dupa un request facut in aplicatie.

## Observatii

- Configurarea metricilor este expusa prin Actuator si colectata automat de Prometheus.
- Grafana foloseste sursa de date Prometheus deja provisionata.
- Zipkin este optional, dar util ca bonus pentru tracing distribuit.
