package com.example.filoangler.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.filoangler.Adapter.GearDatabankAdapter;
import com.example.filoangler.Model.GearDatabankModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class GearDatabankFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_databank, container, false);

        String json = Utils.loadJSONFromAsset(getContext(), "gear_database.json");

        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Map<String, List<GearDatabankModel>>>>() {}.getType();
        Map<String, Map<String, List<GearDatabankModel>>> gearDataMap = gson.fromJson(json, mapType);

        // Flatten the nested map for the adapter
        Map<String, List<GearDatabankModel>> flattenedGearMap = gearDataMap.get("fishing_equipment");

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        GearDatabankAdapter adapter = new GearDatabankAdapter(getContext(), flattenedGearMap);
        recyclerView.setAdapter(adapter);

        return view;
    }
}