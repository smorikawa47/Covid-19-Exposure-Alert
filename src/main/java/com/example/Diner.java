package com.example;
import java.time.*;

public class Diner extends User {
    private Boolean exposed;
    private LocalDate exposure;
    private boolean testResult;
    private LocalDate testResultDate;

    public Diner(){
        this.exposed = false;
        this.testResult = false;
    }

    public Diner(Diner otherDiner){
        this.setName(otherDiner.getName());
        this.setUsername(otherDiner.getUsername());
        this.setEmail(otherDiner.getEmail());
        this.setPassword(otherDiner.getPassword());
        this.setExposed(otherDiner.wasExposed());
        this.setExposureDate(otherDiner.getExposureDate());
        this.setTestResult(otherDiner.getTestResult());
        this.setTestResultDate(otherDiner.getTestResultDate());
    }

    public Boolean wasExposed() {
        return this.exposed;
    }

    public LocalDate getExposureDate() {
        return this.exposure;
    }

    public Boolean getTestResult() {
        return this.testResult;
    }

    public LocalDate getTestResultDate() {
        return this.testResultDate;
    }

    public void setExposed(Boolean exposed) {
        this.exposed = exposed;
    }

    public void setExposureDate(LocalDate exposure) {
        this.exposure = exposure;
    }

    public void setTestResult(Boolean result){
        this.testResult = result;
    }

    public void setTestResultDate(LocalDate date){
        this.testResultDate = date;
    }
}