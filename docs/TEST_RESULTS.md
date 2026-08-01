# Unit Test Results

```bash
./mvnw clean test          # macOS / Linux
.\mvnw.cmd clean test      # Windows
```

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.thedrone.dispatch.entity.DroneTest
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.143 s

Running com.thedrone.dispatch.service.DroneServiceTest
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.880 s

Running com.thedrone.dispatch.DroneDispatchServiceApplicationTests
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 18.58 s

[INFO] Results:
[INFO]
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

## Test cases

`DroneTest` — business rules

1. A newly registered drone is IDLE and empty
2. Maximum capacity is based on drone model
3. Prevent drone being overloaded than maximum capacity
4. Prevent drone entering LOADING when battery is below 25%
5. Battery is reduced after delivery completion and the payload is released
6. Illegal state jumps are rejected
7. Medication name and code formats are enforced

`DroneServiceTest` — integration against in-memory H2

1. Preloaded data is available at startup
2. Registering a drone returns it in IDLE state
3. Loading medication moves the drone to LOADED and persists the payload
4. Loading beyond the weight limit is rejected
5. Loading a drone below 25% battery is rejected
6. A duplicate serial number is rejected
7. Unknown serial numbers raise a not-found error
8. Only drones with battery >= 25% and free capacity are available
9. The scheduler advances a LOADED drone through the full delivery cycle

`DroneDispatchServiceApplicationTests`

1. contextLoads — the application starts and all beans wire correctly

