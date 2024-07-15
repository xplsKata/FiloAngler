package com.example.filoangler;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {
    private FirebaseAuth mAuth;
    private Context Context;

    public AuthManager(Context Context){
        this.mAuth = FirebaseAuth.getInstance();
        this.Context = Context;
    }

    public void RegisterUser(User User){
        mAuth.createUserWithEmailAndPassword(User.GetEmail(), User.GetPassword())
                .addOnCompleteListener((Activity) Context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //Register complete

                        }else{
                            //Register failed

                        }
                    }
                });
    }

    public void LoginUser(User User){
        mAuth.signInWithEmailAndPassword(User.GetEmail(), User.GetPassword())
                .addOnCompleteListener((Activity) Context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //Login complete

                        }else{
                            //Login failed

                        }
                    }
                });
    }

    public void LogOut(){
        mAuth.signOut();
    }

    public FirebaseUser GetCurrentUser(){
        return mAuth.getCurrentUser();
    }
}
