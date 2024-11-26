package com.example.filoangler.fragments;

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
import com.example.filoangler.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class FishDatabankFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_databank, container, false);

        String json = Utils.loadJSONFromAsset(getContext(), "fish_databank.json");

        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, List<FishDatabankModel>>>() {}.getType();
        Map<String, List<FishDatabankModel>> fishDataMap = gson.fromJson(json, mapType);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FishDatabankAdapter adapter = new FishDatabankAdapter(getContext(), fishDataMap);
        recyclerView.setAdapter(adapter);

        return view;
    }

}