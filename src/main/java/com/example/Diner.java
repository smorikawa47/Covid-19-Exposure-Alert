package com.example;
import java.time.*;

public class Diner {
    private String username;
    private String name;
    private String email;
    private String password;
    private Boolean exposed;
    private LocalDate exposure;

    public Diner(){
        this.exposed = false;
    }

    public String getUsername() {
        return this.username;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public Boolean wasExposed() {
        return this.exposed;
    }

    public LocalDate getExposureDate() {
        return this.exposure;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setExposed(Boolean exposed) {
        this.exposed = exposed;
    }

    public void setExposureDate(LocalDate exposure) {
        this.exposure = exposure;
    }
}