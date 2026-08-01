package com.thedrone.dispatch.scheduler;

import com.thedrone.dispatch.service.DroneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DroneScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DroneScheduler.class);

    private final DroneService droneService;

    public DroneScheduler(DroneService droneService) {
        this.droneService = droneService;
    }

    @Scheduled(fixedDelayString = "${drone.scheduler.interval-ms:10000}",
            initialDelayString = "${drone.scheduler.interval-ms:10000}")
    public void advanceDeliveries() {
        try {
            int changed = droneService.advanceDeliveries();
            if (changed > 0) {
                LOGGER.debug("Dispatch cycle: {} drone(s) transitioned.", changed);
            }
        } catch (RuntimeException exception) {
            LOGGER.error("Dispatch cycle failed: {}", exception.getMessage(), exception);
        }
    }
}