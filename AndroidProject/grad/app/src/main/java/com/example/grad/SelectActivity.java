package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.view.View;
import android.widget.Button;

public class SelectActivity extends AppCompatActivity implements OnMapReadyCallback { //OnMapReadyCallback 인터페이스 구현
    private GoogleMap mMap;

    private Button btn_PassengerLogin;
    private Button btn_DriverLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        ////Buttons////
        btn_PassengerLogin = findViewById(R.id.btn_PassengerLogin);
        btn_PassengerLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, PassengerLoginActivity.class);
                startActivity(intent);
            }
        });
        btn_DriverLogin = findViewById(R.id.btn_DriverLogin);
        btn_DriverLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, PassengerLoginActivity.class); // DriverActivity 만들고 수정해야함.
                startActivity(intent);
            }
        });
        ////End Buttons////

        ////Google Map////
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // selectActivity.xml에 있는 fragment의 id를 통해 mapFragment를 찾아 연결
        mapFragment.getMapAsync(this); // getMapAsync가 호출되면 onMapReady 콜백이 실행됨.
        ////End Google Map////
    }

    public void onMapReady(final GoogleMap googleMap){ // null이아닌 googlemap객체를 제공해줄 수 있을때 호출되는 함수
        mMap = googleMap;

        LatLng SEOUL = new LatLng(37.56,126.97); // Latitude:위도 Longitude:경도. LatLng 클래스는 위도경도를 가지는 클래스

        MarkerOptions markerOptions = new MarkerOptions(); // 마커 생성
        markerOptions.position(SEOUL); // 위에서 초기화한 SEOUL객체의 위치로 마커를 이동
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도"); // snippet: 설명
        mMap.addMarker(markerOptions); // mMap객체에 마커 적용

        mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL)); // 카메라를 SEOUL 위도경도 위치로 이동시킴
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10)); // 1로 지정하면 세계지도수준, 숫자가 커질수록 확대됨
    }
}
