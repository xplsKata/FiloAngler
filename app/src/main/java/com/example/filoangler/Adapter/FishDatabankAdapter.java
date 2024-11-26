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
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.FishDatabankModel;
import com.example.filoangler.R;
import com.example.filoangler.activities.DatabankDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FishDatabankAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Object> itemList;

    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_FISH_ITEM = 1;

    public FishDatabankAdapter(Context context, Map<String, List<FishDatabankModel>> sectionedFishMap) {
        this.mContext = context;
        this.itemList = new ArrayList<>();

        // Flatten the map into a list with alternating headers and fish items
        for (Map.Entry<String, List<FishDatabankModel>> entry : sectionedFishMap.entrySet()) {
            String header = entry.getKey();
            List<FishDatabankModel> fishList = entry.getValue();

            itemList.add(header); // Add the section header
            itemList.addAll(fishList); // Add all fish items under the header
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof String) {
            return TYPE_SECTION_HEADER;
        } else {
            return TYPE_FISH_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (viewType == TYPE_SECTION_HEADER) {
            View view = inflater.inflate(R.layout.item_heading, parent, false);
            return new SectionHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_fish_button, parent, false);
            return new FishItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SectionHeaderViewHolder) {
            String sectionHeader = (String) itemList.get(position);
            ((SectionHeaderViewHolder) holder).headerTextView.setText(sectionHeader);
        } else if (holder instanceof FishItemViewHolder) {
            FishDatabankModel fish = (FishDatabankModel) itemList.get(position);
            FishItemViewHolder fishHolder = (FishItemViewHolder) holder;

            fishHolder.fishName.setText(fish.getFishName());

            // Set fish image
            int resourceId = mContext.getResources().getIdentifier(fish.getFishImage(), "drawable", mContext.getPackageName());
            if (resourceId != 0) {
                    Picasso.get().load(resourceId)
                            .into(fishHolder.fishImage);
            }

            fishHolder.fishButton.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, DatabankDetailsActivity.class);
                intent.putExtra("FishName", fish.getFishName());
                intent.putExtra("Fish3DModel", fish.getFish3DModel());
                intent.putExtra("FishDescription", fish.getDescription());
                intent.putExtra("FishBehavior", fish.getBehavior());
                intent.putExtra("FishHabitat", fish.getHabitat());
                intent.putExtra("FishLaw", fish.getLaw());
                mContext.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;

        SectionHeaderViewHolder(View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.txtLetter);
        }
    }

    static class FishItemViewHolder extends RecyclerView.ViewHolder {
        LinearLayout fishButton;
        TextView fishName;
        ImageView fishImage;

        FishItemViewHolder(View itemView) {
            super(itemView);
            fishButton = itemView.findViewById(R.id.btnFish);
            fishName = itemView.findViewById(R.id.txtFishName);
            fishImage = itemView.findViewById(R.id.imgFish);
        }
    }
}