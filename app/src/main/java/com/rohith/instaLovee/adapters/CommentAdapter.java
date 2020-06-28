package com.rohith.instaLovee.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import com.rohith.instaLovee.activity.FollowAndLikeActivity;
import com.rohith.instaLovee.main.MainActivity;
import com.rohith.instaLovee.R;
import com.rohith.instaLovee.models.Comment;
import com.rohith.instaLovee.models.Users;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> mListComment;
    private String mPostId;
    private String current;
    private FirebaseUser mFirebaseUser;

    public CommentAdapter(Context mContext, List<Comment> mListComment, String mPostId, String current) {
        this.mContext = mContext;
        this.mListComment = mListComment;
        this.mPostId = mPostId;
        this.current = current;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_comments, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.ViewHolder holder, int position) {
        final Comment comment = mListComment.get(position);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.txtComment.setText(comment.getComment());
        holder.txtTime.setText(DateUtils.getRelativeTimeSpanString(getDateInMillis(comment.getTimecomment()), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        getUserInfo(holder.imgProfile, holder.txtUsername, comment.getPublisher());
        isLiked(comment.getCommentid(), holder.imgLike);
        nrLikes(holder.txtLike, comment.getCommentid());

        holder.txtComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherid", comment.getPublisher());
                intent.putExtra("current", current);
                mContext.startActivity(intent);
            }
        });

        holder.txtLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FollowAndLikeActivity.class);
                intent.putExtra("idcomment", comment.getCommentid());
                intent.putExtra("title", "Likes");
                mContext.startActivity(intent);
            }
        });

        holder.imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherid", comment.getPublisher());
                intent.putExtra("current", current);
                mContext.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (comment.getPublisher().equals(mFirebaseUser.getUid())) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                    alert.setTitle("Do you wan't delete your comment!");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseDatabase.getInstance().getReference("Comments")
                                    .child(mPostId)
                                    .child(comment.getCommentid())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(mContext, "Delete success!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    alert.show();
                }
                return true;
            }
        });

        holder.imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.imgLike.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference("LikesComment")
                            .child(comment.getCommentid())
                            .child(mFirebaseUser.getUid())
                            .setValue(true);

                } else {
                    FirebaseDatabase.getInstance().getReference("LikesComment")
                            .child(comment.getCommentid())
                            .child(mFirebaseUser.getUid())
                            .removeValue();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListComment.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imgProfile;
        TextView txtUsername, txtComment, txtLike, txtTime;
        ImageView imgLike;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgProfile);
            imgLike = itemView.findViewById(R.id.imgLike);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtLike = itemView.findViewById(R.id.txtLike);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }

    private void getUserInfo(final CircleImageView imageView, final TextView textView, String publisher) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(publisher);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                Picasso.with(mContext).load(users.getImageurl()).fit().into(imageView);
                textView.setText(users.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isLiked(String commentid, final ImageView imageView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("LikesComment")
                .child(commentid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(mFirebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_favorite_red_24dp);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void nrLikes(final TextView likes, String commentid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("LikesComment")
                .child(commentid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    likes.setVisibility(View.VISIBLE);
                    likes.setText(dataSnapshot.getChildrenCount() + " lượt thích");
                } else {
                    likes.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static long getDateInMillis(String srcDate) {
        SimpleDateFormat desiredFormat = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        //EEE MMM dd HH:mm:ss z yyyy
        //Mon May 25 11:13:17 GMT+07:00 2020
        long dateInMillis = 0;
        try {
            Date date = desiredFormat.parse(srcDate);
            dateInMillis = date.getTime();
            return dateInMillis;
        } catch (ParseException e) {
            Log.d("TAG","Exception while parsing date. " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }
}
