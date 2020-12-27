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
    String imagePath = "";
    EditText nicknameEt;
    String div;

    private String url = "https://firebasestorage.googleapis.com/v0/b/sharelendar-8841b.appspot.com/o/";
    private String endUrl = "?alt=media";

    LoadingDialog loading;

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
        div = getIntent().getStringExtra("profileDiv");

        if(div.equals("join")){
            findViewById(R.id.logout).setVisibility(View.GONE);
            findViewById(R.id.img_delete).setVisibility(View.GONE);
        }else{
            findViewById(R.id.logout).setOnClickListener(logout);
            findViewById(R.id.img_delete).setOnClickListener(imgDelete);
        }

        SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
        String profileImg = spf.getString("profileImg", "");
        String nickname = spf.getString("nickname", "");

        imageView = findViewById(R.id.image);
        nicknameEt = findViewById(R.id.nickname);

        try {
            Glide.with(getApplicationContext())
                    .load(url + urlEncoding(profileImg) + endUrl)
                    .override(250, 250)
                    .centerCrop()
                    .into(imageView);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        nicknameEt.setText(nickname);

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/jpg");
            startActivityForResult(intent, 1000);
        });

        findViewById(R.id.join_finish).setOnClickListener(join);
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
            loading.show();
            SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
            String imgPath = spf.getString("profileImg", "");

            if(imgPath.equals("")){
                Toast.makeText(ProfileActivity.this, "삭제할 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                loading.dismiss();
            }else {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference deleteRef = storageRef.child(imgPath);
                deleteRef.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    SharedPreferences.Editor editor = spf.edit();
                                    editor.putString("profileImg", "");
                                    editor.apply();

                                    ImageView imageView = findViewById(R.id.image);
                                    imageView.setImageBitmap(null);

                                    Toast.makeText(ProfileActivity.this, "프로필 이미지가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    loading.dismiss();
                                }else {
                                    Toast.makeText(ProfileActivity.this, "이미지 삭제에 실패 했습니다.", Toast.LENGTH_SHORT).show();
                                    loading.dismiss();
                                }
                            }
                        });
            }


        }
    };

    @Override
    public void onStart(){
        super.onStart();
    }

    private void createAccount(String email, String password, String nickname, String uuid, SharedPreferences.Editor editor) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("firebase Email", "createUserWithEmail:success");

                        String profileImg = "";
                        if(!imagePath.equals("")){
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference();

                            Uri file = Uri.fromFile(new File(imagePath));
                            StorageReference ref = storageRef.child("profile/" + file.getLastPathSegment());
                            UploadTask uploadTask = ref.putFile(file);

                            profileImg = "profile/" + file.getLastPathSegment();
                            String finalProfileImg = profileImg;
                            uploadTask.addOnSuccessListener(taskSnapshot -> {
                                editor.putString("profileImg", finalProfileImg);
                                Log.d("USER_JOIN_IMAGE", "SUCCESS");
                                userDbJoin(email, password, nickname, finalProfileImg, uuid, editor);
                            });


                            uploadTask.addOnFailureListener(e -> {
                                Log.d("USER_IMAGE", "ERRER : " + e);
                                userDbJoin(email, password, nickname, finalProfileImg, uuid, editor);
                            });
                        }else{
                            userDbJoin(email, password, nickname, profileImg, uuid, editor);
                        }
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

    View.OnClickListener join = v -> {
        loading.show();
        if (nicknameEt.getText().toString() != null || !nicknameEt.getText().toString().equals("")) {
            String nickname = nicknameEt.getText().toString();

            SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
            SharedPreferences.Editor editor = spf.edit();

            if (div.equals("join")) {
                String email = getIntent().getStringExtra("email");
                String password = getIntent().getStringExtra("password");
                String uuid = email.split("@")[0] + new RandomNumber(6).numberGen() + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

                editor.putString("email", email);
                editor.putString("password", password);
                editor.putString("nickname", nickname);
                editor.putString("uuid", uuid);

                createAccount(email, password, nickname, uuid, editor);
            } else {
                editor.putString("nickname", nickname);
                String email = getSharedPreferences("user", MODE_PRIVATE).getString("email", "");

                if(!imagePath.equals("")){
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();

                    Uri file = Uri.fromFile(new File(imagePath));
                    StorageReference ref = storageRef.child("profile/" + file.getLastPathSegment());
                    UploadTask uploadTask = ref.putFile(file);

                    String profileImg = "profile/" + file.getLastPathSegment();

                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        Log.d("USER_IMAGE", "SUCCESS");
                        editor.putString("profileImg", profileImg);
                        nicknameChange(nickname, email, editor);
                    });

                    uploadTask.addOnFailureListener(e -> {
                        Log.d("USER_IMAGE", "ERRER : " + e);
                        nicknameChange(nickname, email, editor);
                    });
                }
            }
        }else {
            loading.dismiss();
            Toast.makeText(ProfileActivity.this, "닉네임을 입력해주세요 !", Toast.LENGTH_SHORT).show();
        }
    };

    private void nicknameChange(String newNickname, String email, SharedPreferences.Editor editor){
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
                    }
                });


    }

    private void userDbJoin(String email, String password, String nickname,
                            String profileImg, String uuid, SharedPreferences.Editor editor){
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
                    Toast.makeText(ProfileActivity.this, "회원가입에 실패 했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    editor.clear();
                    loading.dismiss();
                    finish();
                });
    }

    private void login(String email, String password, SharedPreferences.Editor editor){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        Log.d("First Login", "signInWithEmail:success");
                        Toast.makeText(ProfileActivity.this, "회원가입 성공 ! 로그인 완료 !", Toast.LENGTH_SHORT).show();
                        editor.apply();
                        Intent intent = new Intent(ProfileActivity.this, CalendarListActivity.class);
                        loading.dismiss();
                        startActivity(intent);
                        finishAffinity();
                    }else{
                        Log.d("First Login", "signInWithEmail:failure", task.getException());
                        Toast.makeText(ProfileActivity.this, "회원가입에 실패 했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        editor.clear();
                        loading.dismiss();
                        finish();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1000:
                try{
                    imageView.setImageURI(data.getData());
                    imagePath = getPath(data.getData());
                    break;
                }catch (Exception e){
                    break;
                }
        }
    }

    private String getPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }

    public String urlEncoding(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }
}