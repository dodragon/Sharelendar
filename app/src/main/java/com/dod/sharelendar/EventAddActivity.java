package com.dod.sharelendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dod.sharelendar.data.EventModel;
import com.dod.sharelendar.dialog.LoadingDialog;
import com.dod.sharelendar.utils.RandomNumber;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import petrov.kristiyan.colorpicker.ColorPicker;

public class EventAddActivity extends AppCompatActivity {

    String year;
    String month;
    String day;
    Date date;
    String uuid;

    ColorPicker colorPicker;
    SimpleDateFormat format;

    int selectColor = -1;

    LoadingDialog loading;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_add);

        loading = new LoadingDialog(EventAddActivity.this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);

        db = FirebaseFirestore.getInstance();

        Intent getData = getIntent();
        format = new SimpleDateFormat("yyyyMMdd");
        colorPicker = new ColorPicker(this);

        uuid = getData.getStringExtra("uuid");

        year = getData.getStringExtra("year");
        month = getData.getStringExtra("month");
        day = getData.getStringExtra("day");
        try {
            date = format.parse(getData.getStringExtra("date"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ((TextView)findViewById(R.id.year)).setText(year);
        ((TextView)findViewById(R.id.month)).setText(month);
        ((TextView)findViewById(R.id.day)).setText(day);

        selectColor = Color.parseColor(randomColor());
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.circle);
        ImageView imageView = findViewById(R.id.color_img);
        drawable.setColor(selectColor);
        imageView.setImageDrawable(drawable);


        imageView.setOnClickListener(v -> {
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
                            selectColor = color;
                            drawable.setColor(selectColor);
                            imageView.setImageDrawable(drawable);
                        }

                        @Override
                        public void onCancel() {
                            colorPicker.dismissDialog();
                        }
                    }).show();
        });

        Button datePickerBtn = findViewById(R.id.date_picker);
        datePickerBtn.setText(year + "년 " + month + "월 " + day + "일");
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if(year == Integer.parseInt(EventAddActivity.this.year) &&
                        (month + 1) == Integer.parseInt(EventAddActivity.this.month) &&
                        dayOfMonth == Integer.parseInt(EventAddActivity.this.day)){
                    findViewById(R.id.one).setEnabled(true);
                    findViewById(R.id.week).setEnabled(true);
                    findViewById(R.id.p_month).setEnabled(true);
                    findViewById(R.id.p_year).setEnabled(true);
                }else {
                    ((RadioButton)findViewById(R.id.one)).setChecked(true);
                    findViewById(R.id.one).setEnabled(false);
                    findViewById(R.id.week).setEnabled(false);
                    findViewById(R.id.p_month).setEnabled(false);
                    findViewById(R.id.p_year).setEnabled(false);
                }

                datePickerBtn.setText(year + "년 " + ifTen((month + 1)) + "월 " + ifTen(dayOfMonth) + "일");
            }
        }, Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
        datePickerBtn.setOnClickListener(v -> datePickerDialog.show());

        findViewById(R.id.save).setOnClickListener(saveListener);
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

    View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(validationCheckName() && validationCheckColor()){
                loading.show();
                try {
                    saveEvent(makeModel());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void saveEvent(List<EventModel> list){
        CollectionReference reference = db.collection("event");

        int position = 0;
        for(EventModel vo : list){
            int finalPosition = position;
            reference.add(vo).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Log.d("SAVE_EVENT", finalPosition + "번째 완료");

                    if(finalPosition == list.size() - 1){
                        Toast.makeText(EventAddActivity.this, "일정 추가 완료", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        ((CalendarActivity)CalendarActivity.context).finish();
                        Intent intent = new Intent(EventAddActivity.this, CalendarActivity.class);
                        intent.putExtra("uuid", uuid);
                        startActivity(intent);
                        finish();
                    }
                }else {
                    Log.d("SAVE_EVENT", finalPosition + "번째 실패");
                    Log.d("SAVE_EVENT", task.getException().getLocalizedMessage());
                }
            });
            position++;
        }
    }

    private boolean validationCheckName(){
        EditText nameEt = findViewById(R.id.event_name);
        if(nameEt.getText().toString().equals("") || nameEt.getText().toString() == null){
            Toast.makeText(this, "일정을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            nameEt.setFocusable(true);
            return false;
        }else {
            return true;
        }
    }

    private boolean validationCheckColor(){
        int color = selectColor;
        ArrayList<String> colorList = getColors();
        boolean result = false;
        for(int i=0;i<colorList.size();i++){
            if(color == Color.parseColor(colorList.get(i))){
                result = true;
                break;
            }
        }

        if(!result){
            Toast.makeText(this, "유효하지 않은 색상 입니다.", Toast.LENGTH_SHORT).show();
        }

        return result;
    }

    private List<EventModel> makeModel() throws ParseException {
        List<EventModel> list = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");

        Date startDate = getDate(date);
        Date endDate = getDate(format.parse(((Button)findViewById(R.id.date_picker)).getText().toString()));

        int listSize = (int)Math.abs((startDate.getTime() - endDate.getTime()) / (24*60*60*1000)) + 1;
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        String color = "";
        List<String> colors = getColors();
        for(int i=0;i<colors.size();i++){
            if(Color.parseColor(colors.get(i)) == selectColor){
                color = colors.get(i);
                break;
            }
        }

        for(int i=0;i<listSize;i++){
            cal.add(Calendar.DATE, i);
            Date saveDate = cal.getTime();

            EventModel eventModel = new EventModel();
            eventModel.setEventName(((EditText)findViewById(R.id.event_name)).getText().toString());
            eventModel.setCalendar(uuid);
            eventModel.setMakeDate(new Date());
            eventModel.setEventDate(saveDate);
            eventModel.setColor(color);
            eventModel.setRepeat(getRadioResult());
            eventModel.setEventComment(((EditText)findViewById(R.id.comment)).getText().toString());
            eventModel.setMakeUser(getSharedPreferences("user", MODE_PRIVATE).getString("email", ""));
            eventModel.setUserNickname(getSharedPreferences("nickname", MODE_PRIVATE).getString("email", ""));
            eventModel.setEventUuid(makeUuid(uuid));

            if(listSize > 1){
                eventModel.setContinuous(true);
                if(i > 0){
                    eventModel.setEventName("");
                }
            }else {
                eventModel.setContinuous(false);
            }

            list.add(eventModel);
            cal.setTime(startDate);
        }

        return list;
    }

    private String getRadioResult(){
        RadioGroup group = findViewById(R.id.pattern_picker);
        switch (group.getCheckedRadioButtonId()){
            case R.id.one:
                return "one";
            case R.id.week:
                return "week";
            case R.id.p_month:
                return "month";
            case R.id.p_year:
                return "year";
            default:
                return "";
        }
    }

    private Date getDate(Date date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy년 MM월 dd일");

        String firstDate = format2.format(date) + " 00:00:00";

        return format.parse(firstDate);
    }

    private String ifTen(int num){
        if(num < 10){
            return "0" + num;
        }else {
            return String.valueOf(num);
        }
    }

    private String makeUuid(String calUuid){
        return "E" + calUuid + new RandomNumber(6).numberGen() +
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }
}