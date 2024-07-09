package com.example.filoangler;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class User {
    public static void LogOutUser(Context context) {
        FirebaseAuth.getInstance().signOut();
        // Redirect to login screen or perform any other post-logout actions
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void LogOutFromGoogle(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_WEB_CLIENT_ID")
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, gso);
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });
    }

    //This method logs in the user through manual email
    public static void LoginUser(Context context, String email, String password) {
        //Checks if fields have value
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "The input fields must not be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        //This authenticates the user through firebase DB
        try {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Successfully logged in!", Toast.LENGTH_SHORT).show();
                            // Redirect to the main activity or perform other post-login actions
                            Intent intent = new Intent(context, BlogActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Incorrect password or email, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public static void AddUserToDatabase(FirebaseAuth Auth, String RegisterType){
        FirebaseUser User = Auth.getCurrentUser();

        HashMap<String, Object> map = new HashMap<>();
        map.put("Id",User.getUid());
        map.put("Email", User.getEmail());
        map.put("Name", User.getDisplayName());
        map.put("ProfilePic",User.getPhotoUrl().toString());
        map.put("Account",RegisterType);

        FirebaseDatabase.getInstance("https://filoangler-24b41-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference()
                .child("Users")
                .child(User.getUid())
                .setValue(map);
    }

}
