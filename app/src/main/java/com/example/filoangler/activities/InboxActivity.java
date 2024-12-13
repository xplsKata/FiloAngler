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

public class InboxActivity extends AppCompatActivity {
    private RecyclerView conversationsRecyclerView;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversationList;
    private MessagingUtils messagingUtils;
    private EditText searchContactsEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        // Initialize views
        conversationsRecyclerView = findViewById(R.id.conversations_recycler_view);
        searchContactsEditText = findViewById(R.id.search_contacts);

        // Initialize messaging utilities
        messagingUtils = new MessagingUtils();

        // Setup RecyclerView
        conversationList = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(this, conversationList);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationsRecyclerView.setAdapter(conversationAdapter);

        // Fetch user conversations
        fetchUserConversations();

        // Setup search functionality
        setupSearchListener();
    }

    private void fetchUserConversations() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        messagingUtils.getUserConversations(currentUserId)
                .addOnSuccessListener(conversations -> {
                    conversationList.clear();
                    conversationList.addAll(conversations);
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
        List<Conversation> filteredList = new ArrayList<>();
        for (Conversation conversation : conversationList) {
            if (conversation.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(conversation);
            }
        }
        conversationAdapter.filterList(filteredList);
    }
}