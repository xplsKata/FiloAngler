package com.example.filoangler;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;

public class Utils {
    public static void ChangeIntent(Context MainIntent, Class GoTo){
        Intent intent = new Intent(MainIntent, GoTo);
        MainIntent.startActivity(intent);

    }
}
