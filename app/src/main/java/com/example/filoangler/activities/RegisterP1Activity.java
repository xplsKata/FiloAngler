package com.example.filoangler.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.filoangler.R;
import com.squareup.picasso.Picasso;

import java.util.regex.Pattern;

public class RegisterP1Activity extends AppCompatActivity {

    //Elements
    private EditText txtEmail;
    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private Button btnConfirm;
    private ImageView imgLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_p1);

        imgLogo = findViewById(R.id.imgLogo);
        txtEmail = findViewById(R.id.txtEmail);
        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPassword = findViewById(R.id.txtConfirmPw);
        btnConfirm = findViewById(R.id.btnConfirm);

        imgLogo.post(() -> {
            int width = imgLogo.getWidth();
            int height = imgLogo.getHeight();

            // Only proceed with loading if dimensions are valid
            if (width > 0 && height > 0) {
                Picasso.get()
                        .load(R.drawable.logotemp)
                        .resize(width, height)
                        .centerInside()
                        .into(imgLogo);
            } else {
                // Fallback to load without resize
                Picasso.get()
                        .load(R.drawable.logotemp)
                        .into(imgLogo);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate inputs
                if (validateInputs()) {
                    String Email = txtEmail.getText().toString().trim();
                    String Username = txtUsername.getText().toString().trim();
                    String Password = txtPassword.getText().toString().trim();

                    Intent intent = new Intent(RegisterP1Activity.this, RegisterP2Activity.class);
                    intent.putExtra("Email", Email);
                    intent.putExtra("Username", Username);
                    intent.putExtra("Password", Password);
                    startActivity(intent);
                }
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate Email
        String email = txtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            txtEmail.setError("Email is required");
            isValid = false;
        } else if (!isValidEmail(email)) {
            txtEmail.setError("Invalid email format");
            isValid = false;
        }

        // Validate Username
        String username = txtUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            txtUsername.setError("Username is required");
            isValid = false;
        } else if (!isValidUsername(username)) {
            txtUsername.setError("Username can only contain letters and numbers");
            isValid = false;
        }

        // Validate Password
        String password = txtPassword.getText().toString().trim();
        String confirmPassword = txtConfirmPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            txtPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            txtPassword.setError("Password must be at least 6 characters long");
            isValid = false;
        }

        // Validate Confirm Password
        if (TextUtils.isEmpty(confirmPassword)) {
            txtConfirmPassword.setError("Confirm Password is required");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            txtConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    // Email validation method
    private boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    // Username validation method (only letters and numbers)
    private boolean isValidUsername(String username) {
        // Regex pattern to allow only letters and numbers
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
        return pattern.matcher(username).matches();
    }
}