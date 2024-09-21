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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Dialog.FollowListDialog;
import com.example.filoangler.Dialog.PostDetailsDialog;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.NotificationModel;
import com.example.filoangler.Model.PostModel;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.example.filoangler.activities.UserProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder>{

    private Context mContext;
    private List<NotificationModel> mNotifications;
    private AuthManager authManager;
    private LoginManager loginManager;

    public NotificationsAdapter(Context mContext, List<NotificationModel> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_notifications, parent, false);

        return new NotificationsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        authManager = new AuthManager();
        loginManager = new LoginManager(mContext);
        NotificationModel notificationModel = mNotifications.get(position);

        getNotificationInfo(holder.imgProfileIcon,
                holder.txtName,
                notificationModel.getUserId(),
                holder.txtDescription,
                notificationModel.getDescription());

        try{
            if(notificationModel.getisPost()){
                holder.imgPostIcon.setVisibility(View.VISIBLE);
                getPostImage(holder.imgPostIcon, notificationModel.getPostId());
            }else{
                holder.imgPostIcon.setVisibility(View.INVISIBLE);
            }
        }catch (Exception e){
            Log.e("NotificationsAdapter", "Error: " + e);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notificationModel.getisPost()){
                    if(notificationModel.getisPost()){
                        showPostDetailsDialog(notificationModel.getPostId());
                    }else{
                        Utils.goToProfile(notificationModel.getUserId(), mContext);
                    }
                }
            }
        });

    }

    private void showPostDetailsDialog(String postId) {

        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_post_dialog);

        if(postId != null){
            PostDetailsDialog postDetailsDialog = new PostDetailsDialog(mContext, authManager, postId);
            postDetailsDialog.getPost(dialog);
        }

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }
    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imgProfileIcon, imgPostIcon;
        public TextView txtName, txtDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfileIcon = itemView.findViewById(R.id.imgProfileIcon);
            imgPostIcon = itemView.findViewById(R.id.imgPostIcon);
            txtName = itemView.findViewById(R.id.txtName);
            txtDescription = itemView.findViewById(R.id.txtDescription);
        }
    }

    private void getNotificationInfo(ImageView imgProfileIcon,
                                     TextView txtName, String UserId,
                                     TextView txtDescription, String Description) {

        authManager.GetDb().getReference().child("Users")
                .child(UserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileIconURL = snapshot.child("Personal Information").child("ProfileIconURL").getValue(String.class);
                        String firstName = snapshot.child("Personal Information").child("FirstName").getValue(String.class);
                        String lastName = snapshot.child("Personal Information").child("LastName").getValue(String.class);

                        if (profileIconURL != null && !profileIconURL.equals("null")) {
                            Picasso.get().load(profileIconURL).into(imgProfileIcon);
                        } else {
                            imgProfileIcon.setImageResource(R.mipmap.ic_launcher);
                        }

                        txtName.setText(firstName + " " + lastName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        txtDescription.setText(Description);
    }

    private void getPostImage(ImageView imgPostIcon, String postId){

        authManager.GetDb().getReference().child("Posts")
                .child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PostModel postModel = snapshot.getValue(PostModel.class);
                        Picasso.get().load(postModel.getImageURL()).placeholder(R.drawable.logotemp).into(imgPostIcon);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}
