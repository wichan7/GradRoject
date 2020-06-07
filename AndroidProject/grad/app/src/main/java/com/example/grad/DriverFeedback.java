package com.example.grad;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class DriverFeedback extends AppCompatActivity {

    private RatingBar ratingbar;
    private Button btn_send, btn_cancel;
    private String cno;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_feedback);

        ratingbar = (RatingBar)findViewById(R.id.ratingBar);

        btn_send = findViewById(R.id.btn_send);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_send.setOnClickListener(btnListener);
        btn_cancel.setOnClickListener(btnListener);

        cno = getIntent().getStringExtra("cno");

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
                    try {
                        String rating = Float.toString(ratingbar.getRating());
                        Log.i("feedbackactivity","rating:"+rating);
                        String result = new FeedbackTask().execute(cno,rating).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

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

    class FeedbackTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;
                str_url = "http://" + Gloval.ip + ":8080/highquick/feedback.jsp";
                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                //URL연결, 출력스트림 초기화
                sendMsg = "cno=" + strings[0] + "&rating=" + strings[1];
                osw.write(sendMsg);
                osw.flush();
                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "EUC-KR");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    } //showLocTask
}
