package com.go.rider.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.go.rider.R;
import com.go.rider.model.location.Place;
import com.go.rider.util.extension.AppExtensions;

import java.util.ArrayList;
import java.util.List;

public class SavePlaceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context         mContext;
    private List<Place>     places = new ArrayList<>();
    private OnPlaceListener mOnPlaceListener;
    private int             viewType;

    public SavePlaceAdapter(Context mContext, int viewType) {
        this.mContext = mContext;
        this.viewType = viewType;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            default:
            case 0:
                View savePlaceView = layoutInflater.inflate(R.layout.sample_save_place, parent, false);
                return new PlaceViewHolder(savePlaceView);

            case 1:
                View searchPlaceView = layoutInflater.inflate(R.layout.sample_search_place, parent, false);
                return new SearchViewHolder(searchPlaceView);
        }
    }

    private static class PlaceViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView  icon;
        private AppCompatTextView   title;
        private AppCompatTextView   address;
        private AppCompatImageView  moreBtn;

        private PlaceViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            title = v.findViewById(R.id.title);
            address = v.findViewById(R.id.address);
            moreBtn = v.findViewById(R.id.moreBtn);
        }
    }

    private static class SearchViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView  icon;
        private AppCompatTextView   title;
        private AppCompatTextView   address;

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

        private SearchViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            title = v.findViewById(R.id.title);
            address = v.findViewById(R.id.address);
        }
    }

    @Override
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final Place place = places.get(position);

        if(holder instanceof PlaceViewHolder){
            PlaceViewHolder placeViewHolder = (PlaceViewHolder) holder;

            switch (position) {
                case 0:
                    placeViewHolder.title.setText(place.getAddress() != null ? place.getTitle() : AppExtensions.getString(R.string.addHome));
                    placeViewHolder.icon.setImageResource(R.drawable.ic_place_home);
                    break;

                case 1:
                    placeViewHolder.title.setText(place.getAddress() != null ? place.getTitle() : AppExtensions.getString(R.string.addWork));
                    placeViewHolder.icon.setImageResource(R.drawable.ic_place_work);
                    break;

                default:
                    placeViewHolder.title.setText(place.getAddress() != null ? place.getTitle() : AppExtensions.getString(R.string.addOther));
                    placeViewHolder.icon.setImageResource(R.drawable.ic_place_other);
            }

            placeViewHolder.address.setVisibility(place.getAddress() != null ? View.VISIBLE : View.GONE);
            placeViewHolder.address.setText(place.getAddress());

            placeViewHolder.moreBtn.setOnClickListener(v -> {
                AppExtensions.createPopupMenu(v, new Object[]{AppExtensions.Action.Edit, AppExtensions.Action.Delete}, item -> {
                    switch (AppExtensions.Action.valueOf((String) item.getTitle())) {
                        case Edit:
                            switch (position) {
                                case 0: place.setIcon(R.drawable.ic_place_home);    break;
                                case 1: place.setIcon(R.drawable.ic_place_work);    break;
                                default: place.setIcon(R.drawable.ic_place_other);
                            }
                            if(mOnPlaceListener != null) mOnPlaceListener.onEdit(position, place);
                            break;

                        case Delete:
                            if(mOnPlaceListener != null) mOnPlaceListener.onDelete(position);
                            break;
                    }

                    return false;
                });
            });
        }
        else {
            SearchViewHolder searchViewHolder = (SearchViewHolder) holder;
            searchViewHolder.setVisibility(false);

            if(place.getAddress() == null) return;

            searchViewHolder.setVisibility(true);

            switch (position) {
                case 0: searchViewHolder.icon.setImageResource(R.drawable.ic_place_home);   break;
                case 1: searchViewHolder.icon.setImageResource(R.drawable.ic_place_work);   break;
                default: searchViewHolder.icon.setImageResource(R.drawable.ic_place_other);
            }

            searchViewHolder.title.setText(place.getTitle());
            searchViewHolder.address.setText(place.getAddress());
        }

        holder.itemView.setOnClickListener(v -> {
            if(mOnPlaceListener != null) mOnPlaceListener.onSelect(place);
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public void setOnPlaceListener(OnPlaceListener onPlaceListener) {
        this.mOnPlaceListener = onPlaceListener;
    }

    public interface OnPlaceListener {
        void onSelect(Place place);
        void onEdit(int position, Place place);
        void onDelete(int position);
    }
}
