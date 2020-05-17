package com.example.grad;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class DriverCallCheckActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Button btn_accept;
    private String no, addr, time;
    private GoogleMap mMap = null;
    private Geocoder geocoder = null;

    //TODO: 여기서 수락하면 CALL의 STATUS가 1이된다.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_call_check);

        btn_accept = findViewById(R.id.btn_accept);
        btn_accept.setOnClickListener(myOnClickListener);

        Intent intent = getIntent();
        no = intent.getExtras().getString("no");
        addr = intent.getExtras().getString("addr");
        time = intent.getExtras().getString("time");
    }

    View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

 /*           LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.layout_dialog, null);
            setContentView(relativeLayout);*/


  /*          LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_label_editor, null);
            dialogBuilder.setView(dialogView);*/

       /*     new MaterialDialog dialog = new MaterialDialog.Builder(this).customView(R.layout.layout_dialog_single_button, false).build();
            View view = dialog.getCustomView();*/

      /*      Intent intent = new Intent(DriverCallCheckActivity.this, DriverWaitingActivity.class);
            startActivity(intent);
            finish();*/
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);
        createdMarker(addr);
    }

    public void createdMarker(String addr) {

        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocationName(addr, 10); //주소, 최대검색결과개수

            Log.i(this.getClass().getName(), addressList.get(0).toString());

            // 콤마를 기준으로 split
            String[] splitStr = addressList.get(0).toString().split(",");
            String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소
            System.out.println(address);

            String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
            String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
            System.out.println(latitude);
            System.out.println(longitude);

            // 좌표(위도, 경도) 생성
            LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            // 마커 생성
            MarkerOptions mOptions = new MarkerOptions();
            mOptions.title("search result");
            mOptions.snippet(address);
            mOptions.position(point);
            // 마커 추가
            mMap.addMarker(mOptions);

            // 해당 좌표로 화면 줌
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(DriverCallCheckActivity.this, "결과가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
