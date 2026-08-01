# Drone Dispatch Service

REST API for a fleet of drones delivering medications to locations with
difficult access.

Java 17+ · Spring Boot 4.1.0 · Spring Data JPA · H2 (in-memory)

## Build

```bash
.\mvnw.cmd clean package
```

## Run

```bash
.\mvnw.cmd spring-boot:run
```

Starts on http://localhost:8080

H2 console: http://localhost:8080/h2-console
JDBC URL `jdbc:h2:mem:dronedb` · user `sa` · blank password

6 drones and 2 medications are preloaded at startup from
`src/main/resources/data.sql`.

## Test

```bash
.\mvnw.cmd clean test
```

On macOS/Linux use `./mvnw` instead of `.\mvnw.cmd`.

## APIs

| Use case | Method | Path |
|----------|--------|------|
| Registering a drone | POST | `/api/v1/drones` |
| Loading a drone with medication | POST | `/api/v1/drones/{serialNumber}/medications` |
| Checking loaded medications for a given drone | GET | `/api/v1/drones/{serialNumber}/medications` |
| Checking available drones for loading | GET | `/api/v1/drones/available` |
| Check drone information (battery) | GET | `/api/v1/drones/{serialNumber}/battery` |

Import `docs/DroneDispatch.postman_collection.json` into Postman to test these.

## Design decisions

- **Maximum capacity per model:** LIGHTWEIGHT 250g, MIDDLEWEIGHT 500g,
  CRUISERWEIGHT 750g, HEAVYWEIGHT 1000g. The 1000g limit was split evenly
  across the four models. A drone's weight limit cannot exceed its model
  capacity or the 1000g maximum.

- **Battery per delivery:** 10% is deducted when a delivery completes,
  floored at 0%.

- **Scheduler:** runs every 10 seconds and advances one step per cycle:
  LOADED to DELIVERING to DELIVERED to RETURNING to IDLE. Medications are
  released at DELIVERED, so a loaded drone returns to IDLE and empty about
  40 seconds after loading. Transitions are logged to the console. Preloaded
  drone DRN-0004-MW starts LOADED so the cycle is visible right after startup.

- **Loading:** medications are sent as a JSON array and loaded atomically.
  One violation rejects the whole request. The drone is marked LOADED at the
  end of the batch.

- **Medication image:** optional string holding a base64 payload or a URL,
  so the API stays pure JSON.

- **Serial number:** unique, maximum 100 characters, used as the identifier
  in every path.