# Drone Dispatch Service

REST API for a fleet of drones delivering medications to locations with
difficult access.

Java 17+ · Spring Boot 4.1.0 · Spring Data JPA · H2 (in-memory)

## Build

Git Bash, macOS or Linux:
```bash
./mvnw clean package
```

Windows PowerShell:
```powershell
.\mvnw.cmd clean package
```

## Run

Git Bash, macOS or Linux:
```bash
./mvnw spring-boot:run
```

Windows PowerShell:
```powershell
.\mvnw.cmd spring-boot:run
```

Starts on http://localhost:8080

H2 console: http://localhost:8080/h2-console
JDBC URL `jdbc:h2:mem:dronedb` · user `sa` · blank password

10 drones and 2 medications are preloaded at startup from
`src/main/resources/data.sql`.

## Test

Git Bash, macOS or Linux:
```bash
./mvnw clean test
```

Windows PowerShell:
```powershell
.\mvnw.cmd clean test
```

17 tests, all passing. Full results in [`docs/TEST_RESULTS.md`](docs/TEST_RESULTS.md).

## APIs

All requests and responses are JSON. No authentication.

| Use case | Method | Path |
|----------|--------|------|
| Registering a drone | POST | `/api/v1/drones` |
| Loading a drone with medication | POST | `/api/v1/drones/{serialNumber}/medications` |
| Checking loaded medications for a given drone | GET | `/api/v1/drones/{serialNumber}/medications` |
| Checking available drones for loading | GET | `/api/v1/drones/available` |
| Check drone information (battery) | GET | `/api/v1/drones/{serialNumber}/battery` |

Import `docs/DroneDispatch.postman_collection.json` into Postman to run these.

### 1. Registering a drone

`POST /api/v1/drones` → `201 Created`

```bash
curl -X POST http://localhost:8080/api/v1/drones \
  -H "Content-Type: application/json" \
  -d '{
    "serialNumber": "DRN-2001-MW",
    "model": "MIDDLEWEIGHT",
    "weightLimit": 500,
    "batteryCapacity": 90
  }'
```

Request:
```json
{
  "serialNumber": "DRN-2001-MW",
  "model": "MIDDLEWEIGHT",
  "weightLimit": 500,
  "batteryCapacity": 90
}
```

Response:
```json
{
  "availableForLoading": true,
  "batteryCapacity": 90,
  "currentLoadWeight": 0,
  "id": 11,
  "medications": [],
  "model": "MIDDLEWEIGHT",
  "remainingCapacity": 500,
  "serialNumber": "DRN-2001-MW",
  "state": "IDLE",
  "weightLimit": 500
}
```

`model` accepts `LIGHTWEIGHT`, `MIDDLEWEIGHT`, `CRUISERWEIGHT` or `HEAVYWEIGHT`.

### 2. Loading a drone with medication

`POST /api/v1/drones/{serialNumber}/medications` → `200 OK`

Medications are sent as an array and loaded atomically: one violation
rejects the whole request. `image` is optional and holds a base64 payload
or a URL.

```bash
curl -X POST http://localhost:8080/api/v1/drones/DRN-2001-MW/medications \
  -H "Content-Type: application/json" \
  -d '[
    {"name": "Paracetamol_500", "weight": 150, "code": "PARA_500", "image": "aGVsbG8="},
    {"name": "Amoxicillin-250", "weight": 200, "code": "AMOX_250"}
  ]'
```

Request:
```json
[
  {
    "name": "Paracetamol_500",
    "weight": 150,
    "code": "PARA_500",
    "image": "aGVsbG8="
  },
  {
    "name": "Amoxicillin-250",
    "weight": 200,
    "code": "AMOX_250"
  }
]
```

Response:
```json
{
  "availableForLoading": false,
  "batteryCapacity": 90,
  "currentLoadWeight": 350,
  "id": 11,
  "medications": [
    {"code": "PARA_500", "id": 3, "image": "aGVsbG8=", "name": "Paracetamol_500", "weight": 150},
    {"code": "AMOX_250", "id": 4, "image": null, "name": "Amoxicillin-250", "weight": 200}
  ],
  "model": "MIDDLEWEIGHT",
  "remainingCapacity": 150,
  "serialNumber": "DRN-2001-MW",
  "state": "LOADED",
  "weightLimit": 500
}
```

### 3. Checking loaded medications for a given drone

`GET /api/v1/drones/{serialNumber}/medications` → `200 OK`

```bash
curl http://localhost:8080/api/v1/drones/DRN-2001-MW/medications
```

Response:
```json
[
  {"code": "PARA_500", "id": 3, "image": "aGVsbG8=", "name": "Paracetamol_500", "weight": 150},
  {"code": "AMOX_250", "id": 4, "image": null, "name": "Amoxicillin-250", "weight": 200}
]
```

### 4. Checking available drones for loading

`GET /api/v1/drones/available` → `200 OK`

Returns drones that are IDLE or LOADING, have at least 25% battery and
still have free capacity.

```bash
curl http://localhost:8080/api/v1/drones/available
```

Response:
```json
[
  {
    "availableForLoading": true,
    "batteryCapacity": 100,
    "currentLoadWeight": 0,
    "id": 1,
    "medications": [],
    "model": "LIGHTWEIGHT",
    "remainingCapacity": 200,
    "serialNumber": "DRN-0001-LW",
    "state": "IDLE",
    "weightLimit": 200
  }
]
```

### 5. Check drone information (battery)

`GET /api/v1/drones/{serialNumber}/battery` → `200 OK`

```bash
curl http://localhost:8080/api/v1/drones/DRN-0001-LW/battery
```

Response:
```json
{
  "availableForLoading": true,
  "batteryCapacity": 100,
  "currentLoadWeight": 0,
  "id": 1,
  "medications": [],
  "model": "LIGHTWEIGHT",
  "remainingCapacity": 200,
  "serialNumber": "DRN-0001-LW",
  "state": "IDLE",
  "weightLimit": 200
}
```

### Error responses

Every error returns the same JSON shape.

| Scenario | Status | Message |
|---|---|---|
| Load exceeds the weight limit | `422` | `Drone 'DRN-2001-MW' cannot carry 500g: only 200g of its 200g weight limit is still free.` |
| Battery below 25% | `422` | `Drone 'DRN-0008-MW' cannot enter the LOADING state: battery is 15% but at least 25% is required.` |
| Serial number already registered | `422` | `A drone with serial number 'DRN-2001-MW' is already registered.` |
| Medication code not uppercase | `400` | `loadMedications.medications[0].code: code allows only uppercase letters, numbers and '_'` |
| Weight limit above the model capacity | `400` | `Model LIGHTWEIGHT supports at most 250g, but 900g was requested.` |
| Weight limit above 1000g | `400` | `weightLimit: weightLimit must not exceed 1000g` |
| Empty medication array | `400` | `At least one medication must be provided.` |
| Unknown serial number | `404` | `No drone found with serial number 'NOPE-999'.` |

Example — loading 500 g onto a drone with a 200 g limit:

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Drone 'DRN-2001-MW' cannot carry 500g: only 200g of its 200g weight limit is still free."
}
```

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