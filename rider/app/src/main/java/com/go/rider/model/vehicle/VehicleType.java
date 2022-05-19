package com.go.rider.model.vehicle;

import java.io.Serializable;

public class VehicleType implements Serializable {

    private String  id;
    private String  model;
    private String  type;
    private Integer capacity;
    private Double  baseFare;
    private Double  waitingCharge;
    private Double  perKmRate;
    private Double  minFare;
    private Double  cancellationFee;
    private Boolean available;

    public VehicleType() {}

    public VehicleType(VehicleType cloneType, boolean partial) {
        this.id = cloneType.id;
        this.model = cloneType.model;
        this.type = cloneType.type;
        if(!partial) this.capacity = cloneType.capacity;
        if(!partial) this.baseFare = cloneType.baseFare;
        if(!partial) this.waitingCharge = cloneType.waitingCharge;
        if(!partial) this.perKmRate = cloneType.perKmRate;
        if(!partial) this.minFare = cloneType.minFare;
        if(!partial) this.cancellationFee = cloneType.cancellationFee;
        if(!partial) this.available = cloneType.available;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(Double baseFare) {
        this.baseFare = baseFare;
    }

    public Double getWaitingCharge() {
        return waitingCharge;
    }

    public void setWaitingCharge(Double waitingCharge) {
        this.waitingCharge = waitingCharge;
    }

    public Double getPerKmRate() {
        return perKmRate;
    }

    public void setPerKmRate(Double perKmRate) {
        this.perKmRate = perKmRate;
    }

    public Double getMinFare() {
        return minFare;
    }

    public void setMinFare(Double minFare) {
        this.minFare = minFare;
    }

    public Double getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(Double cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public Boolean isAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
