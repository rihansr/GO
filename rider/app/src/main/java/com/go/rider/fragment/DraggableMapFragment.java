package com.go.rider.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import com.airbnb.lottie.LottieAnimationView;
import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.model.location.Place;
import com.go.rider.remote.PermissionManager;
import com.go.rider.service.GoLocationService;
import com.go.rider.util.Constants;
import com.go.rider.util.CustomSnackBar;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.AppExtensions;
import com.go.rider.util.mapUtils.GpsUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import java.util.Objects;

@SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
public class DraggableMapFragment extends DialogFragment implements OnMapReadyCallback {

    private static final String     TAG = DraggableMapFragment.class.getSimpleName();
    private static View             view;
    private AppCompatImageButton    backBtn;
    private AppCompatButton         confirmBtn;
    private AppCompatImageButton    snapBtn;
    private AppCompatTextView       addressInput;
    private AppCompatTextView       messageBox;
    private boolean                 isDragging = false;

    /**
     * Google Map
     **/
    private GoogleMap               mMap;
    private boolean                 goingForPermission = false;
    private boolean                 isGPSOn = false;
    private LottieAnimationView     marker;

    /**
     * Other
     **/
    private Place                   place = new Place();
    private Context                 context;
    private OnPlaceSelectListener   mOnPlaceSelectListener;

    public static DraggableMapFragment show(@NonNull HomeActivity homeActivity, Place place){
        DraggableMapFragment fragment = new DraggableMapFragment();
        if (place != null) {
            Bundle args = new Bundle();
            args.putSerializable(Constants.PLACE_BUNDLE_KEY, place);
            fragment.setArguments(args);
        }
        fragment.show(homeActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_FullDark_FadeAnimation);
        setRetainInstance(true);
        setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!new PermissionManager(PermissionManager.Permission.LOCATION, false).isGranted()){ goingForPermission = true; return; }
        if(goingForPermission){ setLocation(); goingForPermission = false; }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_layout_draggable_map, container, false);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), false);
        super.onViewCreated(view, savedInstanceState);

        initId();

        if (getArguments() == null) { dismiss(); return; }
        Place getPlace = (Place) getArguments().getSerializable(Constants.PLACE_BUNDLE_KEY);
        if (getPlace == null) { dismiss(); return; }
        place = getPlace;
        init();
        getArguments().remove(Constants.PLACE_BUNDLE_KEY);
    }

    private void initId() {
        confirmBtn = view.findViewById(R.id.confirm_btn);
        backBtn = view.findViewById(R.id.back_Btn);
        snapBtn = view.findViewById(R.id.snap_Btn);
        addressInput = view.findViewById(R.id.address_Input_Etxt);
        addressInput.setInputType(InputType.TYPE_NULL);
        marker = view.findViewById(R.id.marker);
        messageBox = view.findViewById(R.id.messageBox);
    }

    private void init() {
        assert getFragmentManager() != null;
        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.draggableMap);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        addressInput.setCompoundDrawablesWithIntrinsicBounds(place.getIcon() != null
                ?
                place.getIcon()
                :
                R.drawable.ic_place_other, 0, R.drawable.ic_search, 0);

        addressInput.setOnClickListener(v ->
                SearchPlaceFragment.show((HomeActivity) context)
                        .setOnPlaceListener(place -> {
                            LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Constants.DEFAULT_ZOOM));
                        })
        );

        backBtn.setOnClickListener(v -> dismiss());

        marker.setOnClickListener(v -> messageBox.setVisibility(View.VISIBLE));

        confirmBtn.setOnClickListener(v -> {
            if (place == null || place.getAddress() == null) {
                messageBox.setVisibility(View.VISIBLE);
                return;
            }

            if (mOnPlaceSelectListener != null) mOnPlaceSelectListener.onSelect(place);
            dismiss();
        });
    }

    /**
     *  Google Map
     **/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        AppExtensions.setMapStyle(googleMap);
        mMap = googleMap;
        AppExtensions.setMapSettings(mMap);

        mMap.setPadding(0, 0, 0, AppExtensions.dpToPx(16));

        if (!new PermissionManager(PermissionManager.Permission.LOCATION, true, response -> setLocation() ).isGranted()) return;
        setLocation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.GPS_PERMISSION_CODE) {
                isGPSOn = true;
            }
        }
        else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.e(Constants.TAG, Objects.requireNonNull(status.getStatusMessage()));
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == status) return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status)){
                CustomSnackBar customSnackBar = new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.playServiceError, R.string.retry, CustomSnackBar.Duration.INDEFINITE);
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
        new GpsUtils(context).turnGPSOn(isGPSEnable -> {
            /**
             * turn on GPS
             **/
            isGPSOn = isGPSEnable;
        });
    }

    private void setLocation() {
        if(!isGooglePlayServicesAvailable()) return;
        if(!isGPSOn) turnGPSOn();

        Location gpsLocation = new GoLocationService(context).getLocation(LocationManager.GPS_PROVIDER);
        LatLng currentLatLng = gpsLocation != null ?  new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude()) : TempStorage.CURRENT_LAT_LNG;

        if(place.getLatitude() != null && place.getLongitude() != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(place.getLatitude(), place.getLongitude()) , Constants.DEFAULT_ZOOM));
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng , Constants.DEFAULT_ZOOM));
        }

        snapBtn.setOnClickListener(v -> {
            if (!new PermissionManager(PermissionManager.Permission.LOCATION, true, response -> setLocation() ).isGranted()) return;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng , Constants.DEFAULT_ZOOM));
        });

        mMap.setOnCameraMoveListener(() -> {
            marker.setProgress(0);

            if(isDragging) return;
            addressInput.setText(null);
            confirmBtn.setEnabled(false);
            confirmBtn.setBackgroundResource(R.drawable.shape_button_disable);
            messageBox.setVisibility(View.GONE);
            isDragging = true;
        });

        mMap.setOnCameraIdleListener(() -> {
            LatLng midLatLng = mMap.getCameraPosition().target;

            place.setAddress(AppExtensions.getAddress(midLatLng.latitude, midLatLng.longitude));
            place.setLatitude(midLatLng.latitude);
            place.setLongitude(midLatLng.longitude);

            if(place.getAddress() != null){
                addressInput.setText(place.getAddress());
                confirmBtn.setEnabled(true);
                confirmBtn.setBackgroundResource(R.drawable.shape_button_enable);
            }

            marker.playAnimation();
            isDragging = false;
        });
    }

    /**
     *  Interface for send comment message to {@link HomeActivity}
     **/
    public void setOnPlaceSelectListener(OnPlaceSelectListener mOnPlaceSelectListener){
        this.mOnPlaceSelectListener = mOnPlaceSelectListener;
    }

    public interface OnPlaceSelectListener {
        void onSelect(Place place);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
