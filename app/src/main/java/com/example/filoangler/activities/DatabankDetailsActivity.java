package com.example.filoangler.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filoangler.R;
import com.example.filoangler.Utils;

public class DatabankDetailsActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtDescription;
    private TextView txtLabelOne;
    private TextView txtContentOne;
    private TextView txtLabelTwo;
    private TextView txtContentTwo;
    private TextView txtLabelThree;
    private TextView txtContentThree;
    private TextView txtLabelFour;
    private TextView txtContentFour;
    private TextView txtModelView;
    private ImageView btnBack;
    private ImageView imgBig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_databank_details);

        initializeViews();
        setTexts();
        setupListeners();
    }

    private void initializeViews() {
        txtName = findViewById(R.id.txtFishName);
        txtModelView = findViewById(R.id.txtViewModel);
        btnBack = findViewById(R.id.btnBack);

        txtLabelOne = findViewById(R.id.textView19);
        txtContentOne = findViewById(R.id.txtFishDescription);
        txtLabelTwo = findViewById(R.id.textView25);
        txtContentTwo = findViewById(R.id.txtFishBehavior);
        txtLabelThree = findViewById(R.id.textView24);
        txtContentThree = findViewById(R.id.txtFishHabitat);
        txtLabelFour = findViewById(R.id.textView22);
        txtContentFour = findViewById(R.id.txtFishLaw);

        imgBig = findViewById(R.id.imgBig);
    }

    private void setTexts() {
        // Determine if it's a Fish or Gear details
        String type = getIntent().getStringExtra("DataType");

        if ("Fish".equals(type)) {
            setupFishDetails();
        } else if ("Gear".equals(type)) {
            setupGearDetails();
        }
    }

    private void setupFishDetails() {
        String FishName = getIntent().getStringExtra("FishName");
        String FishDescription = getIntent().getStringExtra("FishDescription");
        String FishBehavior = getIntent().getStringExtra("FishBehavior");
        String FishHabitat = getIntent().getStringExtra("FishHabitat");
        String FishLaw = getIntent().getStringExtra("FishLaw");
        String BigImage = getIntent().getStringExtra("ImageBig");

        txtLabelOne.setText("Description");
        txtLabelTwo.setText("Behavior");
        txtLabelThree.setText("Habitat");
        txtLabelFour.setText("Law");

        txtName.setText(FishName);
        txtContentOne.setText(FishDescription);
        txtContentTwo.setText(FishBehavior);
        txtContentThree.setText(FishHabitat);

        if (FishLaw == null || FishLaw.equalsIgnoreCase("none")) {
            txtContentFour.setVisibility(View.GONE);
            txtLabelFour.setVisibility(View.GONE);
        } else {
            txtContentFour.setText(FishLaw);
            txtContentFour.setVisibility(View.VISIBLE);
            txtLabelFour.setVisibility(View.VISIBLE);
        }

        if(BigImage != null){
            int resourceId = getResources().getIdentifier(BigImage, "drawable", getPackageName());
            Utils.loadImage(imgBig, resourceId);
        }
    }

    private void setupGearDetails() {
        String gearName = getIntent().getStringExtra("GearName");
        String gearDescription = getIntent().getStringExtra("GearDescription");
        String BigImage = getIntent().getStringExtra("ImageBig");
        String[] tipsForUse = getIntent().getStringArrayExtra("GearTipsForUse");
        String[] maintenanceTips = getIntent().getStringArrayExtra("GearMaintenanceTips");

        txtName.setText(gearName);

        // Description
        txtLabelOne.setText("Description");
        txtContentOne.setText(gearDescription);

        // Tips for Use
        txtLabelTwo.setText("Tips for Use");
        txtContentTwo.setText(formatTipsList(tipsForUse));

        // Maintenance Tips
        txtLabelThree.setText("Maintenance Tips");
        txtContentThree.setText(formatTipsList(maintenanceTips));

        // Hide Fishing Law for Gear
        txtLabelFour.setVisibility(View.GONE);
        txtContentFour.setVisibility(View.GONE);

        if(BigImage != null){
            int resourceId = getResources().getIdentifier(BigImage, "drawable", getPackageName());
            Utils.loadImage(imgBig, resourceId);
        }
    }

    private String formatTipsList(String[] tips) {
        if (tips == null || tips.length == 0) {
            return "No tips available";
        }

        StringBuilder formattedTips = new StringBuilder();
        for (String tip : tips) {
            formattedTips.append("• ").append(tip).append("\n");
        }

        // Remove the last newline character
        return formattedTips.toString().trim();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}