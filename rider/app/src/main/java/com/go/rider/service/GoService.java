package com.go.rider.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

@SuppressLint("StaticFieldLeak")
public class GoService extends MultiDexApplication {

    public static Context   context = GoService.getContext();
    private static Activity activity;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseAnalytics.getInstance(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        GoService.context = getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Context getContext() {
        return context;
    }

    public static void setActivity(Activity currentActivity) {
        activity = currentActivity;
    }

    public static Activity getActivity() {
        return activity;
    }
}
