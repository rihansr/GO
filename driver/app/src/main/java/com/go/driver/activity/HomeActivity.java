package com.go.driver.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.go.driver.R;
import com.go.driver.fragment.AboutFragment;
import com.go.driver.fragment.AlertDialogFragment;
import com.go.driver.fragment.ContactFragment;
import com.go.driver.fragment.NotificationsFragment;
import com.go.driver.fragment.ProfileFragment;
import com.go.driver.fragment.RatingFragment;
import com.go.driver.fragment.SettingsFragment;
import com.go.driver.fragment.TripsFragment;
import com.go.driver.model.notification.Notification;
import com.go.driver.model.other.ActiveStatus;
import com.go.driver.model.other.Rating;
import com.go.driver.model.trip.Message;
import com.go.driver.model.trip.TripInfo;
import com.go.driver.model.trip.TripStatus;
import com.go.driver.model.user.Driver;
import com.go.driver.model.user.Trip;
import com.go.driver.receiver.GpsStatusChangeReceiver;
import com.go.driver.receiver.NetworkStatusChangeReceiver;
import com.go.driver.remote.FirebaseHelper;
import com.go.driver.remote.NotificationHelper;
import com.go.driver.remote.PermissionManager;
import com.go.driver.service.GoService;
import com.go.driver.util.Constants;
import com.go.driver.util.CustomSnackBar;
import com.go.driver.util.extension.DateExtensions;
import com.go.driver.util.map.GpsUtils;
import com.go.driver.util.map.LatLngInterpolator;
import com.go.driver.util.map.MapDirection;
import com.go.driver.util.map.MarkerAnimation;
import com.go.driver.util.SharedPreference;
import com.go.driver.util.TempStorage;
import com.go.driver.util.extension.AppExtensions;
import com.go.driver.wiget.CircleImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.graphics.Color.TRANSPARENT;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

