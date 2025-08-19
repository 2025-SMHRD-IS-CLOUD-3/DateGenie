package com.smhrd.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.smhrd.db.SqlSessionManager;

public class MemberDAO {
	
	// DB에 접근할 수 있도록 해주는 기능이 있는 클래스!
	// --> 한 개의 기능 = 메서드 형태로 작성!
	// SqlSessionManager 클래스에서 만든 메서드
	// getSqlSessionFactory를 통해서 sqlSessionFactory 가져온다.
	SqlSessionFactory sqlSessionFactory = SqlSessionManager.getSqlSessionFactory();
	// 2. 기능 단위로 메서드 작성
	
	// 2.1 회원가입 메서드 (join)
	public int join(UserInfo member) {
	// JoinService에서 보낸 데이터 MavenMember를 받아오기
		// openSession(true) --> 자동 auto commit
		// ex) join을 통해서 DB에 데이터 저장하는 경우 수동으로 commit(x), 자동으로 commit됨
		// 2.1.1 factory를 통해서 sqlsession 생성
		SqlSession sqlsession = sqlSessionFactory.openSession(true);
		// 2.1.2 sqlsession을 이용해서 db에서 기능 작업
		// sqlsession.insert(statement, parameter);
		//					(mapper내의 쿼리문의 id, 쿼리문 실행시에 필요한 데이터)
		// insert 메서드의 리턴값 : insert 구문 실행시에 영향 받는 행의 개수!
		int cnt = sqlsession.insert("join", member);
		// 2.1.3 리턴값(결과값)으로 판단하기 위한 코드: servlet(JoinService)
		// 		 결과값 리턴!!
		System.out.println("결과 값 출력 : " + cnt);
		//2.1.4 db 사용이 끝났기에 sqlSession 닫기
		sqlsession.close();
		return cnt;
	}
	// login 기능 메서드
	public UserInfo login(UserInfo loginMember) {
		// 1. sqlSession 생성
		SqlSession sqlsession = sqlSessionFactory.openSession(true);
		// 2. sqlSession을 이용해서 메서드 실행
		// 쿼리문 : select * from maven_member where id=? pw=?
		//	-> 상단의 쿼리문 실행시에 매칭되는 데이터가 있다면 한 행의 데이터 출력(email,pw,tel, adress)
		//	*리턴값을 설정해주기 위해서 자료형을 4개의 데이터를 하나로 묶을 수 있는 
		//		Maven_member로 설정한다.
		UserInfo sMember = sqlsession.selectOne("login", loginMember);
		// 3. 리턴값 판별
		// 4. sqlSession 닫기
		sqlsession.close();
		return sMember;
	}
	// update 기능 메서드
	public int update(UserInfo updateMem) {
		SqlSession sqlsession = sqlSessionFactory.openSession(true);
		int cnt = sqlsession.update("update", updateMem);
		
		sqlsession.close();
		return cnt;
	}
	// 전체회원정보 조회 기능
	public List<UserInfo> select() {
		// 1. sqlSession 생성
		SqlSession sqlsession = sqlSessionFactory.openSession(true);
		// 2. sqlSession을 통해서 메서드를 통해 DB접근
		//	*주의!! 어떤 메서드 사용할지, 결과값이 어떻게 나올지!!
		//	쿼리문 : select * form MAVEN_MEMBER
		//	--> 쿼리문 실행시에 들어갈 데이터가 없다
		//	--> select 메서드 실행시에 매개변수 불필요!
		
		// * DB에서 한 행(email,pw,address,tel)만 리턴값으로 받을때
		//	-> MavenMember자료형으로 표현
		//	DB의 전체 데이터를 리턴값으로 받을때,
		//	-> MavenMember자료형의 데이터를 여러개 가져올 수 있도록!
		//	-> list를 활용하겠다!
		List<UserInfo> result = sqlsession.selectList("select");
		// 3. sqlSession 닫기
		sqlsession.close();
		// 4. 리턴값 정의
		return result;
	}
	
}

// 메서드 선언
// public void join(int age, String name){
// }
// 메서드 호출
// join(30, "임명진")

// parameter: 매개변수
//		--> 메서드 선언(생성)할 때, 소괄호 안에 들어가는 변수
// argument: 전달인자
//		--> 메서드 호출(사용)할때, 소괄호 안에 들어가는 데이터








