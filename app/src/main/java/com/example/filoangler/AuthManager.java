package com.example.filoangler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AuthManager {
    private final FirebaseAuth mAuth;

    private final FirebaseDatabase mDb;

    public AuthManager(){
        this.mAuth = FirebaseAuth.getInstance();
        this.mDb = FirebaseDatabase.getInstance("https://filoangler-24b41-default-rtdb.asia-southeast1.firebasedatabase.app/");
    }

    public FirebaseAuth GetAuth(){
        return mAuth;
    }

    public FirebaseDatabase GetDb(){
        return mDb;
    }
}
