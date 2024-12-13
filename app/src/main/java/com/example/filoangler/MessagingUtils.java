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
    private static final String MESSAGING_REF = "Messaging";
    private static final String CONVERSATIONS_REF = "Conversations";
    private static final String MESSAGES_REF = "Messages";

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private DatabaseReference messagingRef;

    private LoginManager loginManager;
    private AuthManager authManager;

    public MessagingUtils() {
        database = FirebaseDatabase.getInstance(BuildConfig.firebaseDatabaseApiKey);
        auth = FirebaseAuth.getInstance();
        messagingRef = database.getReference(MESSAGING_REF);
    }

    // Check if users are in each other's following list
    public Task<Boolean> canInitiateChat(String currentUserId, String otherUserId) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        // Reference to current user's Following node
        DatabaseReference currentUserFollowingRef = FirebaseDatabase.getInstance()
                .getReference("Users")
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
        String conversationId = messagingRef
                .child(CONVERSATIONS_REF)
                .child(currentUserId)
                .child(otherUserId)
                .push()
                .getKey();

        // Create conversation entries for both users
        Map<String, Object> conversationUpdates = new HashMap<>();
        conversationUpdates.put("/" + CONVERSATIONS_REF + "/" + currentUserId + "/" + otherUserId + "/conversationId", conversationId);
        conversationUpdates.put("/" + CONVERSATIONS_REF + "/" + otherUserId + "/" + currentUserId + "/conversationId", conversationId);

        messagingRef.updateChildren(conversationUpdates)
                .addOnSuccessListener(aVoid -> taskCompletionSource.setResult(conversationId))
                .addOnFailureListener(taskCompletionSource::setException);

        return taskCompletionSource.getTask();
    }

    // Send a message
    public Task<Void> sendMessage(String conversationId, Message message) {
        DatabaseReference messagesRef = messagingRef.child(MESSAGES_REF).child(conversationId);
        String messageId = messagesRef.push().getKey();

        // Prepare message data
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", message.getSenderId());
        messageData.put("receiverId", message.getReceiverId());
        messageData.put("messageText", message.getMessageText());
        messageData.put("timestamp", message.getTimestamp());

        // Update last message in conversation
        Map<String, Object> conversationUpdate = new HashMap<>();
        conversationUpdate.put("/Conversations/" + message.getSenderId() + "/" + message.getReceiverId() + "/lastMessage", message.getMessageText());
        conversationUpdate.put("/Conversations/" + message.getSenderId() + "/" + message.getReceiverId() + "/lastMessageTimestamp", message.getTimestamp());

        // Batch the updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("/" + MESSAGES_REF + "/" + conversationId + "/" + messageId, messageData);
        updates.putAll(conversationUpdate);

        return messagingRef.updateChildren(updates);
    }

    // Fetch conversations for a user
    public Task<List<Conversation>> getUserConversations(String currentUserId) {
        TaskCompletionSource<List<Conversation>> taskCompletionSource = new TaskCompletionSource<>();
        List<Conversation> conversations = new ArrayList<>();

        DatabaseReference conversationsRef = messagingRef
                .child(CONVERSATIONS_REF)
                .child(currentUserId);

        conversationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot conversationSnapshot : snapshot.getChildren()) {
                    String otherUserId = conversationSnapshot.getKey();

                    // Here you'd typically fetch additional user details
                    // This is a placeholder and would need to be expanded
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
