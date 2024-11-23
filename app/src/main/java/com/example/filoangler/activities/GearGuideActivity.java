package com.example.filoangler.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// GearGuideActivity.java
// GearGuideActivity.java
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

        // Get video URL from intent
        String videoUrl = getIntent().getStringExtra("videoUrl");

        // Initialize ExoPlayer
        initializePlayer(videoUrl);

        // Initialize waypoints
        initializeWaypoints();

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

            // Get the raw resource ID directly from the videoUrl
            // Assuming videoUrl contains just the filename without extension
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
            player.play();

        } catch (Exception e) {
            Log.e("ExoPlayer", "Error initializing player: ", e);
            Toast.makeText(this, "Error initializing player: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initializeWaypoints() {
        waypoints = new ArrayList<>();
        // Add example waypoints - replace with your actual waypoints
        waypoints.add(new Waypoint("Step 1", "Introduction to the gear", 0));
        waypoints.add(new Waypoint("Step 2", "Assembly process begins", 30000)); // 30 seconds
        waypoints.add(new Waypoint("Step 3", "Connecting components", 60000)); // 1 minute
        // Add more waypoints as needed
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

// Waypoint.java and WaypointAdapter.java remain the same as in the previous implementation
