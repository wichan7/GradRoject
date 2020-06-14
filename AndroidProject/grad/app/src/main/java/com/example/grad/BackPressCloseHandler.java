package com.example.grad;

import android.app.Activity;
import android.widget.Toast;

public class BackPressCloseHandler {
    //멤버변수
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;

    //생성자
    public BackPressCloseHandler(Activity context){
        this.activity = context;
    }

    //메서드
    public void onBackPressed(){
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000){
            activity.finish();
            toast.cancel();
        }
    }

    public void showGuide(){
        toast = Toast.makeText(activity, "뒤로가기 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
