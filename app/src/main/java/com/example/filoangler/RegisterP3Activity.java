package com.example.filoangler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.Objects;

public class RegisterP3Activity extends AppCompatActivity {

    //Elements
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_p3);

        RegisterManager registerManager = new RegisterManager();

        btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = getIntent().getStringExtra("Email");
                String Username = getIntent().getStringExtra("Username");
                String Password = getIntent().getStringExtra("Password");
                String FirstName = getIntent().getStringExtra("FirstName");
                String LastName = getIntent().getStringExtra("LastName");
                String Birthdate = getIntent().getStringExtra("Birthdate");
                String ProvinceAddress = getIntent().getStringExtra("ProvinceAddress");
                String CityAddress = getIntent().getStringExtra("CityAddress");
                String AnglerStatus = getIntent().getStringExtra("AnglerStatus");

                User user = new User(Email, Password, Username, FirstName, LastName, Birthdate, ProvinceAddress, CityAddress, AnglerStatus);
                try{
                    registerManager.RegisterUser(user, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> taskAuth) {
                            if(taskAuth.isSuccessful()){
                                Objects.requireNonNull(taskAuth.getResult().getUser()).sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> taskVerify) {
                                        try{
                                            if(taskVerify.isSuccessful()){
                                                registerManager.AddUserToDatabase(user, taskAuth);
                                                Toast.makeText(RegisterP3Activity.this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                                                Utils.ChangeIntent(RegisterP3Activity.this, LoginActivity.class);
                                                finish();
                                            }
                                        }catch(Exception e){
                                            Log.e("RegisterActivity", "sendEmailVerification", taskVerify.getException());
                                        }
                                    }
                                });
                            }
                        }
                    });
                }catch(Exception e){
                    Log.e("TAG", "Error with adding user to db" + e);
                }
            }
        });
    }
}