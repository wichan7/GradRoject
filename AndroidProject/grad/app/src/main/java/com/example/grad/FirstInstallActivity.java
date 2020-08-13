package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FirstInstallActivity extends AppCompatActivity {

    private View btn_PassengerLogin;
    private View btn_DriverLogin;
    private SharedPreferences pref;

    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_install);

        backPressCloseHandler = new BackPressCloseHandler(this);
        btn_PassengerLogin = findViewById(R.id.btn_PassengerLogin);
        btn_PassengerLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstInstallActivity.this, PassengerLoginActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //애니메이션 없에주는 코드
                finish();
            }
        });
        btn_DriverLogin = findViewById(R.id.btn_DriverLogin);
        btn_DriverLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstInstallActivity.this, DriverLoginActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //애니메이션 없에주는 코드
                finish();
            }
        });

        // 자동로그인 구현
        pref = getSharedPreferences(Gloval.PREFERENCE, MODE_PRIVATE);
        int autoLoginValue = pref.getInt("autoLoginValue", Gloval.NEGATIVE);
        
        Intent autoIntent = null;
        if (autoLoginValue != Gloval.NEGATIVE) {
            if (autoLoginValue == Gloval.PASSENGER)
                autoIntent = new Intent(FirstInstallActivity.this, PassengerShortcutActivity.class);
            else if (autoLoginValue == Gloval.DRIVER)
                autoIntent = new Intent(FirstInstallActivity.this, DriverShortcutActivity.class);
            startActivity(autoIntent);
            overridePendingTransition(0, 0); //애니메이션 없에주는 코드
        }
        // 자동로그인 끝
    }

    @Override
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();
    }
}
