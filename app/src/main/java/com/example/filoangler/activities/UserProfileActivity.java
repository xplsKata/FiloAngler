package com.example.filoangler.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filoangler.Adapter.PostAdapter;
import com.example.filoangler.Dialog.FollowListDialog;
import com.example.filoangler.Dialog.UserFeedbackDialog;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.PostModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView imgProfileIcon, imgVerified;
    private ImageButton btnBack, btnMore;
    private Button btnEditProfile, btnFollow, btnMessage;
    private TextView txtName, txtUsername, txtAnglerStatus, txtBio, txtFollowers, txtFollowing, txtPosts;
    private LinearLayout btnFollowers, btnFollowing;
    private LoginManager loginManager;
    private AuthManager authManager;

    private String UserId;
    private String otherUserId;

    private PostAdapter postAdapter;
    private List<PostModel> postList;
    private RecyclerView recyclerViewPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        loginManager = new LoginManager(this);
        authManager = new AuthManager();

        postList = new ArrayList<>();

        postAdapter = new PostAdapter(this, postList);

        recyclerViewPosts = findViewById(R.id.recyclerView);
        recyclerViewPosts.setAdapter(postAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerViewPosts.setLayoutManager(linearLayoutManager);

        imgProfileIcon = findViewById(R.id.imgProfileIcon);
        btnBack = findViewById(R.id.btnBack);
        btnMore = findViewById(R.id.btnMore);
        btnMessage = findViewById(R.id.btnMessage);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        txtName = findViewById(R.id.txtName);
        txtAnglerStatus = findViewById(R.id.txtAnglerStatus);
        txtUsername = findViewById(R.id.txtUsername);
        txtBio = findViewById(R.id.txtBio);
        txtFollowers = findViewById(R.id.txtFollowersCount);
        txtFollowing = findViewById(R.id.txtFollowingCount);
        txtPosts = findViewById(R.id.txtPostsCount);
        btnFollow = findViewById(R.id.btnFollow);
        btnFollowers = findViewById(R.id.btnFollowers);
        btnFollowing = findViewById(R.id.btnFollowing);
        imgVerified = findViewById(R.id.imgVerified);

        UserId = getIntent().getStringExtra("UserId");
        Log.e("UserProfileActivity", "Intent Received: " + UserId);

        otherUserId = getIntent().getStringExtra("UserId");

        populateProfile();
        readPosts();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.followUser(authManager, btnFollow, loginManager, UserId, UserProfileActivity.this);
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ChangeIntent(UserProfileActivity.this, EditProfileActivity.class);
            }
        });

        btnFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFollowers();
            }
        });

        btnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFollowing();
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportUser(UserId);
            }
        });

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateConversation();
            }
        });

    }

    private void initiateConversation() {
        // Get current user ID
        String currentUserId = loginManager.GetCurrentUser().getUid();

        // Reference to the current user's inbox for this other user
        DatabaseReference inboxRef = authManager.GetDb().getReference("Users")
                .child(currentUserId)
                .child("Inbox")
                .child(otherUserId);

        inboxRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if a conversation already exists
                String existingConversationId = snapshot.child("conversationId").getValue(String.class);

                if (existingConversationId != null) {
                    // Conversation already exists, start chat with existing ID
                    startChatActivity(existingConversationId);
                } else {
                    // No existing conversation, create a new one
                    createNewConversation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this,
                        "Failed to check conversation: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewConversation() {
        String currentUserId = loginManager.GetCurrentUser().getUid();

        // Generate a unique conversation ID
        DatabaseReference conversationsRef = authManager.GetDb().getReference("Users")
                .child(currentUserId)
                .child("Inbox")
                .child(otherUserId);

        String conversationId = conversationsRef.push().getKey();

        // Prepare updates for both users' inboxes
        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + "Users" + "/" + currentUserId + "/Inbox/" + otherUserId + "/conversationId", conversationId);
        updates.put("/" + "Users" + "/" + otherUserId + "/Inbox/" + currentUserId + "/conversationId", conversationId);

        authManager.GetDb().getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Start ChatActivity with new conversation details
                    startChatActivity(conversationId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to start conversation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void startChatActivity(String conversationId) {
        Intent chatIntent = new Intent(UserProfileActivity.this, ChatActivity.class);
        chatIntent.putExtra("CONVERSATION_ID", conversationId);
        chatIntent.putExtra("USER_ID", otherUserId);
        startActivity(chatIntent);
    }

    private void viewFollowers(){

        showDialog("Followers");

    }

    private void viewFollowing(){

        showDialog("Following");

    }

    private void populateProfile() {

        Log.e("UserProfileActivity", "Intent Data: " + UserId);
        getProfileDetails();

        if(UserId.equals(loginManager.GetCurrentUser().getUid())){

        }else{

        }

    }

    private void getProfileDetails() {

        DatabaseReference userRef = authManager.GetDb().getReference().child("Users").child(UserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("UserProfileActivity", "Trying to find user");

                if (!snapshot.exists()) {
                    Log.e("ProfileDetails", "User data not found for UserId: " + UserId);
                    Toast.makeText(UserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                try {
                    String firstName = snapshot.child("Personal Information").child("FirstName").getValue(String.class);
                    String lastName = snapshot.child("Personal Information").child("LastName").getValue(String.class);
                    String anglerStatus = snapshot.child("Account Details").child("AnglerStatus").getValue(String.class);
                    Boolean anglerStatusVerified = snapshot.child("Account Details").child("AnglerStatusVerified").getValue(Boolean.class);
                    String profileIconURL = snapshot.child("Account Details").child("ProfileIconURL").getValue(String.class);
                    String username = snapshot.child("Account Details").child("Username").getValue(String.class);
                    String bio = snapshot.child("Personal Information").child("Bio").getValue(String.class);

                    long followingCount = snapshot.child("Following").getChildrenCount();
                    long followerCount = snapshot.child("Followers").getChildrenCount();

                    boolean isVerified = (anglerStatusVerified != null) ? anglerStatusVerified : false;

                    runOnUiThread(() -> {
                        if (profileIconURL != null && !profileIconURL.isEmpty()) {
                            Picasso.get().load(profileIconURL).into(imgProfileIcon);
                        } else {
                            imgProfileIcon.setImageResource(R.drawable.default_icon);
                        }

                        txtBio.setText((bio == null || bio.isEmpty()) ? "None" : bio);
                        txtName.setText(String.format("%s %s", firstName, lastName));
                        txtAnglerStatus.setText(anglerStatus);
                        if(isVerified){
                            imgVerified.setVisibility(View.VISIBLE);
                        }else{
                            imgVerified.setVisibility(View.GONE);
                        }
                        txtUsername.setText(username);
                        txtFollowers.setText(String.valueOf(followerCount));
                        txtFollowing.setText(String.valueOf(followingCount));

                        if(UserId.equals(loginManager.GetCurrentUser().getUid())){
                            btnFollow.setVisibility(View.GONE);
                            btnMore.setVisibility(View.GONE);
                            btnMessage.setVisibility(View.GONE);
                            btnEditProfile.setVisibility(View.VISIBLE);
                        }else{
                            btnEditProfile.setVisibility(View.GONE);
                            btnMore.setVisibility(View.VISIBLE);
                            btnFollow.setVisibility(View.VISIBLE);
                            btnMessage.setVisibility(View.VISIBLE);

                            isFollowed(UserId, btnFollow);
                        }
                    });

                } catch (Exception e) {
                    Log.e("ProfileDetails", "Error processing user data: " + e.getMessage());
                    // Consider showing a user-friendly error message
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getPostsCount();


    }

    private void getPostsCount() {

        authManager.GetDb().getReference().child("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int counter = 0;
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            PostModel postModel = dataSnapshot.getValue(PostModel.class);

                            if(postModel.getAuthor().equals(UserId)){
                                counter++;
                            }

                        }
                        txtPosts.setText(String.valueOf(counter));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void isFollowed(final String id, Button btnFollow){
        DatabaseReference reference = authManager.GetDb().getReference().child("Users")
                .child(loginManager.GetCurrentUser().getUid())
                .child("Following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(id).exists()){
                    btnFollow.setText(R.string.txtFollowing);

                    btnFollow.setBackgroundColor(getResources().getColor(R.color.white));
                    btnFollow.setTextColor(getResources().getColor(R.color.bgBlue));
                }else{
                    btnFollow.setText(R.string.txtFollow);

                    btnFollow.setBackgroundColor(getResources().getColor(R.color.iconBlue));
                    btnFollow.setTextColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPosts() {
        authManager.GetDb().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    PostModel postModel = new PostModel(dataSnapshot);

                    // Check if the post's author matches the UserId of the profile being viewed
                    if(postModel != null &&
                            postModel.getAuthor().equals(UserId) &&
                            postModel.getMediaItems() != null &&
                            !postModel.getMediaItems().isEmpty()){

                        postList.add(postModel);
                        Log.d("PostDebug", "Post added: " + postModel.getPostId());
                    } else {
                        Log.d("PostDebug", "Post NOT added: " + (postModel != null ? postModel.getPostId() : "null post"));
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

    private void showDialog(String title){
        final Dialog dialog = new Dialog(UserProfileActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_follow_list_dialog);

        if(UserId != null){
            FollowListDialog followListDialog = new FollowListDialog(authManager, UserId, title);
            followListDialog.getDialog(dialog, UserProfileActivity.this);
        }else{
            //error here
        }

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void reportUser(String reportedUser){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_report_dialog);

        UserFeedbackDialog userFeedbackDialog = new UserFeedbackDialog(this, reportedUser, reportedUser,"isUser");
        userFeedbackDialog.getDialog(dialog);

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
}