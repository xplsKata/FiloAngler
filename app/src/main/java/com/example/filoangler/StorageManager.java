package com.example.filoangler;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StorageManager {
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    public StorageReference setStorageReference(String Reference){
        return storageReference = FirebaseStorage.getInstance().getReference(Reference);
    }

    public DatabaseReference getDatabaseReference(String Reference){
        return databaseReference = FirebaseDatabase.getInstance("https://filoangler-24b41-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference(Reference);
    }
}
