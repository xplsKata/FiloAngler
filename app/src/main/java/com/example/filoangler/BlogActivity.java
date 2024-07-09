package com.example.filoangler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BlogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);

        FirebaseUser User = FirebaseAuth.getInstance().getCurrentUser();

        Button LogOut = findViewById(R.id.btnLogout);
        LogOut.setText(User.getUid().toString());

        LogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogOut();
            }
        });

    }

    public void LogOut(){
        User.LogOutUser(this);
        User.LogOutFromGoogle(this);
    }
}