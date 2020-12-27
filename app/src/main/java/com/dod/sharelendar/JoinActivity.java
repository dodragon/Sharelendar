package com.dod.sharelendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dod.sharelendar.data.UserModel;
import com.dod.sharelendar.utils.Sha256;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinActivity extends AppCompatActivity {

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.go_profile).setOnClickListener(v -> {
            String email = ((EditText)findViewById(R.id.email)).getText().toString();
            String password = ((EditText)findViewById(R.id.password)).getText().toString();
            String pwCheck = ((EditText)findViewById(R.id.password_check)).getText().toString();

            if(!isValidEmail(email)){
                Toast.makeText(JoinActivity.this, "이메일을 정확히 입력 해주세요.", Toast.LENGTH_SHORT).show();
            }else if(!password.equals(pwCheck)){
                Toast.makeText(JoinActivity.this, "비밀번호가 비밀번호 확인과 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }else if(!isValidPassword(password)){
                Toast.makeText(JoinActivity.this, "비밀번호는 영문, 숫자, 특수문자 조합이어야 합니다.", Toast.LENGTH_SHORT).show();
            }else {
                duplicateEmail(email, password);
            }
        });
    }

    public static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex); Matcher m = p.matcher(email);
        if(m.matches()) {
            err = true;
        }
        return err;
    }

    public static boolean isValidPassword(String password){
        boolean err = false;
        String regex = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&])[A-Za-z[0-9]$@$!%*#?&]{8,20}$";
        Pattern p = Pattern.compile(regex); Matcher m = p.matcher(password);
        if(m.matches()) {
            err = true;
        }
        return err;
    }

    private void duplicateEmail(String email, String password){
        db.collection("user")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<UserModel> list = new ArrayList<>();

                            for(QueryDocumentSnapshot document : task.getResult()){
                                UserModel model = new UserModel();
                                model.setEmail(document.get("email").toString());
                                model.setPassword(document.get("password").toString());
                                model.setNickname(document.get("nickname").toString());
                                model.setProfileImg(document.get("profile_img").toString());
                                model.setUuid(document.get("uuid").toString());
                                list.add(model);
                            }

                            if(list.isEmpty()){
                                Sha256 sha = new Sha256();
                                Intent intent = new Intent(JoinActivity.this, ProfileActivity.class);
                                intent.putExtra("email", email);
                                intent.putExtra("password", sha.encrypt(password));
                                intent.putExtra("profileDiv", "join");
                                startActivity(intent);
                            }else {
                                Toast.makeText(JoinActivity.this, "중복된 이메일 계정이 존재합니다.", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.d("DB SELECT ERROR", task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
}