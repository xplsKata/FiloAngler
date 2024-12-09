package com.example.filoangler.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Dialog.UserFeedbackDialog;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.CommentModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

    private Context mContext;
    private List<CommentModel> mComments;
    private LoginManager loginManager;
    private AuthManager authManager;

    private String PostId;

    public CommentsAdapter(Context mContext, List<CommentModel> mComments, String postId) {
        this.mContext = mContext;
        this.mComments = mComments;
        PostId = postId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_comments, parent, false);

        return new CommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        authManager = new AuthManager();
        loginManager = new LoginManager(mContext);
        CommentModel commentModel = mComments.get(position);

        holder.txtComment.setText(commentModel.getComment());

        authManager.GetDb().getReference().child("Users")
                .child(commentModel.getAuthor())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String firstName = snapshot.child("Personal Information").child("FirstName").getValue(String.class);
                            String lastName = snapshot.child("Personal Information").child("LastName").getValue(String.class);
                            String profileIconURL = snapshot.child("Account Details").child("ProfileIconURL").getValue(String.class);

                            if (profileIconURL != null && !profileIconURL.equals("null")) {
                                Picasso.get().load(profileIconURL).into(holder.imgProfileIcon);
                            } else {
                                holder.imgProfileIcon.setImageResource(R.drawable.default_icon);
                            }

                            holder.txtName.setText(firstName + " " + lastName);
                        } catch (Exception e) {
                            Log.e("CommentError", "Error processing user data: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        isLiked(commentModel.getCommentId(), holder.btnLike, PostId);
        interactionCounter(PostId, holder.txtLikesAmount, commentModel.getCommentId());

        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(holder.btnLike.getTag().equals("Like")){
                        authManager.GetDb().getReference()
                                .child("Posts")
                                .child(PostId)
                                .child("Comments")
                                .child(commentModel.getCommentId())
                                .child("Likes")
                                .child(loginManager.GetCurrentUser().getUid())
                                .setValue(true);
                    }else{
                        authManager.GetDb().getReference()
                                .child("Posts")
                                .child(PostId)
                                .child("Comments")
                                .child(commentModel.getCommentId())
                                .child("Likes")
                                .child(loginManager.GetCurrentUser().getUid())
                                .removeValue();
                    }
                }catch (Exception e){
                    Log.e("LikeError", "Error: " + e);
                }
            }
        });

        holder.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.goToProfile(commentModel.getAuthor(), mContext);
            }
        });

        holder.imgProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.goToProfile(commentModel.getAuthor(), mContext);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                moreDialog(commentModel);

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imgProfileIcon;

        public TextView txtName;
        public TextView txtComment;
        public TextView txtLikesAmount;

        private ImageButton btnLike;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfileIcon = itemView.findViewById(R.id.imgProfileIcon);
            txtName = itemView.findViewById(R.id.txtName);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtLikesAmount = itemView.findViewById(R.id.txtLikesAmount);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }


    public void moreDialog(CommentModel commentModel){
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_comments_more);
        LinearLayout btnDelete = dialog.findViewById(R.id.btnDelete);
        LinearLayout btnReport = dialog.findViewById(R.id.btnReport);

        try{
            if(commentModel.getAuthor().equals(loginManager.GetCurrentUser().getUid())){
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
                deletePost(commentModel);
                dialog.dismiss();
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportPost(commentModel.getAuthor(), commentModel.getCommentId());
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void deletePost(CommentModel commentModel){
        authManager.GetDb().getReference().child("Posts")
                .child(PostId)
                .child("Comments")
                .child(commentModel.getCommentId())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(mContext, "Comment deleted!", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void reportPost(String reportedUser, String commentId){
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_report_dialog);

        UserFeedbackDialog userFeedbackDialog = new UserFeedbackDialog(mContext, PostId, reportedUser, commentId,"isComment");
        userFeedbackDialog.getDialog(dialog);

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void isLiked(String commentId, ImageView imageView, String postId){
        authManager.GetDb().getReference()
                .child("Posts")
                .child(postId)
                .child("Comments")
                .child(commentId)
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

    private void interactionCounter(String postID, TextView text, String commentId){
        authManager.GetDb().getReference()
                .child("Posts")
                .child(postID)
                .child("Comments")
                .child(commentId)
                .child("Likes")
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

}
