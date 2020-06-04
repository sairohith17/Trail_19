package gst.trainingcourse.instagramclone.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.adapters.PostAdapter;
import gst.trainingcourse.instagramclone.models.Post;

public class PostDetailFragment extends BaseFragment {

    private RecyclerView mRecyclerViewPost;
    private List<Post> mListPost = new ArrayList<>();
    private List<String> mListPostSave = new ArrayList<>();
    private PostAdapter mPostAdapter;
    private String mPostId, mPublisher;
    private int mPosition;
    private boolean mCheck;

    private static final String PROFILE_FRAGMENT = "NewProfileFragment";
    public static final String ACTION_NEW_PROFILE = PROFILE_FRAGMENT + "action.newprofile";
    private static final String EXTRA_IS_ROOT_FRAGMENT = ".extra_is_root_fragment";

    public PostDetailFragment() {
        // Required empty public constructor
    }

    public static PostDetailFragment newInstance(boolean isRoot) {

        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_ROOT_FRAGMENT, isRoot);
        PostDetailFragment fragment = new PostDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initData();
        setAdapter();
        if (!mPostId.equals("none") && mPublisher.equals("none")) {
            readPost();
        }
        if (mPublisher.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            readPostFoto();
        } else {
            if (!mPublisher.equals("none")) {
                if (mCheck) {
                    readPostSaveFoto();
                } else {
                    readPostFoto();
                }
            }
        }
    }

    private void initView(View view) {
        mRecyclerViewPost = view.findViewById(R.id.recyclerviewPost);
    }

    private void initData() {
        SharedPreferences preferences = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        mPostId = preferences.getString("postid", "none");
        mPublisher = preferences.getString("publisher", "none");
        mPosition = preferences.getInt("position", 0);
        mCheck = preferences.getBoolean("check", true);
    }

    private void setAdapter() {
        mPostAdapter = new PostAdapter(getContext(), mListPost, ACTION_NEW_PROFILE, currentTab, fragmentInteractionCallback);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerViewPost.setLayoutManager(linearLayoutManager);
        mRecyclerViewPost.setAdapter(mPostAdapter);
    }

    private void readPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(mPostId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListPost.clear();
                Post post = dataSnapshot.getValue(Post.class);
                mListPost.add(post);
                mPostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readPostFoto() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("publisher").equalTo(mPublisher);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListPost.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    mListPost.add(post);
                }
                Collections.reverse(mListPost);
                mPostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerViewPost.scrollToPosition(mPosition);
            }
        }, 200);
    }

    private void readPostSaveFoto() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListPostSave.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mListPostSave.add(snapshot.getKey());
                }
                Test();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerViewPost.scrollToPosition(mPosition);
            }
        }, 200);
    }

    private void Test() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListPost.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    for (String id : mListPostSave) {
                        if (id.equals(post.getPostid())) {
                            mListPost.add(post);
                        }
                    }
                }
                Collections.reverse(mListPost);
                mPostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
