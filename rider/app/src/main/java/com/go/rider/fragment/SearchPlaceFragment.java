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
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.adapter.SearchPlaceAdapter;
import com.go.rider.model.location.Place;
import com.go.rider.util.extension.AppExtensions;
import java.util.Objects;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class SearchPlaceFragment extends DialogFragment {

    private static final String     TAG = SearchPlaceFragment.class.getSimpleName();

    /**
     * Toolbar
     **/
    private AppCompatImageView      backBtn;

    /**
     * Content
     **/
    private AppCompatEditText       placeInput;
    private RecyclerView            searchPlaces;
    private SearchPlaceAdapter      searchPlaceAdapter;

    /**
     * Other
     **/
    private Context                 context;
    private OnPlaceListener         mOnPlaceListener;

    public static SearchPlaceFragment show(HomeActivity homeActivity){
        SearchPlaceFragment fragment = new SearchPlaceFragment();
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
        return inflater.inflate(R.layout.fragment_layout_search_place, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), true);
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        backBtn = view.findViewById(R.id.backBtn);
        placeInput = view.findViewById(R.id.place_Input_Etxt);
        searchPlaces = view.findViewById(R.id.searchPlaces);
    }

    private void init(){
        AppExtensions.showKeyboardInDialog(placeInput);
        placeInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        setPlacesAdapter();

        placeInput.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= placeInput.getRight() - placeInput.getTotalPaddingRight()) {
                    placeInput.setText(null);
                    return true;
                }
            }
            return false;
        });

        placeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().isEmpty()){
                    placeInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    searchPlaceAdapter.clearAll();
                    return;
                }
                placeInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear, 0);
                searchPlaceAdapter.getFilter().filter(s.toString());
            }
        });

        backBtn.setOnClickListener(v -> dismiss());
    }

    private void setPlacesAdapter() {
        searchPlaces.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        searchPlaceAdapter = new SearchPlaceAdapter(context);
        searchPlaces.setAdapter(searchPlaceAdapter);
        searchPlaceAdapter.setOnPlaceSelectListener(place -> {
            if(place.getAddress() == null || place.getLatitude() == null || place.getLongitude() == null) return;
            if(mOnPlaceListener != null) mOnPlaceListener.onPlace(place);
            dismiss();
        });
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
        void onPlace(Place place);
    }
}
