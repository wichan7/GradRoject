package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class PassengerWaitingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_waiting);

        //TODO: call 테이블에 콜 생성. status는 0임.
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent intent = new Intent(PassengerWaitingActivity.this, PassengerDriverInformActivity.class);
                startActivity(intent);
                finish();
            }
        }, 5000);// 1000 = 1초

    }
}
