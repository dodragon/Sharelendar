package com.dod.sharelendar.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.loader.content.CursorLoader;

import com.dod.sharelendar.CalendarListActivity;
import com.dod.sharelendar.R;
import com.dod.sharelendar.data.CalendarModel;
import com.dod.sharelendar.data.UserCalendar;
import com.dod.sharelendar.utils.RandomNumber;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MakeCalendarDialog extends DialogFragment implements View.OnClickListener{

    public static final String TAG_EVENT_DIALOG = "make_calendar_dialog_event";
    private Context context;

    String imagePath = "";
    ImageView imageView;

    FirebaseFirestore db;

    LoadingDialog loading;

    public MakeCalendarDialog(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public static MakeCalendarDialog getInstance(Context context){
        Bundle bundle = new Bundle();

        MakeCalendarDialog dialog = new MakeCalendarDialog(context);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MakeDialog);
        View view = getActivity().getLayoutInflater().inflate(R.layout.make_calendar_dialog, null);

        loading = new LoadingDialog(context);
        loading.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);

        imageView = view.findViewById(R.id.image);

        view.findViewById(R.id.close_dialog).setOnClickListener(this);
        view.findViewById(R.id.maked).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.show();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference reference = storage.getReference();

                Uri file = Uri.fromFile(new File(imagePath));
                StorageReference ref = reference.child("calendar/" + file.getLastPathSegment());
                UploadTask uploadTask = ref.putFile(file);

                SharedPreferences spf = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);

                String calendarImg = "calendar/" + file.getLastPathSegment();
                String name = ((EditText)view.findViewById(R.id.name)).getText().toString();
                String email = spf.getString("email", "");
                String nickname = spf.getString("nickname", "");
                String uuid = makeUuid(spf.getString("uuid", ""));

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    Log.d("CALENDAR_IMAGE", "SUCCESS");
                    calendarInsert(makeCalendarVo(name, calendarImg, email, nickname,
                            uuid));
                });

                uploadTask.addOnFailureListener(e -> {
                    Log.d("CALENDAR_IMAGE", "ERROR : " + e);
                    Toast.makeText(context, "캘린더 이미지를 등록해주세요 !", Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                });
            }
        });

        view.findViewById(R.id.image).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/jpg");
            getActivity().startActivityForResult(intent, 1000);
        });

        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private CalendarModel makeCalendarVo(String name, String img, String hostEmail,
                                         String hostNickname, String uuid){
        CalendarModel vo = new CalendarModel();
        vo.setCalendarName(name);
        vo.setImg(img);
        vo.setHost(hostEmail);
        vo.setHostNickname(hostNickname);
        vo.setUuid(uuid);
        vo.setMakeDate(new Date());

        return vo;
    }

    private UserCalendar makeUserCalendarVo(String uuid, String div, String email){
        UserCalendar vo = new UserCalendar();
        vo.setCalendarUuid(uuid);
        vo.setDiv(div);
        vo.setEmail(email);
        vo.setJoinDate(new Date());

        return vo;
    }

    private void calendarInsert(CalendarModel vo) {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://dod.sharelendar.invite/" + vo.getUuid()))
                .setDomainUriPrefix("https://sharec.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder("com.dod.sharelendar")
                        .build())
                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle(vo.getCalendarName())
                        .setDescription("캘린더 초대장이 왔어요")
                        .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d("MAKE_LINK", "SUCCESS");
                        Map<String, Object> calendarMAp = new HashMap<>();
                        calendarMAp.put("calendar_name", vo.getCalendarName());
                        calendarMAp.put("calendar_img", vo.getImg());
                        calendarMAp.put("host", vo.getHost());
                        calendarMAp.put("host_nickname", vo.getHostNickname());
                        calendarMAp.put("uuid", vo.getUuid());
                        calendarMAp.put("invite_link", task.getResult().getShortLink().toString());
                        calendarMAp.put("make_date", vo.getMakeDate());

                        db.collection("calendar")
                                .add(calendarMAp)
                                .addOnSuccessListener(documentReference -> {
                                    userCalendarInsert(makeUserCalendarVo(vo.getUuid(), "host", vo.getHost()), documentReference.getPath());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "켈린더 생성 실패! 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                    loading.dismiss();
                                    closeDialog();
                                });
                    }else {
                        Log.d("MAKE_LINK", task.getException().getLocalizedMessage());
                        Toast.makeText(context, "켈린더 생성 실패! 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                        closeDialog();
                    }
                });
    }

    private void userCalendarInsert(UserCalendar vo, String path){
        Map<String, Object> userCalendarMap = new HashMap<>();
        userCalendarMap.put("calendar_uuid", vo.getCalendarUuid());
        userCalendarMap.put("div", vo.getDiv());
        userCalendarMap.put("email", vo.getEmail());
        userCalendarMap.put("join_date", vo.getJoinDate());

        db.collection("user_calendar")
                .add(userCalendarMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "캘린더가 생성되었습니다.", Toast.LENGTH_SHORT).show();
                    closeDialog();

                    Intent intent = new Intent(context, CalendarListActivity.class);
                    loading.dismiss();
                    getActivity().startActivity(intent);
                    getActivity().finishAffinity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "켈린더 생성 실패! 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();

                    db.collection("calendar")
                            .document(path)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Calendar 실패", "DB삭제 완료");
                                loading.dismiss();
                                closeDialog();
                            })
                            .addOnFailureListener(e1 -> {
                                Log.d("Calendar 실패", "DB삭제 실패");
                                Toast.makeText(context, "리스트에 캘린더가 계속 보일 시 관리자에게 문의하세요.", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                                closeDialog();
                            });

                    closeDialog();
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1000:
                try{
                    imageView.setImageURI(data.getData());
                    imagePath = getPath(data.getData());
                    break;
                }catch (Exception e){
                    break;
                }
        }
    }

    private String getPath(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(context, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }

    @Override
    public void onClick(View v) {
        closeDialog();
    }

    public void closeDialog(){
        ((Activity)context).findViewById(R.id.deem).setVisibility(View.GONE);
        dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((Activity)context).findViewById(R.id.deem).setVisibility(View.GONE);
    }

    private String makeUuid(String userUuid){
        return "C" + userUuid + new RandomNumber(6).numberGen() +
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }
}
