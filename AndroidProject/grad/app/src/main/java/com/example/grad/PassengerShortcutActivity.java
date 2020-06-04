package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

public class PassengerShortcutActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_shortcut);

        pref = getSharedPreferences(Gloval.PREFERENCE, MODE_PRIVATE);
        id = pref.getString("id","");

        try {
            String result = new ShortcutTask().execute(id).get();
            StringTokenizer st = new StringTokenizer(result, "&");
            String token1 = st.nextToken();
            if (token1.equals("passengerCall")) {
                Intent intent = new Intent(PassengerShortcutActivity.this, PassengerCallActivity.class);
                startActivity(intent);
                finish();

            } else if (token1.equals("passengerWaiting")) {
                String cno = st.nextToken();
                Intent intent = new Intent(PassengerShortcutActivity.this, PassengerWaitingActivity.class);
                intent.putExtra("cno", Integer.parseInt(cno));
                startActivity(intent);
                finish();

            } else if (token1.equals("driverInform")) {
                String cno = st.nextToken();
                Intent intent = new Intent(PassengerShortcutActivity.this, PassengerDriverInformActivity.class);
                intent.putExtra("cno", Integer.parseInt(cno));
                startActivity(intent);
                finish();

            } else if (token1.equals("driversLocation")) {
                String cno = st.nextToken();
                Intent intent = new Intent(PassengerShortcutActivity.this, ShowDriversLocationActivity.class);
                intent.putExtra("cno", cno);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this, "ShortcutTask에서 반환된 값:" + result, Toast.LENGTH_SHORT).show();
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class ShortcutTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;
                str_url = "http://"+ Gloval.ip +":8080/highquick/passengerShortcut.jsp";
                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                //URL연결, 출력스트림 초기화
                sendMsg = "id=" + strings[0];
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
    } //db통신코드 끝

}
