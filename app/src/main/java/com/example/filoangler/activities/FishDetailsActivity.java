package com.example.filoangler.activities;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filoangler.R;
import com.unity3d.player.UnityPlayer;

public class FishDetailsActivity extends AppCompatActivity {

    TextView txtFishName;
    TextView txtFishDescription;
    TextView txtFishBehavior;
    TextView txtFishHabitat;
    TextView txtFishLaw;
    TextView txtFishLawLabel;
    ImageView btnBack;

    //Unity
    FrameLayout unityLayout;
    UnityPlayer mUnityPlayer;

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

        bindUnity();

    }

    private void bindUnity(){
        unityLayout = findViewById(R.id.img_cake_container);

        // Remove Editor Image Placeholder.
        unityLayout.removeAllViews();

        getWindow().setFormat(PixelFormat.RGBX_8888);
        mUnityPlayer = new UnityPlayer(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        unityLayout.addView(mUnityPlayer.getView(), 0, lp);

        /*cakeManager = CakeManager.getInstance(); //Custom Code
        cake = cakeManager.getCurrentCake(); //Custom Code
        cake.addOnCakeChangeListener((property, newValue) -> { //Custom callback
            String message = "Property " + property + " changed to " + newValue;
            Log.d("CakeWatcher", "There's been a change in the cake.");
            // This is how you send data to Unity
            // param1 = Game Object Name Where the Script is
            // param2 = Script Function Name
            // param3 = String of arguments.
            UnityPlayer.UnitySendMessage(getString(R.string.unity_cake_object), getString(R.string.unity_cake_function), message);
        });*/
    }

}