package com.example.filoangler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Registers account and contacts firebase DB
        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUser();
            }
        });
    }

    //Finalizes registration
    public void RegisterUser(){
        EditText txtEmail = findViewById(R.id.txtEmail);
        EditText txtFName = findViewById(R.id.txtFName);
        EditText txtLName = findViewById(R.id.txtLName);
        EditText txtPassword = findViewById(R.id.txtPassword);
        EditText txtConfirmPw = findViewById(R.id.txtConfirmPw);

        //Checks email input
        if((txtEmail.getText().toString()).isEmpty()){
            Toast.makeText(RegisterActivity.this, "Please enter a valid Email address", Toast.LENGTH_LONG).show();
            return;
        }

        //Checks If names are empty
        if((txtFName.getText().toString()).isEmpty() || (txtLName.getText().toString()).isEmpty()){
            Toast.makeText(RegisterActivity.this, "Please fill out both name fields", Toast.LENGTH_LONG).show();
            return;
        }

        //Checks length of password
        if((txtPassword.getText().toString()).length() < 8){
            Toast.makeText(RegisterActivity.this, "Password must be 8 characters or longer", Toast.LENGTH_LONG).show();
            return;
        }

        //Confirms if both PW are the same
        if((txtConfirmPw.getText().toString()).equals(txtPassword.getText().toString())){
            try{
                String UserEmail = txtEmail.getText().toString();
                String UserPassword = txtPassword.getText().toString();
                String UserName = txtFName.getText().toString() + " " + txtLName.getText().toString();

                firebaseAuth.createUserWithEmailAndPassword(UserEmail, UserPassword)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    //Hash for the content of the table
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("Id",(task.getResult().getUser()).getUid());
                                    map.put("Email", UserEmail);
                                    map.put("Name", UserName);
                                    map.put("Password", UserPassword);
                                    map.put("ProfilePic","None");
                                    map.put("Account","Manual");

                                    //Adds credentials to database
                                    FirebaseDatabase.getInstance("https://filoangler-24b41-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                            .getReference()
                                            .child("Users")
                                            .child((task.getResult().getUser()).getUid())
                                            .setValue(map);

                                    Toast.makeText(RegisterActivity.this, "Successfully Registered!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }else{
                                    Toast.makeText(RegisterActivity.this, "Registering Failed! Invalid Email!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }catch(Exception e){
                Toast.makeText(RegisterActivity.this, "Exception error", Toast.LENGTH_SHORT).show();
                Log.d("Error", e.getMessage());
            }
        }else{
            Toast.makeText(RegisterActivity.this,"Password doesn't match. Try again.",Toast.LENGTH_LONG).show();
        }
    }

}