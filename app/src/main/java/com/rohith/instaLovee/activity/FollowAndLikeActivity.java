package com.rohith.instaLovee.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rohith.instaLovee.R;
import com.rohith.instaLovee.adapters.UsersAdapter;
import com.rohith.instaLovee.fragments.BaseFragment;
import com.rohith.instaLovee.models.Users;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FollowAndLikeActivity extends AppCompatActivity {

    public static class FollowAndLike extends BaseFragment {
        String getCurrent() {
            return currentTab;
        }
        FragmentInteractionCallback getCallback() {
            return fragmentInteractionCallback;
        }
        static final String USER_FRAGMENT = "UserFragment";
        static final String ACTION_USERS = USER_FRAGMENT + "action.user";
    }

    private String mId, mTitle, mIdComment;
    private List<String> mListId = new ArrayList<>();
    private RecyclerView mRecyclerViewLikeFollow;
    private Toolbar mToolBar;
    private UsersAdapter mUsersAdapter;
    private List<Users> mListUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_and_like);

        initView();
        setAdapter();
        initData();
        initAction();
    }

    private void initView() {
        mRecyclerViewLikeFollow = findViewById(R.id.recyclerviewLikeFollow);
        mToolBar = findViewById(R.id.toolBar);
    }

    private void initData() {
        Intent intent = getIntent();
        mId = intent.getStringExtra("id");
        mIdComment = intent.getStringExtra("idcomment");
        mTitle = intent.getStringExtra("title");

        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(mTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switch (mTitle) {
            case "Likes":
                if (mId != null && mIdComment == null) {
                    getLikes();
                } else {
                    getLikesComment();
                }
                break;
            case "Following":
                getFollowing();
                break;
            case "Followers":
                getFollowers();
                break;
            case "Views":
                getViews();
                break;
        }
    }

    private void initAction() {
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setAdapter() {
        FollowAndLike followAndLike = new FollowAndLike();
        mUsersAdapter = new UsersAdapter(this, mListUsers, false, FollowAndLike.ACTION_USERS, followAndLike.getCurrent(), followAndLike.getCallback());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerViewLikeFollow.setLayoutManager(linearLayoutManager);
        mRecyclerViewLikeFollow.setAdapter(mUsersAdapter);
    }

    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(mId)
                .child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mListId.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(mId)
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mListId.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikes() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes")
                .child(mId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mListId.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikesComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("LikesComment")
                .child(mIdComment);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mListId.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users users = snapshot.getValue(Users.class);
                    for (String id : mListId) {
                        if (id.equals(users.getId())) {
                            mListUsers.add(users);
                        }
                    }
                }

                mUsersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getViews() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(mId)
                .child(getIntent().getStringExtra("storyid"))
                .child("views");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mListId.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
