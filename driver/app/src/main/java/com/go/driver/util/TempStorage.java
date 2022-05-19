package com.go.driver.util;

import android.location.Location;
import com.go.driver.model.trip.TripInfo;
import com.go.driver.model.user.Driver;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class TempStorage {

    public static Location  CURRENT_LOCATION;
    public static Location  LAST_LOCATION;
    public static LatLng    CURRENT_LAT_LNG;
    public static TripInfo  TRIP_INFO;
    public static Driver    DRIVER;

    public static void setLatLng(LatLng latLng){
        if(latLng == null) CURRENT_LAT_LNG = new LatLng(0, 0);
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

    public static void setDriverInfo(Driver driver, boolean storeIt){
        SharedPreference sp = new SharedPreference();
        String key = SharedPreference.DRIVER_INFO_KEY;

        if (storeIt) {
            DRIVER = driver;
            sp.setSignInData(key, driver == null ? null : new Gson().toJson(driver));
        }
        else {
            String driverInfo = sp.getSignInData(key);
            DRIVER = (driverInfo == null) ? null : new Gson().fromJson(driverInfo, Driver.class);
        }
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
        messages.add("I have arrived");
        messages.add("Okay, got it!");
        messages.add("I'm on my way");
        messages.add("Stuck in traffic");
        return messages;
    }

    public static List<String> getProfileReports(){
        List<String> messages = new ArrayList<>();
        messages.add("My rider made me feel unsafe");
        messages.add("The wrong rider took this trip");
        messages.add("A rider made a mess in my vehicle");
        messages.add("A rider damaged my vehicle");
        messages.add("Rude, vulgar, or profanity");
        messages.add("Harassment or hate speech");
        messages.add("Other inappropriate content");
        return messages;
    }

    public static List<String> getTripIssues(){
        List<String> messages = new ArrayList<>();
        messages.add("My toll or parking fee wasn't included in my fare");
        messages.add("I was in an accident");
        messages.add("Vehicle damage");
        messages.add("I had a different issue");
        messages.add("Fare Issues");
        return messages;
    }
}
