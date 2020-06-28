package com.rohith.instaLovee.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.rohith.instaLovee.R;
import com.rohith.instaLovee.adapters.PostImageUserAdapter;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private ImageView mImgClose;
    private TextView mTxtPost;
    private EditText mEdtDescription;
    private RecyclerView mRecyclerView;
    private Button mBtnAdd;
    private ArrayList<Uri> mListImgUrl = new ArrayList<>();
    private Uri mImgUri;
    private StorageTask mStorageTask;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initView();
        initData();
        initAction();
    }

    private void initView() {
        mImgClose = findViewById(R.id.iconClose);
        mTxtPost = findViewById(R.id.txtPost);
        mEdtDescription = findViewById(R.id.edtDescription);
        mRecyclerView = findViewById(R.id.recyclerviewTest);
        mBtnAdd = findViewById(R.id.btnAdd);
    }

    private void initData() {
        mStorageReference = FirebaseStorage.getInstance().getReference("posts");
        startCropImage();
    }

    private void initAction() {
        mImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mTxtPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCropImage();
            }
        });
    }

    private void startCropImage() {
        CropImage.activity()
                .setAspectRatio(1,1)
                .start(PostActivity.this);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        return map.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting...");
        progressDialog.show();
        final ArrayList<String> list = new ArrayList<>();

        if (mImgUri != null) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
            final String postid = reference.push().getKey();

            for (int i = 0; i < mListImgUrl.size(); i++) {
                final StorageReference storageReference = mStorageReference.child(System.currentTimeMillis()
                        + "...." + i + getFileExtension(mListImgUrl.get(i)));

                mStorageTask = storageReference.putFile(mListImgUrl.get(i));
                mStorageTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return storageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            list.add(downloadUri.toString());

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("postid", postid);
                            hashMap.put("postimages", list);
                            hashMap.put("description", mEdtDescription.getText().toString());
                            hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            hashMap.put("timepost", Calendar.getInstance().getTime().toString());

                            reference.child(postid).updateChildren(hashMap);
                            progressDialog.dismiss();
                            finish();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImgUri = result.getUri();
            mListImgUrl.add(mImgUri);
            setAdapter();
        } else {
            Toast.makeText(getApplicationContext(), "Something gone wrong!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setAdapter() {
        PostImageUserAdapter postImageUserAdapter = new PostImageUserAdapter(this, mListImgUrl);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(postImageUserAdapter);
    }
}
