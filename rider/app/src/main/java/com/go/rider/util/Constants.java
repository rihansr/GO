package com.go.rider.util;

import com.go.rider.BuildConfig;
import com.google.android.libraries.places.api.net.PlacesClient;

public class Constants {

    /**
     * Permission Request
     **/
    public static final int     GPS_PERMISSION_CODE = 1001;
    public static final int     CAMERA_REQUEST_CODE = 1101;
    public static final int     GALLERY_REQUEST_CODE = 1110;
    public static final int     PLACES_REQUEST_CODE = 1111;

    /**
     * GoogleMap
     **/
    public static final String  MAP_API_KEY = BuildConfig.MAP_API_KEY;
    public static final String  GOOGLE_API_URL = BuildConfig.GOOGLE_API_URL;
    public static final String  FCM_URL = BuildConfig.FCM_URL;
    public static               PlacesClient PLACES_CLIENT;
    public static final int     GPS_UPDATE_INTERVAL = 10000;
    public static final int     GPS_FASTEST_INTERVAL = 5000;
    public static final float   DEFAULT_ZOOM = 14f;
    public static final float   DEFAULT_MIN_ZOOM = 6.5f;
    public static final float   DEFAULT_MAX_ZOOM = 22.75f;
    public static final int     UPDATE_INTERVAL = 5000;
    public static final int     FASTEST_INTERVAL = 5000;
    public static final int     DISPLACEMENT = 10;
    public static final int     BOUNDS_PADDING = 160;

    /**
     * Other
     **/
    public static final String  TAG = "Hell";
    public static boolean       IS_INTERNET_CONNECTED = false;
    public static boolean       IS_GPS_ENABLED = false;
    public static final long    SPLASH_TIME_OUT = 3000;
    public static final long    RESEND_CODE_INTERVAL = 30000;
    public static final long    SLIDE_OUT_DURATION = 325;
    public static final String  PLAY_STORE_URL_PREFIX = "https://play.google.com/store/apps/details?id=";
    public static final String  PHONE_PATTERN = "^\\+?(88)?0?1[1356789][0-9]{8}\\b$";
    public static final String  COUNTRY_CODE = "+88";

    /**
     * Intent key
     **/
    public static final String SUCCESS_KEY = "Success_Key";
    public static final String PHOTO_BUNDLE_KEY = "photoLinkKey";
    public static final String PLACE_BUNDLE_KEY = "placeKey";
    public static final String PICKUP_PLACE_BUNDLE_KEY = "pickUpPlaceKey";
    public static final String DROPOFF_PLACE_BUNDLE_KEY = "dropOffPlaceKey";
    public static final String NOTIFICATIONS_INTENT_KEY = "notificationsIntentKey";
    public static final String TOKEN_LISTENER_KEY = "tokenListenerKey";
    public static final String TOKEN_INTENT_KEY = "tokenIntentKey";
    public static final String TRIP_INFO_INTENT_KEY = "tripInfoIntentKey";
    public static final String UID_INTENT_KEY = "uidIntentKey";
    public static final String REPORT_INTENT_KEY = "reportIntentKey";
    public static final String DRIVER_INFO_INTENT_KEY = "driverInfoIntentKey";
    public static final String PHONE_INTENT_KEY = "Phone_Intent_Key";
}
