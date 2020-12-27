package com.dod.sharelendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dod.sharelendar.data.CalendarModel;
import com.dod.sharelendar.data.UserCalendar;
import com.dod.sharelendar.data.UserModel;
import com.dod.sharelendar.dialog.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class InviteActivity extends AppCompatActivity {

    FirebaseFirestore db;

    private String url = "https://firebasestorage.googleapis.com/v0/b/sharelendar-8841b.appspot.com/o/";
    private String endUrl = "?alt=media";

    LoadingDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        loading = new LoadingDialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(pendingDynamicLinkData -> {
                    Uri deepLink = null;
                    if(pendingDynamicLinkData != null){
                        deepLink = pendingDynamicLinkData.getLink();

                        String email = getSharedPreferences("user", MODE_PRIVATE).getString("email", "");
                        String calUuid = deepLink.toString().
                                split("/")[deepLink.toString().
                                split("/").length - 1];
                        UserCalendar ucVo = getUserCalendar(email, calUuid);

                        if(getSharedPreferences("user", MODE_PRIVATE).getString("email", "").equals("")){
                            Toast.makeText(InviteActivity.this, "로그인 이후 링크를 다시 클릭 해 주세요.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, LoginActivity.class);
                            loading.dismiss();
                            startActivity(intent);
                            finish();
                        }else if(ucVo == null || ucVo.getDiv() == null){
                            db = FirebaseFirestore.getInstance();
                            CalendarModel calendarModel = getCalendar(calUuid);

                            if(calendarModel == null || calendarModel.getUuid() == null){
                                Toast.makeText(InviteActivity.this, "이미 삭제된 캘린더 입니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, SplashActivity.class);
                                loading.dismiss();
                                startActivity(intent);
                                finishAffinity();
                            }else {
                                Log.d("캘린더", calendarModel.toString());
                                try {
                                    Glide.with(this)
                                            .load(url + urlEncoding(calendarModel.getImg()) + endUrl)
                                            .override(360, 320)
                                            .centerCrop()
                                            .into((ImageView) findViewById(R.id.image));
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                ((TextView)findViewById(R.id.cal_name)).setText(calendarModel.getCalendarName());

                                findViewById(R.id.accept_btn).setOnClickListener(v ->
                                        insertUserToCalendar(email, calUuid, "normal"));
                                loading.dismiss();
                            }
                        }else {
                            Intent intent;
                            if(ucVo.getDiv().equals("kick")){
                                Toast.makeText(this, "강퇴당한 캘린더 입니다.\n초대에 응할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                intent = new Intent(this, CalendarListActivity.class);

                            }else {
                                intent = new Intent(this, CalendarActivity.class);
                                intent.putExtra("uuid", calUuid);
                            }
                            loading.dismiss();
                            startActivity(intent);
                            finish();
                        }
                    }else {
                        Log.d("INVITE_FAIL", "INTENT_NULL");
                        Toast.makeText(InviteActivity.this, "초대링크 수신 실패!", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("INVITE_FAIL", e.getLocalizedMessage());
                    Toast.makeText(InviteActivity.this, "초대링크 수신 실패!", Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                    finish();
                });
    }

    private CalendarModel getCalendar(String uuid){
        Task<QuerySnapshot> task = db.collection("calendar")
                .whereEqualTo("uuid", uuid)
                .get();

        CalendarModel model = new CalendarModel();
        while (true){
            if(task.isComplete()){
                for(DocumentSnapshot document: task.getResult()){
                    model.setCalendarName(document.get("calendar_name").toString());
                    model.setImg(document.get("calendar_img").toString());
                    model.setUuid(uuid);
                    model.setHost(document.get("host").toString());
                    model.setHostNickname(document.get("host_nickname").toString());
                }
                break;
            }
        }

        if(task.isSuccessful()){
            return model;
        }else{
            return null;
        }
    }

    private UserCalendar getUserCalendar(String email, String calUuid) {
        try{
            Task<QuerySnapshot> task = db.collection("user_calendar")
                    .whereEqualTo("email", email)
                    .whereEqualTo("calendar_uuid", calUuid)
                    .get();

            UserCalendar model = null;
            while (true){
                if(task.isComplete()){
                    model = new UserCalendar();
                    for(DocumentSnapshot document: task.getResult()){
                        model.setEmail(document.getString("email"));
                        model.setDiv(document.getString("div"));
                        model.setCalendarUuid(document.getString("calendar_uuid"));
                        model.setJoinDate(document.getDate("join_date"));
                    }
                    break;
                }
            }

            if(task.isSuccessful()){
                return model;
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void insertUserToCalendar(String email, String uuid, String div){
        Map<String, Object> userCalendarMap = new HashMap<>();
        userCalendarMap.put("calendar_uuid", uuid);
        userCalendarMap.put("div", div);
        userCalendarMap.put("email", email);

        db.collection("user_calendar")
                .add(userCalendarMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            Log.d("INVITE_INSERT_USER", "SUCCESS");
                            Toast.makeText(InviteActivity.this, "초대가 수락 되었습니다!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(InviteActivity.this, CalendarListActivity.class);
                            intent.putExtra("uuid", uuid);
                            startActivity(intent);
                            finishAffinity();
                        }else {
                            Log.d("INVITE_INSERT_USER", task.getException().getLocalizedMessage());
                            Toast.makeText(InviteActivity.this, "초대 수락에 실패 했습니다." +
                                    "\n다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public String urlEncoding(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }
}