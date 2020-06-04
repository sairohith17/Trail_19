package gst.trainingcourse.instagramclone.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.fragments.BaseFragment;
import gst.trainingcourse.instagramclone.control.FragmentUtils;
import gst.trainingcourse.instagramclone.models.Notification;
import gst.trainingcourse.instagramclone.models.Post;
import gst.trainingcourse.instagramclone.models.Users;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> mListNoti;
    private String ACTION_DASHBOARD_PROFILE, ACTION_DASHBOARD_POST;
    private String current;
    private BaseFragment.FragmentInteractionCallback fragmentInteractionCallback;

    public NotificationAdapter(Context mContext, List<Notification> mListNoti, String ACTION_DASHBOARD_POST, String ACTION_DASHBOARD_PROFILE, String current, BaseFragment.FragmentInteractionCallback fragmentInteractionCallback) {
        this.mContext = mContext;
        this.mListNoti = mListNoti;
        this.ACTION_DASHBOARD_POST = ACTION_DASHBOARD_POST;
        this.ACTION_DASHBOARD_PROFILE = ACTION_DASHBOARD_PROFILE;
        this.current = current;
        this.fragmentInteractionCallback = fragmentInteractionCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Notification notification = mListNoti.get(position);

        holder.txtComment.setText(notification.getText());
        getUserInfo(holder.imgProfile, holder.txtUsername, notification.getUserid());

        if (notification.isIspost()) {
            holder.imgPost.setVisibility(View.VISIBLE);
            getPostImage(holder.imgPost, notification.getPostid());
        } else {
            holder.imgPost.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.isIspost()) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("postid", notification.getPostid());
                    editor.putString("publisher", "none");
                    editor.apply();

                    FragmentUtils.sendActionToActivity(ACTION_DASHBOARD_POST, current, true, fragmentInteractionCallback);
                } else {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", notification.getUserid());
                    editor.apply();

                    FragmentUtils.sendActionToActivity(ACTION_DASHBOARD_PROFILE, current, true, fragmentInteractionCallback);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListNoti.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imgProfile;
        ImageView imgPost;
        TextView txtComment, txtUsername;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgProfile);
            imgPost = itemView.findViewById(R.id.imgPostImage);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtUsername = itemView.findViewById(R.id.txtUsername);
        }
    }

    private void getUserInfo(final CircleImageView imageView, final TextView username, String publisherid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(publisherid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                Picasso.with(mContext).load(users.getImageurl()).fit().into(imageView);
                username.setText(users.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostImage(final ImageView imageView, final String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                Picasso.with(mContext).load(post.getPostimages().get(0)).fit().into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
