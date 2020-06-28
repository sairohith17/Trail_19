package com.rohith.instaLovee.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import com.rohith.instaLovee.activity.EditProfileActivity;
import com.rohith.instaLovee.activity.FollowAndLikeActivity;
import com.rohith.instaLovee.activity.OptionActivity;
import com.rohith.instaLovee.R;
import com.rohith.instaLovee.activity.StoryActivity;
import com.rohith.instaLovee.adapters.MyFotoAdapter;
import com.rohith.instaLovee.adapters.StoryAdapter;
import com.rohith.instaLovee.models.Post;
import com.rohith.instaLovee.models.Story;
import com.rohith.instaLovee.models.Users;

public class ProfileFragment extends BaseFragment {

    private CircleImageView mImgProfile, mImgStories, mImgStoriesSeen;
    private ImageView mImgOption;
    private TextView mTxtUsername, mTxtFullname, mTxtPost, mTxtFollowing, mTxtFollowers, mTxtBio;
    private Button mBtnEditProfile;
    private ImageButton mImgBtnFoto, mImgBtnFotoSave;
    private RecyclerView mRecyclerViewFotos, mRecyclerViewFotoSave, mRecyclerViewStory;
    private String mProfileId;
    private MyFotoAdapter mMyFotoAdapter, mMyFotoAdapterSave;
    private List<Post> mListPostImage = new ArrayList<>();
    private List<Post> mListPostImageSave = new ArrayList<>();
    private List<Story> mListStory = new ArrayList();
    private StoryAdapter mStoryAdapter;
    private List<String> mListPostSave = new ArrayList<>();
    private List<String> mListFollowing = new ArrayList<>();
    private FirebaseUser mFirebaseUser;

