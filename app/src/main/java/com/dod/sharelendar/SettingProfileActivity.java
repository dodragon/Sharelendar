package com.dod.sharelendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dod.sharelendar.dialog.LoadingDialog;
import com.dod.sharelendar.utils.RandomNumber;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    CircleImageView imageView;
    EditText nicknameEt;

    String imageName = "";
    Uri imageUri;

    LoadingDialog loading;

    private static final int IMAGE_SELECT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);

        loading = new LoadingDialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imageView = findViewById(R.id.image);
        nicknameEt = findViewById(R.id.nickname);

        nicknameEt.addTextChangedListener(watcher);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/jpg");
            startActivityForResult(intent, IMAGE_SELECT);
        });

        findViewById(R.id.next).setOnClickListener(join);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        findViewById(R.id.img_delete).setOnClickListener(imgDelete);
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
                    imageName = "";
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
                    imageName = getIntent().getStringExtra("email").split("@")[0]
                            + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                            + ".jpg";
                }else {
                    Log.d("크롭 에러", result.getError().getLocalizedMessage());
                }
        }
    }

    View.OnClickListener imgDelete = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imageView.setImageDrawable(getDrawable(R.drawable.profile));
            imageUri = null;
            imageName = "";
        }
    };

    private void saveImg() {
        if (!imageName.equals("")) {
            String imgPath = "profile/" + imageName;

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            StorageReference ref = storageRef.child(imgPath);
            ref.putFile(imageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
                            SharedPreferences.Editor editor = spf.edit();
                            editor.putString("profileImg", imgPath);
                            editor.apply();
                            Log.d("USER_IMAGE", "SUCCESS");
                        } else {
                            Log.d("USER_IMAGE", task.getException().getLocalizedMessage());
                            Toast.makeText(SettingProfileActivity.this, "이미지 업로드에 실패 했습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void startCrop(Uri uri) {
        CropImage.activity(uri)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(15, 15)
                .setFixAspectRatio(true)
                .start(this);
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

    View.OnClickListener join = v -> {
        loading.show();
        if (nicknameEt.getText().toString() != null || !nicknameEt.getText().toString().equals("")) {
            String nickname = nicknameEt.getText().toString();

            SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
            SharedPreferences.Editor editor = spf.edit();
            String email = getIntent().getStringExtra("email");
            String password = getIntent().getStringExtra("password");
            String uuid = email.split("@")[0] + new RandomNumber(6).numberGen() + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            editor.putString("email", email);
            editor.putString("password", password);
            editor.putString("nickname", nickname);
            editor.putString("uuid", uuid);
            editor.apply();

            saveImg();

            createAccount(email, password, nickname, uuid, editor);
        } else if (nicknameEt.getText().toString().contains(" ")) {
            loading.dismiss();
            Toast.makeText(SettingProfileActivity.this, "닉네임에 공백이 들어갈 수 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            loading.dismiss();
            Toast.makeText(SettingProfileActivity.this, "닉네임을 입력해주세요 !", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    private void createAccount(String email, String password, String nickname, String uuid, SharedPreferences.Editor editor) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("firebase Email", "createUserWithEmail:success");
                        userDbJoin(email, password, nickname, "profile/" + imageName, uuid, editor);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("firebase Email", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(this, "회원가입 실패, 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        editor.clear();
                        finish();
                    }
                });
    }

    private void userDbJoin(String email, String password, String nickname,
                            String profileImg, String uuid, SharedPreferences.Editor editor) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("password", password);
        userMap.put("nickname", nickname);
        userMap.put("profile_img", profileImg);
        userMap.put("uuid", uuid);
        userMap.put("join_date", new Date());

        db.collection("user")
                .add(userMap)
                .addOnSuccessListener(documentReference -> {
                    Log.d("USER_JOIN_DB", "SUCCESS : " + documentReference.getId());
                    login(email, password, editor);
                })
                .addOnFailureListener(e -> {
                    Log.d("USER_JOIN_DB", "ERROR : " + e);
                    editor.clear();
                    loading.dismiss();
                    finish();
                });
    }

    private void login(String email, String password, SharedPreferences.Editor editor) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("First Login", "signInWithEmail:success");
                        editor.apply();
                        Intent intent = new Intent(SettingProfileActivity.this, CalendarListActivity.class);
                        loading.dismiss();
                        startActivity(intent);
                        finishAffinity();
                    } else {
                        Log.d("First Login", "signInWithEmail:failure", task.getException());
                        editor.clear();
                        loading.dismiss();
                        finish();
                    }
                });
    }
}