package gst.trainingcourse.instagramclone.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.adapters.HomeAdapter;
import gst.trainingcourse.instagramclone.models.Users;

public class HomeFragment extends BaseFragment {

    private RecyclerView mRecyclerViewPost;
    private HomeAdapter mHomeAdapter;
    private List<Users> mListUsername = new ArrayList<>();
    private List<String> mListFollowing = new ArrayList<>();
    private ProgressBar mPbWaiting;

    private static final String PROFILE_FRAGMENT = "ProfileFragment";
    public static final String ACTION_PROFILE = PROFILE_FRAGMENT + "action.profile";
    private static final String EXTRA_IS_ROOT_FRAGMENT = ".extra_is_root_fragment";

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(boolean isRoot) {

        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_ROOT_FRAGMENT, isRoot);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        setAdapter();
        checkFollowing();
    }

    private void initView(View view) {
        mRecyclerViewPost = view.findViewById(R.id.recyclerviewPost);
        mPbWaiting = view.findViewById(R.id.pbWaiting);
    }

    private void setAdapter() {
        mHomeAdapter = new HomeAdapter(getContext(), mListUsername, ACTION_PROFILE, currentTab, fragmentInteractionCallback);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        mRecyclerViewPost.setLayoutManager(linearLayoutManager);
        mRecyclerViewPost.setAdapter(mHomeAdapter);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerViewPost);
    }

    private void checkFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListFollowing.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mListFollowing.add(snapshot.getKey());
                }

                readPost();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListUsername.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users users = snapshot.getValue(Users.class);
                    for (String id : mListFollowing) {
                        if (id.equals(snapshot.getKey())) {
                            mListUsername.add(users);
                        }
                    }
                }
                Collections.shuffle(mListUsername);
                mHomeAdapter.notifyDataSetChanged();
                mPbWaiting.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
