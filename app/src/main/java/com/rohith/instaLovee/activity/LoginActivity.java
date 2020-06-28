package com.rohith.instaLovee.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.rohith.instaLovee.R;
import com.rohith.instaLovee.helper.CheckConnection;
import com.rohith.instaLovee.main.MainActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText mEdtEmail, mEdtPassword;
    private String mEmail, mPassword;
    private Button mBtnLogin;
    private TextView mTxtRegister;
    private ProgressDialog mProgressDialog;
    private boolean mCheck = true;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initData();
        initAction();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initAction() {
        mTxtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        mEdtPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(event.getRawX() >= (mEdtPassword.getRight() - mEdtPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (mCheck) {
                            mCheck = false;
                            mEdtPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                            mEdtPassword.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_visibility_black_24dp,0);
                        } else {
                            mCheck = true;
                            mEdtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            mEdtPassword.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_visibility_off_black_24dp,0);
                        }
                        return false;
                    }
                }
                return false;
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog = new ProgressDialog(LoginActivity.this);
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.show();

                mEmail = mEdtEmail.getText().toString();
                mPassword = mEdtPassword.getText().toString();

                CheckConnection connection = new CheckConnection(LoginActivity.this);

                if (mEmail.matches("") || mPassword.matches("")) {
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                } else {
                    if (connection.isConnected()) {
                        mFirebaseAuth.signInWithEmailAndPassword(mEmail, mPassword)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseAuth.getCurrentUser().getUid());
                                            reference.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    mProgressDialog.dismiss();
                                                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(i);
                                                    finish();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    mProgressDialog.dismiss();
                                                }
                                            });
                                        } else {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), "Authentication failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), "You must connect internet first!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void initData() {
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    private void initView() {
        mEdtEmail = findViewById(R.id.edtEmail);
        mEdtPassword = findViewById(R.id.edtPassword);
        mBtnLogin = findViewById(R.id.btnLogin);
        mTxtRegister = findViewById(R.id.txtRegister);
    }
}
