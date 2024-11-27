package com.example.filoangler.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filoangler.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText txtEmail;
    private Button btnConfirm;

    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI Components
        initializeComponents();

        // Set up button click listener
        setupButtonListeners();
    }

    private void initializeComponents() {
        txtEmail = findViewById(R.id.txtEmail);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending password reset email...");
        progressDialog.setCancelable(false);
    }

    private void setupButtonListeners() {
        btnConfirm.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void sendPasswordResetEmail() {
        String email = txtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            txtEmail.setError("Email is required");
            txtEmail.requestFocus();
            return;
        }

        // Show progress dialog
        progressDialog.show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Password reset email sent. Check your inbox.",
                                Toast.LENGTH_LONG).show();

                        txtEmail.setText("");
                    } else {
                        String errorMessage = "Failed to send password reset email.";
                        if (task.getException() != null) {
                            // Provide more specific error message
                            if (task.getException() instanceof
                                    com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                                errorMessage = "No user found with this email.";
                            } else if (task.getException() instanceof
                                    com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Invalid email address.";
                            }
                        }

                        Toast.makeText(ForgotPasswordActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}