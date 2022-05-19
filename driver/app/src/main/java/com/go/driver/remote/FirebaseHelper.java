package com.go.driver.remote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.go.driver.activity.SignInActivity;
import com.go.driver.util.Constants;
import com.go.driver.util.extension.AppExtensions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
    public static String        REQUESTED_DRIVERS_KEY = "requestedDrivers";
    public static String        TRIP_STATUS_KEY = "tripStatus";
    public static String        TRIP_CANCELLED_KEY = "tripStatus/tripCancelled";
    public static String        TRIP_STARTED_KEY = "tripStatus/tripStarted";
    public static String        TRIP_FINISHED_KEY = "tripStatus/tripFinished";
    public static String        TRIP_DRIVER_KEY = "driver";
    public static String        TRIP_DRIVER_ID_KEY = "driver/id";
    public static String        DRIVER_RATING_KEY = "driverRating";
    public static String        TRIP_MESSAGES_KEY = "messages";

    public static String        USER_VERIFIED_KEY = "trip/verified";
    public static String        USER_AVAILABILITY_KEY = "trip/preference/available";
    public static String        TOTAL_TRIP_KEY = "trip/totalTrips";
    public static String        USER_ON_TRIP_KEY = "trip/onTrip";
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

    public void uploadPhoto(byte[] photoBytes, DatabaseReference databaseReference, AppExtensions.Photo photoType, OnPhotoUploadListener uploadListener){
        final StorageReference photoPath =  getStoragePath(photoType);
        photoPath.putBytes(photoBytes)
                .addOnSuccessListener(taskSnapshot -> photoPath.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String link = uri.toString().replaceAll("\\.", ",");
                            if (databaseReference != null) {
                                databaseReference.setValue(link).addOnSuccessListener(aVoid -> {
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

    public DatabaseReference driverReference(){
        String userId = Objects.requireNonNull(getFirebaseUser()).getUid();
        return FirebaseDatabase.getInstance().getReference(DRIVERS_TABLE).child(userId);
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

    private StorageReference getStoragePath(AppExtensions.Photo photo){
        String userId = Objects.requireNonNull(getFirebaseUser()).getUid();

        StorageReference reference = FirebaseStorage.getInstance().getReference("Drivers/" + userId + "/");

        switch (photo) {
            case PROFILE:
                return reference.child("profilePhoto");

            case COVER:
                return reference.child("coverPhoto");

            case NID:
                return reference.child("nationalId");

            case LICENSE:
                return reference.child("drivingLicense");

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
        void onSuccess(DataSnapshot data);
        void onFailure();
    }
}
