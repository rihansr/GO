package com.go.driver.model.trip;

import java.io.Serializable;

public class Fare implements Serializable {

    private Double  tripFare;
    private Double  promo;
    private Double  totalFare;

    public Fare() {}

    public Fare(Double tripFare, Double promo, Double totalFare) {
        this.tripFare = tripFare;
        this.promo = promo;
        this.totalFare = totalFare;
    }

    public Double getTripFare() {
        return tripFare;
    }

    public void setTripFare(Double tripFare) {
        this.tripFare = tripFare;
    }

    public Double getPromo() {
        return promo;
    }

    public void setPromo(Double promo) {
        this.promo = promo;
    }

    public Double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(Double totalFare) {
        this.totalFare = totalFare;
    }
}
