package com.example.filoangler.activities;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filoangler.R;
import com.unity3d.player.UnityPlayer;

public class FishDetailsActivity extends AppCompatActivity {

    private TextView txtFishName;
    private TextView txtFishDescription;
    private TextView txtFishBehavior;
    private TextView txtFishHabitat;
    private TextView txtFishLaw;
    private TextView txtFishLawLabel;

    private ImageView btnBack;

    //Unity
    private FrameLayout unityLayout;
    private UnityPlayer mUnityPlayer;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_details);

        initializeViews();
        setTexts();
        setupListeners();
        bindUnity();


    }

    private void initializeViews(){
        txtFishName = findViewById(R.id.txtFishName);
        txtFishDescription = findViewById(R.id.txtFishDescription);
        txtFishBehavior = findViewById(R.id.txtFishBehavior);
        txtFishHabitat = findViewById(R.id.txtFishHabitat);
        txtFishLaw = findViewById(R.id.txtFishLaw);
        txtFishLawLabel = findViewById(R.id.textView22);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setTexts(){
        String FishName = getIntent().getStringExtra("FishName");
        String FishDescription = getIntent().getStringExtra("FishDescription");
        String FishBehavior = getIntent().getStringExtra("FishBehavior");
        String FishHabitat = getIntent().getStringExtra("FishHabitat");
        String FishLaw = getIntent().getStringExtra("FishLaw");

        txtFishName.setText(FishName);
        txtFishDescription.setText(FishDescription);
        txtFishBehavior.setText(FishBehavior);
        txtFishHabitat.setText(FishHabitat);

        if(txtFishLaw.equals("none") || FishLaw.equals("None")){
            txtFishLaw.setVisibility(View.GONE);
            txtFishLawLabel.setVisibility(View.GONE);
        } else {
            txtFishLaw.setText(FishLaw);
            txtFishLaw.setVisibility(View.VISIBLE);
            txtFishLawLabel.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            // Ensure Unity player is properly cleaned up before finishing
            if (mUnityPlayer != null) {
                mUnityPlayer.quit();
            }
            finish();
        });

    }

    //Unity
    @Override
    protected void onDestroy() {
        if (mUnityPlayer != null) {
            mUnityPlayer.quit();
        }
        mUnityPlayer = null; // Set to null to prevent any lingering references
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUnityPlayer != null) {
            mUnityPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUnityPlayer != null) {
            mUnityPlayer.resume();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mUnityPlayer != null) {
            mUnityPlayer.windowFocusChanged(hasFocus);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void bindUnity() {
        unityLayout = findViewById(R.id.imgFishModel);
        unityLayout.removeAllViews();

        mUnityPlayer = new UnityPlayer(this);

        getWindow().setFormat(PixelFormat.RGBA_8888);

        mUnityPlayer.requestFocus();
        mUnityPlayer.windowFocusChanged(true);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        unityLayout.addView(mUnityPlayer.getView(), 0, lp);

        // Get the Fish3DModel from intent
        String Fish3DModel = getIntent().getStringExtra("Fish3DModel");
        loadModel(Fish3DModel);
        setupGestureDetectors();
    }

    private void setupGestureDetectors() {
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                // Send zoom factor to Unity
                UnityPlayer.UnitySendMessage("ModelContainer", "OnZoomReceived", String.valueOf(scaleFactor));
                return true;
            }
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // Format: "distanceX,distanceY"
                String movement = distanceX + "," + distanceY;
                UnityPlayer.UnitySendMessage("ModelContainer", "OnRotationReceived", movement);
                return true;
            }
        });

        unityLayout.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void loadModel(String modelId) {
        // Send the model ID to Unity
        UnityPlayer.UnitySendMessage("ModelContainer", "OnModelDataReceived", modelId);
    }

}