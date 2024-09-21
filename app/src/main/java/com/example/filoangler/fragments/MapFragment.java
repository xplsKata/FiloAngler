package com.example.filoangler.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.filoangler.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LatLngBounds philippinesBounds;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout first
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Now find the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Return the inflated view
        return view;
    }

    @Override
    public void onMapReady(GoogleMap map){
        googleMap = map;

        // Define the bounds of the Philippines (lat/lng values for the country's borders)
        philippinesBounds = new LatLngBounds(
                new LatLng(4.5, 116.0), // Southwest corner of the Philippines
                new LatLng(21.0, 127.0) // Northeast corner of the Philippines
        );

        // Move the camera to the Philippines and restrict user panning to stay within the bounds
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(philippinesBounds, 0));

        // Restrict the map to the bounds of the Philippines
        googleMap.setLatLngBoundsForCameraTarget(philippinesBounds);

        // Optional: Disable zooming out too far (optional)
        googleMap.setMinZoomPreference(5.0f);
        googleMap.setMaxZoomPreference(15.0f);

        // Optional: Add a marker at a specific location within the bounds
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(14.5995, 120.9842)) // Example: Manila coordinates
                .title("Manila"));
    }
}