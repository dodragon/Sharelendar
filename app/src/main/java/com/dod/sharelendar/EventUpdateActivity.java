package com.dod.sharelendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import petrov.kristiyan.colorpicker.ColorPicker;

public class EventUpdateActivity extends AppCompatActivity {

    FirebaseFirestore db;

    EventModel vo;

    String selectedColor;
    GradientDrawable drawable;

    ColorPicker colorPicker;

    String year;
    String month;
    String day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_update);

        LoadingDialog loading = new LoadingDialog(this);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);
        loading.show();

        db = FirebaseFirestore.getInstance();
        colorPicker = new ColorPicker(this);
        vo = (EventModel) getIntent().getSerializableExtra("vo");
        selectedColor = vo.getColor();

        db.collection("event")
                .whereEqualTo("eventUuid", vo.getEventUuid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        settingDisplay(task.getResult().size(), loading);
                    }else {
                        finish();
                        Toast.makeText(this, "다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void settingDisplay(int howLong, LoadingDialog loading){
        year = getDateToString(vo.getEventDate(), "yyyy");
        month = getDateToString(vo.getEventDate(), "MM");
        day = getDateToString(vo.getEventDate(), "dd");


        ((TextView)findViewById(R.id.year)).setText(year);
        ((TextView)findViewById(R.id.month)).setText(month);
        ((TextView)findViewById(R.id.day)).setText(day);

        ((EditText)findViewById(R.id.event_name)).setText(vo.getEventName());
        drawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.circle);
        drawable.setColor(Color.parseColor(selectedColor));

        ImageView colorImageView = findViewById(R.id.color_img);
        colorImageView.setImageDrawable(drawable);
        colorImageView.setOnClickListener(v -> {
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
                            selectedColor = getParseColor(color);
                            drawable.setColor(color);
                            colorImageView.setImageDrawable(drawable);
                        }

                        @Override
                        public void onCancel() {
                            colorPicker.dismissDialog();
                        }
                    }).show();
        });

        Button datePickerBtn = findViewById(R.id.date_picker);

        if(howLong > 1){
            Calendar cal = Calendar.getInstance();
            cal.setTime(vo.getEventDate());
            cal.add(Calendar.DATE, howLong - 1);
            datePickerBtn.setText(getDateToString(cal.getTime(), "yyyy년 MM월 dd일"));
        }else {
            datePickerBtn.setText(year + "년 " + month + "월 " + day + "일");
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            if(year == Integer.parseInt(EventUpdateActivity.this.year) &&
                    (month + 1) == Integer.parseInt(EventUpdateActivity.this.month) &&
                    dayOfMonth == Integer.parseInt(EventUpdateActivity.this.day)){
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
        }, Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
        datePickerBtn.setOnClickListener(v -> datePickerDialog.show());

        if(vo.getRepeat().equals("one")){
            ((RadioButton)findViewById(R.id.one)).setChecked(true);
        }else if(vo.getRepeat().equals("week")){
            ((RadioButton)findViewById(R.id.week)).setChecked(true);
        }else if(vo.getRepeat().equals("month")){
            ((RadioButton)findViewById(R.id.p_month)).setChecked(true);
        }else {
            ((RadioButton)findViewById(R.id.p_year)).setChecked(true);
        }

        ((EditText)findViewById(R.id.comment)).setText(vo.getEventComment());

        findViewById(R.id.save).setOnClickListener(saveListener);
        findViewById(R.id.delete_event).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(EventUpdateActivity.this)
                    .setTitle("일정 삭제")
                    .setMessage("해당 일정을 삭제하시겠습니까?")
                    .setPositiveButton("네", (dialog, which) -> {
                        dialog.dismiss();
                        LoadingDialog loading1 = new LoadingDialog(EventUpdateActivity. this);
                        loading1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        loading1.setCancelable(false);
                        loading1.setCanceledOnTouchOutside(false);
                        loading1.show();

                        deleteEvent(loading1);
                    })
                    .setNegativeButton("아니오", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        loading.dismiss();
    }

    View.OnClickListener saveListener = v -> {
        if (validationCheckName() && validationCheckColor()) {
            LoadingDialog loading = new LoadingDialog(EventUpdateActivity.this);
            loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            loading.setCancelable(false);
            loading.setCanceledOnTouchOutside(false);
            loading.show();
            try {
                saveEvent(makeModel(), loading);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };

    private void deleteEvent(LoadingDialog loading){
        CollectionReference reference = db.collection("event");
        reference.whereEqualTo("eventUuid", vo.getEventUuid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){

                        QuerySnapshot result = task.getResult();
                        for(int i=0;i<result.size();i++){
                            DocumentSnapshot document = result.getDocuments().get(i);

                            int finalI = i;
                            reference.document(document.getId())
                                    .delete()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d("DELETE_EVENT", finalI + "번째 완료");

                                            if (finalI == result.size() - 1) {
                                                Toast.makeText(EventUpdateActivity.this, "일정 삭제 완료", Toast.LENGTH_SHORT).show();
                                                loading.dismiss();
                                                ((CalendarActivity) CalendarActivity.context).finish();
                                                Intent intent = new Intent(EventUpdateActivity.this, CalendarActivity.class);
                                                intent.putExtra("uuid", vo.getCalendar());
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });
                        }
                    }else {
                        Toast.makeText(EventUpdateActivity.this,
                                "일정 수정에 실패 했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveEvent(List<EventModel> list, LoadingDialog loading){
        CollectionReference reference = db.collection("event");
        reference.whereEqualTo("eventUuid", vo.getEventUuid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                         for(DocumentSnapshot document : task.getResult()){
                             reference.document(document.getId())
                                     .delete();
                         }

                         for(int i=0;i<list.size();i++){
                             int finalI = i;
                             reference.add(list.get(i))
                                     .addOnCompleteListener(task1 -> {
                                         Log.d("UPDATE_EVENT", finalI + "번째 완료");
                                         if (finalI == list.size() - 1) {
                                             Toast.makeText(EventUpdateActivity.this, "일정 수정 완료", Toast.LENGTH_SHORT).show();
                                             loading.dismiss();
                                             ((CalendarActivity) CalendarActivity.context).finish();
                                             Intent intent = new Intent(EventUpdateActivity.this, CalendarActivity.class);
                                             intent.putExtra("uuid", vo.getCalendar());
                                             startActivity(intent);
                                             finish();
                                         }
                                     });
                         }
                    }else {
                        Toast.makeText(EventUpdateActivity.this,
                                "일정 수정에 실패 했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validationCheckName() {
        EditText nameEt = findViewById(R.id.event_name);
        if (nameEt.getText().toString().equals("") || nameEt.getText().toString() == null) {
            Toast.makeText(this, "일정을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            nameEt.setFocusable(true);
            return false;
        } else {
            return true;
        }
    }

    private boolean validationCheckColor() {
        String color = selectedColor;
        ArrayList<String> colorList = getColors();
        boolean result = false;
        for (int i = 0; i < colorList.size(); i++) {
            if (color.equals(colorList.get(i))) {
                result = true;
                break;
            }
        }

        if (!result) {
            Toast.makeText(this, "유효하지 않은 색상 입니다.", Toast.LENGTH_SHORT).show();
        }

        return result;
    }

    private List<EventModel> makeModel() throws ParseException {
        List<EventModel> list = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");

        Date startDate = getDate(vo.getEventDate());
        Date endDate = getDate(format.parse(((Button) findViewById(R.id.date_picker)).getText().toString()));

        int listSize = (int) Math.abs((startDate.getTime() - endDate.getTime()) / (24 * 60 * 60 * 1000)) + 1;
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        String color = "";
        List<String> colors = getColors();
        for (int i = 0; i < colors.size(); i++) {
            if (colors.get(i).equals(selectedColor)) {
                color = colors.get(i);
                break;
            }
        }

        for (int i = 0; i < listSize; i++) {
            cal.add(Calendar.DATE, i);
            Date saveDate = cal.getTime();

            EventModel eventModel = new EventModel();
            eventModel.setEventName(((EditText) findViewById(R.id.event_name)).getText().toString());
            eventModel.setCalendar(vo.getCalendar());
            eventModel.setMakeDate(new Date());
            eventModel.setEventDate(saveDate);
            eventModel.setColor(color);
            eventModel.setRepeat(getRadioResult());
            eventModel.setEventComment(((EditText) findViewById(R.id.comment)).getText().toString());
            eventModel.setMakeUser(getSharedPreferences("user", MODE_PRIVATE).getString("email", ""));
            eventModel.setUserNickname(getSharedPreferences("nickname", MODE_PRIVATE).getString("email", ""));
            eventModel.setEventUuid(vo.getEventUuid());

            if (i > 0) {
                eventModel.setContinuous(true);
            } else {
                eventModel.setContinuous(false);
            }

            list.add(eventModel);
            cal.setTime(startDate);
        }

        return list;
    }

    private String getRadioResult() {
        RadioGroup group = findViewById(R.id.pattern_picker);
        switch (group.getCheckedRadioButtonId()) {
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

    private String getDateToString(Date date, String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
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

    private String getParseColor(int color){
        String colorStr = "";
        ArrayList<String> colors = getColors();
        for(int i=0;i<colors.size();i++){
            if(Color.parseColor(colors.get(i)) == color){
                colorStr = colors.get(i);
                break;
            }
        }

        return colorStr;
    }

    private String ifTen(int num){
        if(num < 10){
            return "0" + num;
        }else {
            return String.valueOf(num);
        }
    }
}