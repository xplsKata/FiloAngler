package com.example.filoangler.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.Conversation;
import com.example.filoangler.R;
import com.example.filoangler.activities.ChatActivity;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {
    private Context context;
    private List<Conversation> conversationList;
    private List<Conversation> conversationListFull;

    public ConversationAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
        this.conversationListFull = new ArrayList<>(conversationList);
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

        // Set conversation name (username)
        holder.nameTextView.setText(conversation.getUsername());

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

        // Set profile picture (you'll need to implement image loading, e.g., with Glide)
        // Glide.with(context).load(conversation.getProfilePicUrl()).into(holder.profileImageView);

        // Set click listener to open chat
        holder.itemView.setOnClickListener(v -> {
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("CONVERSATION_ID", conversation.getConversationId());
            chatIntent.putExtra("USER_ID", conversation.getUserId());
            context.startActivity(chatIntent);
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
        // Implement timestamp formatting logic
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
