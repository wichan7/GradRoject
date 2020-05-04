package com.example.grad;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class DriverLicenseActivity extends AppCompatActivity {
    private Button btn_camera, btn_album, btn_submit;
    private ImageView iv_license;
    private Intent intent = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_license);

        iv_license = findViewById(R.id.iv_license); //imageView

        btn_camera = findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(myOnClickListener);

        btn_album = findViewById(R.id.btn_album);
        btn_album.setOnClickListener(myOnClickListener);

        btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(myOnClickListener);

    }

    View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_camera:
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivity(intent);
                    break;
                case R.id.btn_album:
                    //TODO: Album버튼 동작 코드 작성

                    break;
                case R.id.btn_submit:
                    Toast.makeText(DriverLicenseActivity.this, "인증 성공", Toast.LENGTH_SHORT).show();
                    intent = new Intent(DriverLicenseActivity.this, DriverCallListActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        iv_license.setImageURI(data.getData());
    }
}
