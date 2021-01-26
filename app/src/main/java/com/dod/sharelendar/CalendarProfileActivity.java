package com.dod.sharelendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.dod.sharelendar.data.CalendarModel;
import com.dod.sharelendar.dialog.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CalendarProfileActivity extends AppCompatActivity {

    String uuid;
    ImageView imageView;
    Uri imageUri = null;
    String imagePath;

    private static final int IMAGE_REQUEST = 1000;

    private final String url = "https://firebasestorage.googleapis.com/v0/b/sharelendar-8841b.appspot.com/o/";
    private final String endUrl = "?alt=media";

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_profile);

        db = FirebaseFirestore.getInstance();

        imageView = findViewById(R.id.image);
        uuid = getIntent().getStringExtra("uuid");
        makeDisplay();
    }

    private void makeDisplay() {
        LoadingDialog loading = new LoadingDialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        db.collection("calendar")
                .whereEqualTo("uuid", uuid)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        CalendarModel vo = new CalendarModel();
                        for(DocumentSnapshot document : task.getResult()){
                            vo.setCalendarName(document.get("calendar_name").toString());
                            vo.setImg(document.get("calendar_img").toString());
                        }

                        imagePath = vo.getImg();

                        MultiTransformation option = new MultiTransformation(new CenterCrop(), new RoundedCorners(16));
                        try {
                            Glide.with(getApplicationContext())
                                    .load(url + urlEncoding(imagePath) + endUrl)
                                    .apply(RequestOptions.bitmapTransform(option))
                                    .into(imageView);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        imageView.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/jpg");
                            startActivityForResult(intent, IMAGE_REQUEST);
                        });

                        ((EditText)findViewById(R.id.cal_name)).setText(vo.getCalendarName());
                        loading.dismiss();
                    }else {
                        Toast.makeText(CalendarProfileActivity.this, "조회 실패", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        finish();
                    }
                });

        findViewById(R.id.save).setOnClickListener(save);
    }

    View.OnClickListener save = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LoadingDialog loading = new LoadingDialog(CalendarProfileActivity.this);
            loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            loading.setCancelable(false);
            loading.setCanceledOnTouchOutside(false);
            loading.show();

            if(validationCheck()){
                String newName = ((EditText)findViewById(R.id.cal_name)).getText().toString();
                db.collection("calendar")
                        .whereEqualTo("uuid", uuid)
                        .get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                for(DocumentSnapshot document : task.getResult()){
                                    dbChange(document.getId(), newName, loading);
                                }
                            }else {
                                Toast.makeText(CalendarProfileActivity.this,
                                        "수정 실패..!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }else {
                loading.dismiss();
            }
        }
    };

    private void dbChange(String documentId, String name, LoadingDialog loading){
        db.collection("calendar")
                .document(documentId)
                .update(
                        "calendar_name", name,
                        "calendar_img", imagePath
                ).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(imageUri != null){
                            imageUpdate(name, loading);
                        }else {
                            Toast.makeText(CalendarProfileActivity.this,
                                    "수정 완료 되었습니다.", Toast.LENGTH_SHORT).show();

                            ((CalendarOptionActivity)CalendarOptionActivity.CONTEXT).finish();
                            ((CalendarActivity)CalendarActivity.context).finish();
                            ((CalendarListActivity)CalendarListActivity.context).onRestart();
                            loading.dismiss();
                            Intent intent = new Intent(CalendarProfileActivity.this, CalendarActivity.class);
                            intent.putExtra("calName", name);
                            intent.putExtra("uuid", uuid);
                            startActivity(intent);
                            finish();
                        }
                    }else {
                        Toast.makeText(CalendarProfileActivity.this,
                                "수정 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                    finish();
                });
    }

    private void imageUpdate(String name, LoadingDialog loading){
        String deletePath = imagePath;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference();
        reference.child("calendar/" + imageUri.getLastPathSegment());
        reference.putFile(imageUri)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        reference.child(deletePath)
                                .delete()
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        Toast.makeText(CalendarProfileActivity.this,
                                                "수정 완료 되었습니다.", Toast.LENGTH_SHORT).show();

                                        ((CalendarOptionActivity)CalendarOptionActivity.CONTEXT).finish();
                                        ((CalendarActivity)CalendarActivity.context).finish();
                                        ((CalendarListActivity)CalendarListActivity.context).onRestart();
                                        loading.dismiss();
                                        Intent intent = new Intent(CalendarProfileActivity.this, CalendarActivity.class);
                                        intent.putExtra("calName", name);
                                        intent.putExtra("uuid", uuid);
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        Toast.makeText(CalendarProfileActivity.this, "기존 이미지 삭제 실패ㅠㅠ", Toast.LENGTH_SHORT).show();
                                        loading.dismiss();
                                    }
                                    finish();
                                });
                    }else {
                        Toast.makeText(CalendarProfileActivity.this, "이미지 업로드 실패 ㅠㅠ", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        finish();
                    }
                });
    }

    private boolean validationCheck(){
        boolean result = true;
        if(((EditText)findViewById(R.id.cal_name)).getText().toString().equals("")
                || ((EditText)findViewById(R.id.cal_name)).getText().toString() == null){
            result = false;
            Toast.makeText(this, "캘린더 이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public String urlEncoding(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case IMAGE_REQUEST:
                try{
                    MultiTransformation option = new MultiTransformation(new CenterCrop(), new RoundedCorners(16));

                    Glide.with(this)
                            .load(data.getData())
                            .override(200, 180)
                            .apply(RequestOptions.bitmapTransform(option))
                            .into(imageView);

                    imageUri = data.getData();
                    break;
                }catch (Exception e){
                    break;
                }
        }
    }
}