package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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



public class DriverLoginActivity extends AppCompatActivity {
    EditText et_id, et_pwd;
    Button btn_login, btn_signIn;
    String str_type = "driver"; //driveractivity면 driver로 변경

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        et_id = (EditText) findViewById(R.id.et_id);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_signIn = (Button) findViewById(R.id.btn_signIn);
        btn_login.setOnClickListener(btnListener);
        btn_signIn.setOnClickListener(btnListener);

        pref = getSharedPreferences(Gloval.PREFERENCE, MODE_PRIVATE);
        editor = pref.edit();

    } // onCreate 끝

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(DriverLoginActivity.this, FirstInstallActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0); //애니메이션 없에주는 코드
        finish();
    }

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;
                str_url = "http://"+ Gloval.ip +":8080/highquick/login.jsp";
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

    class CheckLicense extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;
                str_url = "http://"+ Gloval.ip +":8080/highquick/checkLicense.jsp";
                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
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
                        String result = new CustomTask().execute(loginid, loginpwd, str_type).get();
                        if (result.equals("success")) {
                            editor.putString("id",loginid);
                            editor.commit();
                            Toast.makeText(DriverLoginActivity.this, getString(R.string.success), Toast.LENGTH_SHORT).show();
                            String result2 = new CheckLicense().execute(loginid).get();
                            if (result2.equals("success")){
                                Intent intent = new Intent(DriverLoginActivity.this, DriverShortcutActivity.class);
                                startActivity(intent);
                                overridePendingTransition(0, 0); //애니메이션 없에주는 코드
                                finish();
                            }else if (result2.equals("nullLicense")){
                                Intent intent = new Intent(DriverLoginActivity.this, DriverLicenseActivity.class);
                                startActivity(intent);
                                overridePendingTransition(0, 0); //애니메이션 없에주는 코드
                                finish();
                            }
                        } else if (result.equals("pwdNotEquals")) {
                            Toast.makeText(DriverLoginActivity.this, getString(R.string.pwdNotEquals), Toast.LENGTH_SHORT).show();
                            et_id.setText("");
                            et_pwd.setText("");
                        } else if (result.equals("noId")) {
                            Toast.makeText(DriverLoginActivity.this, getString(R.string.idNoExist), Toast.LENGTH_SHORT).show();
                            et_id.setText("");
                            et_pwd.setText("");
                        }
                    } catch (Exception e) { }
                    break;
                case R.id.btn_signIn: // 회원가입
                    Intent intent = new Intent(DriverLoginActivity.this, DriverJoinActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0); //애니메이션 없에주는 코드
                    finish();
                    break;
            }
        }
    };
}