package com.dod.sharelendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dod.sharelendar.data.UserModel;
import com.dod.sharelendar.dialog.LoadingDialog;
import com.dod.sharelendar.utils.Sha256;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private long lastTimeBackPressed;

    LoadingDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loading = new LoadingDialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.go_join).setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, JoinActivity.class)));
        findViewById(R.id.login).setOnClickListener(login);
    }

    private View.OnClickListener login = v -> {
        loading.show();
        String email = ((EditText)findViewById(R.id.email)).getText().toString();
        String password = new Sha256().encrypt(((EditText)findViewById(R.id.password)).getText().toString());
        login(email, password);
    };

    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis() - lastTimeBackPressed < 1500){
            finish();
            return;
        }
        lastTimeBackPressed = System.currentTimeMillis();
        Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
    }

    private void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        Log.d("Login", "signInWithEmail:success");
                        Toast.makeText(LoginActivity.this, "로그인 완료 !", Toast.LENGTH_SHORT).show();

                        db.collection("user")
                                .whereEqualTo("email", email)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        List<UserModel> list = new ArrayList<>();

                                        for(QueryDocumentSnapshot document : task1.getResult()){
                                            UserModel model = new UserModel();
                                            model.setEmail(document.get("email").toString());
                                            model.setPassword(document.get("password").toString());
                                            model.setNickname(document.get("nickname").toString());
                                            model.setProfileImg(document.get("profile_img").toString());
                                            model.setUuid(document.get("uuid").toString());
                                            list.add(model);
                                        }

                                        try{
                                            SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = spf.edit();

                                            editor.putString("email", list.get(0).getEmail());
                                            editor.putString("password", list.get(0).getPassword());
                                            editor.putString("profileImg", list.get(0).getProfileImg());
                                            editor.putString("nickname", list.get(0).getNickname());
                                            editor.putString("uuid", list.get(0).getUuid());

                                            editor.apply();

                                            loading.dismiss();

                                            Intent intent = new Intent(LoginActivity.this, CalendarListActivity.class);
                                            startActivity(intent);
                                            finishAffinity();
                                        }catch (Exception e){
                                            e.printStackTrace();
                                            Toast.makeText(this
                                                    , "등록된 회원이 아닙니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        Log.d("DB SELECT ERROR", task1.getException().getLocalizedMessage());
                                        mAuth.signOut();
                                        loading.dismiss();
                                        Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 다시 확인 해주세요.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }else{
                        Log.d("Login", "signInWithEmail:failure", task.getException());
                        loading.dismiss();
                        Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 다시 확인 해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}