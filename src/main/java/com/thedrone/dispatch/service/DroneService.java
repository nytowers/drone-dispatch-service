package com.thedrone.dispatch.service;

import com.thedrone.dispatch.dto.DroneRequest;
import com.thedrone.dispatch.dto.DroneResponse;
import com.thedrone.dispatch.dto.MedicationRequest;
import com.thedrone.dispatch.dto.MedicationResponse;
import com.thedrone.dispatch.entity.Drone;
import com.thedrone.dispatch.entity.Medication;
import com.thedrone.dispatch.enums.DroneState;
import com.thedrone.dispatch.repository.DroneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class DroneService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroneService.class);

    private static final int BATTERY_DRAIN_PER_DELIVERY = 10;

    private final DroneRepository droneRepository;

    public DroneService(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    @Transactional
    public DroneResponse register(DroneRequest request) {
        if (droneRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new IllegalStateException("A drone with serial number '"
                    + request.getSerialNumber() + "' is already registered.");
        }

        Drone drone = droneRepository.save(Drone.register(request.getSerialNumber(),
                request.getModel(),
                request.getWeightLimit(),
                request.getBatteryCapacity()));

        LOGGER.info("Registered drone {} ({}, {}g)",
                drone.getSerialNumber(), drone.getModel(), drone.getWeightLimit());
        return DroneResponse.from(drone);
    }

    @Transactional
    public DroneResponse loadMedications(String serialNumber, List<MedicationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("At least one medication must be provided.");
        }

        Drone drone = findOrFail(serialNumber);

        for (MedicationRequest request : requests) {
            drone.load(Medication.of(request.getName(), request.getWeight(),
                    request.getCode(), request.getImage()));
        }
        drone.markLoaded();

        droneRepository.save(drone);
        LOGGER.info("Loaded {} item(s) on drone {} ({}g / {}g)",
                requests.size(), serialNumber, drone.currentLoadWeight(), drone.getWeightLimit());
        return DroneResponse.from(drone);
    }

    @Transactional(readOnly = true)
    public List<MedicationResponse> getLoadedMedications(String serialNumber) {
        return MedicationResponse.from(findOrFail(serialNumber).getMedications());
    }

    @Transactional(readOnly = true)
    public List<DroneResponse> getAvailableDrones() {
        return droneRepository.findAllWithMedications().stream()
                .filter(Drone::isAvailableForLoading)
                .map(DroneResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DroneResponse getDrone(String serialNumber) {
        return DroneResponse.from(findOrFail(serialNumber));
    }

    @Transactional
    public int advanceDeliveries() {
        List<Drone> fleet = droneRepository.findAllWithMedications();
        int changed = 0;

        for (Drone drone : fleet) {
            DroneState before = drone.getState();

            switch (drone.getState()) {
                case LOADED:
                    drone.moveTo(DroneState.DELIVERING);
                    break;
                case DELIVERING:
                    drone.moveTo(DroneState.DELIVERED);
                    drone.drainBattery(BATTERY_DRAIN_PER_DELIVERY);
                    break;
                case DELIVERED:
                    drone.unload();
                    drone.moveTo(DroneState.RETURNING);
                    break;
                case RETURNING:
                    drone.moveTo(DroneState.IDLE);
                    break;
                default:
                    continue; // IDLE and LOADING wait for the client
            }

            changed++;
            LOGGER.info("Drone {}: {} -> {}", drone.getSerialNumber(), before, drone.getState());
        }

        if (changed > 0) {
            droneRepository.saveAll(fleet);
        }
        return changed;
    }

    private Drone findOrFail(String serialNumber) {
        return droneRepository.findBySerialNumberWithMedications(serialNumber)
                .orElseThrow(() -> new NoSuchElementException(
                        "No drone found with serial number '" + serialNumber + "'."));
    }
}