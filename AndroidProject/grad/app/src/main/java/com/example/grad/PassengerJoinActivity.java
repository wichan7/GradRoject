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
import java.util.concurrent.ExecutionException;

public class PassengerJoinActivity extends AppCompatActivity {
    Button btn_check, btn_submit;
    EditText et_id, et_pwd, et_repwd, et_name, et_middle, et_last;
    final String str_type = "passenger"; //driver클래스로 바꿀땐 이걸 driver로 변경해줘야함.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_join);

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
                            Toast.makeText(PassengerJoinActivity.this, getString(R.string.idCanUse), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("idExist")){
                            Toast.makeText(PassengerJoinActivity.this, getString(R.string.idExist), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(PassengerJoinActivity.this, getString(R.string.pwdNotEquals), Toast.LENGTH_SHORT).show();
                            return;
                        } //비밀번호 같은지 다른지 검사.

                        String result = new CustomTask().execute(str_id, str_pwd, str_name, str_phone, str_type).get();

                        if (result.equals("idVal")){
                            Toast.makeText(PassengerJoinActivity.this, getString(R.string.idVal), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("pwdVal")){
                            Toast.makeText(PassengerJoinActivity.this, getString(R.string.pwdVal), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("phoneVal")){
                            Toast.makeText(PassengerJoinActivity.this, getString(R.string.phoneVal), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("nameVal")){
                            Toast.makeText(PassengerJoinActivity.this, getString(R.string.nameVal), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("idExist")){
                            Toast.makeText(PassengerJoinActivity.this, getString(R.string.idExist), Toast.LENGTH_SHORT).show();
                        }
                        else if (result.equals("success")){
                            Toast.makeText(PassengerJoinActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PassengerJoinActivity.this, PassengerLoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (Exception e){ }
                    break;
                    // 제출 버튼일경우 end
            }
        }
    };

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
