package com.rohith.instaLovee.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.rohith.instaLovee.R;
import com.rohith.instaLovee.main.MainActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEdtUsername, mEdtFullName, mEdtEmail, mEdtPassword;
    private String mUsername, mFullName, mEmail, mPassword;
    private Button mBtnRegister;
    private TextView mTxtLogin;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mReference;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        initData();
        initAcion();
    }

    private void initAcion() {
        mTxtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog = new ProgressDialog(RegisterActivity.this);
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();

                mUsername = mEdtUsername.getText().toString();
                mFullName = mEdtFullName.getText().toString();
                mEmail = mEdtEmail.getText().toString();
                mPassword = mEdtPassword.getText().toString();

                if (mUsername.matches("") || mFullName.matches("") || mEmail.matches("") || mPassword.matches("")) {
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (mPassword.length() < 6) {
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Password must have 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    register(mUsername, mFullName, mEmail, mPassword);
                }
            }
        });
    }

    private void register(final String username, final String fullName, final String email, String password) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            String userid = firebaseUser.getUid();

                            mReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username);
                            hashMap.put("fullname", fullName);
                            hashMap.put("bio", "");
                            hashMap.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/instagramclone-29c33.appspot.com/o/placeholder.png?alt=media&token=42cc105d-f7ed-48ef-8d51-855150ea6bd7");

                            mReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mProgressDialog.dismiss();
                                        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                    }
                                }
                            });
                        } else {
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "You can't register your email and password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initData() {
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    private void initView() {
        mEdtUsername = findViewById(R.id.edtUsername);
        mEdtFullName = findViewById(R.id.edtFullName);
        mEdtEmail = findViewById(R.id.edtEmail);
        mEdtPassword = findViewById(R.id.edtPassword);
        mBtnRegister = findViewById(R.id.btnRegister);
        mTxtLogin = findViewById(R.id.txtLogin);
    }
}
