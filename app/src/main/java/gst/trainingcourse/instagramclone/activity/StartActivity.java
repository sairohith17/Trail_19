package gst.trainingcourse.instagramclone.activity;

import androidx.appcompat.app.AppCompatActivity;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.helper.CheckConnection;
import gst.trainingcourse.instagramclone.main.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    private Button mBtnLogin, mBtnRegister;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onStart() {
        super.onStart();
        CheckConnection connection = new CheckConnection(StartActivity.this);
        if (connection.isConnected()) {
            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (mFirebaseUser != null) {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mBtnLogin = findViewById(R.id.btnLogin);
        mBtnRegister = findViewById(R.id.btnRegister);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, LoginActivity.class));
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, RegisterActivity.class));
            }
        });
    }
}
