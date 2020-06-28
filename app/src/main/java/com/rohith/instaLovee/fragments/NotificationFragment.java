package com.rohith.instaLovee.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rohith.instaLovee.R;
import com.rohith.instaLovee.adapters.NotificationAdapter;
import com.rohith.instaLovee.models.Notification;

public class NotificationFragment extends BaseFragment {

    private RecyclerView mRecyclerViewNoti;
    private List<Notification> mListNoti = new ArrayList<>();
    private NotificationAdapter mNotificationAdapter;

    private static final String NOTIFICATIONS_FRAGMENT = "NotificationsFragment";
    public static final String ACTION_NOTI_PROFILE = NOTIFICATIONS_FRAGMENT + "action.notifi";
    public static final String ACTION_NOTI_POST_DETAIL = NOTIFICATIONS_FRAGMENT + "action.notifidetail";
    private static final String EXTRA_IS_ROOT_FRAGMENT = ".extra_is_root_fragment";

    public NotificationFragment() {
        // Required empty public constructor
    }

    public static NotificationFragment newInstance(boolean isRoot) {

        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_ROOT_FRAGMENT, isRoot);
        NotificationFragment fragment = new NotificationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerViewNoti = view.findViewById(R.id.recyclerviewNoti);
        setAdapter();
        readNotification();
    }

    private void setAdapter() {
        mNotificationAdapter = new NotificationAdapter(getContext(), mListNoti, ACTION_NOTI_POST_DETAIL, ACTION_NOTI_PROFILE, currentTab, fragmentInteractionCallback);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerViewNoti.setLayoutManager(linearLayoutManager);
        mRecyclerViewNoti.setAdapter(mNotificationAdapter);
    }

    private void readNotification() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListNoti.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    mListNoti.add(notification);
                }
                Collections.reverse(mListNoti);
                mNotificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
