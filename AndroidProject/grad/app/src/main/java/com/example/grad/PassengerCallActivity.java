package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class PassengerCallActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Geocoder geocoder;
    private EditText et_search;
    private Button btn_search;
    private Button btn_call;
    private Marker destMarker; //목적지마커는 항상 한개로 유지되어야하므로 destMarker로 관리

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_call);

        btn_search = findViewById(R.id.btn_search); //onClickListener는 onMapReady에서 달아줌
        et_search = findViewById(R.id.et_search);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // selectActivity.xml에 있는 fragment의 id를 통해 mapFragment를 찾아 연결
        mapFragment.getMapAsync(this); // getMapAsync가 호출되면 onMapReady 콜백이 실행됨.

        btn_call = findViewById(R.id.btn_call);
        btn_call.setOnClickListener(myOnClickListener);

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) { //googlemap이 작동완료했을때 실행되는 콜백함수
        mMap = googleMap;
        geocoder = new Geocoder(this);

        mMap.setOnMapClickListener(myOnMapClickListener);
        btn_search.setOnClickListener(myOnClickListener);

    } //OnMapReady 끝


    View.OnClickListener myOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_search:
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
                    if (destMarker == null) //아직 목적지마커가 없는경우
                        destMarker = mMap.addMarker(mOptions2);
                    else { //목적지 마커가 있는경우
                        destMarker.remove(); //원래있던 마커 삭제
                        destMarker = mMap.addMarker(mOptions2);
                    }

                    // 해당 좌표로 화면 줌
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));

                    break;
                case R.id.btn_call:
                    /* 나중에 주석해제
                    if(destMarker==null){ //목적지마커가 없으면
                        Toast.makeText(PassengerCallActivity.this, "목적지를 설정해주세요!", Toast.LENGTH_SHORT).show();
                    }
                     */
                    Intent intent = new Intent(PassengerCallActivity.this, PassengerWaitingActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }; //myOnClickListener 구현


    GoogleMap.OnMapClickListener myOnMapClickListener = new GoogleMap.OnMapClickListener(){
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
            if (destMarker == null) //아직 목적지마커가 없는경우
                destMarker = mMap.addMarker(mOptions);
            else { //목적지 마커가 있는경우
                destMarker.remove(); //원래있던 마커 삭제
                destMarker = mMap.addMarker(mOptions);
            }
        }
    }; //myOnMapClickListener 구현
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