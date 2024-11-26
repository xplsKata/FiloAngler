package com.example.filoangler.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.FishDatabankModel;
import com.example.filoangler.R;
import com.example.filoangler.activities.FishDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GearDatabankAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    @Override
    public int getItemViewType(int position) {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {

    }

    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;

        SectionHeaderViewHolder(View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.txtLetter);
        }
    }

    static class GearItemViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout gearContainerButton;
        TextView gearName;
        ImageView gearImage;

        GearItemViewHolder(View itemView) {
            super(itemView);
            gearContainerButton = itemView.findViewById(R.id.gearContainer);
            gearName = itemView.findViewById(R.id.txtGearName);
            gearImage = itemView.findViewById(R.id.imgGear);
        }
    }

}

