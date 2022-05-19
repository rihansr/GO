package com.go.rider.util;

import android.content.Context;

import com.go.rider.service.GoService;

public class SharedPreference {

    private Context      context;
    private final String FIRST_LAUNCH_SP_NAME = "isFirstLaunch";
    private final String FIRST_LAUNCH_SP_KEY = "launchKey";
    private final String GUIDE_VIEW_SP_NAME = "showGuideView";
    private final String LANGUAGE_SP_NAME = "language";
    private final String LANGUAGE_SP_KEY = "languageKey";
    private final String SIGN_IN_SP_NAME = "signInData";
    private final String VEHICLE_TYPE_SP_NAME = "vehicleTypesData";
    private final String VEHICLE_TYPE_SP_KEY = "vehicleTypesKey";
    private final String LOGGED_IN_SP_NAME = "isLoggedIn";
    private final String LOGGED_IN_SP_KEY = "loggedInKey";
    private final String TRIP_INFO_SP_NAME = "tripInfo";
    private final String TRIP_INFO_SP_KEY = "tripInfoKey";
    private final String FORBID_NOTIFICATION_PERMISSION_SP_NAME = "isNotificationPermissionForbid";
    private final String FORBID_NOTIFICATION_PERMISSION_SP_KEY = "forbidKey";
    public static final String RIDER_INFO_KEY = "riderInfo";

    public SharedPreference() {
        this.context = GoService.getContext();
    }

    public void setFirstLaunch(boolean isFirst){
        context.getSharedPreferences(FIRST_LAUNCH_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(FIRST_LAUNCH_SP_KEY, isFirst).apply();
    }

    public boolean isFirstLaunch(){
        return context.getSharedPreferences(FIRST_LAUNCH_SP_NAME, Context.MODE_PRIVATE).getBoolean(FIRST_LAUNCH_SP_KEY,true);
    }

    public void showGuideView(String key, boolean isShown){
        context.getSharedPreferences(GUIDE_VIEW_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(key, isShown).apply();
    }

    public boolean isGuideViewShown(String key){
        return context.getSharedPreferences(GUIDE_VIEW_SP_NAME, Context.MODE_PRIVATE).getBoolean(key,false);
    }

    public void setLanguageMode(boolean state){
        context.getSharedPreferences(LANGUAGE_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(LANGUAGE_SP_KEY, state).apply();
    }

    public boolean getLanguageMode(){
        return context.getSharedPreferences(LANGUAGE_SP_NAME, Context.MODE_PRIVATE).getBoolean(LANGUAGE_SP_KEY,true);
    }

    public void setSignInData(String key, String value){
        context.getSharedPreferences(SIGN_IN_SP_NAME, Context.MODE_PRIVATE).edit().putString(key, value).apply();
    }

    public String getSignInData(String key){
        return context.getSharedPreferences(SIGN_IN_SP_NAME, Context.MODE_PRIVATE).getString(key,null);
    }

    public void setVehicleTypeData(String value){
        context.getSharedPreferences(VEHICLE_TYPE_SP_NAME, Context.MODE_PRIVATE).edit().putString(VEHICLE_TYPE_SP_KEY, value).apply();
    }

    public String getVehicleTypeData(){
        return context.getSharedPreferences(VEHICLE_TYPE_SP_NAME, Context.MODE_PRIVATE).getString(VEHICLE_TYPE_SP_KEY,null);
    }

    public void setRiderLoggedIn(boolean logged_In){
        context.getSharedPreferences(LOGGED_IN_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(LOGGED_IN_SP_KEY, logged_In).apply();
    }

    public boolean isRiderLoggedIn(){
        return context.getSharedPreferences(LOGGED_IN_SP_NAME, Context.MODE_PRIVATE).getBoolean(LOGGED_IN_SP_KEY,false);
    }

    public void setTripInfo(String value){
        context.getSharedPreferences(TRIP_INFO_SP_NAME, Context.MODE_PRIVATE).edit().putString(TRIP_INFO_SP_KEY, value).apply();
    }

    public String getTripInfo(){
        return context.getSharedPreferences(TRIP_INFO_SP_NAME, Context.MODE_PRIVATE).getString(TRIP_INFO_SP_KEY,null);
    }

    public void setForbidNotificationPermission(boolean isForbidded){
        context.getSharedPreferences(FORBID_NOTIFICATION_PERMISSION_SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(FORBID_NOTIFICATION_PERMISSION_SP_KEY, isForbidded).apply();
    }

    public boolean isNotificationPermissionForbidded(){
        return context.getSharedPreferences(FORBID_NOTIFICATION_PERMISSION_SP_NAME, Context.MODE_PRIVATE).getBoolean(FORBID_NOTIFICATION_PERMISSION_SP_KEY,false);
    }
}
