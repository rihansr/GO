package com.go.rider.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import com.go.rider.R;
import com.go.rider.adapter.DistrictsAdapter;
import com.go.rider.adapter.UpazilasAdapter;
import com.go.rider.model.location.Place;
import com.go.rider.model.other.ActiveStatus;
import com.go.rider.model.address.Address;
import com.go.rider.model.address.District;
import com.go.rider.model.location.Location;
import com.go.rider.model.other.Preference;
import com.go.rider.model.user.Rider;
import com.go.rider.model.user.Trip;
import com.go.rider.remote.FirebaseHelper;
import com.go.rider.receiver.NetworkStatusChangeReceiver;
import com.go.rider.service.GoService;
import com.go.rider.util.Constants;
import com.go.rider.util.CustomSnackBar;
import com.go.rider.util.extension.DateExtensions;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.AppExtensions;
import com.go.rider.wiget.InstantAutoCompleteTextView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

@SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
public class SignUpActivity extends AppCompatActivity {

    private LinearLayoutCompat              rootLayout;
    private AppCompatEditText               name_Input;
    private AppCompatEditText               email_Input;
    private AppCompatEditText               gender_Input;
    private AppCompatEditText               address_Input;
    private AppCompatEditText               country_Input;
    private AppCompatAutoCompleteTextView   district_Input;
    private InstantAutoCompleteTextView     upazila_Input;
    private AppCompatEditText               postalCode_Input;
    private AppCompatImageButton            signUpBtn;
    private boolean                         isProfileCompleted = true;
    private ProgressDialog                  progressDialog;
    private NetworkStatusChangeReceiver     networkStatusChangeReceiver = new NetworkStatusChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppExtensions.fullScreenActivity(getWindow(), true);
        setContentView(R.layout.activity_sign_up);
        GoService.setActivity(SignUpActivity.this);

        idSetup();

        goBack();

        init();
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
        address_Input = findViewById(R.id.address_Input_Etxt);
        country_Input = findViewById(R.id.country_Input_Etxt);
        district_Input = findViewById(R.id.district_Input_Etxt);
        upazila_Input = findViewById(R.id.upazila_Input_Etxt);
        postalCode_Input = findViewById(R.id.postalCode_Input_Etxt);
        postalCode_Input.setTransformationMethod(new AppExtensions.NumericKeyBoardTransformationMethod());
        signUpBtn = findViewById(R.id.signUpBtn);
        progressDialog = new ProgressDialog(SignUpActivity.this, R.style.ProgressDialog);
    }

    private void goBack() {
        AppCompatImageButton go_Back = findViewById(R.id.back_Btn);
        go_Back.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void init() {
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

        signUpBtn.setOnClickListener(view -> {
            if(Constants.IS_INTERNET_CONNECTED) signUpValidation();
        });
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
                !AppExtensions.isEmailValid(email_Input))
            return;

        progressDialog.setMessage(AppExtensions.getString(R.string.processing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        signUp(userId);
    }

    private void signUp(final String userId) {
        final String name = Objects.requireNonNull(name_Input.getText()).toString().trim();
        final String email = Objects.requireNonNull(email_Input.getText()).toString().trim();
        final String phone = getIntent().getStringExtra(Constants.PHONE_INTENT_KEY);
        final String gender = Objects.requireNonNull(gender_Input.getText()).toString().trim();
        final String address = Objects.requireNonNull(address_Input.getText()).toString().trim();
        final String country = Objects.requireNonNull(country_Input.getText()).toString().trim();
        final String district = Objects.requireNonNull(district_Input.getText()).toString().trim();
        final String upazila = Objects.requireNonNull(upazila_Input.getText()).toString().trim();
        final String postalCode = Objects.requireNonNull(postalCode_Input.getText()).toString().trim();
        final String referralCode = AppExtensions.getReferralCode(name);

        if (!AppExtensions.isInputValid(gender_Input, null)
                ||
                !AppExtensions.isInputValid(address_Input, null)
                ||
                !AppExtensions.isInputValid(district_Input, null)
                ||
                !AppExtensions.isInputValid(upazila_Input, null)
                ||
                !AppExtensions.isInputValid(postalCode_Input, null))
            isProfileCompleted = false;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                progressDialog.dismiss();
                Log.e(Constants.TAG, "SignUp Task Failed: ", task.getException());
                return;
            }

            Rider rider = new Rider();
            rider.setId(userId);
            rider.setToken(null);
            rider.setProfilePhoto(null);
            rider.setCoverPhoto(null);
            rider.setName(name);
            rider.setEmail(email);
            rider.setPhone(phone);
            rider.setGender(gender);
            rider.setAddress(new Address(address, country, district, upazila, postalCode));
            rider.setLocation(new Location(0, 0, 0, 0, 0, 0));
            rider.setProfileCompleted(isProfileCompleted);
            rider.setReferralCode(referralCode);

            List<Place> savedPlaces = new ArrayList<>();
            savedPlaces.add(new Place(0, "Home", null, null, null, null));
            savedPlaces.add(new Place(1, "Work", null, null, null, null));

            rider.setTrip(new Trip(false, (double) 0, 0, true, new Preference(true, savedPlaces), null));
            rider.setActive(new ActiveStatus(true, DateExtensions.currentTime()));
            rider.setRegisteredAt(DateExtensions.currentTime());

            FirebaseDatabase.getInstance().getReference(FirebaseHelper.RIDERS_TABLE).child(userId)
                    .setValue(rider)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();

                        TempStorage.setRiderInfo(rider, true);

                        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                        intent.putExtra(Constants.SUCCESS_KEY, "SUCCESS");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .addOnFailureListener(ex -> {
                        progressDialog.dismiss();
                        Log.e(Constants.TAG, "SignUp Exception: " + ex.getMessage());
                        new CustomSnackBar(rootLayout, R.string.Register_Failed, R.string.retry, CustomSnackBar.Duration.SHORT).show();
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
            progressDialog.dismiss();
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
