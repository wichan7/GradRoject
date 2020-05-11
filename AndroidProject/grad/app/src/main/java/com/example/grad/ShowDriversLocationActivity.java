package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//기사의 위치를 보여주는 엑티비티. 승객이 이 화면을 보고 기다림.
public class ShowDriversLocationActivity extends AppCompatActivity {

    Button btn_accept = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_drivers_location);

        btn_accept = findViewById(R.id.btn_accept);
        btn_accept.setOnClickListener(myOnClickListener);
    }

    View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ShowDriversLocationActivity.this, DriverFeedback.class);
            startActivity(intent);
            finish();
        }
    };
}
