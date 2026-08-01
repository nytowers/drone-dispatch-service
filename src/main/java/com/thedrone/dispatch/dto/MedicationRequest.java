package com.thedrone.dispatch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class MedicationRequest {

    @NotBlank(message = "name is required")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$",
            message = "name allows only letters, numbers, '-' and '_'")
    private String name;

    @NotNull(message = "weight is required")
    @Min(value = 1, message = "weight must be greater than 0g")
    private Integer weight;

    @NotBlank(message = "code is required")
    @Pattern(regexp = "^[A-Z0-9_]+$",
            message = "code allows only uppercase letters, numbers and '_'")
    private String code;

    private String image;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}