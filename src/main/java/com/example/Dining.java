package com.example;
import java.time.*;

public class Dining {
    private String restaurant;
    private LocalTime time;
    private LocalDate date;
    private String dinerName;
    private String dinerEmail;

    public Dining(){
        this.time = LocalTime.now();
        this.date = LocalDate.now();
    }

    public String getRestaurant() {
        return this.restaurant;
    }

    public LocalTime getTime() {
        return this.time;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getDinerName() {
        return this.dinerName;
    }

    public String getDinerEmail() {
        return this.dinerEmail;
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDinerName(String name) {
        this.dinerName = name;
    }

    public void setDinerEmail(String email) {
        this.dinerEmail = email;
    }
}