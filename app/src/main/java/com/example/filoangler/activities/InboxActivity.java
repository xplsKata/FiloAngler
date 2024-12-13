package com.example.filoangler.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Adapter.ConversationAdapter;
import com.example.filoangler.MessagingUtils;
import com.example.filoangler.Model.Conversation;
import com.example.filoangler.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InboxActivity extends AppCompatActivity {
    private RecyclerView conversationsRecyclerView;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversationList;
    private List<Conversation> conversationListFull;
    private MessagingUtils messagingUtils;
    private EditText searchContactsEditText;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        // Initialize current user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        conversationsRecyclerView = findViewById(R.id.conversations_recycler_view);
        searchContactsEditText = findViewById(R.id.search_contacts);

        // Initialize messaging utilities
        messagingUtils = new MessagingUtils();

        // Setup RecyclerView
        conversationList = new ArrayList<>();
        conversationListFull = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(this, conversationList, currentUserId);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationsRecyclerView.setAdapter(conversationAdapter);

        // Fetch user conversations
        fetchUserConversations();

        // Setup search functionality
        setupSearchListener();
    }

    private void fetchUserConversations() {
        messagingUtils.getUserConversations(currentUserId)
                .addOnSuccessListener(conversations -> {
                    conversationList.clear();
                    conversationListFull.clear();
                    conversationList.addAll(conversations);
                    conversationListFull.addAll(conversations);
                    conversationAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load conversations", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupSearchListener() {
        searchContactsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterConversations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterConversations(String query) {
        if (query.isEmpty()) {
            conversationList.clear();
            conversationList.addAll(conversationListFull);
            conversationAdapter.notifyDataSetChanged();
            return;
        }

        // Using Java 8 Stream API for filtering
        List<Conversation> filteredList = conversationListFull.stream()
                .filter(conversation -> {
                    // Assuming the adapter will fetch and set the username from Firebase
                    // You might need to adjust this based on exactly how username is retrieved
                    return conversation.getUserId() != null &&
                            conversation.getUserId().toLowerCase().contains(query.toLowerCase());
                })
                .collect(Collectors.toList());

        conversationAdapter.filterList(filteredList);
    }
}