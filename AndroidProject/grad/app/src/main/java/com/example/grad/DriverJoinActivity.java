package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.concurrent.ExecutionException;

public class DriverJoinActivity extends AppCompatActivity {
    Button btn_check, btn_submit;
    EditText et_id, et_pwd, et_repwd, et_name, et_middle, et_last;
    final String str_type = "driver"; //driver클래스로 바꿀땐 이걸 driver로 변경해줘야함.

    //번호인증부분
    Button btn_auth, btn_authconfirm;
    EditText et_authcode;
    boolean isConfirmed;

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(DriverJoinActivity.this, DriverLoginActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0); //애니메이션 없에주는 코드
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_join);

        et_id = findViewById(R.id.et_id);
        btn_check = findViewById(R.id.btn_check);
        et_pwd = findViewById(R.id.et_pwd);
        et_repwd = findViewById(R.id.et_repwd);
        et_name = findViewById(R.id.et_name);
        et_middle = findViewById(R.id.et_middle);
        et_last = findViewById(R.id.et_last);
        btn_submit = findViewById(R.id.btn_submit);
        btn_check.setOnClickListener(btnListener);
        btn_submit.setOnClickListener(btnListener);

        //번호인증부분
        btn_auth = findViewById(R.id.btn_auth);
        btn_auth.setOnClickListener(btnListener);
        btn_authconfirm = findViewById(R.id.btn_authconfirm);
        btn_authconfirm.setOnClickListener(btnListener);
        et_authcode = findViewById(R.id.et_authcode);
        isConfirmed = false;

    }

    View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_check: // 아이디 확인 버튼일경우
                    try {
                        String str_id = et_id.getText().toString();
                        String result = new CustomTask().execute(str_id, str_type).get();

                        if(result.equals("success")){
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.idCanUse), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("idExist")){
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.idExist), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("idVal")){
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.idVal), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e){ }
                    break;
                // 아이디 확인 버튼일경우 end
                case R.id.btn_submit:
                    try {
                        String str_id = et_id.getText().toString();
                        String str_pwd = et_pwd.getText().toString();
                        String str_repwd = et_repwd.getText().toString();
                        String str_name = et_name.getText().toString();
                        String str_phone = "010" + et_middle.getText().toString() + et_last.getText().toString();

                        if (!str_pwd.equals(str_repwd)) {
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.pwdNotEquals), Toast.LENGTH_SHORT).show();
                            return;
                        } //비밀번호 같은지 다른지 검사.

                        String result = new CustomTask().execute(str_id, str_pwd, str_name, str_phone, str_type).get();

                        if (result.equals("idVal")){
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.idVal), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("pwdVal")){
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.pwdVal), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("phoneVal")){
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.phoneVal), Toast.LENGTH_SHORT).show();
                        }
                        else if (isConfirmed == false){
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.phoneNotConfirmed), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("nameVal")){
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.nameVal), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("idExist")){
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.idExist), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("phoneExist")){
                            Toast.makeText(DriverJoinActivity.this, getString(R.string.phoneExist), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("success")){
                            Toast.makeText(DriverJoinActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DriverJoinActivity.this, DriverLoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e){ }
                    break;
                // 제출 버튼일경우 end
                case R.id.btn_auth:
                    if (et_middle.getText().toString().length() != 4 ||
                        et_last.getText().toString().length() != 4 ) {
                        Toast.makeText(DriverJoinActivity.this, "전화번호 8자리를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    btn_auth.setVisibility(View.GONE);
                    et_middle.setEnabled(false);
                    et_last.setEnabled(false);
                    et_authcode.setVisibility(View.VISIBLE);
                    btn_authconfirm.setVisibility(View.VISIBLE);
                    Toast.makeText(DriverJoinActivity.this, "인증번호를 전송했습니다.", Toast.LENGTH_SHORT).show();

                    String phoneMiddle = et_middle.getText().toString();
                    String phoneLast = et_last.getText().toString();
                    String phonenum = "8210" + phoneMiddle + phoneLast;

                    new RequestAuthTask().execute(phonenum);
                    break;
                case R.id.btn_authconfirm:
                    String phoneMiddle2 = et_middle.getText().toString();
                    String phoneLast2 = et_last.getText().toString();
                    String phonenum2 = "8210" + phoneMiddle2 + phoneLast2;
                    String authcode = et_authcode.getText().toString();

                    try {
                        String res = new SendAuthcodeTask().execute(phonenum2,authcode).get();
                        if (res.equals("success")){
                            isConfirmed = true;
                            et_authcode.setVisibility(View.GONE);
                            btn_authconfirm.setVisibility(View.GONE);
                            Toast.makeText(DriverJoinActivity.this, "인증에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else if (res.equals("fail")){
                            Toast.makeText(DriverJoinActivity.this, "인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    class RequestAuthTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;

                str_url = "http://" + Gloval.ip + ":8080/sms/sendSMS.jsp";

                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

                sendMsg = "phone=" + strings[0];

                osw.write(sendMsg);
                osw.flush();
                if (conn.getResponseCode() == conn.HTTP_OK) { //http 연결되면
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

    class SendAuthcodeTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;

                str_url = "http://" + Gloval.ip + ":8080/sms/authCheck.jsp";

                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

                sendMsg = "phone=" + strings[0] + "&authcode=" + strings[1];

                osw.write(sendMsg);
                osw.flush();
                if (conn.getResponseCode() == conn.HTTP_OK) { //http 연결되면
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

    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;

                if (strings.length > 2) //매개변수가 3개이상이면 조인
                    str_url = "http://" + Gloval.ip + ":8080/highquick/join.jsp";
                else // 2개이하면 idcheck.
                    str_url = "http://" + Gloval.ip + ":8080/highquick/check.jsp";

                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

                if (strings.length > 2)
                    sendMsg = "id=" + strings[0] + "&pwd=" + strings[1] + "&name=" + strings[2] +
                            "&phone=" + strings[3] + "&type=" + strings[4];
                else
                    sendMsg = "id=" + strings[0] + "&type=" + strings[1];

                osw.write(sendMsg);
                osw.flush();
                if (conn.getResponseCode() == conn.HTTP_OK) { //http 연결되면
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
}
