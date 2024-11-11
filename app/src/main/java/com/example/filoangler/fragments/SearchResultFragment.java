package com.example.filoangler.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.filoangler.Adapter.PostAdapter;
import com.example.filoangler.Adapter.UserAdapter;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Model.PostModel;
import com.example.filoangler.Model.UserModel;
import com.example.filoangler.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends Fragment {
    private RecyclerView recyclerPosts;
    private RecyclerView recyclerPeople;
    private List<UserModel> mUsers;
    private List<PostModel> mPosts;
    private UserAdapter userAdapter;
    private PostAdapter postAdapter;
    private AuthManager authManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);

        // Initialize components
        recyclerPosts = view.findViewById(R.id.recyclerPosts);
        recyclerPeople = view.findViewById(R.id.recyclerPeople);

        // Initialize lists and adapters
        mUsers = new ArrayList<>();
        mPosts = new ArrayList<>();
        authManager = new AuthManager();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Get search query from arguments
        String searchQuery = getArguments().getString("searchQuery", "");
        if (!searchQuery.isEmpty()) {
            performSearch(searchQuery);
        }

        return view;
    }

    private void setupRecyclerViews() {
        // Setup People RecyclerView
        recyclerPeople.setHasFixedSize(true);
        recyclerPeople.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(getContext(), mUsers, false, "search");
        recyclerPeople.setAdapter(userAdapter);

        // Setup Posts RecyclerView
        recyclerPosts.setHasFixedSize(true);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(getContext(), mPosts);
        recyclerPosts.setAdapter(postAdapter);
    }

    public void performSearch(String searchQuery) {
        searchQuery = searchQuery.toLowerCase();
        searchUsers(searchQuery);
        searchPosts(searchQuery);
    }

    private void searchUsers(String searchQuery) {
        DatabaseReference usersRef = authManager.GetDb().getReference().child("Users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        // Get user details
                        String username = userSnapshot.child("Account Details/Username")
                                .getValue(String.class);
                        String firstName = userSnapshot.child("Personal Information/FirstName")
                                .getValue(String.class);
                        String lastName = userSnapshot.child("Personal Information/LastName")
                                .getValue(String.class);

                        if (username == null) username = "";
                        if (firstName == null) firstName = "";
                        if (lastName == null) lastName = "";

                        // Check if search query matches username or full name
                        boolean matchesUsername = username.toLowerCase().contains(searchQuery);
                        boolean matchesName = (firstName + " " + lastName).toLowerCase()
                                .contains(searchQuery);

                        if (matchesUsername || matchesName) {
                            UserModel user = new UserModel();
                            user.setUserID(userSnapshot.child("Account Details/UserID")
                                    .getValue(String.class));
                            user.setUsername(username);
                            user.setFirstName(firstName);
                            user.setLastName(lastName);
                            user.setProfileIconURL(userSnapshot.child("Account Details/ProfileIconURL")
                                    .getValue(String.class));
                            user.setAnglerStatus(userSnapshot.child("Account Details/AnglerStatus")
                                    .getValue(String.class));

                            mUsers.add(user);
                        }
                    } catch (Exception e) {
                        Log.e("SearchError", "Error processing user: " + e.getMessage());
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SearchError", "Database error: " + error.getMessage());
            }
        });
    }

    private void searchPosts(String searchQuery) {
        DatabaseReference postsRef = authManager.GetDb().getReference().child("Posts");

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mPosts.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    try {
                        String description = postSnapshot.child("Description")
                                .getValue(String.class);

                        if (description != null &&
                                description.toLowerCase().contains(searchQuery)) {
                            PostModel post = postSnapshot.getValue(PostModel.class);
                            if (post != null) {
                                mPosts.add(post);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("SearchError", "Error processing post: " + e.getMessage());
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SearchError", "Database error: " + error.getMessage());
            }
        });
    }
}