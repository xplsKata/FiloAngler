package com.example.filoangler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.annotations.Nullable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private EditText txtEmail;
    private EditText txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);

        Button btnLogin =  findViewById(R.id.btnLogin);
        Button btnGoogle = findViewById(R.id.btnGoogle);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnForgotPw = findViewById(R.id.btnForgotPw);

        mAuth = FirebaseAuth.getInstance();

        //Button login calls method login user
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.LoginUser(LoginActivity.this,txtEmail.getText().toString(),txtPassword.getText().toString());
            }
        });

        //Button google login that uses google account
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleLogin();
            }
        });

        //Button register forwards them to register module
        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ChangeIntent(LoginActivity.this,RegisterActivity.class);
            }
        });

        //Button forgot password forwards them to forgot PW module
        btnForgotPw.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ChangeIntent(LoginActivity.this,ForgotPwActivity.class);
                finish();
            }
        });
    }

    //THIS METHOD LOGS THE USER IN WITH GOOGLE
    private void GoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1015978428089-h86rljsn3d0tlinebld4lbcf8v0phbno.apps.googleusercontent.com")
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this,gso);
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);
    }

    //THIS HANDLES GOOGLE OR FACEBOOK INTENT
    @Override
    protected void onActivityResult(int RequestCode, int ResultCode, @Nullable Intent Data){
        super.onActivityResult(RequestCode, ResultCode, Data);

        if(RequestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(Data);
            try{
                GoogleSignInAccount Account = task.getResult(ApiException.class);
                try{
                    AuthGoogleAccount(Account.getIdToken());
                }catch(Exception b){
                    Toast.makeText(this, "Something went wrong, please try again",Toast.LENGTH_SHORT).show();
                }
            }catch(Exception e){
                //Toast.makeText(this, "Something went wrong, please try again",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //THIS AUTHENTICATES IF THE GOOGLE ACCOUNT IS EXISTING OR NOT
    private void AuthGoogleAccount(String IdToken) {
        AuthCredential Credential = GoogleAuthProvider.getCredential(IdToken,null);
        mAuth.signInWithCredential(Credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            User.AddUserToDatabase(mAuth,"Google");
                            ChangeIntent(LoginActivity.this,BlogActivity.class);
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this,"Something went wrong, please try again",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    //This method logs out the user.
    public void LogoutUser(){
        mAuth.signOut();
    }

    public void ChangeIntent(Context MainIntent, Class GoToIntent){
        Intent intent = new Intent(MainIntent,GoToIntent);
        startActivity(intent);
    }
}