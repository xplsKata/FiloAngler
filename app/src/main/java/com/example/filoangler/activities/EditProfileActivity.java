package com.example.filoangler.activities;

import static com.google.common.io.Files.getFileExtension;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Manager.StorageManager;
import com.example.filoangler.Model.CommentModel;
import com.example.filoangler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private Button btnSave;
    private ImageButton btnClose;
    private TextView btnChangePhoto;
    private EditText txtFirstName, txtLastName, txtUsername, txtBio;
    private ImageView imgProfile;
    private LoginManager loginManager;
    private AuthManager authManager;
    private StorageManager storageManager;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImage;
    private StorageReference storageRef;

    private Uri croppedImageUri;
    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        loginManager = new LoginManager(this);
        authManager = new AuthManager();
        storageManager = new StorageManager();

        btnSave = findViewById(R.id.btnSave);
        btnClose = findViewById(R.id.btnClose);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtUsername = findViewById(R.id.txtUsername);
        txtBio = findViewById(R.id.txtBio);
        imgProfile = findViewById(R.id.imgProfile);

        storageRef = storageManager.setStorageReference("Profile Pictures")
                .child(loginManager.GetCurrentUser().getUid());

        getProfileDetails();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermissions();
            }
        });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            startCrop(imageUri);
                        }
                    }
                }
        );

        // Initialize crop launcher
        cropImage = registerForActivityResult(
                new CropImageContract(),
                result -> {
                    if (result.isSuccessful()) {
                        croppedImageUri = result.getUriContent();
                        imgProfile.setImageURI(croppedImageUri);
                    } else {
                        Toast.makeText(this, "Image cropping failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        } else {
            // For Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void startCrop(Uri imageUri) {
        CropImageOptions options = new CropImageOptions();
        options.imageSourceIncludeGallery = true;
        options.imageSourceIncludeCamera = false;
        options.guidelines = CropImageView.Guidelines.ON;

        // Lock aspect ratio to 1:1
        options.aspectRatioX = 1;
        options.aspectRatioY = 1;
        options.fixAspectRatio = true; // This forces the 1:1 ratio

        // Set initial crop window to be as large as possible while maintaining the aspect ratio
        options.initialCropWindowPaddingRatio = 0;

        // Set output settings
        options.outputCompressFormat = Bitmap.CompressFormat.JPEG;
        options.outputCompressQuality = 90; // Good quality while keeping file size reasonable

        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(imageUri, options);
        cropImage.launch(cropImageContractOptions);
    }

    private void updateProfile() {

        try{
            HashMap<String, Object> personalInformationMap = new HashMap<>();
            personalInformationMap.put("FirstName", txtFirstName.getText().toString());
            personalInformationMap.put("LastName", txtLastName.getText().toString());
            personalInformationMap.put("Bio", txtBio.getText().toString());

            HashMap<String, Object> accountDetailsMap = new HashMap<>();
            accountDetailsMap.put("Username", txtUsername.getText().toString());

            authManager.GetDb().getReference()
                    .child("Users")
                    .child(loginManager.GetCurrentUser().getUid())
                    .child("Personal Information")
                    .updateChildren(personalInformationMap);

            authManager.GetDb().getReference()
                    .child("Users")
                    .child(loginManager.GetCurrentUser().getUid())
                    .child("Account Details")
                    .updateChildren(accountDetailsMap);

            uploadImageToFirebase(croppedImageUri);
        }catch(Exception e){
            Toast.makeText(EditProfileActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
            Log.e("EditProfileActivity", "Error: " + e);
        }finally {
            finish();
        }

    }

    private void getProfileDetails(){

        authManager.GetDb().getReference().child("Users")
                .child(loginManager.GetCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String firstName = snapshot.child("Personal Information").child("FirstName").getValue(String.class);
                        String lastName = snapshot.child("Personal Information").child("LastName").getValue(String.class);
                        String profileIconURL = snapshot.child("Account Details").child("ProfileIconURL").getValue(String.class);
                        String username = snapshot.child("Account Details").child("Username").getValue(String.class);
                        String bio = snapshot.child("Personal Information").child("Bio").getValue(String.class);

                        if (profileIconURL != null && !profileIconURL.equals("null")) {
                            Picasso.get().load(profileIconURL).into(imgProfile);
                        } else {
                            imgProfile.setImageResource(R.mipmap.ic_launcher);
                        }

                        txtFirstName.setText(firstName);
                        txtLastName.setText(lastName);
                        txtUsername.setText(username);
                        txtBio.setText(bio);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {
                                    // Update profile image URL in database
                                    authManager.GetDb().getReference()
                                            .child("Users")
                                            .child(loginManager.GetCurrentUser().getUid())
                                            .child("Account Details")
                                            .child("ProfileIconURL")
                                            .setValue(downloadUri.toString())
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(EditProfileActivity.this,
                                                            "Profile picture updated successfully",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this,
                                    "Failed to upload image: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}