package com.example;
import java.time.*;

public class Diner extends User {
    private Boolean exposed;
    private LocalDate exposure;

    public Diner(){
        this.exposed = false;
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