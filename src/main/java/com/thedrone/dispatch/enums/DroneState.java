package com.thedrone.dispatch.enums;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum DroneState {

    IDLE,
    LOADING,
    LOADED,
    DELIVERING,
    DELIVERED,
    RETURNING;

    private static final Map<DroneState, Set<DroneState>> ALLOWED = new EnumMap<>(DroneState.class);

    static {
        ALLOWED.put(IDLE, EnumSet.of(LOADING));
        ALLOWED.put(LOADING, EnumSet.of(LOADING, LOADED, IDLE));
        ALLOWED.put(LOADED, EnumSet.of(DELIVERING));
        ALLOWED.put(DELIVERING, EnumSet.of(DELIVERED));
        ALLOWED.put(DELIVERED, EnumSet.of(RETURNING));
        ALLOWED.put(RETURNING, EnumSet.of(IDLE));
    }

    public boolean canTransitionTo(DroneState target) {
        return target != null && ALLOWED.get(this).contains(target);
    }

    public Set<DroneState> allowedTransitions() {
        return Collections.unmodifiableSet(ALLOWED.get(this));
    }


    public boolean acceptsLoad() {
        return this == IDLE || this == LOADING;
    }
}