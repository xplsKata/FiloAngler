package com.example.filoangler.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.filoangler.R;
import com.example.filoangler.activities.NewsActivity;

public class NewsFragment extends Fragment {

    private Button btnEvents;
    private Button btnEnvironment;
    private Button btnGear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        btnEvents = view.findViewById(R.id.btnEvents);
        btnEnvironment = view.findViewById(R.id.btnEnvironment);
        btnGear = view.findViewById(R.id.btnGear);

        btnEvents.setOnClickListener(v -> changeIntent("International fishing tourney trains spotlight on Siargao Island"));
        btnEnvironment.setOnClickListener(v -> changeIntent("Environment"));
        btnGear.setOnClickListener(v -> changeIntent("Gear"));

        return view;
    }

    private void changeIntent(String title){
        Intent intent = new Intent(getActivity(), NewsActivity.class);
        intent.putExtra("title", title);
        startActivity(intent);
    }
}