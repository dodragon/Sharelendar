package com.dod.sharelendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dod.sharelendar.dialog.LoadingDialog;
import com.dod.sharelendar.utils.RandomNumber;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    ImageView imageView;
    Uri imageUri;
    String profileImg = "";
    String afterImg;
    EditText nicknameEt;

    String deleteImagePath = "";

    private String url = "https://firebasestorage.googleapis.com/v0/b/sharelendar-8841b.appspot.com/o/";
    private String endUrl = "?alt=media";

    LoadingDialog loading;

    CropImage.ActivityBuilder cropBuilder;

    private static final int IMAGE_SELECT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        loading = new LoadingDialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
        afterImg = spf.getString("profileImg", "");
        String nickname = spf.getString("nickname", "");

        imageView = findViewById(R.id.image);
        nicknameEt = findViewById(R.id.nickname);

        try {
            if(afterImg.equals("")){
                imageView.setImageDrawable(getDrawable(R.drawable.profile));
            }else {
                Glide.with(getApplicationContext())
                        .load(url + urlEncoding(afterImg) + endUrl)
                        .override(250, 250)
                        .centerCrop()
                        .into(imageView);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        nicknameEt.setText(nickname);
        nicknameEt.addTextChangedListener(watcher);

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/jpg");
            startActivityForResult(intent, IMAGE_SELECT);
        });

        findViewById(R.id.save).setOnClickListener(join);
        findViewById(R.id.logout).setOnClickListener(logout);
        findViewById(R.id.img_delete).setOnClickListener(imgDelete);
    }

    View.OnClickListener logout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            loading.show();

            mAuth.signOut();
            SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
            SharedPreferences.Editor editor = spf.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(ProfileActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

            loading.dismiss();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAffinity();
        }
    };

    View.OnClickListener imgDelete = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            deleteImagePath = afterImg;
            imageView.setImageDrawable(getDrawable(R.drawable.profile));
        }
    };

    private void saveImg(String newNickname, String email, SharedPreferences.Editor editor, LoadingDialog loading) {
        Log.d("프로필 이미지", profileImg);
        if (!profileImg.equals("")) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            StorageReference ref = storageRef.child(profileImg);
            ref.putFile(imageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            editor.putString("profileImg", profileImg);
                            Log.d("USER_IMAGE", "SUCCESS");

                            if(!deleteImagePath.equals("")){
                                FirebaseStorage storageDelete = FirebaseStorage.getInstance();
                                StorageReference storageRefDelete = storageDelete.getReference();

                                StorageReference refDelete = storageRefDelete.child(deleteImagePath);
                                refDelete.delete().addOnCompleteListener(taskDelete -> {
                                    if(taskDelete.isSuccessful()){
                                        Toast.makeText(ProfileActivity.this, "기존 프로필 사진이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                        db.collection("user")
                                                .whereEqualTo("email", email)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if(task.isSuccessful()){
                                                            for(DocumentSnapshot document : task.getResult()){
                                                                db.collection("user")
                                                                        .document(document.getId())
                                                                        .update("profile_img", profileImg)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    nicknameChange(newNickname, email, editor, loading);
                                                                                }else {
                                                                                    Toast.makeText(ProfileActivity.this, "프로필 변경 실패..!", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }else {
                                                            Toast.makeText(ProfileActivity.this, "프로필이미지 저장에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }else {
                                        Toast.makeText(ProfileActivity.this, "프로필 사진 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                db.collection("user")
                                        .whereEqualTo("email", email)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()){
                                                    for(DocumentSnapshot document : task.getResult()){
                                                        db.collection("user")
                                                                .document(document.getId())
                                                                .update("profile_img", profileImg)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            nicknameChange(newNickname, email, editor, loading);
                                                                        }else {
                                                                            Toast.makeText(ProfileActivity.this, "프로필 변경 실패..!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }else {
                                                    Toast.makeText(ProfileActivity.this, "프로필이미지 저장에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d("USER_IMAGE", task.getException().getLocalizedMessage());
                            Toast.makeText(ProfileActivity.this, "이미지 업로드에 실패 했습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            if(!deleteImagePath.equals("") && !deleteImagePath.equals("profile/")){
                FirebaseStorage storageDelete = FirebaseStorage.getInstance();
                StorageReference storageRefDelete = storageDelete.getReference();

                StorageReference refDelete = storageRefDelete.child(deleteImagePath);
                refDelete.delete().addOnCompleteListener(taskDelete -> {
                    if(taskDelete.isSuccessful()){
                        Toast.makeText(ProfileActivity.this, "기존 프로필 사진이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        editor.putString("profileImg", "");
                        db.collection("user")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        for(DocumentSnapshot document : task.getResult()){
                                            db.collection("user")
                                                    .document(document.getId())
                                                    .update("profile_img", "")
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                nicknameChange(newNickname, email, editor, loading);
                                                            }else {
                                                                Toast.makeText(ProfileActivity.this, "프로필 사진 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                                                loading.dismiss();
                                                                finish();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }else {
                        Toast.makeText(ProfileActivity.this, "프로필 사진 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        finish();
                    }
                });
            }else {
                nicknameChange(newNickname, email, editor, loading);
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    View.OnClickListener join = v -> {
        loading.show();
        if (nicknameEt.getText().toString() != null || !nicknameEt.getText().toString().equals("")) {
            String nickname = nicknameEt.getText().toString();

            SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
            SharedPreferences.Editor editor = spf.edit();

            editor.putString("nickname", nickname);
            String email = getSharedPreferences("user", MODE_PRIVATE).getString("email", "");

            saveImg(nickname, email, editor, loading);
        }else {
            loading.dismiss();
            Toast.makeText(ProfileActivity.this, "닉네임을 입력해주세요 !", Toast.LENGTH_SHORT).show();
        }
    };

    private void nicknameChange(String newNickname, String email, SharedPreferences.Editor editor, LoadingDialog loading){
        db.collection("user")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d("유저 조회", "ㅇㅇ");
                        for(DocumentSnapshot document : task.getResult()){
                            db.collection("user")
                                    .document(document.getId())
                                    .update("nickname", newNickname)
                                    .addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()){
                                            Log.d("유저 닉넴 변경", "ㅇㅇ");
                                            db.collection("calendar")
                                                    .whereEqualTo("host", email)
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if(task.isSuccessful()){
                                                                Log.d("캘린더 호스트", "ㅇㅇ");
                                                                QuerySnapshot result = task.getResult();
                                                                for(int i=0;i<result.size();i++){
                                                                    DocumentSnapshot document = result.getDocuments().get(i);
                                                                    int finalI = i;
                                                                    db.collection("calendar")
                                                                            .document(document.getId())
                                                                            .update("host_nickname", newNickname)
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Log.d("호스트 변경", "ㅇㅇ");
                                                                                        if(finalI == result.size() - 1){
                                                                                            editor.putString("nickname", newNickname);
                                                                                            editor.apply();

                                                                                            loading.dismiss();
                                                                                            Toast.makeText(ProfileActivity.this, "프로필 변경 성공!", Toast.LENGTH_SHORT).show();
                                                                                            Intent intent = new Intent(ProfileActivity.this, CalendarListActivity.class);
                                                                                            startActivity(intent);
                                                                                        }
                                                                                    }else {
                                                                                        Toast.makeText(ProfileActivity.this, "닉네임 변경에 실패했습니당 ㅠㅠ\n나중에 다시 변경 해주세요 !", Toast.LENGTH_SHORT).show();
                                                                                        Intent intent = new Intent(ProfileActivity.this, CalendarListActivity.class);
                                                                                        loading.dismiss();
                                                                                        startActivity(intent);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }else {
                                                                Toast.makeText(ProfileActivity.this, "닉네임 변경에 실패했습니당 ㅠㅠ\n나중에 다시 변경 해주세요 !", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(ProfileActivity.this, CalendarListActivity.class);
                                                                loading.dismiss();
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    });
                                        }else {
                                            Toast.makeText(ProfileActivity.this, "닉네임 변경에 실패했습니당 ㅠㅠ\n나중에 다시 변경 해주세요 !", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ProfileActivity.this, CalendarListActivity.class);
                                            loading.dismiss();
                                            startActivity(intent);
                                        }
                                        finishAffinity();
                                    });
                        }
                    }else {
                        Toast.makeText(ProfileActivity.this, "닉네임 변경에 실패했습니당 ㅠㅠ\n나중에 다시 변경 해주세요 !", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProfileActivity.this, CalendarListActivity.class);
                        loading.dismiss();
                        startActivity(intent);
                        finishAffinity();
                    }
                });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case IMAGE_SELECT:
                if(resultCode == RESULT_OK){
                    startCrop(data.getData());
                }
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if(result != null){
                    if(resultCode == RESULT_OK){
                        imageUri = result.getUri();
                        Glide.with(this)
                                .load(imageUri)
                                .override(200, 200)
                                .into(imageView);

                        deleteImagePath = afterImg;

                        profileImg = "profile/" + getSharedPreferences("user", MODE_PRIVATE)
                                .getString("email", "")
                                .split("@")[0]
                                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                                + ".jpg";
                    }
                }
        }
    }

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().isEmpty()) {
                findViewById(R.id.nickname_check).setVisibility(View.GONE);
            } else {
                findViewById(R.id.nickname_check).setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void startCrop(Uri uri) {
        Log.d("스타트 크롭", uri.toString());

        cropBuilder = CropImage.activity(uri);
        cropBuilder.setCropShape(CropImageView.CropShape.OVAL)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(15, 15)
                .setFixAspectRatio(true);

        cropBuilder.start(this);
    }

    public String urlEncoding(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }
}