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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.adapter.MessageAdapter;
import com.go.rider.model.other.Report;
import com.go.rider.model.trip.TripInfo;
import com.go.rider.remote.FirebaseHelper;
import com.go.rider.util.Constants;
import com.go.rider.util.CustomSnackBar;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.AppExtensions;
import com.go.rider.util.extension.DateExtensions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

@SuppressLint("SetTextI18n")
public class ReportProfileFragment extends DialogFragment {

    private static final String TAG = ReportProfileFragment.class.getSimpleName();
    private Context             context;
    private String              driverUid = null;
    private RecyclerView        rcvIssues;
    private MessageAdapter      issueAdapter;
    private FirebaseHelper      firebaseHelper;

    public static ReportProfileFragment show(@NonNull HomeActivity homeActivity, String uid){
        ReportProfileFragment fragment = new ReportProfileFragment();
        if(uid != null){
            Bundle args = new Bundle();
            args.putString(Constants.UID_INTENT_KEY, uid);
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
        return inflater.inflate(R.layout.fragment_layout_report_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        rcvIssues = view.findViewById(R.id.rcvIssues);
        issueAdapter = new MessageAdapter(3);
        firebaseHelper = new FirebaseHelper(context);
    }

    private void init(){
        if (getArguments() == null) { dismiss(); return; }

        driverUid = getArguments().getString(Constants.UID_INTENT_KEY);
        getArguments().remove(Constants.UID_INTENT_KEY);
        if (driverUid == null) { dismiss(); return; }
        setAdapter();
    }

    private void setAdapter(){
        rcvIssues.setAdapter(issueAdapter);
        issueAdapter.setData(TempStorage.getProfileReports());
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
        report.setReportedBy(firebaseHelper.getFirebaseUser().getUid());
        report.setReportedTo(driverUid);
        report.setIssue(issue);
        report.setReportedAt(DateExtensions.currentTime());

        if(position == 5){
            dismiss();
            ReportDetailsFragment.show((HomeActivity) context, report);
            return;
        }

        DatabaseReference issueReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.REPORTS_ISSUES_TABLE);
        firebaseHelper.setData(issueReference.child(report.getId()).setValue(report),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override public void onSuccess() {
                        dismiss();
                        ReportDoneFragment.show((HomeActivity) context);
                    }
                    @Override public void onFailure() {}
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
