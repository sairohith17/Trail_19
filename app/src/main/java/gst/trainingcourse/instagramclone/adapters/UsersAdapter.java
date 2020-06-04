package gst.trainingcourse.instagramclone.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.instagramclone.main.MainActivity;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.fragments.BaseFragment;
import gst.trainingcourse.instagramclone.control.FragmentUtils;
import gst.trainingcourse.instagramclone.models.Users;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private Context mContext;
    private List<Users> mLists;
    private boolean mIsFragment;
    private String ACTION_DASHBOARD;
    private String current;
    private BaseFragment.FragmentInteractionCallback fragmentInteractionCallback;
    private FirebaseUser mFirebaseUser;

    public UsersAdapter(Context mContext, List<Users> mLists, boolean mIsFragment, String ACTION_DASHBOARD, String current, BaseFragment.FragmentInteractionCallback fragmentInteractionCallback) {
        this.mContext = mContext;
        this.mLists = mLists;
        this.mIsFragment = mIsFragment;
        this.ACTION_DASHBOARD = ACTION_DASHBOARD;
        this.current = current;
        this.fragmentInteractionCallback = fragmentInteractionCallback;
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersAdapter.ViewHolder holder, int position) {
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Users users = mLists.get(position);

        holder.btnFollow.setVisibility(View.VISIBLE);
        holder.username.setText(users.getUsername());
        holder.fullname.setText(users.getFullname());
        Picasso.with(mContext).load(users.getImageurl()).fit().into(holder.imgProfile);
        isFollowing(users.getId(), holder.btnFollow);

        if (users.getId().equals(mFirebaseUser.getUid())) {
            holder.btnFollow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsFragment) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", users.getId());
                    editor.apply();

                    FragmentUtils.sendActionToActivity(ACTION_DASHBOARD, current, true, fragmentInteractionCallback);
                } else {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("publisherid", users.getId());
                    intent.putExtra("current", current);
                    mContext.startActivity(intent);
                }

            }
        });

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.btnFollow.getText().toString().equals("follow")) {
                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(mFirebaseUser.getUid())
                            .child("following")
                            .child(users.getId())
                            .setValue(true);

                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(users.getId())
                            .child("followers")
                            .child(mFirebaseUser.getUid())
                            .setValue(true);

                    addNotification(users.getId());
                } else {
                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(mFirebaseUser.getUid())
                            .child("following")
                            .child(users.getId())
                            .removeValue();

                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(users.getId())
                            .child("followers")
                            .child(mFirebaseUser.getUid())
                            .removeValue();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imgProfile;
        TextView username, fullname;
        Button btnFollow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.txtUsername);
            fullname = itemView.findViewById(R.id.txtFullname);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            btnFollow = itemView.findViewById(R.id.btnFollow);

        }
    }

    private void addNotification(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", mFirebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    private void isFollowing(final String userid, final Button button) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(mFirebaseUser.getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userid).exists()) {
                    button.setText("following");
                } else {
                    button.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
