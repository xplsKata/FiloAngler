package com.example.filoangler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.filoangler.activities.BloggingActivity;
import com.example.filoangler.activities.GoogleLoginActivity;
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

public class LoginManager {
    public static final int RC_SIGN_IN = 9001;
    AuthManager authManager = new AuthManager();
    private final Context Context;
    private final GoogleSignInClient mGoogleSignInClient;
    private final FirebaseAuth mAuth;

    //Handles the login
    public LoginManager(Context Context) {
        this.mAuth = authManager.GetAuth();
        this.Context = Context;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1015978428089-h86rljsn3d0tlinebld4lbcf8v0phbno.apps.googleusercontent.com")
                .requestEmail()
                .build();
        this.mGoogleSignInClient = GoogleSignIn.getClient(Context, gso);
    }

    public void LoginUser(User User) {
        mAuth.signInWithEmailAndPassword(User.GetEmail(), User.GetPassword())
                .addOnCompleteListener((Activity) Context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, check if email is verified
                            if (GetCurrentUser() != null && GetCurrentUser().isEmailVerified()) {
                                //verified, toast login goods
                                Toast.makeText(Context, "Logged In Successfully!", Toast.LENGTH_SHORT).show();
                                Utils.ChangeIntent(Context, BloggingActivity.class);
                                ((Activity) Context).finish();
                            } else {
                                Toast.makeText(Context, "Email not verified! Verify email first before proceeding!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(Context, "No matching accounts, please try again!", Toast.LENGTH_LONG).show();
                            Log.e("LoginActivity", "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    //Handles Google login
    public void GoogleLogin() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        ((GoogleLoginActivity) Context).startActivityForResult(intent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((GoogleLoginActivity) Context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                    }
                });
    }

    public void SignInResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            //Error
        }
    }

    public void LogOut() {
        mAuth.signOut();
    }

    public FirebaseUser GetCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public FirebaseAuth GetFirebaseAuth() {
        return mAuth;
    }
}
