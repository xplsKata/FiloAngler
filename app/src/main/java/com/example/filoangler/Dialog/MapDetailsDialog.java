package com.example.filoangler.Dialog;

import static java.security.AccessController.getContext;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.HorizontalScrollView;
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
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

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

    private HorizontalScrollView scrollView;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        scrollView = dialog.findViewById(R.id.scrollView);
        recyclerView.setAdapter(commonFishAdapter);

        getDetails(locationId);

    }

    public void getDetails(String locationId){
        authManager.GetDb().getReference().child("Maps")
                .child(locationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        MapLocationsModel mapLocationsModel = snapshot.getValue(MapLocationsModel.class);
                        if (mapLocationsModel != null) {
                            txtLocationName.setText(mapLocationsModel.getLocationName());

                            if(mapLocationsModel.getIsBeach()){
                                txtDescriptionLabel.setText(R.string.txtTerrain);
                                txtCommonFish.setVisibility(View.VISIBLE);
                                scrollView.setVisibility(View.VISIBLE);
                                getCommonFish(locationId);
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
                        // Handle the error
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

                        // Debug log
                        System.out.println("Total fish entries: " + snapshot.getChildrenCount());

                        for (DataSnapshot fishSnapshot : snapshot.getChildren()) {
                            try {
                                CommonFishModel fish = fishSnapshot.getValue(CommonFishModel.class);
                                if (fish != null) {
                                    mFish.add(fish);
                                    // Debug log
                                    System.out.println("Added fish: " + fish.getFishName());
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing fish data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        // Debug log
                        System.out.println("Final fish list size: " + mFish.size());

                        commonFishAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.err.println("Database error: " + error.getMessage());
                    }
                });
    }

}
