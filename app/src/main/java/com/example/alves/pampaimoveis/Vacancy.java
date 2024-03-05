package com.example.alves.pampaimoveis;

/**
 * Created by Leticia on 10/06/2017.
 */

public class Vacancy {
    private String details;
    private String price;
    private Boolean available;

    public Vacancy(){

    }

    public Vacancy(String details, String price){
        this.details = details;
        this.price = price;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}

