package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.sql.Driver;

public class DriverWaitingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_waiting);

        //TODO: call의 status가 1->2가되면 수락된것 1->3이되면 거절된것. 이것에 알맞는 코드 작성
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent intent = new Intent(DriverWaitingActivity.this, ShowPassengersLocationActivity.class);
                startActivity(intent);
                finish();
            }
        }, 5000);// 1000 = 1초
    }
}
