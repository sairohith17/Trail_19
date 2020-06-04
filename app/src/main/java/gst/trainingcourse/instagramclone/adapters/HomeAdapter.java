package gst.trainingcourse.instagramclone.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.fragments.BaseFragment;
import gst.trainingcourse.instagramclone.models.Post;
import gst.trainingcourse.instagramclone.models.Users;

import static gst.trainingcourse.instagramclone.control.FragmentUtils.sendActionToActivity;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private Context mContext;
    private List<Users> mListUsers;
    private ItemPostAdapter itemPostAdapter;
    private FirebaseUser firebaseUser;
    private String ACTION_DASHBOARD;
    private String current;
    private BaseFragment.FragmentInteractionCallback fragmentInteractionCallback;

    public HomeAdapter(Context mContext, List<Users> mListUsers, String ACTION_DASHBOARD, String current, BaseFragment.FragmentInteractionCallback fragmentInteractionCallback) {
        this.mContext = mContext;
        this.mListUsers = mListUsers;
        this.ACTION_DASHBOARD = ACTION_DASHBOARD;
        this.current = current;
        this.fragmentInteractionCallback = fragmentInteractionCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Users users = mListUsers.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.txtUsername.setText(users.getUsername());
        holder.txtFullname.setText(users.getFullname());
        Picasso.with(mContext).load(users.getImageurl()).into(holder.imgProfile);
        getImage(users.getId(), holder.recyclerView);
        isFollowing(users.getId(), holder.button);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", users.getId());
                editor.apply();
                sendActionToActivity(ACTION_DASHBOARD, current, true, fragmentInteractionCallback);
            }
        });

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.button.getText().toString().equals("follow")) {
                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(firebaseUser.getUid())
                            .child("following")
                            .child(users.getId())
                            .setValue(true);

                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(users.getId())
                            .child("followers")
                            .child(firebaseUser.getUid())
                            .setValue(true);

                    addNotification(users.getId());
                } else {
                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(firebaseUser.getUid())
                            .child("following")
                            .child(users.getId())
                            .removeValue();

                    FirebaseDatabase.getInstance().getReference("Follow")
                            .child(users.getId())
                            .child("followers")
                            .child(firebaseUser.getUid())
                            .removeValue();
                }
            }
        });

        holder.icClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference("Follow")
                        .child(firebaseUser.getUid())
                        .child("following")
                        .child(users.getId())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference("Follow")
                        .child(users.getId())
                        .child("followers")
                        .child(firebaseUser.getUid())
                        .removeValue();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imgProfile;
        public Button button;
        RecyclerView recyclerView;
        TextView txtUsername, txtFullname;
        ImageView icClose;
        ViewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerView = itemView.findViewById(R.id.recyclerview);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtFullname = itemView.findViewById(R.id.txtFullname);
            button = itemView.findViewById(R.id.btnFollow);
            icClose = itemView.findViewById(R.id.iconClose);
        }
    }

    private void getImage(final String userid, final RecyclerView recyclerView) {
        final List<Post> mListPost = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListPost.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(userid)) {
                        mListPost.add(post);
                    }
                }
                Collections.reverse(mListPost);
                itemPostAdapter = new ItemPostAdapter(mContext, mListPost);
                GridLayoutManager linearLayoutManager = new GridLayoutManager(mContext, 3);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(itemPostAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotification(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    private void isFollowing(final String userid, final Button button) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
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

    public class ItemPostAdapter extends RecyclerView.Adapter<ItemPostAdapter.HolderTest> {
        private Context context;
        private List<Post> list;

        public ItemPostAdapter(Context context, List<Post> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ItemPostAdapter.HolderTest onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_post_user, parent, false);
            return new HolderTest(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemPostAdapter.HolderTest holder, int position) {
            final Post post = list.get(position);

            Picasso.with(context).load(post.getPostimages().get(0)).into(holder.imageView);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", post.getPublisher());
                    editor.apply();

                    sendActionToActivity(ACTION_DASHBOARD, current, true, fragmentInteractionCallback);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (list.size() > 3) {
                return 3;
            } else {
                return list.size();
            }
        }

        public class HolderTest extends RecyclerView.ViewHolder {
            ImageView imageView;
            public HolderTest(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.img);
            }
        }
    }
}