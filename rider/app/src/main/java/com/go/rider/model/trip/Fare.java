package com.go.rider.model.trip;

import com.go.rider.model.location.Place;
import com.go.rider.model.user.Driver;
import com.go.rider.model.user.Rider;
import com.go.rider.model.vehicle.VehicleType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Fare implements Serializable {

    private double  tripFare;
    private double  promo;
    private double  totalFare;

    public Fare() {}

    public Fare(double tripFare, double promo, double totalFare) {
        this.tripFare = tripFare;
        this.promo = promo;
        this.totalFare = totalFare;
    }

    public double getTripFare() {
        return tripFare;
    }

    public void setTripFare(double tripFare) {
        this.tripFare = tripFare;
    }

    public double getPromo() {
        return promo;
    }

    public void setPromo(double promo) {
        this.promo = promo;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }
}
