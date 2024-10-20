package com.example.filoangler.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.filoangler.R;
import com.example.filoangler.WaveView;

public class TideFragment extends Fragment {

    private WaveView waveView;
    private View water_container;

    private int minHeight;
    private int maxHeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tide, container, false);

        waveView = view.findViewById(R.id.waveView);
        water_container = view.findViewById(R.id.water_container);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Convert dp to pixels
        float density = getResources().getDisplayMetrics().density;
        minHeight = (int) (100 * density);

        // Wait for the parent container to be laid out
        water_container.post(new Runnable() {
            @Override
            public void run() {
                maxHeight = water_container.getHeight() - (int) (10 * density);
                updateWaveViewHeight(getCurrentWaterLevel());
            }
        });

    }

    private void updateWaveViewHeight(float waterLevel) {
        // Ensure waterLevel is between 0 and 1
        waterLevel = Math.max(0, Math.min(1, waterLevel));

        // Calculate new height
        int newHeight = (int) (minHeight + (maxHeight - minHeight) * waterLevel);

        // Update WaveView layout params
        ViewGroup.LayoutParams layoutParams = waveView.getLayoutParams();
        layoutParams.height = newHeight;
        waveView.setLayoutParams(layoutParams);

        waveView.setAmplitude(newHeight * 0.2f); // Adjust amplitude based on new height
        waveView.invalidate();
    }

    // Implement this method to get the current water level
    private float getCurrentWaterLevel() {
        float waterLevel = 1f;

        return waterLevel; // Placeholder return value
    }

}