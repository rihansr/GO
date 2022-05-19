package com.go.rider.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.go.rider.R;
import com.go.rider.receiver.NetworkStatusChangeReceiver;
import com.go.rider.remote.FirebaseHelper;
import com.go.rider.remote.PermissionManager;
import com.go.rider.service.GoService;
import com.go.rider.util.Constants;
import com.go.rider.util.CustomProgressDialog;
import com.go.rider.util.CustomSnackBar;
import com.go.rider.util.extension.AppExtensions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;
import java.util.Objects;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class SignInActivity extends AppCompatActivity {

    private LinearLayoutCompat          rootLayout;
    private AppCompatEditText           phoneInput;
    private AppCompatImageButton        signInBtn;
    private CustomProgressDialog        progressDialog = new CustomProgressDialog(this);
    private NetworkStatusChangeReceiver networkStatusChangeReceiver = new NetworkStatusChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppExtensions.fullScreenActivity(getWindow(), true);
        setContentView(R.layout.activity_sign_in);
        GoService.setActivity(SignInActivity.this);

        new PermissionManager().showPermissionDialogs();

        idSetup();

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
        phoneInput = findViewById(R.id.phoneInput);
        signInBtn = findViewById(R.id.signInBtn);
        phoneInput.setTransformationMethod(new AppExtensions.NumericKeyBoardTransformationMethod());
    }

    private void init(){
        AppExtensions.doGradientText(findViewById(R.id.title));

        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(AppExtensions.isNumberValid(s.toString())){
                    AppExtensions.hideKeyboard(SignInActivity.this.getCurrentFocus());
                    phoneInput.clearFocus();
                }
            }
        });

        signInBtn.setOnClickListener(v -> {
            if(Constants.IS_INTERNET_CONNECTED) verifyPhoneNumber();
        });

        phoneInput.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        if(Constants.IS_INTERNET_CONNECTED) verifyPhoneNumber();
                        return true;

                    default:
                        break;
                }
            }
            return false;
        });
    }

    private void verifyPhoneNumber(){
        String getPhone = Objects.requireNonNull(phoneInput.getText()).toString().trim();
        if(!AppExtensions.isNumberValid(phoneInput)) return;
        String validatedPhone = Constants.COUNTRY_CODE + getPhone;

        progressDialog.show(R.string.pleaseWait, R.string.verifyNumber,false);

        FirebaseDatabase.getInstance().getReference(FirebaseHelper.DRIVERS_TABLE)
                .orderByChild(FirebaseHelper.USER_PHONE_KEY)
                .equalTo(validatedPhone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressDialog.cancel();

                        if(dataSnapshot.getValue() != null){
                            phoneInput.setError(AppExtensions.getString(R.string.phoneExist_Error));
                            AppExtensions.requestFocus(phoneInput);
                            new CustomSnackBar(rootLayout, R.string.haveRecords, R.string.okay, CustomSnackBar.Duration.LONG).show();
                        }
                        else {
                            Intent intent = new Intent(SignInActivity.this, VerificationActivity.class);
                            intent.putExtra(Constants.PHONE_INTENT_KEY, validatedPhone);
                            startActivity(intent);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.cancel();
                    }
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
                networkStatusChangeReceiver.onReceive(SignInActivity.this, null);
                snackbar.dismiss();
            });
        }
    }
}
