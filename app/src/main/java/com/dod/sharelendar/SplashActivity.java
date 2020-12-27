package com.dod.sharelendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    private long lastTimeBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences spf = getSharedPreferences("user", MODE_PRIVATE);
        String userId = spf.getString("email", "");

        Handler handler = new Handler();
        handler.postDelayed(new SplashHandler(userId), 3000);
    }

    private class SplashHandler implements Runnable{

        private String userId;

        public SplashHandler(String userId) {
            this.userId = userId;
        }

        @Override
        public void run() {
            if(userId.equals("")){
                //startActivity(new Intent(getApplication(), ProfileActivity.class));
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