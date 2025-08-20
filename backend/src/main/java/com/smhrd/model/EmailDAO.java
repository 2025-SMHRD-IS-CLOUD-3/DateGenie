package com.smhrd.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.smhrd.db.SqlSessionManager;

public class EmailDAO {

    SqlSessionFactory sqlSessionFactory = SqlSessionManager.getSqlSessionFactory();
    
    // 이메일 인증 토큰 저장
    public int saveVerificationToken(String email, String token, Timestamp expiresAt) {
        SqlSession sqlsession = sqlSessionFactory.openSession(true);
        
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        params.put("token", token);
        params.put("expiresAt", expiresAt);
        
        int cnt = sqlsession.insert("saveVerificationToken", params);
        sqlsession.close();
        return cnt;
    }
    
    // 이메일 인증 완료 여부 확인
    public boolean isEmailVerified(String email) {
        SqlSession sqlsession = sqlSessionFactory.openSession(true);
        
        Integer count = sqlsession.selectOne("isEmailVerified", email);
        sqlsession.close();
        
        return count != null && count > 0;
    }
    
    // 토큰으로 이메일 인증 처리
    public boolean verifyEmailToken(String token) {
        SqlSession sqlsession = sqlSessionFactory.openSession(true);
        
        int cnt = sqlsession.update("verifyEmailToken", token);
        sqlsession.close();
        
        return cnt > 0;
    }
    
    // 만료된 토큰들 삭제 (정리용)
    public int deleteExpiredTokens() {
        SqlSession sqlsession = sqlSessionFactory.openSession(true);
        
        int cnt = sqlsession.delete("deleteExpiredTokens");
        sqlsession.close();
        
        return cnt;
    }
    
    // 특정 이메일의 인증 토큰 유효성 확인 (토큰 존재 + 만료 안됨)
    public boolean isTokenValid(String token) {
        SqlSession sqlsession = sqlSessionFactory.openSession(true);
        
     // 디버깅 추가
        System.out.println("=== isTokenValid 디버깅 ===");
        System.out.println("확인할 토큰: " + token);
        
        Integer count = sqlsession.selectOne("isTokenValid", token);
        System.out.println("DB 조회 결과: " + count);
        
        sqlsession.close();
        
        boolean result = count != null && count > 0;
        System.out.println("최종 유효성 결과: " + result);
        
        return count != null && count > 0;
    }
        
        // 토큰으로 이메일 주소 가져오기
        public String getEmailByToken(String token) {
            SqlSession sqlsession = sqlSessionFactory.openSession();
            String email = sqlsession.selectOne("getEmailByToken", token);
            sqlsession.close();
            return email;   
        
    }
}
	
	
