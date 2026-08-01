package com.thedrone.dispatch.dto;

import com.thedrone.dispatch.entity.Drone;
import com.thedrone.dispatch.enums.DroneModel;
import com.thedrone.dispatch.enums.DroneState;

import java.util.List;
import java.util.stream.Collectors;

public class DroneResponse {

    private final Long id;
    private final String serialNumber;
    private final DroneModel model;
    private final int weightLimit;
    private final int currentLoadWeight;
    private final int remainingCapacity;
    private final int batteryCapacity;
    private final DroneState state;
    private final boolean availableForLoading;
    private final List<MedicationResponse> medications;

    private DroneResponse(Drone drone) {
        this.id = drone.getId();
        this.serialNumber = drone.getSerialNumber();
        this.model = drone.getModel();
        this.weightLimit = drone.getWeightLimit();
        this.currentLoadWeight = drone.currentLoadWeight();
        this.remainingCapacity = drone.remainingCapacity();
        this.batteryCapacity = drone.getBatteryCapacity();
        this.state = drone.getState();
        this.availableForLoading = drone.isAvailableForLoading();
        this.medications = MedicationResponse.from(drone.getMedications());
    }

    public static DroneResponse from(Drone drone) {
        return new DroneResponse(drone);
    }

    public static List<DroneResponse> from(List<Drone> drones) {
        return drones.stream().map(DroneResponse::from).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public DroneModel getModel() {
        return model;
    }

    public int getWeightLimit() {
        return weightLimit;
    }

    public int getCurrentLoadWeight() {
        return currentLoadWeight;
    }

    public int getRemainingCapacity() {
        return remainingCapacity;
    }

    public int getBatteryCapacity() {
        return batteryCapacity;
    }

    public DroneState getState() {
        return state;
    }

    public boolean isAvailableForLoading() {
        return availableForLoading;
    }

    public List<MedicationResponse> getMedications() {
        return medications;
    }
}