package com.go.driver.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.go.driver.R;
import com.go.driver.activity.HomeActivity;
import com.go.driver.adapter.TripAdapter;
import com.go.driver.model.trip.TripInfo;
import com.go.driver.remote.FirebaseHelper;
import com.go.driver.util.Constants;
import com.go.driver.util.TempStorage;
import com.go.driver.util.extension.AppExtensions;
import com.go.driver.util.recyclerView.DividerItemDecoration;
import com.go.driver.util.recyclerView.VerticalSpaceItemDecoration;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import static com.go.driver.activity.HomeActivity.drawerLayout;

public class TripsFragment extends DialogFragment{

    private static final String TAG = TripsFragment.class.getSimpleName();

    /**
     * Toolbar
     **/
    private AppCompatTextView   title;
    private AppCompatImageView  backBtn;

    /**
     * Other
     **/
    private RecyclerView        rcvTrips;
    private TripAdapter         tripAdapter;
    private Context             context;
    private LottieAnimationView empty;
    private LottieAnimationView loader;

    public static TripsFragment show(@NonNull HomeActivity homeActivity){
        TripsFragment fragment = new TripsFragment();
        fragment.show(homeActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_TopLight_SliderAnimation);
        setRetainInstance(true);
        setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        new Handler().postDelayed(() -> drawerLayout.closeDrawer(GravityCompat.START), Constants.SLIDE_OUT_DURATION);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_items, container, false);
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
        rcvTrips = view.findViewById(R.id.rcvItems);
        empty = view.findViewById(R.id.empty);
        loader = view.findViewById(R.id.loader);
    }

    private void init(){
        title.setText(AppExtensions.getString(R.string.yourTrips));
        backBtn.setOnClickListener(v -> dismiss());

        setTripsAdapter();

        loader.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE)
                .orderByChild(FirebaseHelper.TRIP_DRIVER_ID_KEY)
                .equalTo(TempStorage.DRIVER.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<TripInfo> trips = new ArrayList<>();

                        loader.setVisibility(View.GONE);
                        if(!dataSnapshot.exists()) {
                            empty.setVisibility(View.VISIBLE);
                            tripAdapter.setTrips(trips);
                            return;
                        }


                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            TripInfo trip = snapshot.getValue(TripInfo.class);
                            if (trip == null) continue;
                            if(!trip.getTripStatus().isTripFinished()) continue;
                            trips.add(trip);
                        }

                        if(trips.size() == 0) empty.setVisibility(View.VISIBLE);

                        tripAdapter.setTrips(trips);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        empty.setVisibility(View.VISIBLE);
                        loader.setVisibility(View.GONE);
                        tripAdapter.setTrips(null);
                    }
                });
    }

    private void setTripsAdapter() {
        rcvTrips.addItemDecoration(new VerticalSpaceItemDecoration(32));
        rcvTrips.addItemDecoration(new DividerItemDecoration(context, R.drawable.shape_divider));
        tripAdapter = new TripAdapter(context);
        rcvTrips.setAdapter(tripAdapter);
        tripAdapter.setOnTripListener(tripInfo -> TripDetailsFragment.show((HomeActivity) context, tripInfo));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        drawerLayout.openDrawer(GravityCompat.START);
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
