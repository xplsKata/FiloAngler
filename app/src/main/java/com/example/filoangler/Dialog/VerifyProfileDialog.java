package com.example.filoangler.Dialog;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Manager.StorageManager;
import com.example.filoangler.R;
import com.example.filoangler.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VerifyProfileDialog {
    private static final int PICK_IMAGE_REQUEST_1 = 1;
    private static final int PICK_IMAGE_REQUEST_2 = 2;
    private static final int PICK_IMAGE_REQUEST_3 = 3;
    private static final int STORAGE_PERMISSION_CODE = 1001;

    private Context mContext;
    private Activity mActivity;
    private ImageView imgIcon;
    private ImageView imgOne;
    private ImageView imgTwo;
    private ImageView imgThree;
    private Button btnSubmit;
    private Button btnCancel;
    private Dialog mDialog;

    private LoginManager loginManager;
    private AuthManager authManager;
    private StorageManager storageManager;
    private ProgressDialog progressDialog;

    private Uri imageUri1, imageUri2, imageUri3;
    private boolean isUploading = false;
    private int uploadCount = 0;
    private Map<String, String> downloadUrls;
    private int currentImageRequest;

    public VerifyProfileDialog(Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
        this.downloadUrls = new HashMap<>();

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait while we upload your certificates...");
        progressDialog.setCancelable(false);
    }

    public void getDialog(Dialog dialog) {
        this.mDialog = dialog;
        loginManager = new LoginManager(mContext);
        authManager = new AuthManager();
        storageManager = new StorageManager();

        // Initialize views
        imgIcon = dialog.findViewById(R.id.imgIcon);
        imgOne = dialog.findViewById(R.id.imgOne);
        imgTwo = dialog.findViewById(R.id.imgTwo);
        imgThree = dialog.findViewById(R.id.imgThree);
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        btnCancel = dialog.findViewById(R.id.btnCancel);

        // Setup initial state
        Utils.loadImage(imgIcon, R.drawable.shark);
        setupListeners();
    }

    private void setupListeners() {
        imgOne.setOnClickListener(v -> checkPermissionAndPickImage(PICK_IMAGE_REQUEST_1));
        imgTwo.setOnClickListener(v -> checkPermissionAndPickImage(PICK_IMAGE_REQUEST_2));
        imgThree.setOnClickListener(v -> checkPermissionAndPickImage(PICK_IMAGE_REQUEST_3));

        btnSubmit.setOnClickListener(v -> submitCredentials());
        btnCancel.setOnClickListener(v -> mDialog.dismiss());
    }

    private void checkPermissionAndPickImage(int requestCode) {
        currentImageRequest = requestCode;
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            openImagePicker(requestCode);
        }
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker(currentImageRequest);
            } else {
                Toast.makeText(mContext, "Storage permission is required to select images",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            ImageView targetImageView;

            switch (requestCode) {
                case PICK_IMAGE_REQUEST_1:
                    imageUri1 = imageUri;
                    targetImageView = imgOne;
                    break;
                case PICK_IMAGE_REQUEST_2:
                    imageUri2 = imageUri;
                    targetImageView = imgTwo;
                    break;
                case PICK_IMAGE_REQUEST_3:
                    imageUri3 = imageUri;
                    targetImageView = imgThree;
                    break;
                default:
                    return;
            }

            // Load and center the image using Picasso
            Picasso.get()
                    .load(imageUri)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.baseline_stream_24) // Replace with your loading placeholder
                    .into(targetImageView);
        }
    }

    private void submitCredentials() {
        if (isUploading) {
            Toast.makeText(mContext, "Upload in progress...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri1 == null && imageUri2 == null && imageUri3 == null) {
            Toast.makeText(mContext, "Please select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        isUploading = true;
        btnSubmit.setEnabled(false);
        progressDialog.show();

        String userId = loginManager.GetCurrentUser().getUid();
        uploadImages(userId);
    }

    private void uploadImages(String userId) {
        uploadCount = 0;
        downloadUrls.clear();

        if (imageUri1 != null) {
            uploadSingleImage(imageUri1, "ImageOne", userId);
        }
        if (imageUri2 != null) {
            uploadSingleImage(imageUri2, "ImageTwo", userId);
        }
        if (imageUri3 != null) {
            uploadSingleImage(imageUri3, "ImageThree", userId);
        }
    }

    private void uploadSingleImage(Uri imageUri, String imageName, String userId) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageManager.setStorageReference("certifications/" + userId + "/" + fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        downloadUrls.put(imageName, uri.toString());
                        uploadCount++;
                        checkUploadCompletion(userId);
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    isUploading = false;
                    btnSubmit.setEnabled(true);
                    Toast.makeText(mContext, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading: " + imageName + " " + (int)progress + "%");
                });
    }

    private void checkUploadCompletion(String userId) {
        int expectedUploads = 0;
        if (imageUri1 != null) expectedUploads++;
        if (imageUri2 != null) expectedUploads++;
        if (imageUri3 != null) expectedUploads++;

        if (uploadCount == expectedUploads) {
            saveToDatabase(userId);
        }
    }

    private void saveToDatabase(String userId) {
        DatabaseReference certificationRef = storageManager.getDatabaseReference("Certifications")
                .child(userId);

        for (Map.Entry<String, String> entry : downloadUrls.entrySet()) {
            certificationRef.child(entry.getKey()).setValue(entry.getValue());
        }

        certificationRef.child(downloadUrls.keySet().iterator().next()).setValue(
                        downloadUrls.get(downloadUrls.keySet().iterator().next()))
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    isUploading = false;
                    btnSubmit.setEnabled(true);
                    Toast.makeText(mContext, "Upload successful!", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    isUploading = false;
                    btnSubmit.setEnabled(true);
                    Toast.makeText(mContext, "Database update failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}