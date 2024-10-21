package com.example.filoangler.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.TidesModel;
import com.example.filoangler.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TidesAdapter extends RecyclerView.Adapter<TidesAdapter.ViewHolder> {

    private Context mContext;
    private List<TidesModel> mTides;

    public TidesAdapter(Context mContext, List<TidesModel> mTides) {
        this.mContext = mContext;
        this.mTides = mTides;
    }

    @NonNull
    @Override
    public TidesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_tides, parent, false);
        return new TidesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TidesAdapter.ViewHolder holder, int position) {
        TidesModel tide = mTides.get(position);

        holder.txtTide.setText(tide.gettide_type());
        holder.txtDate.setText(new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(tide.gettideDateTime()));
        holder.txtHeight.setText(String.format(Locale.US, "%.2f m", tide.gettideHeight_mt()));
    }

    @Override
    public int getItemCount() {
        return mTides.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTide;
        public TextView txtDate;
        public TextView txtHeight;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTide = itemView.findViewById(R.id.txtTide);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtHeight = itemView.findViewById(R.id.txtHeight);
        }
    }
}