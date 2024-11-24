package com.example.filoangler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.content.SharedPreferences;

import com.example.filoangler.activities.BloggingActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.filoangler.Model.MapLocationsModel;
import com.example.filoangler.Manager.AuthManager;

import java.util.ArrayList;
import java.util.List;

public class LocationMonitoringService extends Service {
    private static final String CHANNEL_ID = "LocationMonitoringChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final float GEOFENCE_RADIUS = 300; // meters

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private AuthManager authManager;
    private List<MapLocationsModel> restrictedLocations;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        authManager = new AuthManager();
        restrictedLocations = new ArrayList<>();
        preferences = getSharedPreferences("MapSettings", MODE_PRIVATE);

        createNotificationChannel();
        setupLocationUpdates();
        loadRestrictedLocations();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Monitoring",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Monitors proximity to restricted fishing areas");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setupLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(30000) // 30 seconds
                .setFastestInterval(10000); // 10 seconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                if (!preferences.getBoolean("location_alerts_enabled", true)) {
                    return;
                }

                Location userLocation = locationResult.getLastLocation();
                checkProximityToRestrictedAreas(userLocation);
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            stopSelf();
        }
    }

    private void loadRestrictedLocations() {
        authManager.GetDb().getReference().child("Maps")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        restrictedLocations.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MapLocationsModel location = dataSnapshot.getValue(MapLocationsModel.class);
                            if (location != null && !location.getIsBeach()) {
                                restrictedLocations.add(location);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
    }

    private void checkProximityToRestrictedAreas(Location userLocation) {
        for (MapLocationsModel restrictedLocation : restrictedLocations) {
            Location locationToCheck = new Location("");
            locationToCheck.setLatitude(restrictedLocation.getLocationLatitude());
            locationToCheck.setLongitude(restrictedLocation.getLocationLongitude());

            float distance = userLocation.distanceTo(locationToCheck);
            if (distance <= GEOFENCE_RADIUS) {
                showRestrictedAreaNotification(restrictedLocation.getLocationName());
                break;
            }
        }
    }

    private void showRestrictedAreaNotification(String locationName) {
        Intent intent = new Intent(this, BloggingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_warning_24)
                .setContentTitle("Restricted Area Alert")
                .setContentText("You are near " + locationName + ", a no-fishing zone!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create a persistent notification for the foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FiloAngler")
                .setContentText("Monitoring fishing zones")
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}