package com.go.driver.model.vehicle;

import java.io.Serializable;

public class VehicleInfo implements Serializable {

    private VehicleType vehicleType;
    private String      drivingLicense;
    private String      drivingLicense_Photo;
    private String      vehicleRegistration;
    private String      vehicleTaxToken;
    private String      vehicleInsurance;
    private String      VehicleCertificatesOfFitness;

    public VehicleInfo() {}

    public VehicleInfo(VehicleType vehicleType, String drivingLicense, String drivingLicense_Photo, String vehicleRegistration) {
        this.vehicleType = vehicleType;
        this.drivingLicense = drivingLicense;
        this.drivingLicense_Photo = drivingLicense_Photo;
        this.vehicleRegistration = vehicleRegistration;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getDrivingLicense() {
        return drivingLicense;
    }

    public void setDrivingLicense(String drivingLicense) {
        this.drivingLicense = drivingLicense;
    }

    public String getDrivingLicense_Photo() {
        return (drivingLicense_Photo == null) ? null : drivingLicense_Photo.replaceAll(",",".");
    }

    public void setDrivingLicense_Photo(String drivingLicense_Photo) {
        this.drivingLicense_Photo = drivingLicense_Photo;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public String getVehicleTaxToken() {
        return vehicleTaxToken;
    }

    public void setVehicleTaxToken(String vehicleTaxToken) {
        this.vehicleTaxToken = vehicleTaxToken;
    }

    public String getVehicleInsurance() {
        return vehicleInsurance;
    }

    public void setVehicleInsurance(String vehicleInsurance) {
        this.vehicleInsurance = vehicleInsurance;
    }

    public String getVehicleCertificatesOfFitness() {
        return VehicleCertificatesOfFitness;
    }

    public void setVehicleCertificatesOfFitness(String vehicleCertificatesOfFitness) {
        VehicleCertificatesOfFitness = vehicleCertificatesOfFitness;
    }
}
