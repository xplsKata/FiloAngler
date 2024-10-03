package com.example.filoangler.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.filoangler.Adapter.FishDatabankAdapter;
import com.example.filoangler.Model.FishDatabankModel;
import com.example.filoangler.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class FishDatabankFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fish_databank, container, false);

        // Read JSON from the assets folder
        String json = loadJSONFromAsset(getContext(), "fish_databank.json");

        // Parse JSON to a map where key is the section (A, B, C, etc.) and value is the list of fish
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, List<FishDatabankModel>>>() {}.getType();
        Map<String, List<FishDatabankModel>> fishDataMap = gson.fromJson(json, mapType);

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Pass data to the adapter
        FishDatabankAdapter adapter = new FishDatabankAdapter(getContext(), fishDataMap);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return json;
    }
}