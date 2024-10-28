 package com.example.filoangler.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;

import com.example.filoangler.Adapter.UserAdapter;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.R;
import com.example.filoangler.Model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

 public class SearchActivity extends AppCompatActivity {

     private RecyclerView recyclerView;
     private SocialAutoCompleteTextView search_bar;
     private List<UserModel> mUsers;
     private UserAdapter userAdapter;
     private FirebaseDatabase mDb;
     private AuthManager authManager = new AuthManager();

     public SearchActivity(){
         this.mDb = authManager.GetDb();
     }

     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search);

         try {
             recyclerView = findViewById(R.id.result_users);
             recyclerView.setHasFixedSize(true);
             recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

             // Initialize mUsers
             mUsers = new ArrayList<>();

             // Initialize UserAdapter
             userAdapter = new UserAdapter(SearchActivity.this, mUsers, false, "search");
             recyclerView.setAdapter(userAdapter);

             search_bar = findViewById(R.id.txtSearch);

             // Call readUsers to populate the list
             readUsers();

             search_bar.addTextChangedListener(new TextWatcher() {
                 @Override
                 public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                 }

                 @Override
                 public void onTextChanged(CharSequence s, int start, int before, int count) {
                     if(TextUtils.isEmpty(search_bar.getText().toString())){
                         readUsers();
                     }else{
                         searchUser(s.toString());
                     }
                 }

                 @Override
                 public void afterTextChanged(Editable s) {

                 }
             });

         } catch(Exception e) {
             Log.e("SEARCH_Error", "onCreate: " + e);
         }

         //THIS STILL LACKS THE FEATURE OF POST SEARCHING WHEN ENTERED
     }

     private void readUsers() {
         try {
             DatabaseReference reference = mDb.getReference().child("Users");
             reference.addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot snapshot) {
                     if(TextUtils.isEmpty(search_bar.getText().toString())) {
                         try {
                                loadUsers(snapshot);
                                userAdapter.notifyDataSetChanged();
                         } catch(Exception e) {
                             Log.e("SEARCH_Error", "Data parsing error: " + e.getMessage());
                         }
                     }
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {
                     Log.e("SEARCH_Error", "Database error: " + error.getMessage());
                 }
             });
         } catch (Exception e) {
             Log.e("SEARCH", "readUsers error: " + e.getMessage());
         }
     }

     private void searchUser(String search){
         Query query = mDb.getReference().child("Users")
                 .orderByChild("Account Details/Username")
                 .startAt(search)
                 .endAt(search + "\uf8ff");

         try{
             query.addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try{
                        loadUsers(snapshot);
                        userAdapter.notifyDataSetChanged();
                    }catch (Exception e){
                        Log.e("SEARCH_Error", "Data parsing error: " + e.getMessage());
                    }
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {

                 }
             });
         }catch(Exception e){
             Log.e("SEARCH_Error", "Error in searchUser" + e.getMessage());
         }
     }

     private void loadUsers(DataSnapshot snapshot){
         mUsers.clear();
         for(DataSnapshot userSnapshot : snapshot.getChildren()) {
             String userId = userSnapshot.getKey();
             DataSnapshot personalInfoSnapshot = userSnapshot.child("Personal Information");
             DataSnapshot accountDetailsSnapshot = userSnapshot.child("Account Details");

             UserModel userModel = new UserModel();

             userModel.setFirstName(personalInfoSnapshot.child("FirstName").getValue(String.class));
             userModel.setLastName(personalInfoSnapshot.child("LastName").getValue(String.class));

             userModel.setUserID(accountDetailsSnapshot.child("UserID").getValue(String.class));
             userModel.setUsername(accountDetailsSnapshot.child("Username").getValue(String.class));
             userModel.setProfileIconURL(accountDetailsSnapshot.child("ProfileIconURL").getValue(String.class));
             userModel.setAnglerStatus(accountDetailsSnapshot.child("AnglerStatus").getValue(String.class));

             if (userModel.getUserID() != null) {
                 mUsers.add(userModel);
                 Log.e("SEARCH_Error", "Added user: " + userModel.getUsername());
             }
         }
     }
 }