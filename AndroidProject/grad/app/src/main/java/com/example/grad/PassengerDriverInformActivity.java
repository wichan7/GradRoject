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
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class PassengerDriverInformActivity extends AppCompatActivity {
    private int cno;

    Button btn_accept, btn_refuse;
    TextView tv_name, tv_phone, tv_count, tv_rating, tv_car, tv_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_driver_inform);

        cno = getIntent().getExtras().getInt("cno");
        btn_accept = findViewById(R.id.btn_accept);
        btn_refuse = findViewById(R.id.btn_refuse);
        btn_accept.setOnClickListener(myOnClickListener);
        btn_refuse.setOnClickListener(myOnClickListener);

        tv_name = findViewById(R.id.tv_name);
        tv_phone = findViewById(R.id.tv_phone);
        tv_count = findViewById(R.id.tv_count);
        tv_rating = findViewById(R.id.tv_rating);
        tv_car = findViewById(R.id.tv_car);
        tv_time = findViewById(R.id.tv_time);

        try {
            String result = new GetDriverDataTask().execute(Integer.toString(cno)).get();          //일단 지금은 이름, 폰번호, 불린횟수, 평점만 &로 묶여서 들어옴

            StringTokenizer st = new StringTokenizer(result, "&");
            while (st.hasMoreTokens()) {
                tv_name.setText(st.nextToken());
                tv_phone.setText(st.nextToken());
                tv_count.setText(st.nextToken());
                tv_rating.setText(st.nextToken());
                tv_car.setText(st.nextToken());
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //region DB연동코드

    //드라이버 정보 가져오는 함수
    class GetDriverDataTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;
                str_url = "http://" + Gloval.ip + ":8080/highquick/getDriverData.jsp";
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
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
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
    }

    // 승객이 수락하는 함수
    class AcceptTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;
                str_url = "http://" + Gloval.ip + ":8080/highquick/passengerCallAccept.jsp";
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
    //endregion

    View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_accept:
                    try {
                        String result = new AcceptTask().execute(Integer.toString(cno)).get();
                        if (result.equals("success")) {
                            Intent intent = new Intent(PassengerDriverInformActivity.this, ShowDriversLocationActivity.class);
                            intent.putExtra("cno",Integer.toString(cno));
                            startActivity(intent);
                            finish();
                        }
                        else if (result.equals("fail")){
                            Toast.makeText(PassengerDriverInformActivity.this,"기사가 콜을 취소했습니다.",Toast.LENGTH_SHORT).show();
                            Intent intent2 = new Intent(PassengerDriverInformActivity.this, PassengerCallActivity.class);
                            startActivity(intent2);
                            finish();
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
