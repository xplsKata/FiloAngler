package com.example.filoangler.Manager;

import com.example.filoangler.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AuthManager {
    private final FirebaseAuth mAuth;

    private final FirebaseDatabase mDb;

    public AuthManager(){
        this.mAuth = FirebaseAuth.getInstance();
        this.mDb = FirebaseDatabase.getInstance(BuildConfig.firebaseDatabaseApiKey);
    }

    public FirebaseAuth GetAuth(){
        return mAuth;
    }

    public FirebaseDatabase GetDb(){
        return mDb;
    }
}
