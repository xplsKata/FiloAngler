package com.example.filoangler.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.Button;

import com.example.filoangler.R;
import com.example.filoangler.fragments.HomeFragment;
import com.example.filoangler.fragments.MapFragment;
import com.example.filoangler.fragments.NotificationsFragment;
import com.example.filoangler.fragments.TideFragment;
import com.example.filoangler.fragments.WeatherFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BloggingActivity extends AppCompatActivity {
    private BottomNavigationView BottomNavigationView;
    private Fragment SelectedFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blogging);

        BottomNavigationView = findViewById(R.id.bottomNavigationView);

        BottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.Home){
                    SelectedFragment = new HomeFragment();
                }else if(item.getItemId() == R.id.Map){
                    SelectedFragment = new MapFragment();
                }else if(item.getItemId() == R.id.Weather){
                    SelectedFragment = new WeatherFragment();
                }else if(item.getItemId() == R.id.Tide){
                    SelectedFragment = new TideFragment();
                }else if(item.getItemId() == R.id.Notifications){
                    SelectedFragment = new NotificationsFragment();
                }
                if(SelectedFragment != null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.bloggingActivityFrameLayout, SelectedFragment).commit();
                }
                return true;
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.bloggingActivityFrameLayout, new HomeFragment()).commit();
    }
}