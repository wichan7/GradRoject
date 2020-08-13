package com.example.grad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NoticeActivity extends AppCompatActivity {
    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; //웹뷰세팅

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        // 웹뷰 시작
        mWebView = (WebView) findViewById(R.id.wv_notice);

        mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        mWebSettings = mWebView.getSettings(); //세부 세팅 등록
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
        mWebSettings.setSupportMultipleWindows(true); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(true); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(true); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        mWebView.loadUrl("https://cafe.naver.com/highquick?iframe_url=/ArticleList.nhn%3Fsearch.clubid=30191046%26search.menuid=2%26search.boardtype=L"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
    }


    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()){
            mWebView.goBack();
        }else{
            SharedPreferences pref = getSharedPreferences(Gloval.PREFERENCE, MODE_PRIVATE);
            int where = pref.getInt("autoLoginValue",Gloval.NEGATIVE);
            Intent intent = null;

            if (where == Gloval.PASSENGER){
                intent = new Intent(this,PassengerCallActivity.class);
            }else if (where == Gloval.DRIVER){
                intent = new Intent(this, DriverCallListActivity.class);
            }else{
                super.onBackPressed();
            }
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0); //애니메이션 없에주는 코드
        }
    } //onBackPressed End


}
