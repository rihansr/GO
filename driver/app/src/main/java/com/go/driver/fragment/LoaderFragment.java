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

public class LoaderFragment extends DialogFragment{

    private static final String     TAG = LoaderFragment.class.getSimpleName();

    public static LoaderFragment show(@NonNull HomeActivity homeActivity){
        LoaderFragment fragment = new LoaderFragment();
        fragment.show(homeActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_TopLight_FadeAnimation);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_custom_loader, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), true);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
