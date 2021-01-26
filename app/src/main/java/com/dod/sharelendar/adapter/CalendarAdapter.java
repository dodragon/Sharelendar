package com.dod.sharelendar.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.ChineseCalendar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.dod.sharelendar.R;
import com.dod.sharelendar.data.DayModel;
import com.dod.sharelendar.data.EventModel;
import com.dod.sharelendar.dialog.EventDialog;
import com.dod.sharelendar.dialog.LoadingDialog;

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
    String uuid;

    SimpleDateFormat format;

    private static Map<String, String> lunarHoliday;
    private static Map<String, String> solarHoliday;

    List<Date> eventDayList;

    List<EventModel> yearRList;
    List<EventModel> monthRList;
    List<EventModel> weekRList;
    List<EventModel> oneRList;

    public CalendarAdapter(List<DayModel> list, List<EventModel> eventList, Context context, List<Date> eventDayList, String uuid) {
        this.list = list;
        this.eventList = eventList;
        this.context = context;
        this.eventDayList = eventDayList;
        this.uuid = uuid;

        toDay = Calendar.getInstance();
        format = new SimpleDateFormat("yyyyMMdd");

        yearRList = divEventList(eventList, "year");
        monthRList = divEventList(eventList, "month");
        weekRList = divEventList(eventList, "week");
        oneRList = divEventList(eventList, "one");

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

            if(!eventList.isEmpty()){
                List<EventModel> thisDayList = settingRepeatEvent(vo.getDate());
                if(!thisDayList.isEmpty()){
                    for(int i=0;i<thisDayList.size();i++){
                        if(i == 5){
                            break;
                        }

                        if(thisDayList.get(i).isContinuous()){
                            holder.eventArr[i].setText("");
                        }else {
                            holder.eventArr[i].setText(thisDayList.get(i).getEventName());
                        }
                        holder.eventArr[i].setBackgroundColor(Color.parseColor(thisDayList.get(i).getColor()));
                    }

                    if(thisDayList.size() > 5){
                        holder.eventEct.setText((thisDayList.size() - 5) + "+");
                    }
                }
            }

            holder.layout.setOnClickListener(v -> {
                EventDialog dialog = EventDialog.getInstance(eventDayList, eventList, context, vo.getDate(), uuid);
                dialog.setCancelable(false);
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

    private List<EventModel> settingRepeatEvent(Date date){
        List<EventModel> newList = new ArrayList<>();

        if(!yearRList.isEmpty()){
            for(int i=0;i<yearRList.size();i++){
                if(repeatDiv(yearRList.get(i).getEventDate(), "year").equals(
                        repeatDiv(date, "year"))){
                    newList.add(yearRList.get(i));
                }
            }
        }

        if(!monthRList.isEmpty()){
            for(int i=0;i<monthRList.size();i++){
                if(repeatDiv(monthRList.get(i).getEventDate(), "month").equals(
                        repeatDiv(date, "month"))){
                    newList.add(monthRList.get(i));
                }
            }
        }

        if(!weekRList.isEmpty()){
            for(int i=0;i<weekRList.size();i++){
                if(repeatDiv(weekRList.get(i).getEventDate(), "week").equals(
                        repeatDiv(date, "week"))){
                    newList.add(weekRList.get(i));
                }
            }
        }

        if(!oneRList.isEmpty()){
            oneRList = sortEventList(oneRList);
            for(int i=0;i<oneRList.size();i++){
                if(repeatDiv(oneRList.get(i).getEventDate(), "one").equals(
                        repeatDiv(date, "one"))){
                    newList.add(oneRList.get(i));
                }
            }
        }

        return newList;
    }

    private List<EventModel> sortEventList(List<EventModel> list){
        List<EventModel> newList = new ArrayList<>();
        List<EventModel> con = new ArrayList<>();
        List<EventModel> nCon = new ArrayList<>();

        for(int i=0;i<list.size();i++){
            if(list.get(i).isContinuous()){
                con.add(list.get(i));
            }else {
                nCon.add(list.get(i));
            }
        }

        newList.addAll(con);
        newList.addAll(nCon);

        return newList;
    }

    private List<EventModel> divEventList(List<EventModel> list, String div){
        List<EventModel> newList = new ArrayList<>();

        for(EventModel vo : list){
            if(vo.getRepeat().equals(div)){
                newList.add(vo);
            }
        }

        return newList;
    }

    private String repeatDiv(Date date, String div){
        SimpleDateFormat format;
        if(div.equals("year")){
            format = new SimpleDateFormat("MMdd");
            return format.format(date);
        }else if(div.equals("month")){
            format = new SimpleDateFormat("dd");
            return format.format(date);
        }else if(div.equals("week")){
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return getWeek(cal.get(Calendar.DAY_OF_WEEK));
        }else {
            format = new SimpleDateFormat("yyyyMMdd");
            return format.format(date);
        }
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

        TextView[] eventArr;
        TextView event1;
        TextView event2;
        TextView event3;
        TextView event4;
        TextView event5;
        TextView eventEct;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            day = itemView.findViewById(R.id.day_text);
            day_event = itemView.findViewById(R.id.event_text);
            layout = itemView.findViewById(R.id.layout);
            lunar_day = itemView.findViewById(R.id.lunar_day);

            event1 = itemView.findViewById(R.id.event_list1);
            event2 = itemView.findViewById(R.id.event_list2);
            event3 = itemView.findViewById(R.id.event_list3);
            event4 = itemView.findViewById(R.id.event_list4);
            event5 = itemView.findViewById(R.id.event_list5);
            eventEct = itemView.findViewById(R.id.ect_event);

            eventArr = new TextView[]{
                event1, event2, event3, event4, event5
            };
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
