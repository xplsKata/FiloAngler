package com.example.filoangler.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.R;
import com.example.filoangler.Model.UserModel;
import com.example.filoangler.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private Context mContext;
    private List<UserModel> mUsers;
    private boolean isFragment;
    private LoginManager loginManager;
    private FirebaseDatabase mDb;
    private String adapterStyle;
    AuthManager authManager = new AuthManager();


    public UserAdapter(Context mContext, List<UserModel> mUsers, boolean isFragment, String adapterStyle) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isFragment = isFragment;
        this.mDb = authManager.GetDb();
        this.adapterStyle = adapterStyle;
    }

    public UserAdapter(Context mContext, List<UserModel> mUsers, boolean isFragment) {
        this(mContext, mUsers, isFragment, "default");  // Use default style
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        loginManager = new LoginManager(mContext);//MIGHT ERROR HERE NOT SURE
        loginManager.GetCurrentUser();

        UserModel userModel = mUsers.get(position);
        holder.btnFollow.setVisibility(View.VISIBLE);

        holder.Username.setText(userModel.getUsername());
        holder.Name.setText(userModel.getFirstName() + " " + userModel.getLastName());

        Picasso.get().load(userModel.getProfileIconURL()).placeholder(R.drawable.default_icon).into(holder.imgProfile);

        isFollowed(holder, userModel.getUserID(), holder.btnFollow);

        if(userModel.getUserID().equals(loginManager.GetCurrentUser().getUid())){
            holder.btnFollow.setVisibility(View.GONE);
        }

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.followUser(authManager, holder.btnFollow, loginManager, userModel, mContext);
            }
        });

        holder.Name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.goToProfile(userModel.getUserID(), mContext);
            }
        });

        holder.imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.goToProfile(userModel.getUserID(), mContext);
            }
        });

    }

    private void isFollowed(ViewHolder view, final String id, Button btnFollow) {
        DatabaseReference reference = authManager.GetDb().getReference().child("Users")
                .child(loginManager.GetCurrentUser().getUid())
                .child("Following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(id).exists()) {
                    btnFollow.setText(R.string.txtFollowing);

                    if (adapterStyle.equals("search")) {
                        // Special styling for search activity
                        btnFollow.setBackgroundColor(mContext.getResources().getColor(R.color.bgBlue));
                        btnFollow.setTextColor(mContext.getResources().getColor(R.color.white));
                    } else {
                        // Default styling for other fragments
                        btnFollow.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                        btnFollow.setTextColor(mContext.getResources().getColor(R.color.bgBlue));
                        view.Username.setTextColor(mContext.getResources().getColor(R.color.white));
                        view.Name.setTextColor(mContext.getResources().getColor(R.color.white));
                    }
                } else {
                    btnFollow.setText(R.string.txtFollow);

                    if (adapterStyle.equals("search")) {
                        // Special styling for search activity
                        btnFollow.setBackgroundColor(mContext.getResources().getColor(R.color.iconBlue));
                        btnFollow.setTextColor(mContext.getResources().getColor(R.color.white));
                    } else {
                        // Default styling for other fragments
                        btnFollow.setBackgroundColor(mContext.getResources().getColor(R.color.iconBlue));
                        btnFollow.setTextColor(mContext.getResources().getColor(R.color.white));
                        view.Username.setTextColor(mContext.getResources().getColor(R.color.white));
                        view.Name.setTextColor(mContext.getResources().getColor(R.color.white));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imgProfile;
        public TextView Username;
        public TextView Name;
        public Button btnFollow;


        public ViewHolder(@NonNull View itemView){
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgProfile);
            Username = itemView.findViewById(R.id.txtUsername);
            Name = itemView.findViewById(R.id.txtName);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }
    }

}
