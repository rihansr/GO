package com.go.driver.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.GravityCompat;

import com.go.driver.R;
import com.go.driver.model.user.Driver;
import com.go.driver.remote.FirebaseHelper;
import com.go.driver.receiver.NetworkStatusChangeReceiver;
import com.go.driver.service.GoService;
import com.go.driver.util.Constants;
import com.go.driver.util.CustomProgressDialog;
import com.go.driver.util.CustomSnackBar;
import com.go.driver.util.TempStorage;
import com.go.driver.util.extension.AppExtensions;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static com.go.driver.activity.HomeActivity.drawerLayout;

public class VerificationActivity extends AppCompatActivity {

    private LinearLayoutCompat          rootLayout;
    private AppCompatButton             resendCodeBtn;
    private AppCompatImageButton        confirmBtn;
    private AppCompatEditText           codeInput;
    private FirebaseAuth                mAuth;
    private String                      mVerificationId;
    private CustomProgressDialog        progressDialog = new CustomProgressDialog(this);
    private NetworkStatusChangeReceiver networkStatusChangeReceiver = new NetworkStatusChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppExtensions.fullScreenActivity(getWindow(), true);
        setContentView(R.layout.activity_verification);
        GoService.setActivity(VerificationActivity.this);

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
        codeInput = findViewById(R.id.codeInput);
        resendCodeBtn = findViewById(R.id.resendCodeBtn);
        confirmBtn = findViewById(R.id.confirmBtn);
        networkStatusChangeReceiver = new NetworkStatusChangeReceiver();
    }

    private void init(){
        AppExtensions.doGradientText(findViewById(R.id.title));

        sendVerificationCode();

        resendCodeBtn.setOnClickListener(v -> {
            if(!Constants.IS_INTERNET_CONNECTED) return;
            resendCodeBtn.setVisibility(View.GONE);
            codeInput.setText(null);
            sendVerificationCode();
        });

        confirmBtn.setOnClickListener(v -> {
            String code = Objects.requireNonNull(codeInput.getText()).toString().trim();
            AppExtensions.hideKeyboard(VerificationActivity.this.getCurrentFocus());
            codeInput.clearFocus();

            if(Constants.IS_INTERNET_CONNECTED) verifyVerificationCode(code);
        });

        codeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) {
                    AppExtensions.hideKeyboard(VerificationActivity.this.getCurrentFocus());
                    codeInput.clearFocus();
                }
            }
        });
    }

    private void goBack() {
        AppCompatImageButton go_Back = findViewById(R.id.back_Btn);
        go_Back.setOnClickListener(v -> VerificationActivity.super.onBackPressed());
    }

    private void sendVerificationCode() {
        if(!getIntent().hasExtra(Constants.PHONE_INTENT_KEY)) return;
        String phoneNumber = getIntent().getStringExtra(Constants.PHONE_INTENT_KEY);
        mAuth = FirebaseAuth.getInstance();

        progressDialog.show(R.string.receiveCode,true);

        if (phoneNumber != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,        // Phone number to verify
                    60,              // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,        // Activity (for callback binding)
                    mCallbacks);        // OnVerificationStateChangedCallbacks
        }

        mAuth.setLanguageCode("en");

        new Handler().postDelayed(() -> resendCodeBtn.setVisibility(View.VISIBLE), Constants.RESEND_CODE_INTERVAL);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            /**
             * Getting the code sent by SMS
             **/
            String code = phoneAuthCredential.getSmsCode();

            /**
             * sometime the code is not detected automatically
             * in this case the code will be null
             * so user has to manually enter the code
             **/
            if (code != null) {
                Log.i(Constants.TAG, "Verification Code: " + code);
                codeInput.setText(code);
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.i(Constants.TAG, "Verification Failed Reason: " + e.getMessage());
            progressDialog.cancel();
            e.printStackTrace();
        }

        @Override
        public void onCodeSent(@NonNull String code, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(code, forceResendingToken);
            mVerificationId = code;
            progressDialog.cancel();
        }
    };

    private void verifyVerificationCode(String code) {
        if(mVerificationId != null){
            resendCodeBtn.setVisibility(View.GONE);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        }
        else {
            resendCodeBtn.setVisibility(View.VISIBLE);
            new CustomSnackBar(rootLayout, R.string.invalidCode, R.string.retry, CustomSnackBar.Duration.LONG).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressDialog.show(R.string.pleaseWait, R.string.checkingVerificationCode,true);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerificationActivity.this, task -> {
                    if (task.isSuccessful()) {
                        verifyPhoneNumber();
                    }
                    else {
                        progressDialog.cancel(); resendCodeBtn.setVisibility(View.VISIBLE);
                        String message = AppExtensions.getString(R.string.fixError);

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            message = AppExtensions.getString(R.string.invalidCode);
                        }

                        new CustomSnackBar(rootLayout, message, R.string.retry, CustomSnackBar.Duration.LONG).show();
                    }
                })
                .addOnFailureListener(e -> { progressDialog.cancel(); resendCodeBtn.setVisibility(View.VISIBLE); });
    }

    private void verifyPhoneNumber(){
        final String phone = getIntent().getStringExtra(Constants.PHONE_INTENT_KEY);

        FirebaseDatabase.getInstance().getReference(FirebaseHelper.DRIVERS_TABLE)
                .orderByChild(FirebaseHelper.USER_PHONE_KEY)
                .equalTo(phone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressDialog.cancel();

                        String userId = Objects.requireNonNull(new FirebaseHelper(VerificationActivity.this).getFirebaseUser()).getUid();

                        if (dataSnapshot.getValue() != null) {
                            Driver driver = dataSnapshot.child(userId).getValue(Driver.class);
                            if(driver == null) return;

                            TempStorage.setDriverInfo(driver, true);

                            Intent intent = new Intent(VerificationActivity.this, HomeActivity.class);
                            intent.putExtra(Constants.SUCCESS_KEY, "SUCCESS");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(VerificationActivity.this, SignUpActivity.class);
                            intent.putExtra(Constants.PHONE_INTENT_KEY, phone);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
                networkStatusChangeReceiver.onReceive(VerificationActivity.this, null);
                snackbar.dismiss();
            });
        }
    }
}
