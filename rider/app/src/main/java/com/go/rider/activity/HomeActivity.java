package com.go.rider.activity;

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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.go.rider.R;
import com.go.rider.adapter.TripFareAdapter;
import com.go.rider.fragment.AboutFragment;
import com.go.rider.fragment.AlertDialogFragment;
import com.go.rider.fragment.ContactFragment;
import com.go.rider.fragment.DriverInfoFragment;
import com.go.rider.fragment.NotificationsFragment;
import com.go.rider.fragment.PlaceSelectionFragment;
import com.go.rider.fragment.ProfileFragment;
import com.go.rider.fragment.RatingFragment;
import com.go.rider.fragment.SettingsFragment;
import com.go.rider.fragment.TripsFragment;
import com.go.rider.model.location.Place;
import com.go.rider.model.notification.Notification;
import com.go.rider.model.other.ActiveStatus;
import com.go.rider.model.other.Rating;
import com.go.rider.model.trip.Message;
import com.go.rider.model.trip.TripInfo;
import com.go.rider.model.trip.TripStatus;
import com.go.rider.model.user.Driver;
import com.go.rider.model.user.Rider;
import com.go.rider.model.user.Trip;
import com.go.rider.model.vehicle.VehicleType;
import com.go.rider.receiver.GpsStatusChangeReceiver;
import com.go.rider.receiver.NetworkStatusChangeReceiver;
import com.go.rider.remote.FirebaseHelper;
import com.go.rider.remote.NotificationManager;
import com.go.rider.remote.PermissionManager;
import com.go.rider.service.GoLocationService;
import com.go.rider.service.GoService;
import com.go.rider.util.Constants;
import com.go.rider.util.CustomSnackBar;
import com.go.rider.util.extension.DateExtensions;
import com.go.rider.util.mapUtils.GpsUtils;
import com.go.rider.util.mapUtils.LatLngInterpolator;
import com.go.rider.util.mapUtils.MapDirection;
import com.go.rider.util.mapUtils.MarkerAnimation;
import com.go.rider.util.SharedPreference;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.AppExtensions;
import com.go.rider.wiget.CircleImageView;
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
import com.google.firebase.database.ChildEventListener;
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

import static android.graphics.Color.TRANSPARENT;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

