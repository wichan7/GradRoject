package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PassengerDriverInformActivity extends AppCompatActivity {
    Button btn_accept, btn_refuse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_driver_inform);

        btn_accept = findViewById(R.id.btn_accept);
        btn_refuse = findViewById(R.id.btn_refuse);
        btn_accept.setOnClickListener(myOnClickListener);
        btn_refuse.setOnClickListener(myOnClickListener);

    }

    View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_accept:
                    Intent intent = new Intent(PassengerDriverInformActivity.this, ShowDriversLocationActivity.class);
                    startActivity(intent);
                    finish();

                    break;

                case R.id.btn_refuse:


                    break;
            }
        }
    };

}
