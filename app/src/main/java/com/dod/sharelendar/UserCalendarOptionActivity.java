package com.dod.sharelendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dod.sharelendar.data.UserCalendar;
import com.dod.sharelendar.data.UserModel;
import com.dod.sharelendar.dialog.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UserCalendarOptionActivity extends AppCompatActivity {

    FirebaseFirestore db;

    LoadingDialog loading;

    UserModel user;
    String calUuid;
    String userDiv;

    private final String url = "https://firebasestorage.googleapis.com/v0/b/sharelendar-8841b.appspot.com/o/";
    private final String endUrl = "?alt=media";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_calendar_option);

        loading = new LoadingDialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);

        loading.show();

        db = FirebaseFirestore.getInstance();

        Intent get = getIntent();
        user = (UserModel) get.getSerializableExtra("user");
        calUuid = get.getStringExtra("calUuid");
        userDiv = get.getStringExtra("div");

        try {
            settingDisplay();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void settingDisplay() throws UnsupportedEncodingException {
        String thisUserDiv = getMyDiv(getSharedPreferences("user", MODE_PRIVATE)
                .getString("email", ""));

        ImageView imageView = findViewById(R.id.image);
        TextView nickname = findViewById(R.id.nickname);
        TextView email = findViewById(R.id.email);
        RadioButton radioButton;

        Glide.with(getApplicationContext())
                .load(url + urlEncoding(user.getProfileImg()) + endUrl)
                .override(250, 250)
                .centerCrop()
                .into(imageView);

        nickname.setText(user.getNickname());
        email.setText(user.getEmail());

        if(userDiv.equals("admin")){
            radioButton = findViewById(R.id.admin_check);
            radioButton.setChecked(true);
        }else if(userDiv.equals("normal")) {
            radioButton = findViewById(R.id.normal_check);
            radioButton.setChecked(true);
        }else {
            findViewById(R.id.checkLayout).setVisibility(View.GONE);
        }

        if(!thisUserDiv.equals("host")){
            findViewById(R.id.admin_check).setEnabled(false);
            findViewById(R.id.normal_check).setEnabled(false);
        }

        findViewById(R.id.kick_btn).setOnClickListener(v -> {
            if(thisUserDiv.equals("normal")){
                Toast.makeText(this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
            }else {
                AlertDialog.Builder alert = new AlertDialog.Builder(UserCalendarOptionActivity.this);
                alert.setTitle("유저 강퇴")
                        .setMessage("해당 유저를 강퇴하시겠습니까?")
                        .setPositiveButton("네", (dialog, which) -> {
                            loading.show();
                            changeUserDiv("kick");
                        })
                        .setNegativeButton("아니오", (dialog, which) -> dialog.cancel()).create();

                alert.show();
            }
        });

        findViewById(R.id.save).setOnClickListener(v -> {
            if(thisUserDiv.equals("normal")){
                Toast.makeText(this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
            }else {
                loading.show();
                String changeDiv;
                RadioGroup group = findViewById(R.id.group);

                if(group.getCheckedRadioButtonId() == R.id.normal_check){
                    changeDiv = "normal";
                }else {
                    changeDiv = "admin";
                }
                changeUserDiv(changeDiv);
            }
        });
        
        loading.dismiss();
    }

    private String getMyDiv(String email){
        try{
            Task<QuerySnapshot> task = db.collection("user_calendar")
                    .whereEqualTo("email", email)
                    .whereEqualTo("calendar_uuid", calUuid)
                    .get();

            String div = null;
            while (true){
                if(task.isComplete()){
                    for(DocumentSnapshot document: task.getResult()){
                        div = document.get("div").toString();
                    }
                    break;
                }
            }

            if(task.isSuccessful()){
                return div;
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void changeUserDiv(String changeDiv){
        db.collection("user_calendar")
                .whereEqualTo("email", user.getEmail())
                .whereEqualTo("calendar_uuid", calUuid)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            String docId = document.getId();
                            db.collection("user_calendar")
                                    .document(docId)
                                    .update("div", changeDiv)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                if(changeDiv.equals("kick")){
                                                    Toast.makeText(UserCalendarOptionActivity.this, "강퇴 성공 !", Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Toast.makeText(UserCalendarOptionActivity.this, "권한 변경 성공 !", Toast.LENGTH_SHORT).show();
                                                }
                                                loading.dismiss();
                                                ((CalendarOptionActivity)CalendarOptionActivity.CONTEXT).onResume();
                                                finish();
                                            }else {
                                                Toast.makeText(UserCalendarOptionActivity.this, "유저 권한 변경 실패!", Toast.LENGTH_SHORT).show();
                                                loading.dismiss();
                                                Log.d("USER_DIV_CHANGE", "update :: " + task.getException().getLocalizedMessage());
                                            }
                                        }
                                    });
                        }
                    }else {
                        Toast.makeText(UserCalendarOptionActivity.this, "유저 권한 변경 실패!", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        Log.d("USER_DIV_CHANGE", "get uc :: " + task.getException().getLocalizedMessage());
                    }
                });
    }

    public String urlEncoding(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }
}