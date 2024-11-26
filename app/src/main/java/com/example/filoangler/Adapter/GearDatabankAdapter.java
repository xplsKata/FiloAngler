package com.example.filoangler.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.GearDatabankModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.example.filoangler.activities.DatabankDetailsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GearDatabankAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Object> itemList;

    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_GEAR_ITEM = 1;

    public GearDatabankAdapter(Context context, Map<String, List<GearDatabankModel>> sectionedGearMap) {
        this.mContext = context;
        this.itemList = new ArrayList<>();

        // Flatten the map into a list with alternating headers and gear items
        for (Map.Entry<String, List<GearDatabankModel>> entry : sectionedGearMap.entrySet()) {
            String header = entry.getKey();
            List<GearDatabankModel> gearList = entry.getValue();

            itemList.add(header); // Add the section header
            itemList.addAll(gearList); // Add all gear items under the header
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof String) {
            return TYPE_SECTION_HEADER;
        } else {
            return TYPE_GEAR_ITEM;
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
            View view = inflater.inflate(R.layout.item_gear, parent, false);
            return new GearItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SectionHeaderViewHolder) {
            String sectionHeader = (String) itemList.get(position);
            ((SectionHeaderViewHolder) holder).headerTextView.setText(sectionHeader);
        } else if (holder instanceof GearItemViewHolder) {
            GearDatabankModel gear = (GearDatabankModel) itemList.get(position);
            GearItemViewHolder gearHolder = (GearItemViewHolder) holder;

            gearHolder.gearName.setText(gear.getName());

            // Set gear image
            int resourceId = mContext.getResources().getIdentifier(gear.getGearImage(), "drawable", mContext.getPackageName());
            if (resourceId != 0) {
                Utils.loadImage(gearHolder.gearImage, resourceId);
            }

            gearHolder.gearContainerButton.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, DatabankDetailsActivity.class);
                intent.putExtra("GearName", gear.getName());
                intent.putExtra("Gear3DModel", gear.getGear3DModel());
                intent.putExtra("GearDescription", gear.getDescription());
                //intent.putExtra("GearTipsForUse", gear.getTipsForUse());
                //intent.putExtra("GearMaintenanceTips", gear.getMaintenanceTips());
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