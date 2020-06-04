package gst.trainingcourse.instagramclone.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import gst.trainingcourse.instagramclone.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;

public class OptionActivity extends AppCompatActivity {

    private RelativeLayout mRlSetting, mRlLogout;
    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        initView();
        initAction();
    }

    private void initView() {
        mToolBar = findViewById(R.id.toolBar);
        mRlSetting = findViewById(R.id.rlSetting);
        mRlLogout = findViewById(R.id.rlLogout);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Options");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initAction() {
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mRlLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(OptionActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        mRlSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show bottom settings
            }
        });
    }
}
