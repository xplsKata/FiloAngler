package com.example.filoangler.activities;

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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.camera.view.PreviewView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.canhub.cropper.CropImageContractOptions;
import com.example.filoangler.Adapter.GalleryAdapter;
import com.example.filoangler.Adapter.GalleryAdapterCallback;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.MediaItem;
import com.example.filoangler.OnSwipeTouchListener;
import com.example.filoangler.R;
import com.example.filoangler.Manager.StorageManager;
import com.example.filoangler.Utils;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PostActivity extends AppCompatActivity implements GalleryAdapterCallback {

    private static final int STORAGE_PERMISSION_CODE = 101;

    private Button btnPost;
    private ImageButton btnClose,btnFlash, btnFlipCamera, btnCapture;
    private SocialAutoCompleteTextView txtImageDescription;
    private StorageManager storageManager;
    private RecyclerView recyclerViewGallery;

    private ArrayList<MediaItem> mediaItems;
    private ArrayList<MediaItem> selectedMediaItems;

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
    private String imageURL;

    private ArrayList<String> imagePaths;
    private ArrayList<String> selectedImagePaths;
    public int currentImageDisplayed = -1; // -1 means camera is showing

    private GalleryAdapter galleryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        storageManager = new StorageManager();

        loadElements();
        loadCamera();

        // Request permissions if needed
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        } else {
            loadImages(0, 20);
        }

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.ChangeIntent(PostActivity.this, BloggingActivity.class);
                finish();
            }
        });

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
               uploadPost();
            }
        });

        recyclerViewGallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (lastVisibleItem + 5 >= totalItemCount) {  // Load more when 5 items are left to reach bottom
                    // Load the next set of 20 images
                    loadImages(totalItemCount, 20);
                }
            }
        });

        // Add swipe gestures for selected images
        findViewById(R.id.imgAdd).setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                showNextSelectedImage();
            }

            @Override
            public void onSwipeRight() {
                showPreviousSelectedImage();
            }
        });

    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Explain why permission is needed
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to load your gallery images")
                    .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(PostActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            // No explanation needed, request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, load images
                loadImages(0, 20);
            } else {
                // Permission denied
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadElements() {

        previewView = findViewById(R.id.cameraPreview);
        btnPost = findViewById(R.id.btnPost);
        btnClose = findViewById(R.id.btnClose);
        btnFlash = findViewById(R.id.btnFlash);
        btnCapture = findViewById(R.id.btnCapture);
        btnFlipCamera = findViewById(R.id.btnFlipCamera);
        imgAdd = findViewById(R.id.imgAdd);
        txtImageDescription = findViewById(R.id.txtImageDescription);

        recyclerViewGallery = findViewById(R.id.recyclerViewGallery);
        mediaItems = new ArrayList<>();
        selectedMediaItems = new ArrayList<>();

        galleryAdapter = new GalleryAdapter(this, mediaItems, selectedMediaItems);
        recyclerViewGallery.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewGallery.setAdapter(galleryAdapter);

        // Add an info text to show remaining selections (optional)
        TextView txtRemainingSelections = findViewById(R.id.txtRemainingSelections); // You'll need to add this to your layout
        if (txtRemainingSelections != null) {
            txtRemainingSelections.setText("You can select up to " + 10 + " items"); // Use the same MAX_SELECTIONS value
        }

    }

    private void loadCamera(){
        try{
            if(ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                activityResultLauncher.launch(Manifest.permission.CAMERA);
            }else{
                startCamera(cameraFacing);
            }
        }catch (Exception e){
            Log.e("CameraError", "Error in StartCamera: " + e);
        }
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

                            // Converts bitmap into File(uri)
                            File squareBitmapFile = new File(getExternalFilesDir(null), "square_" + System.currentTimeMillis() + ".jpg");
                            try (FileOutputStream fileOutputStream = new FileOutputStream(squareBitmapFile)) {
                                squareBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                fileOutputStream.close();
                                Log.e("FileStream", "Success in creating image file");
                            } catch(FileNotFoundException e){
                                Log.e("FileStream", "Error in saving bitmap" + e);
                            }

                            imageUri = Uri.fromFile(squareBitmapFile);
                            imgAdd.setImageURI(imageUri);

                            // Add the captured image to selectedImagePaths
                            selectedImagePaths.add(imageUri.toString());
                            currentImageDisplayed = selectedImagePaths.size() - 1;

                            // Update UI state
                            updateImageDisplayControls();
                            if (galleryAdapter != null) {
                                galleryAdapter.notifyDataSetChanged();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            // Fallback in case of error
                            imageUri = Uri.fromFile(file);
                            imgAdd.setImageURI(imageUri);

                            // Still add to selectedImagePaths even in fallback case
                            selectedImagePaths.add(imageUri.toString());
                            currentImageDisplayed = selectedImagePaths.size() - 1;

                            // Update UI state
                            updateImageDisplayControls();
                            if (galleryAdapter != null) {
                                galleryAdapter.notifyDataSetChanged();
                            }
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
                    Toast.makeText(PostActivity.this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("CameraError", "Error in Capture: " + e);
            Toast.makeText(PostActivity.this, "Failed to capture image", Toast.LENGTH_SHORT).show();
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



    // Add these methods to handle image navigation
    private void showNextSelectedImage() {
        if (selectedImagePaths.size() > 0) {
            currentImageDisplayed = (currentImageDisplayed + 1) % selectedImagePaths.size();
            updateDisplayedImage();
        }
    }

    private void showPreviousSelectedImage() {
        if (selectedImagePaths.size() > 0) {
            currentImageDisplayed = (currentImageDisplayed - 1 + selectedImagePaths.size()) % selectedImagePaths.size();
            updateDisplayedImage();
        }
    }



    public void updateDisplayedImage() {
        if (currentImageDisplayed >= 0 && currentImageDisplayed < selectedImagePaths.size()) {
            imgAdd = findViewById(R.id.imgAdd);  // Make sure this ID exists in your layout
            Uri imageUri = Uri.parse(selectedImagePaths.get(currentImageDisplayed));
            imgAdd.setImageURI(imageUri);
        }
    }

    public void updateImageDisplayControls() {
        if (selectedImagePaths.isEmpty()) {
            previewView.setVisibility(View.VISIBLE);
            imgAdd.setVisibility(View.GONE);
            btnCapture.setVisibility(View.VISIBLE);
            btnFlash.setVisibility(View.VISIBLE);
            btnFlipCamera.setVisibility(View.VISIBLE);
        } else {
            previewView.setVisibility(View.GONE);
            imgAdd.setVisibility(View.VISIBLE);
            btnCapture.setVisibility(View.GONE);
            btnFlash.setVisibility(View.GONE);
            btnFlipCamera.setVisibility(View.GONE);
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

    private Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newDimension = Math.min(width, height);

        int cropW = (width - newDimension) / 2;
        int cropH = (height - newDimension) / 2;

        return Bitmap.createBitmap(bitmap, cropW, cropH, newDimension, newDimension);
    }



    public ArrayList<String> getImagesPath(Context context, int offset, int limit) {
        ArrayList<String> listOfImages = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED
        };

        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT " + limit + " OFFSET " + offset;

        try (Cursor cursor = context.getContentResolver().query(
                uri,
                projection,
                null,
                null,
                sortOrder)) {
            if (cursor != null && cursor.getCount() > 0) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    listOfImages.add(imageUri.toString());
                }
            }
        } catch (Exception e) {
            Log.e("GalleryError", "Error loading images: " + e.getMessage());
        }

        return listOfImages;
    }

    public ArrayList<MediaItem> getMediaItems(Context context, int offset, int limit) {
        ArrayList<MediaItem> mediaList = new ArrayList<>();

        // Query for both images and videos
        String[] projection = {
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.DATE_ADDED
        };

        // Query images
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        getMediaFromUri(context, imageUri, projection, offset, limit, mediaList, false);

        // Query videos
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        getMediaFromUri(context, videoUri, projection, offset, limit, mediaList, true);

        // Sort by date
        Collections.sort(mediaList, (item1, item2) -> {
            // You'll need to add dateAdded to MediaItem class
            return Long.compare(item2.getDateAdded(), item1.getDateAdded());
        });

        return mediaList;
    }

    private void getMediaFromUri(Context context, Uri contentUri, String[] projection,
                                 int offset, int limit, ArrayList<MediaItem> mediaList, boolean isVideo) {
        String sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC LIMIT " + limit + " OFFSET " + offset;

        try (Cursor cursor = context.getContentResolver().query(
                contentUri,
                projection,
                null,
                null,
                sortOrder)) {

            if (cursor != null && cursor.getCount() > 0) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String mimeType = cursor.getString(mimeTypeColumn);
                    long dateAdded = cursor.getLong(dateAddedColumn);
                    Uri mediaUri = ContentUris.withAppendedId(contentUri, id);

                    if (isVideo) {
                        // Get video thumbnail
                        Uri thumbnailUri = ContentUris.withAppendedId(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, id);
                        long duration = getVideoDuration(context, mediaUri);
                        mediaList.add(new MediaItem(mediaUri, thumbnailUri, true, duration, mimeType, dateAdded));
                    } else {
                        mediaList.add(new MediaItem(mediaUri, null, false, 0, mimeType, dateAdded));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("GalleryError", "Error loading media: " + e.getMessage());
        }
    }

    private long getVideoDuration(Context context, Uri videoUri) {
        long duration = 0;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, videoUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = Long.parseLong(time);
            retriever.release();
        } catch (Exception e) {
            Log.e("VideoError", "Error getting video duration: " + e.getMessage());
        }
        return duration;
    }

    private void loadImages(int offset, int limit) {
        ArrayList<String> newImages = getImagesPath(this, offset, limit);
        if (!newImages.isEmpty()) {
            int positionStart = imagePaths.size();
            imagePaths.addAll(newImages);
            galleryAdapter.notifyItemRangeInserted(positionStart, newImages.size());
        }
    }





    private void uploadPost() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if (selectedMediaItems.size() > 0) {
            ArrayList<String> mediaUrls = new ArrayList<>();
            AtomicInteger uploadedCount = new AtomicInteger(0);

            for (int i = 0; i < selectedMediaItems.size(); i++) {
                MediaItem mediaItem = selectedMediaItems.get(i);
                String extension = mediaItem.isVideo() ? ".mp4" : ".jpg";

                StorageReference storageReference = storageManager.setStorageReference("Posts")
                        .child(System.currentTimeMillis() + "_" + i + extension);

                try {
                    UploadTask uploadTask = storageReference.putFile(mediaItem.getUri());
                    int finalI = i;
                    uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return storageReference.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            mediaUrls.add(finalI, downloadUri.toString());

                            if (uploadedCount.incrementAndGet() == selectedMediaItems.size()) {
                                // Create post with both images and videos
                                createPost(mediaUrls, progressDialog);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
                    });
                } catch (Exception e) {
                    progressDialog.dismiss();
                    Log.e("Upload", "Error in uploading post: " + e);
                }
            }
        } else {
            progressDialog.dismiss();
            Toast.makeText(PostActivity.this, "No media selected", Toast.LENGTH_LONG).show();
        }
    }

    private void createPost(ArrayList<String> mediaUrls, ProgressDialog progressDialog) {
        LoginManager loginManager = new LoginManager(PostActivity.this);
        DatabaseReference databaseReference = storageManager.getDatabaseReference("Posts");
        String postId = databaseReference.push().getKey();

        HashMap<String, Object> map = new HashMap<>();
        map.put("PostId", postId);
        map.put("MediaURLs", mediaUrls);
        map.put("Description", txtImageDescription.getText().toString());
        map.put("Author", loginManager.GetFirebaseAuth().getCurrentUser().getUid());
        map.put("DatePosted", Utils.getDateAndTime());

        databaseReference.child(postId).setValue(map)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Utils.ChangeIntent(PostActivity.this, BloggingActivity.class);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(PostActivity.this, "Failed to create post", Toast.LENGTH_LONG).show();
                });
    }


    @Override
    public void onMediaSelectionChanged(MediaItem mediaItem, boolean isSelected) {
        // Update selection count display if you have one
        TextView txtRemainingSelections = findViewById(R.id.txtRemainingSelections);
        if (txtRemainingSelections != null) {
            int remaining = galleryAdapter.getRemainingSelections();
            if (remaining > 0) {
                txtRemainingSelections.setText("You can select " + remaining + " more item" +
                        (remaining == 1 ? "" : "s"));
            } else {
                txtRemainingSelections.setText("Maximum selections reached");
            }
        }
    }

    @Override
    public void onMaxSelectionsReached() {
        Toast.makeText(this, "Maximum number of media items selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateDisplayState(int newImageDisplayed) {
        currentImageDisplayed = newImageDisplayed;
        updateDisplayedImage();
        updateImageDisplayControls();
    }

}
