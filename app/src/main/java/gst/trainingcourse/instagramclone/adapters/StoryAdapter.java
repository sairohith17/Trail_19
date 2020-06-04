package gst.trainingcourse.instagramclone.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import gst.trainingcourse.instagramclone.activity.AddStoryActivity;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.activity.StoryActivity;
import gst.trainingcourse.instagramclone.models.Story;
import gst.trainingcourse.instagramclone.models.Users;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private Context mContext;
    private List<Story> mListStory;

    public StoryAdapter(Context mContext, List<Story> mListStory) {
        this.mContext = mContext;
        this.mListStory = mListStory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_stories_item, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_stories, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Story story = mListStory.get(position);
        usersInfo(holder, story.getUserid(), position);
        if (holder.getAdapterPosition() != 0) {
            seenStory(holder, story.getUserid());
        }

        if (holder.getAdapterPosition() == 0) {
            myStory(holder.txtAddStories, holder.imgStoriesPlus, false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() == 0) {
                    myStory(holder.txtAddStories, holder.imgStoriesPlus, true);
                } else {
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userid", story.getUserid());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListStory.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgStories, imgStoriesSeen, imgStoriesPlus;
        TextView txtUsernameStories, txtAddStories;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgStories = itemView.findViewById(R.id.imgStories);
            imgStoriesSeen = itemView.findViewById(R.id.imgStoriesSeen);
            imgStoriesPlus = itemView.findViewById(R.id.imgStoriesPlus);
            txtUsernameStories = itemView.findViewById(R.id.txtUsernameStories);
            txtAddStories = itemView.findViewById(R.id.txtAddStories);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        return 1;
    }

    private void usersInfo(final ViewHolder viewHolder, String userid, final int pos) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                Picasso.with(mContext).load(users.getImageurl()).fit().into(viewHolder.imgStories);
                if (pos != 0) {
                    Picasso.with(mContext).load(users.getImageurl()).fit().into(viewHolder.imgStoriesSeen);
                    viewHolder.txtUsernameStories.setText(users.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void myStory(final TextView textView, final ImageView imageView, final boolean click) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                        count++;
                    }
                }

                if (click) {
                    if (count > 0) {
                        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
                        dialog.setTitle("Story");
                        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(mContext, StoryActivity.class);
                                intent.putExtra("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                mContext.startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(mContext, AddStoryActivity.class);
                                mContext.startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        if (!((Activity) mContext).isFinishing()) dialog.show();
                    } else {
                        Intent intent = new Intent(mContext, AddStoryActivity.class);
                        mContext.startActivity(intent);
                    }
                } else {
                    if (count > 0) {
                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);
                    } else {
                        textView.setText("Add Story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenStory(final ViewHolder viewHolder, String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!snapshot
                            .child("views")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .exists() && System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeend()) {
                        i++;
                    }
                }

                if (i > 0) {
                    viewHolder.imgStories.setVisibility(View.VISIBLE);
                    viewHolder.imgStoriesSeen.setVisibility(View.GONE);
                } else {
                    viewHolder.imgStories.setVisibility(View.GONE);
                    viewHolder.imgStoriesSeen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