@SuppressLint("SetTextI18n, MissingPermission, InflateParams")
public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    /**
     * Menu
     **/
    public static DrawerLayout          drawerLayout;
    private CoordinatorLayout           contentView;
    private NavigationView              navigationView;
    private AppCompatImageButton        navMenu;
    private CircleImageView             profilePhoto;
    private AppCompatTextView           userName;
    private AppCompatTextView           userPhone;
    private AppCompatTextView           navProfile;
    private AppCompatTextView           navTrips;
    private AppCompatTextView           navNotification;
    private AppCompatTextView           navSettings;
    private AppCompatTextView           navAbout;

    /**
     * Trip Request
     **/
    private LinearLayoutCompat          tripRequest_Content;
    private BottomSheetBehavior         tripRequest_BottomSheet;
    private CircleImageView             riderPhoto;
    private AppCompatTextView           riderName;
    private AppCompatTextView           riderRating;
    private AppCompatTextView           estimateTime;
    private AppCompatTextView           estimateDistance;
    private AppCompatTextView           pickUpLocation;
    private AppCompatTextView           dropOffLocation;
    private AppCompatButton             leftBtn;
    private AppCompatButton             rightBtn;
    private AppCompatImageButton        backBtn;

    /**
     * Firebase
     **/
    private DatabaseReference           onlinePreferenceReference;
    private ValueEventListener          onlinePreferenceListener;
    private DatabaseReference           profileTripReference;
    private ValueEventListener          profileTripListener;
    private DatabaseReference           notificationReference;
    private ValueEventListener          notificationListener;
    private DatabaseReference           tripRequestReference;
    private ValueEventListener          tripRequestListener;

    /**
     * Map
     **/
    private GoogleMap                   mMap;
    private Marker                      mMarker;
    private boolean                     goingForPermission = false;
    private boolean                     isCameraPositionOk = false;
    private boolean                     isMarkerRotating = false;
    private float                       previousRotation = 0;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback            mLocationCallback;
    private LocationRequest             mLocationRequest;
    private AppCompatImageButton        snapBtn;
    private LatLngBounds                bounds = null;
    private MapDirection                mapDirection;
    private boolean                     allowMovingVehicle = true;

    /**
     * Other
     **/
    private List<Notification>          notifications = null;
    private FirebaseHelper              firebaseHelper = new FirebaseHelper(this);
    private SharedPreference            sp = new SharedPreference();
    private NetworkStatusChangeReceiver networkStatusChangeReceiver = new NetworkStatusChangeReceiver();
    private GpsStatusChangeReceiver     gpsStatusChangeReceiver = new GpsStatusChangeReceiver();
    private OnNotificationListener      mOnNotificationListener;
    private OnMessageListener           mOnMessageListener;
    private OnDismissListener           mOnDismissListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppExtensions.fullScreenActivity(getWindow(), false);
        setContentView(R.layout.activity_home);
        GoService.setActivity(HomeActivity.this);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        new SharedPreference().setAppRunning(true);

        onlinePreference();

        checkProfileTrips();

        checkNotifications();

        registerReceiver(networkStatusChangeReceiver, new IntentFilter(CONNECTIVITY_ACTION));

        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        registerReceiver(gpsStatusChangeReceiver, filter);

        LocalBroadcastManager.getInstance(HomeActivity.this).registerReceiver(mTripInfoReceiver, new IntentFilter(Constants.TRIP_INFO_LISTENER_KEY));
        LocalBroadcastManager.getInstance(HomeActivity.this).registerReceiver(mTokenReceiver, new IntentFilter(Constants.TOKEN_LISTENER_KEY));

        if (!new PermissionManager(PermissionManager.Permission.LOCATION, false).isGranted()){ goingForPermission = true; return; }
        if(goingForPermission){ setLocation(); goingForPermission = false; }
    }

    private void init() {
        initId();                                                   /** Initialize Id **/
        handleBottomSheet(tripRequest_BottomSheet, true);

        /**
         * Google Map Initialize
         **/
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        /**
         * Setup Profile Details
         **/
        TempStorage.setDriverInfo(null, false);
        setProfileInfo();

        setDrawer();                                                /** Navigation Drawer Setup **/

        checkDriverToken();                                         /** Check Rider Token Changed or not **/

        TempStorage.setLatLng(null);                                /** Initially Set Map LatLng **/

        TempStorage.setTripInfo(null, false);         /** Check Previous Trip Completed or not **/

        checkTripInfo();                                            /** Check Rider is on Trip or not **/

        /**
         * Check Notification Allowed or not
         **/
        if(!sp.isNotificationPermissionForbidded()
                &&
                !NotificationManagerCompat.from(this).areNotificationsEnabled()) showNotificationDialog();

        /**
         * Check Successfully Logged In or not
         **/
        if (!getIntent().hasExtra(Constants.SUCCESS_KEY)) return;
        if(!Objects.equals(getIntent().getStringExtra(Constants.SUCCESS_KEY), "SUCCESS")) return;

        sp.setDriverLoggedIn(true);
        getIntent().removeExtra(Constants.SUCCESS_KEY);
    }

    private void initId() {
        drawerLayout = findViewById(R.id.drawerLayout);
        contentView = findViewById(R.id.contentView);
        navigationView = findViewById(R.id.navigationView);
        tripRequest_Content = findViewById(R.id.tripRequestContent);
        tripRequest_BottomSheet = BottomSheetBehavior.from(tripRequest_Content);
        riderPhoto = findViewById(R.id.riderPhoto);
        riderName = findViewById(R.id.riderName);
        riderRating = findViewById(R.id.riderRating);
        estimateTime = findViewById(R.id.estimateTime);
        estimateDistance = findViewById(R.id.estimateDistance);
        pickUpLocation = findViewById(R.id.pickUpLocation);
        dropOffLocation = findViewById(R.id.dropOffLocation);
        leftBtn = findViewById(R.id.declineBtn);
        rightBtn = findViewById(R.id.acceptBtn);
        backBtn = findViewById(R.id.back_Btn);
        navMenu = findViewById(R.id.menu_Btn);
        snapBtn = findViewById(R.id.snap_Btn);
        profilePhoto = findViewById(R.id.profilePhoto);
        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        navProfile = findViewById(R.id.navProfile);
        navTrips = findViewById(R.id.navTrips);
        navNotification = findViewById(R.id.navNotification);
        navSettings = findViewById(R.id.navSettings);
        navAbout = findViewById(R.id.navAbout);
    }

    private void setDrawer(){
        navMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.setDrawerElevation(0);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                contentView.setX(navigationView.getWidth() * slideOffset);
                DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) contentView.getLayoutParams();
                contentView.setLayoutParams(params);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        profilePhoto.setOnClickListener(v ->
                ProfileFragment.show(HomeActivity.this).setOnProfileUpdateListener(isUpdated -> {
                    if (isUpdated) setProfileInfo();
                })
        );

        navProfile.setOnClickListener(v ->
                ProfileFragment.show(HomeActivity.this).setOnProfileUpdateListener(isUpdated -> {
                    if (isUpdated) setProfileInfo();
                })
        );

        navTrips.setOnClickListener(v -> TripsFragment.show(HomeActivity.this));

        navNotification.setOnClickListener(v -> NotificationsFragment.show(HomeActivity.this, notifications));

        navSettings.setOnClickListener(v ->
                SettingsFragment.show(HomeActivity.this).setOnSettingsUpdateListener(isUpdated -> {
                    if (isUpdated) setProfileInfo();
                })
        );

        navAbout.setOnClickListener(v -> AboutFragment.show(HomeActivity.this));
    }

    private void onlinePreference() {
        onlinePreferenceReference = firebaseHelper.driverReference();

        onlinePreferenceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) return;
                onlinePreferenceReference.child(FirebaseHelper.USER_ACTIVE_KEY).setValue(new ActiveStatus(true, null));
                onlinePreferenceReference.child(FirebaseHelper.USER_ACTIVE_KEY).onDisconnect().setValue(new ActiveStatus(false, DateExtensions.currentTime()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        onlinePreferenceReference.addValueEventListener(onlinePreferenceListener);
    }

    private void checkProfileTrips() {
        profileTripReference = firebaseHelper.driverReference().child(FirebaseHelper.TRIP_KEY);

        profileTripListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;
                Trip trip = dataSnapshot.getValue(Trip.class);
                if (trip == null) return;
                TempStorage.DRIVER.setTrip(trip);
                TempStorage.setDriverInfo(TempStorage.DRIVER, true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        profileTripReference.addValueEventListener(profileTripListener);
    }

    private void checkNotifications() {
        notificationReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.NOTIFICATIONS_TABLE);

        notificationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notifications = new ArrayList<>();

                if(!dataSnapshot.exists()) {
                    if(mOnNotificationListener != null) mOnNotificationListener.onNotificationReceive(notifications);
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification == null) continue;
                    if(notification.getUserType() != null && notification.getUserType() == 0) continue;
                    if(notification.getExpireAt() != 0 && notification.getExpireAt() < DateExtensions.currentTime()) continue;
                    notifications.add(notification);
                }

                if(mOnNotificationListener != null) mOnNotificationListener.onNotificationReceive(notifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(mOnNotificationListener != null) mOnNotificationListener.onNotificationReceive(null);
            }
        };

        notificationReference.addListenerForSingleValueEvent(notificationListener);
    }

    private void checkDriverToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.i(Constants.TAG, "Token Task Failed: ", task.getException());
                return;
            }

            String currentToken = Objects.requireNonNull(task.getResult());
            updateToken(currentToken);
        });
    }

    private void updateToken(String newToken){
        if(newToken == null) return;
        if (TempStorage.DRIVER == null) return;
        Driver tempDriver = new Driver(TempStorage.DRIVER, false);

        if (tempDriver.getToken() == null || !tempDriver.getToken().equals(newToken)) {
            firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_TOKEN_KEY).setValue(newToken),
                    new FirebaseHelper.OnFirebaseUpdateListener() {
                        @Override
                        public void onSuccess() {
                            tempDriver.setToken(newToken);
                            TempStorage.setDriverInfo(tempDriver, true);
                        }

                        @Override
                        public void onFailure() {}
                    });
        }
    }

    private void setProfileInfo() {
        if(TempStorage.DRIVER == null) return;

        int placeHolder = TempStorage.DRIVER.getGender() == null || TempStorage.DRIVER.getGender().equals(AppExtensions.Gender.Male.toString()) ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;
        AppExtensions.loadPhoto(profilePhoto, TempStorage.DRIVER.getProfilePhoto(), R.dimen.icon_Size_XXXX_Large, placeHolder);

        userName.setText(TempStorage.DRIVER.getName());
        userPhone.setText(TempStorage.DRIVER.getPhone());
    }

    private void checkTripInfo(){
        if(TempStorage.TRIP_INFO == null && TempStorage.DRIVER.getTrip().isOnTrip()) {
            firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
            TempStorage.DRIVER.getTrip().setOnTrip(false);
            TempStorage.setDriverInfo(TempStorage.DRIVER, true);
            if(getIntent().hasExtra(Constants.TRIP_INFO_INTENT_KEY)) getTripRequest(getIntent());
            return;
        }

        if(TempStorage.TRIP_INFO == null) {
            firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
            if(getIntent().hasExtra(Constants.TRIP_INFO_INTENT_KEY)) getTripRequest(getIntent());
            return;
        }

        if(!TempStorage.DRIVER.getTrip().isOnTrip()) {
            TempStorage.setTripInfo(null, true);
            firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
            if(getIntent().hasExtra(Constants.TRIP_INFO_INTENT_KEY)) getTripRequest(getIntent());
            return;
        }

        if (!TempStorage.TRIP_INFO.getTripStatus().isTripCancelled()) {
            FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE).child(TempStorage.TRIP_INFO.getTripId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
                                TempStorage.setTripInfo(null, true);
                                TempStorage.DRIVER.getTrip().setOnTrip(false);
                                TempStorage.setDriverInfo(TempStorage.DRIVER, true);
                                if(getIntent().hasExtra(Constants.TRIP_INFO_INTENT_KEY)) getTripRequest(getIntent());
                                return;
                            }

                            TripInfo tripInfo = dataSnapshot.getValue(TripInfo.class);

                            if(tripInfo == null || tripInfo.getTripStatus().isTripCancelled() || tripInfo.getTripStatus().isTripFinished()) {
                                firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
                                TempStorage.setTripInfo(null, true);
                                TempStorage.DRIVER.getTrip().setOnTrip(false);
                                TempStorage.setDriverInfo(TempStorage.DRIVER, true);
                                if(getIntent().hasExtra(Constants.TRIP_INFO_INTENT_KEY)) getTripRequest(getIntent());
                                return;
                            }

                            tripCancellationDialog(tripInfo, false);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            if(getIntent().hasExtra(Constants.TRIP_INFO_INTENT_KEY)) getTripRequest(getIntent());
                        }
                    });
        }
        else {
            firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false),null);
            TempStorage.setTripInfo(null, true);
            TempStorage.DRIVER.getTrip().setOnTrip(false);
            TempStorage.setDriverInfo(TempStorage.DRIVER, true);
            if(getIntent().hasExtra(Constants.TRIP_INFO_INTENT_KEY)) getTripRequest(getIntent());
        }
    }

    private void trackCurrentTrip(String tripId){
        tripRequestReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE).child(tripId);

        tripRequestListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) { updateUI(); return; }

                TripInfo tripInfo = dataSnapshot.getValue(TripInfo.class);
                if(tripInfo == null) { updateUI(); return; }

                /**
                 * Check trip cancelled or not
                 * if cancelled, keep waiting for next trip otherwise proceed
                 **/
                if(tripInfo.getTripStatus().isTripCancelled()) {
                    new CustomSnackBar(drawerLayout, R.string.rideCancelled, CustomSnackBar.Duration.SHORT).show();
                    firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
                    updateUI(); return;
                }

                /**
                 * Check trip finished or not
                 * if finished, give rating & wait for next trip otherwise proceed
                 **/
                if(tripInfo.getTripStatus().isTripFinished()){
                    new NotificationHelper(tripInfo.getRider().getToken(),
                            "Rate your trip",
                            "Thanks for riding with " + AppExtensions.nameFormat(TempStorage.DRIVER.getName()) + "! Please rate your trip, and you can also add a tip.").send();

                    RatingFragment.show(HomeActivity.this, tripInfo);
                    updateUI(); return;
                }

                backBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Decline));

                /**
                 * Check trip not accepted or not
                 * if accepted then show trip direction(pickup to dropoff point)
                 **/
                if (tripInfo.getTripStatus().isTripAccepted()) {
                    if(tripInfo.getMessages() != null && mOnMessageListener != null) mOnMessageListener.onMessageReceive(tripInfo.getMessages());
                    leftBtn.setText((tripInfo.getMessages() != null && TempStorage.TRIP_INFO != null && TempStorage.TRIP_INFO.getMessages() == null)
                            ||
                            (tripInfo.getMessages() != null && TempStorage.TRIP_INFO != null && TempStorage.TRIP_INFO.getMessages() != null
                                    &&
                                    tripInfo.getMessages().size() != TempStorage.TRIP_INFO.getMessages().size())
                            ?
                            AppExtensions.getHtmlString(R.string.newMessage)
                            :
                            AppExtensions.getString(R.string.contact));

                    leftBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Contact));

                    if(TempStorage.TRIP_INFO != null && TempStorage.TRIP_INFO.getTripStatus().isTripAccepted()){
                        if(tripInfo.getTripStatus().isTripStarted()){
                            if(!TempStorage.TRIP_INFO.getTripStatus().isTripStarted()){
                                leftBtn.setText(AppExtensions.getString(R.string.contact));
                                rightBtn.setText(AppExtensions.getString(R.string.finish));
                                rightBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Finish));
                                showTripDirection(tripInfo);
                            }
                        }

                        TempStorage.setTripInfo(tripInfo, true);
                    }
                    else {
                        if (tripInfo.getDriver() != null) {
                            if (tripInfo.getDriver().getId().equals(TempStorage.DRIVER.getId())) {
                                new NotificationHelper(tripInfo.getRider().getToken(),
                                        "En Route",
                                        "Your driver is on the way. " + AppExtensions.nameFormat(TempStorage.DRIVER.getName()) + " will arrive in " + estimateTime.getText().toString() + ".").send();

                                new NotificationHelper(tripInfo.getRider().getToken(),
                                        "Check your ride, every time",
                                        "Check the license plate" + "(" + TempStorage.DRIVER.getVehicleInfo().getVehicleRegistration() + "), "
                                                +
                                                "car details" + "(" + TempStorage.DRIVER.getVehicleInfo().getVehicleType().getModel() + ") "
                                                +
                                                "and match the driver's photo before you ride.").send();

                                rightBtn.setText(AppExtensions.getString(R.string.start));
                                rightBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Start));
                                allowMovingVehicle = true;
                                mMarker = null;
                                displayLocation();
                                TempStorage.setTripInfo(tripInfo, true);
                            }
                            else {
                                new CustomSnackBar(drawerLayout, "Your ride with " + AppExtensions.nameFormat(tripInfo.getRider().getName()) + " has been booked", CustomSnackBar.Duration.LONG).show();
                                updateUI();
                            }
                        }
                        else updateUI();
                    }
                }
                else {
                    if(tripRequest_BottomSheet.getState() == BottomSheetBehavior.STATE_HIDDEN){
                        if(mOnDismissListener != null) mOnDismissListener.onDismiss();
                        leftBtn.setText(AppExtensions.getString(R.string.decline));
                        leftBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Decline));
                        rightBtn.setText(AppExtensions.getString(R.string.accept));
                        rightBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Accept));
                        showPickupDirection(tripInfo);
                    }
                    else {
                        new CustomSnackBar(drawerLayout, R.string.rideCancelled, CustomSnackBar.Duration.SHORT).show();
                        updateUI();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        tripRequestReference.addValueEventListener(tripRequestListener);
    }

    private void showPickupDirection(TripInfo tripInfo) {
        if(tripInfo == null) return;
        if(TempStorage.CURRENT_LAT_LNG.latitude == 0 || TempStorage.CURRENT_LAT_LNG.longitude == 0) return;
        final LatLng driverPoint =  TempStorage.CURRENT_LAT_LNG;
        final LatLng pickUpPoint =  new LatLng(tripInfo.getPickUpLocation().getLatitude(), tripInfo.getPickUpLocation().getLongitude());

        if(mapDirection != null && mapDirection.isAnimatorPlaying()) mapDirection.stop();

        mapDirection = new MapDirection(mMap, driverPoint, pickUpPoint, new MapDirection.OnDirectionListener() {
            @Override
            public void onSuccess() {
                allowMovingVehicle = false;
                mMap.clear();
            }

            @Override
            public void onData(double distance, long time, LatLngBounds bounds) {
                HomeActivity.this.bounds = bounds;

                showRequestInfo(tripInfo, distance, time);

                mMap.setInfoWindowAdapter(infoWindowAdapter);

                mMap.addMarker(buildMarkerOptions(driverPoint, R.string.myLocation, String.valueOf(time), R.drawable.ic_marker_from, null));
                mMap.addMarker(buildMarkerOptions(pickUpPoint, R.string.pickUpLocation, String.valueOf(time), R.drawable.ic_marker_to, null)).showInfoWindow();
            }

            @Override
            public void onFailure() {}
        });

        mapDirection.show();
    }

    private void showTripDirection(TripInfo tripInfo) {
        if(tripInfo == null) return;
        final LatLng pickUpPoint =  new LatLng(tripInfo.getPickUpLocation().getLatitude(), tripInfo.getPickUpLocation().getLongitude());
        final LatLng dropOffPoint =  new LatLng(tripInfo.getDropOffLocation().getLatitude(), tripInfo.getDropOffLocation().getLongitude());

        if(mapDirection != null && mapDirection.isAnimatorPlaying()) mapDirection.stop();

        mapDirection = new MapDirection(mMap, pickUpPoint, dropOffPoint, new MapDirection.OnDirectionListener() {
            @Override
            public void onSuccess() {
                allowMovingVehicle = true;
                mMarker = null;
                mMap.clear();
                displayLocation();
            }

            @Override
            public void onData(double distance, long time, LatLngBounds bounds) {
                HomeActivity.this.bounds = bounds;

                if(tripRequest_BottomSheet.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    showRequestInfo(tripInfo, distance, time);
                }
                else {
                    String[] t = new DateExtensions(HomeActivity.this, time).getTime();
                    estimateTime.setText(t[0] + " " + t[1]);
                    estimateDistance.setText(AppExtensions.decimalFormat(distance/1000, "#.#", false)
                            + " " + AppExtensions.getString(R.string.km));

                    getViewHeight(tripRequest_Content);
                }

                mMap.setInfoWindowAdapter(infoWindowAdapter);

                mMap.addMarker(buildMarkerOptions(pickUpPoint, R.string.pickUp, String.valueOf(time), R.drawable.ic_marker_from, null));
                mMap.addMarker(buildMarkerOptions(dropOffPoint, R.string.dropOff, String.valueOf(time), R.drawable.ic_marker_to, null)).showInfoWindow();
            }

            @Override
            public void onFailure() {}
        });

        mapDirection.show();
    }

    private void showRequestInfo(TripInfo tripInfo, double distance, long time){
        if(tripInfo == null) return;
        backBtn.setVisibility(View.VISIBLE);
        navMenu.setVisibility(View.GONE);
        handleBottomSheet(tripRequest_BottomSheet, false);

        int placeHolder = tripInfo.getRider().getGender() == null || tripInfo.getRider().getGender().equals(AppExtensions.Gender.Male.toString())
                ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;

        AppExtensions.loadPhoto(riderPhoto, tripInfo.getRider().getProfilePhoto(), R.dimen.icon_Size_XX_Large, placeHolder);

        riderName.setText(tripInfo.getRider().getName());

        if(tripInfo.getRider().getTrip().getRatings() != null){
            int ratingCount = 0; double totalRating = 0;

            for (Map.Entry<String, Rating> entry : tripInfo.getRider().getTrip().getRatings().entrySet()) {
                ratingCount++;
                Rating rating = entry.getValue();
                totalRating = totalRating + rating.getRating();
            }

            if(ratingCount == 0 || totalRating == 0) riderRating.setText(AppExtensions.decimalFormat(0, "0.0", true));
            else riderRating.setText(AppExtensions.decimalFormat(totalRating / ratingCount, "0.0", true));
        }
        else riderRating.setText(AppExtensions.decimalFormat(0, "0.0", true));

        String[] t = new DateExtensions(HomeActivity.this, time).getTime();
        this.estimateTime.setText(t[0] + " " + t[1]);

        this.estimateDistance.setText(AppExtensions.decimalFormat(distance/1000, "#.#", false) + " " + AppExtensions.getString(R.string.kmAway));

        pickUpLocation.setText(tripInfo.getPickUpLocation().getAddress());
        dropOffLocation.setText(tripInfo.getDropOffLocation().getAddress());

        getViewHeight(tripRequest_Content);

        backBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Decline));
    }

    private void tripCancellationDialog(TripInfo tripInfo, boolean tripIsRunning){
        if(tripInfo == null) return;

        AlertDialogFragment cancellationDialog = AlertDialogFragment.show(HomeActivity.this,
                R.string.cancelRide, AppExtensions.getString(R.string.cancelRideMessage) + " " + AppExtensions.nameFormat(tripInfo.getRider().getName()) + "?",
                R.string.yesCancel, tripIsRunning ? R.string.no : R.string.goOn);

        cancellationDialog.setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
            @Override
            public void onLeftButtonClick() {
                DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE).child(tripInfo.getTripId());

                if(tripInfo.getTripStatus().isTripAccepted() || tripInfo.getTripStatus().isTripStarted()){
                    TripInfo tempInfo = new TripInfo(tripInfo);
                    HashMap<String, String> drivers = tempInfo.getRequestedDrivers();
                    if(drivers != null) drivers.remove(TempStorage.DRIVER.getId());

                    List<String> availableDrivers = new ArrayList<>();

                    if(drivers != null && !drivers.isEmpty()){
                        for (Map.Entry<String, String> entry :  drivers.entrySet()){
                            availableDrivers.add(entry.getValue());
                        }
                    }

                    tempInfo.setDriver(null);
                    tempInfo.setMessages(null);

                    firebaseHelper.setData(tripReference.child(FirebaseHelper.TRIP_DRIVER_KEY).setValue(null), null);
                    if(availableDrivers.size() != 0) {
                        tempInfo.setRequestedDrivers(drivers);
                        tempInfo.setMessages(null);
                        tempInfo.setTripStatus(new TripStatus(false, false, false, false));

                        firebaseHelper.setData(tripReference.setValue(tempInfo), new FirebaseHelper.OnFirebaseUpdateListener() {
                            @Override
                            public void onSuccess() {
                                String data = new Gson().toJson(tempInfo);
                                new NotificationHelper(availableDrivers, "Trip Request",
                                        AppExtensions.nameFormat(tempInfo.getRider().getName()) + " sent you a trip request! Please check it out.", data)
                                        .send();
                            }

                            @Override
                            public void onFailure() {
                                tempInfo.setRequestedDrivers(null);
                                TripStatus tripStatus = tempInfo.getTripStatus();
                                tripStatus.setTripCancelled(true);
                                tempInfo.setTripStatus(tripStatus);
                                firebaseHelper.setData(tripReference.setValue(tempInfo), null);
                            }
                        });
                    }
                    else {
                        TripStatus tripStatus = tempInfo.getTripStatus();
                        tripStatus.setTripCancelled(true);
                        tempInfo.setTripStatus(tripStatus);
                        firebaseHelper.setData(tripReference.setValue(tempInfo), null);
                    }

                    firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);

                    TempStorage.DRIVER.getTrip().setOnTrip(false);
                    TempStorage.setDriverInfo( TempStorage.DRIVER, true);
                    if (tripRequest_BottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) TempStorage.setTripInfo(null, true);

                    new NotificationHelper(tripInfo.getRider().getToken(),
                            "Trip Cancelled",
                            "Unfortunately, your driver had to cancel the trip. Please request a new ride and we'll get you moving shortly.").send();
                }
                else {
                    firebaseHelper.setData(tripReference.child(FirebaseHelper.REQUESTED_DRIVERS_KEY).child(TempStorage.DRIVER.getId()).setValue(null), null);
                }

                if(getIntent().hasExtra(Constants.TRIP_INFO_INTENT_KEY)) getTripRequest(getIntent());
            }

            @Override
            public void onRightButtonClick() {
                if (!tripIsRunning) {
                    if (tripInfo.getTripStatus().isTripAccepted() && !tripInfo.getTripStatus().isTripStarted()) {
                        if (tripInfo.getDriver() != null) {
                            if (tripInfo.getDriver().getId().equals(TempStorage.DRIVER.getId())) {
                                leftBtn.setText(AppExtensions.getString(R.string.contact));
                                leftBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Contact));
                                rightBtn.setText(AppExtensions.getString(R.string.start));
                                rightBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Start));
                                showTripDirection(tripInfo);
                                trackCurrentTrip(tripInfo.getTripId());
                            }
                            else {
                                new CustomSnackBar(drawerLayout, "Your ride with " + AppExtensions.nameFormat(tripInfo.getRider().getName()) + " has been booked", CustomSnackBar.Duration.LONG).show();
                                firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
                                TempStorage.DRIVER.getTrip().setOnTrip(false);
                                TempStorage.setDriverInfo(TempStorage.DRIVER, true);
                            }
                        }
                        else {
                            new CustomSnackBar(drawerLayout, R.string.rideCancelled).show();
                            firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
                            TempStorage.DRIVER.getTrip().setOnTrip(false);
                            TempStorage.setDriverInfo(TempStorage.DRIVER, true);
                        }
                    }
                    else if (tripInfo.getTripStatus().isTripStarted()) {
                        leftBtn.setText(AppExtensions.getString(R.string.contact));
                        leftBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Contact));
                        rightBtn.setText(AppExtensions.getString(R.string.finish));
                        rightBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Finish));
                        showTripDirection(tripInfo);
                        trackCurrentTrip(tripInfo.getTripId());
                    }
                }
            }
        });

        cancellationDialog.setCancelable(tripIsRunning);
    }

    public class TripListener implements OnClickListener {

        private TripInfo      tripInfo;
        private boolean       isRunning = true;
        private AppExtensions.TripRequest   action;

        public TripListener(TripInfo tripInfo, AppExtensions.TripRequest action) {
            this.tripInfo = tripInfo;
            this.action = action;
        }

        public TripListener(TripInfo tripInfo, boolean isRunning, AppExtensions.TripRequest action) {
            this.tripInfo = tripInfo;
            this.isRunning = isRunning;
            this.action = action;
        }

        @Override
        public void onClick(View v) {
            DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE);
            DatabaseReference riderReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.RIDERS_TABLE);

            switch (action){
                case Accept:
                    firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(true),
                            new FirebaseHelper.OnFirebaseUpdateListener() {
                                @Override
                                public void onSuccess() {
                                    TempStorage.DRIVER.getTrip().setOnTrip(true);
                                    TempStorage.setDriverInfo(TempStorage.DRIVER, true);
                                }

                                @Override
                                public void onFailure() {}
                            });

                    firebaseHelper.setData(riderReference.child(tripInfo.getRider().getId()).child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(true), null);

                    tripInfo.getVehicleType().setModel(TempStorage.DRIVER.getVehicleInfo().getVehicleType().getModel());
                    tripInfo.setDriver(new Driver(TempStorage.DRIVER, true));
                    tripInfo.getTripStatus().setTripAccepted(true);

                    firebaseHelper.setData(tripReference.child(tripInfo.getTripId()).setValue(tripInfo), null);
                    break;

                case Start:
                    AlertDialogFragment.show(HomeActivity.this, R.string.startRide, R.string.startRideMessage, R.string.notNow, R.string.letsStart)
                            .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                                @Override
                                public void onLeftButtonClick() {}

                                @Override
                                public void onRightButtonClick() {
                                    firebaseHelper.setData(tripReference.child(tripInfo.getTripId()).child(FirebaseHelper.TRIP_STARTED_KEY).setValue(true), null);
                                }
                            });
                    break;

                case Finish:
                    AlertDialogFragment.show(HomeActivity.this, R.string.endRide, R.string.endRideMessage, R.string.notNow, R.string.yes)
                            .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                                @Override
                                public void onLeftButtonClick() {}

                                @Override
                                public void onRightButtonClick() {
                                    firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false),
                                            new FirebaseHelper.OnFirebaseUpdateListener() {
                                                @Override
                                                public void onSuccess() {
                                                    TempStorage.DRIVER.getTrip().setOnTrip(false);
                                                    TempStorage.setDriverInfo(TempStorage.DRIVER, true);
                                                }

                                                @Override
                                                public void onFailure() {}
                                            });
                                    firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.TOTAL_TRIP_KEY).setValue(TempStorage.DRIVER.getTrip().getTotalTrips() + 1), null);

                                    firebaseHelper.setData(riderReference.child(tripInfo.getRider().getId()).child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
                                    firebaseHelper.setData(riderReference.child(tripInfo.getRider().getId()).child(FirebaseHelper.TOTAL_TRIP_KEY).setValue(tripInfo.getRider().getTrip().getTotalTrips() + 1), null);

                                    firebaseHelper.setData(tripReference.child(tripInfo.getTripId()).child(FirebaseHelper.TRIP_FINISHED_KEY).setValue(true), null);
                                }
                            });
                    break;

                case Decline:
                    tripCancellationDialog(tripInfo, isRunning);
                    break;

                case Contact:
                    ContactFragment.show(HomeActivity.this, tripInfo)
                            .setOnContactListener(() -> leftBtn.setText(AppExtensions.getString(R.string.contact)));
                    break;
            }
        }
    }

    private BroadcastReceiver mTripInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (tripRequest_BottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) return;
            getTripRequest(intent);
        }
    };

    private BroadcastReceiver mTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String token = intent.getStringExtra(Constants.TOKEN_INTENT_KEY);
            updateToken(token);
            intent.removeExtra(Constants.TOKEN_INTENT_KEY);
        }
    };

    private void getTripRequest(Intent intent){
        TripInfo tripInfo = (TripInfo) intent.getSerializableExtra(Constants.TRIP_INFO_INTENT_KEY);
        if(tripInfo == null) return;
        else if(TempStorage.TRIP_INFO != null && TempStorage.TRIP_INFO.getTripId().equals(tripInfo.getTripId())) return;
        else if(tripInfo.getPickUpTime() > (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5))) return;
        trackCurrentTrip(tripInfo.getTripId());
        intent.removeExtra(Constants.TRIP_INFO_INTENT_KEY);
    }

    public void handleBottomSheet(BottomSheetBehavior behavior, boolean doHide){
        if(doHide){
            behavior.setHideable(true);
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        else {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            new Handler().postDelayed(() -> { behavior.setHideable(false); }, 500);
        }
    }

    private void updateUI(){
        if(mOnDismissListener != null) mOnDismissListener.onDismiss();
        if (tripRequestReference != null && tripRequestListener != null) tripRequestReference.removeEventListener(tripRequestListener);
        if(mapDirection != null) { mapDirection.cancel(); mapDirection = null; }
        TempStorage.setTripInfo(null, true);
        TempStorage.DRIVER.getTrip().setOnTrip(false);
        TempStorage.setDriverInfo(TempStorage.DRIVER, true);
        mMap.setInfoWindowAdapter(null);
        leftBtn.setText(AppExtensions.getString(R.string.decline));
        leftBtn.setOnClickListener(null);
        rightBtn.setText(AppExtensions.getString(R.string.accept));
        rightBtn.setOnClickListener(null);
        backBtn.setVisibility(View.GONE);
        backBtn.setOnClickListener(null);
        navMenu.setVisibility(View.VISIBLE);
        mMap.clear();
        this.allowMovingVehicle = true;
        this.mMarker = null;
        this.bounds = null;
        handleBottomSheet(tripRequest_BottomSheet, true);
        resizeMap(0, 32, true);
        displayLocation();
    }

    /**
     * Google Map
     **/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        AppExtensions.setMapStyle(googleMap);
        mMap = googleMap;
        AppExtensions.setMapSettings(mMap);
        resizeMap(0, 32, false);

        if (!new PermissionManager(PermissionManager.Permission.LOCATION, true, response -> setLocation() ).isGranted()) return;
        setLocation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.GPS_PERMISSION_CODE) {
                Constants.IS_GPS_ENABLED = true;
            }
            else if (requestCode == Constants.PLACES_REQUEST_CODE) {
                com.google.android.libraries.places.api.model.Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();
                if(latLng == null) return;
                if(bounds != null) mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Constants.BOUNDS_PADDING));
                else mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , Constants.DEFAULT_ZOOM));
            }
        }
        else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.i(Constants.TAG, Objects.requireNonNull(status.getStatusMessage()));
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(HomeActivity.this);
        if (ConnectionResult.SUCCESS == status) return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status)){
                CustomSnackBar customSnackBar = new CustomSnackBar(drawerLayout, R.string.playServiceError, R.string.retry, CustomSnackBar.Duration.INDEFINITE);
                customSnackBar.show();
                customSnackBar.setOnDismissListener(snackbar -> {
                    isGooglePlayServicesAvailable();
                    snackbar.dismiss();
                });
            }
        }
        return false;
    }

    private void turnGPSOn(){
        new GpsUtils(HomeActivity.this).turnGPSOn(isGPSEnable -> {
            /**
             * turn on GPS
             **/
            Constants.IS_GPS_ENABLED = isGPSEnable;
        });
    }

    private void setLocation() {
        if(!isGooglePlayServicesAvailable()) return;
        if(!Constants.IS_GPS_ENABLED) turnGPSOn();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeActivity.this);
        startLocationUpdates();

        snapBtn.setOnClickListener(v -> {
            if(bounds != null) { mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Constants.BOUNDS_PADDING)); return; }
            if (!new PermissionManager(PermissionManager.Permission.LOCATION, true, response -> {
                if(TempStorage.CURRENT_LOCATION == null) return;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TempStorage.CURRENT_LAT_LNG , Constants.DEFAULT_ZOOM));
            }).isGranted()) return;
            else if(TempStorage.CURRENT_LOCATION == null) return;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TempStorage.CURRENT_LAT_LNG , Constants.DEFAULT_ZOOM));
        });

        buildLocationRequest();
        buildLocationCallback();
        displayLocation();
    }

    private void startLocationUpdates() {
        if (!new PermissionManager(PermissionManager.Permission.LOCATION, true, response -> startLocationUpdates() ).isGranted()) return;

        buildLocationRequest();
        buildLocationCallback();

        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void stopLocationUpdates() {
        if (!new PermissionManager(PermissionManager.Permission.LOCATION, false).isGranted()) return;

        buildLocationCallback();
        buildLocationRequest();

        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(Constants.DISPLACEMENT);
    }

    private void buildLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                displayLocation();
            }
        };
    }

    private void displayLocation() {
        if (!new PermissionManager(PermissionManager.Permission.LOCATION, true, response -> displayLocation() ).isGranted()) return;

        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if(TempStorage.DRIVER == null) return;
            TempStorage.CURRENT_LOCATION = location;

            if(TempStorage.CURRENT_LOCATION != null){
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                TempStorage.setLatLng(latLng);

                if(!isCameraPositionOk){
                    animateCamera(location);
                    isCameraPositionOk = true;
                }

                AppExtensions.Vehicle vehicleType = AppExtensions.Vehicle.valueOf(TempStorage.DRIVER.getVehicleInfo().getVehicleType().getType());

                if(allowMovingVehicle){
                    if (mMarker == null) {
                        mMarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .rotation(TempStorage.DRIVER.getLocation().getRotation())
                                .anchor(0.5f,0.5f)
                                .icon(AppExtensions.getMarkerIconFromDrawable(vehicleType, null)));
                    }
                    else {
                        MarkerAnimation.animateMarkerToICS(mMarker, new LatLng(location.getLatitude(), location.getLongitude()), new LatLngInterpolator.Spherical());
                        rotateMarker(mMarker, previousRotation, location.getBearing());

                        if(bounds != null) mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Constants.BOUNDS_PADDING));
                        else mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , Constants.DEFAULT_ZOOM));
                    }
                }

                com.go.driver.model.location.Location driverLocation = new com.go.driver.model.location.Location();
                driverLocation.setRotation(previousRotation);
                driverLocation.setBearing(location.getBearing());
                driverLocation.setCurLatitude(location.getLatitude());
                driverLocation.setCurLongitude(location.getLongitude());
                driverLocation.setLastLatitude(TempStorage.LAST_LOCATION == null? 0 : TempStorage.LAST_LOCATION.getLatitude());
                driverLocation.setLastLongitude(TempStorage.LAST_LOCATION == null? 0 : TempStorage.LAST_LOCATION.getLongitude());

                Driver tempDriver = new Driver(TempStorage.DRIVER, false);
                firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_LOCATION_KEY).setValue(driverLocation),
                        new FirebaseHelper.OnFirebaseUpdateListener() {
                            @Override
                            public void onSuccess() {
                                tempDriver.setLocation(driverLocation);
                                TempStorage.setDriverInfo(tempDriver, true);
                            }

                            @Override
                            public void onFailure() {}
                        });

                TempStorage.LAST_LOCATION = location;
                previousRotation = location.getBearing();

                Log.i(Constants.TAG, String.format("Your location was changed: %f / %f", location.getLatitude(), location.getLongitude()));
            }
        });
    }

    private void animateCamera(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng , Constants.DEFAULT_ZOOM));
    }

    private void rotateMarker(final Marker marker, final float fromRotation, final float toRotation) {
        try {
            if (toRotation == 0) {
                return;
            }

            if (!isMarkerRotating) {
                isMarkerRotating = true;

                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.addUpdateListener(animation -> {
                    int rotation = Math.round(computeRotation(animation.getAnimatedFraction(), fromRotation, toRotation));
                    marker.setRotation(rotation);
                });

                valueAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isMarkerRotating = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        isMarkerRotating = false;
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });

                valueAnimator.setDuration(1000);
                valueAnimator.start();
            }
        }
        catch (Exception ex){
            isMarkerRotating = false;
            ex.printStackTrace();
        }
    }

    private float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation;
        if (direction > 0) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360;
        }

        float result = fraction * rotation + start;
        return (result + 360) % 360;
    }

    private GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
        @Override
        public View getInfoWindow(Marker marker)   {
            if(marker.getTitle() == null || marker.getSnippet() == null) return null;

            View v = getLayoutInflater().inflate(R.layout.sample_info_window, null);

            AppCompatTextView   time = v.findViewById(R.id.time);
            AppCompatTextView   timeUnit = v.findViewById(R.id.timeUnit);
            AppCompatTextView   placeName = v.findViewById(R.id.placeName);

            placeName.setText(marker.getTitle());

            String[] t = new DateExtensions(HomeActivity.this, Long.parseLong(marker.getSnippet())).getTime();
            time.setText(t[0]); timeUnit.setText(t[1]);

            return v;
        }
        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    };

    private MarkerOptions buildMarkerOptions(LatLng point, Object title, String snippet, Integer icon, AppExtensions.Vehicle type){
        MarkerOptions options = new MarkerOptions();
        options.position(point);
        options.anchor(0.5f, 0.5f);
        if(title != null) options.title(title instanceof String ? (String)title : AppExtensions.getString((Integer)title));
        if(snippet != null) options.snippet(snippet);
        options.icon(AppExtensions.getMarkerIconFromDrawable(type == null ? AppExtensions.Vehicle.Other : type, icon));
        return options;
    }

    /**
     * Other
     **/
    private void getViewHeight(View view){
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    resizeMap(view.getHeight(), 16, true);
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    private void resizeMap(int height, int padding, boolean allowZooming){
        int size = height + padding;

        mMap.setPadding(0, 0, 0, size);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setMargins(0, 0, 32, size);
        snapBtn.setLayoutParams(params);

        if(!allowZooming) return;

        if(bounds != null) { mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Constants.BOUNDS_PADDING)); return; }

        if (!new PermissionManager(PermissionManager.Permission.LOCATION, false).isGranted()) return;
        else if(TempStorage.CURRENT_LOCATION == null) return;

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TempStorage.CURRENT_LAT_LNG , Constants.DEFAULT_ZOOM));
    }

    public void showNotificationDialog(){
        View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.layout_notification_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(false);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(alertDialog.getWindow()).getDecorView().setBackgroundColor(TRANSPARENT);
        alertDialog.setCancelable(false);
        alertDialog.show();

        final AppCompatCheckBox dontAskCheckbox = view.findViewById(R.id.dontAskCheckbox);
        final AppCompatButton forbidBtn = view.findViewById(R.id.forbidBtn);
        final AppCompatButton allowBtn = view.findViewById(R.id.allowBtn);

        dontAskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> new SharedPreference().setForbidNotificationPermission(isChecked));

        allowBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
            new PermissionManager().goToNotificationPermissionSetting();
        });

        forbidBtn.setOnClickListener(v -> alertDialog.dismiss());
    }

    @Override
    protected void onStop() {
        super.onStop();
        sp.setAppRunning(false);
        unregisterReceiver(networkStatusChangeReceiver);
        unregisterReceiver(gpsStatusChangeReceiver);
        if (onlinePreferenceReference != null && onlinePreferenceListener != null) onlinePreferenceReference.removeEventListener(onlinePreferenceListener);
        if (profileTripReference != null && profileTripListener != null) profileTripReference.removeEventListener(profileTripListener);
        if (notificationReference != null && notificationListener != null) notificationReference.removeEventListener(notificationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(mTripInfoReceiver);
        LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(mTokenReceiver);
        if (tripRequestReference != null && tripRequestListener != null) tripRequestReference.removeEventListener(tripRequestListener);
        stopLocationUpdates();
    }

    public interface OnNotificationListener {
        void onNotificationReceive(List<Notification> notifications);
    }

    public void setOnNotificationListener(OnNotificationListener mOnNotificationListener) {
        this.mOnNotificationListener = mOnNotificationListener;
    }

    public interface OnMessageListener {
        void onMessageReceive(HashMap<String, Message> messages);
    }

    public void setOnMessageListener(OnMessageListener mOnMessageListener) {
        this.mOnMessageListener = mOnMessageListener;
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public void setOnDismissListener(OnDismissListener mOnDismissListener) {
        this.mOnDismissListener = mOnDismissListener;
    }

    /**
     *  {@link NetworkStatusChangeReceiver} Monitor internet connection
     **/
    public void updateInternetConnectionStatus(boolean isConnected) {
        CustomSnackBar customSnackBar = new CustomSnackBar(drawerLayout, R.string.network_Error, R.string.retry, CustomSnackBar.Duration.INDEFINITE);

        if (isConnected) {
            customSnackBar.dismiss();
        }
        else {
            customSnackBar.show();
            customSnackBar.setOnDismissListener(snackbar -> {
                networkStatusChangeReceiver.onReceive(HomeActivity.this, null);
                snackbar.dismiss();
            });
        }
    }

    /**
     *  {@link GpsStatusChangeReceiver} Monitor Gps Service
     **/
    public void updateGpsStatus(boolean isEnabled) {
        if(!isEnabled) turnGPSOn();
        else displayLocation();
    }
}
