package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class PassengerCallActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Geocoder geocoder;
    private Button btn_search;
    private EditText et_search;

    int cnt = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_call);

        btn_search = findViewById(R.id.btn_search);
        et_search = findViewById(R.id.et_search);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // selectActivity.xml에 있는 fragment의 id를 통해 mapFragment를 찾아 연결
        mapFragment.getMapAsync(this); // getMapAsync가 호출되면 onMapReady 콜백이 실행됨.

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);

        // 맵 터치 이벤트 구현 //
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions mOptions = new MarkerOptions();
                // 마커 타이틀
                mOptions.title("마커 좌표");
                Double latitude = point.latitude; // 위도
                Double longitude = point.longitude; // 경도
                // 마커의 스니펫(간단한 텍스트) 설정
                mOptions.snippet(latitude.toString() + ", " + longitude.toString());
                // LatLng: 위도 경도 쌍을 나타냄
                mOptions.position(new LatLng(latitude, longitude));
                // 마커(핀) 추가
                googleMap.addMarker(mOptions);
            }
        });
        // 맵 터치 이벤트 끝 //

        // 버튼 이벤트 구현 //
        btn_search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = et_search.getText().toString();
                List<Address> addressList = null;

                try {
                    addressList = geocoder.getFromLocationName(str,10); //주소, 최대검색결과개수
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(this.getClass().getName(),addressList.get(0).toString());

                // 콤마를 기준으로 split
                String []splitStr = addressList.get(0).toString().split(",");
                String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
                System.out.println(address);

                String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
                String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
                System.out.println(latitude);
                System.out.println(longitude);

                // 좌표(위도, 경도) 생성
                LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                // 마커 생성
                MarkerOptions mOptions2 = new MarkerOptions();
                mOptions2.title("search result");
                mOptions2.snippet(address);
                mOptions2.position(point);
                // 마커 추가
                mMap.addMarker(mOptions2);
                // 해당 좌표로 화면 줌
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
            }
        });
        // 버튼 이벤트 구현 끝 //

    } //OnMapReady 끝

}

/*
package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PassengerCallActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    int cnt = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_call);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // selectActivity.xml에 있는 fragment의 id를 통해 mapFragment를 찾아 연결
        mapFragment.getMapAsync(this); // getMapAsync가 호출되면 onMapReady 콜백이 실행됨.

        if(mMap != null){
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    MarkerOptions markerOptions = new MarkerOptions(); // 마커 생성
                    markerOptions.position(latLng);
                    markerOptions.title(++cnt + "번 마커");
                    mMap.addMarker(markerOptions); // mMap객체에 마커 적용
                }
            });
            //setOnMapLongClickListener달기 끝.
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng SEOUL = new LatLng(37.56,126.97); // Latitude:위도 Longitude:경도. LatLng 클래스는 위도경도를 가지는 클래스

        MarkerOptions markerOptions = new MarkerOptions(); // 마커 생성
        markerOptions.position(SEOUL); // 위에서 초기화한 SEOUL객체의 위치로 마커를 이동
        markerOptions.title("현재 위치");
        markerOptions.snippet("기사를 호출합니다."); // snippet: 설명
        mMap.addMarker(markerOptions); // mMap객체에 마커 적용

        mMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL)); // 카메라를 SEOUL 위도경도 위치로 이동시킴
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15)); // 1로 지정하면 세계지도수준, 숫자가 커질수록 확대됨
    }
}
*/