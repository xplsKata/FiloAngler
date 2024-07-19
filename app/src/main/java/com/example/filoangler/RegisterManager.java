package com.example.filoangler;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.app.Person;
import android.content.Context;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterManager {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDb;
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
                            if(task.isSuccessful()){
                                //Added successfully
                            }else{
                                //not
                            }
                        }
                    });
        }else{
            //error for adding user
        }
    }

}
