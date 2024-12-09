package com.example.filoangler.Dialog;


import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Adapter.UserAdapter;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Model.UserModel;
import com.example.filoangler.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FollowListDialog {

    public String userId;
    public String title;
    private List<String> idList;

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> mUsers;

    private AuthManager authManager;

    private TextView label;

    public FollowListDialog(AuthManager authManager, String userId, String title){
        this.authManager = authManager;
        this.userId = userId;
        this.title = title;
    }

    public void getDialog(Dialog dialog, Context mContext){

        recyclerView = dialog.findViewById(R.id.recyclerView);
        label = dialog.findViewById(R.id.txtFollowLabel);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(mContext, mUsers, true);
        recyclerView.setAdapter(userAdapter);

        idList = new ArrayList<>();

        label.setText(title);
        label.setTextColor(ContextCompat.getColor(mContext, R.color.white));

        switch (title){
            case "Followers":
                getUserList("Followers");
                break;

            case "Following":
                getUserList("Following");
                break;
        }

    }

    private void getUserList(String branch){
        authManager.GetDb().getReference().child("Users")
                .child(userId)
                .child(branch)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        idList.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            idList.add(dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        showUsers();
    }

    private void showUsers() {
        authManager.GetDb().getReference().child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mUsers.clear();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String userId = userSnapshot.child("Account Details").child("UserID").getValue(String.class);
                            if (userId != null && idList.contains(userId)) {
                                DataSnapshot personalInfoSnapshot = userSnapshot.child("Personal Information");
                                DataSnapshot accountDetailsSnapshot = userSnapshot.child("Account Details");

                                UserModel userModel = new UserModel();

                                userModel.setFirstName(personalInfoSnapshot.child("FirstName").getValue(String.class));
                                userModel.setLastName(personalInfoSnapshot.child("LastName").getValue(String.class));

                                userModel.setUserID(userId);
                                userModel.setUsername(accountDetailsSnapshot.child("Username").getValue(String.class));
                                userModel.setProfileIconURL(accountDetailsSnapshot.child("ProfileIconURL").getValue(String.class));
                                userModel.setAnglerStatus(accountDetailsSnapshot.child("AnglerStatus").getValue(String.class));

                                mUsers.add(userModel);
                                Log.e("SEARCH_Error", "Added user: " + userModel.getUsername());
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("SEARCH_Error", "Database error: " + error.getMessage());
                    }
                });
    }

}
