package com.example.filoangler.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.filoangler.Dialog.MapDetailsDialog;
import com.example.filoangler.Dialog.PostDetailsDialog;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Model.MapLocationsModel;
import com.example.filoangler.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LatLngBounds philippinesBounds;
    private AuthManager authManager;
    private HashMap<Marker, String> markerLocationId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout first
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        authManager = new AuthManager();
        markerLocationId = new HashMap<>();

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

        getLocations();

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                String locationId = markerLocationId.get(marker);

                locationDetailsDialog(locationId);

                return false;
            }
        });
    }

    public void getLocations(){

        authManager.GetDb().getReference().child("Maps")
                .addListenerForSingleValueEvent(new ValueEventListener() {


                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            MapLocationsModel mapLocationsModel = dataSnapshot.getValue(MapLocationsModel.class);

                            String locationId = mapLocationsModel.getLocationId();
                            String locationName = mapLocationsModel.getLocationName();
                            double locationLatitude = mapLocationsModel.getLocationLatitude();
                            double locationLongitude = mapLocationsModel.getLocationLongitude();

                            addMarker(locationLatitude, locationLongitude, locationName, locationId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void addMarker(double latitude, double longitude, String locationName, String locationId){
        Marker marker = googleMap.addMarker(new MarkerOptions()
            .position(new LatLng(latitude, longitude))
            .title(locationName));

        markerLocationId.put(marker, locationId);
    }

    public void locationDetailsDialog(String locationId){

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_map_details_dialog);

        if(locationId != null){
            MapDetailsDialog mapDetailsDialog = new MapDetailsDialog(getContext(), locationId);
            mapDetailsDialog.showDialog(dialog);
        }

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

}