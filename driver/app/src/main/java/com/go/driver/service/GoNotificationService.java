package com.go.driver.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.go.driver.BuildConfig;
import com.go.driver.R;
import com.go.driver.activity.HomeActivity;
import com.go.driver.model.trip.TripInfo;
import com.go.driver.model.user.Driver;
import com.go.driver.remote.NotificationHelper;
import com.go.driver.util.Constants;
import com.go.driver.util.SharedPreference;
import com.go.driver.util.TempStorage;
import com.go.driver.util.extension.AppExtensions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

public class GoNotificationService extends FirebaseMessagingService {

    public static int             NOTIFICATION_ID = 1;
    private SharedPreference      sp = new SharedPreference(GoService.context);
    private LocalBroadcastManager tripBroadcaster;
    private LocalBroadcastManager tokenBroadcaster;

    @Override
    public void onCreate() {
        tripBroadcaster = LocalBroadcastManager.getInstance(this);
        tokenBroadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i(Constants.TAG, "New Token: " + token);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        Intent tokenIntent = new Intent(Constants.TOKEN_LISTENER_KEY);
        tokenIntent.putExtra(Constants.TOKEN_INTENT_KEY, token);
        tokenBroadcaster.sendBroadcast(tokenIntent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String,String> data = remoteMessage.getData();
        
        if(data.containsKey(NotificationHelper.DATA_KEY)) sentTripRequest(data);
        else showNotification(data.get(NotificationHelper.TITLE_KEY), data.get(NotificationHelper.MESSAGE_KEY), null);
    }

    private void sentTripRequest(Map<String,String> notificationData){
        if(!sp.isDriverLoggedIn()) return;
        if(getDriverInfo() == null) return;
        Driver driver = getDriverInfo();
        if(!driver.getTrip().getPreference().isAvailable() || getTripInfo() != null) return;

        String data = notificationData.get(NotificationHelper.DATA_KEY);
        if(data == null) return;

        TripInfo tripInfo = new Gson().fromJson(data, TripInfo.class);

        if(tripInfo == null) return;
        else if(!driver.getTrip().isVerified()) return;
        else if (driver.getLocation().getCurLatitude() == 0 || driver.getLocation().getCurLongitude() == 0) return;
        else if (!AppExtensions.nearByRider(1,
                new LatLng(tripInfo.getPickUpLocation().getLatitude(), tripInfo.getPickUpLocation().getLongitude()),
                new LatLng(driver.getLocation().getCurLatitude(), driver.getLocation().getCurLongitude()))) return;

        Intent tripIntent = new Intent(Constants.TRIP_INFO_LISTENER_KEY);
        tripIntent.putExtra(Constants.TRIP_INFO_INTENT_KEY, tripInfo);
        tripBroadcaster.sendBroadcast(tripIntent);

        if(!sp.isAppRunning()){
            Intent intent= new Intent(getBaseContext(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.TRIP_INFO_INTENT_KEY, tripInfo);
            PendingIntent contentIntent = PendingIntent.getActivity(this,0, intent,PendingIntent.FLAG_ONE_SHOT);
            showNotification(notificationData.get(NotificationHelper.TITLE_KEY), notificationData.get(NotificationHelper.MESSAGE_KEY), contentIntent);
        }
    }

    public Driver getDriverInfo(){
        String driverInfo = sp.getSignInData(SharedPreference.DRIVER_INFO_KEY);
        return (driverInfo == null) ? null : new Gson().fromJson(driverInfo, Driver.class);
    }

    public TripInfo getTripInfo(){
        String tripInfo = sp.getTripInfo();
        return (tripInfo == null) ? null : new Gson().fromJson(tripInfo, TripInfo.class);
    }

    private void showNotification(String title, String message, PendingIntent contentIntent) {
        String CHANNEL_ID = BuildConfig.APPLICATION_ID;
        String CHANNEL_NAME = "GO";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(true)
                .setVibrate(new long[] { 100, 100, 100, 100, 100 })
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDefaults(Notification.DEFAULT_LIGHTS);

        if(contentIntent != null) builder.setContentIntent(contentIntent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(message);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.CYAN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            assert notificationManager != null;
            builder.setChannelId(CHANNEL_ID);
            notificationManager.createNotificationChannel(channel);
        }

        if (NOTIFICATION_ID > 1073741824) {
            NOTIFICATION_ID = 0;
        }

        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID++,builder.build());
    }
}
