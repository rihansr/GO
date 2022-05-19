package com.go.driver.util.map;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.animation.LinearInterpolator;

import com.go.driver.remote.GoogleApiClient;
import com.go.driver.remote.IGoogleApi;
import com.go.driver.util.Constants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapDirection {

    private ValueAnimator       polylineAnimator;
    private boolean             isAnimatorPlaying = false;
    private GoogleMap           mMap;
    private LatLng              from;
    private LatLng              to;
    private OnDirectionListener directionListener;

    public MapDirection(GoogleMap mMap, LatLng from, LatLng to, OnDirectionListener directionListener) {
        this.mMap = mMap;
        this.from = from;
        this.to = to;
        this.directionListener = directionListener;
    }

    public void show() {
        getGoogleApi().getDataFromGoogleApi(getDirectionUrl(from, to))
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            if(response.body() == null) {
                                if(directionListener != null) directionListener.onFailure();
                                return;
                            }

                            if(directionListener != null) directionListener.onSuccess();

                            JSONObject jsonObject = new JSONObject(response.body());
                            JSONArray routes = jsonObject.getJSONArray("routes");
                            String[] results = calculateRoute(routes);
                            double estimateDistance = Double.parseDouble(results[0]);
                            long estimateTime = Long.parseLong(results[1]);

                            List<LatLng> polyLineList = new ArrayList<>();

                            for(int i=0; i<routes.length(); i++){
                                JSONObject route = routes.getJSONObject(i);
                                JSONObject poly = route.getJSONObject("overview_polyline");
                                String polyline = poly.getString("points");
                                polyLineList = decodePoly(polyline);
                            }

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            for(LatLng latLng : polyLineList){
                                builder.include(latLng);
                            }

                            if(directionListener != null) directionListener.onData(estimateDistance, estimateTime, builder.build());

                            Polyline lowerPolyLine = mMap.addPolyline(buildPolylineOptions("#02C0D0", polyLineList));
                            Polyline upperPolyLine = mMap.addPolyline(buildPolylineOptions("#00D3A7", null));

                            polylineAnimator = ValueAnimator.ofInt(0, 100);
                            polylineAnimator.setDuration(2500);
                            polylineAnimator.setRepeatCount(ValueAnimator.INFINITE);
                            polylineAnimator.setRepeatMode(ValueAnimator.REVERSE);
                            polylineAnimator.setInterpolator(new LinearInterpolator());
                            polylineAnimator.addUpdateListener(valueAnimator -> {
                                List<LatLng> points = lowerPolyLine.getPoints();
                                int percentValue = (int) valueAnimator.getAnimatedValue();
                                int size = points.size();
                                int newPoints = (int) (size * (percentValue / 100.0f));
                                List<LatLng> p = points.subList(0, newPoints);
                                upperPolyLine.setPoints(p);
                            });
                            polylineAnimator.start();
                            isAnimatorPlaying = polylineAnimator.isStarted();
                        }
                        catch (JSONException ex){
                            if(directionListener != null) directionListener.onFailure();
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        if(directionListener != null) directionListener.onFailure();
                        t.printStackTrace();
                    }
                });
    }

    public static IGoogleApi getGoogleApi(){
        return GoogleApiClient.getClient(Constants.GOOGLE_API_URL).create(IGoogleApi.class);
    }

    public static String getDirectionUrl(LatLng origin, LatLng destination) {
        String pickUp_Point = "origin=" + origin.latitude + "," + origin.longitude;

        String dropOff_Point = "destination=" + destination.latitude + "," + destination.longitude;

        String sensor = "sensor=false";
        String mode = "mode=driving";
        String preference = "transit_routing_preference=less_driving";

        String parameters = sensor + "&" + mode + "&" + preference + "&" + pickUp_Point + "&" + dropOff_Point;

        return Constants.GOOGLE_API_URL + "maps/api/directions/json?" + parameters + "&key=" + Constants.MAP_API_KEY;
    }

    public boolean isAnimatorPlaying() {
        return isAnimatorPlaying;
    }

    public void stop(){
        polylineAnimator.cancel();
    }

    public void cancel(){
        if(mMap != null) mMap.clear();
        if (isAnimatorPlaying) polylineAnimator.cancel();
    }

    public interface OnDirectionListener{
        void onSuccess();
        void onData(double distance, long time, LatLngBounds bounds);
        void onFailure();
    }

    private String[] calculateRoute(JSONArray routes) throws JSONException {
        String[] results = new String[2];
        JSONObject object = routes.getJSONObject(0);
        JSONArray legs = object.getJSONArray("legs");
        JSONObject legsObject = legs.getJSONObject(0);

        /**
         * Get Distance (Metres)
         **/
        JSONObject distance = legsObject.getJSONObject("distance");
        results[0] = distance.getString("value");

        /**
         * Get Time (Milliseconds)
         **/
        JSONObject time = legsObject.getJSONObject("duration");
        String time_Text = time.getString("value");
        long millis = (long) Double.parseDouble(time_Text) * 1000;
        results[1] = String.valueOf(millis);

        return results;
    }

    private PolylineOptions buildPolylineOptions(String colorCode, List<LatLng> polyLineList){
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.parseColor(colorCode));
        polylineOptions.width(8f);
        polylineOptions.startCap(new SquareCap());
        polylineOptions.endCap(new SquareCap());
        polylineOptions.jointType(JointType.ROUND);
        polylineOptions.geodesic(true);
        if(polyLineList != null) polylineOptions.addAll(polyLineList);
        return polylineOptions;
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
