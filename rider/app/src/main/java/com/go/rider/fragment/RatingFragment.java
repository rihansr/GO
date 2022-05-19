package com.go.rider.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.model.other.Rating;
import com.go.rider.model.trip.TripInfo;
import com.go.rider.remote.FirebaseHelper;
import com.go.rider.util.Constants;
import com.go.rider.util.CustomSnackBar;
import com.go.rider.util.extension.DateExtensions;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.AppExtensions;
import com.go.rider.wiget.CircleImageView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;

@SuppressLint("SetTextI18n")
public class RatingFragment extends DialogFragment {

    private static final String TAG = RatingFragment.class.getSimpleName();
    private Context             context;
    private CircleImageView     driverPhoto;
    private AppCompatTextView   title;
    private AppCompatRatingBar  ratingBar;
    private AppCompatEditText   notesInput;
    private AppCompatButton     ratingBtn;
    private FirebaseHelper      firebaseHelper;

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
        driverPhoto = view.findViewById(R.id.driverPhoto);
        title = view.findViewById(R.id.title);
        ratingBar = view.findViewById(R.id.ratingBar);
        notesInput = view.findViewById(R.id.notes_Input_Etxt);
        ratingBtn = view.findViewById(R.id.ratingBtn);
        firebaseHelper = new FirebaseHelper(context);
    }

    private void init(){
        if (getArguments() == null) { dismiss(); return; }

        TripInfo tripInfo = (TripInfo) getArguments().getSerializable(Constants.TRIP_INFO_INTENT_KEY);
        getArguments().remove(Constants.TRIP_INFO_INTENT_KEY);
        if (tripInfo == null) { dismiss(); return; }

        int placeHolder = tripInfo.getDriver().getGender() == null || tripInfo.getDriver().getGender().equals(AppExtensions.Gender.Male.toString()) ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;
        AppExtensions.loadPhoto(driverPhoto, tripInfo.getDriver().getProfilePhoto(), R.dimen.icon_Size_XXX_Large, placeHolder);

        title.setText(AppExtensions.getString(R.string.howWasRide) + "\t" + AppExtensions.nameFormat(tripInfo.getDriver().getName()) + "?");

        ratingBtn.setOnClickListener(v -> {
            if(!Constants.IS_INTERNET_CONNECTED){
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.network_Error, R.string.retry, CustomSnackBar.Duration.LONG).show();
                return;
            }

            DatabaseReference riderReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.DRIVERS_TABLE).child(tripInfo.getDriver().getId());
            DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE).child(tripInfo.getTripId());

            String notes = Objects.requireNonNull(notesInput.getText()).toString().trim().isEmpty() ? null : notesInput.getText().toString().trim();
            Rating rating = new Rating(TempStorage.RIDER.getId(), ratingBar.getRating(), notes, DateExtensions.currentTime());

            firebaseHelper.setData(riderReference.child(FirebaseHelper.USER_RATINGS_KEY).push().setValue(rating), null);
            firebaseHelper.setData(tripReference.child(FirebaseHelper.RIDER_RATING_KEY).setValue(ratingBar.getRating()),
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
