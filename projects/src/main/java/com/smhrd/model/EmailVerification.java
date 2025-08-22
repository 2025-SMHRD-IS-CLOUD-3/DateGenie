package com.smhrd.model;

import java.sql.Timestamp;

/**
 * 이메일 인증 정보를 담는 모델 클래스
 * USER_EMAIL_AUTH 테이블과 매핑됩니다.
 */
public class EmailVerification {
    
    // 기본키 (NUMBER)
    private Long id;
    
    // 인증할 이메일 주소 (VARCHAR2(255))
    private String email;
    
    // 인증 토큰 (VARCHAR2(255))
    private String verificationToken;
    
    // 생성일시 (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
    private Timestamp createdAt;
    
    // 만료일시 (TIMESTAMP)
    private Timestamp expiresAt;
    
    // 인증 완료 여부 (NUMBER(1,0), DEFAULT 0)
    // 0: 미인증, 1: 인증완료
    private Integer verified;
    
    // 기본 생성자
    public EmailVerification() {}
    
    // 이메일 인증 생성용 생성자
    public EmailVerification(String email, String verificationToken, Timestamp expiresAt) {
        this.email = email;
        this.verificationToken = verificationToken;
        this.expiresAt = expiresAt;
        this.verified = 0; // 기본값: 미인증
    }
    
    // Standard JavaBean getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getVerificationToken() {
        return verificationToken;
    }
    
    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Integer getVerified() {
        return verified;
    }
    
    public void setVerified(Integer verified) {
        this.verified = verified;
    }
    
    // 인증 완료 처리 메서드
    public void markAsVerified() {
        this.verified = 1;
    }
    
    // 인증 완료 여부 확인 메서드
    public boolean checkVerified() {
        return this.verified != null && this.verified == 1;
    }
    
    // 인증 완료 여부 확인 메서드 (별칭)
    public boolean isVerified() {
        return checkVerified();
    }
    
    // 토큰 만료 여부 확인 메서드
    public boolean isExpired() {
        if (expiresAt == null) {
            return true;
        }
        return new Timestamp(System.currentTimeMillis()).after(expiresAt);
    }
    
    // 유효한 토큰인지 확인 (인증되지 않았고 만료되지 않은 경우)
    public boolean isValidForVerification() {
        return !checkVerified() && !isExpired();
    }
    
    @Override
    public String toString() {
        return "EmailVerification [id=" + id + ", email=" + email + 
               ", verified=" + verified + ", createdAt=" + createdAt + 
               ", expiresAt=" + expiresAt + "]";
    }
}