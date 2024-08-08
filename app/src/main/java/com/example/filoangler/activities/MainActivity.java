package com.example.filoangler.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.filoangler.LoginManager;
import com.example.filoangler.R;
import com.example.filoangler.Utils;

public class MainActivity extends AppCompatActivity {

    private LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginManager = new LoginManager(this);

    }

    @Override
    protected void onStart(){
        super.onStart();

        if(loginManager.GetCurrentUser() != null){
            Utils.ChangeIntent(this, BloggingActivity.class);
            finish();
        }else{
            Utils.ChangeIntent(this, LoginActivity.class);
            finish();
        }
    }

}