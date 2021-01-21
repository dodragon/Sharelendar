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
    String profileImg;
    EditText nicknameEt;

    String deleteImagePath = "";

    private String url = "https://firebasestorage.googleapis.com/v0/b/sharelendar-8841b.appspot.com/o/";
    private String endUrl = "?alt=media";

    LoadingDialog loading;

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
        profileImg = spf.getString("profileImg", "");
        String nickname = spf.getString("nickname", "");

        imageView = findViewById(R.id.image);
        nicknameEt = findViewById(R.id.nickname);

        try {
            if(profileImg.equals("")){
                imageView.setImageDrawable(getDrawable(R.drawable.profile));
            }else {
                Glide.with(getApplicationContext())
                        .load(url + urlEncoding(profileImg) + endUrl)
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
            deleteImagePath = profileImg;
            profileImg = "";
            imageView.setImageDrawable(getDrawable(R.drawable.profile));
        }
    };

    private void saveImg(String newNickname, String email, SharedPreferences.Editor editor, LoadingDialog loading) {
        if (!profileImg.equals("")) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            StorageReference ref = storageRef.child(profileImg);
            ref.putFile(imageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            editor.putString("profileImg", profileImg);
                            Log.d("USER_IMAGE", "SUCCESS");
                            nicknameChange(newNickname, email, editor, loading);
                        } else {
                            Log.d("USER_IMAGE", task.getException().getLocalizedMessage());
                            Toast.makeText(ProfileActivity.this, "이미지 업로드에 실패 했습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            if(!deleteImagePath.equals("")){
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                StorageReference ref = storageRef.child(deleteImagePath);
                ref.delete().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(ProfileActivity.this, "기존 프로필 사진이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(ProfileActivity.this, "프로필 사진 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
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
                        for(DocumentSnapshot document : task.getResult()){
                            db.collection("user")
                                    .document(document.getId())
                                    .update("nickname", newNickname)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                editor.putString("nickname", newNickname);
                                                editor.apply();

                                                Toast.makeText(ProfileActivity.this, "프로필 변경 성공!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(ProfileActivity.this, CalendarListActivity.class);
                                                loading.dismiss();
                                                startActivity(intent);
                                            }else {
                                                Toast.makeText(ProfileActivity.this, "닉네임 변경에 실패했습니당 ㅠㅠ\n나중에 다시 변경 해주세요 !", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(ProfileActivity.this, CalendarListActivity.class);
                                                loading.dismiss();
                                                startActivity(intent);
                                            }
                                            finishAffinity();
                                        }
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
                }else {
                    imageView.setImageDrawable(getDrawable(R.drawable.profile));
                    imageUri = null;
                    profileImg = "";
                    Toast.makeText(this, "다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if(resultCode == RESULT_OK){
                    imageUri = result.getUri();
                    Glide.with(this)
                            .load(imageUri)
                            .override(200, 200)
                            .into(imageView);

                    if(profileImg.equals("")){
                        profileImg = "profile/" + getSharedPreferences("user", MODE_PRIVATE)
                                .getString("email", "")
                                .split("@")[0]
                                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                                + ".jpg";
                    }
                }else {
                    imageView.setImageDrawable(getDrawable(R.drawable.profile));
                    imageUri = null;
                    profileImg = "";
                    Toast.makeText(this, "다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                    Log.d("크롭 에러", result.getError().getLocalizedMessage());
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

        CropImage.ActivityBuilder builder = CropImage.activity(uri);
        builder.setCropShape(CropImageView.CropShape.OVAL)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(15, 15)
                .setFixAspectRatio(true)
                .start(this);
    }

    public String urlEncoding(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }
}