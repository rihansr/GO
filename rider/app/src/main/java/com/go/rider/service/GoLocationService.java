package com.go.rider.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.go.rider.remote.PermissionManager;
import com.go.rider.util.Constants;

@SuppressLint("MissingPermission")
public class GoLocationService extends Service implements LocationListener {

    protected LocationManager locationManager;

    public GoLocationService(Context context) {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
    }

    public Location getLocation(String provider) {
        if (locationManager.isProviderEnabled(provider)) {
            locationManager.requestLocationUpdates(provider, Constants.UPDATE_INTERVAL, Constants.DISPLACEMENT, this);
            if (locationManager != null) return locationManager.getLastKnownLocation(provider);
            else return null;
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
