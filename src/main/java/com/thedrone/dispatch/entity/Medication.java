package com.thedrone.dispatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.regex.Pattern;

@Entity
@Table(name = "medications")
public class Medication {

    private static final Pattern NAME_REGEX = Pattern.compile("^[A-Za-z0-9_-]+$");
    private static final Pattern CODE_REGEX = Pattern.compile("^[A-Z0-9_]+$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "weight", nullable = false)
    private int weight;

    @Column(name = "code", nullable = false)
    private String code;

    @Lob
    @Column(name = "image")
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_id")
    private Drone drone;

    protected Medication() {
    }

    private Medication(String name, int weight, String code, String image) {
        if (name == null || !NAME_REGEX.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Medication name allows only letters, numbers, '-' and '_'. Received: " + name);
        }
        if (code == null || !CODE_REGEX.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "Medication code allows only uppercase letters, numbers and '_'. Received: " + code);
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Medication weight must be greater than 0g.");
        }
        this.name = name;
        this.weight = weight;
        this.code = code;
        this.image = image;
    }

    public static Medication of(String name, int weight, String code, String image) {
        return new Medication(name, weight, code, image);
    }

    void assignTo(Drone drone) {
        this.drone = drone;
    }

    void detach() {
        this.drone = null;
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

    public Drone getDrone() {
        return drone;
    }
}