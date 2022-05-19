package com.go.driver.fragment;

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

import com.go.driver.R;
import com.go.driver.activity.HomeActivity;
import com.go.driver.activity.SignInActivity;
import com.go.driver.model.user.Driver;
import com.go.driver.remote.FirebaseHelper;
import com.go.driver.remote.PermissionManager;
import com.go.driver.util.Constants;
import com.go.driver.util.CustomSnackBar;
import com.go.driver.util.SharedPreference;
import com.go.driver.util.TempStorage;
import com.go.driver.util.extension.AppExtensions;
import com.go.driver.wiget.CircleImageView;
import com.google.firebase.auth.FirebaseAuth;

import static com.go.driver.activity.HomeActivity.drawerLayout;

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
    private SwitchCompat                allowAvailable_Switch;
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

        locationAccess_Switch = view.findViewById(R.id.locationAccessSwitch);
        callAccess_Switch = view.findViewById(R.id.callAccessSwitch);
        cameraAccess_Switch = view.findViewById(R.id.cameraAccessSwitch);
        galleryAccess_Switch = view.findViewById(R.id.galleryAccessSwitch);

        allowNotification_Switch = view.findViewById(R.id.allowNotificationSwitch);
        allowAvailable_Switch = view.findViewById(R.id.availabilitySwitch);
        language_Btn = view.findViewById(R.id.language_Btn);
        signOut_Btn = view.findViewById(R.id.signOut_Btn);

        firebaseHelper = new FirebaseHelper(context);
    }

    private void init(){
        setProfileInfo();

        title.setText(AppExtensions.getString(R.string.settings));

        backBtn.setOnClickListener(v -> dismiss());

        profilePhotoHolder.setOnClickListener(v ->
                ProfileFragment.show((HomeActivity) context)
                        .setOnProfileUpdateListener(isUpdated -> {
                            if (!isSettingsUpdated) isSettingsUpdated = isUpdated;
                        })
        );

        for (SwitchCompat switchCompat : new SwitchCompat[]{locationAccess_Switch, callAccess_Switch, cameraAccess_Switch, galleryAccess_Switch}){
            switchCompat.setOnClickListener(v -> new PermissionManager().goToPermissionSetting());
        }

        allowNotification_Switch.setOnClickListener(v -> new PermissionManager().goToNotificationPermissionSetting());

        allowAvailable_Switch.setText(AppExtensions.getString(TempStorage.DRIVER.getTrip().getPreference().isAvailable() ? R.string.online : R.string.offline));
        allowAvailable_Switch.setChecked(TempStorage.DRIVER.getTrip().getPreference().isAvailable());

        allowAvailable_Switch.setOnCheckedChangeListener((buttonView, isChecked) -> setAvailability(isChecked));

        signOut_Btn.setOnClickListener(v -> {
            firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_AVAILABILITY_KEY).setValue(false),
                    new FirebaseHelper.OnFirebaseUpdateListener() {
                        @Override
                        public void onSuccess() {
                            TempStorage.setDriverInfo(null, true);
                            new SharedPreference().setDriverLoggedIn(false);

                            FirebaseAuth.getInstance().signOut();

                            Intent intent = new Intent(context, SignInActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure() {
                            dismissLoader();
                            new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.failureMessage, R.string.retry).show();
                        }
                    });
        });
    }

    private void setProfileInfo() {
        if(TempStorage.DRIVER == null) return;

        int placeHolder = TempStorage.DRIVER.getGender().equals(AppExtensions.Gender.Male.toString()) ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;
        AppExtensions.loadPhoto(profilePhoto, TempStorage.DRIVER.getProfilePhoto(), R.dimen.icon_Size_XX_Large, placeHolder);

        userName.setText(TempStorage.DRIVER.getName());
        userPhone.setText(TempStorage.DRIVER.getPhone());
    }

    private void setAvailability(boolean available){
        if(!Constants.IS_INTERNET_CONNECTED){
            allowAvailable_Switch.setChecked(!available);
            new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.network_Error, R.string.retry, CustomSnackBar.Duration.LONG).show();
            return;
        }

        showLoader();

        firebaseHelper.setData(firebaseHelper.driverReference().child(FirebaseHelper.USER_AVAILABILITY_KEY).setValue(available),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        dismissLoader();
                        allowAvailable_Switch.setText(available ? R.string.online : R.string.offline);
                        allowAvailable_Switch.setChecked(available);
                        Driver tempDriver = TempStorage.DRIVER;
                        tempDriver.getTrip().getPreference().setAvailable(available);
                        TempStorage.setDriverInfo(tempDriver, true);
                    }

                    @Override
                    public void onFailure() {
                        dismissLoader();
                        allowNotification_Switch.setChecked(!available);
                        new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.failureMessage, R.string.retry).show();
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
        drawerLayout.openDrawer(GravityCompat.START);
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
