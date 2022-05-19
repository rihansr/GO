package com.go.rider.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.go.rider.R;
import com.go.rider.model.address.District;

import java.util.LinkedList;
import java.util.List;

public class TestAdapter extends ArrayAdapter<District> {

    private List<District> all_Districts;

    public TestAdapter(@NonNull Context context, List<District> districts) {
        super(context, 0, districts);
        all_Districts = new LinkedList<>(districts);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return districtFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull final ViewGroup parent) {
        if(view == null) view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_item, parent, false);

        AppCompatTextView itemName = view.findViewById(R.id.itemTv);

        final District district = getItem(position);

        if(district != null) itemName.setText(district.getName());

        return view;
    }

    private Filter districtFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<District> filtered_Districts = new LinkedList<>();

            if(charSequence == null || charSequence.length() == 0){
                filtered_Districts.addAll(all_Districts);
            }
            else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (District district : all_Districts){
                    if(district.getName().toLowerCase().contains(filterPattern)){
                        filtered_Districts.add(district);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filtered_Districts;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            clear();
            addAll((List) filterResults.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((District) resultValue).getName();
        }
    };
}
