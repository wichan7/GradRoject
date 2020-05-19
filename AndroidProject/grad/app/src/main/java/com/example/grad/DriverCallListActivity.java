package com.example.grad;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

//TODO: 온오프버튼 구현안됨, Pause에도 cancel() 넣어야할듯?? 수정해야함
public class DriverCallListActivity extends AppCompatActivity {
    private Context mContext;
    private ListView mListView;
    private CallListViewAdapter myCallListViewAdapter;
    private ArrayList<CallListItem> array_calls;        // CallListItem은 String addr, String time으로 이루어진 클래스
    private Timer myTimer;                              // 5초마다 실행시키기 위해 Timer 선언
    private TimerTask myTimerTask;

    @Override
    protected void onPause() {
        super.onPause();
        setListOnOff(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListOnOff(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_call_list);
        this.mContext = getApplicationContext();

        mListView = (ListView) findViewById(R.id.list_calls);
        mListView.setOnItemClickListener(myOnItemClickListener);
        //ListView에 레이아웃 연결과 리스너 연결

        myTimer = new Timer();
        myTimerTask = new MyTimerTask();
    }//onCreate 끝

    protected void setListOnOff(boolean flag) {
        if (flag == true) {
            myTimer = new Timer();
            myTimerTask = new MyTimerTask();
            myTimer.schedule(myTimerTask, 0, 5000); // 5초주기로 반복실행
        } else if (flag == false) {
            myTimer.cancel();
        }
    }

    //region AsyncTask
    class GetCallListTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;
                str_url = "http://" + Gloval.ip + ":8080/highquick/callList.jsp";
                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");

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
    //endregion

    //region myOnItemClickListener
    AdapterView.OnItemClickListener myOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent(DriverCallListActivity.this, DriverCallCheckActivity.class);
            intent.putExtra("no", array_calls.get(position).no);
            intent.putExtra("addr", array_calls.get(position).addr);
            intent.putExtra("time", array_calls.get(position).time);
            startActivity(intent);
            /*
            String s = array_calls.get(position).no;
            Toast.makeText(DriverCallListActivity.this, "아답터position:" + position + "해당 array의 cno:" + s, Toast.LENGTH_SHORT).show();
             */

            //TODO: 이곳에 intent putExtra로 drivercheck로 cno 넘기는 코드 작성.
        }
    };
    //endregion

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            try {

                String result = new GetCallListTask().execute().get(); //result에 no&slocString&calltime 이렇게 묶여서 들어옴

                // array_calls 초기화
                array_calls = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(result, "&");
                while (st.hasMoreTokens()) {
                    String no = st.nextToken();
                    String slocString = st.nextToken();
                    String calltime = st.nextToken();

                    CallListItem cli = new CallListItem(no, slocString, calltime);
                    array_calls.add(cli);
                } // array_calls 초기화 끝

                // 메인쓰레드가 아닌곳에서 UI변경을 할 수 없으므로 runOnUI를 이용해서 변경
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myCallListViewAdapter = null;
                        mListView.setAdapter(null);
                        myCallListViewAdapter = new CallListViewAdapter(mContext, array_calls);
                        mListView.setAdapter(myCallListViewAdapter);
                    }
                });
                //RunOnUI end

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    } //MyTimerTask end

}