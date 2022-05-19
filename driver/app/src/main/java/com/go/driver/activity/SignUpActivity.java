package com.go.driver.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SimpleAdapter;

import com.go.driver.R;
import com.go.driver.adapter.DistrictsAdapter;
import com.go.driver.adapter.UpazilasAdapter;
import com.go.driver.fragment.PhotoActionFragment;
import com.go.driver.model.other.ActiveStatus;
import com.go.driver.model.address.Address;
import com.go.driver.model.address.District;
import com.go.driver.model.other.Preference;
import com.go.driver.model.user.Driver;
import com.go.driver.model.location.Location;
import com.go.driver.model.user.Trip;
import com.go.driver.model.vehicle.VehicleInfo;
import com.go.driver.model.vehicle.VehicleType;
import com.go.driver.remote.FirebaseHelper;
import com.go.driver.receiver.NetworkStatusChangeReceiver;
import com.go.driver.remote.PermissionManager;
import com.go.driver.service.GoService;
import com.go.driver.util.Constants;
import com.go.driver.util.CustomPopupWindow;
import com.go.driver.util.CustomSnackBar;
import com.go.driver.util.extension.DateExtensions;
import com.go.driver.util.TempStorage;
import com.go.driver.util.extension.AppExtensions;
import com.go.driver.wiget.CircleImageView;
import com.go.driver.wiget.InstantAutoCompleteTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

@SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
public class SignUpActivity extends AppCompatActivity {

    private LinearLayoutCompat              rootLayout;
    private AppCompatEditText               name_Input;
    private AppCompatEditText               email_Input;
    private AppCompatEditText               gender_Input;
    private AppCompatEditText               dob_Input;
    private AppCompatEditText               nid_Input;
    private AppCompatEditText               license_Input;
    private FrameLayout                     nid_Photo_Holder;
    private CircleImageView                 nid_Photo;
    private FrameLayout                     license_Photo_Holder;
    private CircleImageView                 license_Photo;
    private AppCompatEditText               vehicleType_Input;
    private AppCompatEditText               vehicleModel_Input;
    private AppCompatEditText               licensePlate_Input;
    private AppCompatEditText               address_Input;
    private AppCompatEditText               country_Input;
    private AppCompatAutoCompleteTextView   district_Input;
    private InstantAutoCompleteTextView     upazila_Input;
    private AppCompatEditText               postalCode_Input;
    private AppCompatImageButton            signUp_Btn;
    private VehicleType                     selectedVehicleType = null;
    private byte[]                          nationalIdPhotoBytes = null;
    private byte[]                          drivingLicensePhotoBytes = null;
    private boolean                         isProfileCompleted = true;
    private ProgressDialog                  progressDialog;
    private FirebaseHelper                  firebaseHelper;
    private AppExtensions.Photo             selectPhoto = AppExtensions.Photo.PROFILE;
    private NetworkStatusChangeReceiver     networkStatusChangeReceiver = new NetworkStatusChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppExtensions.fullScreenActivity(getWindow(), true);
        setContentView(R.layout.activity_sign_up);
        GoService.setActivity(SignUpActivity.this);

        idSetup();

        init();

