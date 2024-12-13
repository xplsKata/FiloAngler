package com.example.filoangler.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.Message;
import com.example.filoangler.R;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;
    private DatabaseReference usersRef;

    private LoginManager loginManager;
    private AuthManager authManager;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.loginManager = new LoginManager(context);
        this.authManager = new AuthManager();

        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.usersRef = authManager.GetDb().getReference().child("Users");
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);

        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_message, parent, false);

        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(view);
        } else {
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder instanceof SentMessageViewHolder) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.messageTextView.setText(message.getMessageText());
            sentHolder.messageTimeTextView.setText(formatTimestamp(message.getTimestamp()));
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.messageTextView.setText(message.getMessageText());
            receivedHolder.messageTimeTextView.setText(formatTimestamp(message.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // ViewHolder for Sent Messages
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        CardView messageCard;
        TextView messageTextView;
        TextView messageTimeTextView;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageCard = itemView.findViewById(R.id.sent_message_card);
            messageTextView = itemView.findViewById(R.id.sent_message_text);
            messageTimeTextView = itemView.findViewById(R.id.sent_message_time);
        }
    }

    // ViewHolder for Received Messages
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        CardView messageCard;
        TextView messageTextView;
        TextView messageTimeTextView;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageCard = itemView.findViewById(R.id.received_message_card);
            messageTextView = itemView.findViewById(R.id.received_message_text);
            messageTimeTextView = itemView.findViewById(R.id.received_message_time);
        }
    }

    // Method to add a new message to the list
    public void addMessage(Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }
}