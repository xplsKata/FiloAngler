package com.example.filoangler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterP2Activity extends AppCompatActivity {

    //Elements
    private EditText txtCode;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_p2);

        txtCode = findViewById(R.id.txtCode);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = getIntent().getStringExtra("Email");
                String Username = getIntent().getStringExtra("Username");
                String Password = getIntent().getStringExtra("Password");

                Intent intent = new Intent(RegisterP2Activity.this, RegisterP3Activity.class);
                intent.putExtra("Email", Email);
                intent.putExtra("Username", Username);
                intent.putExtra("Password", Password);
                startActivity(intent);

            }
        });
    }
}