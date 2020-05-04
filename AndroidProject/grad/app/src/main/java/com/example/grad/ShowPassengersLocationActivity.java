package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

//승객의 위치를 보여주는 엑티비티. 기사가 이 화면을 보고 찾아감.
public class ShowPassengersLocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_passengers_location);
    }
}
