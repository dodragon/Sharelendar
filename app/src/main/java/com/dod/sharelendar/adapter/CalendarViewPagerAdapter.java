package com.dod.sharelendar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dod.sharelendar.R;
import com.dod.sharelendar.data.DayModel;
import com.dod.sharelendar.data.EventModel;
import com.dod.sharelendar.data.MonthModel;
import com.dod.sharelendar.data.YearModel;

import java.util.Date;
import java.util.List;

public class CalendarViewPagerAdapter extends RecyclerView.Adapter<CalendarViewPagerAdapter.ViewHolder> {

    List<MonthModel> list;
    List<EventModel> eventList;
    Context context;
    String uuid;

    List<Date> eventDayList;

    public CalendarViewPagerAdapter(List<MonthModel> list, List<EventModel> eventList, Context context, List<Date> eventDayList, String uuid) {
        this.list = list;
        this.eventList = eventList;
        this.context = context;
        this.eventDayList = eventDayList;
        this.uuid = uuid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_month, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonthModel vo = list.get(position);

        RecyclerView recyclerView = holder.recyclerView;
        CalendarAdapter adapter = new CalendarAdapter(vo.getDayList(), eventList, context, eventDayList, uuid);
        GridLayoutManager layoutManager = new GridLayoutManager(context.getApplicationContext(), 7);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        RecyclerView recyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerView = itemView.findViewById(R.id.recycler);
        }
    }
}
