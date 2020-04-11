package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class PassengerLoginActivity extends AppCompatActivity {
    EditText et_id, et_pwd;
    Button btn_login, btn_signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_login);
        //Button data = (Button) findViewById(R.id.btn_login);

        et_id = (EditText) findViewById(R.id.et_id);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_signIn = (Button) findViewById(R.id.btn_signIn);
        btn_login.setOnClickListener(btnListener);
        btn_signIn.setOnClickListener(btnListener);

        /*
        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("로그 찍히나?","로그찍힘");
                try {
                    String result;
                    CustomTask task = new CustomTask();
                    result = task.execute("rain483","1234").get();
                    Log.i("리턴 값",result);
                } catch (Exception e) {

                }
            }
        });
         */
    } // onCreate 끝

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;
                str_url = "http://"+ Gloval.ip +":8080/highquick/test.jsp";
                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "id=" + strings[0] + "&pwd=" + strings[1] + "&type=" + strings[2];
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

    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) { // View 클래스가 Button, EditText...etc들의 최상위 클래스임
            switch (view.getId()) {
                case R.id.btn_login: // 로그인 버튼 눌렀을 경우
                    //Log.i("로그","찍히냐?");
                    String loginid = et_id.getText().toString();
                    String loginpwd = et_pwd.getText().toString();
                    try {
                        String result = new CustomTask().execute(loginid, loginpwd, "login").get();
                        if (result.equals("true")) {
                            Toast.makeText(PassengerLoginActivity.this, "로그인", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PassengerLoginActivity.this, PassengerCallActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (result.equals("false")) {
                            Toast.makeText(PassengerLoginActivity.this, "아이디 또는 비밀번호가 틀렸음", Toast.LENGTH_SHORT).show();
                            et_id.setText("");
                            et_pwd.setText("");
                        } else if (result.equals("noId")) {
                            Toast.makeText(PassengerLoginActivity.this, "존재하지 않는 아이디", Toast.LENGTH_SHORT).show();
                            et_id.setText("");
                            et_pwd.setText("");
                        }
                    } catch (Exception e) { }
                    break;
                case R.id.btn_signIn: // 회원가입
                    /* 나중에 PassengerSignInActivity 작성할때 참고할 것.
                    String joinid = et_id.getText().toString();
                    String joinpwd = et_pwd.getText().toString();
                    try {
                        String result = new CustomTask().execute(joinid, joinpwd, "join").get();
                        if (result.equals("id")) {
                            Toast.makeText(PassengerLoginActivity.this, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show();
                            et_id.setText("");
                            et_pwd.setText("");
                        } else if (result.equals("ok")) {
                            et_id.setText("");
                            et_pwd.setText("");
                            Toast.makeText(PassengerLoginActivity.this, "회원가입을 축하합니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                     */
                    Intent intent = new Intent(PassengerLoginActivity.this, PassengerJoinActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
}
