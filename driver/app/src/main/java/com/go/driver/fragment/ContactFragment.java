package com.go.driver.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.go.driver.R;
import com.go.driver.activity.HomeActivity;
import com.go.driver.adapter.ChatAdapter;
import com.go.driver.adapter.MessageAdapter;
import com.go.driver.model.trip.Message;
import com.go.driver.model.trip.TripInfo;
import com.go.driver.remote.FirebaseHelper;
import com.go.driver.remote.NotificationHelper;
import com.go.driver.remote.PermissionManager;
import com.go.driver.util.Constants;
import com.go.driver.util.CustomSnackBar;
import com.go.driver.util.extension.DateExtensions;
import com.go.driver.util.TempStorage;
import com.go.driver.util.extension.AppExtensions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class ContactFragment extends DialogFragment implements HomeActivity.OnMessageListener, HomeActivity.OnDismissListener {

    private static final String     TAG = ContactFragment.class.getSimpleName();

    /**
     * Toolbar
     **/
    private AppCompatTextView       riderName;
    private AppCompatImageView      backBtn;
    private AppCompatImageView      callBtn;

    /**
     * Other
     **/
    private RecyclerView            rcvMessages;
    private ChatAdapter             chatAdapter;
    private RecyclerView            rcvDefaultMessages;
    private MessageAdapter          messageAdapter;
    private AppCompatEditText       message_Input;
    private AppCompatImageButton    sendBtn;
    private TripInfo                tripInfo = null;
    private FirebaseHelper          firebaseHelper;
    private OnContactListener       mOnContactListener;
    private Context                 context;
    private Activity                activity;

    public static ContactFragment show(@NonNull HomeActivity homeActivity, TripInfo tripInfo){
        ContactFragment fragment = new ContactFragment();
        if(tripInfo != null){
            Bundle args = new Bundle();
            args.putSerializable(Constants.TRIP_INFO_INTENT_KEY, tripInfo);
            fragment.setArguments(args);
        }
        fragment.show(homeActivity.getSupportFragmentManager(), TAG);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Dialog_TopLight_FadeAnimation);
        setRetainInstance(true);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        AppExtensions.fullScreenDialog(getDialog(), true);
        super.onViewCreated(view, savedInstanceState);

        idSetup(view);

        init();
    }

    private void idSetup(View view) {
        riderName = view.findViewById(R.id.title);
        backBtn = view.findViewById(R.id.leftBtn);
        callBtn = view.findViewById(R.id.rightBtn);
        callBtn.setVisibility(View.VISIBLE);

        rcvMessages = view.findViewById(R.id.rcvMessages);
        message_Input = view.findViewById(R.id.message_Input_Etxt);
        rcvDefaultMessages = view.findViewById(R.id.rcvDefaultMessages);
        message_Input = view.findViewById(R.id.message_Input_Etxt);
        sendBtn = view.findViewById(R.id.sendBtn);

        firebaseHelper = new FirebaseHelper(context);
        chatAdapter = new ChatAdapter(context);
        messageAdapter = new MessageAdapter(0);
    }

    private void init(){
        if (getArguments() == null) { dismiss(); return; }

        tripInfo = (TripInfo) getArguments().getSerializable(Constants.TRIP_INFO_INTENT_KEY);
        getArguments().remove(Constants.TRIP_INFO_INTENT_KEY);
        if (tripInfo == null) { dismiss(); return; }

        ((HomeActivity) activity).setOnMessageListener(this);
        ((HomeActivity) activity).setOnDismissListener(this);

        riderName.setText(tripInfo.getRider().getName());

        backBtn.setOnClickListener(v -> dismiss());

        callBtn.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tripInfo.getRider().getPhone()));

            if (!new PermissionManager(PermissionManager.Permission.PHONE, true, response -> startActivity(callIntent) ).isGranted()) return;

            AlertDialogFragment.show((HomeActivity)context, tripInfo.getRider().getPhone(), null, R.string.cancel, R.string.call)
                    .setOnDialogListener(new AlertDialogFragment.OnDialogListener() {
                        @Override
                        public void onLeftButtonClick() {}

                        @Override
                        public void onRightButtonClick() {
                            startActivity(callIntent);
                        }
                    });
        });

        setMessageAdapter();

        sendBtn.setOnClickListener(v -> {
            String getMessage = Objects.requireNonNull(message_Input.getText()).toString().trim();
            if(!getMessage.isEmpty()) sentMessage(-1, getMessage);
        });
    }

    private void setMessageAdapter() {
        rcvMessages.setAdapter(chatAdapter);
        chatAdapter.setMessages(tripInfo.getMessages());
        chatAdapter.setOnMessageListener(bottomPosition -> rcvMessages.smoothScrollToPosition(bottomPosition));

        rcvDefaultMessages.setAdapter(messageAdapter);
        messageAdapter.setData(TempStorage.getOneClickChats());
        messageAdapter.setOnMessageListener(this::sentMessage);
    }

    private void sentMessage(int position, String senderMessage) {
        if(!Constants.IS_INTERNET_CONNECTED){
            new CustomSnackBar(AppExtensions.getRootView(getDialog()), R.string.network_Error, R.string.retry, CustomSnackBar.Duration.LONG).show();
            return;
        }

        DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference(FirebaseHelper.TRIP_REQUESTS_TABLE).child(tripInfo.getTripId());

        Message message = new Message();
        message.setId(1);
        message.setSenderId(TempStorage.DRIVER.getId());
        message.setPhoto(TempStorage.DRIVER.getProfilePhoto());
        message.setGender(TempStorage.DRIVER.getGender());
        message.setName(TempStorage.DRIVER.getName());
        message.setMessage(senderMessage);
        message.setSentAt(DateExtensions.currentTime());

        firebaseHelper.setData(tripReference.child(FirebaseHelper.TRIP_MESSAGES_KEY).child(String.valueOf(message.getSentAt())).setValue(message),
                new FirebaseHelper.OnFirebaseUpdateListener() {
                    @Override public void onSuccess() {
                        message_Input.setText(null);
                        new NotificationHelper(tripInfo.getRider().getToken(), "Message From Driver", senderMessage).send();
                    }
                    @Override public void onFailure() {}
                });
    }

    @Override
    public void onMessageReceive(HashMap<String, Message> messagesMap) {
        chatAdapter.setMessages(messagesMap);
    }

    public void setOnContactListener(OnContactListener mOnContactListener) {
        this.mOnContactListener = mOnContactListener;
    }

    public interface OnContactListener {
        void onDismiss();
    }

    @Override
    public void onDismiss() {
        dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mOnContactListener != null) mOnContactListener.onDismiss();
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
