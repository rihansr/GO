package com.go.driver.model.user;

import com.go.driver.model.location.Location;
import com.go.driver.model.address.Address;
import com.go.driver.model.other.ActiveStatus;
import com.go.driver.model.other.Rating;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

public class Rider implements Serializable {

    private String          id;
    private String          token;
    private String          profilePhoto;
    private String          coverPhoto;
    private String          name;
    private String          email;
    private String          phone;
    private String          gender;
    private Address         address;
    private Location        location;
    private Trip            trip;
    private ActiveStatus    active;
    private Timestamp       registeredAt;

    public Rider() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getProfilePhoto() {
        return (profilePhoto == null) ? null : profilePhoto.replaceAll(",",".");
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getCoverPhoto() {
        return (coverPhoto == null) ? null : coverPhoto.replaceAll(",",".");
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public ActiveStatus isActive() {
        return active;
    }

    public void setActive(ActiveStatus active) {
        this.active = active;
    }

    public long getRegisteredAt() {
        return registeredAt == null ? 0 : registeredAt.getTime();
    }

    public void setRegisteredAt(Long registeredAt) {
        this.registeredAt = registeredAt == null ? null : new Timestamp(registeredAt);
    }
}
