package com.example.filoangler.activities;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filoangler.R;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Light;
import com.google.ar.sceneform.rendering.Color;

import java.util.concurrent.CompletableFuture;

public class FishDetailsActivity extends AppCompatActivity {

    TextView txtFishName;
    TextView txtFishDescription;
    TextView txtFishBehavior;
    TextView txtFishHabitat;
    TextView txtFishLaw;
    TextView txtFishLawLabel;
    ImageView btnBack;

    private SceneView sceneView;
    private ModelRenderable myRenderable;

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

        // Initialize SceneView
        sceneView = findViewById(R.id.sceneView);

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

        // Load 3D model
        try {
            load3DModel(Fish3DModel);
        } catch (Exception e) {
            Log.e("Sceneform", "Error loading 3D model: " + e.getMessage());
        }

    }

    // Lifecycle methods for SceneView
    @Override
    protected void onResume() {
        super.onResume();
        if(sceneView != null){
            try{
                sceneView.resume();
            }catch (Exception e){

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sceneView != null) {
            sceneView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sceneView != null) {
            sceneView.destroy();
        }
    }

    public void load3DModel(String modelName) {
        try {
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(modelName))
                    .setRegistryId(modelName)
                    .build()
                    .thenAccept(renderable -> {
                        myRenderable = renderable;
                        placeModel();
                    })
                    .exceptionally(throwable -> {
                        Log.e("Sceneform", "Error loading 3D model: " + modelName, throwable);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Error loading 3D model: " + modelName, Toast.LENGTH_LONG).show();
                        });
                        return null;
                    });
        } catch (Exception e) {
            Log.e("Sceneform", "Error setting up 3D model loading: " + modelName, e);
            Toast.makeText(this, "Error setting up 3D model loading: " + modelName, Toast.LENGTH_LONG).show();
        }
    }

    private void placeModel() {
        Node modelNode = new Node();
        modelNode.setRenderable(myRenderable);

        // Adjust model position, rotation, and scale as needed
        modelNode.setLocalPosition(new Vector3(0f, 0f, -1f));
        modelNode.setLocalScale(new Vector3(0.5f, 0.5f, 0.5f));

        sceneView.getScene().addChild(modelNode);
    }
}