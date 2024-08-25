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
import android.widget.ImageButton;

import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.example.filoangler.fragments.HomeFragment;
import com.example.filoangler.fragments.MapFragment;
import com.example.filoangler.fragments.NotificationsFragment;
import com.example.filoangler.fragments.TideFragment;
import com.example.filoangler.fragments.WeatherFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BloggingActivity extends AppCompatActivity {
    private BottomNavigationView BottomNavigationView;
    private Fragment SelectedFragment;
    private ImageButton btnIcon, btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blogging);

        BottomNavigationView = findViewById(R.id.bottomNavigationView);
        btnIcon = findViewById(R.id.btnIcon);
        btnSearch = findViewById(R.id.btnSearch);

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

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ChangeIntent(BloggingActivity.this, SearchActivity.class);
            }
        });

        btnIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager loginManager = new LoginManager(BloggingActivity.this);
                loginManager.LogOut();
                Utils.ChangeIntent(BloggingActivity.this, LoginActivity.class);
                finish();
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.bloggingActivityFrameLayout, new HomeFragment()).commit();
    }
}