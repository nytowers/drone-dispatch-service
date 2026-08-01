package com.thedrone.dispatch.entity;

import com.thedrone.dispatch.enums.DroneModel;
import com.thedrone.dispatch.enums.DroneState;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "drones",
        uniqueConstraints = @UniqueConstraint(name = "uk_drone_serial", columnNames = "serial_number"))
public class Drone {

    public static final int MAX_WEIGHT_LIMIT_GRAMS = 1000;
    public static final int MAX_SERIAL_NUMBER_LENGTH = 100;
    public static final int MIN_BATTERY_FOR_LOADING = 25;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false, length = MAX_SERIAL_NUMBER_LENGTH)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "model", nullable = false, length = 20)
    private DroneModel model;

    @Column(name = "weight_limit", nullable = false)
    private int weightLimit;

    @Column(name = "battery_capacity", nullable = false)
    private int batteryCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private DroneState state;

    @OneToMany(mappedBy = "drone", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Medication> medications = new ArrayList<>();

    protected Drone() {
    }

    private Drone(String serialNumber, DroneModel model, int weightLimit, int batteryCapacity) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Serial number is required.");
        }
        if (serialNumber.length() > MAX_SERIAL_NUMBER_LENGTH) {
            throw new IllegalArgumentException(
                    "Serial number must not exceed " + MAX_SERIAL_NUMBER_LENGTH + " characters.");
        }
        Objects.requireNonNull(model, "Drone model is required.");
        if (weightLimit <= 0 || weightLimit > MAX_WEIGHT_LIMIT_GRAMS) {
            throw new IllegalArgumentException(
                    "Weight limit must be between 1g and " + MAX_WEIGHT_LIMIT_GRAMS + "g.");
        }
        if (!model.supports(weightLimit)) {
            throw new IllegalArgumentException("Model " + model + " supports at most "
                    + model.getMaxCapacityInGrams() + "g, but " + weightLimit + "g was requested.");
        }
        if (batteryCapacity < 0 || batteryCapacity > 100) {
            throw new IllegalArgumentException("Battery capacity must be between 0 and 100.");
        }
        this.serialNumber = serialNumber;
        this.model = model;
        this.weightLimit = weightLimit;
        this.batteryCapacity = batteryCapacity;
        this.state = DroneState.IDLE;
    }

    public static Drone register(String serialNumber, DroneModel model,
                                 int weightLimit, int batteryCapacity) {
        return new Drone(serialNumber, model, weightLimit, batteryCapacity);
    }


    public int currentLoadWeight() {
        return medications.stream().mapToInt(Medication::getWeight).sum();
    }

    public int remainingCapacity() {
        return weightLimit - currentLoadWeight();
    }

    public boolean hasEnoughBatteryToLoad() {
        return batteryCapacity >= MIN_BATTERY_FOR_LOADING;
    }

    public boolean isAvailableForLoading() {
        return state.acceptsLoad() && hasEnoughBatteryToLoad() && remainingCapacity() > 0;
    }

    public void load(Medication medication) {
        Objects.requireNonNull(medication, "Medication is required.");

        if (!hasEnoughBatteryToLoad()) {
            throw new IllegalStateException("Drone '" + serialNumber
                    + "' cannot enter the LOADING state: battery is " + batteryCapacity
                    + "% but at least " + MIN_BATTERY_FOR_LOADING + "% is required.");
        }
        if (!state.acceptsLoad()) {
            throw new IllegalStateException("Drone '" + serialNumber + "' is " + state
                    + " and cannot accept medication right now.");
        }
        if (medication.getWeight() > remainingCapacity()) {
            throw new IllegalStateException("Drone '" + serialNumber + "' cannot carry "
                    + medication.getWeight() + "g: only " + remainingCapacity()
                    + "g of its " + weightLimit + "g weight limit is still free.");
        }

        this.state = DroneState.LOADING;
        medications.add(medication);
        medication.assignTo(this);
    }

    public void markLoaded() {
        if (medications.isEmpty()) {
            throw new IllegalStateException("Drone '" + serialNumber
                    + "' carries no medication and cannot be marked LOADED.");
        }
        moveTo(DroneState.LOADED);
    }

    public void moveTo(DroneState target) {
        if (!state.canTransitionTo(target)) {
            throw new IllegalStateException("Drone '" + serialNumber + "' cannot move from "
                    + state + " to " + target + ". Allowed: " + state.allowedTransitions() + ".");
        }
        this.state = target;
    }

    public void unload() {
        medications.forEach(Medication::detach);
        medications.clear();
    }

    public void drainBattery(int percentage) {
        if (percentage < 0) {
            throw new IllegalArgumentException("Battery drain cannot be negative.");
        }
        this.batteryCapacity = Math.max(0, this.batteryCapacity - percentage);
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

    public int getBatteryCapacity() {
        return batteryCapacity;
    }

    public DroneState getState() {
        return state;
    }

    public List<Medication> getMedications() {
        return Collections.unmodifiableList(medications);
    }
}