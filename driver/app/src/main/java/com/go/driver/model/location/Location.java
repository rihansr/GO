package com.go.driver.model.location;

import java.io.Serializable;

public class Location implements Serializable {

    private float   rotation;
    private float   bearing;
    private double  lastLatitude;
    private double  lastLongitude;
    private double  curLatitude;
    private double  curLongitude;

    public Location() {}

    public Location(float rotation, float bearing, double lastLatitude, double lastLongitude, double curLatitude, double curLongitude) {
        this.rotation = rotation;
        this.bearing = bearing;
        this.lastLatitude = lastLatitude;
        this.lastLongitude = lastLongitude;
        this.curLatitude = curLatitude;
        this.curLongitude = curLongitude;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public double getLastLatitude() {
        return lastLatitude;
    }

    public void setLastLatitude(double lastLatitude) {
        this.lastLatitude = lastLatitude;
    }

    public double getLastLongitude() {
        return lastLongitude;
    }

    public void setLastLongitude(double lastLongitude) {
        this.lastLongitude = lastLongitude;
    }

    public double getCurLatitude() {
        return curLatitude;
    }

    public void setCurLatitude(double curLatitude) {
        this.curLatitude = curLatitude;
    }

    public double getCurLongitude() {
        return curLongitude;
    }

    public void setCurLongitude(double curLongitude) {
        this.curLongitude = curLongitude;
    }
}
