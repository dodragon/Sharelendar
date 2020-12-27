package com.dod.sharelendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.dod.sharelendar.adapter.CalendarListAdapter;
import com.dod.sharelendar.data.CalendarModel;
import com.dod.sharelendar.dialog.LoadingDialog;
import com.dod.sharelendar.dialog.MakeCalendarDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CalendarListActivity extends AppCompatActivity {

    private List<CalendarModel> calendarList;
    private long lastTimeBackPressed;

    MakeCalendarDialog dialog;

    FirebaseFirestore db;

    LoadingDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_list);

        loading = new LoadingDialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        calendarList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        db.collection("user_calendar")
                .whereEqualTo("email",
                        getSharedPreferences("user", MODE_PRIVATE).getString("email", ""))
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        CollectionReference calendarReference = db.collection("calendar");
                        List<String> uuidList = new ArrayList<>();
                        for(QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())){
                            if(!document.getString("div").equals("kick")){
                                uuidList.add( Objects.requireNonNull(document.get("calendar_uuid")).toString());
                            }
                        }
                        calendarReference
                                .orderBy("make_date", Query.Direction.DESCENDING)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        for(QueryDocumentSnapshot document1 : Objects.requireNonNull(task1.getResult())){
                                            CalendarModel model = new CalendarModel();

                                            for(int i=0;i<uuidList.size();i++){
                                                if(uuidList.get(i).equals(Objects.requireNonNull(document1.get("uuid")).toString())){
                                                    model.setCalendarName(Objects.requireNonNull(document1.get("calendar_name")).toString());
                                                    model.setHost(Objects.requireNonNull(document1.get("host")).toString());
                                                    model.setHostNickname(Objects.requireNonNull(document1.get("host_nickname")).toString());
                                                    model.setImg(Objects.requireNonNull(document1.get("calendar_img")).toString());
                                                    model.setUuid(Objects.requireNonNull(document1.get("uuid")).toString());

                                                    calendarList.add(model);
                                                }
                                            }
                                        }

                                        RecyclerView recyclerView = findViewById(R.id.recycler);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(CalendarListActivity.this));
                                        CalendarListAdapter adapter = new CalendarListAdapter(calendarList, CalendarListActivity.this);
                                        recyclerView.setAdapter(adapter);

                                        loading.dismiss();
                                    }else {
                                        Toast.makeText(CalendarListActivity.this,
                                                "캘린더 리스트 생성 실패!", Toast.LENGTH_SHORT).show();
                                        Log.d("CALENDAR_SELECT", task1.getException().getLocalizedMessage());
                                        loading.dismiss();
                                    }
                                });
                    }else {
                        Toast.makeText(CalendarListActivity.this,
                                "캘린더 리스트 생성 실패!", Toast.LENGTH_SHORT).show();
                        Log.d("CALENDAR_SELECT", task.getException().getLocalizedMessage());
                        loading.dismiss();
                    }
                });

        dialog = MakeCalendarDialog.getInstance(CalendarListActivity.this);
        WindowManager.LayoutParams wm = new WindowManager.LayoutParams();
        wm.width=200;
        wm.height=200;

        findViewById(R.id.add_calendar).setOnClickListener(v -> {
            findViewById(R.id.deem).setVisibility(View.VISIBLE);
            dialog.show(getSupportFragmentManager(), MakeCalendarDialog.TAG_EVENT_DIALOG);
        });

        findViewById(R.id.profile).setOnClickListener(v -> {
            Intent intent = new Intent(CalendarListActivity.this, ProfileActivity.class);
            intent.putExtra("profileDiv", "profile");
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed(){
        if(dialog.isInLayout()){
            dialog.dismiss();
            findViewById(R.id.deem).setVisibility(View.GONE);
        }

        if(System.currentTimeMillis() - lastTimeBackPressed < 1500){
            finish();
            return;
        }
        lastTimeBackPressed = System.currentTimeMillis();
        Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1000:
                dialog.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}