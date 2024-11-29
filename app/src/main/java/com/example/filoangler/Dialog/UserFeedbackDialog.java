package com.example.filoangler.Dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Manager.StorageManager;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserFeedbackDialog {

    private String Id;
    private String classification;
    private String reportedUser;
    private String commentId;

    private boolean isFeedback;

    private Context mContext;
    private ImageView imgIcon;

    private Button btnSubmit;
    private Button btnCancel;

    private Dialog mDialog;

    private TextView txtDescription;
    private TextView txtReportDescription;

    private LoginManager loginManager;
    private AuthManager authManager;

    public UserFeedbackDialog(Context context) {
        this.mContext = context;
    }

    public UserFeedbackDialog(Context context, boolean isFeedback) {
        this.mContext = context;
        this.isFeedback = true;
    }

    public UserFeedbackDialog(Context context, String Id, String reportedUser,String classification) {
        this.mContext = context;
        this.Id = Id;
        this.classification = classification;
        this.reportedUser = reportedUser;
        this.isFeedback = false;
    }

    public UserFeedbackDialog(Context context, String Id, String reportedUser, String commentId, String classification) {
        this.mContext = context;
        this.Id = Id;
        this.classification = classification;
        this.reportedUser = reportedUser;
        this.commentId = commentId;
        this.isFeedback = false;
    }

    public void getDialog(Dialog dialog) {
        this.mDialog = dialog;
        loginManager = new LoginManager(mContext);
        authManager = new AuthManager();

        imgIcon = dialog.findViewById(R.id.imgIcon);
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnCancel = dialog.findViewById(R.id.btnCancel);
        txtDescription = dialog.findViewById(R.id.txtFeedback);
        txtReportDescription = dialog.findViewById(R.id.txtReportDescription);

        if(!isFeedback){
            txtReportDescription.setText("Thank you for keeping the community clean, may we know the reason of your report so we can help take action?");
        }else{
            txtReportDescription.setText("Enjoying the application or have a location in mind that you want added? Submit a feedback here and we'll greatly appreciate it!");
        }

        // Setup initial state
        Utils.loadImage(imgIcon, R.drawable.logotemp);
        setupListeners();
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (!isFeedback) {
                     // Get the reason for reporting from the description text view
                     String reason = txtDescription.getText().toString().trim();

                     // Validate the reason is not empty
                     if (reason.isEmpty()) {
                         Toast.makeText(mContext, "Please provide a reason for reporting", Toast.LENGTH_SHORT).show();
                         return;
                     }

                     // Get the current logged-in user's ID
                     String reportedBy = loginManager.GetCurrentUser().getUid();

                     // Create a unique report ID
                     String reportId = UUID.randomUUID().toString();

                     // Create the report object
                     Map<String, Object> reportData = new HashMap<>();
                     reportData.put("ReportedBy", reportedBy);
                     reportData.put("Reason", reason);
                     reportData.put("DateReported", Utils.getDateAndTime());
                     if(reportedUser != null){
                         reportData.put("ReportedUser", reportedUser);
                     }

                     // Add specific details based on the classification
                     switch(classification){
                         case "isPost":
                             reportData.put("id", Id);  // Post ID being reported
                             reportData.put("isPost", true);
                             break;
                         case "isComment":
                             reportData.put("id", Id);  // Comment ID being reported
                             reportData.put("commentId", commentId);
                             reportData.put("isComment", true);
                             break;
                         case "isUser":
                             reportData.put("id", Id);
                             reportData.put("ReportedUser", Id);  // User ID being reported
                             reportData.put("isUser", true);
                             break;
                         default:
                             Toast.makeText(mContext, "Invalid report type", Toast.LENGTH_SHORT).show();
                             return;
                     }

                     // Submit the report to Firebase
                     DatabaseReference reportsRef = authManager.GetDb().getReference().child("Reports").child(reportId);
                     reportsRef.setValue(reportData)
                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()) {
                                         Toast.makeText(mContext, "Report submitted successfully", Toast.LENGTH_SHORT).show();
                                         mDialog.dismiss();
                                     } else {
                                         Toast.makeText(mContext, "Failed to submit report", Toast.LENGTH_SHORT).show();
                                     }
                                 }
                             });

                 }else{
                     txtReportDescription.setText("Enjoying the application or have a location in mind that you want added? Submit a feedback here and we'll greatly appreciate it!");
                     String reason = txtDescription.getText().toString().trim();

                     if (reason.isEmpty()) {
                         Toast.makeText(mContext, "Submitting an empty text box is not allowed.", Toast.LENGTH_SHORT).show();
                         return;
                     }

                     // Get the current logged-in user's ID
                     String feedbackBy = loginManager.GetCurrentUser().getUid();

                     // Create a unique report ID
                     String reportId = UUID.randomUUID().toString();

                     // Create the report object
                     Map<String, Object> reportData = new HashMap<>();
                     reportData.put("FeedbackBy", feedbackBy);
                     reportData.put("Feedback", reason);
                     reportData.put("DateReported", Utils.getDateAndTime());

                     DatabaseReference reportsRef = authManager.GetDb().getReference().child("Feedback").child(reportId);
                     reportsRef.setValue(reportData)
                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()) {
                                         Toast.makeText(mContext, "Feedback submitted successfully", Toast.LENGTH_SHORT).show();
                                         mDialog.dismiss();
                                     } else {
                                         Toast.makeText(mContext, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                                     }
                                 }
                             });
                 }
             }
         });

        btnCancel.setOnClickListener(v -> mDialog.dismiss());
    }
}