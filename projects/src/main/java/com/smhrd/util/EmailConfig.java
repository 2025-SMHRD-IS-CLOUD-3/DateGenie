package com.smhrd.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 이메일 발송 관련 설정을 관리하는 클래스
 * email.properties 파일에서 SMTP 설정을 로드합니다.
 */
public class EmailConfig {
    
    private static final String CONFIG_FILE = "/config/email.properties";
    private static Properties properties = new Properties();
    
    // 싱글톤 패턴으로 설정 로드
    static {
        loadConfig();
    }
    
    /**
     * 설정 파일 로드
     */
    private static void loadConfig() {
        try (InputStream inputStream = EmailConfig.class.getResourceAsStream(CONFIG_FILE)) {
            
            if (inputStream != null) {
                properties.load(inputStream);
                System.out.println("이메일 설정 파일 로드 완료: " + CONFIG_FILE);
            } else {
                System.err.println("이메일 설정 파일을 찾을 수 없습니다: " + CONFIG_FILE);
                loadDefaultConfig();
            }
            
        } catch (IOException e) {
            System.err.println("이메일 설정 파일 로드 실패: " + e.getMessage());
            loadDefaultConfig();
        }
    }
    
    /**
     * 기본 설정 로드 (설정 파일이 없는 경우)
     */
    private static void loadDefaultConfig() {
        properties.setProperty("email.smtp.host", "smtp.gmail.com");
        properties.setProperty("email.smtp.port", "587");
        properties.setProperty("email.smtp.auth", "true");
        properties.setProperty("email.smtp.starttls.enable", "true");
        properties.setProperty("email.from.address", "dategenie.noreply@gmail.com");
        properties.setProperty("email.from.name", "DateGenie");
        properties.setProperty("email.username", "dategenie.noreply@gmail.com");
        properties.setProperty("email.password", "");
        properties.setProperty("email.verification.subject", "DateGenie Email Verification");
        properties.setProperty("email.verification.expiry.hours", "24");
        // 발솨 제한 설정 제거됨 - 무제한 발송 허용
        properties.setProperty("email.debug", "false");
    }
    
    /**
     * SMTP 호스트 가져오기
     */
    public static String getSmtpHost() {
        return properties.getProperty("email.smtp.host");
    }
    
    /**
     * SMTP 포트 가져오기
     */
    public static int getSmtpPort() {
        return Integer.parseInt(properties.getProperty("email.smtp.port", "587"));
    }
    
    /**
     * SMTP 인증 사용 여부
     */
    public static boolean isSmtpAuthEnabled() {
        return Boolean.parseBoolean(properties.getProperty("email.smtp.auth", "true"));
    }
    
    /**
     * STARTTLS 사용 여부
     */
    public static boolean isStartTlsEnabled() {
        return Boolean.parseBoolean(properties.getProperty("email.smtp.starttls.enable", "true"));
    }
    
    /**
     * 발송자 이메일 주소 가져오기 (환경변수 우선)
     */
    public static String getFromAddress() {
        // 1. 환경변수에서 확인
        String envEmail = System.getenv("EMAIL_FROM_ADDRESS");
        if (envEmail != null && !envEmail.isEmpty()) {
            return envEmail;
        }
        
        // 2. 시스템 프로퍼티에서 확인
        String sysEmail = System.getProperty("EMAIL_FROM_ADDRESS");
        if (sysEmail != null && !sysEmail.isEmpty()) {
            return sysEmail;
        }
        
        // 3. 설정 파일에서 확인
        return properties.getProperty("email.from.address");
    }
    
    /**
     * 발송자 이름 가져오기
     */
    public static String getFromName() {
        return properties.getProperty("email.from.name", "DateGenie");
    }
    
    /**
     * SMTP 사용자명 가져오기 (환경변수 우선)
     */
    public static String getUsername() {
        // 1. 환경변수에서 확인
        String envUsername = System.getenv("EMAIL_USERNAME");
        if (envUsername != null && !envUsername.isEmpty()) {
            return envUsername;
        }
        
        // 2. 시스템 프로퍼티에서 확인
        String sysUsername = System.getProperty("EMAIL_USERNAME");
        if (sysUsername != null && !sysUsername.isEmpty()) {
            return sysUsername;
        }
        
        // 3. 설정 파일에서 확인
        return properties.getProperty("email.username");
    }
    
    /**
     * SMTP 패스워드 가져오기 (환경변수 우선)
     */
    public static String getPassword() {
        // 1. 환경변수에서 확인
        String envPassword = System.getenv("EMAIL_PASSWORD");
        if (envPassword != null && !envPassword.isEmpty()) {
            return envPassword;
        }
        
        // 2. 시스템 프로퍼티에서 확인
        String sysPassword = System.getProperty("EMAIL_PASSWORD");
        if (sysPassword != null && !sysPassword.isEmpty()) {
            return sysPassword;
        }
        
        // 3. 설정 파일에서 확인
        return properties.getProperty("email.password", "");
    }
    
    /**
     * 인증 이메일 제목 가져오기
     */
    public static String getVerificationSubject() {
        return properties.getProperty("email.verification.subject", "DateGenie 이메일 인증");
    }
    
    /**
     * 인증 토큰 만료 시간 (시간 단위)
     */
    public static int getVerificationExpiryHours() {
        return Integer.parseInt(properties.getProperty("email.verification.expiry.hours", "24"));
    }
    
    // 발송 제한 설정 메소드들이 제거됨
    // 이제 무제한 이메일 발송이 가능합니다.
    
    /**
     * 디버그 모드 여부
     */
    public static boolean isDebugMode() {
        return Boolean.parseBoolean(properties.getProperty("email.debug", "false"));
    }
    
    /**
     * SMTP Properties 객체 생성
     */
    public static Properties getSmtpProperties() {
        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.host", getSmtpHost());
        smtpProps.put("mail.smtp.port", String.valueOf(getSmtpPort()));
        smtpProps.put("mail.smtp.auth", String.valueOf(isSmtpAuthEnabled()));
        smtpProps.put("mail.smtp.starttls.enable", String.valueOf(isStartTlsEnabled()));
        
        // 한글 인코딩을 위한 추가 속성 설정
        smtpProps.put("mail.mime.charset", "UTF-8");
        smtpProps.put("mail.smtp.sendpartial", "true");
        smtpProps.put("mail.mime.encodefilename", "true");
        smtpProps.put("mail.mime.decodefilename", "true");
        
        if (isDebugMode()) {
            smtpProps.put("mail.debug", "true");
        }
        
        return smtpProps;
    }
    
    /**
     * 설정 확인 및 출력 (디버그용)
     */
    public static void printConfig() {
        System.out.println("=== 이메일 설정 ===");
        System.out.println("SMTP Host: " + getSmtpHost());
        System.out.println("SMTP Port: " + getSmtpPort());
        System.out.println("SMTP Auth: " + isSmtpAuthEnabled());
        System.out.println("STARTTLS: " + isStartTlsEnabled());
        System.out.println("From Address: " + getFromAddress());
        System.out.println("From Name: " + getFromName());
        System.out.println("Username: " + (getUsername() != null ? "설정됨" : "설정되지 않음"));
        System.out.println("Password: " + (getPassword() != null && !getPassword().isEmpty() ? "설정됨" : "설정되지 않음"));
        System.out.println("Verification Subject: " + getVerificationSubject());
        System.out.println("Verification Expiry: " + getVerificationExpiryHours() + "시간");
        System.out.println("발송 제한: 비활성화됨 (무제한 발송 가능)");
        System.out.println("Debug Mode: " + isDebugMode());
        System.out.println("==================");
    }
}