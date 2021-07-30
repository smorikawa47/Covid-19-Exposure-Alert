package com.example;
import java.time.*;

public class Diner extends User {
    private Boolean exposed;
    private LocalDate exposure;

    public Diner(){
        this.exposed = false;
    }

    public Diner(Diner otherDiner){
        this.setName(otherDiner.getName());
        this.setUsername(otherDiner.getUsername());
        this.setEmail(otherDiner.getEmail());
        this.setPassword(otherDiner.getPassword());
        this.setExposed(otherDiner.wasExposed());
        this.setExposureDate(otherDiner.getExposureDate());
    }

    public Boolean wasExposed() {
        return this.exposed;
    }

    public LocalDate getExposureDate() {
        return this.exposure;
    }

    public void setExposed(Boolean exposed) {
        this.exposed = exposed;
    }

    public void setExposureDate(LocalDate exposure) {
        this.exposure = exposure;
    }
}