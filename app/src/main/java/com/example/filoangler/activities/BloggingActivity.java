package com.example.filoangler.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.filoangler.Adapter.SideNavAdapter;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.example.filoangler.fragments.FishDatabankFragment;
import com.example.filoangler.fragments.HomeFragment;
import com.example.filoangler.fragments.MapFragment;
import com.example.filoangler.fragments.NotificationsFragment;
import com.example.filoangler.fragments.TideFragment;
import com.example.filoangler.fragments.WeatherFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class BloggingActivity extends AppCompatActivity {
    private BottomNavigationView BottomNavigationView;
    private Fragment SelectedFragment;
    private ImageButton btnSearch;
    private Button btnLogout;
    private TextView txtName, txtUsername;
    private ImageView imgProfileIcon, btnIcon;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LoginManager loginManager;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blogging);

        loginManager = new LoginManager(this);
        authManager = new AuthManager();

        BottomNavigationView = findViewById(R.id.bottomNavigationView);
        btnIcon = findViewById(R.id.btnIcon);
        btnSearch = findViewById(R.id.btnSearch);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.sideNavBar);

        btnLogout = navigationView.findViewById(R.id.btnLogout);

        authManager.GetDb().getReference().child("Users")
                .child(loginManager.GetCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileIconURL = snapshot.child("Account Details").child("ProfileIconURL").getValue(String.class);

                        if (profileIconURL != null && !profileIconURL.equals("null")) {
                            Picasso.get().load(profileIconURL).into(btnIcon);
                        } else {
                            btnIcon.setImageResource(R.mipmap.ic_launcher);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if(navigationView != null){
            View headerView = navigationView.getHeaderView(0);
            txtName = headerView.findViewById(R.id.txtName);
            txtUsername = headerView.findViewById(R.id.txtUsername);
            imgProfileIcon = headerView.findViewById(R.id.imgProfileIcon);

            SideNavAdapter sideNavAdapter = new SideNavAdapter();
            sideNavAdapter.setSideNavUser(txtName,txtUsername,imgProfileIcon, loginManager, authManager);
        }

        navigationView.bringToFront();

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

        btnIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }else{
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ChangeIntent(BloggingActivity.this, SearchActivity.class);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d("SideNav", "Menu item clicked: " + item.getTitle());
                int id = item.getItemId();

                if (id == R.id.navFishDatabank) {
                    Log.d("SideNav", "Fish Databank clicked");
                    SelectedFragment = new FishDatabankFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.bloggingActivityFrameLayout, SelectedFragment)
                            .commit();
                } else if (id == R.id.navEquipments) {
                    Log.d("SideNav", "Equipments clicked");
                    // Handle Equipments action
                } else if (id == R.id.navMustKnow) {
                    Log.d("SideNav", "Must Know clicked");
                    // Handle Must Know action
                } else if (id == R.id.navProfile) {
                    Log.d("SideNav", "Profile clicked");
                    Intent intent = new Intent(BloggingActivity.this, UserProfileActivity.class);
                    intent.putExtra("UserId", loginManager.GetCurrentUser().getUid());
                    startActivity(intent);
                } else if (id == R.id.navSettings) {
                    Log.d("SideNav", "Settings clicked");
                    // Handle Settings action
                } else {
                    return false;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        if (btnLogout != null) {
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("SideNav", "Logout button clicked");
                    loginManager.LogOut();
                    Utils.ChangeIntent(BloggingActivity.this, LoginActivity.class);
                    finish();
                }
            });

        } else {
            Log.e("SideNav", "Logout button not found in NavigationView");
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.bloggingActivityFrameLayout, new HomeFragment()).commit();
    }

    @Override
    public void onBackPressed(){

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }

    }

}