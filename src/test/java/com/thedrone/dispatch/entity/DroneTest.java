package com.thedrone.dispatch.entity;

import com.thedrone.dispatch.enums.DroneModel;
import com.thedrone.dispatch.enums.DroneState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pure unit tests of the business rules - no Spring context needed. */
class DroneTest {

    private Drone droneWith(int weightLimit, int battery) {
        return Drone.register("DRN-TEST-01", DroneModel.MIDDLEWEIGHT, weightLimit, battery);
    }

    @Test
    @DisplayName("A newly registered drone is IDLE and empty")
    void newDroneStartsIdle() {
        Drone drone = droneWith(500, 100);

        assertEquals(DroneState.IDLE, drone.getState());
        assertEquals(0, drone.currentLoadWeight());
        assertEquals(500, drone.remainingCapacity());
        assertTrue(drone.isAvailableForLoading());
    }

    @Test
    @DisplayName("Maximum capacity is based on drone model")
    void weightLimitCannotExceedModelCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> Drone.register("DRN-X", DroneModel.LIGHTWEIGHT, 400, 100));
    }

    @Test
    @DisplayName("Prevent drone being overloaded than maximum capacity")
    void preventsOverload() {
        Drone drone = droneWith(300, 100);
        drone.load(Medication.of("Paracetamol_500", 250, "PARA_500", null));

        assertThrows(IllegalStateException.class,
                () -> drone.load(Medication.of("Ibuprofen", 100, "IBU_200", null)));
        assertEquals(250, drone.currentLoadWeight());
    }

    @Test
    @DisplayName("Prevent drone entering LOADING when battery is below 25%")
    void preventsLoadingWithLowBattery() {
        Drone drone = droneWith(500, 24);

        assertFalse(drone.isAvailableForLoading());
        assertThrows(IllegalStateException.class,
                () -> drone.load(Medication.of("Insulin", 50, "INS_01", null)));
    }

    @Test
    @DisplayName("Battery is reduced after delivery completion and the payload is released")
    void drainsBatteryAndUnloads() {
        Drone drone = droneWith(500, 60);
        drone.load(Medication.of("Insulin", 100, "INS_01", null));
        drone.markLoaded();

        assertEquals(DroneState.LOADED, drone.getState());

        drone.moveTo(DroneState.DELIVERING);
        drone.moveTo(DroneState.DELIVERED);
        drone.drainBattery(10);
        drone.unload();
        drone.moveTo(DroneState.RETURNING);
        drone.moveTo(DroneState.IDLE);

        assertEquals(DroneState.IDLE, drone.getState());
        assertEquals(50, drone.getBatteryCapacity());
        assertEquals(0, drone.currentLoadWeight());
    }

    @Test
    @DisplayName("Illegal state jumps are rejected")
    void rejectsIllegalTransitions() {
        Drone drone = droneWith(500, 100);

        assertThrows(IllegalStateException.class, () -> drone.moveTo(DroneState.DELIVERING));
    }

    @Test
    @DisplayName("Medication name and code formats are enforced")
    void enforcesMedicationFormat() {
        assertThrows(IllegalArgumentException.class,
                () -> Medication.of("Bad Name!", 10, "CODE_1", null));
        assertThrows(IllegalArgumentException.class,
                () -> Medication.of("Good_Name", 10, "lowercase", null));
    }
}