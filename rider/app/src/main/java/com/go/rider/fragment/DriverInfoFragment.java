package com.go.rider.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.adapter.MessageAdapter;
import com.go.rider.model.other.Rating;
import com.go.rider.model.trip.TripInfo;
import com.go.rider.model.user.Driver;
import com.go.rider.remote.PermissionManager;
import com.go.rider.util.Constants;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.AppExtensions;
import com.go.rider.util.extension.DateExtensions;
import com.go.rider.wiget.CircleImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressLint("SetTextI18n")
public class DriverInfoFragment extends DialogFragment implements HomeActivity.OnDismissListener{

    private static final String TAG = DriverInfoFragment.class.getSimpleName();

    /**
     * Toolbar
     **/
    private AppCompatImageView  backBtn;
    private AppCompatImageView  callBtn;
    private AppCompatImageView  reportBtn;

    private AppCompatImageView  coverPhoto;
    private CircleImageView     profilePhoto;
    private AppCompatTextView   driverName;
    private AppCompatTextView   vehicleModel;
    private AppCompatTextView   totalTrips;
    private AppCompatTextView   driverRating;
    private AppCompatTextView   period;
    private LinearLayoutCompat  notesPanel;
    private RecyclerView        rcvNotes;
    private MessageAdapter      notesAdapter;
    private Activity            activity;
    private Context             context;

    public static DriverInfoFragment show(@NonNull HomeActivity homeActivity, Driver driverInfo, TripInfo tripInfo){
        DriverInfoFragment fragment = new DriverInfoFragment();
        if(driverInfo != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.DRIVER_INFO_INTENT_KEY, driverInfo);
            if(tripInfo != null) args.putSerializable(Constants.TRIP_INFO_INTENT_KEY, tripInfo);
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
        return inflater.inflate(R.layout.fragment_layout_driver_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), true);
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        backBtn = view.findViewById(R.id.back_Btn);
        callBtn = view.findViewById(R.id.call_Btn);
        reportBtn = view.findViewById(R.id.report_Btn);
        coverPhoto = view.findViewById(R.id.cover_Photo);
        profilePhoto = view.findViewById(R.id.profile_Photo);
        driverName = view.findViewById(R.id.driverName);
        vehicleModel = view.findViewById(R.id.vehicleModel);
        totalTrips = view.findViewById(R.id.totalTrips);
        driverRating = view.findViewById(R.id.rating);
        period = view.findViewById(R.id.period);
        notesPanel = view.findViewById(R.id.notesPanel);
        rcvNotes = view.findViewById(R.id.rcvNotes);
    }

    private void init(){
        if (getArguments() == null) { dismiss(); return; }

        Driver driver = (Driver) getArguments().getSerializable(Constants.DRIVER_INFO_INTENT_KEY);
        getArguments().remove(Constants.DRIVER_INFO_INTENT_KEY);
        if (driver == null) { dismiss(); return; }

        ((HomeActivity) activity).setOnDismissListener(this);

        TripInfo tripInfo = (TripInfo) getArguments().getSerializable(Constants.TRIP_INFO_INTENT_KEY);
        if(tripInfo != null) getArguments().remove(Constants.TRIP_INFO_INTENT_KEY);

        backBtn.setOnClickListener(v -> dismiss());

        reportBtn.setVisibility(tripInfo == null ? View.VISIBLE : View.GONE);
        reportBtn.setOnClickListener(view -> ReportProfileFragment.show((HomeActivity) context, driver.getId()));

        callBtn.setVisibility(tripInfo != null ? View.VISIBLE : View.GONE);
        callBtn.setOnClickListener(v -> {
            if(tripInfo == null) return;
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + driver.getPhone()));

            if (!new PermissionManager(PermissionManager.Permission.PHONE, true, response -> startActivity(callIntent) ).isGranted()) return;

            AlertDialogFragment.show((HomeActivity)context, driver.getPhone(), null, R.string.cancel, R.string.call)
                    .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                        @Override
                        public void onLeftButtonClick() {}

                        @Override
                        public void onRightButtonClick() {
                            startActivity(callIntent);
                        }
                    });
        });

        AppExtensions.loadPhoto(coverPhoto, driver.getCoverPhoto(), null, R.drawable.shape_placeholder);

        int placeHolder = driver.getGender() == null || driver.getGender().equals(AppExtensions.Gender.Male.toString()) ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;
        AppExtensions.loadPhoto(profilePhoto, driver.getProfilePhoto(), R.dimen.icon_Size_XXXX_Large, placeHolder);

        profilePhoto.setOnClickListener(v -> PhotoViewFragment.show((HomeActivity) context, driver.getProfilePhoto()));

        driverName.setText(driver.getName());
        vehicleModel.setText(tripInfo != null ? tripInfo.getVehicleType().getModel() : driver.getVehicleInfo().getVehicleType().getModel());
        totalTrips.setText(String.valueOf(driver.getTrip().getTotalTrips()));

        setNotesAdapter();

        List<String> notes = new ArrayList<>();

        if (driver.getTrip().getRatings() != null) {
            int ratingCount = 0; double totalRating = 0;

            for (Map.Entry<String, Rating> entry :  driver.getTrip().getRatings().entrySet()) {
                ratingCount++;
                Rating rating = entry.getValue();
                totalRating = totalRating + rating.getRating();
                if(rating.getNotes() != null) notes.add(rating.getNotes());
            }

            if(ratingCount == 0 || totalRating == 0) driverRating.setText(AppExtensions.decimalFormat(0, "0.0", true));
            else driverRating.setText(AppExtensions.decimalFormat(totalRating / ratingCount, "0.0", true));
        }
        else driverRating.setText(AppExtensions.decimalFormat(0, "0.0", true));

        notesPanel.setVisibility(notes.size() != 0 ? View.VISIBLE : View.GONE);
        if(notes.size() != 0) notesAdapter.setData(notes);

        period.setText(String.valueOf(new DateExtensions(driver.getRegisteredAt()).getYears()));
    }

    private void setNotesAdapter() {
        notesAdapter = new MessageAdapter(1);
        rcvNotes.setAdapter(notesAdapter);
    }

    @Override
    public void onDismiss() {
        dismiss();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        this.activity = (Activity) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
