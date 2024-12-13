package com.example.filoangler;

import androidx.annotation.NonNull;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.Conversation;
import com.example.filoangler.Model.Message;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagingUtils {
    private static final String USERS_REF = "Users";
    private static final String INBOX_REF = "Inbox";
    private static final String MESSAGES_REF = "Messages";

    private FirebaseDatabase database;
    private FirebaseAuth auth;

    public MessagingUtils() {
        database = FirebaseDatabase.getInstance(BuildConfig.firebaseDatabaseApiKey);
        auth = FirebaseAuth.getInstance();
    }

    // Check if users are in each other's following list
    public Task<Boolean> canInitiateChat(String currentUserId, String otherUserId) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        // Reference to current user's Following node
        DatabaseReference currentUserFollowingRef = database.getReference(USERS_REF)
                .child(currentUserId)
                .child("Following");

        currentUserFollowingRef.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean canChat = snapshot.exists();
                taskCompletionSource.setResult(canChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                taskCompletionSource.setException(error.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    // Create a new conversation
    public Task<String> createConversation(String currentUserId, String otherUserId) {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        // Generate a unique conversation ID
        String conversationId = database.getReference(USERS_REF)
                .child(currentUserId)
                .child(INBOX_REF)
                .child(otherUserId)
                .push()
                .getKey();

        // Prepare updates for both users' inboxes
        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + USERS_REF + "/" + currentUserId + "/" + INBOX_REF + "/" + otherUserId + "/conversationId", conversationId);
        updates.put("/" + USERS_REF + "/" + otherUserId + "/" + INBOX_REF + "/" + currentUserId + "/conversationId", conversationId);

        database.getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(conversationId))
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    // Send a message
    public Task<Void> sendMessage(String currentUserId, String otherUserId, String messageText) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        // Get a reference to the conversation
        DatabaseReference inboxRef = database.getReference(USERS_REF)
                .child(currentUserId)
                .child(INBOX_REF)
                .child(otherUserId);

        inboxRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String conversationId = snapshot.child("conversationId").getValue(String.class);

                if (conversationId == null) {
                    // If no conversation exists, create one first
                    createConversation(currentUserId, otherUserId)
                            .addOnSuccessListener(newConversationId -> {
                                sendMessageToConversation(currentUserId, otherUserId, newConversationId, messageText)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                taskCompletionSource.setResult(null);
                                            } else {
                                                taskCompletionSource.setException(task.getException());
                                            }
                                        });
                            })
                            .addOnFailureListener(taskCompletionSource::setException);
                } else {
                    // Send message to existing conversation
                    sendMessageToConversation(currentUserId, otherUserId, conversationId, messageText)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    taskCompletionSource.setResult(null);
                                } else {
                                    taskCompletionSource.setException(task.getException());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                taskCompletionSource.setException(error.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    // Helper method to send message to a specific conversation
    private Task<Void> sendMessageToConversation(String currentUserId, String otherUserId, String conversationId, String messageText) {
        long timestamp = System.currentTimeMillis();

        // Prepare message data
        DatabaseReference messagesRef = database.getReference(USERS_REF)
                .child(currentUserId)
                .child(INBOX_REF)
                .child(otherUserId)
                .child(MESSAGES_REF)
                .child(conversationId);

        // Generate a unique message ID
        String messageId = messagesRef.push().getKey();

        // Prepare message data
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageText", messageText);
        messageData.put("senderId", currentUserId);
        messageData.put("receiverId", otherUserId);
        messageData.put("timestamp", timestamp);

        // Update last message and timestamp for both users
        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + USERS_REF + "/" + currentUserId + "/" + INBOX_REF + "/" + otherUserId + "/lastMessage", messageText);
        updates.put("/" + USERS_REF + "/" + currentUserId + "/" + INBOX_REF + "/" + otherUserId + "/lastMessageTimestamp", timestamp);
        updates.put("/" + USERS_REF + "/" + otherUserId + "/" + INBOX_REF + "/" + currentUserId + "/lastMessage", messageText);
        updates.put("/" + USERS_REF + "/" + otherUserId + "/" + INBOX_REF + "/" + currentUserId + "/lastMessageTimestamp", timestamp);

        // Add the message
        updates.put("/" + USERS_REF + "/" + currentUserId + "/" + INBOX_REF + "/" + otherUserId + "/" + MESSAGES_REF + "/" + conversationId + "/" + messageId, messageData);
        updates.put("/" + USERS_REF + "/" + otherUserId + "/" + INBOX_REF + "/" + currentUserId + "/" + MESSAGES_REF + "/" + conversationId + "/" + messageId, messageData);

        return database.getReference().updateChildren(updates);
    }

    // Fetch conversations for a user
    public Task<List<Conversation>> getUserConversations(String currentUserId) {
        TaskCompletionSource<List<Conversation>> taskCompletionSource = new TaskCompletionSource<>();
        List<Conversation> conversations = new ArrayList<>();

        DatabaseReference inboxRef = database.getReference(USERS_REF)
                .child(currentUserId)
                .child(INBOX_REF);

        inboxRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot conversationSnapshot : snapshot.getChildren()) {
                    String otherUserId = conversationSnapshot.getKey();
                    Conversation conversation = new Conversation(otherUserId, "", "");

                    conversation.setConversationId(conversationSnapshot.child("conversationId").getValue(String.class));
                    conversation.setLastMessage(conversationSnapshot.child("lastMessage").getValue(String.class));
                    conversation.setLastMessageTimestamp(conversationSnapshot.child("lastMessageTimestamp").getValue(Long.class));

                    conversations.add(conversation);
                }
                taskCompletionSource.setResult(conversations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                taskCompletionSource.setException(error.toException());
            }
        });

        return taskCompletionSource.getTask();
    }
}