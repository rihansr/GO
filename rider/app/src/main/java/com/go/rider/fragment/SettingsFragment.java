package com.go.rider.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.activity.SignInActivity;
import com.go.rider.adapter.SavePlaceAdapter;
import com.go.rider.model.location.Place;
import com.go.rider.model.user.Rider;
import com.go.rider.remote.FirebaseHelper;
import com.go.rider.remote.PermissionManager;
import com.go.rider.util.Constants;
import com.go.rider.util.CustomSnackBar;
import com.go.rider.util.SharedPreference;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.AppExtensions;
import com.go.rider.wiget.CircleImageView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;
import static com.go.rider.activity.HomeActivity.drawerLayout;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class SettingsFragment extends DialogFragment {

    private static final String         TAG = SettingsFragment.class.getSimpleName();

    /**
     * Toolbar
     **/
    private AppCompatTextView           title;
    private AppCompatImageView          backBtn;

    /**
     * Profile
     * */
    private RelativeLayout              profilePhotoHolder;
    private CircleImageView             profilePhoto;
    private AppCompatTextView           userName;
    private AppCompatTextView           userPhone;

    /**
     * Saved Address
     **/
    private RecyclerView                rcvPlaces;
    private SavePlaceAdapter            savePlaceAdapter;
    private AppCompatTextView           addAddress_Btn;

    /**
     * Permissions
     **/
    private SwitchCompat                locationAccess_Switch;
    private SwitchCompat                callAccess_Switch;
    private SwitchCompat                cameraAccess_Switch;
    private SwitchCompat                galleryAccess_Switch;

    /**
     * Preferences
     **/
    private SwitchCompat                allowNotification_Switch;
    private AppCompatTextView           language_Btn;
    private AppCompatTextView           signOut_Btn;

    /**
     * Other
     **/
    private LoaderFragment              loader;
    private FirebaseHelper              firebaseHelper;
    private Context                     context;
    private Activity                    activity;
    private boolean                     isSettingsUpdated = false;
    private OnSettingsUpdateListener    mOnSettingsUpdateListener;

    public static SettingsFragment show(HomeActivity homeActivity){
        SettingsFragment fragment = new SettingsFragment();
        fragment.show(homeActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_TopLight_SliderAnimation);
        setRetainInstance(true);
        setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        new Handler().postDelayed(() -> drawerLayout.closeDrawer(GravityCompat.START), Constants.SLIDE_OUT_DURATION);
        setPermissions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), true);
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        title = view.findViewById(R.id.title);
        backBtn = view.findViewById(R.id.leftBtn);

        profilePhotoHolder = view.findViewById(R.id.profilePhotoHolder);
        profilePhoto = view.findViewById(R.id.profilePhoto);
        userName = view.findViewById(R.id.userName);
        userPhone = view.findViewById(R.id.userPhone);

        rcvPlaces = view.findViewById(R.id.rcvPlaces);
        addAddress_Btn = view.findViewById(R.id.addAddress_Btn);

        locationAccess_Switch = view.findViewById(R.id.locationAccessSwitch);
        callAccess_Switch = view.findViewById(R.id.callAccessSwitch);
        cameraAccess_Switch = view.findViewById(R.id.cameraAccessSwitch);
        galleryAccess_Switch = view.findViewById(R.id.galleryAccessSwitch);

        allowNotification_Switch = view.findViewById(R.id.allowNotificationSwitch);
        language_Btn = view.findViewById(R.id.language_Btn);
        signOut_Btn = view.findViewById(R.id.signOut_Btn);

        firebaseHelper = new FirebaseHelper(context);
    }

    private void init(){
        setProfileInfo();

        setPlacesAdapter();

        title.setText(AppExtensions.getString(R.string.settings));

        backBtn.setOnClickListener(v -> dismiss());

        profilePhotoHolder.setOnClickListener(v ->
                ProfileFragment.show((HomeActivity) context)
                        .setOnProfileUpdateListener(isUpdated -> {
                            if (!isSettingsUpdated) isSettingsUpdated = isUpdated;
                        })
        );

        addAddress_Btn.setOnClickListener(v -> {
            List<Place> places = TempStorage.RIDER.getTrip().getPreference().getSavedPlaces();
            SavePlaceFragment.show((HomeActivity) context, new Place(places.size(), null, null, null, null, R.drawable.ic_place_other))
                    .setOnPlaceUpdateListener(editedPlace -> {
                        isSettingsUpdated = true;

                        switch (editedPlace.getId()){
                            case 0: places.set(0, editedPlace);  break;
                            case 1: places.set(1, editedPlace);  break;
                            default: places.add(editedPlace);
                        }

                        for (int position = 0; position<places.size(); position++){
                            places.get(position).setIcon(null);
                            places.get(position).setId(position);
                        }

                        updatePlaces(places, AppExtensions.Action.Edit);
                    });
        });

        for (SwitchCompat switchCompat : new SwitchCompat[]{locationAccess_Switch, callAccess_Switch, cameraAccess_Switch, galleryAccess_Switch}){
            switchCompat.setOnClickListener(v -> new PermissionManager().goToPermissionSetting());
        }

        allowNotification_Switch.setOnClickListener(v -> new PermissionManager().goToNotificationPermissionSetting());

        signOut_Btn.setOnClickListener(v -> {
            TempStorage.setRiderInfo(null, true);
            new SharedPreference().setRiderLoggedIn(false);

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(context, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setProfileInfo() {
        if(TempStorage.RIDER == null) return;

        int placeHolder = TempStorage.RIDER.getGender().equals(AppExtensions.Gender.Male.toString()) ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;
        AppExtensions.loadPhoto(profilePhoto, TempStorage.RIDER.getProfilePhoto(), R.dimen.icon_Size_XX_Large, placeHolder);

        userName.setText(TempStorage.RIDER.getName());
        userPhone.setText(TempStorage.RIDER.getPhone());
    }

    private void setPlacesAdapter() {
        rcvPlaces.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        savePlaceAdapter = new SavePlaceAdapter(context, 0);
        rcvPlaces.setAdapter(savePlaceAdapter);

        savePlaceAdapter.setPlaces(TempStorage.RIDER.getTrip().getPreference().getSavedPlaces());

        savePlaceAdapter.setOnPlaceListener(new SavePlaceAdapter.OnPlaceListener() {
            @Override
            public void onSelect(Place place) {

            }

            @Override
            public void onEdit(int position, Place place) {
                SavePlaceFragment.show((HomeActivity) context, place).setOnPlaceUpdateListener(editedPlace -> {
                    isSettingsUpdated = true;
                    List<Place> places = TempStorage.RIDER.getTrip().getPreference().getSavedPlaces();
                    editedPlace.setIcon(null);
                    places.set(position, editedPlace);
                    updatePlaces(places, AppExtensions.Action.Edit);
                });
            }

            @Override
            public void onDelete(int position) {
                isSettingsUpdated = true;

                List<Place> places = TempStorage.RIDER.getTrip().getPreference().getSavedPlaces();

                switch (position){
                    case 0:
                        places.set(position, new Place(0, "Home", null, null, null, null));
                        break;

                    case 1:
                        places.set(position, new Place(1, "Work", null, null, null, null));
                        break;

                    default:
                        places.remove(position);
                }

                updatePlaces(places, AppExtensions.Action.Delete);
            }
        });
    }

    private void updatePlaces(List<Place> places, AppExtensions.Action action){
        if(!Constants.IS_INTERNET_CONNECTED){
            new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.network_Error, R.string.retry, CustomSnackBar.Duration.LONG).show();
            return;
        }

        showLoader();

        firebaseHelper.setData(firebaseHelper.riderReference().child(FirebaseHelper.USER_SAVED_PLACES_KEY).setValue(places),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        dismissLoader();
                        savePlaceAdapter.setPlaces(places);
                        Rider tempRider = new Rider(TempStorage.RIDER, false);;
                        tempRider.getTrip().getPreference().setSavedPlaces(places);
                        TempStorage.setRiderInfo(tempRider, true);
                    }

                    @Override
                    public void onFailure() {
                        dismissLoader();
                        new CustomSnackBar(AppExtensions.getRootView(getDialog()), action == AppExtensions.Action.Edit ? R.string.editPlaceFailed : R.string.removePlaceFailed).show();
                    }
                });
    }

    private void setPermissions() {
        allowNotification_Switch.setChecked(NotificationManagerCompat.from(context).areNotificationsEnabled());
        locationAccess_Switch.setChecked(new PermissionManager(PermissionManager.Permission.LOCATION, false).isGranted());
        callAccess_Switch.setChecked(new PermissionManager(PermissionManager.Permission.PHONE, false).isGranted());
        cameraAccess_Switch.setChecked(new PermissionManager(PermissionManager.Permission.CAMERA, false).isGranted());
        galleryAccess_Switch.setChecked(new PermissionManager(PermissionManager.Permission.GALLERY, false).isGranted());
    }

    private void showLoader(){
        loader = LoaderFragment.show((HomeActivity) context);
    }

    private void dismissLoader(){
        if (loader != null && loader.getDialog() != null
                &&
                loader.getDialog().isShowing()
                &&
                !loader.isRemoving())

            loader.dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mOnSettingsUpdateListener != null) mOnSettingsUpdateListener.onSettingsUpdate(isSettingsUpdated);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        this.activity = (Activity) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void setOnSettingsUpdateListener(OnSettingsUpdateListener mOnSettingsUpdateListener) {
        this.mOnSettingsUpdateListener = mOnSettingsUpdateListener;
    }

    public interface OnSettingsUpdateListener {
        void onSettingsUpdate(boolean isUpdated);
    }
}
