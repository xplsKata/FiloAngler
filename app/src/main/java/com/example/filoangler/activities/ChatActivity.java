package com.example.filoangler.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Adapter.MessageAdapter;
import com.example.filoangler.BuildConfig;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.MessagingUtils;
import com.example.filoangler.Model.Message;
import com.example.filoangler.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private AuthManager authManager;

    private RecyclerView messagesRecyclerView;
    private EditText messageInputEditText;
    private ImageButton btnSend;
    private ImageButton btnBack;
    private TextView userNameTextView;
    private ShapeableImageView profilePicImageView;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private MessagingUtils messagingUtils;

    private String currentUserId;
    private String otherUserId;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase Auth and Messaging Utils
        FirebaseAuth auth = FirebaseAuth.getInstance();
        authManager = new AuthManager();
        currentUserId = auth.getCurrentUser().getUid();
        messagingUtils = new MessagingUtils();

        // Initialize Views
        initializeViews();

        // Get Intent Extras
        extractIntentExtras();

        // Check if chat is allowed
        checkChatEligibility();
    }

    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInputEditText = findViewById(R.id.edit_message_input);
        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.btn_back);
        userNameTextView = findViewById(R.id.chat_user_name);
        profilePicImageView = findViewById(R.id.chat_profile_pic);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Send button click listener
        btnSend.setOnClickListener(v -> sendMessage());

        // Back button click listener
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void extractIntentExtras() {
        Intent intent = getIntent();
        otherUserId = intent.getStringExtra("USER_ID");

        // Fetch and set user details
        fetchUserDetails();
    }

    private void checkChatEligibility() {
        messagingUtils.canInitiateChat(currentUserId, otherUserId)
                .addOnSuccessListener(canChat -> {
                    if (canChat) {
                        // Create a new conversation
                        createNewConversation();
                    } else {
                        // Cannot chat - show error and finish activity
                        Toast.makeText(this, "You can only chat with users you follow", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking chat eligibility", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void fetchUserDetails() {
        DatabaseReference userRef = authManager.GetDb()
                .getReference("Users")
                .child(otherUserId)
                .child("Account Details");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("Username").getValue(String.class);
                    String profilePicUrl = snapshot.child("ProfileIconURL").getValue(String.class);

                    userNameTextView.setText(username);
                    Picasso.get().load(profilePicUrl).into(profilePicImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch user details", error.toException());
            }
        });
    }

    private void createNewConversation() {
        DatabaseReference conversationsRef = FirebaseDatabase.getInstance(BuildConfig.firebaseDatabaseApiKey)
                .getReference("Users")
                .child(currentUserId)
                .child("Inbox")
                .child(otherUserId);

        conversationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    // Conversation already exists, use the existing conversation ID
                    for (DataSnapshot conversationSnapshot : snapshot.getChildren()) {
                        if (conversationSnapshot.getKey() != null &&
                                !conversationSnapshot.getKey().equals("Messages")) {
                            conversationId = conversationSnapshot.getKey();
                            setupMessageInput();
                            loadMessages();
                            return;
                        }
                    }
                }

                // If no existing conversation is found, create a new one
                messagingUtils.createConversation(currentUserId, otherUserId)
                        .addOnSuccessListener(newConversationId -> {
                            conversationId = newConversationId;
                            setupMessageInput();
                            loadMessages();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ChatActivity.this,
                                    "Failed to create conversation",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this,
                        "Error checking existing conversations",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupMessageInput() {
        btnSend.setEnabled(true);
    }

    private void sendMessage() {
        String messageText = messageInputEditText.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        // Send message via messaging utils
        messagingUtils.sendMessage(currentUserId, otherUserId, messageText)
                .addOnSuccessListener(aVoid -> {
                    // Clear input after successful send
                    messageInputEditText.setText("");

                    // Scroll to bottom
                    messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMessages() {
        if (TextUtils.isEmpty(conversationId)) {
            return;
        }

        // Reference to messages in this conversation
        DatabaseReference messagesRef = FirebaseDatabase.getInstance(BuildConfig.firebaseDatabaseApiKey)
                .getReference("Users")
                .child(currentUserId)
                .child("Inbox")
                .child(otherUserId)
                .child("Messages")
                .child(conversationId);

        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    // Determine message type based on sender
                    message.setMessageType(
                            message.getSenderId().equals(currentUserId)
                                    ? Message.MessageType.SENT
                                    : Message.MessageType.RECEIVED
                    );

                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading messages", error.toException());
            }
        });
    }
}