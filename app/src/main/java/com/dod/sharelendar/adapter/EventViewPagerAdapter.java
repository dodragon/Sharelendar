package com.dod.sharelendar.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dod.sharelendar.CalendarOptionActivity;
import com.dod.sharelendar.EventAddActivity;
import com.dod.sharelendar.R;
import com.dod.sharelendar.data.EventModel;
import com.dod.sharelendar.dialog.LoadingDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class EventViewPagerAdapter extends RecyclerView.Adapter<EventViewPagerAdapter.ViewHolder> {

    List<Date> dateList;
    DialogFragment dialog;
    List<EventModel> eventList;
    String calUuid;
    Context context;

    SimpleDateFormat format;

    public EventViewPagerAdapter(List<Date> dateList, DialogFragment dialog,
                                 List<EventModel> eventList, Context context,
                                 String calUuid) {
        this.dateList = dateList;
        this.dialog = dialog;
        this.eventList = eventList;
        this.context = context;
        this.calUuid = calUuid;

        format = new SimpleDateFormat("yyyyMMdd");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.event_viewpager, parent, false);
        return new EventViewPagerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Date date = dateList.get(position);

        holder.year.setText(getYear(date));
        holder.month.setText(getMonth(date));
        holder.day.setText(getDay(date));

        holder.close.setOnClickListener(v -> dialog.dismiss());

        holder.eventAdd.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventAddActivity.class);
            intent.putExtra("year", getYear(date));
            intent.putExtra("month", getMonth(date));
            intent.putExtra("day", getDay(date));
            intent.putExtra("date", format.format(date));
            intent.putExtra("uuid", calUuid);
            context.startActivity(intent);
        });

        if(eventList.isEmpty()){
            holder.recyclerView.setVisibility(View.GONE);
            holder.empty.setVisibility(View.VISIBLE);
        }else {
            holder.recyclerView.setVisibility(View.VISIBLE);
            holder.empty.setVisibility(View.GONE);

            RecyclerView recyclerView = holder.recyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            EventListAdapter adapter = new EventListAdapter(sortEventList(getOneDayList(date)), context);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    private List<EventModel> getOneDayList(Date thisDay){
        List<EventModel> newList = new ArrayList<>();

        for(int i=0;i<eventList.size();i++){
            if(isSameDate(thisDay, eventList.get(i).getEventDate())){
                newList.add(eventList.get(i));
            }
        }

        return newList;
    }

    private boolean isSameDate(Date date1, Date date2){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        if(format.format(date1).equals(format.format(date2))){
            return true;
        }else {
            return false;
        }
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

    private String getYear(Date date){
        return new SimpleDateFormat("yyyy").format(date);
    }

    private String getMonth(Date date){
        return new SimpleDateFormat("MM").format(date);
    }

    private String getDay(Date date){
        return new SimpleDateFormat("dd").format(date);
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        //header
        TextView year;
        TextView month;
        TextView day;
        Button close;

        //list
        RecyclerView recyclerView;
        TextView empty;

        //btn
        Button eventAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //header
            year = itemView.findViewById(R.id.year);
            month = itemView.findViewById(R.id.month);
            day = itemView.findViewById(R.id.day);
            close = itemView.findViewById(R.id.close_btn);

            //list
            recyclerView = itemView.findViewById(R.id.recycler);
            empty = itemView.findViewById(R.id.empty_text);

            //btn
            eventAdd = itemView.findViewById(R.id.event_add);
        }
    }
}
