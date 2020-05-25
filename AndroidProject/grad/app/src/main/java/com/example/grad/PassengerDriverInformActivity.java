package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class PassengerDriverInformActivity extends AppCompatActivity {
    private int cno;

    Button btn_accept, btn_refuse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_driver_inform);

        cno = getIntent().getExtras().getInt("cno");
        btn_accept = findViewById(R.id.btn_accept);
        btn_refuse = findViewById(R.id.btn_refuse);
        btn_accept.setOnClickListener(myOnClickListener);
        btn_refuse.setOnClickListener(myOnClickListener);


    }



    // 중간에 취소하는 함수
    class CancelTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;
                str_url = "http://" + Gloval.ip + ":8080/highquick/callCancel.jsp";
                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                //URL연결, 출력스트림 초기화
                sendMsg = "cno=" + strings[0];
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
    } // Cancel end

    View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_accept:
                    Intent intent = new Intent(PassengerDriverInformActivity.this, ShowDriversLocationActivity.class);
                    intent.putExtra("cno",cno);
                    startActivity(intent);
                    finish();
                    break;

                case R.id.btn_refuse:
                    try {
                        new CancelTask().execute(Integer.toString(cno)).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(PassengerDriverInformActivity.this,"콜을 취소하셨습니다.",Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(PassengerDriverInformActivity.this, PassengerCallActivity.class);
                    startActivity(intent2);
                    finish();
                    break;
            }
        }
    };

}
