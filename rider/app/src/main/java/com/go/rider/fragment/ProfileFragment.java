package com.go.rider.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import com.go.rider.R;
import com.go.rider.activity.HomeActivity;
import com.go.rider.adapter.DistrictsAdapter;
import com.go.rider.adapter.UpazilasAdapter;
import com.go.rider.model.address.District;
import com.go.rider.model.user.Rider;
import com.go.rider.model.address.Upazila;
import com.go.rider.remote.FirebaseHelper;
import com.go.rider.remote.PermissionManager;
import com.go.rider.util.Constants;
import com.go.rider.util.CustomSnackBar;
import com.go.rider.util.TempStorage;
import com.go.rider.util.extension.AppExtensions;
import com.go.rider.wiget.CircleImageView;
import com.go.rider.wiget.InstantAutoCompleteTextView;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static android.app.Activity.RESULT_OK;
import static com.go.rider.activity.HomeActivity.drawerLayout;

@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class ProfileFragment extends DialogFragment {

    private static final String             TAG = ProfileFragment.class.getSimpleName();
    private AppCompatImageView              coverPhoto;
    private AppCompatImageView              upload_CoverPhoto;
    private AppCompatTextView               cPhUpload_Progress;
    private CircleImageView                 profilePhoto;
    private AppCompatImageView              upload_ProfilePhoto;
    private ProgressBar                     pPhUpload_ProgressBar;
    private AppCompatTextView               pPhUpload_Progress;
    private AppCompatTextView               edit_Name;
    private AppCompatEditText               name_Input;
    private AppCompatEditText               email_Input;
    private AppCompatEditText               phone_Input;
    private AppCompatTextView               edit_Gender;
    private AppCompatEditText               gender_Input;
    private AppCompatTextView               edit_Address;
    private AppCompatEditText               address_Input;
    private AppCompatEditText               country_Input;
    private AppCompatTextView               edit_District;
    private AppCompatAutoCompleteTextView   district_Input;
    private AppCompatTextView               edit_Upazila;
    private InstantAutoCompleteTextView     upazila_Input;
    private AppCompatTextView               edit_PostalCode;
    private AppCompatEditText               postalCode_Input;
    private AppCompatImageView              backBtn;
    private List<Upazila>                   upazilas = new ArrayList<>();
    private boolean                         isProfileCompleted = true;
    private LoaderFragment                  loader;
    private AppExtensions.Photo             selectPhoto = AppExtensions.Photo.PROFILE;
    private String                          userId = null;
    private FirebaseHelper                  firebaseHelper;
    private boolean                         isProfileUpdated = false;
    private Context                         context;
    private Activity                        activity;
    private OnProfileUpdateListener         mOnProfileUpdateListener;

    public static ProfileFragment show(HomeActivity homeActivity){
        ProfileFragment fragment = new ProfileFragment();
        fragment.show(homeActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_TopLight_SliderAnimation);
        setRetainInstance(true);
        setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        new Handler().postDelayed(() -> drawerLayout.closeDrawer(GravityCompat.START), Constants.SLIDE_OUT_DURATION);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), true);
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        coverPhoto = view.findViewById(R.id.cover_Photo);
        upload_CoverPhoto = view.findViewById(R.id.upload_CoverPhoto);
        cPhUpload_Progress = view.findViewById(R.id.coverPhotoUpload_Progress);
        profilePhoto = view.findViewById(R.id.profile_Photo);
        upload_ProfilePhoto = view.findViewById(R.id.upload_ProfilePhoto);
        pPhUpload_ProgressBar = view.findViewById(R.id.profilePhotoUpload_ProgressBar);
        pPhUpload_Progress = view.findViewById(R.id.profilePhotoUpload_Progress);
        pPhUpload_Progress.bringToFront();
        edit_Name = view.findViewById(R.id.editName);
        name_Input = view.findViewById(R.id.name_Input_Etxt);
        name_Input.setInputType(InputType.TYPE_NULL);
        name_Input.setEnabled(false);
        email_Input = view.findViewById(R.id.email_Input_Etxt);
        email_Input.setInputType(InputType.TYPE_NULL);
        email_Input.setEnabled(false);
        phone_Input = view.findViewById(R.id.phone_Input_Etxt);
        phone_Input.setInputType(InputType.TYPE_NULL);
        phone_Input.setEnabled(false);
        edit_Gender = view.findViewById(R.id.editGender);
        gender_Input = view.findViewById(R.id.gender_Input_Etxt);
        gender_Input.setInputType(InputType.TYPE_NULL);
        gender_Input.setEnabled(false);
        edit_Address = view.findViewById(R.id.editAddress);
        address_Input = view.findViewById(R.id.address_Input_Etxt);
        address_Input.setInputType(InputType.TYPE_NULL);
        address_Input.setEnabled(false);
        country_Input = view.findViewById(R.id.country_Input_Etxt);
        country_Input.setInputType(InputType.TYPE_NULL);
        country_Input.setEnabled(false);
        edit_District = view.findViewById(R.id.editDistrict);
        district_Input = view.findViewById(R.id.district_Input_Etxt);
        district_Input.setInputType(InputType.TYPE_NULL);
        district_Input.setEnabled(false);
        edit_Upazila = view.findViewById(R.id.editUpazilla);
        upazila_Input = view.findViewById(R.id.upazila_Input_Etxt);
        upazila_Input.setInputType(InputType.TYPE_NULL);
        upazila_Input.setEnabled(false);
        edit_PostalCode = view.findViewById(R.id.editPostalCode);
        postalCode_Input = view.findViewById(R.id.postalCode_Input_Etxt);
        postalCode_Input.setInputType(InputType.TYPE_NULL);
        postalCode_Input.setEnabled(false);
        backBtn = view.findViewById(R.id.back_Btn);
        firebaseHelper = new FirebaseHelper(context);
        postalCode_Input.setTransformationMethod(new AppExtensions.NumericKeyBoardTransformationMethod());
    }

    private void init(){
        setProfileInfo();

        district_Input.setThreshold(1);
        district_Input.setAdapter(new DistrictsAdapter(context, AppExtensions.getDistricts()));

        district_Input.setOnItemClickListener((parent, view, position, id) -> {
            District district = (District) parent.getAdapter().getItem(position);
            upazilas = district.getUpazilas();
            upazila_Input.setThreshold(1);
            upazila_Input.setAdapter(new UpazilasAdapter(context, upazilas));
        });

        postalCode_Input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == (4)){
                    AppExtensions.hideKeyboard(activity.getCurrentFocus());
                    postalCode_Input.clearFocus();
                }
            }
        });

        gender_Input.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                AppExtensions.createPopupMenu(gender_Input, new Object[]{AppExtensions.Gender.Male, AppExtensions.Gender.Female});
            }
            return false;
        });

        edit_Name.setOnClickListener(v -> updateUI((AppCompatTextView) v, name_Input.isEnabled(), name_Input, InputType.TYPE_TEXT_VARIATION_PERSON_NAME, "name"));

        edit_Gender.setOnClickListener(v -> updateUI((AppCompatTextView) v, gender_Input.isEnabled(), gender_Input, InputType.TYPE_NULL, "gender"));

        edit_Address.setOnClickListener(v -> updateUI((AppCompatTextView) v, address_Input.isEnabled(), address_Input, InputType.TYPE_TEXT_FLAG_MULTI_LINE, "address"));

        edit_District.setOnClickListener(v -> updateUI((AppCompatTextView) v, district_Input.isEnabled(), district_Input, InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_CAP_WORDS, "district"));

        edit_Upazila.setOnClickListener(v -> updateUI((AppCompatTextView) v, upazila_Input.isEnabled(), upazila_Input, InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_CAP_WORDS, "upazila"));

        edit_PostalCode.setOnClickListener(v -> updateUI((AppCompatTextView) v, postalCode_Input.isEnabled(), postalCode_Input, InputType.TYPE_CLASS_NUMBER, "postalCode"));

        backBtn.setOnClickListener(v -> dismiss());
    }

    private void setProfileInfo() {
        if(TempStorage.RIDER == null) return;
        userId = Objects.requireNonNull(firebaseHelper.getFirebaseUser()).getUid();

        AppExtensions.loadPhoto(coverPhoto, TempStorage.RIDER.getCoverPhoto(), null, R.drawable.shape_placeholder);

        int placeHolder = TempStorage.RIDER.getGender() == null || TempStorage.RIDER.getGender().equals(AppExtensions.Gender.Male.toString()) ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;
        AppExtensions.loadPhoto(profilePhoto, TempStorage.RIDER.getProfilePhoto(), R.dimen.icon_Size_XXXX_Large, placeHolder);

        profilePhoto.setOnClickListener(v -> PhotoViewFragment.show((HomeActivity) context, TempStorage.RIDER.getProfilePhoto()));

        upload_CoverPhoto.setOnClickListener(view -> {
            if(!Constants.IS_INTERNET_CONNECTED){
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.network_Error, R.string.retry, CustomSnackBar.Duration.LONG).show();
                return;
            }

            selectPhoto = AppExtensions.Photo.COVER;
            PhotoActionFragment.show((HomeActivity) context).setOnActionListener((dialog, isCapture) -> {
                if(isCapture) captureByCamera();
                else pickFromGallery(R.string.select_CoverPhoto);
                dialog.dismiss();
            });
        });

        upload_ProfilePhoto.setOnClickListener(view -> {
            if(!Constants.IS_INTERNET_CONNECTED){
                new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.network_Error, R.string.retry, CustomSnackBar.Duration.LONG).show();
                return;
            }

            selectPhoto = AppExtensions.Photo.PROFILE;
            PhotoActionFragment.show((HomeActivity) context).setOnActionListener((dialog, isCapture) -> {
                if(isCapture) captureByCamera();
                else pickFromGallery(R.string.select_ProfilePhoto);
                dialog.dismiss();
            });
        });

        name_Input.setText(TempStorage.RIDER.getName());
        email_Input.setText(TempStorage.RIDER.getEmail());
        phone_Input.setText(TempStorage.RIDER.getPhone());
        gender_Input.setText(TempStorage.RIDER.getGender());

        address_Input.setText(TempStorage.RIDER.getAddress().getAddress());
        district_Input.setText(TempStorage.RIDER.getAddress().getDistrict());
        upazila_Input.setText(TempStorage.RIDER.getAddress().getUpazila());
        postalCode_Input.setText(TempStorage.RIDER.getAddress().getPostalCode());
        country_Input.setText(TempStorage.RIDER.getAddress().getCountry());

        if(TempStorage.RIDER.getAddress().getDistrict() != null){
            List<District> districts = AppExtensions.getDistricts();

            for (District district : districts){
                if(district.getName().equals(TempStorage.RIDER.getAddress().getDistrict())){
                    upazilas = district.getUpazilas();
                    break;
                }
            }
        }

        upazila_Input.setThreshold(1);
        upazila_Input.setAdapter(new UpazilasAdapter(context, upazilas));
    }

    private void pickFromGallery(int chooserTitle) {
        if (!new PermissionManager(PermissionManager.Permission.GALLERY, true, response -> {
            pickFromGallery(selectPhoto == AppExtensions.Photo.PROFILE ? R.string.select_ProfilePhoto : R.string.select_CoverPhoto);
        }).isGranted()) return;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, AppExtensions.getString(chooserTitle)), Constants.GALLERY_REQUEST_CODE);
    }

    private void captureByCamera() {
        if (!new PermissionManager(PermissionManager.Permission.CAMERA, true, response -> captureByCamera() ).isGranted()) return;
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, Constants.CAMERA_REQUEST_CODE);
    }

    private void updateUI(AppCompatTextView title, boolean isUpdateDone, View inputField, int defaultType, String fieldName) {
        if(!Constants.IS_INTERNET_CONNECTED){
            new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.network_Error, R.string.retry, CustomSnackBar.Duration.LONG).show();
            return;
        }

        if (isUpdateDone) {
            Rider tempRider = new Rider(TempStorage.RIDER, false);;
            boolean isInputMatched = false;

            String input = null;
            if (inputField instanceof AppCompatEditText){
                input = Objects.requireNonNull(((AppCompatEditText) inputField).getText()).toString().trim();
            }
            else if (inputField instanceof AppCompatAutoCompleteTextView){
                input = Objects.requireNonNull(((AppCompatAutoCompleteTextView) inputField).getText()).toString().trim();
            }

            switch (fieldName) {
                case "name":
                    if (input == null || !AppExtensions.isInputValid(name_Input, R.string.name_Error)) return;
                    if(input.equals(TempStorage.RIDER.getName())) isInputMatched = true;
                    else tempRider.setName(input);
                    break;

                case "gender":
                    if(input != null && TempStorage.RIDER.getGender() != null && input.equals(TempStorage.RIDER.getGender())) isInputMatched = true;
                    else tempRider.setGender(input);
                    break;

                case "address":
                    if(input != null && TempStorage.RIDER.getAddress().getAddress() != null && input.equals(TempStorage.RIDER.getAddress().getAddress())) isInputMatched = true;
                    tempRider.getAddress().setAddress(input);
                    break;

                case "district":
                    if(input != null && TempStorage.RIDER.getAddress().getDistrict() != null && input.equals(TempStorage.RIDER.getAddress().getDistrict())) isInputMatched = true;
                    tempRider.getAddress().setDistrict(input);
                    break;

                case "upazila":
                    if(input != null && TempStorage.RIDER.getAddress().getUpazila() != null && input.equals(TempStorage.RIDER.getAddress().getUpazila())) isInputMatched = true;
                    tempRider.getAddress().setUpazila(input);
                    break;

                case "postalCode":
                    if(input != null && TempStorage.RIDER.getAddress().getPostalCode() != null && input.equals(TempStorage.RIDER.getAddress().getPostalCode())) isInputMatched = true;
                    tempRider.getAddress().setPostalCode(input);
                    break;
            }

            if (tempRider.getGender() == null
                    ||
                    tempRider.getAddress().getAddress() == null
                    ||
                    tempRider.getAddress().getDistrict() == null
                    ||
                    tempRider.getAddress().getUpazila() == null
                    ||
                    tempRider.getAddress().getPostalCode() == null)
                isProfileCompleted = false;

            tempRider.setProfileCompleted(isProfileCompleted);

            if(isInputMatched){
                inputField.clearFocus();
                inputField.setEnabled(false);
                title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_pen, 0);

                if (inputField instanceof AppCompatEditText){
                    ((AppCompatEditText) inputField).setInputType(InputType.TYPE_NULL);
                    ((AppCompatEditText) inputField).setHint(null);
                }
                else if (inputField instanceof AppCompatAutoCompleteTextView){
                    ((AppCompatAutoCompleteTextView) inputField).setInputType(InputType.TYPE_NULL);
                    ((AppCompatAutoCompleteTextView) inputField).setHint(null);
                }

                return;
            }

            showLoader();

            firebaseHelper.setData(firebaseHelper.riderReference().setValue(tempRider),
                    new FirebaseHelper.OnFirebaseUpdateListener() {
                        @Override
                        public void onSuccess() {
                            isProfileUpdated = true;
                            dismissLoader();
                            TempStorage.setRiderInfo(tempRider, true);

                            inputField.clearFocus();
                            inputField.setEnabled(false);
                            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_pen, 0);

                            if (inputField instanceof AppCompatEditText){
                                ((AppCompatEditText) inputField).setInputType(InputType.TYPE_NULL);
                                ((AppCompatEditText) inputField).setHint(null);
                            }
                            else if (inputField instanceof AppCompatAutoCompleteTextView){
                                ((AppCompatAutoCompleteTextView) inputField).setInputType(InputType.TYPE_NULL);
                                ((AppCompatAutoCompleteTextView) inputField).setHint(null);
                            }
                        }

                        @Override
                        public void onFailure() {
                            dismissLoader();
                            AppExtensions.customToast(R.string.profileUpdateFailed);
                        }
                    });
        }
        else {
            String hint;
            switch (fieldName){
                case "name":
                    hint = AppExtensions.getString(R.string.name_Hint);
                    break;
                case "gender":
                    hint = AppExtensions.getString(R.string.gender_Hint);
                    break;
                case "address":
                    hint = AppExtensions.getString(R.string.address_Hint);
                    break;
                case "district":
                    hint = AppExtensions.getString(R.string.district_Hint);
                    break;
                case "upazila":
                    hint = AppExtensions.getString(R.string.upazila_Hint);
                    break;
                case "postalCode":
                    hint = AppExtensions.getString(R.string.postalCode_Hint);
                    break;
                default:
                    hint = null;
            }

            inputField.setEnabled(true);
            inputField.requestFocus();
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_done, 0);

            if (inputField instanceof AppCompatEditText){
                ((AppCompatEditText) inputField).setInputType(defaultType);
                ((AppCompatEditText) inputField).setHint(hint);

            }
            else if (inputField instanceof AppCompatAutoCompleteTextView){
                ((AppCompatAutoCompleteTextView) inputField).setInputType(defaultType);
                ((AppCompatAutoCompleteTextView) inputField).setHint(hint);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            try {
                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());
                if(mBitmap == null) return;

                switch (selectPhoto){
                    case COVER:
                        uploadCoverPhoto(mBitmap);
                        break;

                    case PROFILE:
                        uploadProfilePhoto(mBitmap);
                        break;
                }
            }
            catch (IOException ex) {
                Toast.makeText(context, ex.getMessage()+"", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
        }
        else if(requestCode == Constants.CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            try {
                Bitmap mBitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                if (mBitmap == null) return;

                switch (selectPhoto){
                    case COVER:
                        uploadCoverPhoto(mBitmap);
                        break;

                    case PROFILE:
                        uploadProfilePhoto(mBitmap);
                        break;
                }
            }
            catch (Exception ex){
                Toast.makeText(context, ex.getMessage()+"", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
        }
    }

    private void uploadCoverPhoto(Bitmap mBitmap){
        showLoader();

        byte[] photoBytes = AppExtensions.getBitmapBytes(mBitmap);
        DatabaseReference photoReference = firebaseHelper.riderReference().child("coverPhoto");

        firebaseHelper.uploadPhoto(photoBytes, photoReference, AppExtensions.Photo.COVER,
                new FirebaseHelper.OnPhotoUploadListener() {
                    @Override
                    public void onSuccess(String photoLink) {
                        Rider tempRider = new Rider(TempStorage.RIDER, false);;
                        tempRider.setCoverPhoto(photoLink);
                        TempStorage.setRiderInfo(tempRider, true);

                        isProfileUpdated = true;
                        coverPhoto.setImageBitmap(mBitmap);

                        cPhUpload_Progress.setVisibility(View.GONE);
                        cPhUpload_Progress.setText(null);
                        dismissLoader();
                    }

                    @Override
                    public void onFailure() {
                        cPhUpload_Progress.setVisibility(View.GONE);
                        cPhUpload_Progress.setText(null);
                        dismissLoader();
                    }

                    @Override
                    public void onProgress(double progress) {
                        cPhUpload_Progress.setVisibility(View.VISIBLE);
                        cPhUpload_Progress.setText((int)progress + "%");
                    }
                });
    }

    private void uploadProfilePhoto(Bitmap mBitmap){
        showLoader();

        byte[] photoBytes = AppExtensions.getBitmapBytes(mBitmap);
        DatabaseReference photoReference = firebaseHelper.riderReference().child("profilePhoto");

        firebaseHelper.uploadPhoto(photoBytes, photoReference, AppExtensions.Photo.PROFILE,
                new FirebaseHelper.OnPhotoUploadListener() {
                    @Override
                    public void onSuccess(String photoLink) {
                        Rider tempRider = new Rider(TempStorage.RIDER, false);;
                        tempRider.setProfilePhoto(photoLink);
                        TempStorage.setRiderInfo(tempRider, true);

                        isProfileUpdated = true;
                        profilePhoto.setImageBitmap(mBitmap);

                        pPhUpload_ProgressBar.setVisibility(View.GONE);
                        pPhUpload_ProgressBar.setProgress(0);
                        pPhUpload_Progress.setVisibility(View.GONE);
                        pPhUpload_Progress.setText(null);
                        dismissLoader();
                    }

                    @Override
                    public void onFailure() {
                        pPhUpload_ProgressBar.setVisibility(View.GONE);
                        pPhUpload_ProgressBar.setProgress(0);
                        pPhUpload_Progress.setVisibility(View.GONE);
                        pPhUpload_Progress.setText(null);
                        dismissLoader();
                    }

                    @Override
                    public void onProgress(double progress) {
                        pPhUpload_ProgressBar.setVisibility(View.VISIBLE);
                        pPhUpload_ProgressBar.setProgress((int) progress);
                        pPhUpload_Progress.setVisibility(View.VISIBLE);
                        pPhUpload_Progress.setText((int) progress + "%");
                    }
                });
    }

    private void showLoader(){
        loader = LoaderFragment.show((HomeActivity) context);
    }

    private void dismissLoader(){
        if (loader != null && loader.getDialog() != null
                &&
                loader.getDialog().isShowing()
                &&
                !loader.isRemoving())

            loader.dismiss();
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
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mOnProfileUpdateListener != null) mOnProfileUpdateListener.onProfileUpdate(isProfileUpdated);
    }

    public void setOnProfileUpdateListener(OnProfileUpdateListener mOnProfileUpdateListener) {
        this.mOnProfileUpdateListener = mOnProfileUpdateListener;
    }

    public interface OnProfileUpdateListener {
        void onProfileUpdate(boolean isUpdated);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        this.activity = (Activity) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
