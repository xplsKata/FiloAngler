package com.example.filoangler.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.Waypoint;
import com.example.filoangler.R;

import java.util.List;
import java.util.Locale;

public class WaypointAdapter extends RecyclerView.Adapter<WaypointAdapter.WaypointViewHolder> {
    private List<Waypoint> waypoints;
    private OnWaypointClickListener listener;
    private int currentWaypoint = -1;

    public WaypointAdapter(List<Waypoint> waypoints, OnWaypointClickListener listener) {
        this.waypoints = waypoints;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WaypointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waypoint, parent, false);
        return new WaypointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaypointViewHolder holder, int position) {
        Waypoint waypoint = waypoints.get(position);
        holder.titleText.setText(waypoint.getTitle());
        holder.descriptionText.setText(waypoint.getDescription());
        holder.timestampText.setText(formatTime(waypoint.getTimestamp()));

        // Highlight current waypoint
        holder.itemView.setBackgroundColor(position == currentWaypoint ?
                Color.parseColor("#E0E0E0") : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return waypoints.size();
    }

    public void setCurrentWaypoint(int position) {
        int oldPosition = currentWaypoint;
        currentWaypoint = position;
        if (oldPosition != -1) notifyItemChanged(oldPosition);
        notifyItemChanged(currentWaypoint);
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    class WaypointViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView timestampText;

        WaypointViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.waypointTitle);
            descriptionText = itemView.findViewById(R.id.waypointDescription);
            timestampText = itemView.findViewById(R.id.waypointTimestamp);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onWaypointClick(position);
                }
            });
        }
    }

    public interface OnWaypointClickListener {
        void onWaypointClick(int position);
    }
}
