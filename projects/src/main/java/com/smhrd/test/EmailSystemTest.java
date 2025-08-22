package com.smhrd.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.smhrd.service.EmailService;
import com.smhrd.service.EmailVerificationService;
import com.smhrd.util.EmailConfig;

/**
 * 이메일 시스템 전체 테스트 클래스
 * 설정, 데이터베이스 연결, 이메일 발송 기능을 종합적으로 테스트합니다.
 */
public class EmailSystemTest {
    
    public static void main(String[] args) {
        System.out.println("=== DateGenie 이메일 시스템 진단 시작 ===\n");
        
        // 1. 이메일 설정 테스트
        testEmailConfiguration();
        
        // 2. 데이터베이스 연결 및 테이블 존재 확인
        testDatabaseConnection();
        
        // 3. 이메일 서비스 테스트
        testEmailService();
        
        // 4. 이메일 인증 서비스 시스템 상태
        testEmailVerificationService();
        
        System.out.println("\n=== DateGenie 이메일 시스템 진단 완료 ===");
    }
    
    /**
     * 이메일 설정 테스트
     */
    private static void testEmailConfiguration() {
        System.out.println("1. 이메일 설정 테스트");
        System.out.println("-------------------");
        
        try {
            // 설정 정보 출력
            EmailConfig.printConfig();
            
            // 중요 설정 값 확인
            String username = EmailConfig.getUsername();
            String password = EmailConfig.getPassword();
            String fromAddress = EmailConfig.getFromAddress();
            
            System.out.println("\n[진단 결과]");
            
            if (username != null && !username.isEmpty()) {
                System.out.println("✅ SMTP 사용자명: 설정됨 (" + username + ")");
            } else {
                System.out.println("❌ SMTP 사용자명: 설정되지 않음 - 이메일 발송 불가");
            }
            
            if (password != null && !password.isEmpty()) {
                System.out.println("✅ SMTP 패스워드: 설정됨 (길이: " + password.length() + ")");
            } else {
                System.out.println("❌ SMTP 패스워드: 설정되지 않음 - 이메일 발송 불가");
            }
            
            if (fromAddress != null && !fromAddress.isEmpty()) {
                System.out.println("✅ 발송자 주소: 설정됨 (" + fromAddress + ")");
            } else {
                System.out.println("❌ 발송자 주소: 설정되지 않음");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 이메일 설정 테스트 실패: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 데이터베이스 연결 및 테이블 존재 확인
     */
    private static void testDatabaseConnection() {
        System.out.println("2. 데이터베이스 연결 테스트");
        System.out.println("-------------------------");
        
        Connection conn = null;
        
        try {
            // 데이터베이스 연결 정보
            String url = "jdbc:oracle:thin:@project-db-campus.smhrd.com:1524:xe";
            String username = "campus_24IS_CLOUD3_p2_4";
            String password = "smhrd4";
            
            System.out.println("데이터베이스 URL: " + url);
            System.out.println("사용자명: " + username);
            
            // 연결 시도
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ 데이터베이스 연결 성공");
            
            // 메타데이터 가져오기
            DatabaseMetaData metaData = conn.getMetaData();
            
            // EMAIL_VERIFICATION 테이블 존재 확인
            ResultSet tables = metaData.getTables(null, "CAMPUS_24IS_CLOUD3_P2_4", "EMAIL_VERIFICATION", null);
            
            if (tables.next()) {
                System.out.println("✅ EMAIL_VERIFICATION 테이블 존재함");
                
                // 테이블 컬럼 정보 확인
                ResultSet columns = metaData.getColumns(null, "CAMPUS_24IS_CLOUD3_P2_4", "EMAIL_VERIFICATION", null);
                System.out.println("테이블 컬럼:");
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String dataType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    System.out.println("  - " + columnName + " (" + dataType + "(" + columnSize + "))");
                }
                columns.close();
                
            } else {
                System.out.println("❌ EMAIL_VERIFICATION 테이블이 존재하지 않음");
                System.out.println("   해결방법: projects/src/main/resources/sql/create_email_verification_sequence.sql 실행 필요");
                System.out.println("   먼저 테이블을 생성해야 합니다:");
                System.out.println("   CREATE TABLE CAMPUS_24IS_CLOUD3_P2_4.EMAIL_VERIFICATION (");
                System.out.println("       ID NUMBER PRIMARY KEY,");
                System.out.println("       EMAIL VARCHAR2(255) NOT NULL,");
                System.out.println("       VERIFICATION_TOKEN VARCHAR2(255) UNIQUE NOT NULL,");
                System.out.println("       CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,");
                System.out.println("       EXPIRES_AT TIMESTAMP NOT NULL,");
                System.out.println("       VERIFIED NUMBER(1,0) DEFAULT 0");
                System.out.println("   );");
            }
            tables.close();
            
            // 시퀀스 존재 확인
            ResultSet sequences = metaData.getTables(null, "CAMPUS_24IS_CLOUD3_P2_4", "EMAIL_VERIFICATION_SEQ", new String[]{"SEQUENCE"});
            if (sequences.next()) {
                System.out.println("✅ EMAIL_VERIFICATION_SEQ 시퀀스 존재함");
            } else {
                System.out.println("⚠️ EMAIL_VERIFICATION_SEQ 시퀀스 확인 불가 (메타데이터 제한)");
            }
            sequences.close();
            
        } catch (Exception e) {
            System.out.println("❌ 데이터베이스 연결 실패: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                // 무시
            }
        }
        
        System.out.println();
    }
    
    /**
     * 이메일 서비스 테스트
     */
    private static void testEmailService() {
        System.out.println("3. 이메일 서비스 테스트");
        System.out.println("---------------------");
        
        try {
            boolean configTest = EmailService.testEmailConfiguration();
            
            if (configTest) {
                System.out.println("✅ 이메일 서비스 설정 정상");
            } else {
                System.out.println("❌ 이메일 서비스 설정 오류");
                System.out.println("   가능한 원인:");
                System.out.println("   1. email.properties 파일이 로드되지 않음");
                System.out.println("   2. SMTP 인증 정보가 올바르지 않음");
                System.out.println("   3. 네트워크 연결 문제");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 이메일 서비스 테스트 실패: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 이메일 인증 서비스 시스템 상태 테스트
     */
    private static void testEmailVerificationService() {
        System.out.println("4. 이메일 인증 서비스 시스템 상태");
        System.out.println("--------------------------------");
        
        try {
            EmailVerificationService service = new EmailVerificationService();
            String status = service.getSystemStatus();
            System.out.println(status);
            
        } catch (Exception e) {
            System.out.println("❌ 이메일 인증 서비스 상태 확인 실패: " + e.getMessage());
        }
        
        System.out.println();
    }
}