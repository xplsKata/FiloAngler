package com.example.filoangler.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.CommonFishModel;
import com.example.filoangler.R;

import java.util.List;

public class CommonFishAdapter extends RecyclerView.Adapter<CommonFishAdapter.ViewHolder> {
    private Context mContext;
    private List<CommonFishModel> mFish;
    private String locationId;

    public CommonFishAdapter(Context mContext, List<CommonFishModel> mFish, String locationId) {
        this.mContext = mContext;
        this.mFish = mFish;
        this.locationId = locationId;
        setHasStableIds(true); // Add this for better performance
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_common_fish, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommonFishModel fish = mFish.get(position);
        System.out.println("Binding fish at position " + position + ": " + fish.getFishName());

        if (fish != null) {
            holder.txtFishName.setText(fish.getFishName());

            String imageName = fish.getFishImage();
            if (imageName != null && !imageName.isEmpty()) {
                int resourceId = mContext.getResources().getIdentifier(
                        imageName,
                        "drawable",
                        mContext.getPackageName()
                );

                if (resourceId != 0) {
                    holder.imgFish.setImageResource(resourceId);
                } else {
                    holder.imgFish.setImageResource(R.drawable.fish_barracuda);
                    System.out.println("Resource not found for image: " + imageName);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFish != null ? mFish.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return position; // or use a unique ID from your fish model if available
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgFish;
        public TextView txtFishName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFish = itemView.findViewById(R.id.imgFish);
            txtFishName = itemView.findViewById(R.id.txtFishName);
        }
    }
}
