package com.example.filoangler.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.CommentModel;
import com.example.filoangler.Model.PostModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{
    private Context mContext;
    private List<PostModel> mPost;
    private LoginManager loginManager;
    private AuthManager authManager;

    private RecyclerView recyclerView;
    private CommentsAdapter commentsAdapter;
    private List<CommentModel> commentModelList;
    private ImagePagerAdapter imagePagerAdapter;

    public PostAdapter(){
        authManager = new AuthManager();
    }

    public PostAdapter(Context mContext, List<PostModel> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
        if (mContext != null) {
            this.loginManager = new LoginManager(mContext);
        }
        this.authManager = new AuthManager();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_post, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        loginManager = new LoginManager(mContext);
        authManager = new AuthManager();

        PostModel postModel = mPost.get(position);

        imagePagerAdapter = new ImagePagerAdapter(mContext, postModel.getImageURLs());
        holder.viewPagerImages.setAdapter(imagePagerAdapter);

        if (postModel.getImageURLs().size() > 1) {
            holder.layoutDots.setVisibility(View.VISIBLE);
            setupImageIndicator(holder, postModel.getImageURLs().size());
        } else {
            holder.layoutDots.setVisibility(View.GONE);
        }

        getPost(postModel,
                holder.viewPagerImages,
                holder.txtCaption,
                holder.imgProfile,
                holder.txtName,
                holder.txtUsername,
                holder.txtUsernameCpt,
                holder.txtDateAndTime);
        isLiked(postModel.getPostId(), holder.btnLike);
        interactionCounter(postModel.getPostId(), holder.txtLikesAmount, "Likes");
        interactionCounter(postModel.getPostId(), holder.txtCommentsAmount, "Comments");

        setupClickListeners(holder, postModel);
    }


    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imgProfile;

        public ImageButton btnLike;
        public ImageButton btnComment;
        public ImageButton btnMore;

        public TextView txtUsername;
        public TextView txtName;
        public TextView txtLikesAmount;
        public TextView txtCommentsAmount;
        public TextView txtUsernameCpt;
        public TextView txtCaption;
        public TextView txtDateAndTime;

        public ViewPager2 viewPagerImages;
        public LinearLayout layoutDots;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgProfileIcon);

            btnComment = itemView.findViewById(R.id.btnComment);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnMore = itemView.findViewById(R.id.btnMore);

            txtUsername = itemView.findViewById(R.id.txtUsername);
            txtName = itemView.findViewById(R.id.txtName);
            txtLikesAmount = itemView.findViewById(R.id.txtLikesAmount);
            txtCommentsAmount = itemView.findViewById(R.id.txtCommentsAmount);
            txtUsernameCpt = itemView.findViewById(R.id.txtUsernameCpt);
            txtCaption = itemView.findViewById(R.id.txtCaption);
            txtDateAndTime = itemView.findViewById(R.id.txtDate);


            viewPagerImages = itemView.findViewById(R.id.viewPagerImages);
            layoutDots = itemView.findViewById(R.id.layoutDots);
        }
    }

    private void setupImageIndicator(ViewHolder holder, int imageCount) {
        holder.layoutDots.removeAllViews();
        ImageView[] dots = new ImageView[imageCount];

        for (int i = 0; i < imageCount; i++) {
            dots[i] = new ImageView(mContext);
            dots[i].setImageDrawable(ContextCompat.getDrawable(mContext,
                    i == 0 ? R.drawable.dot_selected : R.drawable.dot_unselected));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            holder.layoutDots.addView(dots[i], params);
        }

        holder.viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < imageCount; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(mContext,
                            i == position ? R.drawable.dot_selected : R.drawable.dot_unselected));
                }
            }
        });
    }

    private void setupClickListeners(ViewHolder holder, PostModel postModel) {
        holder.btnLike.setOnClickListener(v ->
                likeButton(postModel.getPostId(), postModel.getAuthor(), holder.btnLike));

        holder.txtName.setOnClickListener(v ->
                Utils.goToProfile(postModel.getAuthor(), mContext));

        holder.imgProfile.setOnClickListener(v ->
                Utils.goToProfile(postModel.getAuthor(), mContext));

        holder.btnComment.setOnClickListener(v ->
                showComments(postModel.getPostId(), postModel.getAuthor()));

        holder.btnMore.setOnClickListener(v ->
                moreDialog(postModel));
    }

    public void getPost(PostModel postModel,
                        ViewPager2 viewPagerImages,
                        TextView txtCaption,
                        ImageView imgProfile,
                        TextView txtName,
                        TextView txtUsername,
                        TextView txtUsernameCpt,
                        TextView txtDateAndTime) {

        // Image handling is now done by ViewPager adapter
        txtCaption.setText(postModel.getDescription());
        if (postModel.getDatePosted() != null) {
            txtDateAndTime.setText(postModel.getDatePosted());
        }

        authManager.GetDb().getReference().child("Users")
                .child(postModel.getAuthor())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String firstName = snapshot.child("Personal Information").child("FirstName").getValue(String.class);
                            String lastName = snapshot.child("Personal Information").child("LastName").getValue(String.class);
                            String profileIconURL = snapshot.child("Account Details").child("ProfileIconURL").getValue(String.class);
                            String username = snapshot.child("Account Details").child("Username").getValue(String.class);

                            if (profileIconURL != null && !profileIconURL.equals("null")) {
                                Picasso.get().load(profileIconURL).into(imgProfile);
                            } else {
                                imgProfile.setImageResource(R.drawable.default_icon);
                            }

                            txtName.setText(firstName + " " + lastName);
                            txtUsername.setText(username);
                            txtUsernameCpt.setText(username);
                        } catch (Exception e) {
                            Log.e("PostError", "Error processing user data: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PostError", "Error: " + error);
                    }
                });
    }

    public void likeButton(String PostId, String AuthorId, ImageButton btnLike){
        if(btnLike.getTag().equals("Like")){
            authManager.GetDb().getReference()
                    .child("Posts")
                    .child(PostId)
                    .child("Likes")
                    .child(loginManager.GetCurrentUser().getUid())
                    .setValue(true);

            Utils.notifyAuthor(PostId, AuthorId, mContext.getString(R.string.txtLiked), loginManager, authManager);
        }else{
            authManager.GetDb().getReference()
                    .child("Posts")
                    .child(PostId)
                    .child("Likes")
                    .child(loginManager.GetCurrentUser().getUid())
                    .removeValue();
        }

    }

    public void showComments(String postId, String authorId) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_comments);

        TextView txtNoComments = dialog.findViewById(R.id.txtNoComments);
        ImageView imgProfileIcon = dialog.findViewById(R.id.imgProfileIcon);
        EditText txtComment = dialog.findViewById(R.id.txtComment);
        ImageButton btnPost = dialog.findViewById(R.id.btnPost);

        authManager.GetDb().getReference().child("Users")
                .child(loginManager.GetCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String profileIconURL = snapshot.child("Account Details").child("ProfileIconURL").getValue(String.class);

                            if (profileIconURL != null && !profileIconURL.equals("null")) {
                                Picasso.get().load(profileIconURL).into(imgProfileIcon);
                            } else {
                                imgProfileIcon.setImageResource(R.drawable.default_icon);
                            }
                        } catch (Exception e) {
                            Log.e("PostAdapterError", "Error processing user data: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(txtComment.getText().toString())){
                    postComment(postId, txtComment, authorId);
                }else{
                    Toast.makeText(mContext, "Comment cannot be empty", Toast.LENGTH_LONG).show();
                }
            }
        });

        recyclerView = dialog.findViewById(R.id.resultComments);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        commentModelList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(mContext, commentModelList, postId);
        recyclerView.setAdapter(commentsAdapter);

        loadComments(postId, commentModelList, commentsAdapter, txtNoComments);

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void postComment(String postId, EditText text, String authorId){
        String commentId = authManager.GetDb().getReference().child("Posts").child(postId).child("Comments").push().getKey();

        HashMap<String, Object> map = new HashMap<>();
        map.put("Comment", text.getText().toString());
        map.put("Author", loginManager.GetCurrentUser().getUid());
        map.put("CommentId", commentId);
        map.put("PostId", postId);

        authManager.GetDb().getReference().child("Posts")
                .child(postId)
                .child("Comments")
                .child(commentId)
                .setValue(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            text.setText("");  // Clear the input field
                        } else {
                            Toast.makeText(mContext, "Something went wrong, please try again!", Toast.LENGTH_LONG).show();
                            Log.e("CommentPost", "Error: " + task.getException().getMessage());
                        }
                    }
                });

        Utils.notifyAuthor(postId, authorId, mContext.getString(R.string.txtCommented), loginManager, authManager);
    }

    public void loadComments(String postId, List<CommentModel> commentList, CommentsAdapter commentsAdapter, TextView text) {
        try{
            authManager.GetDb().getReference()
                    .child("Posts")
                    .child(postId)
                    .child("Comments")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            commentList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                /*String commentId = snapshot.getKey();
                                String comment = snapshot.child("Comment").getValue(String.class);
                                String author = snapshot.child("Author").getValue(String.class);

                                //CommentModel commentModel = new CommentModel(comment, author, postId, commentId);
                                CommentModel commentModel = dataSnapshot.getValue(CommentModel.class);
                                commentList.add(commentModel);*/

                                CommentModel commentModel = snapshot.getValue(CommentModel.class);
                                commentList.add(commentModel);

                            }

                            if (commentList.isEmpty()) {
                                text.setVisibility(View.VISIBLE);
                            } else {
                                text.setVisibility(View.GONE);
                            }

                            commentsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            text.setVisibility(View.VISIBLE);
                            text.setText("Error loading comments");
                        }
                    });
        }catch (Exception e){
            Log.e("CommentLoadError", "Error " + e);
        }
    }

    public void isLiked(String postID, ImageView imageView){
        authManager.GetDb().getReference()
                .child("Posts")
                .child(postID)
                .child("Likes")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(loginManager.GetCurrentUser().getUid()).exists()){
                            imageView.setImageResource(R.drawable.baseline_favorite_24);
                            imageView.setTag("Liked");
                        }else{
                            imageView.setImageResource(R.drawable.baseline_favorite_border_24);
                            imageView.setTag("Like");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void interactionCounter(String postID, TextView text, String path){
        authManager.GetDb().getReference()
                .child("Posts")
                .child(postID)
                .child(path)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getChildrenCount() > 0){
                            text.setText(snapshot.getChildrenCount() + "");
                        }else{
                            text.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void moreDialog(PostModel postModel){
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_comments_more);
        LinearLayout btnDelete = dialog.findViewById(R.id.btnDelete);
        LinearLayout btnReport = dialog.findViewById(R.id.btnReport);

        try{
            if(postModel.getAuthor().equals(loginManager.GetCurrentUser().getUid())){
                btnDelete.setVisibility(View.VISIBLE);
                btnReport.setVisibility(View.GONE);
            }else{
                btnDelete.setVisibility(View.GONE);
                btnReport.setVisibility(View.VISIBLE);
            }

        }catch (Exception e){
            Log.e("CommentsAdapter", "Error: " + e);
        }

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost(postModel);
                dialog.dismiss();
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportPost();
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void deletePost(PostModel postModel){
        authManager.GetDb().getReference().child("Posts")
                .child(postModel.getPostId())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(mContext, "Post deleted!", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void reportPost(){
        //REPORT POST HERE
    }
}
