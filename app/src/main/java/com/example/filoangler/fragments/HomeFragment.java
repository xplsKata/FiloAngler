package com.example.filoangler.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Adapter.PostAdapter;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.PostModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.example.filoangler.activities.PostActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ImageButton btnAddPost;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<PostModel> postList;

    private List<String> followingList;
    private LoginManager loginManager;
    private AuthManager authManager;

    // Permission request launchers
    private ActivityResultLauncher<String[]> multiplePermissionsLauncher;
    private ActivityResultLauncher<Intent> settingsLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize permission launchers
        multiplePermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (Boolean isGranted : permissions.values()) {
                        if (!isGranted) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        // All permissions granted, proceed to PostActivity
                        navigateToPostActivity();
                    } else {
                        // Some permissions denied, show rationale
                        showPermissionRationaleDialog();
                    }
                }
        );

        // Launcher for settings to allow manual permission management
        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Check permissions again after returning from settings
                    if (checkStoragePermissions()) {
                        navigateToPostActivity();
                    } else {
                        showPermissionRationaleDialog();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        loginManager = new LoginManager(getContext());
        authManager = new AuthManager();

        recyclerViewPosts = view.findViewById(R.id.resultPosts);
        recyclerViewPosts.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerViewPosts.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();

        postAdapter = new PostAdapter(getContext(), postList);

        recyclerViewPosts.setAdapter(postAdapter);

        btnAddPost = view.findViewById(R.id.btnAddPost);

        followingList = new ArrayList<>();

        readPosts();

        btnAddPost.setOnClickListener(v -> handleAddPostClick());

        return view;
    }

    private void handleAddPostClick() {
        // Check if permissions are already granted
        if (checkStoragePermissions()) {
            navigateToPostActivity();
        } else {
            // Request permissions
            requestStoragePermissions();
        }
    }

    private boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 (API 33) and above
            return ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and 12
            return true; // Scoped storage doesn't require runtime permissions
        } else {
            // For Android 10 and below
            return ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 (API 33) and above
            multiplePermissionsLauncher.launch(new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and 12, no runtime permissions needed
            navigateToPostActivity();
        } else {
            // For Android 10 and below
            multiplePermissionsLauncher.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Storage Permissions Required")
                .setMessage("This app needs storage access to upload media. Please grant storage permissions to proceed.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    // Open app settings to allow manual permission management
                    Intent intent = new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", requireContext().getPackageName(), null)
                    );
                    settingsLauncher.launch(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(requireContext(),
                            "Cannot proceed without storage permissions",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void navigateToPostActivity() {
        Utils.ChangeIntent(getActivity(), PostActivity.class);
    }

    // Existing readPosts method remains the same as in your original code
    private void readPosts() {
        authManager.GetDb().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    // Log statements for debugging
                    Log.d("PostDebug", "PostId: " + dataSnapshot.getKey());

                    PostModel postModel = new PostModel(dataSnapshot);

                    if(postModel != null && postModel.getMediaItems() != null && !postModel.getMediaItems().isEmpty()){
                        postList.add(postModel);
                        Log.d("PostDebug", "Post added: " + postModel.getPostId());
                    } else {
                        Log.d("PostDebug", "Post NOT added: " + postModel.getPostId());
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeError", "Failed to read posts: " + error.getMessage());
            }
        });
    }
}