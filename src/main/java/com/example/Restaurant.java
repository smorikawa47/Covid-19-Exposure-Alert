package com.example;

public class Restaurant extends User {
    private int id;
    private boolean premium;

    public Restaurant() {
        this.premium = false;
    }

    public int getId() {
        return this.id;
    }

    public boolean getPremiumStatus(){
        return this.premium;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPremiumStatus(boolean status){
        this.premium = status;
    }
}