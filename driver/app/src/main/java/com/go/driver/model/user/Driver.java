package com.go.driver.model.user;

import com.go.driver.model.location.Location;
import com.go.driver.model.other.Rating;
import com.go.driver.model.other.Preference;
import com.go.driver.model.vehicle.VehicleInfo;
import com.go.driver.model.address.Address;
import com.go.driver.model.other.ActiveStatus;

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
    private Timestamp       dateOfBirth;
    private Integer         age;
    private String          nationalId;
    private String          nationalId_Photo;
    private Address         address;
    private Location        location;
    private String          referralCode;
    private VehicleInfo     vehicleInfo;
    private Boolean         profileCompleted;
    private Trip            trip;
    private ActiveStatus    active;
    private Timestamp       registeredAt;

    public Driver() {}

    public Driver(Driver cloneDriver, boolean partial) {
        this.id = cloneDriver.id;
        this.token = cloneDriver.token;
        this.profilePhoto = cloneDriver.profilePhoto;
        this.coverPhoto = cloneDriver.coverPhoto;
        this.name = cloneDriver.name;
        this.phone = cloneDriver.phone;
        this.email = cloneDriver.email;
        this.gender = cloneDriver.gender;
        if(!partial) this.dateOfBirth = cloneDriver.dateOfBirth;
        this.age = cloneDriver.age;
        if(!partial) this.nationalId = cloneDriver.nationalId;
        if(!partial) this.nationalId_Photo = cloneDriver.nationalId_Photo;
        this.address = cloneDriver.address;
        this.location = cloneDriver.location;
        if(!partial) this.referralCode = cloneDriver.referralCode;
        if(!partial) this.vehicleInfo = cloneDriver.vehicleInfo;
        if(!partial) this.profileCompleted = cloneDriver.profileCompleted;
        this.trip = new Trip(cloneDriver.trip, partial);
        if(!partial) this.active = cloneDriver.active;
        this.registeredAt = cloneDriver.registeredAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
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

    public long getDateOfBirth() {
        return dateOfBirth == null ? 0 : dateOfBirth.getTime();
    }

    public void setDateOfBirth(Long dateOfBirth) {
        this.dateOfBirth = dateOfBirth == null ? null : new Timestamp(dateOfBirth);
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getNationalId_Photo() {
        return (nationalId_Photo == null) ? null : nationalId_Photo.replaceAll(",",".");
    }

    public void setNationalId_Photo(String nationalId_Photo) {
        this.nationalId_Photo = nationalId_Photo;
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

    public Boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(Boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
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
