package com.example.filoangler.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.filoangler.Adapter.PostAdapter;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.PostModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostDetailsDialog {

    private AuthManager authManager;
    private LoginManager loginManager;
    private String postId;
    private String userId;
    private PostModel postModel;
    private PostAdapter postAdapter;
    private Context mContext;

    public PostDetailsDialog(Context context, AuthManager authManager, String postId) {
        this.mContext = context;
        this.authManager = authManager;
        this.postId = postId;
        this.loginManager = new LoginManager(context);
        List<PostModel> dummyList = new ArrayList<>();
        this.postAdapter = new PostAdapter(context, dummyList);
    }

    public void getPost(Dialog dialog) {
        ImageView imgPostImage = dialog.findViewById(R.id.imgPostImage);
        ImageView imgProfile = dialog.findViewById(R.id.imgProfileIcon);

        TextView txtCaption = dialog.findViewById(R.id.txtCaption);
        TextView txtName = dialog.findViewById(R.id.txtName);
        TextView txtUsername = dialog.findViewById(R.id.txtUsername);
        TextView txtUsernameCpt = dialog.findViewById(R.id.txtUsernameCpt);
        TextView txtLikesAmount = dialog.findViewById(R.id.txtLikesAmount);
        TextView txtCommentsAmount = dialog.findViewById(R.id.txtCommentsAmount);

        ImageButton btnComment = dialog.findViewById(R.id.btnComment);
        ImageButton btnLike = dialog.findViewById(R.id.btnLike);
        ImageButton btnMore = dialog.findViewById(R.id.btnMore);

        authManager.GetDb().getReference().child("Posts").child(postId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postModel = snapshot.getValue(PostModel.class);
                        if (postModel != null) {
                            userId = postModel.getAuthor();
                            Picasso.get().load(postModel.getImageURL()).into(imgPostImage);
                            txtCaption.setText(postModel.getDescription());

                            // Fetch user details
                            authManager.GetDb().getReference().child("Users").child(postModel.getAuthor())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String firstName = snapshot.child("Personal Information").child("FirstName").getValue(String.class);
                                            String lastName = snapshot.child("Personal Information").child("LastName").getValue(String.class);
                                            String profileIconURL = snapshot.child("Personal Information").child("ProfileIconURL").getValue(String.class);
                                            String username = snapshot.child("Account Details").child("Username").getValue(String.class);

                                            txtName.setText(firstName + " " + lastName);
                                            txtUsername.setText(username);
                                            txtUsernameCpt.setText(username);
                                            if (profileIconURL != null && !profileIconURL.equals("null")) {
                                                Picasso.get().load(profileIconURL).into(imgProfile);
                                            } else {
                                                imgProfile.setImageResource(R.mipmap.ic_launcher);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Handle error
                                        }
                                    });

                            postAdapter.interactionCounter(postId, txtLikesAmount, "Likes");
                            postAdapter.interactionCounter(postId, txtCommentsAmount, "Comments");
                            postAdapter.isLiked(postId, btnLike);

                            setupClickListeners(btnLike, btnComment, btnMore, txtName, imgProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void setupClickListeners(ImageButton btnLike, ImageButton btnComment, ImageButton btnMore, TextView txtName, ImageView imgProfile) {
        btnLike.setOnClickListener(v -> {
            if (postModel != null) {
                postAdapter.likeButton(postId, userId, btnLike);
            }
        });

        btnComment.setOnClickListener(v -> {
            if (postModel != null) {
                postAdapter.showComments(postId, userId);
            }
        });

        btnMore.setOnClickListener(v -> {
            if (postModel != null) {
                postAdapter.moreDialog(postModel);
            }
        });

        txtName.setOnClickListener(v -> {
            if (userId != null) {
                Utils.goToProfile(userId, mContext);
            }
        });

        imgProfile.setOnClickListener(v -> {
            if (userId != null) {
                Utils.goToProfile(userId, mContext);
            }
        });
    }
}