package com.example.filoangler.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Adapter.WaypointAdapter;
import com.example.filoangler.Model.Waypoint;
import com.example.filoangler.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GearGuideActivity extends AppCompatActivity {
    private PlayerView playerView;
    private ExoPlayer player;
    private SeekBar seekBar;
    private TextView currentTimeText;
    private TextView totalTimeText;
    private RecyclerView waypointsRecyclerView;
    private WaypointAdapter waypointAdapter;
    private Handler handler = new Handler();
    private List<Waypoint> waypoints;
    private boolean isUserSeeking = false;
    private String currentVideoFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gear_guide);

        // Initialize views
        playerView = findViewById(R.id.playerView);
        seekBar = findViewById(R.id.seekBar);
        currentTimeText = findViewById(R.id.currentTimeText);
        totalTimeText = findViewById(R.id.totalTimeText);
        waypointsRecyclerView = findViewById(R.id.waypointsRecyclerView);

        // Get video filename from intent
        currentVideoFileName = getIntent().getStringExtra("videoUrl");

        // Initialize waypoints before player
        initializeWaypoints();

        // Initialize ExoPlayer
        initializePlayer(currentVideoFileName);

        // Set up waypoints RecyclerView
        waypointAdapter = new WaypointAdapter(waypoints, position -> {
            // Handle waypoint click - seek to timestamp
            player.seekTo(waypoints.get(position).getTimestamp());
            player.play();
        });
        waypointsRecyclerView.setAdapter(waypointAdapter);
        waypointsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up seekbar and time tracking
        setupPlayerControls();
    }

    private void initializePlayer(String videoUrl) {
        try {
            // Build the player
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            // Get the raw resource ID
            int rawId = getResources().getIdentifier(videoUrl, "raw", getPackageName());

            if (rawId == 0) {
                throw new IllegalArgumentException("Could not find video resource: " + videoUrl);
            }

            // Create media item from raw resource
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + rawId);

            // Create media source
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                    Util.getUserAgent(this, getPackageName()));

            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(videoUri));

            // Prepare the player
            player.setMediaSource(mediaSource);
            player.prepare();

            // Add a listener to get the duration once it's available
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        // Set the total duration
                        long duration = player.getDuration();
                        seekBar.setMax((int) duration);
                        totalTimeText.setText(formatTime(duration));
                    }
                }
            });

            player.play();

        } catch (Exception e) {
            Log.e("ExoPlayer", "Error initializing player: ", e);
            Toast.makeText(this, "Error initializing player: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initializeWaypoints() {
        waypoints = new ArrayList<>();
        try {
            // Move the JSON file to the assets folder and read it
            InputStream is = getAssets().open("guide_waypoints.json");
            JsonObject jsonObject = new Gson().fromJson(
                    new InputStreamReader(is),
                    JsonObject.class
            );

            // Remove the .mp4 extension from the filename for comparison
            String videoKey = currentVideoFileName;
            if (videoKey.endsWith(".mp4")) {
                videoKey = videoKey.substring(0, videoKey.length() - 4);
            }

            // Get the specific video's waypoints
            if (!jsonObject.has(videoKey)) {
                Log.w("GearGuideActivity", "No waypoints found for video: " + videoKey);
                return;
            }

            JsonObject videoData = jsonObject.getAsJsonObject(videoKey);
            if (!videoData.has("steps")) {
                Log.w("GearGuideActivity", "No steps array found for video: " + videoKey);
                return;
            }

            JsonArray steps = videoData.getAsJsonArray("steps");
            for (JsonElement stepElement : steps) {
                JsonObject step = stepElement.getAsJsonObject();

                try {
                    String stepId = step.get("id").getAsString();
                    String timestamp = step.get("timestamp").getAsString();
                    String description = step.get("description").getAsString();

                    // Parse the timestamp with better error handling
                    long milliseconds = parseTimestamp(timestamp);

                    waypoints.add(new Waypoint(
                            stepId,
                            description,
                            (int) milliseconds
                    ));
                } catch (Exception e) {
                    Log.e("GearGuideActivity", "Error parsing step: " + step.toString(), e);
                }
            }

            is.close();

            if (waypoints.isEmpty()) {
                Log.w("GearGuideActivity", "No valid waypoints were loaded");
                Toast.makeText(this, "No waypoints available for this guide", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("GearGuideActivity", "Successfully loaded " + waypoints.size() + " waypoints");
            }

        } catch (IOException e) {
            Log.e("GearGuideActivity", "Error loading waypoints file", e);
            Toast.makeText(this, "Error loading waypoints", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("GearGuideActivity", "Error parsing waypoints", e);
            Toast.makeText(this, "Error parsing waypoints", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPlayerControls() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentTimeText.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                player.seekTo(seekBar.getProgress());
            }
        });

        // Start progress updates
        startProgressUpdate();
    }

    private void startProgressUpdate() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying() && !isUserSeeking) {
                    long currentPosition = player.getCurrentPosition();
                    seekBar.setProgress((int) currentPosition);
                    currentTimeText.setText(formatTime(currentPosition));
                    updateCurrentWaypoint(currentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        }, 0);
    }

    private void updateCurrentWaypoint(long currentPosition) {
        for (int i = 0; i < waypoints.size(); i++) {
            if (currentPosition >= waypoints.get(i).getTimestamp() &&
                    (i == waypoints.size() - 1 || currentPosition < waypoints.get(i + 1).getTimestamp())) {
                waypointAdapter.setCurrentWaypoint(i);
                break;
            }
        }
    }

    private long parseTimestamp(String timestamp) {
        try {
            String[] parts = timestamp.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid timestamp format: " + timestamp);
            }

            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);

            return (minutes * 60L + seconds) * 1000L; // Convert to milliseconds
        } catch (Exception e) {
            Log.e("GearGuideActivity", "Error parsing timestamp: " + timestamp, e);
            throw e;
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60));
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}