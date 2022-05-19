package com.go.rider.model.other;

import com.go.rider.model.location.Place;

import java.io.Serializable;
import java.util.List;

public class Preference implements Serializable {

    private boolean     available;
    private List<Place> savedPlaces;

    public Preference() {}

    public Preference(boolean available, List<Place> savedPlaces) {
        this.available = available;
        this.savedPlaces = savedPlaces;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<Place> getSavedPlaces() {
        return savedPlaces;
    }

    public void setSavedPlaces(List<Place> savedPlaces) {
        this.savedPlaces = savedPlaces;
    }
}
