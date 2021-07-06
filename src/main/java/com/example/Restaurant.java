package com.example;

public class Restaurant {
    private String name;
    private int id;
    private String username;
    private String password;
    private boolean premium;

    public Restaurant() {
        this.premium = false;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public boolean getPremiumStatus(){
        return this.premium;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPremiumStatus(boolean status){
        this.premium = status;
    }
}