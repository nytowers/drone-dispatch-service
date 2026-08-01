package com.thedrone.dispatch.dto;

import com.thedrone.dispatch.entity.Medication;

import java.util.List;
import java.util.stream.Collectors;

public class MedicationResponse {

    private final Long id;
    private final String name;
    private final int weight;
    private final String code;
    private final String image;

    private MedicationResponse(Medication medication) {
        this.id = medication.getId();
        this.name = medication.getName();
        this.weight = medication.getWeight();
        this.code = medication.getCode();
        this.image = medication.getImage();
    }

    public static MedicationResponse from(Medication medication) {
        return new MedicationResponse(medication);
    }

    public static List<MedicationResponse> from(List<Medication> medications) {
        return medications.stream().map(MedicationResponse::from).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public String getCode() {
        return code;
    }

    public String getImage() {
        return image;
    }
}