package com.go.driver.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.go.driver.R;
import com.go.driver.activity.HomeActivity;
import com.go.driver.util.extension.AppExtensions;

public class ReportDoneFragment extends DialogFragment {

    private static final String TAG = ReportDoneFragment.class.getSimpleName();

    public static ReportDoneFragment show(@NonNull HomeActivity homeActivity){
        ReportDoneFragment fragment = new ReportDoneFragment();
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
        return inflater.inflate(R.layout.fragment_layout_report_done, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view){
        view.findViewById(R.id.doneBtn).setOnClickListener(v -> dismiss());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
