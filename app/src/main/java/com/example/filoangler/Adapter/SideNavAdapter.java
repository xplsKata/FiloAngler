package com.example.filoangler.Adapter;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.R;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class SideNavAdapter {
    private Context mContext;

    public SideNavAdapter(){

    }

    public SideNavAdapter(Context context){
        this.mContext = context;
    }

    public void setSideNavUser(TextView Name, TextView Username, ImageView imgProfileIcon, LoginManager loginManager, AuthManager authManager){

        authManager.GetDb().getReference().child("Users")
                .child(loginManager.GetCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            String firstName = snapshot.child("Personal Information").child("FirstName").getValue(String.class);
                            String lastName = snapshot.child("Personal Information").child("LastName").getValue(String.class);
                            String profileIconURL = snapshot.child("Personal Information").child("ProfileIconURL").getValue(String.class);
                            String username = snapshot.child("Account Details").child("Username").getValue(String.class);

                            if (profileIconURL != null && !profileIconURL.equals("null")) {
                                Picasso.get().load(profileIconURL).into(imgProfileIcon);
                            } else {
                                imgProfileIcon.setImageResource(R.mipmap.ic_launcher);
                            }

                            Name.setText(firstName + " " + lastName);
                            Username.setText(username);
                        } catch (Exception e) {
                            Log.e("SideNav", "Error processing user data: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("SideNav", "Error processing user data for sidebar");
                    }
                });

    }

}
