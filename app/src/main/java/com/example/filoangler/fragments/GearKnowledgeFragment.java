package com.example.filoangler.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.filoangler.R;
import com.example.filoangler.activities.GearGuideActivity;

public class GearKnowledgeFragment extends Fragment {

    private View guideReelSetup;
    private View guideRodSetup;
    private View guideLeaderLineSetup;
    private View guideLureHookSetupOne;
    private View guideLureHookSetupTwo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gear_knowledge, container, false);

        initializeElements(view);
        setupListeners();

        return view;
    }

    private void initializeElements(View view){
        guideReelSetup = view.findViewById(R.id.reelSetupContainer);
        guideRodSetup = view.findViewById(R.id.rodSetupContainer);
        guideLeaderLineSetup = view.findViewById(R.id.leaderLineContainer);
        guideLureHookSetupOne = view.findViewById(R.id.lureHookImageContainer1);
        guideLureHookSetupTwo = view.findViewById(R.id.lureHookImageContainer2);
    }

    private void setupListeners(){
        guideReelSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGuide("guide_reel_setup");
            }
        });

        guideRodSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGuide("guide_rod_setup");
            }
        });

        guideLeaderLineSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGuide("guide_leader_line_setup");
            }
        });

        guideLureHookSetupOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGuide("guide_lure_hook_one_setup");
            }
        });

        guideLureHookSetupTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGuide("guide_lure_hook_two_setup");
            }
        });

    }

    private void showGuide(String videoUrl) {
        Intent intent = new Intent(getActivity(), GearGuideActivity.class);
        intent.putExtra("videoUrl", videoUrl);
        startActivity(intent);
    }
}