package com.go.driver.fragment;

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

import com.go.driver.R;
import com.go.driver.activity.HomeActivity;
import com.go.driver.model.other.Report;
import com.go.driver.remote.FirebaseHelper;
import com.go.driver.util.Constants;
import com.go.driver.util.CustomSnackBar;
import com.go.driver.util.extension.AppExtensions;
import com.go.driver.util.extension.DateExtensions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

@SuppressLint("SetTextI18n")
public class ReportDetailsFragment extends DialogFragment {

    private static final String TAG = ReportDetailsFragment.class.getSimpleName();
    private Context             context;
    private AppCompatTextView   title;
    private AppCompatEditText   detailsInput;
    private AppCompatButton     submitBtn;
    private FirebaseHelper      firebaseHelper;

    public static ReportDetailsFragment show(@NonNull HomeActivity homeActivity, Report report){
        ReportDetailsFragment fragment = new ReportDetailsFragment();
        if(report != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.REPORT_INTENT_KEY, report);
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
        return inflater.inflate(R.layout.fragment_layout_report_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        title = view.findViewById(R.id.title);
        detailsInput = view.findViewById(R.id.notes_Input_Etxt);
        submitBtn = view.findViewById(R.id.submitBtn);
        firebaseHelper = new FirebaseHelper(context);
    }

    private void init(){
        if (getArguments() == null) { dismiss(); return; }

        Report report = (Report) getArguments().getSerializable(Constants.REPORT_INTENT_KEY);
        getArguments().remove(Constants.REPORT_INTENT_KEY);
        if (report == null) { dismiss(); return; }

        title.setText(report.getIssue());

        submitBtn.setOnClickListener(v -> {
            if(!Constants.IS_INTERNET_CONNECTED){
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.network_Error, R.string.retry, CustomSnackBar.Duration.LONG).show();
                return;
            }

            String details = Objects.requireNonNull(detailsInput.getText()).toString().trim().isEmpty() ? null : detailsInput.getText().toString().trim();
            report.setDetails(details);
            report.setReportedAt(DateExtensions.currentTime());

            DatabaseReference issueReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.REPORTS_ISSUES_TABLE);
            firebaseHelper.setData(issueReference.child(report.getId()).setValue(report),
                    new FirebaseHelper.OnFirebaseUpdateListener() {
                        @Override public void onSuccess() {
                            dismiss();
                            ReportDoneFragment.show((HomeActivity) context);
                        }
                        @Override public void onFailure() {}
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
