package com.example.filoangler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterP1Activity extends AppCompatActivity {

    //Elements
    private EditText txtEmail;
    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_p1);

        txtEmail = findViewById(R.id.txtEmail);
        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPassword = findViewById(R.id.txtConfirmPw);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MAKE SURE TO ADD HERE A VALIDATOR IF IT EXIST OR NOT

                if(TextUtils.isEmpty(txtEmail.getText().toString()) ||
                        TextUtils.isEmpty(txtPassword.getText().toString()) ||
                        TextUtils.isEmpty(txtUsername.getText().toString())){
                    Toast.makeText(RegisterP1Activity.this, "Input fields must not be empty!", Toast.LENGTH_LONG).show();
                }

                if(!txtPassword.getText().toString().equals(txtConfirmPassword.getText().toString())){
                    Toast.makeText(RegisterP1Activity.this,"Password does not match, please try again!", Toast.LENGTH_LONG).show();
                }else{
                    String Email = txtEmail.getText().toString();
                    String Username = txtUsername.getText().toString();
                    String Password = txtPassword.getText().toString();

                    Intent intent = new Intent(RegisterP1Activity.this, RegisterP2Activity.class);
                    intent.putExtra("Email", Email);
                    intent.putExtra("Username", Username);
                    intent.putExtra("Password", Password);
                    startActivity(intent);
                }
            }
        });
    }
}