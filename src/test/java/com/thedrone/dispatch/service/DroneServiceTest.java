package com.thedrone.dispatch.service;

import com.thedrone.dispatch.dto.DroneRequest;
import com.thedrone.dispatch.dto.DroneResponse;
import com.thedrone.dispatch.dto.MedicationRequest;
import com.thedrone.dispatch.enums.DroneModel;
import com.thedrone.dispatch.enums.DroneState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests against the in-memory H2 database.
 * The scheduler is pushed far out so it cannot interfere with assertions.
 */
@SpringBootTest
@TestPropertySource(properties = "drone.scheduler.interval-ms=3600000")
class DroneServiceTest {

    @Autowired
    private DroneService droneService;

    private DroneRequest droneRequest(String serial, DroneModel model, int limit, int battery) {
        DroneRequest request = new DroneRequest();
        request.setSerialNumber(serial);
        request.setModel(model);
        request.setWeightLimit(limit);
        request.setBatteryCapacity(battery);
        return request;
    }

    private List<MedicationRequest> medication(String name, int weight, String code) {
        MedicationRequest request = new MedicationRequest();
        request.setName(name);
        request.setWeight(weight);
        request.setCode(code);
        return List.of(request);
    }

    @Test
    @DisplayName("Preloaded data is available at startup")
    void preloadedDataExists() {
        assertFalse(droneService.getAvailableDrones().isEmpty());
    }

    @Test
    @DisplayName("Registering a drone returns it in IDLE state")
    void registersDrone() {
        DroneResponse response = droneService.register(
                droneRequest("IT-REG-001", DroneModel.HEAVYWEIGHT, 800, 90));

        assertEquals("IT-REG-001", response.getSerialNumber());
        assertEquals(DroneState.IDLE, response.getState());
        assertEquals(800, response.getRemainingCapacity());
    }

    @Test
    @DisplayName("Loading medication moves the drone to LOADED and persists the payload")
    void loadsMedication() {
        droneService.register(droneRequest("IT-LOAD-001", DroneModel.MIDDLEWEIGHT, 500, 95));

        DroneResponse loaded = droneService.loadMedications("IT-LOAD-001",
                medication("Paracetamol_500", 200, "PARA_500"));

        assertEquals(DroneState.LOADED, loaded.getState());
        assertEquals(200, loaded.getCurrentLoadWeight());
        assertEquals(1, droneService.getLoadedMedications("IT-LOAD-001").size());
    }

    @Test
    @DisplayName("Loading beyond the weight limit is rejected")
    void rejectsOverload() {
        droneService.register(droneRequest("IT-OVER-001", DroneModel.LIGHTWEIGHT, 200, 95));

        assertThrows(IllegalStateException.class, () ->
                droneService.loadMedications("IT-OVER-001", medication("Heavy_Box", 500, "HVY_01")));
    }

    @Test
    @DisplayName("Loading a drone below 25% battery is rejected")
    void rejectsLowBattery() {
        droneService.register(droneRequest("IT-BATT-001", DroneModel.MIDDLEWEIGHT, 500, 20));

        assertThrows(IllegalStateException.class, () ->
                droneService.loadMedications("IT-BATT-001", medication("Insulin", 50, "INS_01")));
    }

    @Test
    @DisplayName("A duplicate serial number is rejected")
    void rejectsDuplicateSerial() {
        droneService.register(droneRequest("IT-DUP-001", DroneModel.LIGHTWEIGHT, 200, 80));

        assertThrows(IllegalStateException.class, () ->
                droneService.register(droneRequest("IT-DUP-001", DroneModel.LIGHTWEIGHT, 200, 80)));
    }

    @Test
    @DisplayName("Unknown serial numbers raise a not-found error")
    void rejectsUnknownDrone() {
        assertThrows(NoSuchElementException.class, () -> droneService.getDrone("DOES-NOT-EXIST"));
    }

    @Test
    @DisplayName("Only drones with battery >= 25% and free capacity are available")
    void listsAvailableDrones() {
        droneService.register(droneRequest("IT-AVAIL-OK", DroneModel.CRUISERWEIGHT, 700, 55));
        droneService.register(droneRequest("IT-AVAIL-LOW", DroneModel.CRUISERWEIGHT, 700, 10));

        List<String> serials = droneService.getAvailableDrones().stream()
                .map(DroneResponse::getSerialNumber)
                .toList();

        assertTrue(serials.contains("IT-AVAIL-OK"));
        assertFalse(serials.contains("IT-AVAIL-LOW"));
    }

    @Test
    @DisplayName("The scheduler advances a LOADED drone through the full delivery cycle")
    void advancesDeliveryLifecycle() {
        droneService.register(droneRequest("IT-CYCLE-001", DroneModel.MIDDLEWEIGHT, 500, 80));
        droneService.loadMedications("IT-CYCLE-001", medication("Insulin", 100, "INS_01"));

        droneService.advanceDeliveries();
        assertEquals(DroneState.DELIVERING, droneService.getDrone("IT-CYCLE-001").getState());

        droneService.advanceDeliveries();
        DroneResponse delivered = droneService.getDrone("IT-CYCLE-001");
        assertEquals(DroneState.DELIVERED, delivered.getState());
        assertEquals(70, delivered.getBatteryCapacity());

        droneService.advanceDeliveries();
        DroneResponse returning = droneService.getDrone("IT-CYCLE-001");
        assertEquals(DroneState.RETURNING, returning.getState());
        assertEquals(0, returning.getCurrentLoadWeight());

        droneService.advanceDeliveries();
        assertEquals(DroneState.IDLE, droneService.getDrone("IT-CYCLE-001").getState());
    }
}