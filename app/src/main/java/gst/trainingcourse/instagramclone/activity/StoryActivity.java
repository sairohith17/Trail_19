package gst.trainingcourse.instagramclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.models.Story;
import gst.trainingcourse.instagramclone.models.Users;
import jp.shts.android.storiesprogressview.StoriesProgressView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    private StoriesProgressView mStoriesProgressView;
    private ImageView mImageStory, mImageStoryPhoto, mImageStoryDelete;
    private TextView mTxtStoryUsername, mTxtSeenNumber, mTxtStoryTime;
    private List<String> mImages = new ArrayList<>();
    private List<Long> mTime = new ArrayList<>();
    private List<String> mStoryIds = new ArrayList<>();
    private String mUserId;
    private LinearLayout linearLayout;
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    mStoriesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    mStoriesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        mStoriesProgressView = findViewById(R.id.progressStory);
        mImageStory = findViewById(R.id.imgStory);
        mImageStoryPhoto = findViewById(R.id.imgStoryPhoto);
        mTxtStoryUsername = findViewById(R.id.txtStoryUsername);
        linearLayout = findViewById(R.id.llSeen);
        mTxtSeenNumber = findViewById(R.id.txtSeenNumber);
        mTxtStoryTime = findViewById(R.id.txtStoryTime);
        mImageStoryDelete = findViewById(R.id.imgStoryDelete);

        linearLayout.setVisibility(View.GONE);
        mImageStoryDelete.setVisibility(View.GONE);

        mUserId = getIntent().getStringExtra("userid");

        if (mUserId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            linearLayout.setVisibility(View.VISIBLE);
            mImageStoryDelete.setVisibility(View.VISIBLE);
        }

        getStories(mUserId);
        userInfo(mUserId);

        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStoriesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStoriesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoryActivity.this, FollowAndLikeActivity.class);
                intent.putExtra("id", mUserId);
                intent.putExtra("title", "Views");
                intent.putExtra("storyid", mStoryIds.get(counter));
                startActivity(intent);
            }
        });

        mImageStoryDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                        .child(mUserId).child(mStoryIds.get(counter));
                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Delete success", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onNext() {
        Picasso.with(getApplicationContext()).load(mImages.get(++counter)).into(mImageStory);
        mTxtStoryTime.setText(DateUtils.getRelativeTimeSpanString(mTime.get(counter), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        addView(mStoryIds.get(counter));
        seenNumber(mStoryIds.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        Picasso.with(getApplicationContext()).load(mImages.get(--counter)).into(mImageStory);
        mTxtStoryTime.setText(DateUtils.getRelativeTimeSpanString(mTime.get(counter), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        seenNumber(mStoryIds.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }

    private void getStories(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mImages.clear();
                mStoryIds.clear();
                mTime.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    long timecurrent = System.currentTimeMillis();

                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                        mImages.add(story.getImageurl());
                        mStoryIds.add(story.getStoryid());
                        mTime.add(story.getTimestart());
                    }
                }

                mStoriesProgressView.setStoriesCount(mImages.size());
                mStoriesProgressView.setStoryDuration(5000L);
                mStoriesProgressView.setStoriesListener(StoryActivity.this);
                mStoriesProgressView.startStories(counter);

                Picasso.with(getApplicationContext()).load(mImages.get(counter)).into(mImageStory);
                mTxtStoryTime.setText(DateUtils.getRelativeTimeSpanString(mTime.get(counter), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
                addView(mStoryIds.get(counter));
                seenNumber(mStoryIds.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void userInfo(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);

                Picasso.with(getApplicationContext()).load(users.getImageurl()).fit().into(mImageStoryPhoto);
                mTxtStoryUsername.setText(users.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addView(String storyid) {
        FirebaseDatabase.getInstance().getReference("Story")
                .child(mUserId)
                .child(storyid)
                .child("views")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(true);
    }

    private void seenNumber(String storyid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(mUserId)
                .child(storyid)
                .child("views");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mTxtSeenNumber.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        mStoriesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mStoriesProgressView.resume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mStoriesProgressView.destroy();
        super.onDestroy();
    }
}
