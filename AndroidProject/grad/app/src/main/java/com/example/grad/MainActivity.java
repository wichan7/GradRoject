package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btn_gotoSelect;
    private Button btn_gotoPassengerCall;
    private Button btn_gotoDriverCallList;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_gotoSelect = findViewById(R.id.btn_gotoSelect);
        btn_gotoSelect.setOnClickListener(this);
        btn_gotoPassengerCall = findViewById(R.id.btn_gotoPassengerCall);
        btn_gotoPassengerCall.setOnClickListener(this);
        btn_gotoDriverCallList = findViewById(R.id.btn_gotoDriverCallList);
        btn_gotoDriverCallList.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_gotoSelect:
                intent = new Intent(MainActivity.this, FirstInstallActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.btn_gotoPassengerCall:
                intent = new Intent(MainActivity.this, PassengerCallActivity.class);
                startActivity(intent);
                finish();

            case R.id.btn_gotoDriverCallList:
                intent = new Intent(MainActivity.this, DriverCallListActivity.class);
                startActivity(intent);
                finish();
        }
    }
}
