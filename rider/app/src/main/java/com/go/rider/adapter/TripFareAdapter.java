package com.go.rider.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.go.rider.R;
import com.go.rider.model.trip.Fare;
import com.go.rider.model.trip.TripInfo;
import com.go.rider.model.vehicle.VehicleType;
import com.go.rider.util.extension.DateExtensions;
import com.go.rider.util.extension.AppExtensions;

import java.util.ArrayList;
import java.util.List;

public class TripFareAdapter extends RecyclerView.Adapter<TripFareAdapter.ViewHolder> {

    private Context                 mContext;
    private List<VehicleType>       vehicleTypes = new ArrayList<>();
    private OnTypeSelectListener    mOnTypeSelectListener;
    private TripInfo                tripInfo = new TripInfo();
    private int                     checkedPosition = -1;

    public TripFareAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setTypes(List<VehicleType> vehicleTypes, TripInfo tripInfo) {
        checkedPosition = -1;
        this.vehicleTypes = vehicleTypes;
        this.tripInfo = tripInfo;
        notifyDataSetChanged();
    }

    public void clearAllTypes(){
        vehicleTypes.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripFareAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.sample_trip_fare, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayoutCompat  itemHolder;
        private AppCompatImageView  vehicleIcon;
        private AppCompatTextView   vehicleType;
        private AppCompatTextView   vehicleCapacity;
        private AppCompatTextView   tripFare;
        private AppCompatTextView   dropOffTime;

        private ViewHolder(View v) {
            super(v);
            itemHolder = v.findViewById(R.id.itemHolder);
            vehicleIcon = v.findViewById(R.id.vehicleIcon);
            vehicleType = v.findViewById(R.id.vehicleType);
            vehicleCapacity = v.findViewById(R.id.vehicleCapacity);
            tripFare = v.findViewById(R.id.tripFare);
            dropOffTime = v.findViewById(R.id.dropOffTime);
        }

        public void setVisibility(boolean isVisible) {
            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (isVisible) {
                param.height = LinearLayoutCompat.LayoutParams.WRAP_CONTENT;
                param.width = LinearLayoutCompat.LayoutParams.MATCH_PARENT;
                itemView.setVisibility(View.VISIBLE);
            }
            else {
                param.height = 0;
                param.width = 0;
                itemView.setVisibility(View.GONE);
            }

            itemView.setLayoutParams(param);
        }
    }

    @Override
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    public void onBindViewHolder(@NonNull final TripFareAdapter.ViewHolder holder, final int position) {
        VehicleType type = getType(position);

        holder.itemHolder.setBackgroundColor(checkedPosition == position
                ?
                ContextCompat.getColor(mContext, R.color.colorTrans)
                :
                Color.WHITE
        );

        if(checkedPosition == -1){
            tripInfo.setFare(getTripFare(getType(0)));
            tripInfo.setVehicleType(new VehicleType(getType(0), true));
            if(mOnTypeSelectListener != null) mOnTypeSelectListener.onTypeSelect(tripInfo);
            holder.itemHolder.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorTrans));
            checkedPosition = 0;
        }

        if(!type.isAvailable()) {
            holder.setVisibility(false);
            return;
        }

        switch (AppExtensions.Vehicle.valueOf(type.getType())){
            case Car:
                holder.vehicleIcon.setImageResource(R.drawable.ic_car);
                break;

            case Bike:
                holder.vehicleIcon.setImageResource(R.drawable.ic_bike);
                break;

            case Taxi:
                holder.vehicleIcon.setImageResource(R.drawable.ic_taxi);
                break;
        }

        holder.tripFare.setText(AppExtensions.decimalFormat(getTripFare(type).getTotalFare(), "#.##", false));

        holder.vehicleType.setText(type.getType());
        holder.vehicleCapacity.setText(type.getCapacity() + "");
        holder.dropOffTime.setText(new DateExtensions(tripInfo.getDropOffTime()).defaultTimeFormat().toLowerCase()
                + " " + AppExtensions.getString(R.string.dropOff));

        holder.itemView.setOnClickListener(v -> {
            if(checkedPosition == position) return;
            notifyDataSetChanged();
            checkedPosition = position;
            tripInfo.setFare(getTripFare(type));
            tripInfo.setVehicleType(new VehicleType(type, true));
            if(mOnTypeSelectListener != null) mOnTypeSelectListener.onTypeSelect(tripInfo);
        });
    }

    private VehicleType getType(int position) {
        return vehicleTypes.get(position);
    }

    private Fare getTripFare(VehicleType type) {
        /**
         * MinFare < (BaseFare + (PerMinRate * min) + (PerKmRate * km))
         **/
        double calculateFare = type.getBaseFare()
                +
                (type.getWaitingCharge() * (double) ((tripInfo.getEstimatedTime() / 1000) / 60))
                +
                (type.getPerKmRate() * (tripInfo.getEstimatedDistance() / 1000));

        double tripFare = Math.round(calculateFare > type.getMinFare() ? calculateFare : type.getMinFare());
        double promo = Math.round(0);
        double total = tripFare - promo;

        return new Fare(tripFare, promo, total);
    }

    @Override
    public int getItemCount() {
        return vehicleTypes.size();
    }

    public void setOnTypeSelectListener(OnTypeSelectListener onTypeSelectListener) {
        this.mOnTypeSelectListener = onTypeSelectListener;
    }

    public interface OnTypeSelectListener {
        void onTypeSelect(TripInfo info);
    }
}
