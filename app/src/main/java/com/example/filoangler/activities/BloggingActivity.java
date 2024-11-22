package com.example.filoangler.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.filoangler.Adapter.SideNavAdapter;
import com.example.filoangler.Dialog.VerifyProfileDialog;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.example.filoangler.fragments.FishDatabankFragment;
import com.example.filoangler.fragments.HomeFragment;
import com.example.filoangler.fragments.MapFragment;
import com.example.filoangler.fragments.NoInternetFragment;
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
    private boolean isOfflineMode = false;
    private boolean isAccountLoading = true;

    private BottomNavigationView bottomNavigationView;
    private Fragment selectedFragment;
    private ImageButton btnSearch;
    private Button btnLogout;
    private TextView txtName, txtUsername;
    private ImageView imgProfileIcon, btnIcon;
    private View loadingOverlay;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LoginManager loginManager;
    private AuthManager authManager;
    private VerifyProfileDialog verifyProfileDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blogging);

        isOfflineMode = getIntent().getBooleanExtra("offline_mode", false);
        verifyProfileDialog = new VerifyProfileDialog(this, this);

        initializeViews();
        setupUserProfile();
        setupNavigationListeners();

        if (isOfflineMode) {
            setupOfflineMode();
            // Skip loading in offline mode
            isAccountLoading = false;
            updateLoadingState(false);
            // Show NoInternetFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.bloggingActivityFrameLayout, new NoInternetFragment())
                    .commit();
        } else {
            // Only show loading overlay in online mode
            updateLoadingState(true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.bloggingActivityFrameLayout, new HomeFragment())
                    .commit();
        }
    }

    private void initializeViews() {
        loginManager = new LoginManager(this);
        authManager = new AuthManager();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        btnIcon = findViewById(R.id.btnIcon);
        btnSearch = findViewById(R.id.btnSearch);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.sideNavBar);

        btnLogout = navigationView.findViewById(R.id.btnLogout);

        // Initialize loading overlay
        loadingOverlay = findViewById(R.id.loadingOverlay);
        // Don't show loading overlay initially if in offline mode
        if (!isOfflineMode) {
            updateLoadingState(true);
        }
    }

    private void updateLoadingState(boolean isLoading) {
        // Skip loading state updates if in offline mode
        if (isOfflineMode) {
            isAccountLoading = false;
            if (loadingOverlay != null) {
                loadingOverlay.setVisibility(View.GONE);
            }
            return;
        }

        isAccountLoading = isLoading;
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Disable interactions while loading (only in online mode)
        bottomNavigationView.setEnabled(!isLoading);
        btnSearch.setEnabled(!isLoading);
        btnIcon.setEnabled(!isLoading);
        if (btnLogout != null) btnLogout.setEnabled(!isLoading);
    }

    private void setupOfflineMode() {
        // Disable specific side navigation items
        Menu sideMenu = navigationView.getMenu();
        MenuItem profileItem = sideMenu.findItem(R.id.navProfile);
        MenuItem helpItem = sideMenu.findItem(R.id.btnHelp);
        MenuItem proficiencyTestItem = sideMenu.findItem(R.id.navProficiencyTest);

        if (profileItem != null) profileItem.setEnabled(false);
        if (helpItem != null) helpItem.setEnabled(false);
        if (proficiencyTestItem != null) proficiencyTestItem.setEnabled(false);

        // Disable search button
        btnSearch.setEnabled(false);
        btnSearch.setAlpha(0.5f);
    }

    private void setupUserProfile() {
        if (!isOfflineMode) {
            if (loginManager.GetCurrentUser() != null) {
                authManager.GetDb().getReference().child("Users")
                        .child(loginManager.GetCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String profileIconURL = snapshot.child("Account Details")
                                        .child("ProfileIconURL").getValue(String.class);

                                if (profileIconURL != null && !profileIconURL.equals("null")) {
                                    Picasso.get().load(profileIconURL).into(btnIcon);
                                } else {
                                    btnIcon.setImageResource(R.drawable.default_icon);
                                }

                                // Account loading complete
                                updateLoadingState(false);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle error case
                                updateLoadingState(false);
                            }
                        });
            }

            if (navigationView != null) {
                View headerView = navigationView.getHeaderView(0);
                txtName = headerView.findViewById(R.id.txtName);
                txtUsername = headerView.findViewById(R.id.txtUsername);
                imgProfileIcon = headerView.findViewById(R.id.imgProfileIcon);

                SideNavAdapter sideNavAdapter = new SideNavAdapter();
                sideNavAdapter.setSideNavUser(txtName, txtUsername, imgProfileIcon,
                        loginManager, authManager);
            }
        } else {
            // Set default values for offline mode
            btnIcon.setImageResource(R.drawable.default_icon);

            if (navigationView != null) {
                View headerView = navigationView.getHeaderView(0);
                txtName = headerView.findViewById(R.id.txtName);
                txtUsername = headerView.findViewById(R.id.txtUsername);
                imgProfileIcon = headerView.findViewById(R.id.imgProfileIcon);

                if (txtName != null) txtName.setText("Offline Mode");
                if (txtUsername != null) txtUsername.setText("Guest User");
                if (imgProfileIcon != null) imgProfileIcon.setImageResource(R.drawable.default_icon);
            }
        }

        navigationView.bringToFront();
    }

    private void setupNavigationListeners() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (isAccountLoading && !isOfflineMode) {
                return false;
            }

            if (isOfflineMode) {
                // In offline mode, show NoInternetFragment for all navigation items
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.bloggingActivityFrameLayout, new NoInternetFragment())
                        .commit();
                return true;
            }

            if (item.getItemId() == R.id.Home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.Map) {
                selectedFragment = new MapFragment();
            } else if (item.getItemId() == R.id.Weather) {
                selectedFragment = new WeatherFragment();
            } else if (item.getItemId() == R.id.Tide) {
                selectedFragment = new TideFragment();
            } else if (item.getItemId() == R.id.Notifications) {
                selectedFragment = new NotificationsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.bloggingActivityFrameLayout, selectedFragment)
                        .commit();
            }
            return true;
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
                if (!isOfflineMode) {  // Only allow search in online mode
                    Utils.ChangeIntent(BloggingActivity.this, SearchActivity.class);
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (isOfflineMode) {
                    // In offline mode, only allow certain menu items
                    int id = item.getItemId();
                    if (id == R.id.navProfile || id == R.id.btnHelp) {
                        return false;
                    }
                }

                Log.d("SideNav", "Menu item clicked: " + item.getTitle());
                int id = item.getItemId();

                if (id == R.id.navFishDatabank) {
                    Log.d("SideNav", "Fish Databank clicked");
                    selectedFragment = new FishDatabankFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.bloggingActivityFrameLayout, selectedFragment)
                            .commit();
                } else if (id == R.id.navEquipments) {
                    Log.d("SideNav", "Equipments clicked");
                    // Handle Equipments action
                } else if (id == R.id.navMustKnow) {
                    Log.d("SideNav", "Must Know clicked");
                    // Handle Must Know action
                } else if (id == R.id.navProfile && !isOfflineMode) {
                    Log.d("SideNav", "Profile clicked");
                    Intent intent = new Intent(BloggingActivity.this, UserProfileActivity.class);
                    intent.putExtra("UserId", loginManager.GetCurrentUser().getUid());
                    startActivity(intent);
                } else if (id == R.id.navProficiencyTest && !isOfflineMode) {
                    Log.d("SideNav", "Proficiency Test clicked");
                    Utils.ChangeIntent(BloggingActivity.this, QuizzesActivity.class);
                }  else if (id == R.id.navVerifyProfile && !isOfflineMode) {
                    Log.d("SideNav", "Verify profile clicked");
                    showDialog();
                }  else {
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
                    if (!isOfflineMode) {
                        loginManager.LogOut();
                    }
                    Utils.ChangeIntent(BloggingActivity.this, LoginActivity.class);
                    finish();
                }
            });
        } else {
            Log.e("SideNav", "Logout button not found in NavigationView");
        }
    }

    private void showDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_verify_profile);

        verifyProfileDialog.getDialog(dialog);

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (verifyProfileDialog != null) {
            verifyProfileDialog.handleActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (verifyProfileDialog != null) {
            verifyProfileDialog.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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