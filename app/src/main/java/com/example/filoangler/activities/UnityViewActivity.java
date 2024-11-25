package com.example.filoangler.activities;

import android.os.Bundle;
import android.content.Intent;
import com.unity3d.player.UnityPlayerActivity;

public class UnityViewActivity extends UnityPlayerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the Fish3DModel from intent
        Intent intent = getIntent();
        if (intent != null) {
            String fish3DModel = intent.getStringExtra("Fish3DModel");
            // Send the model ID to Unity
            if (fish3DModel != null) {
                mUnityPlayer.UnitySendMessage("ModelContainer", "OnModelDataReceived", fish3DModel);
            }
        }
    }
}