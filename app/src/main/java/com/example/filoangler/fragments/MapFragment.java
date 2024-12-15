package com.example.filoangler.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.filoangler.Dialog.MapDetailsDialog;
import com.example.filoangler.LocationMonitoringService;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Model.MapLocationsModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private LatLngBounds philippinesBounds;
    private AuthManager authManager;
    private HashMap<Marker, String> markerLocationId;
    private HashMap<Marker, Integer> markerVisitCounts;


    private Intent serviceIntent;

    private MediaPlayer alertSound;
    private boolean isPlayingAlert = false;
    private Timer alertTimer;
    private TimerTask alertTask;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float GEOFENCE_RADIUS = 300; // meters

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isLocationEnabled = true; // Default value
    private SharedPreferences preferences;
    private HashMap<String, Circle> restrictedAreaCircles;

    private AlertDialog currentAlertDialog;
    private boolean isAlertDialogShowing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        authManager = new AuthManager();
        markerLocationId = new HashMap<>();
        markerVisitCounts = new HashMap<>();

        // Initialize preferences
        preferences = requireActivity().getSharedPreferences("MapSettings", Context.MODE_PRIVATE);
        isLocationEnabled = preferences.getBoolean("location_alerts_enabled", true);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        restrictedAreaCircles = new HashMap<>();

        // Initialize alert sound
        alertSound = MediaPlayer.create(requireContext(), R.raw.alert_sound);

        // Setup location callback
        setupLocationCallback();

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Enable options menu
        setHasOptionsMenu(true);

        checkLocationPermission();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationMonitoring();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        MenuItem toggleItem = menu.findItem(R.id.toggle_location_alerts);
        toggleItem.setTitle(isLocationEnabled ? "Disable Alerts" : "Enable Alerts");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.toggle_location_alerts) {
            isLocationEnabled = !isLocationEnabled;
            preferences.edit().putBoolean("location_alerts_enabled", isLocationEnabled).apply();
            item.setTitle(isLocationEnabled ? "Disable Alerts" : "Enable Alerts");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request only location permissions
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            checkGPSEnabled();
        }
    }

    private void checkGPSEnabled() {
        LocationManager locationManager = (LocationManager) requireContext()
                .getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("GPS Required")
                    .setMessage("Please enable GPS to use location features")
                    .setPositiveButton("Settings", (dialog, which) -> {
                        // Open location settings directly
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            startLocationUpdates();
        }
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || !isLocationEnabled) {
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    checkProximityToRestrictedAreas(location);
                }
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000); // Update every 5 seconds

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null);
    }

    private void startLocationMonitoring() {
        // Start the foreground service
        serviceIntent = new Intent(requireContext(), LocationMonitoringService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(serviceIntent);
        } else {
            requireContext().startService(serviceIntent);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Define the bounds of the Philippines (lat/lng values for the country's borders)
        philippinesBounds = new LatLngBounds(
                new LatLng(4.5, 116.0), // Southwest corner of the Philippines
                new LatLng(21.0, 127.0) // Northeast corner of the Philippines
        );

        // Move the camera to the Philippines and restrict user panning to stay within the bounds
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(philippinesBounds, 0));

        // Restrict the map to the bounds of the Philippines
        googleMap.setLatLngBoundsForCameraTarget(philippinesBounds);

        // Optional: Disable zooming out too far (optional)
        googleMap.setMinZoomPreference(5.0f);
        googleMap.setMaxZoomPreference(15.0f);

        // Move the camera to the Philippines and restrict user panning to stay within the bounds
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(philippinesBounds, 0));

        // Restrict the map to the bounds of the Philippines
        googleMap.setLatLngBoundsForCameraTarget(philippinesBounds);

        // Optional: Disable zooming out too far (optional)
        googleMap.setMinZoomPreference(5.0f);
        googleMap.setMaxZoomPreference(15.0f);

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                String locationId = markerLocationId.get(marker);
                int visitCount = markerVisitCounts.get(marker);

                locationDetailsDialog(locationId, visitCount);

                return false;
            }
        });

        // Enable location layer if permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            checkGPSEnabled();
        } else {
            checkLocationPermission();
        }

        getLocations();
    }

    public void getLocations() {
        authManager.GetDb().getReference().child("Maps")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MapLocationsModel location = dataSnapshot.getValue(MapLocationsModel.class);
                            if (location != null) {
                                addMarker(location.getLocationLatitude(),
                                        location.getLocationLongitude(),
                                        location.getLocationName(),
                                        location.getLocationId());

                                // Add circles for restricted areas
                                if (!location.getIsBeach()) {
                                    addRestrictedArea(location);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public void addMarker(double latitude, double longitude, String locationName, String locationId) {
        // Create a query to find visits for this location using a flexible name matching
        DatabaseReference visitsRef = authManager.GetDb().getReference("LocationVisits");

        visitsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalVisitCount = 0;

                // Iterate through all location visit entries
                for (DataSnapshot locationSnapshot : snapshot.getChildren()) {
                    // Check if the location name contains the marker's location name (case-insensitive)
                    if (locationName.toLowerCase().contains(locationSnapshot.getKey().toLowerCase())) {
                        // Sum up visits for matching locations
                        for (DataSnapshot dateSnapshot : locationSnapshot.getChildren()) {
                            Integer visitCount = dateSnapshot.getValue(Integer.class);
                            if (visitCount != null) {
                                totalVisitCount += visitCount;
                            }
                        }
                    }
                }

                // Customize marker based on visit count
                BitmapDescriptor markerIcon;
                if (totalVisitCount > 0) {
                    // Change marker color based on visit count
                    if (totalVisitCount < 1) {
                        markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                    } else if (totalVisitCount < 5) {
                        markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                    } else {
                        markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                    }
                } else {
                    markerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                }

                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(locationName)
                        .snippet("Visits this month: " + totalVisitCount)
                        .icon(markerIcon));

                markerLocationId.put(marker, locationId);
                markerVisitCounts.put(marker, totalVisitCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                // Add error logging or user notification if needed
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title(locationName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                markerLocationId.put(marker, locationId);
            }
        });
    }

    private void addRestrictedArea(MapLocationsModel location) {
        LatLng position = new LatLng(location.getLocationLatitude(), location.getLocationLongitude());
        Circle circle = googleMap.addCircle(new CircleOptions()
                .center(position)
                .radius(GEOFENCE_RADIUS)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0))
                .strokeWidth(2));

        restrictedAreaCircles.put(location.getLocationId(), circle);
    }

    private void checkProximityToRestrictedAreas(Location userLocation) {
        if (!isLocationEnabled) return;

        boolean isInRestrictedArea = false;
        for (Circle circle : restrictedAreaCircles.values()) {
            Location circleCenter = new Location("");
            circleCenter.setLatitude(circle.getCenter().latitude);
            circleCenter.setLongitude(circle.getCenter().longitude);

            float distance = userLocation.distanceTo(circleCenter);
            if (distance <= GEOFENCE_RADIUS) {
                isInRestrictedArea = true;
                showProximityAlert();
                break;
            }
        }

        // Stop alert sound if user is no longer in restricted area
        if (!isInRestrictedArea && isPlayingAlert) {
            stopAlertSound();
        }
    }

    private void stopAlertSound() {
        isPlayingAlert = false;
        if (alertTimer != null) {
            alertTimer.cancel();
            alertTimer = null;
        }
        if (alertTask != null) {
            alertTask.cancel();
            alertTask = null;
        }
        if (alertSound != null && alertSound.isPlaying()) {
            alertSound.stop();
            alertSound.prepareAsync();
        }
    }

    private void showProximityAlert() {
        // Only show one dialog at a time
        if (isAlertDialogShowing) {
            return;
        }

        // Start continuous alert sound
        handleAlertSound();

        // Check if fragment is still attached to activity
        if (getContext() == null || getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        isAlertDialogShowing = true;

        // Run on UI thread to show dialog
        requireActivity().runOnUiThread(() -> {
            // Dismiss existing dialog if any
            if (currentAlertDialog != null && currentAlertDialog.isShowing()) {
                currentAlertDialog.dismiss();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                    .setTitle("Restricted Area Alert")
                    .setMessage("You are approaching a no-fishing zone!")
                    .setPositiveButton("OK", (dialog, which) -> {
                        isAlertDialogShowing = false;
                        // Don't stop the alert sound here
                    })
                    .setOnCancelListener(dialog -> {
                        isAlertDialogShowing = false;
                        // Don't stop the alert sound here
                    });

            currentAlertDialog = builder.create();
            currentAlertDialog.show();
        });
    }

    private void handleAlertSound() {
        if (alertTimer != null) {
            alertTimer.cancel();
            alertTimer = null;
        }

        if (alertTask != null) {
            alertTask.cancel();
            alertTask = null;
        }

        alertTimer = new Timer();
        alertTask = new TimerTask() {
            @Override
            public void run() {
                if (alertSound != null && !alertSound.isPlaying() && isPlayingAlert) {
                    alertSound.start();
                }
            }
        };

        // Schedule alert sound to play every 3 seconds
        alertTimer.scheduleAtFixedRate(alertTask, 0, 3000);
        isPlayingAlert = true;
    }

    public void locationDetailsDialog(String locationId, int visitCount) {
        // Dismiss any showing proximity alert before showing location details
        if (currentAlertDialog != null && currentAlertDialog.isShowing()) {
            currentAlertDialog.dismiss();
        }

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_map_details_dialog);

        if (locationId != null) {
            MapDetailsDialog mapDetailsDialog = new MapDetailsDialog(getContext(), locationId, visitCount);
            mapDetailsDialog.showDialog(dialog);
        }

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    checkGPSEnabled();
                    startLocationMonitoring();
                    if (googleMap != null) {
                        googleMap.setMyLocationEnabled(true);
                    }
                }
            } else {
                // Permission denied
                new AlertDialog.Builder(requireContext())
                        .setTitle("Location Permission Required")
                        .setMessage("This app needs location permission to alert you about restricted fishing areas. Please grant location permission in settings.")
                        .setPositiveButton("Settings", (dialog, which) -> {
                            // Open app settings
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Dismiss any showing dialog when fragment is paused
        if (currentAlertDialog != null && currentAlertDialog.isShowing()) {
            currentAlertDialog.dismiss();
        }
        isAlertDialogShowing = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAlertSound();
        if (alertSound != null) {
            alertSound.release();
            alertSound = null;
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (serviceIntent != null) {
            requireContext().stopService(serviceIntent);
        }
        if (currentAlertDialog != null && currentAlertDialog.isShowing()) {
            currentAlertDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the location service when view is destroyed
        if (serviceIntent != null) {
            requireContext().stopService(serviceIntent);
        }
    }
}
