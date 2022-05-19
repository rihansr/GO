package com.go.rider.model.trip;

import com.go.rider.model.location.Place;
import com.go.rider.model.user.Driver;
import com.go.rider.model.user.Rider;
import com.go.rider.model.vehicle.VehicleType;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

public class TripInfo implements Serializable {

    private String          tripId;
    private Driver          driver;
    private Rider           rider;
    private VehicleType     vehicleType;
    private Place           pickUpLocation;
    private Timestamp       pickUpTime;
    private Place           dropOffLocation;
    private Timestamp       dropOffTime;
    private Double          estimatedDistance;
    private Timestamp       estimatedTime;
    private Fare            fare;
    private Float           riderRating;
    private Float           driverRating;
    private String          paymentType;
    private TripStatus      tripStatus;
    private HashMap<String, String>  requestedDrivers;
    private HashMap<String, Message> messages;

    public TripInfo() {}

    public TripInfo(TripInfo cloneInfo) {
        this.tripId = cloneInfo.tripId;
        this.driver = cloneInfo.driver;
        this.rider = cloneInfo.rider;
        this.requestedDrivers = cloneInfo.requestedDrivers;
        this.vehicleType = cloneInfo.vehicleType;
        this.pickUpLocation = cloneInfo.pickUpLocation;
        this.pickUpTime = cloneInfo.pickUpTime;
        this.dropOffLocation = cloneInfo.dropOffLocation;
        this.dropOffTime = cloneInfo.dropOffTime;
        this.estimatedDistance = cloneInfo.estimatedDistance;
        this.estimatedTime = cloneInfo.estimatedTime;
        this.fare = cloneInfo.fare;
        this.riderRating = cloneInfo.riderRating;
        this.driverRating = cloneInfo.driverRating;
        this.paymentType = cloneInfo.paymentType;
        this.tripStatus = cloneInfo.tripStatus;
        this.messages = cloneInfo.messages;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Rider getRider() {
        return rider;
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Place getPickUpLocation() {
        return pickUpLocation;
    }

    public void setPickUpLocation(Place pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
    }

    public long getPickUpTime() {
        return pickUpTime == null ? 0 : pickUpTime.getTime();
    }

    public void setPickUpTime(Long pickUpTime) {
        this.pickUpTime = pickUpTime == null ? null : new Timestamp(pickUpTime);
    }

    public Place getDropOffLocation() {
        return dropOffLocation;
    }

    public void setDropOffLocation(Place dropOffLocation) {
        this.dropOffLocation = dropOffLocation;
    }

    public long getDropOffTime() {
        return dropOffTime == null ? 0 : dropOffTime.getTime();
    }

    public void setDropOffTime(Long dropOffTime) {
        this.dropOffTime = dropOffTime == null ? null : new Timestamp(dropOffTime);
    }

    public Double getEstimatedDistance() {
        return estimatedDistance;
    }

    public void setEstimatedDistance(Double estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }

    public long getEstimatedTime() {
        return estimatedTime == null ? 0 : estimatedTime.getTime();
    }

    public void setEstimatedTime(Long estimatedTime) {
        this.estimatedTime = estimatedTime == null ? null : new Timestamp(estimatedTime);
    }

    public Fare getFare() {
        return fare;
    }

    public void setFare(Fare fare) {
        this.fare = fare;
    }

    public Float getRiderRating() {
        return riderRating;
    }

    public void setRiderRating(Float riderRating) {
        this.riderRating = riderRating;
    }

    public Float getDriverRating() {
        return driverRating;
    }

    public void setDriverRating(Float driverRating) {
        this.driverRating = driverRating;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public TripStatus getTripStatus() {
        return tripStatus;
    }

    public void setTripStatus(TripStatus tripStatus) {
        this.tripStatus = tripStatus;
    }

    public HashMap<String, String> getRequestedDrivers() {
        return requestedDrivers;
    }

    public void setRequestedDrivers(HashMap<String, String> requestedDrivers) {
        this.requestedDrivers = requestedDrivers;
    }

    public HashMap<String, Message> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, Message> messages) {
        this.messages = messages;
    }
}
