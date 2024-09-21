package com.example.filoangler.Manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.filoangler.BuildConfig;
import com.example.filoangler.Model.UserModel;
import com.example.filoangler.Utils;
import com.example.filoangler.activities.BloggingActivity;
import com.example.filoangler.activities.GoogleLoginActivity;
import com.example.filoangler.activities.RegisterP2Activity;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginManager {
    public static final int RC_SIGN_IN = 9001;
    private final Context context;
    private final GoogleSignInClient mGoogleSignInClient;
    private final FirebaseAuth mAuth;
    private final FirebaseDatabase mDb;

    public LoginManager(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        this.mDb = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.googleLoginApiKey)
                .requestEmail()
                .build();
        this.mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public void LoginUser(UserModel userModel) {
        mAuth.signInWithEmailAndPassword(userModel.getEmail(), userModel.getPassword())
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (GetCurrentUser() != null && GetCurrentUser().isEmailVerified()) {
                                Toast.makeText(context, "Logged In Successfully!", Toast.LENGTH_SHORT).show();
                                Utils.ChangeIntent(context, BloggingActivity.class);
                                ((Activity) context).finish();
                            } else {
                                Toast.makeText(context, "Email not verified! Verify email first before proceeding!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(context, "No matching accounts, please try again!", Toast.LENGTH_LONG).show();
                            Log.e("LoginActivity", "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    public void GoogleLogin() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        ((Activity) context).startActivityForResult(intent, RC_SIGN_IN);
    }

    public void handleSignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w("LoginManager", "Google sign in failed", e);
            Toast.makeText(context, "Google sign in failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkUserInDatabase(user);
                        } else {
                            Log.w("LoginManager", "signInWithCredential:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkUserInDatabase(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            DatabaseReference userRef = mDb.getReference().child("Users").child(firebaseUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Utils.ChangeIntent(context, BloggingActivity.class);
                    } else {
                        Utils.ChangeIntent(context, RegisterP2Activity.class);
                    }
                    ((Activity) context).finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("LoginManager", "DatabaseError: " + databaseError.getMessage());
                    Toast.makeText(context, "Database error, please try again", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void LogOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
    }

    public FirebaseUser GetCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public FirebaseAuth GetFirebaseAuth() {
        return mAuth;
    }
}
