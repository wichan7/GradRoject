package com.example.grad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class PassengerCallActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap = null;
    private Geocoder geocoder = null;
    private Button btn_search = null, btn_call = null, btn_menu = null;
    private CheckBox cb_isQuick = null;
    private Marker destMarker = null; //목적지마커는 항상 한개로 유지되어야하므로 destMarker로 관리
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private BackPressCloseHandler backPressCloseHandler;

    //EditText 자동완성을 위한 변수
    private ArrayList<String> array_result;
    private ArrayAdapter<String> autoAdapter;
    private AutoCompleteTextView et_search = null;

    //메뉴버튼을 위한 멤버변수
    DrawerLayout drawerLayout;
    ConstraintLayout sideView;
    private TextView tv_welcome;
    private Button btn_logout, btn_exit, btn_notice;
    //region GPS를 위한 변수선언부

    private Marker currentMarker = null;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    private static final int PERMISSIONS_REQUEST_CODE = 100; // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private boolean needRequest = false;
    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소
    Location mCurrentLocatiion;
    LatLng currentPosition;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.

    //endregion

    //onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_call);
        pref = getSharedPreferences(Gloval.PREFERENCE, MODE_PRIVATE);
        editor = pref.edit();

        backPressCloseHandler = new BackPressCloseHandler(this);
        tv_welcome = findViewById(R.id.tv_welcome);
        String id = pref.getString("id","");
        tv_welcome.setText(id+"님\n반갑습니다.");
        drawerLayout = findViewById(R.id.dl_main);
        sideView = findViewById(R.id.sideView);
        btn_menu = findViewById(R.id.btn_menu);
        btn_menu.setOnClickListener(myOnClickListener);
        btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(myOnClickListener);
        btn_exit = findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(myOnClickListener);
        btn_notice = findViewById(R.id.btn_notice);
        btn_notice.setOnClickListener(myOnClickListener);
        cb_isQuick = findViewById(R.id.cb_isQuick);
        btn_search = findViewById(R.id.btn_search); //onClickListener는 onMapReady에서 달아줌
        btn_call = findViewById(R.id.btn_call);
        btn_call.setOnClickListener(myOnClickListener);
        et_search = (AutoCompleteTextView)findViewById(R.id.et_search);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // selectActivity.xml에 있는 fragment의 id를 통해 mapFragment를 찾아 연결
        mapFragment.getMapAsync(this); // getMapAsync가 호출되면 onMapReady 콜백이 실행됨.

        //자동완성을 위한 코드
        array_result = Gloval.getStringArrayPref(this,"array_result");
        autoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line , array_result);
        et_search.setAdapter(autoAdapter);
        //et_search 자동완성 끝

        //region GPS를 위한 코드
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLayout = findViewById(R.id.layout_passengerCall);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //endregion
    } //onCreate 끝

    @Override
    public void onMapReady(final GoogleMap googleMap) { //googlemap이 작동완료했을때 실행되는 콜백함수
        mMap = googleMap;
        geocoder = new Geocoder(this);

        mMap.setOnMapClickListener(myOnMapClickListener);
        btn_search.setOnClickListener(myOnClickListener);

        //region GPS를 위한 코드

        setDefaultLocation(); //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에 지도의 초기위치를 서울로 설정

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED ) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            startLocationUpdates(); // 3. 위치 업데이트 시작
        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( PassengerCallActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //endregion
    } //OnMapReady 끝

    //region GPS를 위한 코드

    private boolean isCameraUpdated = false;
    LocationCallback locationCallback = new LocationCallback() { //locationCallback 객체 생성
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1); //location = locationList.get(0);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                //2020-08-03 주석처리. 마커 안만들어서 필요X
                //String markerTitle = getCurrentAddress(currentPosition);
                //String markerSnippet = "위도:" + String.valueOf(location.getLatitude()) + " 경도:" + String.valueOf(location.getLongitude());

                //현재 위치에 마커 생성하고 이동
                //setCurrentLocation(location, markerTitle, markerSnippet); 우리 프로그램에선 마커생성 필요 X

                mCurrentLocatiion = location;

                // 2020 05 10 추가. 처음에만 화면을 현재위치로 이동시키기위해 작성
                if (!isCameraUpdated){
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPosition,15);
                    mMap.moveCamera(cameraUpdate);
                    isCameraUpdated = true;
                }
                // 2020 05 10 end
            }
        }
    }; //locationCallback객체 생성 끝

    // 로케이션을 업데이트하는 함수
    private void startLocationUpdates() {
        if (checkLocationServicesStatus() == false) {
            showDialogForLocationServiceSetting();
        }else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION); //Fine location은 gps랑 네트워크 이용. Coarse보다 더 정확
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);//네트워크만 이용.

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED ) {
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

            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
        Gloval.setStringArrayPref(this,"array_result", array_result);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(sideView)) {
            drawerLayout.closeDrawer(sideView);
        } else {
            backPressCloseHandler.onBackPressed();
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
            //2020-08-03 주석처리. 현재주소를 알필요없음
            //Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "---";
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

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng,15);
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

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) { // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            }
            else {
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
                }else {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(PassengerCallActivity.this);
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

    //region myOnClickListener, myOnMapClickListener 코드
    View.OnClickListener myOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_menu: //메뉴버튼 누를때
                    drawerLayout.openDrawer(GravityCompat.START);
                    break;

                case R.id.btn_search:
                    if (destMarker != null) destMarker.remove();

                    String str = et_search.getText().toString();
                    List<Address> addressList = null;

                    try {
                        addressList = geocoder.getFromLocationName(str,1); //주소, 최대검색결과개수
                        Toast.makeText(PassengerCallActivity.this,addressList.toString(),Toast.LENGTH_LONG);
                        if(addressList.size() == 0){
                            Toast.makeText(PassengerCallActivity.this,"검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
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
                        System.out.println("lat:"+latitude+" long:"+longitude);
                        LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        // 마커 추가
                        setDestMarker(point);
                        // 해당 좌표로 화면 줌
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point,mMap.getCameraPosition().zoom));

                        if(array_result.size()==0){
                            autoAdapter.add(str);
                            array_result.add(str);
                        }else{
                            for(int i=0; i<array_result.size(); i++){
                                if(array_result.get(i).equals(str))
                                    break;
                                if(i == array_result.size()-1) {
                                    autoAdapter.add(str);
                                    array_result.add(str);
                                }
                            }
                        }

                        /* 주소가 기록되는 기능
                        StringTokenizer st = new StringTokenizer(address, " ");
                        String tstr = "";
                        st.nextToken();
                        st.nextToken(); // 국가, 시까지 제거
                        for(;;){
                            if (st.hasMoreTokens()){
                                tstr += st.nextToken() + " ";
                            }
                            else break;
                        }
                        tstr = tstr.substring(0,tstr.length()-1); //마지막에 공백 생기므로 공백 제거
                        address = tstr;
                        if(array_result.size()==0){
                            autoAdapter.add(address);
                            array_result.add(address);
                        }else{
                            for(int i=0; i<array_result.size(); i++){
                                Log.i("zz",array_result.get(i));
                                if(array_result.get(i).equals(address))
                                    break;
                                if(i == array_result.size()-1) {
                                    autoAdapter.add(address);
                                    array_result.add(address);
                                }
                            }
                        }
                        //array_result에 저장.
                        */
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(PassengerCallActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.btn_call:
                    if(destMarker==null){ //목적지마커가 없으면
                        Toast.makeText(PassengerCallActivity.this, "목적지를 설정해주세요!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String id = pref.getString("id","");
                    String slocString = getCurrentAddress(currentPosition);
                    String slocLat = Double.toString(currentPosition.latitude);
                    String slocLong = Double.toString(currentPosition.longitude);
                    String sdestLat = Double.toString(destMarker.getPosition().latitude);
                    String sdestLong = Double.toString(destMarker.getPosition().longitude);
                    String isQuick = Boolean.toString(cb_isQuick.isChecked());

                    try {
                        String result = new CustomTask().execute(id,slocString,slocLat,slocLong,sdestLat,sdestLong,isQuick).get();
                        StringTokenizer st = new StringTokenizer(result, "&");
                        result = st.nextToken();
                        Log.i("PassengerCallActivity",result);
                        if(result.equals("success")){
                            int cno = Integer.parseInt(st.nextToken());
                            Log.i("PassengerCallActivity","받아온 cno:"+cno);
                            Intent intent = new Intent(PassengerCallActivity.this, PassengerWaitingActivity.class);
                            intent.putExtra("cno", cno);
                            startActivity(intent);
                            finish();
                        }
                        else if(result.equals("callExist")){
                            Toast.makeText(PassengerCallActivity.this, "콜이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(PassengerCallActivity.this, "오류", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;

                case R.id.btn_exit:
                    finishAffinity();
                    System.exit(0);
                    break;

                case R.id.btn_logout:
                    editor.putInt("autoLoginValue", Gloval.NEGATIVE);
                    editor.commit();
                    Intent intent = new Intent(PassengerCallActivity.this,FirstInstallActivity.class);
                    finish();
                    break;

                case R.id.btn_notice:
                    Intent intent2 = new Intent(PassengerCallActivity.this,NoticeActivity.class);
                    startActivity(intent2);
                    overridePendingTransition(0, 0); //애니메이션 없에주는 코드
                    finish();
                    break;
            }
        }
    }; //myOnClickListener 구현


    GoogleMap.OnMapClickListener myOnMapClickListener = new GoogleMap.OnMapClickListener(){
        @Override
        public void onMapClick(LatLng point) {
            setDestMarker(point);
        }
    }; //myOnMapClickListener 구현
    //endregion

    //region AsyncTask (DB통신)
    class CustomTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;

        @Override
        protected String doInBackground(String... strings) { //id, slocString, slocLat, slocLong, sdestLat, sdestLong 으로 6개가 필요
            try {
                String str, str_url;
                str_url = "http://"+ Gloval.ip +":8080/highquick/makeCall.jsp";
                URL url = new URL(str_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                //URL연결, 출력스트림 초기화
                sendMsg = "id=" + strings[0] + "&slocString=" + strings[1] + "&slocLat=" + strings[2] + "&slocLong=" + strings[3] + "&sdestLat=" + strings[4]
                        + "&sdestLong=" + strings[5] + "&isQuick=" + strings[6];
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

    public void setDestMarker(LatLng latlng){
        MarkerOptions mOptions = new MarkerOptions();
        // 마커 타이틀
        //mOptions.title("목적지");
        // LatLng: 위도 경도 쌍을 나타냄
        mOptions.position(latlng);
        /* 목적지 주소를 얻는 코드. 필요없어서 주석처리
        //목적지 주소 검색
        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 콤마를 기준으로 split
        String []splitStr = addressList.get(0).toString().split(",");
        String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
        */
        // 마커 간단한 정보 넣기
        //mOptions.snippet(address);

        Bitmap destImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.destmarker), 170, 170, false);
        mOptions.icon(BitmapDescriptorFactory.fromBitmap(destImage));
        // 마커(핀) 추가
        if (destMarker == null) //아직 목적지마커가 없는경우
            destMarker = mMap.addMarker(mOptions);
        else { //목적지 마커가 있는경우
            destMarker.remove(); //원래있던 마커 삭제
            destMarker = mMap.addMarker(mOptions);
        }
        destMarker.setDraggable(true);
    }

}