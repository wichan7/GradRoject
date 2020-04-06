package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FirstInstallActivity extends AppCompatActivity {

    private Button btn_PassengerLogin;
    private Button btn_DriverLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_install);

        btn_PassengerLogin = findViewById(R.id.btn_PassengerLogin);
        btn_PassengerLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstInstallActivity.this, PassengerLoginActivity.class);
                startActivity(intent);
            }
        });
        btn_DriverLogin = findViewById(R.id.btn_DriverLogin);
        btn_DriverLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstInstallActivity.this, DriverLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
