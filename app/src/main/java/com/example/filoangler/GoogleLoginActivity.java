package com.example.filoangler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.annotations.Nullable;

public class GoogleLoginActivity extends AppCompatActivity {
    private LoginManager LoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoginManager = new LoginManager(this);

        GoogleLogin();

    }

    public void GoogleLogin(){
        LoginManager.GoogleLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LoginManager.RC_SIGN_IN) {
            LoginManager.SignInResult(data);
        }
    }
}