        goBack();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStatusChangeReceiver, new IntentFilter(CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStatusChangeReceiver);
    }

    private void idSetup(){
        rootLayout = findViewById(R.id.rootLayout);
        name_Input = findViewById(R.id.name_Input_Etxt);
        email_Input = findViewById(R.id.email_Input_Etxt);
        gender_Input = findViewById(R.id.gender_Input_Etxt);
        gender_Input.setInputType(InputType.TYPE_NULL);
        dob_Input = findViewById(R.id.dob_Input_Etxt);
        dob_Input.setInputType(InputType.TYPE_NULL);
        nid_Input = findViewById(R.id.nid_Input_Etxt);
        nid_Input.setTransformationMethod(new AppExtensions.NumericKeyBoardTransformationMethod());
        nid_Photo_Holder = findViewById(R.id.nid_Photo_Holder);
        nid_Photo = findViewById(R.id.nid_Photo);
        license_Input = findViewById(R.id.drivingLicense_Input_Etxt);
        license_Photo_Holder = findViewById(R.id.drivingLicense_Photo_Holder);
        license_Photo = findViewById(R.id.drivingLicense_Photo);
        vehicleType_Input = findViewById(R.id.vehicleType_Input_Etxt);
        vehicleType_Input.setInputType(InputType.TYPE_NULL);
        vehicleModel_Input = findViewById(R.id.vehicleModel_Input_Etxt);
        licensePlate_Input = findViewById(R.id.licensePlate_Input_Etxt);
        address_Input = findViewById(R.id.address_Input_Etxt);
        country_Input = findViewById(R.id.country_Input_Etxt);
        district_Input = findViewById(R.id.district_Input_Etxt);
        upazila_Input = findViewById(R.id.upazila_Input_Etxt);
        postalCode_Input = findViewById(R.id.postalCode_Input_Etxt);
        postalCode_Input.setTransformationMethod(new AppExtensions.NumericKeyBoardTransformationMethod());
        signUp_Btn = findViewById(R.id.signUpBtn);
        firebaseHelper = new FirebaseHelper(SignUpActivity.this);
        progressDialog = new ProgressDialog(SignUpActivity.this, R.style.ProgressDialog);
        networkStatusChangeReceiver = new NetworkStatusChangeReceiver();
    }

    private void init(){
        getVehicleTypes();

        AppExtensions.doGradientText(findViewById(R.id.title));
        country_Input.setText("Bangladesh");

        district_Input.setThreshold(1);
        district_Input.setAdapter(new DistrictsAdapter(SignUpActivity.this, AppExtensions.getDistricts()));

        district_Input.setOnItemClickListener((parent, view, position, id) -> {
            District district = (District) parent.getAdapter().getItem(position);
            upazila_Input.setThreshold(1);
            upazila_Input.setAdapter(new UpazilasAdapter(SignUpActivity.this, district.getUpazilas()));
            upazila_Input.setText(null);
            upazila_Input.requestFocus();
        });

        upazila_Input.setOnItemClickListener((parent, view, position, id) -> postalCode_Input.requestFocus());

        postalCode_Input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == (4)){
                    AppExtensions.hideKeyboard(SignUpActivity.this.getCurrentFocus());
                    postalCode_Input.clearFocus();
                }
            }
        });

        gender_Input.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                AppExtensions.createPopupMenu(gender_Input, new Object[]{AppExtensions.Gender.Male, AppExtensions.Gender.Female});
            }
            return false;
        });

        dob_Input.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                AppExtensions.setDatePicker(dob_Input);
            }
            return false;
        });

        nid_Photo_Holder.setOnClickListener(view -> {
            selectPhoto = AppExtensions.Photo.NID;
            PhotoActionFragment.show(SignUpActivity.this).setOnActionListener((dialog, isCapture) -> {
                if(isCapture) captureByCamera();
                else pickFromGallery(R.string.select_Nid);
                dialog.dismiss();
            });
        });

        license_Photo_Holder.setOnClickListener(view -> {
            selectPhoto = AppExtensions.Photo.LICENSE;
            PhotoActionFragment.show(SignUpActivity.this).setOnActionListener((dialog, isCapture) -> {
                if(isCapture) captureByCamera();
                else pickFromGallery(R.string.select_Nid);
                dialog.dismiss();
            });
        });

        signUp_Btn.setOnClickListener(view -> {
            if(Constants.IS_INTERNET_CONNECTED) signUpValidation();
        });
    }

    private void getVehicleTypes() {
        FirebaseDatabase.getInstance().getReference(FirebaseHelper.VEHICLE_TYPES_TABLE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()) return;

                        final ArrayList<VehicleType> vehicleTypes = new ArrayList<>();
                        final ArrayList<HashMap<String, String>> brands = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            VehicleType vehicleType = snapshot.getValue(VehicleType.class);
                            if (vehicleType == null) continue;
                            if(!vehicleType.isAvailable()) continue;

                            vehicleTypes.add(vehicleType);
                            HashMap<String, String> map = new HashMap<>();
                            map.put("type", vehicleType.getType());
                            brands.add(map);
                        }

                        setVehicleTypes(vehicleTypes, brands);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void setVehicleTypes(final ArrayList<VehicleType> vehicleTypes, final ArrayList<HashMap<String, String>> typeMaps){
        vehicleType_Input.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                AppExtensions.hideKeyboard(SignUpActivity.this.getCurrentFocus());

                SimpleAdapter adapter = new SimpleAdapter(SignUpActivity.this, typeMaps, R.layout.sample_item, new String[]{"type"}, new int[]{R.id.itemTv});

                new CustomPopupWindow(SignUpActivity.this, vehicleType_Input, adapter)
                        .setOnItemClickListener((popupWindow, parent, view, position, id) -> {
                            vehicleType_Input.setText(typeMaps.get(position).get("type"));
                            selectedVehicleType = vehicleTypes.get(position);
                            popupWindow.dismiss();
                        });
            }

            return false;
        });
    }

    private void goBack() {
        AppCompatImageButton go_Back = findViewById(R.id.back_Btn);
        go_Back.setOnClickListener(v -> SignUpActivity.super.onBackPressed());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            try {
                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                if(mBitmap == null) return;

                switch (selectPhoto){
                    case NID:
                        nationalIdPhotoBytes = null;
                        nid_Photo.setImageBitmap(mBitmap);
                        nationalIdPhotoBytes = AppExtensions.getBitmapBytes(mBitmap);
                        break;

                    case LICENSE:
                        drivingLicensePhotoBytes = null;
                        license_Photo.setImageBitmap(mBitmap);
                        drivingLicensePhotoBytes = AppExtensions.getBitmapBytes(mBitmap);
                        break;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            try {
                Bitmap mBitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                if (mBitmap == null) return;

                switch (selectPhoto){
                    case NID:
                        nationalIdPhotoBytes = null;
                        nid_Photo.setImageBitmap(mBitmap);
                        nationalIdPhotoBytes = AppExtensions.getBitmapBytes(mBitmap);
                        break;

                    case LICENSE:
                        drivingLicensePhotoBytes = null;
                        license_Photo.setImageBitmap(mBitmap);
                        drivingLicensePhotoBytes = AppExtensions.getBitmapBytes(mBitmap);
                        break;
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    private void pickFromGallery(int chooserTitle) {
        if (!new PermissionManager(PermissionManager.Permission.GALLERY, true, response ->
                pickFromGallery(selectPhoto == AppExtensions.Photo.PROFILE ? R.string.select_ProfilePhoto : R.string.select_CoverPhoto)
        ).isGranted()) return;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, AppExtensions.getString(chooserTitle)), Constants.GALLERY_REQUEST_CODE);
    }

    private void captureByCamera() {
        if (!new PermissionManager(PermissionManager.Permission.CAMERA, true, response -> captureByCamera() ).isGranted()) return;
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, Constants.CAMERA_REQUEST_CODE);
    }

    private void signUpValidation() {
        String userId = Objects.requireNonNull(new FirebaseHelper(SignUpActivity.this).getFirebaseUser()).getUid();

        if(!getIntent().hasExtra(Constants.PHONE_INTENT_KEY)){
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (!AppExtensions.isInputValid(name_Input, R.string.name_Error)
                ||
                !AppExtensions.isEmailValid(email_Input)
                ||
                !AppExtensions.isInputValid(nid_Input, R.string.nid_Error)
                ||
                !AppExtensions.isInputValid(nid_Input, nationalIdPhotoBytes == null, R.string.nidPhoto_Error)
                ||
                !AppExtensions.isInputValid(license_Input, R.string.license_Error)
                ||
                !AppExtensions.isInputValid(license_Input, drivingLicensePhotoBytes == null, R.string.licensePhoto_Error)
                ||
                !AppExtensions.isInputValid(vehicleType_Input, selectedVehicleType == null, R.string.vehicleType_Error)
                ||
                !AppExtensions.isInputValid(vehicleModel_Input, R.string.vehicleModel_Error)
                ||
                !AppExtensions.isInputValid(licensePlate_Input, R.string.licensePlate_Error)
                ||
                !AppExtensions.isInputValid(district_Input, R.string.district_Error)
                ||
                !AppExtensions.isInputValid(upazila_Input, R.string.upazila_Error)
        ) return;

        progressDialog.setMessage(AppExtensions.getString(R.string.processing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        if(nationalIdPhotoBytes != null) uploadNIDPhoto(userId, nationalIdPhotoBytes);
        else if(drivingLicensePhotoBytes != null) uploadLicensePhoto(userId, null, drivingLicensePhotoBytes);
        else signUp(userId, null, null);
    }

    private void uploadNIDPhoto(final String userId, byte[] nidPhotoBytes){
        firebaseHelper.uploadPhoto(nidPhotoBytes, null, AppExtensions.Photo.NID, new FirebaseHelper.OnPhotoUploadListener() {
            @Override
            public void onSuccess(String nidPhotoLink) {
                if(drivingLicensePhotoBytes != null) uploadLicensePhoto(userId, nidPhotoLink, drivingLicensePhotoBytes);
                else signUp(userId, nidPhotoLink, null);
            }

            @Override
            public void onFailure() {}

            @Override
            public void onProgress(double progress) {
                progressDialog.setMessage(((int)(drivingLicensePhotoBytes == null ? progress : progress/2)) + "% " + AppExtensions.getString(R.string.complete));
            }
        });
    }

    private void uploadLicensePhoto(final String userId, final String nidPhoto, byte[] licensePhotoBytes){
        firebaseHelper.uploadPhoto(licensePhotoBytes, null, AppExtensions.Photo.LICENSE, new FirebaseHelper.OnPhotoUploadListener() {
            @Override
            public void onSuccess(String licensePhotoLink) {
                signUp(userId, nidPhoto, licensePhotoLink);
            }

            @Override
            public void onFailure() {}

            @Override
            public void onProgress(double progress) {
                progressDialog.setMessage(((int)(drivingLicensePhotoBytes == null ? progress : (50 + ((int)progress/2)))) + "% " + AppExtensions.getString(R.string.complete));
            }
        });
    }

    private void signUp(final String userId, final String nidPhoto, final String licensePhoto){
        final String name = Objects.requireNonNull(name_Input.getText()).toString().trim();
        final String phone = getIntent().getStringExtra(Constants.PHONE_INTENT_KEY);
        final String email = Objects.requireNonNull(email_Input.getText()).toString().trim();
        final String gender = Objects.requireNonNull(gender_Input.getText()).toString().trim();
        final String dob = Objects.requireNonNull(dob_Input.getText()).toString().trim();
        final Long birthDate = dob.isEmpty() ? null : new DateExtensions(dob).getBirthDate();
        final Integer age = dob.isEmpty() ? null : new DateExtensions(SignUpActivity.this, dob).getAge();
        final String nationalId = Objects.requireNonNull(nid_Input.getText()).toString().trim();
        final String vehicleModel = Objects.requireNonNull(vehicleModel_Input.getText()).toString().trim();
        final String licensePlate = Objects.requireNonNull(licensePlate_Input.getText()).toString().trim();
        final String address = Objects.requireNonNull(address_Input.getText()).toString().trim();
        final String country = Objects.requireNonNull(country_Input.getText()).toString().trim();
        final String district = Objects.requireNonNull(district_Input.getText()).toString().trim();
        final String upazila = Objects.requireNonNull(upazila_Input.getText()).toString().trim();
        final String postalCode = Objects.requireNonNull(postalCode_Input.getText()).toString().trim();
        final String drivingLicense = Objects.requireNonNull(license_Input.getText()).toString().trim();
        final String referralCode = AppExtensions.getReferralCode(name);

        if (!AppExtensions.isInputValid(gender_Input, null)
                ||
                !AppExtensions.isInputValid(dob_Input, null)
                ||
                !AppExtensions.isInputValid(address_Input, null)
                ||
                !AppExtensions.isInputValid(postalCode_Input, null))
            isProfileCompleted = false;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                progressDialog.cancel();
                Log.e(Constants.TAG, "Driver Add Task Failed: ", task.getException());
                return;
            }

            Driver driver = new Driver();
            driver.setId(userId);
            driver.setToken(null);
            driver.setProfilePhoto(null);
            driver.setCoverPhoto(null);
            driver.setName(name);
            driver.setPhone(phone);
            driver.setEmail(email);
            driver.setGender(gender);
            driver.setDateOfBirth(birthDate);
            driver.setAge(age);
            driver.setNationalId(nationalId);
            driver.setNationalId_Photo(nidPhoto);
            driver.setAddress(new Address(address, country, district, upazila, postalCode));
            driver.setLocation(new Location(0, 0, 0, 0, 0, 0));
            driver.setReferralCode(referralCode);
            VehicleType vehicleType = new VehicleType(selectedVehicleType, true);
            vehicleType.setModel(vehicleModel);
            driver.setVehicleInfo(new VehicleInfo(vehicleType, drivingLicense, licensePhoto, licensePlate));
            driver.setProfileCompleted(isProfileCompleted);
            driver.setTrip(new Trip(false, (double) 0, 0, true, new Preference(false), null));
            driver.setActive(new ActiveStatus(true, DateExtensions.currentTime()));
            driver.setRegisteredAt(DateExtensions.currentTime());

            FirebaseDatabase.getInstance().getReference(FirebaseHelper.DRIVERS_TABLE).child(userId)
                    .setValue(driver)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.cancel();

                        TempStorage.setDriverInfo(driver, true);

                        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                        intent.putExtra(Constants.SUCCESS_KEY, "SUCCESS");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .addOnFailureListener(ex -> {
                        progressDialog.cancel();
                        Log.e(Constants.TAG, "SignUp Exception: " + ex.getMessage());
                        new CustomSnackBar(rootLayout, R.string.Register_Failed, R.string.retry).show();
                    });
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            progressDialog.cancel();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     *  {@link NetworkStatusChangeReceiver} Monitor internet connection
     **/
    public void updateInternetConnectionStatus(boolean isConnected) {
        CustomSnackBar customSnackBar = new CustomSnackBar(rootLayout, R.string.network_Error, R.string.retry, CustomSnackBar.Duration.INDEFINITE);

        if (isConnected) {
            customSnackBar.dismiss();
        }
        else {
            customSnackBar.show();
            customSnackBar.setOnDismissListener(snackbar -> {
                networkStatusChangeReceiver.onReceive(SignUpActivity.this, null);
                snackbar.dismiss();
            });
        }
    }
}