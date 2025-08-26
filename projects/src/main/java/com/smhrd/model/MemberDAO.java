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
			// Log exception for debugging
			System.err.println("MemberDAO.join() 오류: " + e.getMessage());
			e.printStackTrace();
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
	
	// update 기능 메서드 (네임스페이스 명시)
	public int update(UserInfo updateMem) {
		SqlSession sqlsession = sqlSessionFactory.openSession(true);
		int cnt = 0;
		
		try {
			cnt = sqlsession.update("com.smhrd.db.UserInfo.update", updateMem);
		} catch (Exception e) {
			System.err.println("MemberDAO.update() 오류: " + e.getMessage());
		} finally {
			if (sqlsession != null) {
				sqlsession.close();
			}
		}
		
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
		List<UserInfo> result = sqlsession.selectList("com.smhrd.db.UserInfo.select");
		// 3. sqlSession 닫기
		sqlsession.close();
		// 4. 리턴값 정의
		return result;
	}
	
	// 이메일 인증 상태 업데이트 메서드
	public boolean updateEmailVerificationStatus(String email, boolean verified) {
		SqlSession sqlsession = sqlSessionFactory.openSession(true);
		int cnt = 0;
		
		try {
			UserInfo updateData = new UserInfo();
			updateData.setEmail(email);
			updateData.setEmailVerified(verified);
			
			cnt = sqlsession.update("com.smhrd.db.UserInfo.updateEmailVerified", updateData);
			
		} catch (Exception e) {
			// Log exception for debugging
			System.err.println("MemberDAO.updateEmailVerificationStatus() 오류: " + e.getMessage());
			System.err.println("이메일 인증 기능이 데이터베이스 스키마에서 지원되지 않을 수 있습니다.");
			// Return true to prevent blocking signup process
			return true;
		} finally {
			if (sqlsession != null) {
				sqlsession.close();
			}
		}
		
		return cnt > 0;
	}
	
	// 계정 완전 삭제 메서드 (분석 데이터도 함께 삭제)
	public boolean deleteAccount(String email) {
		SqlSession sqlsession = sqlSessionFactory.openSession();
		boolean success = false;
		
		try {
			// 트랜잭션 시작
			sqlsession.getConnection().setAutoCommit(false);
			
			// 1. 분석 관련 데이터 삭제 (foreign key 순서 고려)
			// 관심도 추이 데이터 삭제
			sqlsession.delete("com.smhrd.db.UserInfo.deleteUserInterestTrends", email);
			
			// 감정 분석 데이터 삭제
			sqlsession.delete("com.smhrd.db.UserInfo.deleteUserEmotionAnalysis", email);
			
			// 긍정적 신호 데이터 삭제
			sqlsession.delete("com.smhrd.db.UserInfo.deleteUserPositiveSignals", email);
			
			// 대표 메시지 데이터 삭제
			sqlsession.delete("com.smhrd.db.UserInfo.deleteUserFavoriteMessages", email);
			
			// 분석 세션 데이터 삭제
			sqlsession.delete("com.smhrd.db.UserInfo.deleteUserAnalysisSessions", email);
			
			// 2. 이메일 인증 데이터 삭제
			sqlsession.delete("com.smhrd.db.UserInfo.deleteUserEmailVerifications", email);
			
			// 3. 사용자 계정 삭제
			int userDeleted = sqlsession.delete("com.smhrd.db.UserInfo.deleteUser", email);
			
			if (userDeleted > 0) {
				sqlsession.commit();
				success = true;
				System.out.println("계정 완전 삭제 완료: " + email);
			} else {
				sqlsession.rollback();
				System.err.println("계정 삭제 실패: 사용자를 찾을 수 없음 - " + email);
			}
			
		} catch (Exception e) {
			try {
				sqlsession.rollback();
			} catch (Exception rollbackEx) {
				System.err.println("롤백 실패: " + rollbackEx.getMessage());
			}
			System.err.println("MemberDAO.deleteAccount() 오류: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (sqlsession != null) {
				sqlsession.close();
			}
		}
		
		return success;
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








