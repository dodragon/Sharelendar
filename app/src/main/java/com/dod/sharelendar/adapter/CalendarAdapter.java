package com.dod.sharelendar.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.icu.util.ChineseCalendar;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.dod.sharelendar.R;
import com.dod.sharelendar.data.DayModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder>{

    List<DayModel> list;
    Context context;
    Calendar toDay;

    SimpleDateFormat format;

    public CalendarAdapter(List<DayModel> list, Context context) {
        this.list = list;
        this.context = context;
        toDay = Calendar.getInstance();
        format = new SimpleDateFormat("yyyyMMdd");
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

            holder.lunar_day.setText(getLunar(format.format(vo.getDate())));

            if(vo.getYear() == toDay.get(Calendar.YEAR) && vo.getMonth() == toDay.get(Calendar.MONTH) + 1 && vo.getDay() == toDay.get(Calendar.DAY_OF_MONTH)){
                holder.layout.setBackgroundColor(Color.parseColor("#edd682"));
            }
        }else {
            holder.layout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private static String convertSolarToLunar(String date) {
        ChineseCalendar cc = new ChineseCalendar();
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
        cal.set(Calendar.MONTH, Integer.parseInt(date.substring(4, 6)) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(6)));

        cc.setTimeInMillis(cal.getTimeInMillis());

        int y = cc.get(ChineseCalendar.EXTENDED_YEAR) - 2637;
        int m = cc.get(ChineseCalendar.MONTH) + 1;
        int d = cc.get(ChineseCalendar.DAY_OF_MONTH);

        StringBuffer ret = new StringBuffer();
        //ret.append(String.format("%04d", y)).append("-");
        ret.append(String.format("%02d", m)).append("/");
        ret.append(String.format("%02d", d));

        return ret.toString();
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

        if(chinaMM < 10)         // 월
            chinaDate += "0" + Integer.toString(chinaMM) ;
        else
            chinaDate += Integer.toString(chinaMM) ;


        chinaDate += "/ " ;          // 날짜 구분자


        if(chinaDD < 10)         // 일
            chinaDate += "0" + Integer.toString(chinaDD) ;
        else
            chinaDate += Integer.toString(chinaDD) ;

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
}
