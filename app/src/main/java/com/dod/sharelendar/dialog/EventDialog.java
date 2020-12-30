package com.dod.sharelendar.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.dod.sharelendar.R;
import com.dod.sharelendar.adapter.EventViewPagerAdapter;
import com.dod.sharelendar.data.EventModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EventDialog extends DialogFragment{

    List<Date> dateList;
    List<EventModel> eventList;
    Context context;
    Date selectDate;

    public EventDialog(List<Date> dateList, List<EventModel> eventList, Context context, Date selectDate) {
        this.dateList = dateList;
        this.eventList = eventList;
        this.context = context;
        this.selectDate = selectDate;
    }

    public static EventDialog getInstance(List<Date> dateList, List<EventModel> eventList, Context context, Date selectDate){
        Bundle bundle = new Bundle();

        EventDialog dialog = new EventDialog(dateList, eventList, context, selectDate);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MakeDialog);
        View view = getActivity().getLayoutInflater().inflate(R.layout.event_dialog, null);

        ViewPager2 viewPager = view.findViewById(R.id.viewpager);
        EventViewPagerAdapter adapter = new EventViewPagerAdapter(dateList, this, eventList, context);
        viewPager.setAdapter(adapter);
        viewPager.setFocusable(true);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setCurrentItem(getSelectPosition(), false);
        viewPager.setPadding(20, 0, 20, 0);
        viewPager.setOffscreenPageLimit(10);

        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private int getSelectPosition(){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        int selectPosition = 0;

        for(int i=0;i<dateList.size();i++){
            if(format.format(dateList.get(i)).equals(format.format(selectDate))){
                selectPosition = i;
                break;
            }
        }

        return selectPosition;
    }

    @Override
    public void onResume(){
        super.onResume();

        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();

        int width = dm.widthPixels;
        int height = getResources().getDimensionPixelSize(R.dimen.event_dialog_height);
        getDialog().getWindow().setLayout(width, height);

    }
}
