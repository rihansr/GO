package com.go.driver.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import com.go.driver.R;
import com.go.driver.activity.HomeActivity;
import com.go.driver.model.other.Rating;
import com.go.driver.model.trip.TripInfo;
import com.go.driver.remote.FirebaseHelper;
import com.go.driver.util.Constants;
import com.go.driver.util.CustomSnackBar;
import com.go.driver.util.extension.DateExtensions;
import com.go.driver.util.TempStorage;
import com.go.driver.util.extension.AppExtensions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RatingFragment extends DialogFragment {

    private static final String TAG = RatingFragment.class.getSimpleName();
    private AppCompatTextView   tripFare;
    private AppCompatTextView   riderName;
    private AppCompatRatingBar  ratingBar;
    private AppCompatButton     ratingBtn;
    private FirebaseHelper      firebaseHelper;
    private Context             context;

    public static RatingFragment show(@NonNull HomeActivity homeActivity, TripInfo tripInfo){
        RatingFragment fragment = new RatingFragment();
        if(tripInfo != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.TRIP_INFO_INTENT_KEY, tripInfo);
            fragment.setArguments(args);
        }
        fragment.show(homeActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        AppExtensions.halfScreenDialog(getDialog());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_rating, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        tripFare = view.findViewById(R.id.tripFare);
        riderName = view.findViewById(R.id.riderName);
        ratingBar = view.findViewById(R.id.ratingBar);
        ratingBtn = view.findViewById(R.id.ratingBtn);
        firebaseHelper = new FirebaseHelper(context);
    }

    private void init(){
        if (getArguments() == null) { dismiss(); return; }

        TripInfo tripInfo = (TripInfo) getArguments().getSerializable(Constants.TRIP_INFO_INTENT_KEY);
        getArguments().remove(Constants.TRIP_INFO_INTENT_KEY);
        if (tripInfo == null) { dismiss(); return; }

        tripFare.setText(AppExtensions.decimalFormat(tripInfo.getFare().getTotalFare(), "#.#", false));
        riderName.setText(AppExtensions.nameFormat(tripInfo.getRider().getName()));

        ratingBtn.setOnClickListener(v -> {
            if(!Constants.IS_INTERNET_CONNECTED){
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.network_Error, R.string.retry, CustomSnackBar.Duration.LONG).show();
                return;
            }

            DatabaseReference riderReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.RIDERS_TABLE).child(tripInfo.getRider().getId());
            DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE).child(tripInfo.getTripId());

            Rating rating = new Rating(TempStorage.DRIVER.getId(), ratingBar.getRating(), DateExtensions.currentTime());

            firebaseHelper.setData(riderReference.child(FirebaseHelper.USER_RATINGS_KEY).push().setValue(rating), null);
            firebaseHelper.setData(tripReference.child(FirebaseHelper.DRIVER_RATING_KEY).setValue(ratingBar.getRating()),
                    new FirebaseHelper.OnFirebaseUpdateListener() {
                        @Override public void onSuccess() { dismiss(); }
                        @Override public void onFailure() { dismiss(); }
                    });
        });
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
