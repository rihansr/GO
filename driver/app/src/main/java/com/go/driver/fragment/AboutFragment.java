package com.go.driver.fragment;

import android.annotation.SuppressLint;
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
import com.go.driver.BuildConfig;
import com.go.driver.R;
import com.go.driver.activity.HomeActivity;
import com.go.driver.util.Constants;
import com.go.driver.util.extension.AppExtensions;
import static com.go.driver.activity.HomeActivity.drawerLayout;

@SuppressLint("SetTextI18n")
public class AboutFragment extends DialogFragment{

    private static final String TAG = AboutFragment.class.getSimpleName();

    /**
     * Toolbar
     **/
    private AppCompatTextView   title;
    private AppCompatImageView  backBtn;

    /**
     * Other
     **/
    private AppCompatTextView   rideSharingTv;
    private AppCompatTextView   versionTv;

    public static AboutFragment show(@NonNull HomeActivity homeActivity){
        AboutFragment fragment = new AboutFragment();
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
        return inflater.inflate(R.layout.fragment_layout_about, container, false);
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
        rideSharingTv = view.findViewById(R.id.rideSharingTv);
        versionTv = view.findViewById(R.id.versionTv);
    }

    private void init(){
        title.setText(AppExtensions.getString(R.string.about));
        backBtn.setOnClickListener(v -> dismiss());
        versionTv.setText("V\t"+ BuildConfig.VERSION_NAME);
        AppExtensions.doGradientText(rideSharingTv);
        AppExtensions.doGradientText(versionTv);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
