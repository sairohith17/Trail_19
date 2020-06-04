package gst.trainingcourse.instagramclone.adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import gst.trainingcourse.instagramclone.activity.CommentsActivity;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.fragments.BaseFragment;
import gst.trainingcourse.instagramclone.fragments.ShareBottomSheet;
import gst.trainingcourse.instagramclone.control.FragmentUtils;
import gst.trainingcourse.instagramclone.models.Post;
import gst.trainingcourse.instagramclone.models.Users;

public class MyFotoAdapter extends RecyclerView.Adapter<MyFotoAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mListMyPost;
    private boolean mCheck;
    private String ACTION_DASHBOARD;
    private String current;
    private BaseFragment.FragmentInteractionCallback fragmentInteractionCallback;

    public MyFotoAdapter(Context mContext, List<Post> mListMyPost, boolean mCheck, String ACTION_DASHBOARD, String current, BaseFragment.FragmentInteractionCallback fragmentInteractionCallback) {
        this.mContext = mContext;
        this.mListMyPost = mListMyPost;
        this.mCheck = mCheck;
        this.ACTION_DASHBOARD = ACTION_DASHBOARD;
        this.current = current;
        this.fragmentInteractionCallback = fragmentInteractionCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_fotos, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Post post = mListMyPost.get(position);

        Picasso.with(mContext).load(post.getPostimages().get(0)).into(holder.imageView);

        if (post.getPostimages().size() > 1) {
            holder.imgMore.setVisibility(View.VISIBLE);
        } else {
            holder.imgMore.setVisibility(View.GONE);
        }

        final Dialog builder = new Dialog(mContext);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(R.layout.dialog_review_image);

        final ImageView imageView = builder.findViewById(R.id.img);
        final ImageView imgLike = builder.findViewById(R.id.imgLike);
        final ImageView imgComment = builder.findViewById(R.id.imgComment);
        final ImageView imgShare = builder.findViewById(R.id.imgShare);
        final ImageView imgMore = builder.findViewById(R.id.imgMore);
        final CircleImageView imgProfile = builder.findViewById(R.id.imgProfile);
        final TextView txtUsername = builder.findViewById(R.id.txtUsername);
        final TextView txtLike = builder.findViewById(R.id.txtLike);
        final TextView txtComment = builder.findViewById(R.id.txtComment);
        final TextView txtShare = builder.findViewById(R.id.txtShare);
        final TextView txtMore = builder.findViewById(R.id.txtMore);

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                userInfo(post, imgProfile, txtUsername);
                Picasso.with(mContext).load(post.getPostimages().get(0)).into(imageView);
                isLiked(post.getPostid(), imgLike);
                builder.show();
                return false;
            }
        });

        holder.imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (builder.isShowing()) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    int action = motionEvent.getActionMasked();
                    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                        int x = (int) motionEvent.getRawX();
                        int y = (int) motionEvent.getRawY();
                        if (getLocationOnScreen(imgComment).contains(x, y)) {
                            txtComment.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(mContext, CommentsActivity.class);
                            intent.putExtra("postid", post.getPostid());
                            intent.putExtra("publisherid", post.getPublisher());
                            mContext.startActivity(intent);
                        } else if (getLocationOnScreen(imgLike).contains(x, y)) {
                            if (imgLike.getTag().equals("like")) {
                                FirebaseDatabase.getInstance().getReference("Likes")
                                        .child(post.getPostid())
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(true);
                                Toast.makeText(mContext, "Like", Toast.LENGTH_SHORT).show();
                                addNotification(post.getPublisher(), post.getPostid());
                            } else {
                                FirebaseDatabase.getInstance().getReference("Likes")
                                        .child(post.getPostid())
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .removeValue();
                                Toast.makeText(mContext, "UnLike", Toast.LENGTH_SHORT).show();
                            }
                            txtLike.setVisibility(View.INVISIBLE);
                        } else if (getLocationOnScreen(imgShare).contains(x, y)) {
                            txtShare.setVisibility(View.INVISIBLE);
                            ShareBottomSheet shareBottomSheet = ShareBottomSheet.newInstance(post.getPostimages().get(0));
                            shareBottomSheet.show((((FragmentActivity)mContext).getSupportFragmentManager()), "bottom");
                        } else if (getLocationOnScreen(imgMore).contains(x, y)) {
                            txtMore.setVisibility(View.INVISIBLE);
                            Toast.makeText(mContext, "More", Toast.LENGTH_SHORT).show();
                        }
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        builder.dismiss();
                        return true;
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        int x = (int) motionEvent.getRawX();
                        int y = (int) motionEvent.getRawY();
                        if (getLocationOnScreen(imgComment).contains(x, y)) {
                            vibrate();
                            txtComment.setVisibility(View.VISIBLE);
                            txtLike.setVisibility(View.INVISIBLE);
                            txtShare.setVisibility(View.INVISIBLE);
                            txtMore.setVisibility(View.INVISIBLE);
                        } else if (getLocationOnScreen(imgLike).contains(x, y)) {
                            vibrate();
                            txtLike.setVisibility(View.VISIBLE);
                            txtShare.setVisibility(View.INVISIBLE);
                            txtMore.setVisibility(View.INVISIBLE);
                            txtComment.setVisibility(View.INVISIBLE);
                        } else if (getLocationOnScreen(imgShare).contains(x, y)) {
                            vibrate();
                            txtShare.setVisibility(View.VISIBLE);
                            txtComment.setVisibility(View.INVISIBLE);
                            txtLike.setVisibility(View.INVISIBLE);
                            txtMore.setVisibility(View.INVISIBLE);
                        } else if (getLocationOnScreen(imgMore).contains(x, y)) {
                            vibrate();
                            txtMore.setVisibility(View.VISIBLE);
                            txtComment.setVisibility(View.INVISIBLE);
                            txtLike.setVisibility(View.INVISIBLE);
                            txtShare.setVisibility(View.INVISIBLE);
                        } else {
                            txtComment.setVisibility(View.INVISIBLE);
                            txtLike.setVisibility(View.INVISIBLE);
                            txtShare.setVisibility(View.INVISIBLE);
                            txtMore.setVisibility(View.INVISIBLE);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", "none");
                editor.putString("publisher", post.getPublisher());
                editor.putInt("position", position);
                if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(post.getPublisher())) {
                    if (mCheck) {
                        editor.putBoolean("check", mCheck);
                    } else {
                        editor.putBoolean("check", mCheck);
                    }
                }
                editor.apply();

                FragmentUtils.sendActionToActivity(ACTION_DASHBOARD, current, true, fragmentInteractionCallback);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListMyPost.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, imgMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgPostImage);
            imgMore = itemView.findViewById(R.id.imgMoreImg);
        }
    }

    private Rect getLocationOnScreen(View mView) {
        Rect mRect = new Rect();
        int[] location = new int[2];

//        mView.getLocationInWindow(location);
        mView.getLocationOnScreen(location);

        mRect.left = location[0];
        mRect.top = location[1];
        mRect.right = location[0] + mView.getWidth();
        mRect.bottom = location[1] + mView.getHeight();

        return mRect;
    }

    private void isLiked(String postid, final ImageView imageView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()) {
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

    private void userInfo(final Post post, final ImageView imgProfile, final TextView txtUsername) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users users = snapshot.getValue(Users.class);
                    if (post.getPublisher().equals(users.getId())) {
                        Picasso.with(mContext).load(users.getImageurl()).into(imgProfile);
                        txtUsername.setText(users.getUsername());
                    }
                }
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
        hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap.put("text", "liked your post");
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);

        reference.push().setValue(hashMap);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(20);
        }
    }
}
