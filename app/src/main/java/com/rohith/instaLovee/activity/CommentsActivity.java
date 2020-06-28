package com.rohith.instaLovee.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import com.rohith.instaLovee.R;
import com.rohith.instaLovee.adapters.CommentAdapter;
import com.rohith.instaLovee.fragments.BaseFragment;
import com.rohith.instaLovee.models.Comment;
import com.rohith.instaLovee.models.Users;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private EditText mEdtAddComment;
    private CircleImageView mImgProfile;
    private TextView mTxtPost;
    private Toolbar mToolBar;
    private String mPostId, mPublisherId;
    private RecyclerView mRecyclerViewComment;
    private CommentAdapter mCommentAdapter;
    private List<Comment> mListComment = new ArrayList<>();
    private FirebaseUser mFirebaseUser;

    public static class CommentFragment extends BaseFragment {
        String getCurrent() {
            return currentTab;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        initView();
        initData();
        setAdapter();
        initAction();
    }

    private void initView() {
        mEdtAddComment = findViewById(R.id.edtAddComment);
        mImgProfile = findViewById(R.id.imgProfile);
        mTxtPost = findViewById(R.id.txtPost);
        mToolBar = findViewById(R.id.toolBar);
        mRecyclerViewComment = findViewById(R.id.recyclerviewComment);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void  initData() {
        Intent intent = getIntent();
        mPostId = intent.getStringExtra("postid");
        mPublisherId = intent.getStringExtra("publisherid");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getImage();
        readComment();
    }

    private void initAction() {
        mTxtPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEdtAddComment.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "You can't sent empty comment!", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });
    }

    private void setAdapter() {
        CommentFragment commentFragment = new CommentFragment();
        mCommentAdapter = new CommentAdapter(this, mListComment, mPostId, commentFragment.getCurrent());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerViewComment.setLayoutManager(linearLayoutManager);
        mRecyclerViewComment.setAdapter(mCommentAdapter);
    }

    private void addComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments")
                .child(mPostId);

        String commentid = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", mEdtAddComment.getText().toString());
        hashMap.put("publisher", mFirebaseUser.getUid());
        hashMap.put("commentid", commentid);
        hashMap.put("timecomment", Calendar.getInstance().getTime().toString());

        reference.child(commentid).setValue(hashMap);
        addNotification();
        mEdtAddComment.setText("");
    }

    private void addNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(mPublisherId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", mFirebaseUser.getUid());
        hashMap.put("text", "commented: " + mEdtAddComment.getText().toString());
        hashMap.put("postid", mPostId);
        hashMap.put("ispost", true);

        reference.push().setValue(hashMap);
    }

    private void getImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(mFirebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                Picasso.with(getApplicationContext()).load(users.getImageurl()).fit().into(mImgProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments")
                .child(mPostId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListComment.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    mListComment.add(comment);
                }
                mCommentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
