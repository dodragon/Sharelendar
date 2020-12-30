package com.dod.sharelendar.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.dod.sharelendar.R;
import com.dod.sharelendar.data.EventModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import petrov.kristiyan.colorpicker.ColorPicker;

public class EventViewPagerAdapter extends RecyclerView.Adapter<EventViewPagerAdapter.ViewHolder> {

    List<Date> dateList;
    DialogFragment dialog;
    List<EventModel> eventList;
    Context context;

    ColorPicker colorPicker;
    int selectColor = 0;

    public EventViewPagerAdapter(List<Date> dateList, DialogFragment dialog, List<EventModel> eventList, Context context) {
        this.dateList = dateList;
        this.dialog = dialog;
        this.eventList = eventList;
        this.context = context;

        colorPicker = new ColorPicker((Activity) context);
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

        selectColor = Color.parseColor(randomColor());
        holder.colorImg.setBackgroundColor(selectColor);
        holder.colorImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(colorPicker.getDialogViewLayout().getParent() != null){
                    ((ViewGroup)colorPicker.getDialogViewLayout().getParent()).removeView(colorPicker.getDialogViewLayout());
                }

                colorPicker.setColors(getColors())
                        .setColumns(4)
                        .setRoundColorButton(true)
                        .setColorButtonTickColor(Color.LTGRAY)
                        .setTitle("색상을 선택 긔긔")
                        .setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                            @Override
                            public void onChooseColor(int position1, int color) {
                                holder.colorImg.setColorFilter(color);
                                selectColor = color;
                            }

                            @Override
                            public void onCancel() {
                                colorPicker.dismissDialog();
                            }
                        }).show();
            }
        });

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int year = Integer.parseInt(getYear(date));
        int month = Integer.parseInt(getMonth(date));
        int day = Integer.parseInt(getDay(date));

        holder.datePicker.setMinDate(date.getTime());
        holder.datePicker.init(year, month-1, day, null);

        if(eventList.isEmpty()){
            holder.toggle.setVisibility(View.GONE);
            holder.listLayout.setVisibility(View.GONE);
            holder.addLayout.setVisibility(View.VISIBLE);
            holder.save.setVisibility(View.VISIBLE);
        }else {
            holder.toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked){
                    holder.addLayout.setVisibility(View.GONE);
                    holder.save.setVisibility(View.GONE);
                    holder.listLayout.setVisibility(View.VISIBLE);
                }else {
                    holder.addLayout.setVisibility(View.VISIBLE);
                    holder.save.setVisibility(View.VISIBLE);
                    holder.listLayout.setVisibility(View.GONE);
                }
            });

            //TODO: list Recycler 추가
        }

        //TODO: Save 활성
    }

    @Override
    public int getItemCount() {
        return dateList.size();
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

    private String randomColor(){
        return getColors().get(new Random().nextInt(getColors().size()));
    }

    private ArrayList<String> getColors(){
        ArrayList<String> colors = new ArrayList<>();

        colors.add("#c1db2a");
        colors.add("#f4d53d");
        colors.add("#fc9d35");
        colors.add("#e55151");
        colors.add("#55bf55");
        colors.add("#1ead68");
        colors.add("#46d1bd");
        colors.add("#5a94f2");
        colors.add("#45cfff");
        colors.add("#4b7fe8");
        colors.add("#ed85a0");
        colors.add("#a275d8");

        return colors;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        //header
        TextView year;
        TextView month;
        TextView day;
        Button close;
        ToggleButton toggle;

        //list
        RecyclerView recyclerView;

        //add
        EditText eventName;
        CircleImageView colorImg;
        DatePicker datePicker;
        RadioGroup patternPicker;
        EditText comment;
        Button save;

        //layout
        LinearLayout listLayout;
        ScrollView addLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //header
            year = itemView.findViewById(R.id.year);
            month = itemView.findViewById(R.id.month);
            day = itemView.findViewById(R.id.day);
            close = itemView.findViewById(R.id.close_btn);
            toggle = itemView.findViewById(R.id.toggle);

            //list
            recyclerView = itemView.findViewById(R.id.recycler);

            //add
            eventName = itemView.findViewById(R.id.event_name);
            colorImg = itemView.findViewById(R.id.color_img);
            datePicker = itemView.findViewById(R.id.date_picker);
            patternPicker = itemView.findViewById(R.id.pattern_picker);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save_btn);

            //layout
            listLayout = itemView.findViewById(R.id.event_list_layout);
            addLayout = itemView.findViewById(R.id.event_add_layout);
        }
    }
}
