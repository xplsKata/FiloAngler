package com.example.filoangler.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.FishDatabankModel;
import com.example.filoangler.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FishDatabankAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<Object> itemList;

    private static final int TYPE_SECTION_HEADER = 0;
    private static final int TYPE_FISH_BUTTON = 1;

    public FishDatabankAdapter(Context context, Map<String, List<FishDatabankModel>> sectionedFishMap) {
        this.mContext = context;
        this.itemList = new ArrayList<>();

        // Flatten the map into a list with alternating headers and buttons
        for (Map.Entry<String, List<FishDatabankModel>> entry : sectionedFishMap.entrySet()) {
            String header = entry.getKey();
            List<FishDatabankModel> fishNames = entry.getValue();

            itemList.add(header); // Add the section header
            itemList.addAll(fishNames); // Add all fish names under the header
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof String && ((String) itemList.get(position)).length() == 1) {
            return TYPE_SECTION_HEADER; // Section header
        } else {
            return TYPE_FISH_BUTTON; // Fish button
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (viewType == TYPE_SECTION_HEADER) {
            View view = inflater.inflate(R.layout.item_fish_heading, parent, false);
            return new SectionHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_fish_button, parent, false);
            return new FishButtonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SectionHeaderViewHolder) {
            String sectionHeader = (String) itemList.get(position);
            ((SectionHeaderViewHolder) holder).headerTextView.setText(sectionHeader);
        } else if (holder instanceof FishButtonViewHolder) {
            String fishName = (String) itemList.get(position);
            ((FishButtonViewHolder) holder).fishName.setText(fishName);

            ((FishButtonViewHolder) holder).fishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle button click for fish
                }
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

    static class FishButtonViewHolder extends RecyclerView.ViewHolder {
        LinearLayout fishButton;
        TextView fishName;

        FishButtonViewHolder(View itemView) {
            super(itemView);
            fishButton = itemView.findViewById(R.id.btnFish);
            fishName = itemView.findViewById(R.id.txtFishName);
        }
    }
}