@SuppressLint("SetTextI18n, MissingPermission, InflateParams")
public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    /**
     * Drawer
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
     * Firebase
     **/
    private DatabaseReference           onlinePreferenceReference;
    private ValueEventListener          onlinePreferenceListener;
    private DatabaseReference           profileTripReference;
    private ValueEventListener          profileTripListener;
    private DatabaseReference           vehicleReference;
    private ValueEventListener          vehicleListener;
    private DatabaseReference           notificationReference;
    private ValueEventListener          notificationListener;
    private DatabaseReference           tripRequestReference;
    private ValueEventListener          tripRequestListener;
    private DatabaseReference           availableDriversReference;
    private ChildEventListener          availableDriversListener;
    private DatabaseReference           driverReference;
    private ValueEventListener          driverListener;
    private List<Driver>                availableDrivers;
    private List<String>                availableKeys;

    /**
     * Map
     **/
    private GoogleMap                   mMap;
    private GoLocationService           gpsLocation;
    private boolean                     goingForPermission = false;
    private boolean                     isLocationUpdateStart = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback            mLocationCallback;
    private LocationRequest             mLocationRequest;
    private AppCompatImageButton        snapBtn;
    private MapDirection                mapDirection;
    private List<Marker>                markers;
    private Marker                      marker;

    /**
     * Trip Address Content
     **/
    private LinearLayoutCompat          tripAddress_Content;
    private BottomSheetBehavior         address_BottomSheet;
    private AppCompatTextView           greetingsTv;
    private AppCompatTextView           whereTo_Btn;
    private AppCompatTextView           homeAddress;
    private LinearLayoutCompat          homeAddress_Btn;
    private AppCompatTextView           workAddress;
    private LinearLayoutCompat          workAddress_Btn;
    private View                        divider;
    private ProgressBar                 loader;

    /**
     * Trip Fares Content
     **/
    private LinearLayoutCompat          tripFares_Content;
    private BottomSheetBehavior         tripFares_BottomSheet;
    private AppCompatImageButton        backBtn;
    private RecyclerView                rcvTripFares;
    private TripFareAdapter             tripFareAdapter;
    private AppCompatButton             confirm_Btn;
    private TripInfo                    tripInfo = null;
    private LatLngBounds                bounds = null;
    private AppExtensions.Vehicle       selectedVehicle = AppExtensions.Vehicle.Car;

    /**
     * Trip Request Content
     **/
    private FrameLayout                 findingTrips_Layout;
    private AppCompatTextView           findingTrips;
    private LinearLayoutCompat          tripRequest_Content;
    private BottomSheetBehavior         tripRequest_BottomSheet;
    private CircleImageView             driverPhoto;
    private AppCompatTextView           driverName;
    private AppCompatTextView           driverRating;
    private AppCompatTextView           estimateTime;
    private AppCompatTextView           estimateDistance;
    private AppCompatTextView           tripAddressTitle;
    private AppCompatTextView           tripAddress;
    private AppCompatButton             contactBtn;
    private AppCompatButton             cancelBtn;

    /**
     * Other
     **/
    private List<Notification>          notifications = null;
    private int                         height = 0;
    private boolean                     isSwappingBottomSheet = false;
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

        onlinePreference();

        checkRiderInfo();

        checkVehicleTypes();

        checkNotifications();

        registerReceiver(networkStatusChangeReceiver, new IntentFilter(CONNECTIVITY_ACTION));

        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        registerReceiver(gpsStatusChangeReceiver, filter);

        LocalBroadcastManager.getInstance(HomeActivity.this).registerReceiver(mTokenReceiver, new IntentFilter(Constants.TOKEN_LISTENER_KEY));

        if (!new PermissionManager(PermissionManager.Permission.LOCATION, false).isGranted()){ goingForPermission = true; return; }
        if (goingForPermission) { setLocation(); goingForPermission = false; }
    }

    private void init() {
        initId();                                             /** Initialize Id **/

        /**
         * Google Map Initialize
         **/
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        /**
         * Setup Profile Details
         **/
        TempStorage.setRiderInfo(null, false);
        setProfileInfo();

        setDrawer();                                          /** Navigation Drawer Setup **/

        checkRiderToken();                                    /** Check Rider Token Changed or not **/

        AppExtensions.initializePlaces();                     /** Google Places Initialize **/

        TempStorage.setLatLng(null);                          /** Initially Set Map LatLng **/

        TempStorage.setTripInfo(null, false);   /** Check Previous Trip Completed or not **/

        checkTripInfo();                                      /** Check Rider is on Trip or not **/

        greetingsTv.setText(AppExtensions.showGreeting());

        whereTo_Btn.setOnClickListener(v -> {
            if(!TempStorage.RIDER.getTrip().isVerified()){
                new CustomSnackBar(drawerLayout, R.string.notVerified, CustomSnackBar.Duration.LONG).show();
                return;
            }
            if (TempStorage.TRIP_INFO != null && TempStorage.RIDER.getTrip().isOnTrip()) { tripCancellationDialog(TempStorage.TRIP_INFO, false);  return; }
            PlaceSelectionFragment.show(HomeActivity.this, getMyCurrentPlace(), new Place()).setOnPlaceListener(this::showTripDirection);
        });

        setTripsAdapter();

        confirm_Btn.setOnClickListener(v -> {
            if(!Constants.IS_INTERNET_CONNECTED) return;
            if(!TempStorage.RIDER.getTrip().isVerified()){
                new CustomSnackBar(drawerLayout, R.string.notVerified, CustomSnackBar.Duration.LONG).show();
                return;
            }
            if (TempStorage.TRIP_INFO != null && TempStorage.RIDER.getTrip().isOnTrip()) { tripCancellationDialog(TempStorage.TRIP_INFO, false);  return; }
            fetchAvailableDrivers();
        });

        /**
         * Check Notification Allowed or not
         **/
        if (!sp.isNotificationPermissionForbidded() && !NotificationManagerCompat.from(this).areNotificationsEnabled()) showNotificationDialog();

        /**
         * Check Successfully Logged In or not
         **/
        if (!getIntent().hasExtra(Constants.SUCCESS_KEY)) return;
        if (!Objects.equals(getIntent().getStringExtra(Constants.SUCCESS_KEY), "SUCCESS")) return;

        sp.setRiderLoggedIn(true);
        getIntent().removeExtra(Constants.SUCCESS_KEY);
    }

    private void initId() {
        drawerLayout = findViewById(R.id.drawerLayout);
        contentView = findViewById(R.id.contentView);
        navigationView = findViewById(R.id.navigationView);

        navMenu = findViewById(R.id.menu_Btn);
        navProfile = findViewById(R.id.navProfile);
        navTrips = findViewById(R.id.navTrips);
        navNotification = findViewById(R.id.navNotification);
        navSettings = findViewById(R.id.navSettings);
        navAbout = findViewById(R.id.navAbout);

        profilePhoto = findViewById(R.id.profilePhoto);
        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);

        tripAddress_Content = findViewById(R.id.tripAddressContent);
        address_BottomSheet = BottomSheetBehavior.from(tripAddress_Content);
        greetingsTv = findViewById(R.id.greetingsTv);
        greetingsTv = findViewById(R.id.greetingsTv);
        whereTo_Btn = findViewById(R.id.whereTo_Btn);
        homeAddress = findViewById(R.id.homeAddress);
        homeAddress_Btn = findViewById(R.id.homeAddress_Btn);
        workAddress = findViewById(R.id.workAddress);
        workAddress_Btn = findViewById(R.id.workAddress_Btn);
        divider = findViewById(R.id.divider);
        snapBtn = findViewById(R.id.snap_Btn);
        loader = findViewById(R.id.loadingProgress);

        tripFares_Content = findViewById(R.id.tripFaresContent);
        tripFares_BottomSheet = BottomSheetBehavior.from(tripFares_Content);
        backBtn = findViewById(R.id.back_Btn);
        rcvTripFares = findViewById(R.id.rcvFares);
        confirm_Btn = findViewById(R.id.confirm_btn);

        findingTrips_Layout = findViewById(R.id.findingTripsLayout);
        findingTrips = findViewById(R.id.findingTrips);
        tripRequest_Content = findViewById(R.id.tripRequestContent);
        tripRequest_BottomSheet = BottomSheetBehavior.from(tripRequest_Content);
        driverPhoto = findViewById(R.id.driverPhoto);
        driverName = findViewById(R.id.driverName);
        driverRating = findViewById(R.id.driverRating);
        estimateTime = findViewById(R.id.estimateTime);
        estimateDistance = findViewById(R.id.estimateDistance);
        tripAddressTitle = findViewById(R.id.tripAddressTitle);
        tripAddress = findViewById(R.id.tripAddress);
        contactBtn = findViewById(R.id.contactBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
    }

    private void setDrawer() {
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
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        profilePhoto.setOnClickListener(v -> ProfileFragment.show(HomeActivity.this)
                .setOnProfileUpdateListener(isUpdated -> {
                    drawerLayout.openDrawer(GravityCompat.START);
                    if (isUpdated) setProfileInfo();
                })
        );

        navProfile.setOnClickListener(v -> ProfileFragment.show(HomeActivity.this)
                .setOnProfileUpdateListener(isUpdated -> {
                    drawerLayout.openDrawer(GravityCompat.START);
                    if (isUpdated) setProfileInfo();
                })
        );

        navTrips.setOnClickListener(v -> TripsFragment.show(HomeActivity.this));

        navNotification.setOnClickListener(v -> NotificationsFragment.show(HomeActivity.this, notifications));

        navSettings.setOnClickListener(v ->
                SettingsFragment.show(HomeActivity.this)
                        .setOnSettingsUpdateListener(isUpdated -> {
                            drawerLayout.openDrawer(GravityCompat.START);
                            if (isUpdated) {
                                setProfileInfo();
                                getViewHeight(tripAddress_Content, address_BottomSheet.getPeekHeight(), 16);
                            }
                        })
        );

        navAbout.setOnClickListener(v -> AboutFragment.show(HomeActivity.this));
    }

    private void checkRiderInfo() {
        profileTripReference = firebaseHelper.riderReference().child(FirebaseHelper.TRIP_KEY);

        profileTripListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;
                Trip trip = dataSnapshot.getValue(Trip.class);
                if (trip == null) return;
                TempStorage.RIDER.setTrip(trip);
                TempStorage.setRiderInfo(TempStorage.RIDER, true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        profileTripReference.addValueEventListener(profileTripListener);
    }

    private void onlinePreference() {
        onlinePreferenceReference = firebaseHelper.riderReference();

        onlinePreferenceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;
                onlinePreferenceReference.child(FirebaseHelper.USER_ACTIVE_KEY).setValue(new ActiveStatus(true, null));
                onlinePreferenceReference.child(FirebaseHelper.USER_ACTIVE_KEY).onDisconnect().setValue(new ActiveStatus(false, DateExtensions.currentTime()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        onlinePreferenceReference.addValueEventListener(onlinePreferenceListener);
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
                    if(notification.getUserType() != null && notification.getUserType() == 1) continue;
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

    private void checkRiderToken(){
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
        if (TempStorage.RIDER == null) return;
        Rider tempRider = new Rider(TempStorage.RIDER, false);

        if (tempRider.getToken() == null || !tempRider.getToken().equals(newToken)) {
            firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_TOKEN_KEY).setValue(newToken),
                    new FirebaseHelper.OnFirebaseUpdateListener() {
                        @Override
                        public void onSuccess() {
                            tempRider.setToken(newToken);
                            TempStorage.setRiderInfo(tempRider, true);
                        }

                        @Override
                        public void onFailure() {}
                    });
        }
    }

    private void setProfileInfo() {
        if (TempStorage.RIDER == null) return;

        int placeHolder = TempStorage.RIDER.getGender() == null || TempStorage.RIDER.getGender().equals(AppExtensions.Gender.Male.toString()) ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;
        AppExtensions.loadPhoto(profilePhoto, TempStorage.RIDER.getProfilePhoto(), R.dimen.icon_Size_XXXX_Large, placeHolder);

        userName.setText(TempStorage.RIDER.getName());
        userPhone.setText(TempStorage.RIDER.getPhone());

        Place homePlace = TempStorage.RIDER.getTrip().getPreference().getSavedPlaces().get(0);
        Place workPlace = TempStorage.RIDER.getTrip().getPreference().getSavedPlaces().get(1);

        homeAddress_Btn.setVisibility(homePlace.getAddress() != null ? View.VISIBLE : View.GONE);
        homeAddress.setText(homePlace.getAddress());

        divider.setVisibility(homePlace.getAddress() != null && workPlace.getAddress() != null ? View.VISIBLE : View.GONE);

        workAddress_Btn.setVisibility(workPlace.getAddress() != null ? View.VISIBLE : View.GONE);
        workAddress.setText(workPlace.getAddress());

        homeAddress_Btn.setOnClickListener(v -> {
            if(!Constants.IS_INTERNET_CONNECTED) return;
            if(!TempStorage.RIDER.getTrip().isVerified()){
                new CustomSnackBar(drawerLayout, R.string.notVerified, CustomSnackBar.Duration.LONG).show();
                return;
            }
            if (TempStorage.TRIP_INFO != null && TempStorage.RIDER.getTrip().isOnTrip()) { tripCancellationDialog(TempStorage.TRIP_INFO, false);  return; }
            showTripDirection(getMyCurrentPlace(), homePlace);
        });

        workAddress_Btn.setOnClickListener(v -> {
            if(!Constants.IS_INTERNET_CONNECTED) return;
            if(!TempStorage.RIDER.getTrip().isVerified()){
                new CustomSnackBar(drawerLayout, R.string.notVerified, CustomSnackBar.Duration.LONG).show();
                return;
            }
            if (TempStorage.TRIP_INFO != null && TempStorage.RIDER.getTrip().isOnTrip()) { tripCancellationDialog(TempStorage.TRIP_INFO, false);  return; }
            showTripDirection(getMyCurrentPlace(), workPlace);
        });
    }

    private void checkVehicleTypes() {
        vehicleReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.VEHICLE_TYPES_TABLE);

        vehicleListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) return;

                List<VehicleType> vehicleTypes = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    VehicleType vehicleType = snapshot.getValue(VehicleType.class);
                    if (vehicleType == null) continue;
                    vehicleTypes.add(vehicleType);
                }

                TempStorage.storeVehicleTypes(vehicleTypes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        vehicleReference.orderByChild(FirebaseHelper.VEHICLE_AVAILABILITY_KEY).equalTo(true).addValueEventListener(vehicleListener);
    }

    private void checkTripInfo(){
        if(TempStorage.TRIP_INFO == null && TempStorage.RIDER.getTrip().isOnTrip()) {
            firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
            TempStorage.RIDER.getTrip().setOnTrip(false);
            TempStorage.setRiderInfo(TempStorage.RIDER, true);
            return;
        }

        if(TempStorage.TRIP_INFO == null) {
            firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
            return;
        }

        if (!TempStorage.TRIP_INFO.getTripStatus().isTripCancelled() && TempStorage.TRIP_INFO.getRequestedDrivers() != null) {
            FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE).child(TempStorage.TRIP_INFO.getTripId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
                                TempStorage.setTripInfo(null, true);
                                TempStorage.RIDER.getTrip().setOnTrip(false);
                                TempStorage.setRiderInfo(TempStorage.RIDER, true);
                                return;
                            }

                            TripInfo tripInfo = dataSnapshot.getValue(TripInfo.class);

                            if(tripInfo == null || tripInfo.getTripStatus().isTripCancelled() || tripInfo.getTripStatus().isTripFinished()) {
                                firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
                                TempStorage.setTripInfo(null, true);
                                TempStorage.RIDER.getTrip().setOnTrip(false);
                                TempStorage.setRiderInfo(TempStorage.RIDER, true);
                                return;
                            }

                            tripCancellationDialog(tripInfo, false);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
        }
        else {
            firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false),null);
            TempStorage.setTripInfo(null, true);
            TempStorage.RIDER.getTrip().setOnTrip(false);
            TempStorage.setRiderInfo(TempStorage.RIDER, true);
        }
    }

    private void snapAllActiveDriversOnMap() {
        loader.setVisibility(View.VISIBLE);
        mMap.clear();
        availableDrivers = new ArrayList<>();
        availableKeys = new ArrayList<>();
        markers = new ArrayList<>();

        availableDriversReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.DRIVERS_TABLE);

        availableDriversListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                loader.setVisibility(View.INVISIBLE);

                String key = dataSnapshot.getKey();
                if(key == null) return;

                Driver driver = dataSnapshot.getValue(Driver.class);
                if(isConditionMatched(driver, TempStorage.CURRENT_LAT_LNG, selectedVehicle)) {
                    /**
                     * New Driver added
                     **/
                    availableDrivers.add(driver);
                    availableKeys.add(key);
                    showMarker(driver, null);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                loader.setVisibility(View.INVISIBLE);

                String key = dataSnapshot.getKey();
                if(key == null) return;

                Driver driver = dataSnapshot.getValue(Driver.class);

                if(isConditionMatched(driver, TempStorage.CURRENT_LAT_LNG, selectedVehicle)) {
                    if(!availableKeys.contains(key)){
                        /**
                         * Driver Joined Again
                         **/
                        availableDrivers.add(driver);
                        availableKeys.add(key);
                        showMarker(driver, null);
                    }

                    /**
                     * Driver Changed Location
                     **/
                    int pos = availableKeys.indexOf(key);
                    availableDrivers.set(pos, driver);
                    availableKeys.set(pos, key);
                    showMarker(driver, markers.get(pos));
                }
                else {
                    /**
                     * Driver unavailable or far from rider
                     **/
                    if(!availableKeys.contains(key)) return;
                    int pos = availableKeys.indexOf(key);
                    availableDrivers.remove(pos);
                    availableKeys.remove(pos);
                    markers.get(pos).remove();
                    markers.remove(pos);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                loader.setVisibility(View.INVISIBLE);

                String key = dataSnapshot.getKey();
                if(key == null) return;

                /**
                 * Driver Gone
                 **/
                if(!availableKeys.contains(key)) return;
                int pos = availableKeys.indexOf(key);
                availableDrivers.remove(pos);
                availableKeys.remove(pos);
                markers.get(pos).remove();
                markers.remove(pos);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                loader.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loader.setVisibility(View.INVISIBLE);
            }
        };

        availableDriversReference.addChildEventListener(availableDriversListener);
    }

    private void showTripAddress() {
        mMap.setInfoWindowAdapter(null);
        mMap.setOnInfoWindowClickListener(null);

        this.bounds = null;
        backBtn.setVisibility(View.GONE);
        navMenu.setVisibility(View.VISIBLE);

        handleBottomSheet(tripFares_BottomSheet, true, false);
        handleBottomSheet(tripRequest_BottomSheet, true, false);
        handleBottomSheet(address_BottomSheet, false, true);
        getViewHeight(tripAddress_Content, address_BottomSheet.getPeekHeight(), 16);

        address_BottomSheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {}

            @Override
            public void onSlide(@NonNull View view, float v) {
                if (isSwappingBottomSheet) return;
                resizeMap(address_BottomSheet.getPeekHeight(), 24, v);

                if (v == 0 || v == 1) {
                    if (!new PermissionManager(PermissionManager.Permission.LOCATION, false).isGranted()) return;
                    else if (TempStorage.CURRENT_LOCATION == null) return;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TempStorage.CURRENT_LAT_LNG, Constants.DEFAULT_ZOOM));
                }
            }
        });
    }

    private void showTripFares() {
        navMenu.setVisibility(View.GONE);
        backBtn.setVisibility(View.VISIBLE);

        handleBottomSheet(address_BottomSheet, true, true);
        handleBottomSheet(tripFares_BottomSheet, false, false);
        getViewHeight(tripFares_Content, 0, 16);

        backBtn.setOnClickListener(v -> { showTripAddress(); snapAllActiveDriversOnMap(); });
    }

    private void setTripsAdapter() {
        rcvTripFares.setLayoutManager(new LinearLayoutManager(HomeActivity.this, RecyclerView.VERTICAL, false));
        tripFareAdapter = new TripFareAdapter(HomeActivity.this);
        rcvTripFares.setAdapter(tripFareAdapter);
        tripFareAdapter.setOnTypeSelectListener(info -> HomeActivity.this.tripInfo = info);
    }

    private void fetchAvailableDrivers(){
        FirebaseDatabase.getInstance().getReference(FirebaseHelper.DRIVERS_TABLE)
                .orderByChild(FirebaseHelper.USER_AVAILABILITY_KEY)
                .equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        HashMap<String, String> availableDrivers = new HashMap<>();
                        List<String> availableTokens = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Driver driver = snapshot.getValue(Driver.class);
                            if (!isConditionMatched(driver,
                                    new LatLng(HomeActivity.this.tripInfo.getPickUpLocation().getLatitude(), HomeActivity.this.tripInfo.getPickUpLocation().getLongitude()),
                                    AppExtensions.Vehicle.valueOf(HomeActivity.this.tripInfo.getVehicleType().getType()))) continue;
                            if(driver.getTrip().isOnTrip()) continue;
                            availableDrivers.put(driver.getId(), driver.getToken());
                            if(!availableTokens.contains(driver.getToken())) availableTokens.add(driver.getToken());
                        }

                        if(!availableDrivers.isEmpty()){
                            HomeActivity.this.tripInfo.setRequestedDrivers(availableDrivers);
                            sentTripRequest(availableTokens);
                        }
                        else {
                            new CustomSnackBar(drawerLayout, R.string.noDriverAvailable, CustomSnackBar.Duration.LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        new CustomSnackBar(drawerLayout, R.string.noDriverAvailable, CustomSnackBar.Duration.LONG).show();
                        databaseError.toException().printStackTrace();
                    }
                });
    }

    private void sentTripRequest(List<String> driverTokens){

        firebaseHelper.setData(FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE)
                        .child(this.tripInfo.getTripId()).setValue(this.tripInfo),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        if(driverTokens.size() != 0) {
                            String data = new Gson().toJson(HomeActivity.this.tripInfo);
                            new NotificationManager(driverTokens, "Trip Request",
                                    AppExtensions.nameFormat(TempStorage.RIDER.getName()) + " sent you a trip request! Please check it out.",
                                    data).send();
                        }

                        trackCurrentTrip(HomeActivity.this.tripInfo.getTripId());
                    }

                    @Override
                    public void onFailure() {
                        updateUI();
                        new CustomSnackBar(drawerLayout, R.string.noDriverAvailable, CustomSnackBar.Duration.LONG).show();
                    }
                });

    }

    private void trackCurrentTrip(String tripId){
        tripRequestReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE).child(tripId);

        tripRequestListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    new CustomSnackBar(drawerLayout, R.string.noDriverAvailable, CustomSnackBar.Duration.LONG).show();
                    updateUI(); return;
                }

                TripInfo tripInfo = dataSnapshot.getValue(TripInfo.class);

                if(tripInfo == null) {
                    new CustomSnackBar(drawerLayout, R.string.noDriverAvailable, CustomSnackBar.Duration.LONG).show();
                    updateUI(); return;
                }

                /**
                 * Check trip cancelled or not
                 * if cancelled, back to home otherwise proceed
                 **/
                if(tripInfo.getTripStatus().isTripCancelled()) {
                    new CustomSnackBar(drawerLayout, R.string.rideCancelled).show();
                    firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
                    updateUI(); return;
                }

                /**
                 * Check all drivers cancelled trip or not
                 * if cancelled, back to home otherwise proceed
                 **/
                if(tripInfo.getRequestedDrivers() == null){
                    new CustomSnackBar(drawerLayout, R.string.noDriverAvailable, CustomSnackBar.Duration.LONG).show();
                    updateUI(); return;
                }

                /**
                 * Check trip cancelled or not
                 * if cancelled, give rating & chill otherwise proceed
                 **/
                if(tripInfo.getTripStatus().isTripFinished()){
                    RatingFragment.show(HomeActivity.this, tripInfo);
                    updateUI(); return;
                }

                /**
                 * Check trip not accepted or not
                 * if accepted then show trip direction(pickup to dropoff point)
                 **/
                if (tripInfo.getTripStatus().isTripAccepted()) {
                    if (tripInfo.getMessages() != null && mOnMessageListener != null) mOnMessageListener.onMessageReceive(tripInfo.getMessages());
                    contactBtn.setText((tripInfo.getMessages() != null && TempStorage.TRIP_INFO != null && TempStorage.TRIP_INFO.getMessages() == null)
                            ||
                            (tripInfo.getMessages() != null && TempStorage.TRIP_INFO != null && TempStorage.TRIP_INFO.getMessages() != null
                                    &&
                                    tripInfo.getMessages().size() != TempStorage.TRIP_INFO.getMessages().size())
                            ?
                            AppExtensions.getHtmlString(R.string.newMessage)
                            :
                            AppExtensions.getString(R.string.contact));

                    contactBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Contact));
                    driverPhoto.setOnClickListener(v -> DriverInfoFragment.show(HomeActivity.this, tripInfo.getDriver(), tripInfo));

                    if (TempStorage.TRIP_INFO != null && TempStorage.TRIP_INFO.getTripStatus().isTripAccepted()) {
                        if (tripInfo.getTripStatus().isTripStarted()) {
                            if (!TempStorage.TRIP_INFO.getTripStatus().isTripStarted()) {
                                showTripDirection(tripInfo);
                            }
                        }

                        TempStorage.setTripInfo(tripInfo, true);
                    }
                    else {
                        if (tripInfo.getDriver() != null) {
                            findingTrips_Layout.setVisibility(View.GONE);
                            findingTrips.setOnClickListener(null);
                            showDriverDirection(tripInfo);
                            TempStorage.RIDER.getTrip().setOnTrip(true);
                            TempStorage.setRiderInfo(TempStorage.RIDER, true);
                            TempStorage.setTripInfo(tripInfo, true);
                        }
                        else {
                            if (tripInfo.getRequestedDrivers() == null || tripInfo.getRequestedDrivers().isEmpty()) {
                                new CustomSnackBar(drawerLayout, R.string.noDriverAvailable, CustomSnackBar.Duration.LONG).show();
                                updateUI();
                            }
                            else {
                                if(findingTrips_Layout.getVisibility() == View.GONE){
                                    if(mOnDismissListener != null) mOnDismissListener.onDismiss();
                                    if(tripFares_BottomSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) handleBottomSheet(tripFares_BottomSheet, true, false);
                                    if(tripRequest_BottomSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) handleBottomSheet(tripRequest_BottomSheet, true, false);
                                    findingTrips_Layout.setVisibility(View.VISIBLE);
                                    findingTrips.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Finding));
                                    if (driverReference != null && driverListener != null) driverReference.removeEventListener(driverListener);
                                    TempStorage.RIDER.getTrip().setOnTrip(false);
                                    TempStorage.setRiderInfo(TempStorage.RIDER, true);
                                    TempStorage.setTripInfo(tripInfo, true);
                                    height = 0; resizeMap(0, 32, 0);
                                }
                            }
                        }
                    }
                }
                else {
                    if(findingTrips_Layout.getVisibility() == View.GONE){
                        if(mOnDismissListener != null) mOnDismissListener.onDismiss();
                        if(tripFares_BottomSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) handleBottomSheet(tripFares_BottomSheet, true, false);
                        if(tripRequest_BottomSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) handleBottomSheet(tripRequest_BottomSheet, true, false);
                        findingTrips_Layout.setVisibility(View.VISIBLE);
                        findingTrips.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Finding));
                        if (driverReference != null && driverListener != null) driverReference.removeEventListener(driverListener);
                        TempStorage.RIDER.getTrip().setOnTrip(false);
                        TempStorage.setRiderInfo(TempStorage.RIDER, true);
                        TempStorage.setTripInfo(tripInfo, true);
                        height = 0; resizeMap(0, 32, 0);
                    }
                    else {
                        new CustomSnackBar(drawerLayout, R.string.noDriverAvailable, CustomSnackBar.Duration.LONG).show();
                        updateUI();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        tripRequestReference.addValueEventListener(tripRequestListener);
    }

    private boolean isConditionMatched(Driver driver, LatLng pickUpPoint, AppExtensions.Vehicle vehicle){
        if (driver == null) return false;
        else if (!driver.getTrip().getPreference().isAvailable()) return false;
        else if(vehicle != AppExtensions.Vehicle.valueOf(driver.getVehicleInfo().getVehicleType().getType())) return false;
        else if (driver.getLocation().getCurLatitude() == 0 || driver.getLocation().getCurLongitude() == 0) return false;
        else return AppExtensions.nearByRider(1, pickUpPoint, new LatLng(driver.getLocation().getCurLatitude(), driver.getLocation().getCurLongitude()));
    }

    private void showTripDirection(final Place pickUp, final Place dropOff) {
        final LatLng pickUpPoint =  new LatLng(pickUp.getLatitude(), pickUp.getLongitude());
        final LatLng dropOffPoint =  new LatLng(dropOff.getLatitude(), dropOff.getLongitude());

        loader.setVisibility(View.VISIBLE);
        if(mapDirection != null && mapDirection.isAnimatorPlaying()) mapDirection.stop();

        mapDirection = new MapDirection(mMap, pickUpPoint, dropOffPoint, new MapDirection.OnDirectionListener() {
            @Override
            public void onSuccess() {
                loader.setVisibility(View.INVISIBLE);
                if (availableDriversReference != null && availableDriversListener != null) availableDriversReference.removeEventListener(availableDriversListener);
                mMap.clear();
            }

            @Override
            public void onData(double distance, long time, LatLngBounds bounds) {
                HomeActivity.this.bounds = bounds;
                long currentTime = DateExtensions.currentTime();

                HomeActivity.this.tripInfo = new TripInfo();
                HomeActivity.this.tripInfo.setTripId(AppExtensions.getRandomCode(8));
                HomeActivity.this.tripInfo.setDriver(null);
                HomeActivity.this.tripInfo.setRider(new Rider(TempStorage.RIDER, true));
                HomeActivity.this.tripInfo.setPickUpLocation(pickUp);
                HomeActivity.this.tripInfo.setPickUpTime(currentTime);
                HomeActivity.this.tripInfo.setDropOffLocation(dropOff);
                HomeActivity.this.tripInfo.setDropOffTime(currentTime + time);
                HomeActivity.this.tripInfo.setEstimatedDistance(distance);
                HomeActivity.this.tripInfo.setEstimatedTime(time);
                HomeActivity.this.tripInfo.setRiderRating(null);
                HomeActivity.this.tripInfo.setDriverRating(null);
                HomeActivity.this.tripInfo.setPaymentType(AppExtensions.Payment.Cash.toString());
                HomeActivity.this.tripInfo.setTripStatus(new TripStatus(false, false, false, false));
                HomeActivity.this.tripInfo.setMessages(null);

                tripFareAdapter.setTypes(TempStorage.getVehicleTypes(), HomeActivity.this.tripInfo);
                mMap.setInfoWindowAdapter(infoWindowAdapter);
                mMap.setOnInfoWindowClickListener(marker -> {
                    PlaceSelectionFragment.show(HomeActivity.this, pickUp, dropOff)
                            .setOnPlaceListener((from, to) -> {
                                tripFareAdapter.clearAllTypes();
                                showTripDirection(from, to);
                            });
                });

                mMap.addMarker(buildMarkerOptions(pickUpPoint, HomeActivity.this.tripInfo.getPickUpLocation().getTitle() != null
                        ?
                        HomeActivity.this.tripInfo.getPickUpLocation().getTitle()
                        :
                        R.string.unknownLocation, String.valueOf(time), R.drawable.ic_marker_from, null
                ));

                mMap.addMarker(buildMarkerOptions(dropOffPoint, HomeActivity.this.tripInfo.getDropOffLocation().getTitle() != null
                        ?
                        HomeActivity.this.tripInfo.getDropOffLocation().getTitle()
                        :
                        R.string.unknownLocation, String.valueOf(time), R.drawable.ic_marker_to, null
                )).showInfoWindow();

                showTripFares();
            }

            @Override
            public void onFailure() {
                loader.setVisibility(View.INVISIBLE);
            }
        });

        mapDirection.show();
    }

    private void showDriverDirection(TripInfo tripInfo) {
        if(tripInfo == null) return;
        final LatLng driverPoint = new LatLng(tripInfo.getDriver().getLocation().getCurLatitude(), tripInfo.getDriver().getLocation().getCurLongitude());
        final LatLng pickUpPoint = new LatLng(tripInfo.getPickUpLocation().getLatitude(), tripInfo.getPickUpLocation().getLongitude());

        if(mapDirection != null && mapDirection.isAnimatorPlaying()) mapDirection.stop();

        mapDirection = new MapDirection(mMap, driverPoint, pickUpPoint, new MapDirection.OnDirectionListener() {
            @Override
            public void onSuccess() {
                if (availableDriversReference != null && availableDriversListener != null) availableDriversReference.removeEventListener(availableDriversListener);
                mMap.clear();
                marker = null;
                trackDriver(tripInfo);
            }

            @Override
            public void onData(double distance, long time, LatLngBounds bounds) {
                HomeActivity.this.bounds = bounds;
                showRequestInfo(tripInfo, "driver", distance, time);

                mMap.setInfoWindowAdapter(infoWindowAdapter);
                mMap.setOnInfoWindowClickListener(null);

                mMap.addMarker(buildMarkerOptions(driverPoint, R.string.driver_Location, String.valueOf(time), R.drawable.ic_marker_from, null)).showInfoWindow();
                mMap.addMarker(buildMarkerOptions(pickUpPoint, R.string.my_Location, String.valueOf(time), R.drawable.ic_marker_to, null));
            }

            @Override
            public void onFailure() {}
        });

        mapDirection.show();
    }

    private void showTripDirection(TripInfo tripInfo) {
        if(tripInfo == null) return;
        if(TempStorage.CURRENT_LAT_LNG.latitude == 0 || TempStorage.CURRENT_LAT_LNG.longitude == 0) return;
        final LatLng pickUpPoint =  new LatLng(tripInfo.getPickUpLocation().getLatitude(), tripInfo.getPickUpLocation().getLongitude());
        final LatLng dropOffPoint =  new LatLng(tripInfo.getDropOffLocation().getLatitude(), tripInfo.getDropOffLocation().getLongitude());

        if(mapDirection != null && mapDirection.isAnimatorPlaying()) mapDirection.stop();

        mapDirection = new MapDirection(mMap, pickUpPoint, dropOffPoint, new MapDirection.OnDirectionListener() {
            @Override
            public void onSuccess() {
                if (availableDriversReference != null && availableDriversListener != null) availableDriversReference.removeEventListener(availableDriversListener);
                mMap.clear();
                marker = null;
                trackDriver(tripInfo);
            }

            @Override
            public void onData(double distance, long time, LatLngBounds bounds) {
                HomeActivity.this.bounds = bounds;

                if(tripRequest_BottomSheet.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    showRequestInfo(tripInfo, "dropoff", distance, time);
                }
                else {
                    String[] t = new DateExtensions(HomeActivity.this, time).getTime();
                    estimateTime.setText(t[0] + " " + t[1]);
                    estimateDistance.setText(AppExtensions.decimalFormat(distance/1000, "#.#", false)
                            + " " + AppExtensions.getString(R.string.km));
                    tripAddressTitle.setText(AppExtensions.getString(R.string.dropOff_Location));
                    tripAddress.setText(tripInfo.getDropOffLocation().getAddress());

                    getViewHeight(tripRequest_Content, 0, 16);
                }

                mMap.setInfoWindowAdapter(infoWindowAdapter);
                mMap.setOnInfoWindowClickListener(null);

                mMap.addMarker(buildMarkerOptions(pickUpPoint, R.string.pickUp, String.valueOf(time), R.drawable.ic_marker_from, null));
                mMap.addMarker(buildMarkerOptions(dropOffPoint, R.string.dropOff, String.valueOf(time), R.drawable.ic_marker_to, null)).showInfoWindow();
            }

            @Override
            public void onFailure() {

            }
        });

        mapDirection.show();
    }

    private void showRequestInfo(TripInfo tripInfo, String addressType, double distance, long time) {
        if(tripInfo == null) return;
        handleBottomSheet(tripFares_BottomSheet, true, false);
        handleBottomSheet(tripRequest_BottomSheet, false, false);

        int placeHolder = tripInfo.getDriver().getGender() == null ||  tripInfo.getDriver().getGender().equals(AppExtensions.Gender.Male.toString())
                ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;

        AppExtensions.loadPhoto(driverPhoto, tripInfo.getDriver().getProfilePhoto(), R.dimen.icon_Size_XX_Large, placeHolder);

        driverName.setText( tripInfo.getDriver().getName());

        if (tripInfo.getDriver().getTrip().getRatings() != null) {
            int ratingCount = 0; double totalRating = 0;

            for (Map.Entry<String, Rating> entry :  tripInfo.getDriver().getTrip().getRatings().entrySet()) {
                ratingCount++;
                Rating rating = entry.getValue();
                totalRating = totalRating + rating.getRating();
            }

            if(ratingCount == 0 || totalRating == 0) driverRating.setText(AppExtensions.decimalFormat(0, "0.0", true));
            else driverRating.setText(AppExtensions.decimalFormat(totalRating / ratingCount, "0.0", true));
        }
        else driverRating.setText(AppExtensions.decimalFormat(0, "0.0", true));

        String[] t = new DateExtensions(HomeActivity.this, time).getTime();
        this.estimateTime.setText(t[0] + " " + t[1]);

        this.estimateDistance.setText(AppExtensions.decimalFormat(distance / 1000, "#.#", false) + " " + AppExtensions.getString(R.string.kmAway));

        switch (addressType){
            case "driver":
                tripAddressTitle.setText(AppExtensions.getString(R.string.driver_Location));
                tripAddress.setText(AppExtensions.getAddress(tripInfo.getDriver().getLocation().getCurLatitude(),  tripInfo.getDriver().getLocation().getCurLongitude()));
                break;

            case "dropoff":
                tripAddressTitle.setText(AppExtensions.getString(R.string.dropOff_Location));
                tripAddress.setText(tripInfo.getDropOffLocation().getAddress());
                break;
        }

        getViewHeight(tripRequest_Content, 0, 16);

        backBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Cancel));
        cancelBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Cancel));
    }

    private void trackDriver(TripInfo tripInfo){
        if(tripInfo == null || tripInfo.getDriver() == null) return;
        driverReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.DRIVERS_TABLE).child(tripInfo.getDriver().getId());

        driverListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) return;
                Driver driver = dataSnapshot.getValue(Driver.class);
                if(driver == null) return;
                showMarker(driver);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        driverReference.addValueEventListener(driverListener);
    }

    private void tripCancellationDialog(TripInfo tripInfo, boolean tripIsRunning) {
        if(tripInfo == null) return;

        AlertDialogFragment cancellationDialog = AlertDialogFragment.show(HomeActivity.this,
                R.string.cancelRide, R.string.cancelRideMessage,
                R.string.yesCancel, tripIsRunning ? R.string.no : R.string.goOn);

        cancellationDialog.setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
            @Override
            public void onLeftButtonClick() {
                firebaseHelper.setData(FirebaseDatabase.getInstance()
                        .getReference(FirebaseHelper.TRIP_REQUESTS_TABLE)
                        .child(tripInfo.getTripId())
                        .child(FirebaseHelper.TRIP_CANCELLED_KEY)
                        .setValue(true), null);

                firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false),
                        new FirebaseHelper.OnFirebaseUpdateListener() {
                            @Override
                            public void onSuccess() {
                                TempStorage.RIDER.getTrip().setOnTrip(false);
                                TempStorage.setRiderInfo( TempStorage.RIDER, true);
                                TempStorage.setTripInfo(null, true);
                            }

                            @Override
                            public void onFailure() {
                                new CustomSnackBar(drawerLayout, R.string.failureMessage, R.string.retry).show();
                                tripCancellationDialog(tripInfo, tripIsRunning);
                            }
                        });
            }

            @Override
            public void onRightButtonClick() {
                if (!tripIsRunning) {
                    if (tripInfo.getTripStatus().isTripAccepted() && !tripInfo.getTripStatus().isTripStarted()) {
                        if (tripInfo.getDriver() != null) {
                            navMenu.setVisibility(View.GONE);
                            backBtn.setVisibility(View.VISIBLE);
                            handleBottomSheet(address_BottomSheet, true, true);
                            contactBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Contact));
                            showDriverDirection(tripInfo);
                            trackCurrentTrip(tripInfo.getTripId());
                        }
                        else {
                            if (tripInfo.getRequestedDrivers() == null || tripInfo.getRequestedDrivers().size() == 0) {
                                new CustomSnackBar(drawerLayout, R.string.noDriverAvailable, CustomSnackBar.Duration.LONG).show();
                                firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_ON_TRIP_KEY).setValue(false), null);
                            }
                            else {
                                navMenu.setVisibility(View.GONE);
                                backBtn.setVisibility(View.VISIBLE);
                                handleBottomSheet(address_BottomSheet, true, true);
                                findingTrips_Layout.setVisibility(View.VISIBLE);
                                findingTrips.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Finding));
                                TempStorage.RIDER.getTrip().setOnTrip(false);
                                TempStorage.setRiderInfo(TempStorage.RIDER, true);
                                TempStorage.setTripInfo(tripInfo, true);
                                height = 0; resizeMap(0, 32, 0);
                                trackCurrentTrip(tripInfo.getTripId());
                            }
                        }
                    }
                    else if (tripInfo.getTripStatus().isTripStarted()) {
                        navMenu.setVisibility(View.GONE);
                        backBtn.setVisibility(View.VISIBLE);
                        handleBottomSheet(address_BottomSheet, true, true);
                        contactBtn.setOnClickListener(new TripListener(tripInfo, AppExtensions.TripRequest.Contact));
                        showTripDirection(tripInfo);
                        trackCurrentTrip(tripInfo.getTripId());
                    }
                    else {
                        trackCurrentTrip(tripInfo.getTripId());
                    }
                }
            }
        });

        cancellationDialog.setCancelable(tripIsRunning);
    }

    private Place getMyCurrentPlace() {
        if (!new PermissionManager(PermissionManager.Permission.LOCATION, false).isGranted() || !Constants.IS_GPS_ENABLED) return new Place();
        Location gpsLocation = this.gpsLocation.getLocation(LocationManager.GPS_PROVIDER);

        double latitude = gpsLocation != null ? gpsLocation.getLatitude() : TempStorage.CURRENT_LOCATION.getLatitude();
        double longitude = gpsLocation != null ? gpsLocation.getLongitude() : TempStorage.CURRENT_LOCATION.getLongitude();

        Place myLocation = new Place();
        myLocation.setTitle(AppExtensions.getString(R.string.my_Location));
        myLocation.setLatitude(latitude);
        myLocation.setLongitude(longitude);
        myLocation.setAddress(AppExtensions.getAddress(latitude, longitude));
        return myLocation;
    }

    public class TripListener implements View.OnClickListener {

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
            switch (action){
                case Finding:
                case Cancel:
                    tripCancellationDialog(tripInfo, isRunning);
                    break;

                case Contact:
                    ContactFragment.show(HomeActivity.this, tripInfo)
                            .setOnContactListener(() -> contactBtn.setText(AppExtensions.getString(R.string.contact)));
                    break;
            }
        }
    }

    public void handleBottomSheet(BottomSheetBehavior behavior, boolean doHide, boolean isSwapping){
        this.isSwappingBottomSheet = isSwapping;

        if(doHide){
            behavior.setHideable(true);
            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        else {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            new Handler().postDelayed(() -> { behavior.setHideable(false); isSwappingBottomSheet = false; }, 500);
        }
    }

    private void updateUI() {
        if(mOnDismissListener != null) mOnDismissListener.onDismiss();
        if (tripRequestReference != null && tripRequestListener != null) tripRequestReference.removeEventListener(tripRequestListener);
        if (driverReference != null && driverListener != null) driverReference.removeEventListener(driverListener);
        if(mapDirection != null) { mapDirection.cancel(); mapDirection = null; }
        if(findingTrips_Layout.getVisibility() == View.VISIBLE) {
            findingTrips_Layout.setVisibility(View.GONE);
            findingTrips.setOnClickListener(null);
        }
        TempStorage.setTripInfo(null, true);
        TempStorage.RIDER.getTrip().setOnTrip(false);
        TempStorage.setRiderInfo(TempStorage.RIDER, true);
        this.tripInfo = null;
        this.marker = null;
        this.markers.clear();
        this.tripFareAdapter.clearAllTypes();
        showTripAddress();
        snapAllActiveDriversOnMap();
    }

    private BroadcastReceiver mTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String token = intent.getStringExtra(Constants.TOKEN_INTENT_KEY);
            updateToken(token);
            intent.removeExtra(Constants.TOKEN_INTENT_KEY);
        }
    };

    /**
     * Google Map
     **/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        AppExtensions.setMapStyle(googleMap);
        mMap = googleMap;
        AppExtensions.setMapSettings(mMap);

        showTripAddress();

        if (!new PermissionManager(PermissionManager.Permission.LOCATION, true, response -> setLocation() ).isGranted()) return;
        setLocation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.GPS_PERMISSION_CODE) {
                Constants.IS_GPS_ENABLED = true;
            } else if (requestCode == Constants.PLACES_REQUEST_CODE) {
                com.google.android.libraries.places.api.model.Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();
                if (latLng == null) return;
                if(bounds != null) mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Constants.BOUNDS_PADDING));
                else mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , Constants.DEFAULT_ZOOM));
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.e(Constants.TAG, Objects.requireNonNull(status.getStatusMessage()));
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(HomeActivity.this);
        if (ConnectionResult.SUCCESS == status) return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status)) {
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

    private void turnGPSOn() {
        new GpsUtils(HomeActivity.this).turnGPSOn(isGPSEnable -> {
            /**
             * turn on GPS
             **/
            Constants.IS_GPS_ENABLED = isGPSEnable;
        });
    }

    private void setLocation() {
        if (!isGooglePlayServicesAvailable()) return;
        if (!Constants.IS_GPS_ENABLED) turnGPSOn();

        mMap.setMyLocationEnabled(true);
        gpsLocation = new GoLocationService(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeActivity.this);
        startLocationUpdates();

        buildLocationRequest();
        buildLocationCallback();
        displayLocation();
        snapAllActiveDriversOnMap();

        snapBtn.setOnClickListener(v -> {
            if (bounds != null) { mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Constants.BOUNDS_PADDING)); return; }
            if (!new PermissionManager(PermissionManager.Permission.LOCATION, true, response -> {
                if(TempStorage.CURRENT_LOCATION == null) return;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TempStorage.CURRENT_LAT_LNG, Constants.DEFAULT_ZOOM));
            }).isGranted()) return;
            else if (TempStorage.CURRENT_LOCATION == null) return;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TempStorage.CURRENT_LAT_LNG, Constants.DEFAULT_ZOOM));
        });
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
            TempStorage.CURRENT_LOCATION = location;

            if (TempStorage.CURRENT_LOCATION != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                TempStorage.setLatLng(latLng);

                if (!isLocationUpdateStart) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.DEFAULT_ZOOM));
                    isLocationUpdateStart = true;
                }

                com.go.rider.model.location.Location riderLocation = new com.go.rider.model.location.Location();
                riderLocation.setRotation(location.getBearing());
                riderLocation.setBearing(location.getBearing());
                riderLocation.setCurLatitude(location.getLatitude());
                riderLocation.setCurLongitude(location.getLongitude());
                riderLocation.setLastLatitude(TempStorage.LAST_LOCATION == null ? 0 : TempStorage.LAST_LOCATION.getLatitude());
                riderLocation.setLastLongitude(TempStorage.LAST_LOCATION == null ? 0 : TempStorage.LAST_LOCATION.getLongitude());

                firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_LOCATION_KEY).setValue(riderLocation), null);

                TempStorage.LAST_LOCATION = location;

                Log.e(Constants.TAG, String.format("Your location was changed: %f / %f", location.getLatitude(), location.getLongitude()));
            }
        });
    }

    private void showMarker(Driver driver, Marker mMarker) {
        AppExtensions.Vehicle vehicleType = AppExtensions.Vehicle.valueOf(driver.getVehicleInfo().getVehicleType().getType());

        if(mMarker == null){
            mMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(driver.getLocation().getCurLatitude(), driver.getLocation().getCurLongitude()))
                    .rotation(driver.getLocation().getRotation())
                    .anchor(0.5f,0.5f)
                    .icon(AppExtensions.getMarkerIconFromDrawable(vehicleType, null))
            );
            markers.add(mMarker);
        }
        else {
            MarkerAnimation.animateMarkerToICS(mMarker,
                    new LatLng(driver.getLocation().getCurLatitude(), driver.getLocation().getCurLongitude()),
                    new LatLngInterpolator.Spherical());
            rotateMarker(mMarker, driver.getLocation().getRotation(), driver.getLocation().getBearing());
        }
    }

    private void showMarker(Driver driver) {
        AppExtensions.Vehicle vehicleType = AppExtensions.Vehicle.valueOf(driver.getVehicleInfo().getVehicleType().getType());

        if(this.marker == null){
            this.marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(driver.getLocation().getCurLatitude(), driver.getLocation().getCurLongitude()))
                    .rotation(driver.getLocation().getRotation())
                    .anchor(0.5f,0.5f)
                    .icon(AppExtensions.getMarkerIconFromDrawable(vehicleType, null))
            );
        }
        else {
            MarkerAnimation.animateMarkerToICS(this.marker,
                    new LatLng(driver.getLocation().getCurLatitude(), driver.getLocation().getCurLongitude()),
                    new LatLngInterpolator.Spherical());
            rotateMarker(this.marker, driver.getLocation().getRotation(), driver.getLocation().getBearing());
        }
    }

    private void rotateMarker(final Marker marker, final float fromRotation, final float toRotation) {
        try {
            /**
             * NOTE: If this location does not have a bearing then 0.0 is returned.
             **/
            if (toRotation == 0) {
                return;
            }

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.addUpdateListener(animation -> {
                int rotation = Math.round(computeRotation(animation.getAnimatedFraction(), fromRotation, toRotation));
                marker.setRotation(rotation);
            });

            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {}

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });

            valueAnimator.setDuration(1000);
            valueAnimator.start();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public float computeRotation(float fraction, float start, float end) {
        float normalizeEnd = end - start; // rotate start to 0
        float normalizedEndAbs = (normalizeEnd + 360) % 360;

        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
        float rotation = direction > 0 ? normalizedEndAbs : (normalizedEndAbs-360);

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
        public View getInfoContents(Marker marker) { return null; }

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
    private void getViewHeight(View view, int peekHeight, int padding) {
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    height = view.getHeight();
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    resizeMap(peekHeight, padding, 1);

                    if (bounds != null) { mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Constants.BOUNDS_PADDING)); return; }
                    if (!new PermissionManager(PermissionManager.Permission.LOCATION, false).isGranted()) return;
                    else if (TempStorage.CURRENT_LOCATION == null) return;

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TempStorage.CURRENT_LAT_LNG, Constants.DEFAULT_ZOOM));
                }
            });
        }
    }

    private void resizeMap(int peekHeight, int padding, float v) {
        if (height == 0) return;
        int resizedView = height - peekHeight;
        int size = (int) (resizedView * v) + peekHeight + padding;

        mMap.setPadding(0, 0, 0, size);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setMargins(0, 0, 32, size);
        snapBtn.setLayoutParams(params);
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
        unregisterReceiver(networkStatusChangeReceiver);
        unregisterReceiver(gpsStatusChangeReceiver);
        if (onlinePreferenceReference != null && onlinePreferenceListener != null) onlinePreferenceReference.removeEventListener(onlinePreferenceListener);
        if (profileTripReference != null && profileTripListener != null) profileTripReference.removeEventListener(profileTripListener);
        if (vehicleReference != null && vehicleListener != null) vehicleReference.removeEventListener(vehicleListener);
        if (notificationReference != null && notificationListener != null) notificationReference.removeEventListener(notificationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(mTokenReceiver);
        if (tripRequestReference != null && tripRequestListener != null) tripRequestReference.removeEventListener(tripRequestListener);
        if (driverReference != null && driverListener != null) driverReference.removeEventListener(driverListener);
        if (availableDriversReference != null && availableDriversListener != null) availableDriversReference.removeEventListener(availableDriversListener);
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
