package com.example.filoangler.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filoangler.R;

public class FishDetailsActivity extends AppCompatActivity {

    TextView txtFishName;
    TextView txtFishDescription;
    TextView txtFishBehavior;
    TextView txtFishHabitat;
    TextView txtFishLaw;
    TextView txtFishLawLabel;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_details);

        // Initialize views
        txtFishName = findViewById(R.id.txtFishName);
        txtFishDescription = findViewById(R.id.txtFishDescription);
        txtFishBehavior = findViewById(R.id.txtFishBehavior);
        txtFishHabitat = findViewById(R.id.txtFishHabitat);
        txtFishLaw = findViewById(R.id.txtFishLaw);
        txtFishLawLabel = findViewById(R.id.textView22);
        btnBack = findViewById(R.id.btnBack);

        // Get data from intent
        String FishName = getIntent().getStringExtra("FishName");
        String Fish3DModel = getIntent().getStringExtra("Fish3DModel");
        String FishDescription = getIntent().getStringExtra("FishDescription");
        String FishBehavior = getIntent().getStringExtra("FishBehavior");
        String FishHabitat = getIntent().getStringExtra("FishHabitat");
        String FishLaw = getIntent().getStringExtra("FishLaw");

        // Set text views
        txtFishName.setText(FishName);
        txtFishDescription.setText(FishDescription);
        txtFishBehavior.setText(FishBehavior);
        txtFishHabitat.setText(FishHabitat);

        // Handle fish law visibility
        if(FishLaw.equals("none") || FishLaw.equals("None")){
            txtFishLaw.setVisibility(View.GONE);
            txtFishLawLabel.setVisibility(View.GONE);
        } else {
            txtFishLaw.setText(FishLaw);
            txtFishLaw.setVisibility(View.VISIBLE);
            txtFishLawLabel.setVisibility(View.VISIBLE);
        }

        // Set back button click listener
        btnBack.setOnClickListener(v -> finish());



    }

}