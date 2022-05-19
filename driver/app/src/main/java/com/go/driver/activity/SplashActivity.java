package com.go.driver.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.go.driver.R;
import com.go.driver.receiver.NetworkStatusChangeReceiver;
import com.go.driver.service.GoService;
import com.go.driver.util.Constants;
import com.go.driver.util.SharedPreference;
import com.go.driver.util.extension.AppExtensions;
import com.google.firebase.auth.FirebaseAuth;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class SplashActivity extends AppCompatActivity {

    private NetworkStatusChangeReceiver networkStatusChangeReceiver = new NetworkStatusChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppExtensions.fullScreenActivity(getWindow(), true);
        setContentView(R.layout.activity_splash);
        GoService.setActivity(SplashActivity.this);

        new Handler().postDelayed(this::launchNewActivity, Constants.SPLASH_TIME_OUT);
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

    private void launchNewActivity() {
        if(FirebaseAuth.getInstance().getCurrentUser() != null && new SharedPreference().isDriverLoggedIn()){
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
