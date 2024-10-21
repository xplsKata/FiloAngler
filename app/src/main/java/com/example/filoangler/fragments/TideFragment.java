package com.example.filoangler.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filoangler.Adapter.TidesAdapter;
import com.example.filoangler.BuildConfig;
import com.example.filoangler.Model.CitiesModel;
import com.example.filoangler.Model.ProvinceModel;
import com.example.filoangler.Model.TidesModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.example.filoangler.WaveView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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

    private Button btnMore;

    private RecyclerView recyclerView;

    private int minHeight;
    private int maxHeight;

    private OkHttpClient client = new OkHttpClient();

    private String worldWeatherOnline_API = BuildConfig.worldWeatherOnlineApiKey;
    private String worldWeatherOnline_URL = "https://api.worldweatheronline.com/premium/v1/marine.ashx?";

    private String openWeather_API = BuildConfig.openWeatherApiKey;
    private static final String GEOCODING_API_URL = "http://api.openweathermap.org/geo/1.0/direct";

    private List<JSONObject> sevenDayForecast;
    private List<String> cityProvinceNames;
    private Gson gson;

    private TidesAdapter tidesAdapter;
    private List<TidesModel> tidesList;

    private String location;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tide, container, false);
        gson = new Gson();

        loadElements(view);
        loadAutoComplete();

        txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || event != null
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {

                    location = txtSearch.getText().toString();
                    if(location.isEmpty()){
                        Toast.makeText(getContext(), "Please enter a location", Toast.LENGTH_LONG).show();
                    }else if(!cityProvinceNames.contains(location)){
                        Toast.makeText(getContext(), "Invalid location", Toast.LENGTH_LONG).show();
                    }else{
                        getCoordinatesAndFetchTide(location);
                    }
                    return true;
                }
                return false;
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

    private void makeApiCall(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    private void loadElements(View view){
        waveView = view.findViewById(R.id.waveView);
        water_container = view.findViewById(R.id.water_container);

        txtSearch = view.findViewById(R.id.txtSearch);

        txtHighestTide = view.findViewById(R.id.txtHighestTide);
        txtHighestTideTime = view.findViewById(R.id.txtHighestTideTime);
        txtLowestTide = view.findViewById(R.id.txtLowestTide);
        txtLowestTideTime = view.findViewById(R.id.txtLowestTideTime);
        txtCurrentTide = view.findViewById(R.id.txtCurrentTide);
        txtLocation = view.findViewById(R.id.txtLocation);

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
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONObject data = jsonObject.getJSONObject("data");
                        updateUIWithTideData(data);
                        storeSevenDayForecast(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing tide data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to get tide data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateUIWithTideData(JSONObject data) throws JSONException {
        JSONArray tideArray = data.getJSONArray("weather").getJSONObject(0).getJSONArray("tides").getJSONObject(0).getJSONArray("tide_data");

        double highestTide = Double.MIN_VALUE;
        double lowestTide = Double.MAX_VALUE;
        String highestTideTime = "";
        String lowestTideTime = "";

        for (int i = 0; i < tideArray.length(); i++) {
            JSONObject tideData = tideArray.getJSONObject(i);
            double tideHeight = tideData.getDouble("tideHeight_mt");
            String tideTime = tideData.getString("tideTime");

            if (tideHeight > highestTide) {
                highestTide = tideHeight;
                highestTideTime = tideTime;
            }
            if (tideHeight < lowestTide) {
                lowestTide = tideHeight;
                lowestTideTime = tideTime;
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
            txtCurrentTide.setText(String.format(Locale.US, "%.2f m", getCurrentTideHeight(tideArray)));
            txtLocation.setText(location);

            updateWaveViewHeight(calculateWaterLevel(finalHighestTide, finalLowestTide, getCurrentTideHeight(tideArray)));
        });
    }

    private double getCurrentTideHeight(JSONArray tideArray) {
        try {
            return tideArray.getJSONObject(0).getDouble("tideHeight_mt");
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private float calculateWaterLevel(double highest, double lowest, double current) {
        return (float) ((current - lowest) / (highest - lowest));
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
        // Encode the location string to handle spaces and special characters
        String encodedLocation = Uri.encode(location);
        String url = GEOCODING_API_URL + "?q=" + encodedLocation + "&limit=1&appid=" + openWeather_API;
        Log.e("Location", url);//LOG

        makeApiCall(url,new Callback() {
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
                            getTideData(lat, lon);
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

    public List<JSONObject> getSevenDayForecast() {
        return sevenDayForecast;
    }

}