package com.example.grad;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DriverFeedback extends AppCompatActivity {

    private RatingBar ratingbar;
    private Button btn_send, btn_cancel;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_feedback);

        final RatingBar ratingbar = (RatingBar)findViewById(R.id.ratingBar);

        btn_send = findViewById(R.id.btn_send);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_send.setOnClickListener(btnListener);
        btn_cancel.setOnClickListener(btnListener);

        ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (ratingbar.getRating()<1.0f){
                    ratingbar.setRating(1);
                }

            }

        });
    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_send :

                    Toast.makeText(DriverFeedback.this, "감사합니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DriverFeedback.this, PassengerCallActivity.class);
                    startActivity(intent);
                    finish();
                 break;
                case R.id.btn_cancel :
                    Toast.makeText(DriverFeedback.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(DriverFeedback.this, PassengerCallActivity.class);
                    startActivity(intent1);
                    finish();
            }
        }
    };


}
