package gst.trainingcourse.instagramclone.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.chahinem.pageindicator.PageIndicator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import gst.trainingcourse.instagramclone.activity.CommentsActivity;
import gst.trainingcourse.instagramclone.activity.FollowAndLikeActivity;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.fragments.BaseFragment;
import gst.trainingcourse.instagramclone.fragments.ShareBottomSheet;
import gst.trainingcourse.instagramclone.control.FragmentUtils;
import gst.trainingcourse.instagramclone.models.Post;
import gst.trainingcourse.instagramclone.models.Users;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mListPost;
    private String ACTION_DASHBOARD;
    private String current;
    private BaseFragment.FragmentInteractionCallback fragmentInteractionCallback;
    private FirebaseUser mFirebaseUser;

    public PostAdapter(Context mContext, List<Post> mListPost, String ACTION_DASHBOARD, String current, BaseFragment.FragmentInteractionCallback fragmentInteractionCallback) {
        this.mContext = mContext;
        this.mListPost = mListPost;
        this.ACTION_DASHBOARD = ACTION_DASHBOARD;
        this.current = current;
        this.fragmentInteractionCallback = fragmentInteractionCallback;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostAdapter.ViewHolder holder, int position) {
        final Post post = mListPost.get(position);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.txtTimePost.setText(DateUtils.getRelativeTimeSpanString(getDateInMillis(post.getTimepost()), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));

        publisherInfo(holder.imgProfile, holder.txtUsername, post.getPublisher());
        isLiked(post.getPostid(), holder.imgLike);
        nrLikes(holder.txtLike, post.getPostid());
        getComments(post.getPostid(), holder.txtComment);
        isSaved(post.getPostid(), holder.imgSave);

        ViewImagePagerAdapter viewImagePagerAdapter = new ViewImagePagerAdapter(mContext, post.getPostimages());
        holder.viewPager.setAdapter(viewImagePagerAdapter);

        if (post.getPostimages().size() > 1) {
            holder.pageIndicator.setVisibility(View.VISIBLE);
            holder.pageIndicator.attachTo(holder.viewPager);
            holder.pageIndicator.swipeNext();
            holder.pageIndicator.swipePrevious();

            holder.txtViewPager.setVisibility(View.VISIBLE);
            holder.txtViewPager.setText(1 + "/" + post.getPostimages().size());

            holder.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    holder.txtViewPager.setText(position + 1 + "/" + post.getPostimages().size());
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            holder.txtViewPager.setVisibility(View.GONE);
            holder.pageIndicator.setVisibility(View.GONE);
        }

        if (post.getDescription().equals("")) {
            holder.txtDescription.setVisibility(View.GONE);
        } else {
            holder.txtDescription.setVisibility(View.VISIBLE);
            holder.txtDescription.setText(post.getDescription());
        }

        if (mFirebaseUser.getUid().equals(post.getPublisher())) {
            holder.imgSave.setVisibility(View.GONE);
        }

        holder.imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.imgLike.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference("Likes")
                            .child(post.getPostid())
                            .child(mFirebaseUser.getUid())
                            .setValue(true);

                    addNotification(post.getPublisher(), post.getPostid());
                } else {
                    FirebaseDatabase.getInstance().getReference("Likes")
                            .child(post.getPostid())
                            .child(mFirebaseUser.getUid())
                            .removeValue();
                }
            }
        });

        holder.txtComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.imgSave.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference("Saves")
                            .child(mFirebaseUser.getUid())
                            .child(post.getPostid())
                            .setValue(true);
                    Toast.makeText(mContext, "Save", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseDatabase.getInstance().getReference("Saves")
                            .child(mFirebaseUser.getUid())
                            .child(post.getPostid())
                            .removeValue();
                }
            }
        });

        holder.imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                FragmentUtils.sendActionToActivity(ACTION_DASHBOARD, current, true, fragmentInteractionCallback);
            }
        });

        holder.txtUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                FragmentUtils.sendActionToActivity(ACTION_DASHBOARD, current, true, fragmentInteractionCallback);
            }
        });

        holder.txtLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FollowAndLikeActivity.class);
                intent.putExtra("id", post.getPostid());
                intent.putExtra("title", "Likes");
                mContext.startActivity(intent);
            }
        });

        holder.imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.edit:
                                editPost(post.getPostid());
                                return true;
                            case R.id.delete:
                                FirebaseDatabase.getInstance().getReference("Posts")
                                        .child(post.getPostid())
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(mContext, "Delete success!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                return true;
                            case R.id.report:
                                Toast.makeText(mContext, "Report!!!!!!!", Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if (!post.getPublisher().equals(mFirebaseUser.getUid())) {
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });

        holder.imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareBottomSheet shareBottomSheet = ShareBottomSheet.newInstance(post.getPostimages().get(0));
                shareBottomSheet.show((((FragmentActivity) mContext).getSupportFragmentManager()), "bottom");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListPost.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile, imgShare, imgLike, imgComment, imgSave, imgMore;
        TextView txtUsername, txtLike, txtDescription, txtComment, txtViewPager, txtTimePost;
        PageIndicator pageIndicator;
        ViewPager viewPager;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPager = itemView.findViewById(R.id.viewPager);
            pageIndicator = itemView.findViewById(R.id.pageIndicator);
            txtViewPager = itemView.findViewById(R.id.txtViewPager);
            imgMore = itemView.findViewById(R.id.imgMore);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            imgShare = itemView.findViewById(R.id.imgShare);
            imgLike = itemView.findViewById(R.id.imgLike);
            imgComment = itemView.findViewById(R.id.imgComment);
            imgSave = itemView.findViewById(R.id.imgSave);
            txtLike = itemView.findViewById(R.id.txtLike);
            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtTimePost = itemView.findViewById(R.id.txtTimePost);
        }
    }

    private void getComments(String postid, final TextView textView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textView.setText("View All " + dataSnapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotification(String userid, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", mFirebaseUser.getUid());
        hashMap.put("text", "liked your post");
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);

        reference.push().setValue(hashMap);
    }

    private void isLiked(String postid, final ImageView imageView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes")
                .child(postid);
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

    private void nrLikes(final TextView likes, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void publisherInfo(final ImageView imgProfile, final TextView username, final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                Picasso.with(mContext).load(users.getImageurl()).fit().into(imgProfile);
                username.setText(users.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isSaved(final String postid, final ImageView imageView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(mFirebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.ic_bookmark_black_24dp);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void editPost(final String postid) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle("Edit Post");
        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(lp);
        alert.setView(editText);

        getText(postid, editText);

        alert.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("description", editText.getText().toString());
                FirebaseDatabase.getInstance().getReference("Posts")
                        .child(postid).updateChildren(hashMap);
                Toast.makeText(mContext, "Edit success!", Toast.LENGTH_SHORT).show();
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

    private void getText(String postid, final EditText editText) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static long getDateInMillis(String srcDate) {
        SimpleDateFormat desiredFormat = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

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
