package com.example.filoangler.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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

        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ChangeIntent(getActivity(), PostActivity.class);
            }
        });

        return view;
    }

    private void readPosts() {
        authManager.GetDb().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    // Log the entire snapshot to verify structure
                    Log.d("PostDebug", "PostId: " + dataSnapshot.getKey());
                    Log.d("PostDebug", "Full Snapshot: " + dataSnapshot.toString());

                    // Log specific children
                    if (dataSnapshot.child("mediaUrls").exists()) {
                        Log.d("PostDebug", "MediaUrls exist");
                        for (DataSnapshot mediaSnapshot : dataSnapshot.child("mediaUrls").getChildren()) {
                            Log.d("PostDebug", "Media URL: " + mediaSnapshot.child("url").getValue());
                            Log.d("PostDebug", "Is Video: " + mediaSnapshot.child("isVideo").getValue());
                        }
                    } else {
                        Log.d("PostDebug", "No mediaUrls child found");
                    }

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