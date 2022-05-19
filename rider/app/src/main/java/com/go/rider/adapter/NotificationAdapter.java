package com.go.rider.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.go.rider.R;
import com.go.rider.model.notification.Notification;
import com.go.rider.util.extension.AppExtensions;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context                 mContext;
    private List<Notification>      notifications = new ArrayList<>();
    private OnNotificationListener  mOnNotificationListener;

    public NotificationAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null && notifications.size() != 0 ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    private Notification getNotification(int position){
        return notifications.get(position);
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.sample_notification, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private AppCompatImageView  photo;
        private AppCompatTextView   title;
        private AppCompatTextView   message;

        private ViewHolder(View v) {
            super(v);
            photo = v.findViewById(R.id.photo);
            title = v.findViewById(R.id.title);
            message = v.findViewById(R.id.message);
        }
    }

    @Override
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    public void onBindViewHolder(@NonNull final NotificationAdapter.ViewHolder holder, final int position) {
        if (getNotification(position).getPhoto() != null) {
            AppExtensions.loadPhoto(holder.photo, getNotification(position).getPhoto(), R.dimen.icon_Size_Medium, R.drawable.shape_divider);
        }
        else {
            holder.photo.setImageDrawable(null);
        }

        holder.title.setVisibility(getNotification(position).getTitle() != null ? View.VISIBLE : View.GONE);
        holder.title.setText(getNotification(position).getTitle());

        holder.message.setVisibility(getNotification(position).getMessage() != null ? View.VISIBLE : View.GONE);
        holder.message.setText(getNotification(position).getMessage());

        holder.itemView.setOnClickListener(v -> mOnNotificationListener.onClick(getNotification(position)));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setOnNotificationListener(OnNotificationListener onNotificationListener) {
        this.mOnNotificationListener = onNotificationListener;
    }

    public interface OnNotificationListener {
        void onClick(Notification notification);
    }
}
