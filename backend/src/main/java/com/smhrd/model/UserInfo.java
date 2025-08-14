package com.smhrd.model;

import lombok.Data;

@Data // getter, setter, toString
		// 어노테이션 : 코드 사이에 특별한 의미, 기능을 수행하도록 하는 기술!
		// 프로그램에 관한 데이터를 제공하고 코드에 정보를 추가하는 방법!
		// --> 코드가 깔끔해지고 재사용이 가능하다!
public class UserInfo {
	// 1. 필드(데이터)
	// *특정한 값(데이터)을 넣지 않고 해당 데이터가 들어갈 공간만 만든다.
	// email, pw, tel, address

	private String email;
	private String pw;
	private String nickname;
	
	// email, pw, tel, address를 하나로 묶어줄 수 있는
	// 내가 만든 자료형(객체)
	// 연동할 DB의 컬럼명과 동일하게 필드값 설정!

	// 2. 메서드(행위, 행동)
	// getter, setter, 생성자 (4개의 필드 초기화 시키는 생성자)!
	
	public UserInfo(String email, String pw, String nickname) {
		this.email = email;
		this.pw = pw;
		this.nickname = nickname;
	}

	public UserInfo(String email, String pw) {
		this.email = email;
		this.pw = pw;
	}
	
	// 로그인 요청 데이터
	
	// 로그인 응답 데이터
	

}
