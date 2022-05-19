package com.go.rider.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.DialogFragment;
import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.util.extension.AppExtensions;

@SuppressLint("ClickableViewAccessibility")
public class PhotoActionFragment extends DialogFragment {

    private static final String TAG = PhotoActionFragment.class.getSimpleName();
    private LinearLayoutCompat  actionCapture;
    private LinearLayoutCompat  actionPick;
    private OnActionListener    mOnActionListener;

    public static PhotoActionFragment show(@NonNull AppCompatActivity appCompatActivity){
        PhotoActionFragment fragment = new PhotoActionFragment();
        fragment.show(appCompatActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    public static PhotoActionFragment show(@NonNull HomeActivity homeActivity){
        PhotoActionFragment fragment = new PhotoActionFragment();
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
        return inflater.inflate(R.layout.fragment_layout_photo_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        actionCapture = view.findViewById(R.id.actionCamera);
        actionPick = view.findViewById(R.id.actionGallery);
    }

    private void init(){
        actionCapture.setOnClickListener(v -> {
            if (mOnActionListener != null) mOnActionListener.onAction(getDialog(), true);
        });

        actionPick.setOnClickListener(v -> {
            if (mOnActionListener != null) mOnActionListener.onAction(getDialog(), false);
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void setOnActionListener(OnActionListener mOnActionListener) {
        this.mOnActionListener = mOnActionListener;
    }

    public interface OnActionListener {
        void onAction(Dialog dialog, boolean isCapture);
    }
}
