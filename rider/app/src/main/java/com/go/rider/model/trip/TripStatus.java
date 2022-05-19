package com.go.rider.model.trip;

import java.io.Serializable;

public class TripStatus implements Serializable {

    private boolean tripAccepted;
    private boolean tripCancelled;
    private boolean tripStarted;
    private boolean tripFinished;

    public TripStatus() {}

    public TripStatus(boolean tripAccepted, boolean tripCancelled, boolean tripStarted, boolean tripFinished) {
        this.tripAccepted = tripAccepted;
        this.tripCancelled = tripCancelled;
        this.tripStarted = tripStarted;
        this.tripFinished = tripFinished;
    }

    public boolean isTripAccepted() {
        return tripAccepted;
    }

    public void setTripAccepted(boolean tripAccepted) {
        this.tripAccepted = tripAccepted;
    }

    public boolean isTripCancelled() {
        return tripCancelled;
    }

    public void setTripCancelled(boolean tripCancelled) {
        this.tripCancelled = tripCancelled;
    }

    public boolean isTripStarted() {
        return tripStarted;
    }

    public void setTripStarted(boolean tripStarted) {
        this.tripStarted = tripStarted;
    }

    public boolean isTripFinished() {
        return tripFinished;
    }

    public void setTripFinished(boolean tripFinished) {
        this.tripFinished = tripFinished;
    }
}
