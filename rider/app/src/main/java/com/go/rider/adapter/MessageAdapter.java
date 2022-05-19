package com.go.rider.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.go.rider.R;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int               viewType;
    private List<String>      messages = new ArrayList<>();
    private OnMessageListener mOnMessageListener;

    public MessageAdapter(int viewType) {
        this.viewType = viewType;
    }

    public void setData(List<String> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    private String getMessage(int position){
        return messages.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (this.viewType){
            case 0: return new MessageViewHolder(layoutInflater.inflate(R.layout.sample_message, parent, false));
            case 1: return new MessageViewHolder(layoutInflater.inflate(R.layout.sample_notes, parent, false));
            case 2: return new MessageViewHolder(layoutInflater.inflate(R.layout.sample_issue, parent, false));
            case 3: return new MessageViewHolder(layoutInflater.inflate(R.layout.sample_report, parent, false));
            default: return new MessageViewHolder(layoutInflater.inflate(R.layout.sample_item, parent, false));
        }
    }

    private static class MessageViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView itemTv;

        private MessageViewHolder(View v) {
            super(v);
            itemTv = v.findViewById(R.id.itemTv);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        MessageViewHolder itemHolder = (MessageViewHolder) viewHolder;
        itemHolder.itemTv.setText(getMessage(position));
        itemHolder.itemView.setOnClickListener(v -> mOnMessageListener.onClick(position, getMessage(position)));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.mOnMessageListener = onMessageListener;
    }

    public interface OnMessageListener {
        void onClick(int position, String message);
    }
}
