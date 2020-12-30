package com.dod.sharelendar.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.icu.util.ChineseCalendar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.dod.sharelendar.CalendarActivity;
import com.dod.sharelendar.R;
import com.dod.sharelendar.data.DayModel;
import com.dod.sharelendar.data.EventModel;
import com.dod.sharelendar.dialog.EventDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder>{

    List<DayModel> list;
    List<EventModel> eventList;
    Context context;
    Calendar toDay;

    SimpleDateFormat format;

    private static Map<String, String> lunarHoliday;
    private static Map<String, String> solarHoliday;

    List<Date> eventDayList;

    public CalendarAdapter(List<DayModel> list, List<EventModel> eventList, Context context, List<Date> eventDayList) {
        this.list = list;
        this.eventList = eventList;
        this.context = context;
        this.eventDayList = eventDayList;
        toDay = Calendar.getInstance();
        format = new SimpleDateFormat("yyyyMMdd");

        holidaySetting();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new CalendarAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DayModel vo = list.get(position);

        if(vo.getDay() != 0){
            holder.day.setText(String.valueOf(vo.getDay()));
            if(vo.getWeek().equals("일")) {
                holder.day.setTextColor(Color.RED);
            }else if(vo.getWeek().equals("토")){
                holder.day.setTextColor(Color.BLUE);
            }else {
                holder.day.setTextColor(Color.BLACK);
            }

            if(solarHoliday.get(vo.getMonth() + "/" + vo.getDay()) != null){
                holder.day.setTextColor(Color.RED);
                holder.day_event.setText(solarHoliday.get(vo.getMonth() + "/" + vo.getDay()));
            }else if(lunarHoliday.get(getLunar(format.format(vo.getDate()))) != null){
                holder.lunar_day.setText(getLunar(format.format(vo.getDate())));
                holder.day.setTextColor(Color.RED);
                holder.day_event.setText(lunarHoliday.get(getLunar(format.format(vo.getDate()))));
            }else {
                holder.day_event.setVisibility(View.GONE);
            }

            if(lunarHoliday.get(getLunar(format.format(vo.getDate()))) == null) {
                holder.lunar_day.setVisibility(View.GONE);
            }

            if(vo.getYear() == toDay.get(Calendar.YEAR) && vo.getMonth() == toDay.get(Calendar.MONTH) + 1 && vo.getDay() == toDay.get(Calendar.DAY_OF_MONTH)){
                holder.layout.setBackgroundColor(Color.parseColor("#edd682"));
            }

            holder.layout.setOnClickListener(v -> {
                ((TextView)((Activity)context).findViewById(R.id.year)).setText(String.valueOf(vo.getYear()));
                ((TextView)((Activity)context).findViewById(R.id.month)).setText(String.valueOf(vo.getMonth()));
                ((TextView)((Activity)context).findViewById(R.id.day)).setText(String.valueOf(vo.getDay()));

                EventDialog dialog = EventDialog.getInstance(eventDayList, eventList, context, vo.getDate());
                dialog.show(((FragmentActivity)context).getSupportFragmentManager(), "EVENT_DIALOG");
            });
        }else {
            holder.layout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public String getLunar(String today) {
        ChineseCalendar chinaCal = new ChineseCalendar();
        Calendar cal = Calendar.getInstance() ;

        cal.set(Calendar.YEAR, Integer.parseInt(today.substring(0, 4)));
        cal.set(Calendar.MONTH, Integer.parseInt(today.substring(4, 6)) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(today.substring(6)));

        chinaCal.setTimeInMillis(cal.getTimeInMillis());


        int chinaYY = chinaCal.get(ChineseCalendar.EXTENDED_YEAR) - 2637 ;
        int chinaMM = chinaCal.get(ChineseCalendar.MONTH) + 1;
        int chinaDD = chinaCal.get(ChineseCalendar.DAY_OF_MONTH);

        String chinaDate = "" ;     // 음력 날짜

        //chinaDate += chinaYY ;      // 년
        //chinaDate += "년 " ;          // 연도 구분자

        chinaDate += Integer.toString(chinaMM);
        chinaDate += "/" ;          // 날짜 구분자
        chinaDate += Integer.toString(chinaDD);

        return chinaDate;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView day;
        TextView day_event;
        TextView lunar_day;
        ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            day = itemView.findViewById(R.id.day_text);
            day_event = itemView.findViewById(R.id.event_text);
            layout = itemView.findViewById(R.id.layout);
            lunar_day = itemView.findViewById(R.id.lunar_day);
        }
    }

    private void holidaySetting(){
        lunarHoliday = new HashMap<>();
        solarHoliday = new HashMap<>();

        lunarHoliday.put("12/30", "설 연휴");
        lunarHoliday.put("1/1", "설");
        lunarHoliday.put("1/2", "설 연휴");
        lunarHoliday.put("4/8", "석가탄신일");
        lunarHoliday.put("8/14", "추석연휴");
        lunarHoliday.put("8/15", "추석");
        lunarHoliday.put("8/16", "추석연휴");

        solarHoliday.put("1/1", "신정");
        solarHoliday.put("3/1", "삼일절");
        solarHoliday.put("5/5", "어린이날");
        solarHoliday.put("6/6", "현충일");
        solarHoliday.put("8/15", "광복절");
        solarHoliday.put("10/3", "개천절");
        solarHoliday.put("10/9", "한글날");
        solarHoliday.put("12/25", "성탄절");
    }
}
