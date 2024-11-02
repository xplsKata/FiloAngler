package com.example.filoangler.Dialog;

import android.app.Dialog;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.filoangler.R;

import com.squareup.picasso.Picasso;

public class MiscWeatherDialog extends Fragment {

    private TextView txtWind;
    private TextView txtTemperature;
    private TextView txtHumidity;

    private ImageView imgWindIcon;
    private ImageView imgTemperatureIcon;
    private ImageView imgHumidityIcon;

    private double windSpeed;
    private double temperature;
    private int humidity;

    public MiscWeatherDialog(){

    }

    public MiscWeatherDialog(double windSpeed, double temperature, int humidity){
        this.windSpeed = windSpeed;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public void getDialog(Dialog dialog) {

        txtWind = dialog.findViewById(R.id.txtWind);
        txtTemperature = dialog.findViewById(R.id.txtTemperature);
        txtHumidity = dialog.findViewById(R.id.txtHumidity);

        imgWindIcon = dialog.findViewById(R.id.imgWindIcon);
        imgTemperatureIcon = dialog.findViewById(R.id.imgTemperatureIcon);
        imgHumidityIcon = dialog.findViewById(R.id.imgHumidityIcon);

        loadImage(imgWindIcon, R.drawable.weather_wind);
        loadImage(imgTemperatureIcon, R.drawable.weather_thermometer);
        loadImage(imgHumidityIcon, R.drawable.weather_humidity);

        txtWind.setText(getWindSpeedText());
        txtTemperature.setText(getTemperatureText());
        txtHumidity.setText(getHumidityText());

    }

    private void loadImage(ImageView imageView, int Icon) {
        imageView.post(() -> {
            int width = imageView.getWidth();
            int height = imageView.getHeight();

            if (width > 0 && height > 0) {
                Picasso.get()
                        .load(Icon)
                        .resize(width, height)
                        .into(imageView);
            } else {
                Picasso.get()
                        .load(Icon)
                        .into(imageView);
            }
        });
    }

    private String getWindSpeedText(){
        final double HIGH_SPEED_THRESHOLD = 30;
        final double MEDIUM_SPEED_THRESHOLD = 15;

        if (windSpeed > HIGH_SPEED_THRESHOLD) {
            return "High Speed: Strong winds can create rough waters, making fishing challenging and potentially unsafe. " +
                    "Use weighted lures to avoid surface drift, and be cautious near water edges.";
        } else if (windSpeed > MEDIUM_SPEED_THRESHOLD) {
            return "Medium Speed: Moderate winds can stir up baitfish, attracting larger fish. " +
                    "Use mid-weight lures and position yourself with the wind at your back for easier casting.";
        } else {
            return "Low Speed: Calm conditions make for smooth casting but may make fish more cautious. " +
                    "Use light lures or bait, and move quietly to avoid spooking fish.";
        }
    }

    private String getTemperatureText(){
        final double HIGH_TEMP_THRESHOLD = 30;
        final double MEDIUM_TEMP_THRESHOLD = 15;

        if (temperature >= HIGH_TEMP_THRESHOLD) {
            return "High Temperature: In hot weather, fish tend to stay deeper where it's cooler. " +
                    "Stay hydrated, fish in shaded areas, and try early morning or late afternoon for better fish activity.";
        } else if (temperature >= MEDIUM_TEMP_THRESHOLD) {
            return "Medium Temperature: Mild temperatures are ideal for fish to be active at varying depths. " +
                    "Experiment with different lure depths, and bring layers in case of temperature shifts.";
        } else {
            return "Low Temperature: Colder weather often drives fish to the bottom where water is warmer. " +
                    "Dress warmly, target deeper areas, and use slower lures for more responsive bites.";
        }
    }

    private String getHumidityText(){
        final int HIGH_HUMIDITY_THRESHOLD = 80;
        final int MEDIUM_HUMIDITY_THRESHOLD = 50;

        if (humidity >= HIGH_HUMIDITY_THRESHOLD) {
            return "High Humidity: High moisture levels may make conditions feel warmer. " +
                    "Wear lightweight, breathable clothing, and stay hydrated to avoid exhaustion.";
        } else if (humidity >= MEDIUM_HUMIDITY_THRESHOLD) {
            return "Medium Humidity: Comfortable fishing conditions, but still pack water to stay hydrated. " +
                    "Check for changing weather, as medium humidity can precede rain.";
        } else {
            return "Low Humidity: Dry air can be comfortable but may quickly dehydrate. " +
                    "Bring extra water and use sun protection, as low humidity often coincides with clear skies.";
        }
    }
}
