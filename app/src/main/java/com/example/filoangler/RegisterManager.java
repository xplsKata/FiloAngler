package com.example.filoangler;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.filoangler.activities.BloggingActivity;
import com.example.filoangler.activities.RegisterP2Activity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RegisterManager {

    private final FirebaseAuth mAuth;
    private final FirebaseDatabase mDb;
    AuthManager authManager = new AuthManager();

    public RegisterManager(){
        this.mAuth = authManager.GetAuth();
        this.mDb = authManager.GetDb();
    }

    public void RegisterUser(User User, OnCompleteListener<AuthResult> onCompleteListener){
        mAuth.createUserWithEmailAndPassword(User.GetEmail(), User.GetPassword())
                .addOnCompleteListener(onCompleteListener);
    }

    public void AddUserToDatabase(User User, Task<AuthResult> Task){
        try{
            if(Task.isSuccessful()){
                Map<String, Object> AccountDetailsMap = new HashMap<>();
                Map<String, Object> PersonalInformationMap = new HashMap<>();

                AccountDetailsMap.put("User ID", (Task.getResult().getUser()).getUid());
                AccountDetailsMap.put("Email", User.GetEmail());
                AccountDetailsMap.put("Password", User.GetPassword());
                AccountDetailsMap.put("Username", User.GetUsername());
                AccountDetailsMap.put("Profile Icon", "null");
                AccountDetailsMap.put("Class", User.GetClass());

                PersonalInformationMap.put("First Name", User.GetFirstName());
                PersonalInformationMap.put("Last Name", User.GetLastname());
                PersonalInformationMap.put("Birthdate", User.GetBirthdate());
                PersonalInformationMap.put("City Address", User.GetCityAddress());
                PersonalInformationMap.put("Province Address", User.GetProvinceAddress());

                Map<String, Object> UserMap = new HashMap<>();
                UserMap.put("Account Details", AccountDetailsMap);
                UserMap.put("Personal Information", PersonalInformationMap);

                mDb.getReference()
                        .child("Users")
                        .child(Task.getResult()
                                .getUser()
                                .getUid())
                        .setValue(UserMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                try{
                                    if(task.isSuccessful()){
                                        //Added successfully
                                    }else{
                                        //not
                                    }
                                }catch(Exception e){
                                    Log.e("RegisterManager", "Error in registering in the user!" + e);
                                }
                            }
                        });
            }
        }catch(Exception e){
            Log.e("RegisterManager", "Error in registering in the user!" + e);
        }
    }

    public void CheckUserInDatabase(Context Context){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User exists, proceed to BloggingActivity
                        Utils.ChangeIntent(Context, BloggingActivity.class);
                    } else {
                        // User does not exist, proceed to RegisterP2Activity
                        Utils.ChangeIntent(Context, RegisterP2Activity.class);
                    }
                    ((Activity) Context).finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("LoginActivity", "DatabaseError: " + databaseError.getMessage());
                    Toast.makeText(Context, "Database error, please try again", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
