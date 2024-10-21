package com.example.filoangler.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.example.filoangler.R;
import com.example.filoangler.WaveView;

public class TideFragment extends Fragment {

    private WaveView waveView;
    private View water_container;

    private AutoCompleteTextView txtSearch;

    private TextView txtHighestTide;
    private TextView txtHighestTideTime;
    private TextView txtLowestTide;
    private TextView txtLowestTideTime;
    private TextView txtCurrentTide;
    private TextView txtLocation;
    private TextView txtAmLtDate;
    private TextView txtAmHtDate;
    private TextView txtPmLtDate;
    private TextView txtPmHtDate;
    private TextView txtAmLtHeight;
    private TextView txtPmLtHeight;
    private TextView txtAmHtHeight;
    private TextView txtPmHtHeight;

    private Button btnMore;

    private int minHeight;
    private int maxHeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tide, container, false);

        loadElements(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Convert dp to pixels
        float density = getResources().getDisplayMetrics().density;
        minHeight = (int) (10 * density);

        // Wait for the parent container to be laid out
        water_container.post(new Runnable() {
            @Override
            public void run() {
                maxHeight = water_container.getHeight() - (int) (10 * density);
                updateWaveViewHeight(getCurrentWaterLevel());
            }
        });

    }

    private void loadElements(View view){
        waveView = view.findViewById(R.id.waveView);
        water_container = view.findViewById(R.id.water_container);

        txtSearch = view.findViewById(R.id.txtSearch);

        txtHighestTide = view.findViewById(R.id.txtHighestTide);
        txtHighestTideTime = view.findViewById(R.id.txtHighestTideTime);
        txtLowestTide = view.findViewById(R.id.txtLowestTide);
        txtLowestTideTime = view.findViewById(R.id.txtLowestTideTime);
        txtCurrentTide = view.findViewById(R.id.txtCurrentTide);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtAmLtDate = view.findViewById(R.id.txtAmLtDate);
        txtAmHtDate = view.findViewById(R.id.txtAmHtDate);
        txtPmLtDate = view.findViewById(R.id.txtPmLtDate);
        txtPmHtDate = view.findViewById(R.id.txtPmHtDate);
        txtAmLtHeight = view.findViewById(R.id.txtAmLtHeight);
        txtPmLtHeight = view.findViewById(R.id.txtPmLtHeight);
        txtAmHtHeight = view.findViewById(R.id.txtAmHtHeight);
        txtPmHtHeight = view.findViewById(R.id.txtPmHtHeight);

        btnMore = view.findViewById(R.id.btnMore);
    }

    private void updateWaveViewHeight(float waterLevel) {
        waterLevel = Math.max(0, Math.min(1, waterLevel));

        int newHeight = (int) (minHeight + (maxHeight - minHeight) * waterLevel);

        ViewGroup.LayoutParams layoutParams = waveView.getLayoutParams();
        layoutParams.height = newHeight;
        waveView.setLayoutParams(layoutParams);

        waveView.setAmplitude(newHeight * 0.2f); // Adjust amplitude based on new height
        waveView.invalidate();
    }

    private float getCurrentWaterLevel() {
        float waterLevel = 0.5f;

        return waterLevel;
    }

}