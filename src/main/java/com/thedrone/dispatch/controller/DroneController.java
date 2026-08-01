package com.thedrone.dispatch.controller;

import com.thedrone.dispatch.dto.DroneRequest;
import com.thedrone.dispatch.dto.DroneResponse;
import com.thedrone.dispatch.dto.MedicationRequest;
import com.thedrone.dispatch.dto.MedicationResponse;
import com.thedrone.dispatch.service.DroneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/v1/drones",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class DroneController {

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DroneResponse register(@Valid @RequestBody DroneRequest request) {
        return droneService.register(request);
    }

    @PostMapping(value = "/{serialNumber}/medications", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DroneResponse loadMedications(@PathVariable String serialNumber,
                                         @RequestBody List<@Valid MedicationRequest> medications) {
        return droneService.loadMedications(serialNumber, medications);
    }

    @GetMapping("/{serialNumber}/medications")
    public List<MedicationResponse> getLoadedMedications(@PathVariable String serialNumber) {
        return droneService.getLoadedMedications(serialNumber);
    }

    @GetMapping("/available")
    public List<DroneResponse> getAvailableDrones() {
        return droneService.getAvailableDrones();
    }

    @GetMapping("/{serialNumber}/battery")
    public DroneResponse getBattery(@PathVariable String serialNumber) {
        return droneService.getDrone(serialNumber);
    }
}