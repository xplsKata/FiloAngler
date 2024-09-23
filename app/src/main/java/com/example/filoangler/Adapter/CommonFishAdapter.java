package com.example.filoangler.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Model.CommonFishModel;
import com.example.filoangler.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommonFishAdapter extends RecyclerView.Adapter<CommonFishAdapter.ViewHolder>{

    private Context mContext;
    private List<CommonFishModel> mFish;
    private AuthManager authManager;

    private String locationId;

    public CommonFishAdapter(Context mContext, List<CommonFishModel> mFish, String locationId) {
        this.mContext = mContext;
        this.mFish = mFish;
        this.locationId = locationId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_common_fish, parent, false);

        return new CommonFishAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        authManager = new AuthManager();
        CommonFishModel commonFishModel = mFish.get(position);

        authManager.GetDb().getReference().child("Maps")
                .child(locationId)
                .child("Common Fish")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Picasso.get().load(commonFishModel.getFishImage()).into(holder.imgFish);
                        holder.txtFishName.setText(commonFishModel.getFishName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return mFish.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imgFish;
        public TextView txtFishName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgFish = itemView.findViewById(R.id.imgFish);
            txtFishName = itemView.findViewById(R.id.txtFishName);

        }
    }

}
