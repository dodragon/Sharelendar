package com.dod.sharelendar.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class FirebaseUseStorage {

    StorageReference storageRef;
    FirebaseStorage storage;

    Context context;

    public FirebaseUseStorage(Context context) {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        this.context = context;
    }

    public void uploadFile(String path){
        Log.d("패스", path);

    }
}
