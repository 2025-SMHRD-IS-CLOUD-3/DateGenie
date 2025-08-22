package com.smhrd.service;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import com.smhrd.model.EmailVerification;
import com.smhrd.model.EmailVerificationDAO;
import com.smhrd.util.EmailConfig;

/**
 * 이메일 인증 토큰 관리 서비스
 * 토큰 생성, 검증, 인증 처리 등의 비즈니스 로직을 담당합니다.
 */
public class EmailVerificationService {
    
    private final EmailVerificationDAO emailVerificationDAO;
    private final SecureRandom secureRandom;
    
    public EmailVerificationService() {
        this.emailVerificationDAO = new EmailVerificationDAO();
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * 이메일 인증 토큰 생성 및 발송
     * @param email 인증할 이메일 주소
     * @param baseUrl 웹사이트 기본 URL
     * @return 성공 여부 결과
     */
    public VerificationResult createAndSendVerification(String email, String baseUrl) {
        
        try {
            // 1. 이메일 유효성 검사
            if (!isValidEmail(email)) {
                return new VerificationResult(false, "유효하지 않은 이메일 주소입니다.", null);
            }
            
            // 2. 기존 미완료 토큰 확인
            if (emailVerificationDAO.hasValidToken(email)) {
                return new VerificationResult(false, "이미 인증 메일이 발송되었습니다. 이메일을 확인해주세요.", null);
            }
            
            // 3. 기존 토큰 정리 (같은 이메일의 이전 토큰들 삭제)
            emailVerificationDAO.deleteByEmail(email);
            
            // 4. 새로운 인증 토큰 생성
            String verificationToken = generateSecureToken();
            
            // 5. 만료 시간 설정
            long expiryMillis = System.currentTimeMillis() + 
                TimeUnit.HOURS.toMillis(EmailConfig.getVerificationExpiryHours());
            Timestamp expiresAt = new Timestamp(expiryMillis);
            
            // 6. 데이터베이스에 저장
            EmailVerification verification = new EmailVerification(email, verificationToken, expiresAt);
            int insertResult = emailVerificationDAO.insertVerification(verification);
            
            if (insertResult <= 0) {
                return new VerificationResult(false, "인증 토큰 저장에 실패했습니다.", null);
            }
            
            // 7. 6자리 인증 코드 생성
            String verificationCode = generateCodeFromToken(verificationToken);
            
            // 8. 이메일 발송 (토큰과 코드 포함)
            boolean emailSent = EmailService.sendVerificationEmail(email, verificationToken, verificationCode, baseUrl);
            
            if (!emailSent) {
                // 이메일 발송 실패 시 토큰 삭제
                emailVerificationDAO.deleteByEmail(email);
                return new VerificationResult(false, "인증 이메일 발송에 실패했습니다.", null);
            }
            
            return new VerificationResult(true, "인증 이메일이 발송되었습니다. 이메일을 확인해주세요.", verificationToken);
            
        } catch (Exception e) {
            System.err.println("이메일 인증 생성 실패: " + e.getMessage());
            e.printStackTrace();
            return new VerificationResult(false, "인증 처리 중 오류가 발생했습니다.", null);
        }
    }
    
    /**
     * 이메일 인증 토큰 검증 및 인증 완료 처리
     * @param token 인증 토큰
     * @return 인증 결과
     */
    public VerificationResult verifyEmail(String token) {
        
        try {
            // 1. 토큰 유효성 검사
            if (token == null || token.trim().isEmpty()) {
                return new VerificationResult(false, "유효하지 않은 인증 토큰입니다.", null);
            }
            
            // 2. 토큰으로 인증 정보 조회
            EmailVerification verification = emailVerificationDAO.findByToken(token.trim());
            
            if (verification == null) {
                return new VerificationResult(false, "존재하지 않는 인증 토큰입니다.", null);
            }
            
            // 3. 이미 인증 완료된 경우
            if (verification.checkVerified()) {
                return new VerificationResult(true, "이미 인증이 완료된 이메일입니다.", verification.getEmail());
            }
            
            // 4. 토큰 만료 확인
            if (verification.isExpired()) {
                return new VerificationResult(false, "인증 토큰이 만료되었습니다. 새로운 인증 메일을 요청해주세요.", null);
            }
            
            // 5. 인증 완료 처리
            int updateResult = emailVerificationDAO.updateVerificationStatus(token.trim());
            
            if (updateResult <= 0) {
                return new VerificationResult(false, "인증 처리에 실패했습니다.", null);
            }
            
            return new VerificationResult(true, "이메일 인증이 완료되었습니다.", verification.getEmail());
            
        } catch (Exception e) {
            System.err.println("이메일 인증 검증 실패: " + e.getMessage());
            e.printStackTrace();
            return new VerificationResult(false, "인증 처리 중 오류가 발생했습니다.", null);
        }
    }
    
    /**
     * 이메일 인증 상태 확인
     * @param email 확인할 이메일 주소
     * @return 인증 완료 여부
     */
    public boolean isEmailVerified(String email) {
        try {
            return emailVerificationDAO.isEmailVerified(email);
        } catch (Exception e) {
            System.err.println("이메일 인증 상태 확인 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 인증 메일 재발송
     * @param email 재발송할 이메일 주소
     * @param baseUrl 웹사이트 기본 URL
     * @return 재발송 결과
     */
    public VerificationResult resendVerification(String email, String baseUrl) {
        
        try {
            // 1. 이미 인증 완료된 경우
            if (isEmailVerified(email)) {
                return new VerificationResult(false, "이미 인증이 완료된 이메일입니다.", email);
            }
            
            // 2. 기존 토큰들 삭제하고 새로 생성
            return createAndSendVerification(email, baseUrl);
            
        } catch (Exception e) {
            System.err.println("인증 메일 재발송 실패: " + e.getMessage());
            return new VerificationResult(false, "재발송 처리 중 오류가 발생했습니다.", null);
        }
    }

    /**
     * 6자리 인증 코드로 이메일 인증 처리 (인라인 UI용)
     * @param email 인증할 이메일 주소
     * @param verificationCode 6자리 인증 코드
     * @return 인증 결과
     */
    public VerificationResult verifyEmailCode(String email, String verificationCode) {
        
        try {
            // 1. 입력값 유효성 검사
            if (email == null || email.trim().isEmpty()) {
                return new VerificationResult(false, "이메일을 입력해주세요.", null);
            }
            
            if (verificationCode == null || !verificationCode.matches("^[0-9]{6}$")) {
                return new VerificationResult(false, "올바른 6자리 인증 코드를 입력해주세요.", null);
            }
            
            // 2. 이메일로 인증 정보 조회
            EmailVerification verification = emailVerificationDAO.findByEmail(email.trim());
            
            if (verification == null) {
                return new VerificationResult(false, "인증 요청이 존재하지 않습니다. 인증 메일을 다시 요청해주세요.", null);
            }
            
            // 3. 이미 인증 완료된 경우
            if (verification.checkVerified()) {
                return new VerificationResult(true, "이미 인증이 완료된 이메일입니다.", email);
            }
            
            // 4. 토큰 만료 확인
            if (verification.isExpired()) {
                return new VerificationResult(false, "인증 코드가 만료되었습니다. 새로운 인증 메일을 요청해주세요.", null);
            }
            
            // 5. 토큰에서 6자리 코드 추출 (토큰의 마지막 6자리 숫자)
            String expectedCode = generateCodeFromToken(verification.getVerificationToken());
            
            if (!verificationCode.equals(expectedCode)) {
                return new VerificationResult(false, "잘못된 인증 코드입니다. 다시 확인해주세요.", null);
            }
            
            // 6. 인증 완료 처리
            int updateResult = emailVerificationDAO.updateVerificationStatus(verification.getVerificationToken());
            
            if (updateResult <= 0) {
                return new VerificationResult(false, "인증 처리에 실패했습니다.", null);
            }
            
            return new VerificationResult(true, "이메일 인증이 완료되었습니다.", email);
            
        } catch (Exception e) {
            System.err.println("이메일 코드 인증 실패: " + e.getMessage());
            e.printStackTrace();
            return new VerificationResult(false, "인증 처리 중 오류가 발생했습니다.", null);
        }
    }

    /**
     * 인증 메일 재발송 (인라인 UI용)
     * @param email 재발송할 이메일 주소
     * @param baseUrl 웹사이트 기본 URL
     * @return 재발송 결과
     */
    public VerificationResult resendVerificationEmail(String email, String baseUrl) {
        return resendVerification(email, baseUrl);
    }
    
    /**
     * 만료된 토큰들 정리 (스케줄러에서 사용)
     * @return 정리된 토큰 수
     */
    public int cleanupExpiredTokens() {
        try {
            int deletedCount = emailVerificationDAO.deleteExpiredTokens();
            System.out.println("만료된 인증 토큰 " + deletedCount + "개 정리 완료");
            return deletedCount;
        } catch (Exception e) {
            System.err.println("만료된 토큰 정리 실패: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 보안성 높은 랜덤 토큰 생성
     * @return Base64 인코딩된 32바이트 토큰
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * 토큰에서 일관된 6자리 숫자 코드 생성
     * @param token 원본 토큰
     * @return 6자리 숫자 코드
     */
    private String generateCodeFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return "000000";
        }
        
        try {
            // 토큰의 해시값을 사용하여 일관된 6자리 코드 생성
            int hashCode = Math.abs(token.hashCode());
            // 6자리 숫자로 변환 (100000 ~ 999999)
            int code = 100000 + (hashCode % 900000);
            return String.valueOf(code);
        } catch (Exception e) {
            // 에러 발생 시 기본값 반환
            return "123456";
        }
    }
    
    /**
     * 이메일 주소 유효성 검사
     * @param email 검사할 이메일 주소
     * @return 유효성 여부
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // 기본적인 이메일 형식 검사
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * 인증 결과를 담는 내부 클래스
     */
    public static class VerificationResult {
        private final boolean success;
        private final String message;
        private final String email;
        
        public VerificationResult(boolean success, String message, String email) {
            this.success = success;
            this.message = message;
            this.email = email;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getEmail() {
            return email;
        }
        
        @Override
        public String toString() {
            return "VerificationResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }
    
    /**
     * 이메일 인증 시스템 상태 확인 (헬스체크용)
     * @return 시스템 상태 정보
     */
    public String getSystemStatus() {
        try {
            StringBuilder status = new StringBuilder();
            status.append("=== 이메일 인증 시스템 상태 ===\n");
            
            // 이메일 설정 테스트
            boolean emailConfigOk = EmailService.testEmailConfiguration();
            status.append("이메일 설정: ").append(emailConfigOk ? "정상" : "오류").append("\n");
            
            // 데이터베이스 연결 테스트
            try {
                int expiredCount = emailVerificationDAO.deleteExpiredTokens();
                status.append("데이터베이스 연결: 정상\n");
                status.append("만료 토큰 정리: ").append(expiredCount).append("개\n");
            } catch (Exception e) {
                status.append("데이터베이스 연결: 오류 - ").append(e.getMessage()).append("\n");
            }
            
            status.append("토큰 만료 시간: ").append(EmailConfig.getVerificationExpiryHours()).append("시간\n");
            status.append("발송 제한: 이메일당 ").append(EmailConfig.getRateLimitPerEmail()).append("회/시간\n");
            status.append("=============================");
            
            return status.toString();
            
        } catch (Exception e) {
            return "시스템 상태 확인 실패: " + e.getMessage();
        }
    }
}