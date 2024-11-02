package com.example.filoangler.Dialog;

import android.app.Dialog;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.filoangler.R;
import com.squareup.picasso.Picasso;

public class TideDetailsDialog extends Fragment {

    private String description;
    private double highTideHeight;
    private double lowTideHeight;
    private double currentHeight;

    private TextView txtDescription;
    private TextView txtBehavior;
    private TextView txtTips;
    private ImageView imgWeatherIcon;
    private ImageView imgFish;
    private ImageView imgTips;
    private ImageView imgBackground;

    public TideDetailsDialog(){

    }

    public TideDetailsDialog(String description, double highTideHeight, double lowTideHeight, double currentHeight){
        this.description = description;
        this.highTideHeight = highTideHeight;
        this.lowTideHeight = lowTideHeight;
        this.currentHeight = currentHeight;
    }

    public void getDialog(Dialog dialog){
        txtDescription = dialog.findViewById(R.id.txtWind);
        txtBehavior = dialog.findViewById(R.id.txtTemperature);
        txtTips = dialog.findViewById(R.id.txtHumidity);
        imgWeatherIcon = dialog.findViewById(R.id.imgWindIcon);
        imgFish = dialog.findViewById(R.id.imgTemperatureIcon);
        imgTips = dialog.findViewById(R.id.imgHumidityIcon);
        imgBackground = dialog.findViewById(R.id.imgBackground);

        loadImage(imgWeatherIcon, R.drawable.sea_level);
        loadImage(imgFish, R.drawable.fish);
        loadImage(imgTips, R.drawable.bulb);

        txtDescription.setText(description);
        txtBehavior.setText(getBehaviorForTideLevel());
        txtTips.setText(getTipForTideLevel());
    }

    private void loadImage(ImageView imageView, int Icon){
        imageView.post(() -> {
            int width = imageView.getWidth();
            int height = imageView.getHeight();

            // Only proceed with loading if dimensions are valid
            if (width > 0 && height > 0) {
                Picasso.get()
                        .load(Icon)
                        .resize(width, height)
                        .into(imageView);
            } else {
                // Fallback to load without resize
                Picasso.get()
                        .load(Icon)
                        .into(imageView);
            }
        });
    }

    private String getBehaviorForTideLevel(){
        if (currentHeight >= highTideHeight * 0.9) {
            return "Highest Tide: Fish are closer to the shoreline, feeding in shallow areas where food has been stirred up.";
        } else if (currentHeight <= lowTideHeight * 1.1) {
            return "Lowest Tide: Fish typically move to deeper waters, avoiding shallow zones that are overly exposed.";
        } else {
            return "Current Tide Level: Fish are often more active during rising or falling tides as they follow baitfish with the moving water.";
        }
    }

    private String getTipForTideLevel() {
        if (currentHeight >= highTideHeight * 0.9) {
            return "Highest Tide: Fish along the shoreline and around structures, but be mindful of strong currents. Ensure solid footing on wet rocks.";
        } else if (currentHeight <= lowTideHeight * 1.1) {
            return "Lowest Tide: Cast further out into deeper water, avoiding exposed and slippery rocks or muddy areas.";
        } else {
            return "Current Tide Level: Follow the moving water to fish hotspots, adjusting your position as the tide shifts. Stay cautious in areas prone to quick water level changes.";
        }
    }

}


