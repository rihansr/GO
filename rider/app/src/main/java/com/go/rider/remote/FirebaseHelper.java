package com.go.rider.remote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.go.rider.activity.SignInActivity;
import com.go.rider.util.Constants;
import com.go.rider.util.extension.AppExtensions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class FirebaseHelper {

    /**
     * Firebase Table
     **/
    public static final String  DRIVERS_TABLE = "Drivers";
    public static final String  RIDERS_TABLE = "Riders";
    public static final String  VEHICLE_TYPES_TABLE = "VehicleTypes";
    public static final String  TRIP_REQUESTS_TABLE = "TripRequests";
    public static final String  NOTIFICATIONS_TABLE = "Notifications";
    public static final String  REPORTS_ISSUES_TABLE = "Reports&Issues";

    /**
     * Database Child Keys
     **/
    public static String        USER_PHONE_KEY = "phone";
    public static String        USER_TOKEN_KEY = "token";
    public static String        USER_ACTIVE_KEY = "active";
    public static String        USER_LOCATION_KEY = "location";

    public static String        TRIP_KEY = "trip";
    public static String        TRIP_CANCELLED_KEY = "tripStatus/tripCancelled";
    public static String        TRIP_RIDER_ID_KEY = "rider/id";
    public static String        RIDER_RATING_KEY = "riderRating";
    public static String        TRIP_MESSAGES_KEY = "messages";
    public static String        VEHICLE_AVAILABILITY_KEY = "available";

    public static String        USER_VERIFIED_KEY = "trip/verified";
    public static String        USER_AVAILABILITY_KEY = "trip/preference/available";
    public static String        USER_ON_TRIP_KEY = "trip/onTrip";
    public static String        USER_SAVED_PLACES_KEY = "trip/preference/savedPlaces";
    public static String        USER_RATINGS_KEY = "trip/ratings";

    /**
     * Other
     **/
    private Context             context;
    private Activity            activity;

    public FirebaseHelper(Context context) {
        this.context = context;
        this.activity = (Activity) context;
    }

    public void uploadPhoto(byte[] photoBytes, DatabaseReference databaseReference, AppExtensions.Photo photoType, OnPhotoUploadListener uploadListener) {
        final StorageReference photoPath = getStoragePath(photoType);

        photoPath.putBytes(photoBytes)
                .addOnSuccessListener(taskSnapshot -> photoPath.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String link = uri.toString().replaceAll("\\.", ",");
                            if (databaseReference != null) {
                                databaseReference.setValue(link)
                                        .addOnSuccessListener(aVoid -> {
                                            if (uploadListener != null) uploadListener.onSuccess(link);
                                        });
                            }
                            else if (uploadListener != null) uploadListener.onSuccess(link);
                        }))
                .addOnFailureListener(ex -> {
                    Log.i(Constants.TAG, "Uploading " + photoType.toString() + " Photo Exception: " + ex.getMessage());
                    if (uploadListener != null) uploadListener.onFailure();
                    ex.printStackTrace();
                })
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if (uploadListener != null) uploadListener.onProgress(progress);
                });
    }

    public void setData(Task<Void> task, OnFirebaseUpdateListener updateListener) {
        task.addOnSuccessListener(aVoid -> {
                if (updateListener != null) updateListener.onSuccess();
            })
            .addOnFailureListener(ex -> {
                Log.i(Constants.TAG, "Database Exception: " + ex.getMessage());
                if (updateListener != null) updateListener.onFailure();
            });
    }

    public FirebaseUser getFirebaseUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Intent intent = new Intent(context, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return null;
        }
        else return user;
    }

    public DatabaseReference riderReference(){
        String userId = Objects.requireNonNull(getFirebaseUser()).getUid();
        return FirebaseDatabase.getInstance().getReference(RIDERS_TABLE).child(userId);
    }

    private StorageReference getStoragePath(AppExtensions.Photo photo){
        String userId = Objects.requireNonNull(getFirebaseUser()).getUid();
        StorageReference reference = FirebaseStorage.getInstance().getReference(RIDERS_TABLE + "/" + userId + "/");

        switch (photo) {
            case PROFILE:
                return reference.child("profilePhoto");

            case COVER:
                return reference.child("coverPhoto");

            default:
                return reference.child("other");
        }
    }

    public interface OnPhotoUploadListener {
        void onSuccess(String photoLink);
        void onFailure();
        void onProgress(double progress);
    }

    public interface OnFirebaseUpdateListener {
        void onSuccess();
        void onFailure();
    }

    public interface OnFirebaseDataListener {
        void onData(DataSnapshot data, boolean isSuccess);
        void onFailure();
    }
}
