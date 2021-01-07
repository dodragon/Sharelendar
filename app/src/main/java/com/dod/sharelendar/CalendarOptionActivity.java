package com.dod.sharelendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dod.sharelendar.adapter.CalendarUserAdapter;
import com.dod.sharelendar.data.CalendarModel;
import com.dod.sharelendar.data.UserModel;
import com.dod.sharelendar.dialog.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarOptionActivity extends AppCompatActivity {

    FirebaseFirestore db;
    String uuid;

    LoadingDialog loading;

    public static Context CONTEXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_option);

        CONTEXT = this;
        makeDisplay();
    }

    private void makeDisplay(){
        loading = new LoadingDialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        db = FirebaseFirestore.getInstance();
        uuid = getIntent().getStringExtra("uuid");

        db.collection("user_calendar")
                .whereEqualTo("calendar_uuid", uuid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<UserModel> userList = new ArrayList<>();
                        Map<String, String> divMap = new HashMap<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            userList.add(getUser(document.get("email").toString()));
                            divMap.put(document.get("email").toString(), document.get("div").toString());
                        }

                        RecyclerView recyclerView = findViewById(R.id.recycler);
                        recyclerView.setLayoutManager(new LinearLayoutManager(CalendarOptionActivity.this));
                        CalendarUserAdapter adapter = new CalendarUserAdapter(makeUserList(divMap, userList), divMap, uuid, CalendarOptionActivity.this);
                        recyclerView.setAdapter(adapter);

                        loading.dismiss();
                    } else {
                        Toast.makeText(CalendarOptionActivity.this, "인원 조회 실패 !", Toast.LENGTH_SHORT).show();
                        Log.d("OPTION_USER_SELECT", task.getException().getLocalizedMessage());
                        loading.dismiss();
                    }
                });

        setInviteLinkBtn();
        findViewById(R.id.exit_cal).setOnClickListener(v -> {
            LoadingDialog loading = new LoadingDialog(this);
            loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            loading.setCancelable(false);
            loading.setCanceledOnTouchOutside(false);
            loading.show();

            SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
            String email = spf.getString("email", "");

            db.collection("user_calendar")
                    .whereEqualTo("email", email)
                    .whereEqualTo("calendar_uuid", uuid)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            loading.dismiss();
                            for (DocumentSnapshot document : task.getResult()) {
                                String div = document.get("div").toString();
                                AlertDialog.Builder builder = new AlertDialog.Builder(CalendarOptionActivity.this);

                                if (div.equals("host")) {
                                    builder.setTitle("캘린더 삭제")
                                            .setMessage("현재 캘린더를 삭제 하시겠습니까?")
                                            .setCancelable(false)
                                            .setNegativeButton("취소", (dialog, which) -> dialog.cancel())
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    loading.show();
                                                    db.collection("user_calendar")
                                                            .whereEqualTo("calendar_uuid", uuid)
                                                            .get()
                                                            .addOnCompleteListener(task1 -> {
                                                                if (task1.isSuccessful()) {
                                                                    for (DocumentSnapshot document1 : task1.getResult()) {
                                                                        deleteUserCalendar(document1.getId(), false);
                                                                    }

                                                                    db.collection("calendar")
                                                                            .whereEqualTo("uuid", uuid)
                                                                            .get()
                                                                            .addOnCompleteListener(task11 -> {
                                                                                if (task11.isSuccessful()) {
                                                                                    for (DocumentSnapshot document1 : task11.getResult()) {
                                                                                        deleteCalendar(document1.getId());
                                                                                    }
                                                                                    deleteAllEvent(uuid);
                                                                                } else {
                                                                                    Toast.makeText(CalendarOptionActivity.this, "캘린더 삭제 실패..!", Toast.LENGTH_SHORT).show();
                                                                                    Log.d("calendar_delete11", task11.getException().getLocalizedMessage());
                                                                                    loading.dismiss();
                                                                                }
                                                                            });
                                                                } else {
                                                                    Toast.makeText(CalendarOptionActivity.this, "캘린더 삭제 실패..!", Toast.LENGTH_SHORT).show();
                                                                    Log.d("calendar_delete", task1.getException().getLocalizedMessage());
                                                                    loading.dismiss();
                                                                }
                                                            });
                                                }
                                            });
                                } else {
                                    builder.setTitle("캘린더 나가기")
                                            .setMessage("현재 캘린더를 나가시겠습니까?")
                                            .setCancelable(false)
                                            .setNegativeButton("취소", (dialog, which) -> dialog.cancel())
                                            .setPositiveButton("확인", (dialog, which) -> {
                                                loading.show();
                                                db.collection("user_calendar")
                                                        .whereEqualTo("email", email)
                                                        .whereEqualTo("calendar_uuid", uuid)
                                                        .get()
                                                        .addOnCompleteListener(task12 -> {
                                                            if (task12.isSuccessful()) {
                                                                for (DocumentSnapshot document12 : task12.getResult()) {
                                                                    deleteUserCalendar(document12.getId(), true);
                                                                }
                                                                deleteMyEvent(uuid, email);
                                                            } else {
                                                                Toast.makeText(CalendarOptionActivity.this, "캘린더 나가기 실패..!", Toast.LENGTH_SHORT).show();
                                                                Log.d("calendar_exit", task12.getException().getLocalizedMessage());
                                                                loading.dismiss();
                                                            }
                                                        });
                                            });
                                }

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        } else {
                            Toast.makeText(CalendarOptionActivity.this, "캘린더 나가기 실패..!", Toast.LENGTH_SHORT).show();
                            Log.d("calendar_exit", task.getException().getLocalizedMessage());
                            loading.dismiss();
                        }
                    });
        });

        if(loading.isShowing()){
            loading.dismiss();
        }
    }

    private void deleteAllEvent(String calUuid){
        db.collection("event")
                .whereEqualTo("calendar", calUuid)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            deleteEvent(document.getId());
                        }
                    }else {
                        Log.d("Event Doc Select Fail", task.getException().getLocalizedMessage());
                    }
                });
    }

    private void deleteMyEvent(String calUuid, String email){
        db.collection("event")
                .whereEqualTo("calendar", calUuid)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            deleteEvent(document.getId());
                        }
                    }else {
                        Log.d("Event Doc Select Fail", task.getException().getLocalizedMessage());
                    }
                });
    }

    private void deleteEvent(String docId){
        db.collection("event")
                .document(docId)
                .delete()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d("Event Doc Delete Success", task.getException().getLocalizedMessage());
                    }else {
                        Log.d("Event Doc Delete Fail", task.getException().getLocalizedMessage());
                    }
                });
    }

    private void deleteCalendar(String docId) {
        db.collection("calendar")
                .document(docId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CalendarOptionActivity.this, "캘린더 삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CalendarOptionActivity.this, CalendarListActivity.class);
                        startActivity(intent);
                        loading.dismiss();
                        finish();
                    } else {
                        Toast.makeText(CalendarOptionActivity.this, "캘린더 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        Log.d("calendar_delete_db", task.getException().getLocalizedMessage());
                        loading.dismiss();
                    }
                });
    }

    private void deleteUserCalendar(String docId, boolean isOne) {
        db.collection("user_calendar")
                .document(docId)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loading.dismiss();
                        if (isOne) {
                            Toast.makeText(CalendarOptionActivity.this, "캘린더 나가기 완료!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CalendarOptionActivity.this, CalendarListActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.d("캘린더 삭제", docId + ">> 완료");
                        }
                    } else {
                        Toast.makeText(CalendarOptionActivity.this, "캘린더 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        Log.d("user_calendar_delete", task.getException().getLocalizedMessage());
                        loading.dismiss();
                    }
                });
    }

    private UserModel getUser(String email) {
        Task<QuerySnapshot> task = db.collection("user")
                .whereEqualTo("email", email)
                .get();

        UserModel model = new UserModel();
        while (true) {
            if (task.isComplete()) {
                for (DocumentSnapshot document : task.getResult()) {
                    model.setEmail(document.get("email").toString());
                    model.setProfileImg(document.get("profile_img").toString());
                    model.setNickname(document.get("nickname").toString());
                    model.setUuid(document.get("uuid").toString());
                }
                break;
            }
        }

        if (task.isSuccessful()) {
            return model;
        } else {
            return new UserModel();
        }
    }

    private void setInviteLinkBtn() {
        findViewById(R.id.invite_link_copy).setOnClickListener(v -> {
            String link = getCalendar(uuid).getLink();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("invite link", link);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(CalendarOptionActivity.this, "초대링크가 복사 되었습니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private CalendarModel getCalendar(String uuid) {
        Task<QuerySnapshot> task = db.collection("calendar")
                .whereEqualTo("uuid", uuid)
                .get();

        CalendarModel model = new CalendarModel();
        while (true) {
            if (task.isComplete()) {
                for (DocumentSnapshot document : task.getResult()) {
                    model.setCalendarName(document.get("calendar_name").toString());
                    model.setImg(document.get("calendar_img").toString());
                    model.setUuid(uuid);
                    model.setHost(document.get("host").toString());
                    model.setHostNickname(document.get("host_nickname").toString());
                    model.setLink(document.get("invite_link").toString());
                }
                break;
            }
        }

        if (task.isSuccessful()) {
            return model;
        } else {
            return null;
        }
    }

    private List<UserModel> makeUserList(Map<String, String> divMap, List<UserModel> userList) {
        List<UserModel> newList = new ArrayList<>();
        UserModel hostModel = new UserModel();
        List<UserModel> adminList = new ArrayList<>();
        List<UserModel> normalList = new ArrayList<>();

        for (int i = 0; i < userList.size(); i++) {
            if (divMap.get(userList.get(i).getEmail()).equals("host")) {
                hostModel = userList.get(i);
            } else if (divMap.get(userList.get(i).getEmail()).equals("admin")) {
                adminList.add(userList.get(i));
            } else if (divMap.get(userList.get(i).getEmail()).equals("normal")){
                normalList.add(userList.get(i));
            }
        }

        newList.add(hostModel);
        newList.addAll(adminList);
        newList.addAll(normalList);

        return newList;
    }

    @Override
    public void onResume() {
        super.onResume();

        makeDisplay();
    }
}