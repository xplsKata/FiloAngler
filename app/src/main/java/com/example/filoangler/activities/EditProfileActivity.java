package com.example.filoangler.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.CommentModel;
import com.example.filoangler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private Button btnSave;
    private ImageButton btnClose;
    private TextView btnChangePhoto;
    private EditText txtFirstName, txtLastName, txtUsername, txtBio;
    private ImageView imgProfile;
    private LoginManager loginManager;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        loginManager = new LoginManager(this);
        authManager = new AuthManager();

        btnSave = findViewById(R.id.btnSave);
        btnClose = findViewById(R.id.btnClose);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtUsername = findViewById(R.id.txtUsername);
        txtBio = findViewById(R.id.txtBio);
        imgProfile = findViewById(R.id.imgProfile);

        getProfileDetails();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ADD CHANGE PHOTO FUNCTIONALITY HERE
            }
        });

    }

    private void updateProfile() {

        try{
            HashMap<String, Object> personalInformationMap = new HashMap<>();
            personalInformationMap.put("FirstName", txtFirstName.getText().toString());
            personalInformationMap.put("LastName", txtLastName.getText().toString());
            personalInformationMap.put("Bio", txtBio.getText().toString());

            HashMap<String, Object> accountDetailsMap = new HashMap<>();
            accountDetailsMap.put("Username", txtUsername.getText().toString());

            authManager.GetDb().getReference()
                    .child("Users")
                    .child(loginManager.GetCurrentUser().getUid())
                    .child("Personal Information")
                    .updateChildren(personalInformationMap);

            authManager.GetDb().getReference()
                    .child("Users")
                    .child(loginManager.GetCurrentUser().getUid())
                    .child("Account Details")
                    .updateChildren(accountDetailsMap);
        }catch(Exception e){
            Toast.makeText(EditProfileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
            Log.e("EditProfileActivity", "Error: " + e);
        }finally {
            finish();
        }

    }

    private void getProfileDetails(){

        authManager.GetDb().getReference().child("Users")
                .child(loginManager.GetCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String firstName = snapshot.child("Personal Information").child("FirstName").getValue(String.class);
                        String lastName = snapshot.child("Personal Information").child("LastName").getValue(String.class);
                        String profileIconURL = snapshot.child("Personal Information").child("ProfileIconURL").getValue(String.class);
                        String username = snapshot.child("Account Details").child("Username").getValue(String.class);
                        String bio = snapshot.child("Personal Information").child("Bio").getValue(String.class);

                        if (profileIconURL != null && !profileIconURL.equals("null")) {
                            Picasso.get().load(profileIconURL).into(imgProfile);
                        } else {
                            imgProfile.setImageResource(R.mipmap.ic_launcher);
                        }

                        txtFirstName.setText(firstName);
                        txtLastName.setText(lastName);
                        txtUsername.setText(username);
                        txtBio.setText(bio);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}