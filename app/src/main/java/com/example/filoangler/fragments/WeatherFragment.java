package com.example.filoangler.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filoangler.BuildConfig;
import com.example.filoangler.Dialog.MoonDialogFragment;
import com.example.filoangler.Dialog.WeatherDialogFragment;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.CitiesModel;
import com.example.filoangler.Model.ProvinceModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherFragment extends Fragment {

    private AutoCompleteTextView txtSearch;

    //Weather
    private TextView txtWeatherDescription;
    private TextView txtWeatherOne;
    private TextView txtWeatherTwo;
    private TextView txtWeatherThree;
    private TextView txtWeatherFour;
    private TextView txtWeatherFive;
    private TextView txtWeatherSix;

    private ImageView imgWeatherBackground;
    private ImageView imgWeatherToday;
    private ImageView imgWeatherOne;
    private ImageView imgWeatherTwo;
    private ImageView imgWeatherThree;
    private ImageView imgWeatherFour;
    private ImageView imgWeatherFive;
    private ImageView imgWeatherSix;

    private TextView btnWeatherMore;

    //Additional
    private TextView btnMiscLearnMore;
    private TextView txtWindSpeed;
    private TextView txtTemperature;
    private TextView txtHumidity;

    private ImageView imgWind;
    private ImageView imgTemperature;
    private ImageView imgHumidity;

    //Moon
    private TextView txtMoonDescription;
    private TextView txtMoonOne;
    private TextView txtMoonTwo;
    private TextView txtMoonThree;
    private TextView txtMoonFour;
    private TextView txtMoonFive;
    private TextView txtMoonSix;

    private ImageView imgMoonToday;
    private ImageView imgMoonOne;
    private ImageView imgMoonTwo;
    private ImageView imgMoonThree;
    private ImageView imgMoonFour;
    private ImageView imgMoonFive;
    private ImageView imgMoonSix;

    private TextView btnMoonMore;

    //VisualCrossing
    private String visualCrossing_API = BuildConfig.visualCrossingApiKey;
    private static final String VISUAL_CROSSING_API_URL = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/";

    private AuthManager authManager;
    private LoginManager loginManager;

    private OkHttpClient client = new OkHttpClient();

    private String location;
    private String weatherDescription;
    private int currentWeatherIcon;
    private String moonDescription;
    private int currentMoonIcon;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        authManager = new AuthManager();
        loginManager = new LoginManager(getContext());

        loadElements(view);
        loadAutoComplete();
        getWeatherForCurrentLocation();

        txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || event != null
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {

                    location = txtSearch.getText().toString();
                    fetchWeather(location);

                    return true;
                }
                return false;
            }
        });

        btnWeatherMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeatherMoreDialog();
            }
        });

        btnMoonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoonMoreDialog();
            }
        });

        btnMiscLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMiscInfoMoreDialog();
            }
        });


        return view;
    }

    private void makeApiCall(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    public void loadElements(View view){
        txtSearch = view.findViewById(R.id.txtSearch);

        txtWeatherDescription = view.findViewById(R.id.txtWeatherDescription);
        txtWeatherOne = view.findViewById(R.id.txtWeatherOne);
        txtWeatherTwo = view.findViewById(R.id.txtWeatherTwo);
        txtWeatherThree = view.findViewById(R.id.txtWeatherThree);
        txtWeatherFour = view.findViewById(R.id.txtWeatherFour);
        txtWeatherFive = view.findViewById(R.id.txtWeatherFive);
        txtWeatherSix = view.findViewById(R.id.txtWeatherSix);

        imgWeatherBackground = view.findViewById(R.id.imgWeatherBackground);
        imgWeatherToday = view.findViewById(R.id.imgWeatherToday);
        imgWeatherOne = view.findViewById(R.id.imgWeatherOne);
        imgWeatherTwo = view.findViewById(R.id.imgWeatherTwo);
        imgWeatherThree = view.findViewById(R.id.imgWeatherThree);
        imgWeatherFour = view.findViewById(R.id.imgWeatherFour);
        imgWeatherFive = view.findViewById(R.id.imgWeatherFive);
        imgWeatherSix = view.findViewById(R.id.imgWeatherSix);

        btnWeatherMore = view.findViewById(R.id.btnWeatherMore);

        btnMiscLearnMore = view.findViewById(R.id.btnMiscLearnMore);
        txtWindSpeed = view.findViewById(R.id.txtWindSpeed);
        txtTemperature = view.findViewById(R.id.txtTemperature);
        txtHumidity = view.findViewById(R.id.txtHumidity);

        imgWind = view.findViewById(R.id.imgWind);
        imgTemperature = view.findViewById(R.id.imgTemperature);
        imgHumidity = view.findViewById(R.id.imgHumidity);

        txtMoonDescription = view.findViewById(R.id.txtMoonDescription);
        txtMoonOne = view.findViewById(R.id.txtMoonOne);
        txtMoonTwo = view.findViewById(R.id.txtMoonTwo);
        txtMoonThree = view.findViewById(R.id.txtMoonThree);
        txtMoonFour = view.findViewById(R.id.txtMoonFour);
        txtMoonFive = view.findViewById(R.id.txtMoonFive);
        txtMoonSix = view.findViewById(R.id.txtMoonSix);

        imgMoonToday = view.findViewById(R.id.imgMoonToday);
        imgMoonOne = view.findViewById(R.id.imgMoonOne);
        imgMoonTwo = view.findViewById(R.id.imgMoonTwo);
        imgMoonThree = view.findViewById(R.id.imgMoonThree);
        imgMoonFour = view.findViewById(R.id.imgMoonFour);
        imgMoonFive = view.findViewById(R.id.imgMoonFive);
        imgMoonSix = view.findViewById(R.id.imgMoonSix);

        btnMoonMore = view.findViewById(R.id.btnMoonMore);
    }

    public void loadAutoComplete(){
        String citiesJson = Utils.loadJSONFromAsset(getContext(), "cities.json");
        String provincesJson = Utils.loadJSONFromAsset(getContext(), "provinces.json");

        Gson gson = new Gson();
        Type cityListType = new TypeToken<List<CitiesModel>>(){}.getType();
        Type provinceListType = new TypeToken<List<ProvinceModel>>(){}.getType();

        List<CitiesModel> cityList = gson.fromJson(citiesJson, cityListType);
        List<ProvinceModel> provinceList = gson.fromJson(provincesJson, provinceListType);

        Map<String, String> provinceMap = new HashMap<>();
        for (ProvinceModel province : provinceList) {
            provinceMap.put(province.getKey(), province.getName());
        }

        List<String> cityProvinceNames = new ArrayList<>();
        for (CitiesModel city : cityList) {
            String provinceName = provinceMap.get(city.getProvince());
            String fullName = city.getName() + ", " + provinceName + ", Philippines";
            cityProvinceNames.add(fullName);
        }

        setAutoComplete(cityProvinceNames);
    }

    public void setAutoComplete(List<String> cityNames){

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cityNames);

        txtSearch.setAdapter(adapter);

        txtSearch.setThreshold(1);

    }

    public void loadImageFromStorage(int drawableId, ImageView imageView) {
        handler.post(() ->{
            Picasso.get().load(drawableId)
                    .resize(imageView.getWidth(), imageView.getHeight())
                    .into(imageView);
        });
    }

    private void updateUI(String weatherData) {
        try {
            JSONObject json = new JSONObject(weatherData);
            JSONArray days = json.getJSONArray("days");
            JSONObject currentConditions = json.getJSONObject("currentConditions");

            // Update current weather
            weatherDescription = currentConditions.getString("conditions");
            double temp = currentConditions.getDouble("temp");
            int humidity = currentConditions.getInt("humidity");
            double windSpeed = currentConditions.getDouble("windspeed");

            txtWeatherDescription.setText(weatherDescription + " in " + location);
            txtTemperature.setText(String.format("%.1f°C", temp));
            txtHumidity.setText(humidity + "%");
            txtWindSpeed.setText(String.format("%.1f km/h", windSpeed));

            currentWeatherIcon = getWeatherIconResource(weatherDescription);
            updateWeatherIcon(weatherDescription, imgWeatherToday);
            Picasso.get().load(getWeatherBackgroundResource(weatherDescription)).into(imgWeatherBackground);

            // Update 6-day forecast
            TextView[] forecastTexts = {txtWeatherOne, txtWeatherTwo, txtWeatherThree, txtWeatherFour, txtWeatherFive, txtWeatherSix};
            ImageView[] forecastImages = {imgWeatherOne, imgWeatherTwo, imgWeatherThree, imgWeatherFour, imgWeatherFive, imgWeatherSix};

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

            for (int i = 1; i < 7; i++) {
                JSONObject day = days.getJSONObject(i);
                String dateString = day.getString("datetime");
                Date date = inputFormat.parse(dateString);
                String formattedDate = outputFormat.format(date);

                forecastTexts[i-1].setText(formattedDate);
                updateWeatherIcon(day.getString("conditions"), forecastImages[i-1]);
                Log.e("Weather", "Weather count: " + i);
            }

            // Set icons for humidity, temperature, and wind speed
            loadImageFromStorage(R.drawable.weather_humidity, imgHumidity);
            loadImageFromStorage(R.drawable.weather_thermometer, imgTemperature);
            loadImageFromStorage(R.drawable.weather_wind, imgWind);

            // Update moon phase information
            updateMoonPhase(days);

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void getWeatherForCurrentLocation() {
        authManager.GetDb().getReference().child("Users")
                .child(loginManager.GetCurrentUser().getUid())
                .child("Personal Information")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String province = snapshot.child("ProvinceAddress").getValue(String.class);
                        String city = snapshot.child("CityAddress").getValue(String.class);

                        location = city + ", " + province + ", Philippines";
                        Log.e("Location", location);
                        fetchWeather(location);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get user location", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void fetchWeather(String location) {
        String encodedLocation = Uri.encode(location);
        String url = VISUAL_CROSSING_API_URL + encodedLocation + "?unitGroup=metric&key=" + visualCrossing_API + "&contentType=json";

        makeApiCall(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get weather data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String weatherData = response.body().string();
                    getActivity().runOnUiThread(() -> updateUI(weatherData));
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get weather data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private int getWeatherIconResource(String weatherDescription) {
        weatherDescription = weatherDescription.toLowerCase();

        if (weatherDescription.contains("rain") || weatherDescription.contains("drizzle")) {
            return R.drawable.weather_rain;
        } else if (weatherDescription.contains("cloud")) {
            return R.drawable.weather_cloudy;
        } else if (weatherDescription.contains("clear") || weatherDescription.contains("sun")) {
            return R.drawable.weather_sunny;
        } else if (weatherDescription.contains("thunder") || weatherDescription.contains("storm")) {
            return R.drawable.weather_thunder;
        } else {
            return R.drawable.weather_cloudy;
        }
    }

    private int getWeatherBackgroundResource(String weatherDescription) {
        weatherDescription = weatherDescription.toLowerCase();

        if (weatherDescription.contains("rain") || weatherDescription.contains("drizzle")) {
            return R.drawable.bg_rain;
        } else if (weatherDescription.contains("cloud")) {
            return R.drawable.bg_cloudy;
        } else if (weatherDescription.contains("clear") || weatherDescription.contains("sun")) {
            return R.drawable.bg_sunny;
        } else if (weatherDescription.contains("thunder") || weatherDescription.contains("storm")) {
            return R.drawable.bg_thunder;
        } else {
            return R.drawable.bg_cloudy; // default background
        }
    }

    private void updateWeatherIcon(String weatherDescription, ImageView imageView) {
        int iconName = getWeatherIconResource(weatherDescription);
        loadImageFromStorage(iconName, imageView);
    }

    private void updateMoonPhase(JSONArray days) throws JSONException {
        TextView[] moonTexts = {txtMoonOne, txtMoonTwo, txtMoonThree, txtMoonFour, txtMoonFive, txtMoonSix};
        ImageView[] moonImages = {imgMoonToday, imgMoonOne, imgMoonTwo, imgMoonThree, imgMoonFour, imgMoonFive, imgMoonSix};

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

        double currentMoonPhase = days.getJSONObject(0).getDouble("moonphase");
        String moonPhaseDescription = getMoonPhaseDescription(currentMoonPhase);
        txtMoonDescription.setText(moonPhaseDescription + " will be seen tonight in the Philippines");
        updateMoonIcon(currentMoonPhase, imgMoonToday);

        moonDescription = txtMoonDescription.getText().toString();
        currentMoonIcon = getMoonIconResource(currentMoonPhase);


        for (int i = 0; i < moonTexts.length; i++) {
            JSONObject day = days.getJSONObject(i + 1);
            String dateString = day.getString("datetime");
            try {
                Date date = inputFormat.parse(dateString);
                String formattedDate = outputFormat.format(date);
                moonTexts[i].setText(formattedDate);

                double moonPhase = day.getDouble("moonphase");
                updateMoonIcon(moonPhase, moonImages[i + 1]);
                Log.e("Weather", "Moon count: " + (i + 1));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private String getMoonPhaseDescription(double moonPhase) {
        if (moonPhase < 0.0625) return "New Moon";
        else if (moonPhase < 0.1875) return "Waxing Crescent Moon";
        else if (moonPhase < 0.3125) return "First Quarter Moon";
        else if (moonPhase < 0.4375) return "Waxing Gibbous Moon";
        else if (moonPhase < 0.5625) return "Full Moon";
        else if (moonPhase < 0.6875) return "Waning Gibbous Moon";
        else if (moonPhase < 0.8125) return "Last Quarter Moon";
        else if (moonPhase < 0.9375) return "Waning Crescent Moon";
        else return "New Moon";
    }

    private int getMoonIconResource(double moonPhase) {
        if (moonPhase < 0.0625) return R.drawable.moon_phase_1;        // New Moon
        else if (moonPhase < 0.1875) return R.drawable.moon_phase_2;   // Waxing Crescent
        else if (moonPhase < 0.3125) return R.drawable.moon_phase_3;   // First Quarter
        else if (moonPhase < 0.4375) return R.drawable.moon_phase_4;   // Waxing Gibbous
        else if (moonPhase < 0.5625) return R.drawable.moon_phase_5;   // Full Moon
        else if (moonPhase < 0.6875) return R.drawable.moon_phase_6;   // Waning Gibbous
        else if (moonPhase < 0.8125) return R.drawable.moon_phase_7;   // Last Quarter
        else if (moonPhase < 0.9375) return R.drawable.moon_phase_8;   // Waning Crescent
        else return R.drawable.moon_phase_1;                           // Back to New Moon
    }

    private void updateMoonIcon(double moonPhase, ImageView imageView) {
        int iconResource = getMoonIconResource(moonPhase);
        loadImageFromStorage(iconResource, imageView);
    }

    private void showWeatherMoreDialog() {

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_weather_dialog);

        if(txtWeatherDescription != null) {
            WeatherDialogFragment weatherDialogFragment = new WeatherDialogFragment(
                    getContext(),
                    txtWeatherDescription.getText().toString(),
                    currentWeatherIcon,
                    getWeatherBackgroundResource(weatherDescription)
            );
            weatherDialogFragment.getDialog(dialog);
        } else {
            return;
        }

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void showMoonMoreDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_weather_dialog);

        if(txtMoonDescription != null) {
            MoonDialogFragment moonDialogFragment = new MoonDialogFragment(
                    getContext(),
                    moonDescription,
                    currentMoonIcon
            );
            moonDialogFragment.getDialog(dialog);
        } else {
            return;
        }

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void showMiscInfoMoreDialog(){

    }

}