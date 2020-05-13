package com.example.grad;

//이 클래스는 DriverCallList클래스에서 사용할 ListViewAdapter를 위한 클래스입니다.
public class CallListItem {
    public String no;
    public String addr;
    public String time;

    public CallListItem(String _no, String _addr, String _time){
        no = _no;
        addr = _addr;
        time = _time;
    }
}
