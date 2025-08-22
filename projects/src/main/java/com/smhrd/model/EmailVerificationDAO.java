package com.smhrd.model;

import java.sql.Timestamp;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.smhrd.db.SqlSessionManager;

/**
 * 이메일 인증 관련 데이터베이스 접근 객체
 * USER_EMAIL_AUTH 테이블에 대한 CRUD 작업을 담당합니다.
 */
public class EmailVerificationDAO {
    
    // SqlSessionFactory 가져오기
    SqlSessionFactory sqlSessionFactory = SqlSessionManager.getSqlSessionFactory();
    
    /**
     * 새로운 이메일 인증 토큰을 데이터베이스에 저장
     * @param verification 저장할 이메일 인증 정보
     * @return 성공 시 1, 실패 시 0
     */
    public int insertVerification(EmailVerification verification) {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        int result = 0;
        
        try {
            System.out.println("=== DEBUG: Attempting to insert EmailVerification ===");
            System.out.println("Email: " + verification.getEmail());
            System.out.println("Token: " + verification.getVerificationToken());
            System.out.println("ExpiresAt: " + verification.getExpiresAt());
            System.out.println("Verified: " + verification.getVerified());
            System.out.println("=====================================");
            
            result = sqlSession.insert("com.smhrd.db.EmailVerification.insert", verification);
            
            System.out.println("=== DEBUG: Insert successful! Result: " + result + " ===");
        } catch (Exception e) {
            System.err.println("=== DETAILED ERROR INFORMATION ===");
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            
            Throwable cause = e.getCause();
            while (cause != null) {
                System.err.println("Cause: " + cause.getClass().getName() + " - " + cause.getMessage());
                cause = cause.getCause();
            }
            
            String errorMsg = e.getMessage().toLowerCase();
            if (errorMsg.contains("table") && errorMsg.contains("not exist")) {
                System.err.println("DIAGNOSIS: USER_EMAIL_AUTH 테이블이 존재하지 않습니다.");
                System.err.println("해결방법: email_verification_setup.sql 파일을 Oracle에서 실행하세요.");
            } else if (errorMsg.contains("sequence") && errorMsg.contains("not exist")) {
                System.err.println("DIAGNOSIS: USER_EMAIL_AUTH_SEQ 시퀀스가 존재하지 않습니다.");
                System.err.println("해결방법: email_verification_setup.sql 파일을 Oracle에서 실행하세요.");
            } else if (errorMsg.contains("insufficient") || errorMsg.contains("privilege")) {
                System.err.println("DIAGNOSIS: 데이터베이스 권한이 부족합니다.");
                System.err.println("해결방법: 데이터베이스 관리자에게 권한 요청하세요.");
            }
            
            System.err.println("================================");
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        
        return result;
    }
    
    /**
     * 인증 토큰으로 이메일 인증 정보 조회
     * @param token 조회할 인증 토큰
     * @return 찾은 이메일 인증 정보, 없으면 null
     */
    public EmailVerification findByToken(String token) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        EmailVerification result = null;
        
        try {
            result = sqlSession.selectOne("com.smhrd.db.EmailVerification.findByToken", token);
        } catch (Exception e) {
            System.err.println("토큰으로 인증 정보 조회 실패: " + e.getMessage());
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        
        return result;
    }
    
    /**
     * 이메일 주소로 최신 인증 정보 조회
     * @param email 조회할 이메일 주소
     * @return 찾은 이메일 인증 정보, 없으면 null
     */
    public EmailVerification findByEmail(String email) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        EmailVerification result = null;
        
        try {
            result = sqlSession.selectOne("com.smhrd.db.EmailVerification.findByEmail", email);
        } catch (Exception e) {
            System.err.println("이메일로 인증 정보 조회 실패: " + e.getMessage());
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        
        return result;
    }
    
    /**
     * 이메일로 모든 인증 정보 조회 (히스토리 확인용)
     * @param email 조회할 이메일 주소
     * @return 해당 이메일의 모든 인증 정보 리스트
     */
    public List<EmailVerification> findAllByEmail(String email) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        List<EmailVerification> result = null;
        
        try {
            result = sqlSession.selectList("com.smhrd.db.EmailVerification.findAllByEmail", email);
        } catch (Exception e) {
            System.err.println("이메일로 모든 인증 정보 조회 실패: " + e.getMessage());
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        
        return result;
    }
    
    /**
     * 인증 상태를 완료로 업데이트
     * @param token 업데이트할 인증 토큰
     * @return 성공 시 1, 실패 시 0
     */
    public int updateVerificationStatus(String token) {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        int result = 0;
        
        try {
            result = sqlSession.update("com.smhrd.db.EmailVerification.updateVerified", token);
        } catch (Exception e) {
            System.err.println("인증 상태 업데이트 실패: " + e.getMessage());
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        
        return result;
    }
    
    /**
     * 특정 이메일의 모든 인증 토큰 삭제 (재발송 시 사용)
     * @param email 삭제할 이메일 주소
     * @return 삭제된 레코드 수
     */
    public int deleteByEmail(String email) {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        int result = 0;
        
        try {
            result = sqlSession.delete("com.smhrd.db.EmailVerification.deleteByEmail", email);
        } catch (Exception e) {
            System.err.println("이메일 인증 토큰 삭제 실패: " + e.getMessage());
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        
        return result;
    }
    
    /**
     * 만료된 인증 토큰들을 삭제 (정리 작업용)
     * @return 삭제된 레코드 수
     */
    public int deleteExpiredTokens() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        int result = 0;
        
        try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            result = sqlSession.delete("com.smhrd.db.EmailVerification.deleteExpired", now);
        } catch (Exception e) {
            System.err.println("만료된 토큰 삭제 실패: " + e.getMessage());
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        
        return result;
    }
    
    /**
     * 이메일의 유효한 인증 토큰이 있는지 확인
     * @param email 확인할 이메일 주소
     * @return 유효한 토큰이 있으면 true, 없으면 false
     */
    public boolean hasValidToken(String email) {
        EmailVerification verification = findByEmail(email);
        return verification != null && verification.isValidForVerification();
    }
    
    /**
     * 특정 이메일이 인증 완료되었는지 확인
     * @param email 확인할 이메일 주소
     * @return 인증 완료되었으면 true, 아니면 false
     */
    public boolean isEmailVerified(String email) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        Integer count = 0;
        
        try {
            count = sqlSession.selectOne("com.smhrd.db.EmailVerification.countVerified", email);
        } catch (Exception e) {
            System.err.println("이메일 인증 상태 확인 실패: " + e.getMessage());
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
        
        return count != null && count > 0;
    }
}