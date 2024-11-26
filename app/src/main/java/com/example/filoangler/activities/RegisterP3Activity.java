package com.example.filoangler.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.filoangler.BuildConfig;
import com.example.filoangler.R;
import com.example.filoangler.Manager.RegisterManager;
import com.example.filoangler.Model.UserModel;
import com.example.filoangler.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class RegisterP3Activity extends AppCompatActivity {

    //Elements
    private Button btnConfirm;
    private ImageView imgLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_p3);

        RegisterManager registerManager = new RegisterManager();
        imgLogo = findViewById(R.id.imgLogo);

        btnConfirm = findViewById(R.id.btnConfirm);
        imgLogo.post(() -> {
            int width = imgLogo.getWidth();
            int height = imgLogo.getHeight();

            // Only proceed with loading if dimensions are valid
            if (width > 0 && height > 0) {
                Picasso.get()
                        .load(R.drawable.logotemp)
                        .resize(width, height)
                        .centerInside()
                        .into(imgLogo);
            } else {
                // Fallback to load without resize
                Picasso.get()
                        .load(R.drawable.logotemp)
                        .into(imgLogo);
            }
        });

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
                String Bio = "null";
                String ProfileIconURL = BuildConfig.defaultProfileIconURL;

                UserModel userModel = new UserModel(Email,
                        Password,
                        Username,
                        FirstName,
                        LastName,
                        Birthdate,
                        ProvinceAddress,
                        CityAddress,
                        AnglerStatus,
                        Bio,
                        ProfileIconURL);
                try{
                    registerManager.RegisterUser(userModel, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> taskAuth) {
                            if(taskAuth.isSuccessful()){
                                Objects.requireNonNull(taskAuth.getResult().getUser()).sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> taskVerify) {
                                        try{
                                            if(taskVerify.isSuccessful()){
                                                registerManager.AddUserToDatabase(userModel, taskAuth);
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
                    Log.e("TAG", "Error with adding userModel to db" + e);
                }
            }
        });
    }
}