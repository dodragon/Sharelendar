package com.dod.sharelendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dod.sharelendar.adapter.CalendarViewPagerAdapter;
import com.dod.sharelendar.data.DayModel;
import com.dod.sharelendar.data.EventModel;
import com.dod.sharelendar.data.MonthModel;
import com.dod.sharelendar.dialog.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    FirebaseFirestore db;

    private static final int CALENDAR_START_YEAR = 1994;
    private static final int CALENDAR_END_YEAR = 2100;

    private String uuid;

    LoadingDialog loading;

    public static Date SELECT_DAY = null;

    public static Context context;
    ViewPager2 viewPager;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        settingDisplay();
    }

    private void settingDisplay(){
        context = this;

        loading = new LoadingDialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        db = FirebaseFirestore.getInstance();

        Calendar cal = Calendar.getInstance();
        uuid = getIntent().getStringExtra("uuid");

        ((TextView)findViewById(R.id.year_month)).setText(cal.get(Calendar.YEAR)
                + "년 "
                + (cal.get(Calendar.MONTH) + 1)
                + "월");


        findViewById(R.id.option).setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, CalendarOptionActivity.class);
            intent.putExtra("uuid", uuid);
            startActivity(intent);
        });

        db.collection("event")
                .whereEqualTo("calendar", uuid)
                .orderBy("makeDate")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<EventModel> eventList = new ArrayList<>();
                            for(DocumentSnapshot document : task.getResult()){
                                EventModel eventModel = new EventModel();
                                eventModel.setCalendar(document.getString("calendar"));
                                eventModel.setColor(document.getString("color"));
                                eventModel.setEventDate(document.getDate("eventDate"));
                                eventModel.setEventName(document.getString("eventName"));
                                eventModel.setEventComment(document.getString("eventComment"));
                                eventModel.setContinuous(document.getBoolean("continuous"));
                                eventModel.setMakeDate(document.getDate("makeDate"));
                                eventModel.setMakeUser(document.getString("makeUser"));
                                eventModel.setRepeat(document.getString("repeat"));
                                eventModel.setEventUuid(document.getString("eventUuid"));
                                eventList.add(eventModel);
                            }

                            List<MonthModel> list = getAllYearList();

                            int selectPosition = 0;
                            for(int i=0;i<list.size();i++){
                                if(list.get(i).getYear() == cal.get(Calendar.YEAR) && list.get(i).getMonth() == (cal.get(Calendar.MONTH) + 1)){
                                    selectPosition = i;
                                    break;
                                }
                            }

                            viewPager = findViewById(R.id.viewPager);
                            CalendarViewPagerAdapter adapter = new CalendarViewPagerAdapter(list, eventList, CalendarActivity.this, getEventDayList(), uuid);
                            viewPager.setAdapter(adapter);
                            viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                            viewPager.setCurrentItem(selectPosition, false);
                            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                                @Override
                                public void onPageSelected(int position) {
                                    super.onPageSelected(position);

                                    ((TextView)findViewById(R.id.year_month)).setText(list.get(position).getYear()
                                            + "년 "
                                            + list.get(position).getMonth()
                                            + "월");
                                }
                            });

                            loading.dismiss();
                        }else {
                            Log.d("캘린더 생성", task.getException().getLocalizedMessage());
                            Toast.makeText(CalendarActivity.this, "캘린더 조회 실패 !", Toast.LENGTH_SHORT).show();
                            finish();
                            loading.dismiss();
                        }
                    }
                });
    }

    private List<Date> getEventDayList(){
        List<Date> list = new ArrayList<>();
        List<MonthModel> yearList = getAllYearList();

        for(int i=0;i<yearList.size();i++){
            List<DayModel> dayList = yearList.get(i).getDayList();
            for(int j=0;j<dayList.size();j++){
                if(dayList.get(j).getDate() != null){
                    list.add(dayList.get(j).getDate());
                }
            }
        }

        return list;
    }

    private List<MonthModel> getAllYearList(){
        List<MonthModel> list = new ArrayList<>();

        for(int i=CALENDAR_START_YEAR;i<=CALENDAR_END_YEAR;i++){
            list.addAll(getMonthList(i));
        }

        return list;
    }

    private List<MonthModel> getMonthList(int year){
        List<MonthModel> monthList = new ArrayList<>();

        for(int i=0;i<12;i++){
            MonthModel model = new MonthModel();
            model.setYear(year);
            model.setMonth(i+1);
            model.setCalendarNumber("");
            model.setDayList(getDayList(year, i+1, getLastDay(year, i)));
            monthList.add(model);
        }

        return monthList;
    }

    private List<DayModel> getDayList(int year, int month, int lastDay){
        List<DayModel> dayList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month-1 , 1);

        int lastDayPosition = -1;
        int day = 0;
        for(int i=0;i<42;i++){
            DayModel model = new DayModel();
            if(i < 7 && cal.get(Calendar.DAY_OF_WEEK) - 1 > i){
                model.setDay(0);
                lastDayPosition++;
            }else if(dayList.size() > 0 && dayList.get(lastDayPosition).getDay() == lastDay){
                model.setDay(0);
            }else{
                day++;
                model.setYear(year);
                model.setMonth(month);
                model.setDay(day);
                model.setCalendarNumber("");
                Calendar thisCal = Calendar.getInstance();
                thisCal.set(year, month-1, day);
                model.setDate(new Date(thisCal.getTimeInMillis()));
                model.setSchedules(new ArrayList<>());//Todo : 수정해라
                model.setWeek(getWeek(thisCal.get(Calendar.DAY_OF_WEEK)));

                lastDayPosition++;
            }
            dayList.add(model);
        }

        return dayList;
    }

    private int getLastDay(int year, int month){
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, cal.get(Calendar.DAY_OF_MONTH));
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private String getWeek(int dayOfWeek){
        String korDayOfWeek = "";
        switch (dayOfWeek){
            case 1:
                korDayOfWeek = "일";
                break;
            case 2:
                korDayOfWeek = "월";
                break;
            case 3:
                korDayOfWeek = "화";
                break;
            case 4:
                korDayOfWeek = "수";
                break;
            case 5:
                korDayOfWeek = "목";
                break;
            case 6:
                korDayOfWeek = "금";
                break;
            case 7:
                korDayOfWeek = "토";
                break;
        }

        return korDayOfWeek;
    }

    @Override
    public void onResume() {
        super.onResume();
        //settingDisplay();
    }
}