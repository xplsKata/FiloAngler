package com.example.filoangler.fragments;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filoangler.Adapter.TidesAdapter;
import com.example.filoangler.BuildConfig;
import com.example.filoangler.Dialog.TideDetailsDialog;
import com.example.filoangler.Model.CitiesModel;
import com.example.filoangler.Model.ProvinceModel;
import com.example.filoangler.Model.TidesModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.example.filoangler.WaveView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TideFragment extends Fragment {

    private WaveView waveView;
    private View water_container;

    private AutoCompleteTextView txtSearch;

    private TextView txtHighestTide;
    private TextView txtHighestTideTime;
    private TextView txtLowestTide;
    private TextView txtLowestTideTime;
    private TextView txtCurrentTide;
    private TextView txtLocation;
    private TextView txtDescription;

    private Button btnMore;

    private RecyclerView recyclerView;

    private int minHeight;
    private int maxHeight;

    private FrameLayout overlayContainer;
    private boolean isDataLoaded = false;

    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    private OkHttpClient client;

    private String worldWeatherOnline_API = BuildConfig.worldWeatherOnlineApiKey;
    private String worldWeatherOnline_URL = "https://api.worldweatheronline.com/premium/v1/marine.ashx?";

    private String openWeather_API = BuildConfig.openWeatherApiKey;
    private static final String GEOCODING_API_URL = "https://api.openweathermap.org/geo/1.0/direct";

    private List<JSONObject> sevenDayForecast;
    private List<String> cityProvinceNames;
    private Gson gson;

    private TidesAdapter tidesAdapter;
    private List<TidesModel> tidesList;

    private String location;
    private double highTideHeight;
    private double lowTideHeight;
    private double currentHeight;

    private int originalEndMargin;
    private ValueAnimator searchBarAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tide, container, false);
        gson = new Gson();

        client = createOkHttpClient();

        loadElements(view);
        loadAutoComplete();
        hideText();

        txtSearch.clearFocus();
        txtSearch.setFocusable(false);
        txtSearch.setFocusableInTouchMode(false);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) txtSearch.getLayoutParams();
        originalEndMargin = params.getMarginEnd();

        searchBarAnimator = new ValueAnimator();
        searchBarAnimator.setDuration(300);
        searchBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) txtSearch.getLayoutParams();
                params.setMarginEnd((Integer) animation.getAnimatedValue());
                txtSearch.setLayoutParams(params);
            }
        });

        txtSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                txtSearch.setFocusable(true);
                txtSearch.setFocusableInTouchMode(true);
                return false;
            }
        });

        txtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    expandSearchBar();
                } else {
                    collapseSearchBar();
                }
            }
        });

        txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_NEXT ||
                        (event != null &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    // Prevent default behavior
                    if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
                        return true;
                    }

                    // Process the search
                    location = txtSearch.getText().toString();
                    if(location.isEmpty()){
                        Toast.makeText(getContext(), "Please enter a location", Toast.LENGTH_LONG).show();
                    }else{
                        getCoordinatesAndFetchTide(location);
                        Utils.hideKeyboard(getActivity());
                        txtSearch.clearFocus();
                        txtSearch.setFocusable(false);
                        txtSearch.setFocusableInTouchMode(false);
                    }
                    return true;
                }
                return false;
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDataLoaded) {
                    showDialog();
                } else {
                    Toast.makeText(getContext(), "Please search for a location first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Convert dp to pixels
        float density = getResources().getDisplayMetrics().density;
        minHeight = (int) (50 * density);

        // Wait for the parent container to be laid out
        water_container.post(new Runnable() {
            @Override
            public void run() {
                maxHeight = water_container.getHeight() - (int) (30 * density);
                updateWaveViewHeight(getCurrentWaterLevel());
            }
        });

    }

    private void expandSearchBar() {
        if (searchBarAnimator.isRunning()) {
            searchBarAnimator.cancel();
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) txtSearch.getLayoutParams();

        // Animate from current margin to 0
        searchBarAnimator.setIntValues(
                params.getMarginEnd(),
                150
        );
        searchBarAnimator.start();
    }

    private void collapseSearchBar() {
        if (searchBarAnimator.isRunning()) {
            searchBarAnimator.cancel();
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) txtSearch.getLayoutParams();

        // Animate back to the original end margin
        searchBarAnimator.setIntValues(
                params.getMarginEnd(),
                originalEndMargin
        );
        searchBarAnimator.start();
    }

    private void makeApiCall(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .build();

        try {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("TideFragment", "API Call Failed", e);
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Network error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                    callback.onFailure(call, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        // Explicitly check for null response body
                        if (response.body() == null) {
                            Log.w("TideFragment", "Response body was null");
                            return;
                        }

                        callback.onResponse(call, response);
                    } catch (IllegalStateException e) {
                        // Specifically handle the "closed" error
                        if ("closed".equals(e.getMessage())) {
                            Log.w("TideFragment", "Response processing completed despite closed body", e);
                            // Since the data was already processed, we can silently handle this
                            return;
                        }

                        // For other exceptions, log and show a toast
                        Log.e("TideFragment", "Response Processing Error", e);
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                    "Error processing response: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        Log.e("TideFragment", "Unexpected Response Processing Error", e);
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(),
                                    "Unexpected error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    } finally {
                        // Ensure the response is always closed
                        if (response.body() != null) {
                            response.close();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e("TideFragment", "API Call Setup Error", e);
        }
    }

    private void loadElements(View view){
        overlayContainer = view.findViewById(R.id.overlay_container);
        waveView = view.findViewById(R.id.waveView);
        water_container = view.findViewById(R.id.water_container);

        txtSearch = view.findViewById(R.id.txtSearch);

        txtHighestTide = view.findViewById(R.id.txtHighestTide);
        txtHighestTideTime = view.findViewById(R.id.txtHighestTideTime);
        txtLowestTide = view.findViewById(R.id.txtLowestTide);
        txtLowestTideTime = view.findViewById(R.id.txtLowestTideTime);
        txtCurrentTide = view.findViewById(R.id.txtCurrentTide);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtDescription = view.findViewById(R.id.txtDescription);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tidesList = new ArrayList<>();
        tidesAdapter = new TidesAdapter(getContext(), tidesList);
        recyclerView.setAdapter(tidesAdapter);

        btnMore = view.findViewById(R.id.btnMore);
    }

    public void loadAutoComplete(){
        String citiesJson = Utils.loadJSONFromAsset(getContext(), "coastal_cities.json");
        String provincesJson = Utils.loadJSONFromAsset(getContext(), "provinces.json");

        gson = new Gson();
        Type cityListType = new TypeToken<List<CitiesModel>>(){}.getType();
        Type provinceListType = new TypeToken<List<ProvinceModel>>(){}.getType();

        List<CitiesModel> cityList = gson.fromJson(citiesJson, cityListType);
        List<ProvinceModel> provinceList = gson.fromJson(provincesJson, provinceListType);

        Map<String, String> provinceMap = new HashMap<>();
        for (ProvinceModel province : provinceList) {
            provinceMap.put(province.getKey(), province.getName());
        }

        cityProvinceNames = new ArrayList<>();
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

    private void updateWaveViewHeight(float waterLevel) {
        waterLevel = Math.max(0, Math.min(1, waterLevel));

        int newHeight = (int) (minHeight + (maxHeight - minHeight) * waterLevel);

        ViewGroup.LayoutParams layoutParams = waveView.getLayoutParams();
        layoutParams.height = newHeight;
        waveView.setLayoutParams(layoutParams);

        waveView.setAmplitude(newHeight * 0.2f); // Adjust amplitude based on new height
        waveView.invalidate();
    }

    private float getCurrentWaterLevel() {
        float waterLevel = 0.5f;

        return waterLevel;
    }

    private void getTideData(double lat, double lon) {
        String url = worldWeatherOnline_URL + "key=" + worldWeatherOnline_API + "&q=" + lat + "," + lon + "&format=json&tide=yes";
        Log.e("TideData", "Tide Url: " + url);

        makeApiCall(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get tide data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        // Use responseBody.string() only once
                        String jsonData = response.body().string();

                        try {
                            JSONObject jsonObject = new JSONObject(jsonData);
                            JSONObject data = jsonObject.getJSONObject("data");
                            updateUIWithTideData(data);
                            storeSevenDayForecast(data);
                        } catch (JSONException e) {
                            Log.e("TideFragment", "JSON Parsing Error", e);
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing tide data", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        // Log the error response
                        String errorBody = response.body() != null ? response.body().string() : "No error body";
                        Log.e("TideFragment", "Error Response: " + errorBody);

                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get tide data", Toast.LENGTH_SHORT).show());
                    }
                } finally {
                    // Ensure the response is always closed
                    if (response.body() != null) {
                        response.close();
                    }
                }
            }
        });
    }

    private void updateUIWithTideData(JSONObject data) throws JSONException {
        JSONArray weatherArray = data.getJSONArray("weather");
        if (weatherArray.length() > 0) {
            JSONObject todayWeather = weatherArray.getJSONObject(0);
            JSONArray tidesArray = todayWeather.getJSONArray("tides");
            if (tidesArray.length() > 0) {
                JSONArray tideDataArray = tidesArray.getJSONObject(0).getJSONArray("tide_data");

                double highestTide = Double.MIN_VALUE;
                double lowestTide = Double.MAX_VALUE;
                String highestTideTime = "";
                String lowestTideTime = "";

                tidesList.clear(); // Clear existing tide data

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

                for (int i = 0; i < tideDataArray.length(); i++) {
                    JSONObject tideData = tideDataArray.getJSONObject(i);
                    double tideHeight = tideData.getDouble("tideHeight_mt");
                    String tideTime = tideData.getString("tideTime");
                    String tideDateTime = tideData.getString("tideDateTime");
                    String tideType = tideData.getString("tide_type");

                    if (tideHeight > highestTide) {
                        highestTide = tideHeight;
                        highestTideTime = tideTime;
                    }
                    if (tideHeight < lowestTide) {
                        lowestTide = tideHeight;
                        lowestTideTime = tideTime;
                    }

                    // Create TidesModel object and add to list
                    try {
                        Date tideDate = inputFormat.parse(tideDateTime);
                        TidesModel tideModel = new TidesModel(tideTime, tideHeight, tideDate, tideType);
                        tidesList.add(tideModel);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                final double finalHighestTide = highestTide;
                final double finalLowestTide = lowestTide;
                final String finalHighestTideTime = highestTideTime;
                final String finalLowestTideTime = lowestTideTime;

                getActivity().runOnUiThread(() -> {
                    txtHighestTide.setText(String.format(Locale.US, "%.2f m", finalHighestTide));
                    txtHighestTideTime.setText(finalHighestTideTime);
                    txtLowestTide.setText(String.format(Locale.US, "%.2f m", finalLowestTide));
                    txtLowestTideTime.setText(finalLowestTideTime);
                    txtCurrentTide.setText(String.format(Locale.US, "%.2f m", getCurrentTideHeight(tideDataArray)));
                    txtLocation.setText(location);
                    txtDescription.setText("The current tide level in " + location + " is "
                            + String.format(Locale.US, "%.2f m", getCurrentTideHeight(tideDataArray)));

                    showText();
                    updateWaveViewHeight(calculateWaterLevel(finalHighestTide, finalLowestTide, getCurrentTideHeight(tideDataArray)));

                    isDataLoaded = true;
                    if (overlayContainer != null) {
                        overlayContainer.setVisibility(View.GONE);
                    }

                    // Update RecyclerView
                    tidesAdapter.notifyDataSetChanged();
                });
            }
        }
    }

    private double getCurrentTideHeight(JSONArray tideDataArray) {
        try {
            // Parse the tide times and heights
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
            Date currentTime = new Date();
            Date highTideTime = null;
            Date lowTideTime = null;
            highTideHeight = 0;
            lowTideHeight = 0;

            // Find high and low tide points
            for (int i = 0; i < tideDataArray.length(); i++) {
                JSONObject tideData = tideDataArray.getJSONObject(i);
                String tideType = tideData.getString("tide_type");
                double height = tideData.getDouble("tideHeight_mt");
                Date tideTime = timeFormat.parse(tideData.getString("tideTime"));

                if (tideType.equalsIgnoreCase("HIGH")) {
                    highTideTime = tideTime;
                    highTideHeight = height;
                } else if (tideType.equalsIgnoreCase("LOW")) {
                    lowTideTime = tideTime;
                    lowTideHeight = height;
                }
            }

            if (highTideTime == null || lowTideTime == null) {
                Log.e("TideCalculation", "Missing high or low tide data");
                return 0;
            }

            // Convert all times to minutes since midnight for easier calculation
            int currentMinutes = timeToMinutes(currentTime);
            int highTideMinutes = timeToMinutes(highTideTime);
            int lowTideMinutes = timeToMinutes(lowTideTime);

            // Calculate the period (in minutes) between high and low tide
            int period;
            if (highTideMinutes > lowTideMinutes) {
                period = 2 * Math.abs(highTideMinutes - lowTideMinutes);
            } else {
                period = 2 * Math.abs(lowTideMinutes - highTideMinutes);
            }

            // Calculate phase shift based on whether high or low tide comes first
            double phaseShift = (highTideMinutes < lowTideMinutes) ? Math.PI : 0;

            // Calculate where we are in the cycle
            double angle = (2 * Math.PI * (currentMinutes - Math.min(highTideMinutes, lowTideMinutes))) / period;
            angle += phaseShift;

            // Calculate the current height using sinusoidal interpolation
            double heightDifference = highTideHeight - lowTideHeight;
            double middleHeight = (highTideHeight + lowTideHeight) / 2;
            currentHeight = middleHeight + (heightDifference / 2) * Math.cos(angle);

            // Log the calculation details
            Log.d("TideCalculation", String.format(Locale.US,
                    "Current time: %02d:%02d\n" +
                            "High tide: %02d:%02d (%.2fm)\n" +
                            "Low tide: %02d:%02d (%.2fm)\n" +
                            "Calculated height: %.2fm",
                    currentMinutes / 60, currentMinutes % 60,
                    highTideMinutes / 60, highTideMinutes % 60, highTideHeight,
                    lowTideMinutes / 60, lowTideMinutes % 60, lowTideHeight,
                    currentHeight
            ));

            return currentHeight;

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private float calculateWaterLevel(double highest, double lowest, double current) {
        double range = highest - lowest;
        double position = current - lowest;
        float level = (float) (position / range);

        Log.d("WaterLevel", String.format(Locale.US,
                "Water Level Calculation:\n" +
                        "Highest: %.2fm\n" +
                        "Lowest: %.2fm\n" +
                        "Current: %.2fm\n" +
                        "Calculated Level: %.2f",
                highest, lowest, current, level
        ));

        return level;
    }

    private void storeSevenDayForecast(JSONObject data) throws JSONException {
        JSONArray weatherArray = data.getJSONArray("weather");
        sevenDayForecast = new ArrayList<>();

        for (int i = 0; i < weatherArray.length(); i++) {
            sevenDayForecast.add(weatherArray.getJSONObject(i));
            Log.e("TideData", "Tide Day: " + weatherArray.getJSONObject(i));
        }
    }

    private void getCoordinatesAndFetchTide(String location) {
        String encodedLocation = Uri.encode(location);
        String url = GEOCODING_API_URL + "?q=" + encodedLocation + "&limit=1&appid=" + openWeather_API;

        // Add more detailed logging
        Log.e("LocationAPI", "Full URL: " + url);
        Log.e("LocationAPI", "API Key: " + openWeather_API); // Log the API key (be careful in production)

        try{
            makeApiCall(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Log the full exception details
                    Log.e("LocationAPI", "API Call Failure", e);
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Failed to get location data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                    handleLocationSearchFailure();
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
                                getTideData(lat, lon);
                            } else {
                                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing location data", Toast.LENGTH_SHORT).show());
                            handleLocationSearchFailure();
                        }
                    } else {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get location data", Toast.LENGTH_SHORT).show());
                        handleLocationSearchFailure();
                    }
                    String responseBody = response.body().string();
                    Log.e("LocationAPI", "Response Code: " + response.code());
                    Log.e("LocationAPI", "Response Body: " + responseBody);
                }
            });
        }catch (Exception e){
            Log.e("LocationAPI", "Error: " + e.getMessage());
        }
    }

    private void handleLocationSearchFailure() {
        getActivity().runOnUiThread(() -> {
            // Ensure overlay is visible if data load fails
            if (overlayContainer != null) {
                overlayContainer.setVisibility(View.VISIBLE);
            }
            isDataLoaded = false;
        });
    }

    private void hideText(){
        txtHighestTide.setVisibility(View.GONE);
        txtHighestTideTime.setVisibility(View.GONE);
        txtLowestTide.setVisibility(View.GONE);
        txtLowestTideTime.setVisibility(View.GONE);
        txtDescription.setVisibility(View.INVISIBLE);
    }

    private void showText(){
        txtHighestTide.setVisibility(View.VISIBLE);
        txtHighestTideTime.setVisibility(View.VISIBLE);
        txtLowestTide.setVisibility(View.VISIBLE);
        txtLowestTideTime.setVisibility(View.VISIBLE);
        txtLocation.setVisibility(View.VISIBLE);
        txtDescription.setVisibility(View.VISIBLE);

    }

    private int timeToMinutes(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }

    private void showDialog(){
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_weather_misc_dialog);

        if(txtDescription != null) {
            TideDetailsDialog tideDetailsDialog = new TideDetailsDialog(
                    txtDescription.getText().toString(),
                    highTideHeight,
                    lowTideHeight,
                    currentHeight
                    );
            tideDetailsDialog.getDialog(dialog);
        } else {
            return;
        }

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

}