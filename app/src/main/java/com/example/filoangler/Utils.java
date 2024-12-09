package com.example.filoangler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.filoangler.Manager.AuthManager;
import com.example.filoangler.Manager.LoginManager;
import com.example.filoangler.Model.UserModel;
import com.example.filoangler.activities.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Utils {
    public static void ChangeIntent(Context MainIntent, Class GoTo){
        Intent intent = new Intent(MainIntent, GoTo);
        MainIntent.startActivity(intent);
    }

    public static void goToProfile(String UserId, Context mContext){
        Intent intent = new Intent(mContext, UserProfileActivity.class);
        intent.putExtra("UserId", UserId);
        mContext.startActivity(intent);
    }

    public static void followUser(AuthManager authManager, Button btnFollow, LoginManager loginManager, UserModel userModel, Context mContext){
        if(btnFollow.getText().toString().equals("Follow")){
            authManager.GetDb().getReference().child("Users")
                    .child(loginManager.GetCurrentUser().getUid())
                    .child("Following")
                    .child(userModel.getUserID()).setValue(true);

            authManager.GetDb().getReference().child("Users")
                    .child(userModel.getUserID())
                    .child("Followers")
                    .child(loginManager.GetCurrentUser().getUid()).setValue(true);
            notifyAuthor(userModel.getUserID(), mContext.getString(R.string.txtFollowed) , loginManager, authManager, loginManager.GetCurrentUser().getUid());
        }else{
            authManager.GetDb().getReference().child("Users")
                    .child(loginManager.GetCurrentUser().getUid())
                    .child("Following")
                    .child(userModel.getUserID()).removeValue();

            authManager.GetDb().getReference().child("Users")
                    .child(userModel.getUserID())
                    .child("Followers")
                    .child(loginManager.GetCurrentUser().getUid()).removeValue();
        }
    }

    public static void followUser(AuthManager authManager, Button btnFollow, LoginManager loginManager, String UserId, Context mContext){
        if(btnFollow.getText().toString().equals("Follow")){
            authManager.GetDb().getReference().child("Users")
                    .child(loginManager.GetCurrentUser().getUid())
                    .child("Following")
                    .child(UserId).setValue(true);

            authManager.GetDb().getReference().child("Users")
                    .child(UserId)
                    .child("Followers")
                    .child(loginManager.GetCurrentUser().getUid()).setValue(true);
            notifyAuthor(UserId, mContext.getString(R.string.txtFollowed) , loginManager, authManager, loginManager.GetCurrentUser().getUid());
        }else{
            authManager.GetDb().getReference().child("Users")
                    .child(loginManager.GetCurrentUser().getUid())
                    .child("Following")
                    .child(UserId).removeValue();

            authManager.GetDb().getReference().child("Users")
                    .child(UserId)
                    .child("Followers")
                    .child(loginManager.GetCurrentUser().getUid()).removeValue();
        }
    }

    public static void notifyAuthor(String postId, String authorId,
                                    String description, LoginManager loginManager,
                                    AuthManager authManager){

        HashMap<String, Object> map = new HashMap<>();

        map.put("UserId", loginManager.GetCurrentUser().getUid());
        map.put("Description", description);
        map.put("PostId", postId);
        map.put("isPost", true);

        authManager.GetDb().getReference().child("Users")
                .child(authorId)
                .child("Notifications")
                .push()
                .setValue(map);

    }

    public static void notifyAuthor(String authorId, String description, LoginManager loginManager, AuthManager authManager, String userId){

        HashMap<String, Object> map = new HashMap<>();

        map.put("UserId", loginManager.GetCurrentUser().getUid());
        map.put("Description", description);
        map.put("PostId", userId);
        map.put("isPost", false);

        authManager.GetDb().getReference().child("Users")
                .child(authorId)
                .child("Notifications")
                .push()
                .setValue(map);

    }

    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return json;
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static String getDateAndTime() {
        java.util.Date date = new java.util.Date();
        return new java.text.SimpleDateFormat("MM-dd-yy hh:mma").format(date);
    }

    public static void loadImage(ImageView imageView, int Icon){
        imageView.post(() -> {
            int width = imageView.getWidth();
            int height = imageView.getHeight();

            // Only proceed with loading if dimensions are valid
            if (width > 0 && height > 0) {
                Picasso.get()
                        .load(Icon)
                        .resize(width, height)
                        .into(imageView);
            } else {
                // Fallback to load without resize
                Picasso.get()
                        .load(Icon)
                        .into(imageView);
            }
        });
    }

}
