package com.example.filoangler.Dialog;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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

public class MoonDialogFragment extends Fragment {

    private Context mContext;
    private Map<String, MoonDialogFragment.MoonInfo> moonInfoMap;

    private String moonPhase;
    private int moonIcon;

    private TextView txtDescription;
    private TextView txtBehavior;
    private TextView txtTips;
    private ImageView imgMoonIcon;
    private ImageView imgFish;
    private ImageView imgTips;
    private ImageView imgBackground;

    private static class MoonInfo {
        String Behavior;
        String Tip;
    }

    public MoonDialogFragment(Context context, String moonPhase, int moonIcon) {
        this.mContext = context;
        this.moonPhase = moonPhase;
        this.moonIcon = moonIcon;
    }

    public MoonDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mContext != null) {
            loadJSONFromAsset("moon_more_info.json");
        }
    }

    public void getDialog(Dialog dialog) {
        txtDescription = dialog.findViewById(R.id.txtDescription);
        txtBehavior = dialog.findViewById(R.id.txtFishBehavior);
        txtTips = dialog.findViewById(R.id.txtTips);
        imgMoonIcon = dialog.findViewById(R.id.imgWeatherIcon);
        imgFish = dialog.findViewById(R.id.imgFish);
        imgTips = dialog.findViewById(R.id.imgTips);
        imgBackground = dialog.findViewById(R.id.imgBackground);

        if (moonInfoMap == null && mContext != null) {
            loadJSONFromAsset("moon_more_info.json");
        }

        updateMoonInfo(moonPhase);

        loadImage(imgMoonIcon, moonIcon);
        loadImage(imgFish, R.drawable.fish);
        loadImage(imgTips, R.drawable.bulb);
        //loadBackgroundImage(background, imgBackground);

        txtDescription.setText(moonPhase);
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

    private void loadBackgroundImage(int drawableId, ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.get()
                .load(drawableId)
                .fit()
                .into(imageView);
    }

    private void updateMoonInfo(String moonPhase) {
        if (moonInfoMap != null) {
            String normalizedMoonPhase = getNormalizedMoonPhase(moonPhase);
            Log.d(TAG, "Normalized moon phase: " + normalizedMoonPhase);

            MoonDialogFragment.MoonInfo info = moonInfoMap.get(normalizedMoonPhase);
            if (info != null) {
                Log.d(TAG, "Found moon info: " + info.toString());
                setBehavior(info.Behavior);
                setTips(info.Tip);
            } else {
                Log.e(TAG, "No moon info found for: " + normalizedMoonPhase);
            }
        } else {
            Log.e(TAG, "moonInfoMap is null!");
        }
    }

    private String getNormalizedMoonPhase(String moonPhase) {
        moonPhase = moonPhase.toLowerCase().trim();

        // Map various possible inputs to the standardized moon phases
        if (moonPhase.contains("new moon") || moonPhase.equals("new")) {
            return "New Moon";
        } else if (moonPhase.contains("waxing crescent")) {
            return "Waxing Crescent";
        } else if (moonPhase.contains("first quarter")) {
            return "First Quarter";
        } else if (moonPhase.contains("waxing gibbous")) {
            return "Waxing Gibbous";
        } else if (moonPhase.contains("full moon") || moonPhase.equals("full")) {
            return "Full Moon";
        } else if (moonPhase.contains("waning gibbous")) {
            return "Waning Gibbous";
        } else if (moonPhase.contains("last quarter")) {
            return "Last Quarter";
        } else if (moonPhase.contains("waning crescent")) {
            return "Waning Crescent";
        }
        return "New Moon"; // default case
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
            Log.d(TAG, "Loaded JSON: " + json);

            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, MoonDialogFragment.MoonInfo>>(){}.getType();
            moonInfoMap = gson.fromJson(json, type);

            if (moonInfoMap != null) {
                Log.d(TAG, "Successfully loaded " + moonInfoMap.size() + " moon phase entries");
                for (Map.Entry<String, MoonDialogFragment.MoonInfo> entry : moonInfoMap.entrySet()) {
                    Log.d(TAG, "Moon Phase: " + entry.getKey() + " -> " + entry.getValue().toString());
                }
            } else {
                Log.e(TAG, "Failed to parse JSON - moonInfoMap is null");
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
        loadJSONFromAsset("moon_more_info.json");
    }
}