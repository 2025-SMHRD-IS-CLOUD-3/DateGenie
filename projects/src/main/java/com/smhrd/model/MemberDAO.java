package com.smhrd.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.smhrd.db.SqlSessionManager;
import com.smhrd.util.SecurityUtils;

public class MemberDAO {
	
	// DB에 접근할 수 있도록 해주는 기능이 있는 클래스!
	// --> 한 개의 기능 = 메서드 형태로 작성!
	// SqlSessionManager 클래스에서 만든 메서드
	// getSqlSessionFactory를 통해서 sqlSessionFactory 가져온다.
	SqlSessionFactory sqlSessionFactory = SqlSessionManager.getSqlSessionFactory();
	// 2. 기능 단위로 메서드 작성
	
	// 회원가입 메서드
	public int join(UserInfo member) {
		SqlSession sqlsession = sqlSessionFactory.openSession(true);
		int cnt = 0;
		
		try {
			// Hash password without salt
			String hashedPassword = SecurityUtils.hashPasswordSimple(member.getPw());
			
			// Update member object with hashed password
			member.setPw(hashedPassword);
			
			cnt = sqlsession.insert("com.smhrd.db.UserInfo.join", member);
			
		} catch (Exception e) {
			// Silent exception handling
		} finally {
			if (sqlsession != null) {
				sqlsession.close();
			}
		}
		
		return cnt;
	}
	// 로그인 메서드
	public UserInfo login(UserInfo loginMember) {
		SqlSession sqlsession = sqlSessionFactory.openSession();
		UserInfo result = null;
		
		try {
			// Get user data by email only
			UserInfo storedUser = sqlsession.selectOne("com.smhrd.db.UserInfo.login", loginMember);
			
			if (storedUser != null) {
				// Verify password using stored hash (without salt)
				String inputPassword = loginMember.getPw();
				String storedHash = storedUser.getPw();
				
				if (SecurityUtils.verifyPasswordSimple(inputPassword, storedHash)) {
					result = storedUser;
				}
			}
			
		} catch (Exception e) {
			// Silent exception handling
		} finally {
			if (sqlsession != null) {
				sqlsession.close();
			}
		}
		
		return result;
	}
	
	// 이메일 중복 체크 메서드
	public UserInfo checkEmailExists(String email) {
		SqlSession sqlsession = sqlSessionFactory.openSession();
		UserInfo result = null;
		
		try {
			result = sqlsession.selectOne("com.smhrd.db.UserInfo.checkEmail", email);
			
		} catch (Exception e) {
			// Silent exception handling
		} finally {
			if (sqlsession != null) {
				sqlsession.close();
			}
		}
		
		return result;
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








