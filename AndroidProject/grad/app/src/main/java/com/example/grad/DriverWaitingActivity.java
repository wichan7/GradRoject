package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Driver;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class DriverWaitingActivity extends AppCompatActivity {
    private int cno;
    private int status;
    private BackPressCloseHandler backPressCloseHandler;
    private Timer myTimer;                              // 5초마다 실행시키기 위해 Timer 선언
    private TimerTask myTimerTask;
    private Button btn_cancel;

    @Override
    protected void onDestroy(){
        super.onDestroy();
        myTimer.cancel();
    }

    @Override
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_waiting);

        backPressCloseHandler = new BackPressCloseHandler(this);
        //cno 가져오는 코드
        Intent intent = getIntent();
        cno = intent.getExtras().getInt("cno");

        //btn 연결
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new MyOnClickListener());

        //타이머 시작
        myTimer = new Timer();
        myTimerTask = new MyTimerTask();
        myTimer.schedule(myTimerTask, 0, 5000); // 0초후에 시작, 5초주기로 반복실행

    }

    class MyTimerTask extends TimerTask{
        @Override
        public void run() {
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        String result = new GetStatusTask().execute(Integer.toString(cno)).get();
                        status = Integer.parseInt(result);

                        if (status == -1) {                                                                // -1: 콜이 종료된 상태
                            Toast.makeText(DriverWaitingActivity.this, "종료된 콜입니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DriverWaitingActivity.this, DriverCallListActivity.class);
                            startActivity(intent);
                            finish();
                        }else if (status == 1){                                                           // 1: 기사만 수락한 상태
                            //TODO: 기다릴때 뭐 뱅글뱅글 도는 로딩창같은거 넣으면 좋을듯
                        }else if (status == 2){                                                           // 2: 2이면 승객이 수락한 것
                            Intent intent = new Intent(DriverWaitingActivity.this, ShowPassengersLocationActivity.class);
                            intent.putExtra("cno",Integer.toString(cno));
                            startActivity(intent);
                            finish();
                        }

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 0); //handler end

        }
    }

    //region AsyncTask DB연동 코드

    // status를 가져오는 함수
    class GetStatusTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) { //id, slocString, slocLat, slocLong, sdestLat, sdestLong 으로 6개가 필요
            try {
                String str, str_url;
                str_url = "http://" + Gloval.ip + ":8080/highquick/getStatus.jsp";
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
    } //getStatus end

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

    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_cancel:
                    try {
                        new CancelTask().execute(Integer.toString(cno)).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(DriverWaitingActivity.this,"콜을 취소하셨습니다.",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DriverWaitingActivity.this, DriverCallListActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }


}
