package com.example.filoangler.activities;

import static com.google.common.io.Files.getFileExtension;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.camera.view.PreviewView;

import com.canhub.cropper.CropImageContractOptions;
import com.example.filoangler.LoginManager;
import com.example.filoangler.R;
import com.example.filoangler.StorageManager;
import com.example.filoangler.Utils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class PostActivity extends AppCompatActivity {

    private Button btnPost;
    private ImageButton btnClose,btnFlash, btnFlipCamera, btnCapture;
    private SocialAutoCompleteTextView txtImageDescription;
    private StorageManager storageManager;

    //CropImage
    private ImageView imgAdd;
    private ActivityResultLauncher<CropImageContractOptions> cropImage;

    //CameraX
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            startCamera(cameraFacing);
        }
    });
    private PreviewView previewView;
    private String capturedImageFilePath;

    //Bitmap
    private Bitmap squareBitmap;

    //Uri
    private Uri imageUri;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        previewView = findViewById(R.id.cameraPreview);
        btnPost = findViewById(R.id.btnPost);
        btnClose = findViewById(R.id.btnClose);
        btnFlash = findViewById(R.id.btnFlash);
        btnCapture = findViewById(R.id.btnCapture);
        btnFlipCamera = findViewById(R.id.btnFlipCamera);
        imgAdd = findViewById(R.id.imgAdd);
        txtImageDescription = findViewById(R.id.txtImageDescription);

        storageManager = new StorageManager();

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ChangeIntent(PostActivity.this, BloggingActivity.class);
                finish();
            }
        });

        //Starts the camera
        try{
            if(ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                activityResultLauncher.launch(Manifest.permission.CAMERA);
            }else{
                startCamera(cameraFacing);
            }
        }catch (Exception e){
            Log.e("CameraError", "Error in StartCamera: " + e);
        }

        btnFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraFacing == CameraSelector.LENS_FACING_BACK){
                    cameraFacing = CameraSelector.LENS_FACING_FRONT;
                }else{
                    cameraFacing = CameraSelector.LENS_FACING_BACK;
                }
                startCamera(cameraFacing); // Restart camera with new facing
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Upload();
            }
        });

    }

    public void startCamera(int cameraFacing){
        try{
            int aspectRatio = aspectRatio(previewView.getWidth(), previewView.getHeight());
            ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

            listenableFuture.addListener(() -> {
                try{
                    ProcessCameraProvider cameraProvider = (ProcessCameraProvider) listenableFuture.get();
                    Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                    ImageCapture imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();

                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(cameraFacing).build();

                    cameraProvider.unbindAll();

                    Camera camera = cameraProvider.bindToLifecycle(PostActivity.this, cameraSelector, preview, imageCapture);

                    btnCapture.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                                activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            }else{
                                takePicture(imageCapture);
                            }
                        }
                    });

                    btnFlash.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setFlashIcon(camera);
                        }
                    });

                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                } catch(ExecutionException | InterruptedException e){
                    Log.e("CameraError", "Error in CameraListener: " + e);
                }
            }, ContextCompat.getMainExecutor(this));
        }catch (Exception e){
            Log.e("CameraError", "Error in StartCamera Function: " + e);
        }
    }

    public void takePicture(ImageCapture imageCapture){
        try {
            final File file = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    capturedImageFilePath = file.getAbsolutePath();
                    runOnUiThread(() -> {
                        try {
                            // Load the image and read its EXIF metadata
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                            // Rotate the bitmap based on the EXIF orientation
                            Bitmap rotatedBitmap = rotateBitmap(bitmap, orientation);

                            // Crop the rotated bitmap to square
                            squareBitmap = cropToSquare(rotatedBitmap);

                            // Set the cropped, rotated bitmap to imgAdd
                            //imgAdd.setImageBitmap(squareBitmap);

                            //Converts bitmap into File(uri)
                            File squareBitmapFile = new File(getExternalFilesDir(null), "square_" + System.currentTimeMillis() + ".jpg");
                            try (FileOutputStream fileOutputStream = new FileOutputStream(squareBitmapFile)) {
                                squareBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                fileOutputStream.close();
                                Log.e("FileStream", "Success in creating image file");
                            }catch(FileNotFoundException e){
                                Log.e("FileStream", "Error in saving bitmap" + e);
                            }

                            imageUri = Uri.fromFile(squareBitmapFile);
                            imgAdd.setImageURI(imageUri);

                        } catch (IOException e) {
                            e.printStackTrace();
                            // Fallback in case of error
                            imageUri = Uri.fromFile(file);
                            imgAdd.setImageURI(imageUri);
                        }

                        btnCapture.setVisibility(View.GONE);
                        btnFlash.setVisibility(View.GONE);
                        btnFlipCamera.setVisibility(View.GONE);
                        imgAdd.setVisibility(View.VISIBLE);
                        previewView.setVisibility(View.GONE);
                    });
                }
                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e("CameraError", "Error in Saving");
                }
            });
        } catch (Exception e) {
            Log.e("CameraError", "Error in Capture: " + e);
        }
    }

    private void setFlashIcon(Camera camera){
        try{
            if(camera.getCameraInfo().hasFlashUnit()){
                if(camera.getCameraInfo().getTorchState().getValue() == 0){
                    camera.getCameraControl().enableTorch(true);
                    btnFlash.setImageResource(R.drawable.baseline_flash_off_24);
                }else{
                    camera.getCameraControl().enableTorch(false);
                    btnFlash.setImageResource(R.drawable.baseline_flash_on_24);
                }
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PostActivity.this, "Camera flash is unavailable!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }catch(Exception e){
            Log.e("CameraError", "Error in Flash: " + e);
        }
    }

    private int aspectRatio(int width, int height){
        try{
            double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
            if(Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)){
                return AspectRatio.RATIO_4_3;
            }
            return AspectRatio.RATIO_16_9;
        }catch(Exception e){
            Log.e("CameraError", "Error in AspectRatio: " + e);
        }
        return AspectRatio.RATIO_4_3;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Method to crop bitmap to square
    private Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newDimension = Math.min(width, height);

        int cropW = (width - newDimension) / 2;
        int cropH = (height - newDimension) / 2;

        return Bitmap.createBitmap(bitmap, cropW, cropH, newDimension, newDimension);
    }

    private void Upload(){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if(imageUri != null){
            StorageReference storageReference = storageManager.setStorageReference("Posts")
                    .child(System.currentTimeMillis() + "." + getFileExtension(UriFileExtension()));

            try{
                StorageTask uploadTask = storageReference.putFile(imageUri);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<? extends Object>>() {
                    @Override
                    public Task<? extends Object> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return storageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        try{
                            Uri downloadUri = task.getResult();
                            imageUrl = downloadUri.toString();

                            LoginManager loginManager = new LoginManager(PostActivity.this);

                            DatabaseReference databaseReference = storageManager.getDatabaseReference("Posts");
                            String postId = databaseReference.push().getKey();

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("PostId", postId);
                            map.put("ImageUrl", imageUrl);
                            map.put("Description", txtImageDescription.getText().toString());
                            map.put("PublisherDetails", loginManager.GetFirebaseAuth().getCurrentUser().getUid());

                            databaseReference.child(postId).setValue(map);

                            progressDialog.dismiss();
                            Utils.ChangeIntent(PostActivity.this, BloggingActivity.class);
                            finish();
                        }catch (Exception e){
                            Log.e("Post", "Error in posting" + e);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostActivity.this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show();
                        Log.e("Post", "Error in posting");
                        Utils.ChangeIntent(PostActivity.this, BloggingActivity.class);
                        finish();
                    }
                });
            }catch (Exception e){
                Log.e("Upload", "Error in uploading post" + e);
            }
        }else{
            Toast.makeText(PostActivity.this, "No image was selected", Toast.LENGTH_LONG).show();
        }

    }

    private String UriFileExtension(){
        String path = imageUri.getPath();
        if(path != null){
            return path;
        }
        return null;
    }

    //NOTE THIS FEATURE STILL NEEDS A HANDLER FOR IMAGES, A BUTTON FOR CAMERA OPTION, TEXT ONLY OPTION AND CROP OPTION.
}
