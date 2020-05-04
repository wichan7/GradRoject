package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DriverCallCheckActivity extends AppCompatActivity {
    private Button btn_accept;

    //TODO: 여기서 수락하면 CALL의 STATUS가 1이된다.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_call_check);

        btn_accept = findViewById(R.id.btn_accept);
        btn_accept.setOnClickListener(myOnClickListener);

    }

    View.OnClickListener myOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(DriverCallCheckActivity.this, DriverWaitingActivity.class);
            startActivity(intent);
            finish();
        }
    };
}
