package com.example;
//import java.time.*;

public class User {
    private String username;
    private String name;
    private String email;
    private String password;

    public User(){
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

    public boolean isDiner() {
        return this instanceof Diner;
    }

    public boolean isRestaurant() {
        return this instanceof Restaurant;
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
}

