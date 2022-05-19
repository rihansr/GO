package com.go.rider.util;

import android.content.Context;
import android.location.Location;

import com.go.rider.model.trip.TripInfo;
import com.go.rider.model.user.Rider;
import com.go.rider.model.vehicle.VehicleInfo;
import com.go.rider.model.vehicle.VehicleType;
import com.go.rider.service.GoService;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TempStorage {

    public static Location  CURRENT_LOCATION;
    public static Location  LAST_LOCATION;
    public static LatLng    CURRENT_LAT_LNG;
    public static TripInfo  TRIP_INFO;
    public static Rider     RIDER;

    public static void setLatLng(LatLng latLng){
        if(latLng == null) CURRENT_LAT_LNG = new LatLng(24, 90);
        else CURRENT_LAT_LNG = latLng;
    }

    public static void setTripInfo(TripInfo tripInfo, boolean storeIt){
        SharedPreference sp = new SharedPreference();
        if (storeIt) {
            TRIP_INFO = tripInfo;
            sp.setTripInfo(tripInfo == null ? null : new Gson().toJson(tripInfo));
        }
        else {
            String info = sp.getTripInfo();
            TRIP_INFO = (info == null) ? null : new Gson().fromJson(info, TripInfo.class);
        }
    }

    public static void setRiderInfo(Rider rider, boolean storeIt){
        SharedPreference sp = new SharedPreference();
        String key = SharedPreference.RIDER_INFO_KEY;

        if (storeIt) {
            RIDER = rider;
            sp.setSignInData(key, rider == null ? null : new Gson().toJson(rider));
        }
        else {
            String riderInfo = sp.getSignInData(key);
            RIDER = (riderInfo == null) ? null : new Gson().fromJson(riderInfo, Rider.class);
        }
    }

    public static void storeVehicleTypes(List<VehicleType> vehicleTypes){
        SharedPreference sp = new SharedPreference();
        Type listType = new TypeToken<List<VehicleType>>(){}.getType();
        String types = vehicleTypes == null || vehicleTypes.size() == 0
                ?
                null
                :
                new Gson().toJson(vehicleTypes, listType);

        sp.setVehicleTypeData(types);
    }

    public static List<VehicleType> getVehicleTypes() {
        SharedPreference sp = new SharedPreference();
        ArrayList<VehicleType> vehicleTypes = new ArrayList<>();

        String json = sp.getVehicleTypeData();

        if (json != null) {
            Type listType = new TypeToken<List<VehicleType>>() {}.getType();
            List<VehicleType> types = new Gson().fromJson(json, listType);
            vehicleTypes.addAll(types);
        }

        return vehicleTypes;
    }

    public static List<String> getCancellationMessages(){
        List<String> messages = new ArrayList<>();
        messages.add("Rider isn't here");
        messages.add("Wrong address shown");
        messages.add("Don't charge rider");
        messages.add("Rider requested cancel");
        messages.add("Too many riders");
        messages.add("Too much luggage");
        messages.add("Unaccompanied minor");
        messages.add("No car seat");
        messages.add("Other");
        return messages;
    }

    public static List<String> getOneClickChats(){
        List<String> messages = new ArrayList<>();
        messages.add("Where are you?");
        messages.add("Okay!");
        messages.add("I am here");
        messages.add("Be right there");
        messages.add("Looking for you");
        return messages;
    }

    public static List<String> getProfileReports(){
        List<String> messages = new ArrayList<>();
        messages.add("Inappropriate thank you note");
        messages.add("Rude, vulgar, or profanity");
        messages.add("Sexually explicit");
        messages.add("Harassment or hate speech");
        messages.add("Threatening, violent or suicidal");
        messages.add("Other inappropriate content");
        return messages;
    }

    public static List<String> getTripIssues(){
        List<String> messages = new ArrayList<>();
        messages.add("I was involved in an accident");
        messages.add("My fare was too high");
        messages.add("My vehicle wasn't what i expected");
        messages.add("I had a different issue");
        messages.add("My driver didn't match the driver photo in my app");
        return messages;
    }
}
