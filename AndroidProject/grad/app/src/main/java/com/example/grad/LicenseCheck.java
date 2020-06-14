package com.example.grad;

import java.io.*;
import java.net.*;

/*
 * LicenseCode를 검사하는 클래스입니다.
 * 라이센스를 검사하고 html을 가져오는 코드는 작성된 상태입니다.
 * 가져온 html에서 결과를 파싱하여 LicenseCheck.SUCCESS 또는 LicenseCheck.FAIL을 리턴하는 코드를 작성해주세요!
 */
public class LicenseCheck {
	
	public static final int SUCCESS = 1;
	public static final int FAIL = 0;
	private String res = null;
	
	public LicenseCheck() {
	}
	
	public int check(String year, String month, String date, String name, String licenNo0, String licenNo1, String licenNo2, String licenNo3) {

		try {
			URL aURL = new URL("https://www.efine.go.kr/licen/truth/licenTruth.do?subMenuLv=010100");	//면허 인증 사이트.
			URLConnection uc = aURL.openConnection();
			uc.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(uc.getOutputStream());
			out.write("checkPage=2&flag=searchPage" 							//checkPage랑 flag는 뭔지 잘 모르겠음.
					+"&regYear="+year+"&regMonth="+month+"&regDate="+date		//년, 월, 일.
					+"&name="+name												//이름
					+"&licenNo0="+licenNo0+"&licenNo1="+licenNo1				//면허번호 2자리,2자리
					+"&licenNo2="+licenNo2+"&licenNo3="+licenNo3);				//면허번호 6자리,2자리
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			
			String inputLine;
			int i = 1;
			while ((inputLine = in.readLine()) != null) {
				if(i == 2119) {
					String k =inputLine.trim().substring(22);
					res = k.substring(0,k.indexOf("<"));
					break;
				}
				i++;
			}
			
			if (res.equals("전산 자료와 일치 합니다.")) {
				return LicenseCheck.SUCCESS;
			}
			
			/////요구:읽어온 html코드에 결과 메시지부분을 파싱하여 결과 메시지 부분이 "전산 자료와 일치 합니다."일 경우 LicenseCheck.SUCCESS리턴.
			/////아닐경우 LicenseCheck.FAIL을 리턴하는 코드를 작성해주세요!!
			} catch(Exception e) {
				e.printStackTrace();
			}
		
		return LicenseCheck.FAIL;
	}
	
}
