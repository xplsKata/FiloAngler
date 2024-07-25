package com.example.filoangler.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.filoangler.LoginManager;
import com.google.firebase.database.annotations.Nullable;

public class GoogleLoginActivity extends AppCompatActivity {
    private com.example.filoangler.LoginManager LoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoginManager = new LoginManager(GoogleLoginActivity.this);
        LoginManager.GoogleLogin();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == com.example.filoangler.LoginManager.RC_SIGN_IN) {
            LoginManager.SignInResult(data);
        }
    }
}