    private static final String DETAIL_LIST_FRAGMENT = "PostListFragment";
    public static final String ACTION_DETAIL_LIST = DETAIL_LIST_FRAGMENT + "action.list";
    private static final String EXTRA_IS_ROOT_FRAGMENT = ".extra_is_root_fragment";

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(boolean isRoot) {

        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_ROOT_FRAGMENT, isRoot);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        setAdapterMyFoto();
        setAdapterFotoSave();
        setAdapterStory();
        initData();
        initAction();
    }

    private void initView(View view) {
        mImgProfile = view.findViewById(R.id.imgProfile);
        mImgStories = view.findViewById(R.id.imgStories);
        mImgStoriesSeen = view.findViewById(R.id.imgStoriesSeen);
        mImgOption = view.findViewById(R.id.imgOption);
        mTxtUsername = view.findViewById(R.id.txtUsername);
        mTxtFullname = view.findViewById(R.id.txtFullname);
        mTxtFollowers = view.findViewById(R.id.txtFollowers);
        mTxtFollowing = view.findViewById(R.id.txtFollowing);
        mTxtPost = view.findViewById(R.id.txtPost);
        mTxtBio = view.findViewById(R.id.txtBio);
        mBtnEditProfile = view.findViewById(R.id.btnEditProfile);
        mImgBtnFoto = view.findViewById(R.id.imgFotos);
        mImgBtnFotoSave = view.findViewById(R.id.imgSaveFotos);
        mRecyclerViewFotos = view.findViewById(R.id.recyclerviewFotos);
        mRecyclerViewFotoSave = view.findViewById(R.id.recyclerviewFotoSave);
        mRecyclerViewStory = view.findViewById(R.id.recyclerviewStories);
        mRecyclerViewFotos.setVisibility(View.VISIBLE);
        mRecyclerViewFotoSave.setVisibility(View.GONE);
    }

    private void initData() {
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences prefs = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        mProfileId = prefs.getString("profileid", "none");

        userInfo();
        getFollow();
        getNrPost();
        myFotos();
        mySaveFoto();
        if (mProfileId.equals(mFirebaseUser.getUid())) {
            checkFollowStory();
            readStory();
            mBtnEditProfile.setText("Edit Profile");
            mImgOption.setVisibility(View.VISIBLE);
        } else {
            checkFollow();
            readStoryDif();
            mRecyclerViewStory.setVisibility(View.GONE);
            mImgBtnFotoSave.setVisibility(View.GONE);
            mImgOption.setVisibility(View.GONE);
        }
    }

    private void initAction() {
        mBtnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String btnState = mBtnEditProfile.getText().toString();

                if (btnState.equals("Edit Profile")) {
                    startActivity(new Intent(getActivity(), EditProfileActivity.class));
                } else if (btnState.equals("follow")) {
                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(mFirebaseUser.getUid())
                            .child("following")
                            .child(mProfileId)
                            .setValue(true);

                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(mProfileId)
                            .child("followers")
                            .child(mFirebaseUser.getUid())
                            .setValue(true);

                    addNotification();
                } else if (btnState.equals("following")) {
                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(mFirebaseUser.getUid())
                            .child("following")
                            .child(mProfileId)
                            .removeValue();

                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(mProfileId)
                            .child("followers")
                            .child(mFirebaseUser.getUid())
                            .removeValue();
                }
            }
        });

        mImgBtnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerViewFotos.setVisibility(View.VISIBLE);
                mRecyclerViewFotoSave.setVisibility(View.GONE);
                setAdapterMyFoto();
            }
        });

        mImgBtnFotoSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerViewFotos.setVisibility(View.GONE);
                mRecyclerViewFotoSave.setVisibility(View.VISIBLE);
                setAdapterFotoSave();
            }
        });

        mTxtFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FollowAndLikeActivity.class);
                intent.putExtra("id", mProfileId);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        mTxtFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FollowAndLikeActivity.class);
                intent.putExtra("id", mProfileId);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });

        mImgStoriesSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), StoryActivity.class);
                intent.putExtra("userid", mProfileId);
                startActivity(intent);
            }
        });

        mImgStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), StoryActivity.class);
                intent.putExtra("userid", mProfileId);
                startActivity(intent);
            }
        });

        mImgOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), OptionActivity.class));
            }
        });
    }

    private void setAdapterMyFoto() {
        mMyFotoAdapter = new MyFotoAdapter(getContext(), mListPostImage, false, ACTION_DETAIL_LIST, currentTab, fragmentInteractionCallback);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        mRecyclerViewFotos.setLayoutManager(linearLayoutManager);
        mRecyclerViewFotos.setAdapter(mMyFotoAdapter);
    }

    private void setAdapterFotoSave() {
        mMyFotoAdapterSave = new MyFotoAdapter(getContext(), mListPostImageSave, true, ACTION_DETAIL_LIST, currentTab, fragmentInteractionCallback);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        mRecyclerViewFotoSave.setLayoutManager(linearLayoutManager);
        mRecyclerViewFotoSave.setAdapter(mMyFotoAdapterSave);
    }

    private void setAdapterStory() {
        mStoryAdapter = new StoryAdapter(getContext(), mListStory);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        mRecyclerViewStory.setLayoutManager(linearLayoutManager);
        mRecyclerViewStory.setAdapter(mStoryAdapter);
    }

    private void addNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(mProfileId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", mFirebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(mProfileId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }

                Users users = dataSnapshot.getValue(Users.class);
                Picasso.with(getContext()).load(users.getImageurl()).into(mImgProfile);
                Picasso.with(getContext()).load(users.getImageurl()).into(mImgStories);
                Picasso.with(getContext()).load(users.getImageurl()).into(mImgStoriesSeen);
                mTxtUsername.setText(users.getUsername());
                mTxtFullname.setText(users.getFullname());
                mTxtBio.setText(users.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(mFirebaseUser.getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(mProfileId).exists()) {
                    mBtnEditProfile.setText("following");
                } else {
                    mBtnEditProfile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFollowStory() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(mFirebaseUser.getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListFollowing.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mListFollowing.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(mProfileId)
                .child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mTxtFollowers.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Follow")
                .child(mProfileId)
                .child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mTxtFollowing.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getNrPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(mProfileId)) {
                        i++;
                    }
                }
                mTxtPost.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void myFotos() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListPostImage.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(mProfileId)) {
                        mListPostImage.add(post);
                    }
                }
                Collections.reverse(mListPostImage);
                mMyFotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void mySaveFoto() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(mFirebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListPostSave.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mListPostSave.add(snapshot.getKey());
                }
                readSaveFoto();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readSaveFoto() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListPostImageSave.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    for (String id : mListPostSave) {
                        if (id.equals(post.getPostid())) {
                            mListPostImageSave.add(post);
                        }
                    }
                }
                Collections.reverse(mListPostImageSave);
                mMyFotoAdapterSave.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readStory() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis();
                mListStory.clear();
                mListStory.add(new Story("", 0, 0, "", FirebaseAuth.getInstance().getCurrentUser().getUid()));
                for (String id : mListFollowing) {
                    int count = 0;
                    Story story = null;
                    for (DataSnapshot snapshot : dataSnapshot.child(id).getChildren()) {
                        story = snapshot.getValue(Story.class);
                        if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                            count++;
                        }
                    }

                    if (count > 0) {
                        mListStory.add(story);
                    }
                }
                mStoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readStoryDif() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(mProfileId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis();
                int count = 0;
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                        count++;
                    }

                    if (!snapshot
                            .child("views")
                            .child(mFirebaseUser.getUid())
                            .exists() && System.currentTimeMillis() < story.getTimeend()) {
                        i++;
                    }
                }

                if (count > 0) {
                    if (i > 0) {
                        mImgStories.setVisibility(View.VISIBLE);
                        mImgStoriesSeen.setVisibility(View.GONE);
                    } else {
                        mImgStories.setVisibility(View.GONE);
                        mImgStoriesSeen.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
