package com.example.grad;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class DriverLicenseActivity extends AppCompatActivity {
    /* 카메라, 앨범 코드
    private Button btn_camera, btn_album;
    private ImageView iv_license;
    */
    private Button btn_submit, btn_submit_test;
    private EditText et_name, et_birth, et_license, et_carnum;
    private Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_license);

        et_name = findViewById(R.id.et_name);
        et_birth = findViewById(R.id.et_birth);
        et_license = findViewById(R.id.et_license);
        et_carnum = findViewById(R.id.et_carnum);
        btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(myOnClickListener);
        btn_submit_test = findViewById(R.id.btn_submit_test);
        btn_submit_test.setOnClickListener(myOnClickListener);

        /* 카메라, 앨범 코드
        iv_license = findViewById(R.id.iv_license); //imageView

        btn_camera = findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(myOnClickListener);

        btn_album = findViewById(R.id.btn_album);
        btn_album.setOnClickListener(myOnClickListener);
         */
    }

    View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                /* 카메라, 앨범 코드
                case R.id.btn_camera:
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivity(intent);
                    break;
                case R.id.btn_album:
                    break;
                 */
                case R.id.btn_submit:
                    submitButtonProcess(false);
                    break;

                case R.id.btn_submit_test:
                    submitButtonProcess(true);
                    break;
            }
        }
    };

    /* 카메라, 앨범 코드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        iv_license.setImageURI(data.getData());
    }
     */

    //메서드
    public void submitButtonProcess(boolean isTest){
        String name = et_name.getText().toString();
        if (name.equals("")){
            Toast.makeText(DriverLicenseActivity.this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        String birth = et_birth.getText().toString();
        if (birth.length() != 8){
            Toast.makeText(DriverLicenseActivity.this, "생일을 형식에 맞게 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        String license = et_license.getText().toString();
        if (license.length() != 12){
            Toast.makeText(DriverLicenseActivity.this, "면허 번호를 형식에 맞게 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        String carnum = et_carnum.getText().toString();
        if (carnum.equals("")){
            Toast.makeText(DriverLicenseActivity.this, "차 번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isTest == false){ //Test모드이면 이 과정 패스
            // 인증과정.
            String year = birth.substring(0,4);
            String month = birth.substring(4,6);
            String date = birth.substring(6,8);
            String licenNo0 = license.substring(0,2);
            String licenNo1 = license.substring(2,4);
            String licenNo2 = license.substring(4,10);
            String licenNo3 = license.substring(10,12);

            try {
                String result1 = new LicenseCheckTask().execute(year,month,date,name,licenNo0,licenNo1,licenNo2,licenNo3).get();
                int result2 = Integer.parseInt(result1);
                if (result2 == LicenseCheck.FAIL){
                    Toast.makeText(DriverLicenseActivity.this, "인증에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } //if(isTest) End

        //여기부터는 DB 코드입니다.
        SharedPreferences pref = getSharedPreferences(Gloval.PREFERENCE, MODE_PRIVATE);
        String id = pref.getString("id","");
        try {
            String result = new SetDriverDataTask().execute(id, birth, carnum).get();
            if (result.equals("success")){
                Toast.makeText(DriverLicenseActivity.this, "인증 성공", Toast.LENGTH_SHORT).show();
                intent = new Intent(DriverLicenseActivity.this, DriverShortcutActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    } //submitButtonProcess End

    class SetDriverDataTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            try {
                String str, str_url;
                str_url = "http://" + Gloval.ip + ":8080/highquick/setDriverData.jsp";
                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                //URL연결, 출력스트림 초기화
                sendMsg = "id=" + strings[0] + "&birth=" + strings[1] + "&carNum=" + strings[2];
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

    class LicenseCheckTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            LicenseCheck checker = new LicenseCheck();
            int result1 = checker.check(strings[0],strings[1],strings[2],strings[3],strings[4],strings[5],strings[6],strings[7]);

            return Integer.toString(result1);
        }

    }

}
