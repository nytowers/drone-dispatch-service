package com.thedrone.dispatch.enums;

public enum DroneModel {

    LIGHTWEIGHT(250),
    MIDDLEWEIGHT(500),
    CRUISERWEIGHT(750),
    HEAVYWEIGHT(1000);

    private final int maxCapacityInGrams;

    DroneModel(int maxCapacityInGrams) {
        this.maxCapacityInGrams = maxCapacityInGrams;
    }

    public int getMaxCapacityInGrams() {
        return maxCapacityInGrams;
    }

    public boolean supports(int weightLimitInGrams) {
        return weightLimitInGrams > 0 && weightLimitInGrams <= maxCapacityInGrams;
    }
}