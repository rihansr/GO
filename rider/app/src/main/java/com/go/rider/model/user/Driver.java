package com.go.rider.model.user;

import com.go.rider.model.other.ActiveStatus;
import com.go.rider.model.location.Location;
import com.go.rider.model.other.Rating;
import com.go.rider.model.other.Preference;
import com.go.rider.model.vehicle.VehicleInfo;
import com.go.rider.model.address.Address;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

public class Driver implements Serializable {

    private String          id;
    private String          token;
    private String          profilePhoto;
    private String          coverPhoto;
    private String          name;
    private String          phone;
    private String          email;
    private String          gender;
    private Long            dateOfBirth;
    private Integer         age;
    private Address         address;
    private Location        location;
    private VehicleInfo     vehicleInfo;
    private Trip            trip;
    private ActiveStatus    active;
    private Timestamp       registeredAt;

    public Driver() {}

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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

    public VehicleInfo getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(VehicleInfo vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
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
