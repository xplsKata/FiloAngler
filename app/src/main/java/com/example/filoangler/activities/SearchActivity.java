 package com.example.filoangler.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.example.filoangler.Adapter.UserAdapter;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.R;
import com.example.filoangler.Model.UserModel;
import com.example.filoangler.fragments.SearchResultFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

 public class SearchActivity extends AppCompatActivity {

     private boolean isSearching = false;

     private RecyclerView recyclerView;
     private EditText search_bar;
     private List<UserModel> mUsers;
     private UserAdapter userAdapter;
     private FirebaseDatabase mDb;
     private AuthManager authManager = new AuthManager();
     private ImageButton btnBack;
     private FrameLayout frameSearchResult;

     public SearchActivity(){
         this.mDb = authManager.GetDb();
     }

     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search);

         try {
             // Initialize views
             recyclerView = findViewById(R.id.result_users);
             frameSearchResult = findViewById(R.id.frameSearchResult);
             search_bar = findViewById(R.id.txtSearch);
             btnBack = findViewById(R.id.btnBack);

             // Initial visibility setup
             recyclerView.setVisibility(View.VISIBLE);
             frameSearchResult.setVisibility(View.GONE);

             // Setup RecyclerView
             recyclerView.setHasFixedSize(true);
             recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

             // Initialize lists and adapters
             mUsers = new ArrayList<>();
             userAdapter = new UserAdapter(SearchActivity.this, mUsers, false, "search");
             recyclerView.setAdapter(userAdapter);

             // Call readUsers to populate initial list
             readUsers();

             // Setup search bar listener
             search_bar.addTextChangedListener(new TextWatcher() {
                 @Override
                 public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                 }

                 @Override
                 public void onTextChanged(CharSequence s, int start, int before, int count) {
                     if (TextUtils.isEmpty(s.toString())) {
                         // Reset to initial state
                         recyclerView.setVisibility(View.VISIBLE);
                         frameSearchResult.setVisibility(View.GONE);
                         isSearching = false;
                         readUsers();
                     } else {
                         // Show suggestions while typing
                         searchUser(s.toString());
                     }
                 }

                 @Override
                 public void afterTextChanged(Editable s) {
                 }
             });

             // Add key listener for search activation
             search_bar.setOnEditorActionListener((v, actionId, event) -> {
                 if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                     performSearch(search_bar.getText().toString());
                     return true;
                 }
                 return false;
             });

         } catch (Exception e) {
             Log.e("SEARCH_Error", "onCreate: " + e);
         }

         btnBack.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 finish();
             }
         });
     }

     private void performSearch(String searchQuery) {
         if (!TextUtils.isEmpty(searchQuery)) {
             // Hide keyboard
             InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(search_bar.getWindowToken(), 0);

             // Update visibility
             recyclerView.setVisibility(View.GONE);
             frameSearchResult.setVisibility(View.VISIBLE);
             isSearching = true;

             // Create and show SearchResultFragment
             SearchResultFragment searchResultFragment = new SearchResultFragment();
             Bundle args = new Bundle();
             args.putString("searchQuery", searchQuery);
             searchResultFragment.setArguments(args);

             getSupportFragmentManager()
                     .beginTransaction()
                     .replace(R.id.frameSearchResult, searchResultFragment)
                     .addToBackStack(null)
                     .commit();
         }
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

     public void onBackPressed() {
         if (isSearching) {
             // Reset to initial state
             recyclerView.setVisibility(View.VISIBLE);
             frameSearchResult.setVisibility(View.GONE);
             isSearching = false;
             search_bar.setText("");
             readUsers();

             // Clear fragment backstack
             getSupportFragmentManager().popBackStack();
         } else {
             super.onBackPressed();
         }
     }
 }