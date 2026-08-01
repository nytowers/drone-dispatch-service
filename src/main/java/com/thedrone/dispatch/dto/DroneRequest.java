package com.thedrone.dispatch.dto;

import com.thedrone.dispatch.enums.DroneModel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DroneRequest {

    @NotBlank(message = "serialNumber is required")
    @Size(max = 100, message = "serialNumber must not exceed 100 characters")
    private String serialNumber;

    @NotNull(message = "model is required (LIGHTWEIGHT, MIDDLEWEIGHT, CRUISERWEIGHT, HEAVYWEIGHT)")
    private DroneModel model;

    @NotNull(message = "weightLimit is required")
    @Min(value = 1, message = "weightLimit must be at least 1g")
    @Max(value = 1000, message = "weightLimit must not exceed 1000g")
    private Integer weightLimit;

    @NotNull(message = "batteryCapacity is required")
    @Min(value = 0, message = "batteryCapacity must be between 0 and 100")
    @Max(value = 100, message = "batteryCapacity must be between 0 and 100")
    private Integer batteryCapacity;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public DroneModel getModel() {
        return model;
    }

    public void setModel(DroneModel model) {
        this.model = model;
    }

    public Integer getWeightLimit() {
        return weightLimit;
    }

    public void setWeightLimit(Integer weightLimit) {
        this.weightLimit = weightLimit;
    }

    public Integer getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(Integer batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }
}