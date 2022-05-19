package com.go.driver.model.user;

import com.go.driver.model.address.Address;
import com.go.driver.model.location.Location;
import com.go.driver.model.other.ActiveStatus;
import com.go.driver.model.other.Preference;
import com.go.driver.model.other.Rating;
import com.go.driver.model.vehicle.VehicleInfo;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;

public class Trip implements Serializable {

    private Boolean         onTrip;
    private Double          balance;
    private int             totalTrips;
    private Boolean         verified;
    private Preference      preference;
    private HashMap<String, Rating> ratings;

    public Trip() {}

    public Trip(Boolean onTrip, Double balance, int totalTrips, Boolean verified, Preference preference, HashMap<String, Rating> ratings) {
        this.onTrip = onTrip;
        this.balance = balance;
        this.totalTrips = totalTrips;
        this.verified = verified;
        this.preference = preference;
        this.ratings = ratings;
    }

    public Trip(Trip cloneTrip, boolean partial) {
        if(!partial) this.onTrip = cloneTrip.onTrip;
        if(!partial) this.balance = cloneTrip.balance;
        this.totalTrips = cloneTrip.totalTrips;
        this.verified = cloneTrip.verified;
        if(!partial) this.preference = cloneTrip.preference;
        this.ratings = cloneTrip.ratings;
    }

    public Boolean isOnTrip() {
        return onTrip;
    }

    public void setOnTrip(Boolean onTrip) {
        this.onTrip = onTrip;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public int getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(int totalTrips) {
        this.totalTrips = totalTrips;
    }

    public Boolean isVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }

    public HashMap<String, Rating> getRatings() {
        return ratings;
    }

    public void setRatings(HashMap<String, Rating> ratings) {
        this.ratings = ratings;
    }
}
