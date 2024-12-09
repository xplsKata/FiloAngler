package com.example.filoangler.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Adapter.CommonFishAdapter;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Model.CommonFishModel;
import com.example.filoangler.Model.MapLocationsModel;
import com.example.filoangler.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapDetailsDialog {

    private Context mContext;
    private AuthManager authManager;
    private String locationId;

    private TextView txtLocationName;
    private TextView txtDescriptionLabel;
    private TextView txtDescription;
    private TextView txtCommonFish;

    private RecyclerView recyclerView;

    private ScrollView scrollView;

    private CommonFishAdapter commonFishAdapter;

    private List<CommonFishModel> mFish;

    public MapDetailsDialog(){

    }

    public MapDetailsDialog(Context mContext, String locationId) {
        this.mContext = mContext;
        this.locationId = locationId;
        authManager = new AuthManager();
    }

    public void showDialog(Dialog dialog){
        mFish = new ArrayList<>();
        commonFishAdapter = new CommonFishAdapter(mContext, mFish, locationId);

        txtLocationName = dialog.findViewById(R.id.txtLocationName);
        txtDescriptionLabel = dialog.findViewById(R.id.txtDescriptionLabel);
        txtDescription = dialog.findViewById(R.id.txtDescription);
        txtCommonFish = dialog.findViewById(R.id.txtCommonFish);

        recyclerView = dialog.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        // Ensure proper layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        scrollView = dialog.findViewById(R.id.scrollView);
        recyclerView.setAdapter(commonFishAdapter);

        getDetails(locationId);
    }

    public void getDetails(String locationId) {
        System.out.println("Fetching details for locationId: " + locationId);

        authManager.GetDb().getReference().child("Maps")
                .child(locationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Print the raw data
                        System.out.println("Raw snapshot value: " + snapshot.getValue());

                        // Check if Common Fish exists in the raw data
                        DataSnapshot commonFishSnapshot = snapshot.child("Common Fish");
                        System.out.println("Common Fish exists: " + commonFishSnapshot.exists());
                        System.out.println("Common Fish children count: " + commonFishSnapshot.getChildrenCount());

                        MapLocationsModel mapLocationsModel = snapshot.getValue(MapLocationsModel.class);
                        System.out.println("MapLocationsModel parsed: " + (mapLocationsModel != null));

                        if (mapLocationsModel != null) {
                            System.out.println("Location Name: " + mapLocationsModel.getLocationName());
                            System.out.println("IsBeach: " + mapLocationsModel.getIsBeach());

                            txtLocationName.setText(mapLocationsModel.getLocationName());

                            if (mapLocationsModel.getIsBeach()) {
                                txtDescriptionLabel.setText(R.string.txtTerrain);
                                txtCommonFish.setVisibility(View.VISIBLE);
                                scrollView.setVisibility(View.VISIBLE);

                                // Try alternative way to get fish data
                                mFish.clear();

                                // Directly iterate through Common Fish children
                                for (DataSnapshot fishSnapshot : commonFishSnapshot.getChildren()) {
                                    try {
                                        System.out.println("Processing fish key: " + fishSnapshot.getKey());
                                        System.out.println("Raw fish data: " + fishSnapshot.getValue());

                                        CommonFishModel fish = fishSnapshot.getValue(CommonFishModel.class);
                                        if (fish != null) {
                                            System.out.println("Fish details - Name: " + fish.getFishName()
                                                    + ", Image: " + fish.getFishImage()
                                                    + ", ID: " + fish.getFishId());
                                            mFish.add(fish);
                                        } else {
                                            System.out.println("Failed to parse fish data");
                                        }
                                    } catch (Exception e) {
                                        System.err.println("Error parsing fish: " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                }

                                System.out.println("Final mFish list size: " + mFish.size());
                                commonFishAdapter.notifyDataSetChanged();

                            } else {
                                txtDescriptionLabel.setText(R.string.txtDetails);
                                txtCommonFish.setVisibility(View.GONE);
                                scrollView.setVisibility(View.GONE);
                            }

                            txtDescription.setText(mapLocationsModel.getLocationDescription());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.err.println("Database error: " + error.getMessage());
                    }
                });
    }

    public void getCommonFish(String locationId) {
        authManager.GetDb().getReference()
                .child("Maps")
                .child(locationId)
                .child("Common Fish")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mFish.clear();

                        System.out.println("Fetching fish data for location: " + locationId);
                        System.out.println("Number of fish entries: " + snapshot.getChildrenCount());

                        for (DataSnapshot fishSnapshot : snapshot.getChildren()) {
                            try {
                                CommonFishModel fish = fishSnapshot.getValue(CommonFishModel.class);
                                if (fish != null) {
                                    mFish.add(fish);
                                    System.out.println("Added fish: " + fish.getFishName());
                                } else {
                                    System.out.println("Failed to parse fish data for key: " + fishSnapshot.getKey());
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing fish at key " + fishSnapshot.getKey() + ": " + e.getMessage());
                                // Print the raw data for debugging
                                System.err.println("Raw data: " + fishSnapshot.getValue());
                            }
                        }

                        System.out.println("Total fish loaded: " + mFish.size());
                        commonFishAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.err.println("Database error: " + error.getMessage());
                    }
                });
    }

}
