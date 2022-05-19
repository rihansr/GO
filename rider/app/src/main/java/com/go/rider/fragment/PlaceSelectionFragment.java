package com.go.rider.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.adapter.SavePlaceAdapter;
import com.go.rider.adapter.SearchPlaceAdapter;
import com.go.rider.model.location.Place;
import com.go.rider.util.Constants;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.AppExtensions;
import java.util.Objects;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class PlaceSelectionFragment extends DialogFragment {

    private static final String     TAG = PlaceSelectionFragment.class.getSimpleName();

    /**
     * Toolbar
     **/
    private AppCompatImageView      backBtn;

    /**
     * Content
     **/
    private AppCompatImageView      from;
    private AppCompatEditText       pickUpInput;
    private AppCompatImageView      to;
    private AppCompatEditText       dropOffInput;
    private RecyclerView            searchPlaces;
    private RecyclerView            savePlaces;
    private SavePlaceAdapter        savePlaceAdapter;
    private SearchPlaceAdapter      searchPlaceAdapter;
    private AppCompatTextView       setLocation;

    /**
     * Other
     **/
    private Place                   pickUp = new Place();
    private Place                   dropOff = new Place();
    private boolean                 isTyping = false;
    private boolean                 isPickUpPlace = false;
    private Context                 context;
    private OnPlaceListener         mOnPlaceListener;

    public static PlaceSelectionFragment show(HomeActivity homeActivity, Place pickUp, Place dropOff){
        PlaceSelectionFragment fragment = new PlaceSelectionFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.PICKUP_PLACE_BUNDLE_KEY, pickUp);
        args.putSerializable(Constants.DROPOFF_PLACE_BUNDLE_KEY, dropOff);
        fragment.setArguments(args);
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
        return inflater.inflate(R.layout.fragment_layout_place_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), true);
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        if (getArguments() == null) { dismiss(); return; }
        Place getPickUpPlace = (Place) getArguments().getSerializable(Constants.PICKUP_PLACE_BUNDLE_KEY);
        if(getPickUpPlace != null) pickUp = getPickUpPlace;
        getArguments().remove(Constants.PICKUP_PLACE_BUNDLE_KEY);

        Place getDropOffPlace = (Place) getArguments().getSerializable(Constants.DROPOFF_PLACE_BUNDLE_KEY);
        if(getDropOffPlace != null) dropOff = getDropOffPlace;
        getArguments().remove(Constants.DROPOFF_PLACE_BUNDLE_KEY);

        init();
    }

    private void idSetup(View view) {
        backBtn = view.findViewById(R.id.backBtn);

        from = view.findViewById(R.id.from);
        pickUpInput = view.findViewById(R.id.pickUp_Input_Etxt);
        to = view.findViewById(R.id.to);
        dropOffInput = view.findViewById(R.id.dropOff_Input_Etxt);

        searchPlaces = view.findViewById(R.id.searchPlaces);
        savePlaces = view.findViewById(R.id.savePlaces);
        setLocation = view.findViewById(R.id.setLocation);
    }

    private void init(){
        if(pickUp.getAddress() != null) pickUpInput.setText(pickUp.getAddress());
        if(dropOff.getAddress() != null) dropOffInput.setText(dropOff.getAddress());

        setPlacesAdapter();

        updateUI(pickUpInput, from);
        updateUI(dropOffInput, to);

        setLocation.setOnClickListener(v ->
                DraggableMapFragment.show((HomeActivity) context, new Place()).setOnPlaceSelectListener(this::confirmSelectedPlace)
        );

        backBtn.setOnClickListener(v -> dismiss());
    }

    private void updateUI(AppCompatEditText inputField, AppCompatImageView point){
        inputField.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                Objects.requireNonNull(inputField.getText()).toString().trim().isEmpty() ? 0 : R.drawable.ic_clear,
                0
        );

        inputField.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= inputField.getRight() - inputField.getTotalPaddingRight()) {
                    inputField.setText(null);
                    return true;
                }
            }
            return false;
        });

        inputField.setOnFocusChangeListener((v, hasFocus) -> {
            isTyping = hasFocus;
            if(hasFocus) isPickUpPlace = point == from;
            point.setColorFilter(ContextCompat.getColor(context, hasFocus ? R.color.colorLightGray : R.color.icon_Color_Light), android.graphics.PorterDuff.Mode.SRC_IN);
        });

        inputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().isEmpty()){
                    inputField.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    searchPlaceAdapter.clearAll();
                    return;
                }
                inputField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear, 0);
                if(!isTyping) return;
                searchPlaceAdapter.getFilter().filter(s.toString());
            }
        });
    }

    private void setPlacesAdapter() {
        searchPlaces.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        searchPlaceAdapter = new SearchPlaceAdapter(context);
        searchPlaces.setAdapter(searchPlaceAdapter);
        searchPlaceAdapter.setOnPlaceSelectListener(this::confirmSelectedPlace);

        savePlaces.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        savePlaceAdapter = new SavePlaceAdapter(context, 1);
        savePlaces.setAdapter(savePlaceAdapter);

        savePlaceAdapter.setPlaces(TempStorage.RIDER.getTrip().getPreference().getSavedPlaces());

        savePlaceAdapter.setOnPlaceListener(new SavePlaceAdapter.OnPlaceListener() {
            @Override
            public void onSelect(Place place) {
                confirmSelectedPlace(place);
            }

            @Override
            public void onEdit(int position, Place place) {}

            @Override
            public void onDelete(int position) {}
        });
    }

    private void confirmSelectedPlace(Place place) {
        if(place.getAddress() == null || place.getLatitude() == null || place.getLongitude() == null) return;
        if(isPickUpPlace){
            pickUp = place;
            pickUpInput.setText(place.getAddress());
        }
        else {
            dropOff = place;
            dropOffInput.setText(place.getAddress());
        }

        String pickUpPlace = Objects.requireNonNull(pickUpInput.getText()).toString().trim();
        String dropOffPlace = Objects.requireNonNull(dropOffInput.getText()).toString().trim();

        if(pickUpPlace.isEmpty() || pickUp.getAddress() == null || pickUp.getLatitude() == null || pickUp.getLongitude() == null) {
            pickUpInput.setText(null);
            pickUpInput.requestFocus();
            return;
        }
        if(dropOffPlace.isEmpty() || dropOff.getAddress() == null || dropOff.getLatitude() == null || dropOff.getLongitude() == null) {
            dropOffInput.setText(null);
            dropOffInput.requestFocus();
            return;
        }

        if(mOnPlaceListener != null) mOnPlaceListener.onPlace(pickUp, dropOff);
        dismiss();
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

    public void setOnPlaceListener(OnPlaceListener mOnPlaceListener) {
        this.mOnPlaceListener = mOnPlaceListener;
    }

    public interface OnPlaceListener {
        void onPlace(Place from, Place to);
    }
}
