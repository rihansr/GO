package com.go.rider.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.go.rider.R;
import com.go.rider.model.trip.TripInfo;
import com.go.rider.util.extension.DateExtensions;
import com.go.rider.util.extension.AppExtensions;

import java.util.ArrayList;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    private Context         mContext;
    private List<TripInfo>  trips = new ArrayList<>();
    private OnTripListener  mOnTripListener;

    public TripAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setTrips(List<TripInfo> trips) {
        this.trips = trips != null && trips.size() != 0 ? trips : new ArrayList<>();
        notifyDataSetChanged();
    }

    private TripInfo getTripInfo(int position){
        return trips.get(position);
    }

    @NonNull
    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.sample_trip_info, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView   tripDate;
        private AppCompatTextView   tripFare;
        private AppCompatTextView   vehicleModel;
        private AppCompatRatingBar  riderRating;
        private AppCompatTextView   pickUpLocation;
        private AppCompatTextView   dropOffLocation;

        private ViewHolder(View v) {
            super(v);
            tripDate = v.findViewById(R.id.tripDate);
            tripFare = v.findViewById(R.id.tripFare);
            vehicleModel = v.findViewById(R.id.vehicleModel);
            riderRating = v.findViewById(R.id.riderRating);
            pickUpLocation = v.findViewById(R.id.pickUpLocation);
            dropOffLocation = v.findViewById(R.id.dropOffLocation);
        }
    }

    @Override
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    public void onBindViewHolder(@NonNull final TripAdapter.ViewHolder holder, final int position) {
        holder.tripDate.setText(new DateExtensions(mContext, getTripInfo(position).getPickUpTime()).defaultDateTimeFormat());
        holder.tripFare.setText(AppExtensions.decimalFormat(getTripInfo(position).getFare().getTotalFare(), "#.##", false));
        holder.vehicleModel.setText(getTripInfo(position).getVehicleType().getModel());
        holder.riderRating.setVisibility(getTripInfo(position).getRiderRating() != null ? View.VISIBLE : View.GONE);
        holder.riderRating.setRating(getTripInfo(position).getRiderRating() != null ? getTripInfo(position).getRiderRating() : 0);
        holder.pickUpLocation.setText(getTripInfo(position).getPickUpLocation().getAddress());
        holder.dropOffLocation.setText(getTripInfo(position).getDropOffLocation().getAddress());
        holder.itemView.setOnClickListener(v -> mOnTripListener.onClick(getTripInfo(position)));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public void setOnTripListener(OnTripListener onTripListener) {
        this.mOnTripListener = onTripListener;
    }

    public interface OnTripListener {
        void onClick(TripInfo tripInfo);
    }
}
