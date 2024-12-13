package com.example.filoangler.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.Conversation;
import com.example.filoangler.R;
import com.example.filoangler.activities.ChatActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private Context context;
    private List<Conversation> conversationList;
    private List<Conversation> conversationListFull;
    private DatabaseReference usersRef;
    private String currentUserId;

    private LoginManager loginManager;
    private AuthManager authManager;

    public ConversationAdapter(Context context, List<Conversation> conversationList, String currentUserId) {
        this.loginManager = new LoginManager(context);
        this.authManager = new AuthManager();

        this.context = context;
        this.conversationList = conversationList;
        this.conversationListFull = new ArrayList<>(conversationList);
        this.currentUserId = currentUserId;
        this.usersRef = authManager.GetDb().getReference().child("Users");
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        // Determine the other user's ID (not the current user)
        String otherUserId = conversation.getUserId();

        // Fetch user details
        usersRef.child(otherUserId).child("Account Details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Set conversation name (username)
                    String username = snapshot.child("Username").getValue(String.class);
                    holder.nameTextView.setText(username != null ? username : "Unknown User");

                    // Load profile picture
                    String profilePicUrl = snapshot.child("ProfileIconURL").getValue(String.class);
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Picasso.get().load(profilePicUrl).into(holder.profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors
                holder.nameTextView.setText("Unknown User");
            }
        });

        // Set last message
        holder.lastMessageTextView.setText(conversation.getLastMessage());

        // Set timestamp
        holder.timestampTextView.setText(formatTimestamp(conversation.getLastMessageTimestamp()));

        // Set unread count
        if (conversation.getUnreadCount() > 0) {
            holder.unreadCountTextView.setText(String.valueOf(conversation.getUnreadCount()));
            holder.unreadCountTextView.setVisibility(View.VISIBLE);
        } else {
            holder.unreadCountTextView.setVisibility(View.GONE);
        }

        // Set click listener to open chat
        holder.itemView.setOnClickListener(v -> {
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("CONVERSATION_ID", conversation.getConversationId());
            chatIntent.putExtra("USER_ID", otherUserId);
            context.startActivity(chatIntent);
            Log.e("ConversationId", conversation.getConversationId());
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public void filterList(List<Conversation> filteredList) {
        conversationList = filteredList;
        notifyDataSetChanged();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profileImageView;
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView timestampTextView;
        TextView unreadCountTextView;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.conversation_profile_pic);
            nameTextView = itemView.findViewById(R.id.conversation_name);
            lastMessageTextView = itemView.findViewById(R.id.conversation_last_message);
            timestampTextView = itemView.findViewById(R.id.conversation_timestamp);
            unreadCountTextView = itemView.findViewById(R.id.conversation_unread_count);
        }
    }
}