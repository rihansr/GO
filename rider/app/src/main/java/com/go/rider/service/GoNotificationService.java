package com.go.rider.service;

import android.app.Notification;
import android.app.NotificationChannel;
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

import com.go.rider.BuildConfig;
import com.go.rider.R;
import com.go.rider.remote.NotificationManager;
import com.go.rider.util.Constants;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

public class GoNotificationService extends FirebaseMessagingService {

    public static int             NOTIFICATION_ID = 1;
    private LocalBroadcastManager tokenBroadcaster;

    @Override
    public void onCreate() {
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
        showNotification(data.get(NotificationManager.TITLE_KEY), data.get(NotificationManager.MESSAGE_KEY));
    }

    private void showNotification(String title, String message) {
        String CHANNEL_ID = BuildConfig.APPLICATION_ID;
        String CHANNEL_NAME = "GO";

        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, android.app.NotificationManager.IMPORTANCE_DEFAULT);
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
