package com.example;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;


public class Restaurant extends User {
    private int id;
    private boolean premium;
    private LocalDate exposure;
    private float latitude;
    private float longitude;

    public Restaurant() {
        this.premium = false;
    }

    public Restaurant(Restaurant otherRestaurant){
        this.setName(otherRestaurant.getName());
        this.setUsername(otherRestaurant.getUsername());
        this.setEmail(otherRestaurant.getEmail());
        this.setPassword(otherRestaurant.getPassword());
        this.setId(otherRestaurant.getId());
        this.setPremiumStatus(otherRestaurant.getPremiumStatus());
        this.setExposureDate(otherRestaurant.getExposureDate());
        this.setCoordinates(otherRestaurant.getCoordinates().get(0), otherRestaurant.getCoordinates().get(1));
    }

    public boolean hadRecentExposure(){
        return (this.exposure.plusDays(14).isBefore(LocalDate.now()));
    }

    public int getId() {
        return this.id;
    }

    public boolean getPremiumStatus(){
        return this.premium;
    }

    public LocalDate getExposureDate(){
        return this.exposure;
    }

    public List<Float> getCoordinates(){
        return Arrays.asList(this.latitude, this.longitude);
    }

    public float getLatitude(){
        return this.latitude;
    }

    public float getLongitude(){
        return this.longitude;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPremiumStatus(boolean status){
        this.premium = status;
    }

    public void setExposureDate(LocalDate date){
        this.exposure = date;
    }

    public void setCoordinates(float latitude, float longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLatitude(float latitude){
        this.latitude = latitude;
    }

    public void setLongitude(float longitude){
        this.longitude = longitude;
    }
}