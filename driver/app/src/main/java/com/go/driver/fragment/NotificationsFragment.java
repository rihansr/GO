package com.go.driver.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.go.driver.R;
import com.go.driver.activity.HomeActivity;
import com.go.driver.adapter.NotificationAdapter;
import com.go.driver.model.notification.Notification;
import com.go.driver.util.Constants;
import com.go.driver.util.extension.AppExtensions;
import com.go.driver.util.recyclerView.DividerItemDecoration;
import com.go.driver.util.recyclerView.VerticalSpaceItemDecoration;

import java.io.Serializable;
import java.util.List;

import static com.go.driver.activity.HomeActivity.drawerLayout;

public class
NotificationsFragment extends DialogFragment implements HomeActivity.OnNotificationListener{

    private static final String     TAG = NotificationsFragment.class.getSimpleName();

    /**
     * Toolbar
     **/
    private AppCompatTextView   title;
    private AppCompatImageView  backBtn;

    /**
     * Other
     **/
    private RecyclerView        rcvNotifications;
    private NotificationAdapter notificationAdapter;
    private Context             context;
    private Activity            activity;
    private LottieAnimationView empty;

    public static NotificationsFragment show(@NonNull HomeActivity homeActivity, List<Notification> notifications){
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.NOTIFICATIONS_INTENT_KEY, (Serializable) notifications);
        fragment.setArguments(args);
        fragment.show(homeActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_TopLight_SliderAnimation);
        setRetainInstance(true);
        setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        new Handler().postDelayed(() -> drawerLayout.closeDrawer(GravityCompat.START), Constants.SLIDE_OUT_DURATION);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_items, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), true);
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        title = view.findViewById(R.id.title);
        backBtn = view.findViewById(R.id.leftBtn);
        rcvNotifications = view.findViewById(R.id.rcvItems);
        empty = view.findViewById(R.id.empty);
    }

    private void init(){
        if (getArguments() == null) { dismiss(); return; }

        List<Notification> notifications = (List<Notification>) getArguments().getSerializable(Constants.NOTIFICATIONS_INTENT_KEY);
        getArguments().remove(Constants.NOTIFICATIONS_INTENT_KEY);

        ((HomeActivity) activity).setOnNotificationListener(this);

        title.setText(AppExtensions.getString(R.string.notification));
        backBtn.setOnClickListener(v -> dismiss());

        setTripsAdapter();

        empty.setVisibility(notifications == null || notifications.size() == 0 ? View.VISIBLE : View.GONE);
        notificationAdapter.setNotifications(notifications);
    }

    private void setTripsAdapter() {
        rcvNotifications.addItemDecoration(new VerticalSpaceItemDecoration(32));
        rcvNotifications.addItemDecoration(new DividerItemDecoration(context, R.drawable.shape_divider));
        notificationAdapter = new NotificationAdapter(context);
        rcvNotifications.setAdapter(notificationAdapter);
    }

    @Override
    public void onNotificationReceive(List<Notification> notifications) {
        notificationAdapter.setNotifications(notifications);
        empty.setVisibility(notifications == null || notifications.size() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        this.activity = (Activity) context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
