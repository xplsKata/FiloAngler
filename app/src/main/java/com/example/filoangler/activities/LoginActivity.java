package com.example.filoangler.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.filoangler.LoginManager;
import com.example.filoangler.R;
import com.example.filoangler.User;
import com.example.filoangler.Utils;

public class LoginActivity extends AppCompatActivity {

    //Auth
    private com.example.filoangler.LoginManager LoginManager;

    //Elements
    private EditText txtEmail;
    private EditText txtPassword;

    private Button btnForgotPassword;
    private Button btnCreateAccount;
    private Button btnLogin;
    private Button btnGoogle;
    private Button btnOfflineMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginManager = new LoginManager(this);

        btnForgotPassword = findViewById(R.id.btnForgotPw);
        btnCreateAccount = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnOfflineMode = findViewById(R.id.btnOfflineMode);

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);

        //ForgotPassword Button
        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ChangeIntent(LoginActivity.this, ForgotPasswordActivity.class);
            }
        });

        //CreateAccount Button
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ChangeIntent(LoginActivity.this, RegisterP1Activity.class);
            }
        });

        //Login Button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = txtEmail.getText().toString();
                String Password = txtPassword.getText().toString();

                if(TextUtils.isEmpty(Email) || TextUtils.isEmpty(Password)){
                    Toast.makeText(LoginActivity.this, "Input fields must not be empty! Try again.", Toast.LENGTH_LONG).show();
                }else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                    Toast.makeText(LoginActivity.this, "Please input a valid email!", Toast.LENGTH_LONG).show();
                }else{
                    try{
                        User User = new User(Email, Password);
                        LoginManager.LoginUser(User);
                    }catch(Exception e){
                        Toast.makeText(LoginActivity.this, "Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
                        Log.e("LoginActivity", "Error in logging in the user!" + e);
                    }
                }
            }
        });

        //Google Login Button
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ChangeIntent(LoginActivity.this, GoogleLoginActivity.class);
            }
        });

        //Offline mode Button
        btnOfflineMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}