package com.example.filoangler.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filoangler.R;

public class DatabankDetailsActivity extends AppCompatActivity {

    private TextView txtFishName;
    private TextView txtFishDescription;
    private TextView txtFishBehavior;
    private TextView txtFishHabitat;
    private TextView txtFishLaw;
    private TextView txtViewModel;
    private TextView txtFishLawLabel;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_details);

        initializeViews();
        setTexts();
        setupListeners();
    }

    private void initializeViews() {
        txtFishName = findViewById(R.id.txtFishName);
        txtFishDescription = findViewById(R.id.txtFishDescription);
        txtFishBehavior = findViewById(R.id.txtFishBehavior);
        txtFishHabitat = findViewById(R.id.txtFishHabitat);
        txtFishLaw = findViewById(R.id.txtFishLaw);
        txtFishLawLabel = findViewById(R.id.textView22);
        btnBack = findViewById(R.id.btnBack);
        txtViewModel = findViewById(R.id.txtViewModel);
    }

    private void setTexts() {
        String FishName = getIntent().getStringExtra("FishName");
        String FishDescription = getIntent().getStringExtra("FishDescription");
        String FishBehavior = getIntent().getStringExtra("FishBehavior");
        String FishHabitat = getIntent().getStringExtra("FishHabitat");
        String FishLaw = getIntent().getStringExtra("FishLaw");

        txtFishName.setText(FishName);
        txtFishDescription.setText(FishDescription);
        txtFishBehavior.setText(FishBehavior);
        txtFishHabitat.setText(FishHabitat);

        if (FishLaw == null || FishLaw.equalsIgnoreCase("none")) {
            txtFishLaw.setVisibility(View.GONE);
            txtFishLawLabel.setVisibility(View.GONE);
        } else {
            txtFishLaw.setText(FishLaw);
            txtFishLaw.setVisibility(View.VISIBLE);
            txtFishLawLabel.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        txtViewModel.setOnClickListener(v -> launch3DView());
    }

    private void launch3DView() {
        // Get the Fish3DModel ID from intent
        String fish3DModel = getIntent().getStringExtra("Fish3DModel");

        // Create intent for Unity activity
        Intent intent = new Intent(this, UnityViewActivity.class);
        // Pass the model ID to Unity
        intent.putExtra("Fish3DModel", fish3DModel);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}