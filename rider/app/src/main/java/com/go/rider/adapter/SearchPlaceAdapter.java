package com.go.rider.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.go.rider.R;
import com.go.rider.model.location.Place;
import com.go.rider.util.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SearchPlaceAdapter extends RecyclerView.Adapter<SearchPlaceAdapter.ViewHolder> implements Filterable {

    private Context                         mContext;
    private List<AutocompletePrediction>    mResultList = new ArrayList<>();
    private OnPlaceSelectListener           mOnPlaceSelectListener;

    public SearchPlaceAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public SearchPlaceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.sample_search_place, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView   title;
        private AppCompatTextView   address;

        private ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            address = v.findViewById(R.id.address);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchPlaceAdapter.ViewHolder holder, final int position) {
        AutocompletePrediction item = getItem(position);
        holder.title.setText(item.getPrimaryText(null));
        holder.address.setText(item.getSecondaryText(null));

        holder.itemView.setOnClickListener(v -> {
            try {
                String placeID = item.getPlaceId();

                /**
                 * To specify which data types to return, pass an array of Place.Fields in your FetchPlaceRequest
                 * Use only those fields which are required.
                 **/
                List<com.google.android.libraries.places.api.model.Place.Field> placeFields = Arrays.asList(
                        com.google.android.libraries.places.api.model.Place.Field.ID,
                        com.google.android.libraries.places.api.model.Place.Field.NAME,
                        com.google.android.libraries.places.api.model.Place.Field.ADDRESS,
                        com.google.android.libraries.places.api.model.Place.Field.LAT_LNG);

                FetchPlaceRequest request = FetchPlaceRequest.builder(placeID, placeFields).build();

                Constants.PLACES_CLIENT.fetchPlace(request)
                        .addOnSuccessListener(task -> {
                            com.google.android.libraries.places.api.model.Place place = task.getPlace();
                            LatLng latLng = place.getLatLng();
                            if(latLng == null) return;
                            if (mOnPlaceSelectListener != null)
                                mOnPlaceSelectListener.onSelect(new Place(place.getName(), place.getAddress(), latLng.latitude, latLng.longitude));
                        })
                        .addOnFailureListener(Throwable::printStackTrace);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private AutocompletePrediction getItem(int position) {
        return mResultList.get(position);
    }

    public void clearAll(){
        mResultList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();

                List<AutocompletePrediction> filterData = new ArrayList<>();

                if (charSequence != null) filterData = getAutocomplete(charSequence);

                results.values = filterData;
                results.count = filterData != null ? filterData.size() : 0;

                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                try {
                    mResultList.clear();
                    mResultList.addAll((List) results.values);
                    notifyDataSetChanged();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof AutocompletePrediction) {
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                }
                else {
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }

    private List<AutocompletePrediction> getAutocomplete(CharSequence constraint) {
        /**
         * BD RectangularBounds
         **/
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(20.3756582, 88.0075306),
                new LatLng(26.6382534, 92.6804979)
        );

        FindAutocompletePredictionsRequest.Builder requestBuilder = FindAutocompletePredictionsRequest.builder()
                .setQuery(constraint.toString())
                .setCountry("BD")
                .setLocationBias(bounds)
                .setSessionToken(AutocompleteSessionToken.newInstance())
                .setTypeFilter(TypeFilter.ADDRESS);

        Task<FindAutocompletePredictionsResponse> results = Constants.PLACES_CLIENT.findAutocompletePredictions(requestBuilder.build());

        /**
         * Wait to get results
         **/
        try {
            Tasks.await(results, 60, TimeUnit.SECONDS);
        }
        catch (ExecutionException | InterruptedException | TimeoutException ex) {
            ex.printStackTrace();
        }

        if (results.isSuccessful()) {
            if (results.getResult() != null) {
                return results.getResult().getAutocompletePredictions();
            }
            else return null;
        }
        else return null;
    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }

    public void setOnPlaceSelectListener(OnPlaceSelectListener onPlaceSelectListener) {
        this.mOnPlaceSelectListener = onPlaceSelectListener;
    }

    public interface OnPlaceSelectListener {
        void onSelect(Place place);
    }
}
