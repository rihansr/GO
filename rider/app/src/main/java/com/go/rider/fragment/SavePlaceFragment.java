package com.go.rider.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.model.location.Place;
import com.go.rider.util.Constants;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.AppExtensions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Objects;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class SavePlaceFragment extends DialogFragment {

    private static final String     TAG = SavePlaceFragment.class.getSimpleName();

    /**
     * Toolbar
     **/
    private AppCompatTextView       title;
    private AppCompatImageView      backBtn;

    /**
     * Content
     **/
    private AppCompatTextView       placeInput;
    private AppCompatImageView      place_Home;
    private AppCompatImageView      place_Work;
    private AppCompatImageView      place_Other;
    private TextInputLayout         placeNameHolder;
    private TextInputEditText       placeNameInput;
    private AppCompatImageButton    updateBtn;

    /**
     * Other
     **/
    private Place                   place = new Place();
    private Integer                 tempId = null;
    private String                  tempTitle = null;
    private Context                 context;
    private OnPlaceUpdateListener   mOnPlaceUpdateListener;

    public static SavePlaceFragment show(HomeActivity homeActivity, Place place){
        SavePlaceFragment fragment = new SavePlaceFragment();
        if (place != null) {
            Bundle args = new Bundle();
            args.putSerializable(Constants.PLACE_BUNDLE_KEY, place);
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
        setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_save_place, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), true);
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        if (getArguments() == null) { dismiss(); return; }
        Place getPlace = (Place) getArguments().getSerializable(Constants.PLACE_BUNDLE_KEY);
        if (getPlace == null) { dismiss(); return; }
        place = getPlace;
        init();
        getArguments().remove(Constants.PLACE_BUNDLE_KEY);
    }

    private void idSetup(View view) {
        title = view.findViewById(R.id.title);
        backBtn = view.findViewById(R.id.leftBtn);

        placeInput = view.findViewById(R.id.place_Input_Etxt);
        place_Home = view.findViewById(R.id.place_Home);
        place_Work = view.findViewById(R.id.place_Work);
        place_Other = view.findViewById(R.id.place_Other);
        placeNameHolder = view.findViewById(R.id.placeNameHolder);
        placeNameInput = view.findViewById(R.id.placeName_Input_Etxt);
        updateBtn = view.findViewById(R.id.updateBtn);
    }

    private void init(){
        title.setText(AppExtensions.getString(R.string.savePlace));
        placeInput.setText(place.getAddress());
        placeNameInput.setText(place.getTitle());
        updateBtn.setVisibility(isGivenDataOk() ? View.VISIBLE : View.GONE);
        tempId = place.getId();
        tempTitle = place.getTitle();

        switch (place.getId()){
            case 0:
                updatePlaceInfo(0, "Home", R.drawable.ic_place_home, View.GONE);
                updateUI(false, new AppCompatImageView[]{place_Home, place_Work, place_Other});
                break;

            case 1:
                updatePlaceInfo(1, "Work", R.drawable.ic_place_work, View.GONE);
                updateUI(false, new AppCompatImageView[]{place_Work, place_Other, place_Home});
                break;

            default:
                updatePlaceInfo(tempId != null
                        ?
                        tempId
                        :
                        TempStorage.RIDER.getTrip().getPreference().getSavedPlaces().size(), place.getTitle(), R.drawable.ic_place_other, View.VISIBLE);
                updateUI(true, new AppCompatImageView[]{place_Other, place_Home, place_Work});
        }

        place_Home.setOnClickListener(v -> {
            updatePlaceInfo(0, "Home", R.drawable.ic_place_home, View.GONE);
            updateUI(true, new AppCompatImageView[]{(AppCompatImageView) v, place_Work, place_Other});
        });

        place_Work.setOnClickListener(v -> {
            updatePlaceInfo(1, "Work", R.drawable.ic_place_work, View.GONE);
            updateUI(true, new AppCompatImageView[]{(AppCompatImageView) v, place_Other, place_Home});
        });

        place_Other.setOnClickListener(v -> {
            updatePlaceInfo(tempId != null
                    ?
                    tempId
                    :
                    TempStorage.RIDER.getTrip().getPreference().getSavedPlaces().size(), tempTitle, R.drawable.ic_place_other, View.VISIBLE);

            updateUI(true, new AppCompatImageView[]{(AppCompatImageView) v, place_Home, place_Work});
        });

        placeInput.setOnClickListener(v ->
                DraggableMapFragment.show((HomeActivity) context, place)
                        .setOnPlaceSelectListener(updatedPlace -> {
                            place = updatedPlace;
                            placeInput.setText(place.getAddress());
                        })
        );

        updateBtn.setOnClickListener(v -> {
            if (place.getId() > 1) {
                String title = Objects.requireNonNull(placeNameInput.getText()).toString().trim();
                place.setTitle(title);
            }

            if(mOnPlaceUpdateListener != null) mOnPlaceUpdateListener.onUpdate(place);
            dismiss();
        });

        backBtn.setOnClickListener(v -> dismiss());

        placeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateBtn.setVisibility(isGivenDataOk() ? View.VISIBLE : View.GONE);
            }
        });

        placeNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateBtn.setVisibility(isGivenDataOk() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void updatePlaceInfo(int id, String title, int icon, int visibility){
        place.setId(id);
        place.setTitle(title);
        place.setIcon(icon);
        placeNameInput.setText(title);
        placeNameHolder.setVisibility(visibility);
    }

    private void updateUI(boolean buttonsEnable, AppCompatImageView[] views) {
        views[0].setEnabled(buttonsEnable);
        views[0].setColorFilter(ContextCompat.getColor(context, R.color.icon_Color_Light), android.graphics.PorterDuff.Mode.SRC_IN);
        views[0].setBackgroundTintList(null);

        views[1].setEnabled(buttonsEnable);
        views[1].setColorFilter(ContextCompat.getColor(context, R.color.icon_Color_Gray), android.graphics.PorterDuff.Mode.SRC_IN);
        views[1].setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSmokeWhite));

        views[2].setEnabled(buttonsEnable);
        views[2].setColorFilter(ContextCompat.getColor(context, R.color.icon_Color_Gray), android.graphics.PorterDuff.Mode.SRC_IN);
        views[2].setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorSmokeWhite));
    }

    private boolean isGivenDataOk(){
        String place = Objects.requireNonNull(placeInput.getText()).toString().trim();
        String placeName = Objects.requireNonNull(placeNameInput.getText()).toString().trim();

        return !TextUtils.isEmpty(place) && !TextUtils.isEmpty(placeName);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(Objects.requireNonNull(getActivity()), getTheme()) {

            /**
             * Hide soft keyboard when click outside
             **/
            @Override
            public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View v = getCurrentFocus();
                    if (v instanceof AppCompatEditText) {
                        Rect outRect = new Rect();
                        v.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            v.clearFocus();
                            InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null)
                                imm.hideSoftInputFromWindow(Objects.requireNonNull(getView()).getWindowToken(), 0);
                        }
                    }
                }
                return super.dispatchTouchEvent(event);
            }
        };
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

    public void setOnPlaceUpdateListener(OnPlaceUpdateListener mOnPlaceUpdateListener) {
        this.mOnPlaceUpdateListener = mOnPlaceUpdateListener;
    }

    public interface OnPlaceUpdateListener {
        void onUpdate(Place place);
    }
}
