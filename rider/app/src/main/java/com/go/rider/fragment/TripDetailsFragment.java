package com.go.rider.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.adapter.MessageAdapter;
import com.go.rider.model.other.Report;
import com.go.rider.model.trip.TripInfo;
import com.go.rider.model.user.Driver;
import com.go.rider.remote.FirebaseHelper;
import com.go.rider.util.Constants;
import com.go.rider.util.CustomSnackBar;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.DateExtensions;
import com.go.rider.util.extension.AppExtensions;
import com.go.rider.wiget.CircleImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@SuppressLint("SetTextI18n")
public class TripDetailsFragment extends DialogFragment{

    private static final String     TAG = TripDetailsFragment.class.getSimpleName();

    /**
     * Toolbar
     **/
    private AppCompatTextView       title;
    private AppCompatImageView      backBtn;

    /**
     * Other
     **/
    private AppCompatTextView       tripDate;
    private AppCompatTextView       tripId;
    private AppCompatTextView       greetingTv;
    private AppCompatTextView       pickUpLocation;
    private AppCompatTextView       dropOffLocation;
    private AppCompatTextView       tripFare;
    private AppCompatTextView       paymentType;
    private AppCompatTextView       subtotal;
    private AppCompatTextView       promo;
    private AppCompatTextView       total;
    private CircleImageView         driverPhoto;
    private AppCompatTextView       driverName;
    private AppCompatRatingBar      riderRating;
    private AppCompatTextView       vehicleModel;
    private RecyclerView            rcvIssues;
    private MessageAdapter          issueAdapter;
    private FirebaseHelper          firebaseHelper;
    private TripInfo                tripInfo = null;
    private Context                 context;

    public static TripDetailsFragment show(@NonNull HomeActivity homeActivity, TripInfo tripInfo){
        TripDetailsFragment fragment = new TripDetailsFragment();
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
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_TopLight_FadeAnimation);
        setRetainInstance(true);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_trip_details, container, false);
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

        tripDate = view.findViewById(R.id.tripDate);
        tripId = view.findViewById(R.id.tripId);

        greetingTv = view.findViewById(R.id.greetingTv);
        pickUpLocation = view.findViewById(R.id.pickUpLocation);
        dropOffLocation = view.findViewById(R.id.dropOffLocation);

        tripFare = view.findViewById(R.id.tripFare);
        paymentType = view.findViewById(R.id.paymentType);
        subtotal = view.findViewById(R.id.subtotal);
        promo = view.findViewById(R.id.promo);
        total = view.findViewById(R.id.total);

        driverPhoto = view.findViewById(R.id.driverPhoto);
        driverName = view.findViewById(R.id.driverName);
        riderRating = view.findViewById(R.id.riderRating);
        vehicleModel = view.findViewById(R.id.vehicleModel);

        rcvIssues = view.findViewById(R.id.rcvIssues);
        firebaseHelper = new FirebaseHelper(context);
        issueAdapter = new MessageAdapter(2);
    }

    private void init(){
        if (getArguments() == null) { dismiss(); return; }

        tripInfo = (TripInfo) getArguments().getSerializable(Constants.TRIP_INFO_INTENT_KEY);
        getArguments().remove(Constants.TRIP_INFO_INTENT_KEY);
        if (tripInfo == null) { dismiss(); return; }

        title.setText(AppExtensions.getString(R.string.tripDetails));
        backBtn.setOnClickListener(v -> dismiss());

        tripDate.setText(new DateExtensions(context, tripInfo.getPickUpTime()).tripDateFormat());
        tripId.setText(AppExtensions.getString(R.string.tripId) + tripInfo.getTripId());

        String name = tripInfo.getRider().getName();
        String message = "Thanks " + name + ", for choosing GO " + tripInfo.getVehicleType().getType();
        int nameIndex = message.indexOf(name);
        Spannable greeting = new SpannableString(message);
        greeting.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorBlack)), nameIndex, (nameIndex + name.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        greetingTv.setText(greeting);
        pickUpLocation.setText(tripInfo.getPickUpLocation().getAddress());
        dropOffLocation.setText(tripInfo.getDropOffLocation().getAddress());

        tripFare.setText(AppExtensions.decimalFormat(tripInfo.getFare().getTotalFare(), "#.##", false));
        paymentType.setText(tripInfo.getPaymentType());
        subtotal.setText(AppExtensions.decimalFormat(tripInfo.getFare().getTripFare(), "#.##", false));
        promo.setText(AppExtensions.decimalFormat(tripInfo.getFare().getPromo() * -1, "#.##", false));
        total.setText(AppExtensions.decimalFormat(tripInfo.getFare().getTotalFare(), "#.##", false));

        int placeHolder = tripInfo.getDriver().getGender() == null || tripInfo.getDriver().getGender().equals(AppExtensions.Gender.Male.toString()) ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;
        AppExtensions.loadPhoto(driverPhoto, tripInfo.getDriver().getProfilePhoto(), R.dimen.icon_Size_Large, placeHolder);

        driverPhoto.setOnLongClickListener(view -> {
            ReportProfileFragment.show((HomeActivity) context, tripInfo.getDriver().getId());
            return false;
        });

        vehicleModel.setText(tripInfo.getVehicleType().getModel());
        driverName.setText(tripInfo.getDriver().getName());
        riderRating.setRating(tripInfo.getRiderRating() == null ? 0 : tripInfo.getRiderRating());

        FirebaseDatabase.getInstance().getReference(FirebaseHelper.DRIVERS_TABLE)
                .child(tripInfo.getDriver().getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.exists()) return;
                        Driver driver = snapshot.getValue(Driver.class);
                        if(driver == null) return;

                        driverName.setText(driver.getName());

                        int placeHolder = driver.getGender() == null || driver.getGender().equals(AppExtensions.Gender.Male.toString()) ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;
                        AppExtensions.loadPhoto(driverPhoto, driver.getProfilePhoto(), R.dimen.icon_Size_Large, placeHolder);
                        driverPhoto.setOnClickListener(v -> DriverInfoFragment.show((HomeActivity) context, driver, null));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        setIssueAdapter();
    }

    private void setIssueAdapter() {
        rcvIssues.setAdapter(issueAdapter);
        issueAdapter.setData(TempStorage.getTripIssues());
        issueAdapter.setOnMessageListener(this::doReport);
    }

    private void doReport(int position, String issue) {
        if(!Constants.IS_INTERNET_CONNECTED){
            new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.network_Error, R.string.retry, CustomSnackBar.Duration.LONG).show();
            return;
        }

        Report report = new Report();
        report.setId("RP-" + AppExtensions.getRandomCode(8));
        report.setReportFrom("RIDER");
        report.setTripId(tripInfo.getTripId());
        report.setReportedBy(firebaseHelper.getFirebaseUser().getUid());
        report.setIssue(issue);

        ReportDetailsFragment.show((HomeActivity) context, report);
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
