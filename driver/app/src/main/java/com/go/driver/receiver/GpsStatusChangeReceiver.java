package com.go.driver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.go.driver.activity.HomeActivity;
import com.go.driver.util.Constants;

public class GpsStatusChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = false;

            if (locationManager != null) {
                isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }

            boolean isNetworkEnabled = false;
            if (locationManager != null) {
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }

            if (isGpsEnabled || isNetworkEnabled) {
                if(!Constants.IS_GPS_ENABLED){
                    Constants.IS_GPS_ENABLED = true;
                    ((HomeActivity) context).updateGpsStatus(true);
                }
            } else {
                if(Constants.IS_GPS_ENABLED){
                    Constants.IS_GPS_ENABLED = false;
                    ((HomeActivity) context).updateGpsStatus(false);
                }
            }


        }
    }
}
