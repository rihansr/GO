package com.go.driver.model.location;

import java.io.Serializable;

public class Place implements Serializable {

    private int     id;
    private String  title;
    private String  address;
    private Double  latitude;
    private Double  longitude;
    private Integer icon;

    public Place() {}

    public Place(int id, String title, String address, Double latitude, Double longitude, Integer icon) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
