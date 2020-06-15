package com.example.grad;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class DriverCallCheckActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Button btn_accept = null;               // 수락 버튼
    private LatLng sLoc, sDest;                      // 승객 위치와 승객 목적지를 담을 변수
    private String cno;                               // 전 intent에서 넘어온 callnumber

    //region GPS를 위한 변수선언부
    private GoogleMap mMap = null;
    private Geocoder geocoder = null;
    private Marker currentMarker = null;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    private static final int PERMISSIONS_REQUEST_CODE = 100; // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private boolean needRequest = false;
    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소
    Location mCurrentLocatiion;
    LatLng currentPosition;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_call_check);

        btn_accept = findViewById(R.id.btn_accept);      // OnClickListener는 onMapReady에서 달아줌.
        cno = getIntent().getStringExtra("no");     // 전 인텐트에서 넘어온 call_no를 받음

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // selectActivity.xml에 있는 fragment의 id를 통해 mapFragment를 찾아 연결
        mapFragment.getMapAsync(this); // getMapAsync가 호출되면 onMapReady 콜백이 실행됨.

        //region GPS를 위한 코드
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLayout = findViewById(R.id.layout_driverCallCheck);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //endregion
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) { //googlemap이 작동완료했을때 실행되는 콜백함수
        mMap = googleMap;
        geocoder = new Geocoder(this);
        btn_accept.setOnClickListener(myOnClickListener);

        //region GPS를 위한 코드

        setDefaultLocation(); //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에 지도의 초기위치를 서울로 설정

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            startLocationUpdates(); // 3. 위치 업데이트 시작
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(DriverCallCheckActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //endregion

        // DB에서 Location을 받아 마커를 생성하는 부분
        try {
            String result = new GetLocDataTask().execute(cno).get();
            Log.i("db결과", result);
            StringTokenizer st = new StringTokenizer(result, "&");
            String slocLat = st.nextToken();
            String slocLong = st.nextToken();
            String sdestLat = st.nextToken();
            String sdestLong = st.nextToken();
            // 뒤에 gloc은 필요없으므로 나머지 2개는 읽지않음
            sLoc = new LatLng(Double.parseDouble(slocLat), Double.parseDouble(slocLong));
            sDest = new LatLng(Double.parseDouble(sdestLat), Double.parseDouble(sdestLong));

            //마커 생성
            MarkerOptions slocMarker = new MarkerOptions();
            slocMarker.position(sLoc);
            slocMarker.title("승객의 위치");
            slocMarker.snippet("승객의 위치입니다.");
            MarkerOptions sdestMarker = new MarkerOptions();
            sdestMarker.position(sDest);
            sdestMarker.title("승객의 목적지");
            sdestMarker.snippet("승객의 목적지입니다.");
            //마커 사진넣기 + 사이즈 조정 false일때 사이즈 크기가 변동안됨 / true는 작은걸 확대시키는데 Out of Memory 발생
            Bitmap locImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.locmarker), 170, 170, false);
            slocMarker.icon(BitmapDescriptorFactory.fromBitmap(locImage));
            Bitmap destImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.destmarker), 170, 170, false);
            sdestMarker.icon(BitmapDescriptorFactory.fromBitmap(destImage));


            //sdestMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));//마커색깔 변경용
            //slocMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            //TODO: 마커 색상 변경(승객위치와 목적지 구분을 위해)
            mMap.addMarker(slocMarker);
            mMap.addMarker(sdestMarker);
            //TODO: 위치와 목적지사이의 직선거리를 계산해서 알맞은 카메라 줌(v) 결정하기, 카메라 위치도 그 중간에 두기
            // 두 점 사이의 거리=√(( X2 - X1 ) ^2 + ( Y2 - Y1 ) ^2)
            // 두 점의 중점 (X,Y) = ( (X1+X2)/2 , (Y1+Y2)/2 )
            //////////////////////////////////////////////////////////////////////////////////////////////////
            Log.d("Location_", "sLoc 위도 : " + sLoc.latitude + " 경도 :" + sLoc.longitude); //목적지 위치1
            Log.d("Location_", "sDest 위도 : " + sDest.latitude + " 경도 :" + sDest.longitude); //목적지 위치1
            double test1 = sLoc.latitude - sDest.latitude;
            double test2 = sLoc.longitude - sDest.longitude;
            double result1 = Math.pow(test1, 2) + Math.pow(test2, 2);
            double mid_distance = Math.sqrt(result1) * Math.pow(10, 5); //두점 사이의 거리
            Log.d("Location_", "위도 제곱값 : " + Math.pow(test1, 2) + " 경도 제곱값 : " + Math.pow(test2, 2) + " 두 점 사이의 거리 : " + mid_distance);

            double mid_x = (sLoc.latitude + sDest.latitude) / 2;
            double mid_y = (sLoc.longitude + sDest.longitude) / 2;
            Log.d("Location_", "X : " + mid_x + " Y : " + mid_y);
            LatLng Mid_loc = new LatLng(mid_x, mid_y);
            //////////////////////////////////////////////////////////////////////////////////////////
            /*MarkerOptions smidMarker = new MarkerOptions();//가운데 위치 확인용
            smidMarker.position(Mid_loc);
            smidMarker.title("가운데의 위치");
            smidMarker.snippet("가운데의 위치입니다.");
            mMap.addMarker(smidMarker);*/

            ////////////////추가로 계산예정///////////////////////////////////////////////////////////

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(Mid_loc, getZoomLevel(mid_distance));
            mMap.moveCamera(cameraUpdate);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    } //OnMapReady 끝

    //거리별 계산한 근사값
    /*
    776 16.7   -> 16  구의역 직선
    1450 15.5  -> 15  직선 어린이대공원
    5980 13.5  -> 13 대각선 압구정
    10204 13   -> 12.3 대각선 가천대
    1440 15.5  -> 15 직선 광양중
    1000 16    -> 15.6    대각선 혜민병원
    6278 13    -> 12.6 대각선 압구정역
    */
    public float getZoomLevel(double mid_distance) {
        float setZoom = 0;
        if (375 < mid_distance && mid_distance < 750) {
            if (mid_distance < (375 + 750) / 2) {
                setZoom = 16f + 0.8f;
            } else {
                setZoom = 16f + 0.3f;
            }
        } else if (750 < mid_distance && mid_distance < 1500) {
            if (mid_distance < (750 + 1500) / 2) {
                setZoom = 15f + 0.8f;
            } else {
                setZoom = 15f + 0.3f;
            }
        } else if (1500 < mid_distance && mid_distance < 3000) {
            if (mid_distance < (1500 + 3000) / 2) {
                setZoom = 14f + 0.8f;
            } else {
                setZoom = 14f + 0.3f;
            }
        } else if (3000 < mid_distance && mid_distance < 6000) {
            if (mid_distance < (3000 + 6000) / 2) {
                setZoom = 13f + 0.8f;
            } else {
                setZoom = 13f + 0.3f;
            }
        } else if (6000 < mid_distance && mid_distance < 12000) {
            if (mid_distance < (6000 + 12000) / 2) {
                setZoom = 12f + 0.8f;
            } else {
                setZoom = 12f + 0.3f;
            }
        } else if (12000 < mid_distance && mid_distance < 24000) {
            if (mid_distance < (12000 + 24000) / 2) {
                setZoom = 11f + 0.8f;
            } else {
                setZoom = 11f + 0.3f;
            }
        } else if (24000 < mid_distance && mid_distance < 48000) {
            if (mid_distance < (24000 + 48000) / 2) {
                setZoom = 10f + 0.8f;
            } else {
                setZoom = 10f + 0.3f;
            }
        } else if (48000 < mid_distance && mid_distance < 96000) {
            if (mid_distance < (48000 + 96000) / 2) {
                setZoom = 9f + 0.8f;
            } else {
                setZoom = 9f + 0.3f;
            }
        } else if (96000 < mid_distance && mid_distance < 192000) {
            if (mid_distance < (96000 + 192000) / 2) {
                setZoom = 8f + 0.8f;
            } else {
                setZoom = 8f + 0.3f;
            }
        }
        return setZoom;
    }

    //region GPS를 위한 코드
    LocationCallback locationCallback = new LocationCallback() { //locationCallback 객체 생성
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1); //location = locationList.get(0);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude()) + " 경도:" + String.valueOf(location.getLongitude());

                //현재 위치에 마커 생성하고 이동
                //setCurrentLocation(location, markerTitle, markerSnippet); 우리 프로그램에선 마커생성 필요 X

                mCurrentLocatiion = location;
            }
        }
    }; //locationCallback객체 생성 끝

    // 로케이션을 업데이트하는 함수
    private void startLocationUpdates() {
        if (checkLocationServicesStatus() == false) {
            showDialogForLocationServiceSetting();
        } else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION); //Fine location은 gps랑 네트워크 이용. Coarse보다 더 정확
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);//네트워크만 이용.

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }
    }

    @Override
    protected void onStart() { //시작할때 퍼미션 검사하려고 override한 것, *참고 oncreate->onstart->onstop 순서임
        super.onStart();
        if (checkPermission()) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap != null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) { //현재위치를 String 주소로 반환
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            //매개변수 문제
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //location을 받아 현재 위치에 마커를 만드는 함수
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
        mMap.moveCamera(cameraUpdate);

    }

    public void setDefaultLocation() {
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        //String markerTitle = "위치정보 가져올 수 없음";
        //String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        //if (currentMarker != null) currentMarker.remove();

        //MarkerOptions markerOptions = new MarkerOptions();
        //markerOptions.position(DEFAULT_LOCATION);
        //markerOptions.title(markerTitle);
        //markerOptions.snippet(markerSnippet);
        //markerOptions.draggable(true);
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        //currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {  //두 퍼미션이 모두 부여되어있으면 true 아님 false
            return true;
        }
        return false;
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) { // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                } else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }

        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() { //checkLocationServicesStatus가 false이면 실행됨

        AlertDialog.Builder builder = new AlertDialog.Builder(DriverCallCheckActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }
    //endregion

    View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_accept:
                    //TODO: status가 1이되고, 승객의 수락을 기다리는 화면으로 넘어감
                    SharedPreferences pref = getSharedPreferences(Gloval.PREFERENCE, MODE_PRIVATE);
                    String id = pref.getString("id", "");
                    String glocLat = Double.toString(currentPosition.latitude);
                    String glocLong = Double.toString(currentPosition.longitude);

                    try {
                        String result = new AcceptTask().execute(cno, id, glocLat, glocLong).get();
                        if (result.equals("success")) {                                           // 성공적으로 콜을 잡은경우
                            Intent intent = new Intent(DriverCallCheckActivity.this, DriverWaitingActivity.class);
                            intent.putExtra("cno", Integer.parseInt(cno));
                            startActivity(intent);
                            finish();
                        } else if (result.equals("callExist")) {                                  // 기사에게 이미 콜이 있을경우
                            Toast.makeText(DriverCallCheckActivity.this, "이미 진행중인 콜이 있습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DriverCallCheckActivity.this, DriverCallListActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (result.equals("fail")) {                                       // 이미 기사가 정해진경우
                            //finish하고 toast메세지 띄움.
                            Toast.makeText(DriverCallCheckActivity.this, "종료된 콜입니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DriverCallCheckActivity.this, DriverCallListActivity.class);
                            startActivity(intent);
                            finish();
                        } else {                                                                   // 다른 값이 들어왔을 때.
                            Log.i("DriverCallCheckActivity", "AcceptTask에서 예상치 못한 값이 넘어옴.");
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

    //region AsyncTask (DB통신)
    class GetLocDataTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) { //id, slocString, slocLat, slocLong, sdestLat, sdestLong 으로 6개가 필요
            try {
                String str, str_url;
                str_url = "http://" + Gloval.ip + ":8080/highquick/getLocData.jsp";
                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                //URL연결, 출력스트림 초기화
                sendMsg = "cno=" + strings[0];
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

    class AcceptTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) { // pref에 저장된 id,
            try {
                String str, str_url;
                str_url = "http://" + Gloval.ip + ":8080/highquick/driverCallAccept.jsp";   //
                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                //URL연결, 출력스트림 초기화
                sendMsg = "cno=" + strings[0] + "&id=" + strings[1] + "&glocLat=" + strings[2] + "&glocLong=" + strings[3];
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
    //endregion

}


// ctrl+'/' 로 주석해제 가능
//package com.example.grad;
//
//import android.content.Intent;
//import android.location.Address;
//import android.location.Geocoder;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//
//import java.io.IOException;
//import java.util.List;
//
//public class DriverCallCheckActivity extends AppCompatActivity implements OnMapReadyCallback {
//    private Button btn_accept;
//    private String no, addr, time;
//    private GoogleMap mMap = null;
//    private Geocoder geocoder = null;
//
//    //TODO: 여기서 수락하면 CALL의 STATUS가 1이된다.
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_driver_call_check);
//
//        btn_accept = findViewById(R.id.btn_accept);
//        btn_accept.setOnClickListener(myOnClickListener);
//
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // selectActivity.xml에 있는 fragment의 id를 통해 mapFragment를 찾아 연결
//        mapFragment.getMapAsync(this); // getMapAsync가 호출되면 onMapReady 콜백이 실행됨.
//
//        Intent intent = getIntent();
//        no = intent.getExtras().getString("no");
//        addr = intent.getExtras().getString("addr");
//        time = intent.getExtras().getString("time");
//
//
//        Log.d("onCreate", "no : " + no);
//        Log.d("onCreate", "addr : " + addr);
//        Log.d("onCreate", "time : " + time);
//    }
//
//    View.OnClickListener myOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//
//           /* LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.layout_dialog, null);
//            setContentView(relativeLayout);*/
//
//
//  /*          LayoutInflater inflater = this.getLayoutInflater();
//            View dialogView = inflater.inflate(R.layout.alert_label_editor, null);
//            dialogBuilder.setView(dialogView);*/
//
//       /*     new MaterialDialog dialog = new MaterialDialog.Builder(this).customView(R.layout.layout_dialog_single_button, false).build();
//            View view = dialog.getCustomView();*/
//
//      /*      Intent intent = new Intent(DriverCallCheckActivity.this, DriverWaitingActivity.class);
//            startActivity(intent);
//            finish();*/
//        }
//    };
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        geocoder = new Geocoder(this);
//        createdMarker(addr);
//
//        mMap.setMyLocationEnabled(true);
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//    }
//
//    public void createdMarker(String addr) {
//
//        List<Address> addressList = null;
//
//        try {
//            addressList = geocoder.getFromLocationName(addr, 10); //주소, 최대검색결과개수
//
//            Log.i(this.getClass().getName(), addressList.get(0).toString());
//
//            // 콤마를 기준으로 split
//            String[] splitStr = addressList.get(0).toString().split(",");
//            String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소
//            System.out.println(address);
//
//            String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
//            String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
//            System.out.println(latitude);
//            System.out.println(longitude);
//
//            // 좌표(위도, 경도) 생성
//            LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
//            // 마커 생성
//            MarkerOptions mOptions = new MarkerOptions();
//            mOptions.title("search result");
//            mOptions.snippet(address);
//            mOptions.position(point);
//            // 마커 추가
//            mMap.addMarker(mOptions);
//
//            // 해당 좌표로 화면 줌
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
//
//            Log.d("createdMarker", "latitude : " + latitude);
//            Log.d("createdMarker", "longitude : " + longitude);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(DriverCallCheckActivity.this, "결과가 없습니다.", Toast.LENGTH_SHORT).show();
//        }
//    }
//}
