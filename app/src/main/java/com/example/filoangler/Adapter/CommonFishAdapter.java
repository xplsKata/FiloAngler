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
        CommonFishModel fish = mFish.get(position);
        if (fish != null) {
            // Set the fish name
            holder.txtFishName.setText(fish.getFishName());

            // Handle the image
            String imageName = fish.getFishImage();
            if (imageName != null && !imageName.isEmpty()) {
                // Get the resource ID from the image name
                int resourceId = mContext.getResources().getIdentifier(
                        imageName,  // The name in your database (e.g., "fish_barracuda")
                        "drawable",
                        mContext.getPackageName()
                );

                if (resourceId != 0) {
                    holder.imgFish.setImageResource(resourceId);
                } else {
                    // Set a default image if resource not found
                    holder.imgFish.setImageResource(R.drawable.fish_barracuda);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFish != null ? mFish.size() : 0;
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
