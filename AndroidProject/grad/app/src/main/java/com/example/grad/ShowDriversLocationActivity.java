package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

//기사의 위치를 보여주는 엑티비티. 승객이 이 화면을 보고 기다림.
public class ShowDriversLocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_drivers_location);
    }
}
