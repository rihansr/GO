package com.go.driver.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.go.driver.R;
import com.go.driver.model.trip.Message;
import com.go.driver.util.extension.DateExtensions;
import com.go.driver.util.extension.AppExtensions;
import com.go.driver.wiget.CircleImageView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context           mContext;
    private List<Message>     messages = new ArrayList<>();
    private OnMessageListener mOnMessageListener;

    public ChatAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setMessages(HashMap<String, Message> messagesMap) {
        if(messagesMap != null) {
            List<Message> messages = new ArrayList<>(messagesMap.values());
            Collections.sort(messages);
            this.messages = messages;
            notifyDataSetChanged();
            if(mOnMessageListener != null) mOnMessageListener.notifyMessage(messages.size() - 1);
        }
        else {
            this.messages = new ArrayList<>();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getMessage(position).getId();
    }

    private Message getMessage(int position){
        return messages.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            default:
            case 1:
                View senderView = layoutInflater.inflate(R.layout.sample_sender_message, parent, false);
                return new SenderViewHolder(senderView);

            case 0:
                View receiverView = layoutInflater.inflate(R.layout.sample_receiver_message, parent, false);
                return new ReceiverViewHolder(receiverView);
        }
    }

    private static class SenderViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView   message;
        private AppCompatTextView   sentAt;
        private View                divider;

        private SenderViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.message);
            sentAt = v.findViewById(R.id.sentAt);
            divider = v.findViewById(R.id.divider);
        }
    }

    private static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView     photo;
        private AppCompatTextView   message;
        private AppCompatTextView   sentAt;
        private View                divider;

        private ReceiverViewHolder(View v) {
            super(v);
            photo = v.findViewById(R.id.photo);
            message = v.findViewById(R.id.message);
            sentAt = v.findViewById(R.id.sentAt);
            divider = v.findViewById(R.id.divider);
        }
    }

    @Override
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final Message message = getMessage(position);

        if(holder instanceof SenderViewHolder){
            SenderViewHolder senderViewHolder = (SenderViewHolder) holder;

            senderViewHolder.divider.setVisibility(isConditionMatch(position, 1) ? View.VISIBLE : View.GONE);

            senderViewHolder.message.setText(message.getMessage());
            senderViewHolder.sentAt.setText(new DateExtensions(mContext, message.getSentAt()).defaultTimeFormat());

            senderViewHolder.itemView.setOnClickListener(v ->
                    senderViewHolder.sentAt.setVisibility(senderViewHolder.sentAt.getVisibility() == View.GONE ? View.VISIBLE : View.GONE)
            );

        }
        else {
            ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;

            receiverViewHolder.divider.setVisibility(isConditionMatch(position, 0) ? View.VISIBLE : View.GONE);

            if (isConditionMatch(position, 0)) {
                int placeHolder = message.getGender() == null || message.getGender().equals(AppExtensions.Gender.Male.toString()) ? R.drawable.ic_avatar_male : R.drawable.ic_avatar_female;
                AppExtensions.loadPhoto(receiverViewHolder.photo, message.getPhoto(), R.dimen.icon_Size_Medium, placeHolder);
            }
            else {
                receiverViewHolder.photo.setImageDrawable(null);
            }

            receiverViewHolder.message.setText(message.getMessage());
            receiverViewHolder.sentAt.setText(new DateExtensions(mContext, message.getSentAt()).defaultTimeFormat());

            receiverViewHolder.itemView.setOnClickListener(v ->
                    receiverViewHolder.sentAt.setVisibility(receiverViewHolder.sentAt.getVisibility() == View.GONE ? View.VISIBLE : View.GONE)
            );
        }
    }

    public boolean isConditionMatch(int position, int id){
        return (position + 1) == getItemCount() || getMessage(position + 1).getId() != id;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.mOnMessageListener = onMessageListener;
    }

    public interface OnMessageListener {
        void notifyMessage(int itemCount);
    }
}
