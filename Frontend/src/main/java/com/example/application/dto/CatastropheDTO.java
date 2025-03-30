package com.example.application.dto;

import java.time.LocalDate;

public class CatastropheDTO {
    private Integer id;
    private String name;
    private String description;
    private double locationX;
    private double locationY;
    private LocalDate startDate;
    private String emergencyLevel;

    // Constructor vacío
    public CatastropheDTO() {
    }

    // Constructor completo
    public CatastropheDTO(String name, String description, double locationX, double locationY,
                          LocalDate startDate, String emergencyLevel) {
        this.name = name;
        this.description = description;
        this.locationX = locationX;
        this.locationY = locationY;
        this.startDate = startDate;
        this.emergencyLevel = emergencyLevel;
    }

    // Getters y setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLocationX() {
        return locationX;
    }

    public void setLocationX(double locationX) {
        this.locationX = locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    public void setLocationY(double locationY) {
        this.locationY = locationY;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(String emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }
}
