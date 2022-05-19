package com.go.rider.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;

import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.util.Constants;
import com.go.rider.util.extension.AppExtensions;
import com.ortiz.touchview.TouchImageView;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class PhotoViewFragment extends DialogFragment {

    private static final String     TAG = PhotoViewFragment.class.getSimpleName();
    private Context                 context;
    private String                  photoLink;
    private TouchImageView          photoHolder;
    private AppCompatImageView      backBtn;

    public static PhotoViewFragment show(HomeActivity homeActivity, String photo){
        PhotoViewFragment fragment = new PhotoViewFragment();
        if (photo != null) {
            Bundle args = new Bundle();
            args.putString(Constants.PHOTO_BUNDLE_KEY, photo);
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_layout_photo_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), true);
        super.onViewCreated(view, savedInstanceState);

        initId(view);

        if (getArguments() == null) { dismiss(); return; }
        photoLink = getArguments().getString(Constants.PHOTO_BUNDLE_KEY);
        init();
        getArguments().remove(Constants.PHOTO_BUNDLE_KEY);
    }

    private void initId(View view) {
        photoHolder = view.findViewById(R.id.photoHolder);
        backBtn = view.findViewById(R.id.backBtn);
    }

    private void init() {
        AppExtensions.loadPhoto(photoHolder, photoLink, null, R.drawable.ic_placeholder);
        backBtn.setOnClickListener(v -> dismiss());
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
