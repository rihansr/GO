package com.go.rider.util.extension;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.go.rider.R;
import com.go.rider.model.address.District;
import com.go.rider.remote.FCMClient;
import com.go.rider.remote.IFCMService;
import com.go.rider.service.GoService;
import com.go.rider.util.Constants;
import com.go.rider.util.TempStorage;
import com.go.rider.wiget.InstantAutoCompleteTextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.libraries.places.api.Places;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AppExtensions {

    /**
     * Enums
     **/
    public enum Photo {
        PROFILE, COVER, OTHER
    }

    public enum Gender {
        Male, Female
    }

    public enum Action {
        Edit, Delete
    }

    public enum Vehicle {
        Car, Bike, Taxi, Other
    }

    public enum Payment {
        Cash, Card, Bkash
    }

    public enum TripRequest {
        Finding, Cancel, Contact
    }


    /**
     * Dialog & Activity Styles
     **/
    public static void halfScreenDialog(Dialog dialog){
        if (dialog == null) return;

        Window window = dialog.getWindow();
        if (window == null) return;

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.windowAnimations = R.style.DialogDefaultAnimation;
        window.setGravity(Gravity.BOTTOM);
        window.setAttributes(params);
    }

    public static void fullScreenDialog(Dialog dialog, boolean isLightStatusBar){
        if (dialog == null) return;

        Window window = dialog.getWindow();
        if (window == null) return;

        if (isLightStatusBar) window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        else window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public static void fullScreenActivity(Window window, boolean isLightStatusBar){
        if (window == null) return;
        if (isLightStatusBar) window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        else window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }


    /**
     * Soft Keyboard
     **/
    public static class NumericKeyBoardTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return source;
        }
    }

    public static void hideKeyboard(AppCompatEditText[] editTexts) {
        InputMethodManager imm = (InputMethodManager) GoService.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(GoService.getActivity().getWindow().getDecorView().getWindowToken(), 0);

        for(AppCompatEditText editText : editTexts){
            editText.clearFocus();
        }
    }

    public static void hideKeyboard(View view) {
        if(view != null){
            InputMethodManager imm = (InputMethodManager) GoService.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showKeyboard(View view) {
        if(view != null){
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) GoService.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void showKeyboardInDialog(View view) {
        if(view != null){
            view.requestFocus();
            InputMethodManager imm = (InputMethodManager) GoService.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)  imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public static void hideKeyboardInDialog() {
        InputMethodManager imm = (InputMethodManager) GoService.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)  imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void requestFocus(View view) {
        if (view.requestFocus()) {
            Objects.requireNonNull(GoService.getActivity().getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public static void requestFocus(AlertDialog dialog, View view) {
        if (view.requestFocus()) {
            Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    /**
     * Photo Actions
     **/
    public static void loadPhoto(AppCompatImageView holder, String photo, Integer size, Integer placeholder) {
        if (photo != null) {
            Glide.with(GoService.getContext())
                    .load(photo)
                    .override(size != null ? (int) getDimension(size) : Target.SIZE_ORIGINAL, size != null ? (int) getDimension(size) : Target.SIZE_ORIGINAL)
                    .error(placeholder != null ? placeholder : R.drawable.ic_placeholder)
                    .fallback(placeholder != null ? placeholder : R.drawable.ic_placeholder)
                    .placeholder(placeholder != null ? placeholder : R.drawable.ic_placeholder)
                    .into(holder);
        } else {
            holder.setImageResource(placeholder != null ? placeholder : R.drawable.ic_placeholder);
        }
    }

    private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        if(width<maxSize || height<maxSize) return image;

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        }
        else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static byte[] getBitmapBytes(Bitmap mBitmap){
        Bitmap resizedBitmap = getResizedBitmap(mBitmap, 1024);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }


    /**
     * Validate Input Field
     **/
    public static boolean isEmailValid(AppCompatEditText editText){
        String input = Objects.requireNonNull(editText.getText()).toString().trim();

        if (TextUtils.isEmpty(Objects.requireNonNull(input))) {
            editText.setError(getString(R.string.email_Error));
            AppExtensions.requestFocus(editText);
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            editText.setError(getString(R.string.validEmail_Error));
            AppExtensions.requestFocus(editText);
            return false;
        }
        else return true;
    }

    public static boolean isNumberValid(AppCompatEditText editText){
        String input = Objects.requireNonNull(editText.getText()).toString().trim();

        if (TextUtils.isEmpty(Objects.requireNonNull(input))) {
            editText.setError(getString(R.string.phone_Error));
            AppExtensions.requestFocus(editText);
            return false;
        } else if (input.length() != 11 || !(Constants.COUNTRY_CODE + input).matches(Constants.PHONE_PATTERN)) {
            editText.setError(getString(R.string.validPhone_Error));
            AppExtensions.requestFocus(editText);
            return false;
        }
        else return true;
    }

    public static boolean isNumberValid(String phone){
        return phone.length() == 11 && (Constants.COUNTRY_CODE + phone).matches(Constants.PHONE_PATTERN);
    }

    public static boolean isInputValid(View inputField, Integer errorMessage){
        String input = null;
        if(inputField instanceof AppCompatEditText){
            input = Objects.requireNonNull(((AppCompatEditText)inputField).getText()).toString().trim();
        }
        else if(inputField instanceof InstantAutoCompleteTextView){
            input = Objects.requireNonNull(((InstantAutoCompleteTextView)inputField).getText()).toString().trim();
        }
        else if(inputField instanceof AppCompatAutoCompleteTextView){
            input = Objects.requireNonNull(((AppCompatAutoCompleteTextView)inputField).getText()).toString().trim();
        }

        if(TextUtils.isEmpty(Objects.requireNonNull(input))){
            if(errorMessage != null){

                if(inputField instanceof AppCompatEditText){
                    ((AppCompatEditText)inputField).setError(getString(errorMessage));
                }
                else if(inputField instanceof InstantAutoCompleteTextView){
                    ((InstantAutoCompleteTextView)inputField).setError(getString(errorMessage));
                }
                else if(inputField instanceof AppCompatAutoCompleteTextView){
                    ((AppCompatAutoCompleteTextView)inputField).setError(getString(errorMessage));
                }
                AppExtensions.requestFocus(inputField);
            }
            return false;
        }
        else return true;
    }

    public static boolean isInputValid(AppCompatEditText editText, boolean isNull, Integer errorMessage){
        if(isNull){
            if(errorMessage != null){
                editText.setError(getString(errorMessage));
                AppExtensions.requestFocus(editText);
            }
            return false;
        }
        else return true;
    }


    /**
     * Map
     **/
    public static void setMapStyle(GoogleMap mMap){
        try {
            boolean isSuccess = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(GoService.getContext(), R.raw.uber_style_map)
            );
            if (!isSuccess) {
                Log.e(Constants.TAG, "Map style load failed!!!");
            }
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public static void setMapSettings(GoogleMap mMap){
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMinZoomPreference(Constants.DEFAULT_MIN_ZOOM);
        mMap.setMaxZoomPreference(Constants.DEFAULT_MAX_ZOOM);
    }

    public static void initializePlaces(){
        if (!Places.isInitialized()) {
            Places.initialize(GoService.getContext(), Constants.MAP_API_KEY);
            Constants.PLACES_CLIENT = Places.createClient(GoService.getContext());
        }
    }

    public static boolean nearByRider(int rad, LatLng from, LatLng to) {
        Location locationA = new Location("point A");
        locationA.setLatitude(from.latitude);
        locationA.setLongitude(from.longitude);
        Location locationB = new Location("point B");
        locationB.setLatitude(to.latitude);
        locationB.setLongitude(to.longitude);
        int distance = (int) locationA.distanceTo(locationB);
        return distance / 1000 <= rad;
    }

    public static BitmapDescriptor getMarkerIconFromDrawable(Vehicle vehicle, Integer defaultDrawable) {
        Canvas canvas = new Canvas();

        Drawable drawable;  int width;  int height;

        switch (vehicle){
            case Car:
                drawable = getDrawable(R.drawable.ic_marker_car);
                width = 48; height = 96;
                break;

            case Bike:
                drawable = getDrawable(R.drawable.ic_marker_bike);
                width = 73; height = 102;
                break;

            case Taxi:
                drawable = getDrawable(R.drawable.ic_marker_taxi);
                width = 96; height = 96;
                break;

            default:
                drawable = getDrawable(defaultDrawable);
                width = 42; height = 42;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);

        if (drawable != null) {
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static String getCityName(double lat, double lng){
        Geocoder geocoder = new Geocoder(GoService.getContext(),  Locale.US);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lat, lng, 5);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            for (Address adr : addresses) {
                if (adr.getLocality() != null && adr.getLocality().length() > 0) {
                    return adr.getLocality();
                }
            }
        }
        return null;
    }

    public static String getAddress(double latitude, double longitude){
        try {
            Geocoder geocoder = new Geocoder(GoService.getContext(), Locale.US);
            /**
             * Here 1 represent max location result to returned, by documents it recommended 1 to 5
             **/
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            return addresses.get(0).getAddressLine(addresses.get(0).getMaxAddressLineIndex());
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Notification
     **/
    public static IFCMService getFCMClient(){
        return FCMClient.getClient(Constants.FCM_URL).create(IFCMService.class);
    }


    /**
     * Formatter
     **/
    public static String nameFormat(String name) {
        String[] parts = name.trim().split(" ");
        for (String n : parts) {
            name = n.trim().replaceAll("[^(A-Za-z)]", "").trim();
            if (name.length() > 2) break;
        }
        return name;
    }

    public static String getReferralCode(String name) {
        name = nameFormat(name);
        name = name.toUpperCase();
        name = name.length() > 4 ? name.substring(0, 4) : name;
        int limit = 7;
        int length = name.length();
        String randomCode = getRandomCode(limit - length);
        return name + randomCode;
    }

    public static String getRandomCode(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789";
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }

        return sb.toString();
    }

    public static String decimalFormat(double number, String format, Boolean alwaysDecimal) {
        if(format == null || format.trim().isEmpty()) return new DecimalFormat().format(Math.round(number));
        DecimalFormat df = new DecimalFormat(format);
        return (number % 1 == 0) && (alwaysDecimal == null || !alwaysDecimal) ? String.valueOf((int) number) : df.format(number);
    }


    /**
     * Others
     **/
    public static void customToast(Object message){
        Toast customToast = Toast.makeText(GoService.getContext(), message instanceof String ? (String)message : getString((Integer)message), Toast.LENGTH_LONG);
        customToast.setGravity(Gravity.BOTTOM,0,50);
        View toastView = customToast.getView();

        TextView textView = toastView.findViewById(android.R.id.message);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        toastView.setPadding(15,10,15,10);
        toastView.setBackgroundResource(R.drawable.shape_toast);

        customToast.show();
    }

    public static void createPopupMenu(View input, Object[] items) {
        final PopupMenu mPopup = new PopupMenu(GoService.getContext(), input);

        for (Object item : items) {
            if(item == null) continue;
            mPopup.getMenu().add(item instanceof Integer ? getString((Integer)item) : item.toString());
        }

        mPopup.setOnMenuItemClickListener(item -> {
            if(input instanceof TextView) ((TextView)input).setText(item.getTitle());
            if(input instanceof EditText) ((EditText)input).setText(item.getTitle());
            return true;
        });

        mPopup.show();
    }

    public static void createPopupMenu(View view, Object[] items, PopupMenu.OnMenuItemClickListener listener) {
        final PopupMenu mPopup = new PopupMenu(GoService.getContext(), view);

        for (Object item : items) {
            if(item == null) continue;
            mPopup.getMenu().add(item instanceof Integer ? getString((Integer)item) : item.toString());
        }

        mPopup.setOnMenuItemClickListener(listener);

        mPopup.show();
    }

    public static List<District> getDistricts() {
        try {
            InputStream inputStream = GoService.getContext().getAssets().open("districts.json");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            return new LinkedList<>(Arrays.asList(new GsonBuilder().setPrettyPrinting().create().fromJson(new String(buffer, StandardCharsets.UTF_8), District[].class)));
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return new LinkedList<>();
        }
    }

    public static void doGradientText(AppCompatTextView tv){
        TextPaint paint = tv.getPaint();
        float width = paint.measureText(tv.getText().toString());

        Shader textShader = new LinearGradient(0, 0, width, tv.getTextSize(),
                new int[]{
                        Color.parseColor("#02C0D0"),
                        Color.parseColor("#00D3A7"),
                        Color.parseColor("#01C7C0")
                }, null, Shader.TileMode.CLAMP);

        tv.getPaint().setShader(textShader);
    }

    public static String showGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if(timeOfDay < 7){
            greeting =  getString(R.string.hello);
        }
        else if(timeOfDay < 12){
            greeting =  getString(R.string.morning);
        }
        else if(timeOfDay < 16){
            greeting =  getString(R.string.afternoon);
        }
        else if(timeOfDay < 21){
            greeting =  getString(R.string.evening);
        }
        else {
            greeting = getString(R.string.hello);
        }

        return greeting + (TempStorage.RIDER == null ? "" : ", " + nameFormat(TempStorage.RIDER.getName()));
    }

    public static void shareFile(Context context, File sharePath) {
        Uri uri;
        if (Build.VERSION.SDK_INT < 24) {
            uri = Uri.parse("file://" + sharePath);
        }
        else {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", new File(String.valueOf(sharePath)));
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.setType("*/*");
        context.startActivity(Intent.createChooser(shareIntent, getString(R.string.shareAPkVia)));
    }

    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = GoService.getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static View getRootView(Dialog dialog){
        return Objects.requireNonNull(Objects.requireNonNull(dialog).getWindow()).getDecorView().getRootView();
    }


    /**
     * Resources
     **/
    public static String getString(int id){
        return GoService.getContext().getResources().getString(id);
    }

    public static Spanned getHtmlString(Object string){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(string instanceof String ? (String)string : getString((Integer)string) , Html.FROM_HTML_MODE_LEGACY);
        else
            return Html.fromHtml(string instanceof String ? (String)string : getString((Integer)string));
    }

    public static float getDimension(int id){
        return GoService.getContext().getResources().getDimension(id);
    }

    public static Drawable getDrawable(int id){
        return ContextCompat.getDrawable(GoService.getContext(), id);
    }
}
