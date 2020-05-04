package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DriverCallListActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btn_call1, btn_call2, btn_call3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_call_list);

        btn_call1 = findViewById(R.id.btn_call1);
        btn_call2 = findViewById(R.id.btn_call2);
        btn_call3 = findViewById(R.id.btn_call3);

        btn_call1.setOnClickListener(this);
        btn_call2.setOnClickListener(this);
        btn_call3.setOnClickListener(this);
        
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(DriverCallListActivity.this, DriverCallCheckActivity.class);
        startActivity(intent);
        finish();
    }
}
