package com.example.filoangler.Dialog;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.filoangler.R;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WeatherDialogFragment extends Fragment {

    private Context mContext;
    private Map<String, WeatherInfo> weatherInfoMap;

    private String description;
    private int weatherIcon;

    private TextView txtDescription;
    private TextView txtBehavior;
    private TextView txtTips;
    private ImageView imgWeatherIcon;
    private ImageView imgFish;
    private ImageView imgTips;

    private static class WeatherInfo {
        String Behavior;
        String Tip;
    }

    public WeatherDialogFragment(Context context, String description, int weatherIcon) {
        this.mContext = context;
        this.description = description;
        this.weatherIcon = weatherIcon;
    }

    public WeatherDialogFragment(){
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mContext != null) {
            loadJSONFromAsset("weather_more_info.json");
        }
    }

    public void getDialog(Dialog dialog){
        txtDescription = dialog.findViewById(R.id.txtDescription);
        txtBehavior = dialog.findViewById(R.id.txtFishBehavior);
        txtTips = dialog.findViewById(R.id.txtTips);
        imgWeatherIcon = dialog.findViewById(R.id.imgWeatherIcon);
        imgFish = dialog.findViewById(R.id.imgFish);
        imgTips = dialog.findViewById(R.id.imgTips);

        if (weatherInfoMap == null && mContext != null) {
            loadJSONFromAsset("weather_more_info.json");
        }

        updateWeatherInfo(description);

        loadIcon(imgWeatherIcon, weatherIcon);
        loadIcon(imgFish, R.drawable.fish);
        loadIcon(imgTips, R.drawable.bulb);

        txtDescription.setText(description);
    }

    private void loadIcon(ImageView imageView, int Icon){
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

    private void updateWeatherInfo(String weatherDescription) {
        if (weatherInfoMap != null) {
            String normalizedWeather = getNormalizedWeatherType(weatherDescription);
            Log.d(TAG, "Normalized weather: " + normalizedWeather);

            WeatherInfo info = weatherInfoMap.get(normalizedWeather);
            if (info != null) {
                Log.d(TAG, "Found weather info: " + info.toString());
                setBehavior(info.Behavior);
                setTips(info.Tip);
            } else {
                Log.e(TAG, "No weather info found for: " + normalizedWeather);
            }
        } else {
            Log.e(TAG, "weatherInfoMap is null!");
        }
    }

    private String getNormalizedWeatherType(String weatherDescription) {
        weatherDescription = weatherDescription.toLowerCase().trim();

        if (weatherDescription.contains("rain")) {
            return "Rain";
        } else if (weatherDescription.contains("drizzle")) {
            return "Drizzle";
        } else if (weatherDescription.contains("cloud")) {
            return "Cloudy";
        } else if (weatherDescription.contains("clear")) {
            return "Clear";
        } else if (weatherDescription.contains("sun")) {
            return "Sunny";
        } else if (weatherDescription.contains("thunder") || weatherDescription.contains("storm")) {
            return "Thunder Storm";
        }
        return "Cloudy"; // default case
    }

    private void setDescription(String description) {
        this.description = description;
        txtDescription.setText(description);
    }

    private void setBehavior(String behavior) {
        if (txtBehavior != null) {
            Log.d(TAG, "Setting behavior: " + behavior);
            txtBehavior.setText(behavior);
        } else {
            Log.e(TAG, "txtBehavior is null!");
        }
    }

    private void setTips(String tip) {
        if (txtTips != null) {
            Log.d(TAG, "Setting tips: " + tip);
            txtTips.setText(tip);
        } else {
            Log.e(TAG, "txtTips is null!");
        }
    }

    private void loadJSONFromAsset(String fileName) {
        try {
            if (mContext == null) {
                Log.e(TAG, "Context is null when trying to load JSON!");
                return;
            }

            InputStream is = mContext.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            Log.d(TAG, "Loaded JSON: " + json);  // This will help verify the JSON content

            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, WeatherInfo>>(){}.getType();
            weatherInfoMap = gson.fromJson(json, type);

            if (weatherInfoMap != null) {
                Log.d(TAG, "Successfully loaded " + weatherInfoMap.size() + " weather entries");
                for (Map.Entry<String, WeatherInfo> entry : weatherInfoMap.entrySet()) {
                    Log.d(TAG, "Weather: " + entry.getKey() + " -> " + entry.getValue().toString());
                }
            } else {
                Log.e(TAG, "Failed to parse JSON - weatherInfoMap is null");
            }

        } catch (IOException e) {
            Log.e(TAG, "Error loading JSON file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        loadJSONFromAsset("weather_more_info.json");
    }
}