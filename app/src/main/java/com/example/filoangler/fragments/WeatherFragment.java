package com.example.filoangler.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filoangler.BuildConfig;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.CitiesModel;
import com.example.filoangler.Model.ProvinceModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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

    private ImageView imgWeatherToday;
    private ImageView imgWeatherOne;
    private ImageView imgWeatherTwo;
    private ImageView imgWeatherThree;
    private ImageView imgWeatherFour;
    private ImageView imgWeatherFive;
    private ImageView imgWeatherSix;

    private TextView btnWeatherMore;

    //Additional
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

    //OpenWeatherApi
    private String openWeather_API = BuildConfig.openWeatherApiKey;
    private String weatherBit_API = BuildConfig.weatherBitApiKey;

    private static final String GEOCODING_API_URL = "http://api.openweathermap.org/geo/1.0/direct";

    private AuthManager authManager;
    private LoginManager loginManager;

    private OkHttpClient client = new OkHttpClient();

    private String location;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);
        authManager = new AuthManager();
        loginManager = new LoginManager(getContext());

        loadElements(view);
        loadAutoComplete();
        getWeatherForCurrentLocation();

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

        imgWeatherToday = view.findViewById(R.id.imgWeatherToday);
        imgWeatherOne = view.findViewById(R.id.imgWeatherOne);
        imgWeatherTwo = view.findViewById(R.id.imgWeatherTwo);
        imgWeatherThree = view.findViewById(R.id.imgWeatherThree);
        imgWeatherFour = view.findViewById(R.id.imgWeatherFour);
        imgWeatherFive = view.findViewById(R.id.imgWeatherFive);
        imgWeatherSix = view.findViewById(R.id.imgWeatherSix);

        btnWeatherMore = view.findViewById(R.id.btnWeatherMore);

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

    private void loadImageFromStorage(String path, ImageView imageView) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get().load(uri.toString()).into(imageView);
        }).addOnFailureListener(e -> {
            // Handle any errors
            e.printStackTrace();
        });
    }

    private void updateUI(String currentWeatherData, String forecastData) {
        try {
            JSONObject currentJson = new JSONObject(currentWeatherData);
            JSONObject forecastJson = new JSONObject(forecastData);

            // Update current weather
            String description = currentJson.getJSONArray("weather").getJSONObject(0).getString("description");
            double temp = currentJson.getJSONObject("main").getDouble("temp");
            int humidity = currentJson.getJSONObject("main").getInt("humidity");
            double windSpeed = currentJson.getJSONObject("wind").getDouble("speed");

            txtWeatherDescription.setText(description + " in " + location);
            txtTemperature.setText(String.format("%.1f°C", temp));
            txtHumidity.setText(humidity + "%");
            txtWindSpeed.setText(String.format("%.1f m/s", windSpeed));

            updateWeatherIcon(currentJson.getJSONArray("weather").getJSONObject(0).getString("main"), imgWeatherToday);

            // Update 6-day forecast
            TextView[] forecastTexts = {txtWeatherOne, txtWeatherTwo, txtWeatherThree, txtWeatherFour, txtWeatherFive, txtWeatherSix};
            ImageView[] forecastImages = {imgWeatherOne, imgWeatherTwo, imgWeatherThree, imgWeatherFour, imgWeatherFive, imgWeatherSix};

            JSONArray forecastDays = forecastJson.getJSONArray("data");
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

            for (int i = 1; i < 7; i++) {
                JSONObject day = forecastDays.getJSONObject(i);
                String dateString = day.getString("datetime");
                Date date = inputFormat.parse(dateString);
                String formattedDate = outputFormat.format(date);

                forecastTexts[i-1].setText(formattedDate);
                updateWeatherIcon(day.getJSONObject("weather").getString("description"), forecastImages[i-1]);
            }

            // Set icons for humidity, temperature, and wind speed
            loadImageFromStorage("Weather/humidity.png", imgHumidity);
            loadImageFromStorage("Weather/thermometer.png", imgTemperature);
            loadImageFromStorage("Weather/wind.png", imgWind);

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
                        Log.e("Location", location);//LOG
                        getCoordinatesAndFetchWeather(location);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getWeatherData(double lat, double lon) {
        // OpenWeatherMap API call for current weather
        String openWeatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + openWeather_API;

        // WeatherBit API call for 7-day forecast
        String weatherBitUrl = "https://api.weatherbit.io/v2.0/forecast/daily?lat=" + lat + "&lon=" + lon + "&days=7&units=M&key=" + weatherBit_API;

        // Make OpenWeatherMap API call
        makeApiCall(openWeatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get current weather data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String currentWeatherData = response.body().string();
                    // Make WeatherBit API call after successful OpenWeatherMap call
                    makeApiCall(weatherBitUrl, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get forecast data", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String forecastData = response.body().string();
                                getActivity().runOnUiThread(() -> updateUI(currentWeatherData, forecastData));
                            } else {
                                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get forecast data", Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get current weather data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateWeatherIcon(String weatherDescription, ImageView imageView) {
        String iconName;

        weatherDescription = weatherDescription.toLowerCase();

        if (weatherDescription.contains("rain") || weatherDescription.contains("drizzle")) {
            iconName = "weather_rain";
        } else if (weatherDescription.contains("cloud")) {
            iconName = "weather_cloudy";
        } else if (weatherDescription.contains("clear") || weatherDescription.contains("sun")) {
            iconName = "weather_sunny";
        } else if (weatherDescription.contains("thunder") || weatherDescription.contains("storm")) {
            iconName = "weather_thunder";
        } else {
            // Default to cloudy if we can't determine the weather
            iconName = "weather_cloudy";
        }

        String iconPath = "Weather/" + iconName + ".png";
        loadImageFromStorage(iconPath, imageView);
    }

    private void getCoordinatesAndFetchWeather(String location) {
        // Encode the location string to handle spaces and special characters
        String encodedLocation = Uri.encode(location);
        String url = GEOCODING_API_URL + "?q=" + encodedLocation + "&limit=1&appid=" + openWeather_API;
        Log.e("Location", url);//LOG

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get location data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        if (jsonArray.length() > 0) {
                            JSONObject locationData = jsonArray.getJSONObject(0);
                            double lat = locationData.getDouble("lat");
                            double lon = locationData.getDouble("lon");
                            getWeatherData(lat, lon);
                        } else {
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing location data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get location data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}