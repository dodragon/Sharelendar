package com.dod.sharelendar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private long lastTimeBackPressed;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        permissionCheck();
    }

    private void permissionCheck(){
        if(TedPermission.isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && TedPermission.isGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
            String userId = spf.getString("email", "");

            Handler handler = new Handler();
            handler.postDelayed(new SplashHandler(userId), 3000);
        }else {
            TedPermission.with(this)
                    .setPermissionListener(listener)
                    .setRationaleMessage("프로필, 캘린더 이미지등을 이용하기 위해서 해당 권한이 필요합니다.")
                    .setDeniedMessage("왜 거부하셨어요...\n하지만 설정에서 다시 하심 되요...(무룩)")
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .check();
        }
    }

    PermissionListener listener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(SplashActivity.this, "권한이 허용 되었습니다.", Toast.LENGTH_SHORT).show();

            SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
            String userId = spf.getString("email", "");

            Handler handler = new Handler();
            handler.postDelayed(new SplashHandler(userId), 3000);
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(SplashActivity.this, "권한이 거부되었으며 일부 기능을 이용할 수 없습니다.\n" +
                    "설정 > 앱 에서 다시 설정하실 수 있습니다.", Toast.LENGTH_SHORT).show();
        }
    };

    private class SplashHandler implements Runnable{

        private String userId;

        public SplashHandler(String userId) {
            this.userId = userId;
        }

        @Override
        public void run() {
            if(userId.equals("")){
                startActivity(new Intent(getApplication(), LoginActivity.class));
            }else {
                startActivity(new Intent(getApplication(), CalendarListActivity.class));
            }
            SplashActivity.this.finish();
        }
    }

    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis() - lastTimeBackPressed < 1500){
            finish();
            return;
        }
        lastTimeBackPressed = System.currentTimeMillis();
        Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
    }
}