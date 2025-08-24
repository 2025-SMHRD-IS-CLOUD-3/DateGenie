package com.smhrd.service;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.UnsupportedEncodingException;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.smhrd.util.EmailConfig;

/**
 * 이메일 발송을 담당하는 서비스 클래스
 * JavaMail API를 사용하여 SMTP를 통해 이메일을 발송합니다.
 */
public class EmailService {
    
    // 발송 제한 기능 제거됨 - 무제한 이메일 발송 허용
    
    // 이메일 세션 (싱글톤 패턴)
    private static Session emailSession = null;
    
    /**
     * 이메일 세션 초기화 및 가져오기
     */
    private static Session getEmailSession() {
        if (emailSession == null) {
            synchronized (EmailService.class) {
                if (emailSession == null) {
                    // 한글 인코딩을 위한 시스템 속성 설정 (전역)
                    System.setProperty("mail.mime.charset", "UTF-8");
                    System.setProperty("mail.mime.encodefilename", "true");
                    System.setProperty("mail.mime.decodefilename", "true");
                    System.setProperty("file.encoding", "UTF-8");
                    
                    Properties props = EmailConfig.getSmtpProperties();
                    
                    // SMTP 인증이 필요한 경우
                    if (EmailConfig.isSmtpAuthEnabled()) {
                        emailSession = Session.getInstance(props, new Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(
                                    EmailConfig.getUsername(), 
                                    EmailConfig.getPassword()
                                );
                            }
                        });
                    } else {
                        emailSession = Session.getInstance(props);
                    }
                    
                    // 디버그 모드 설정
                    emailSession.setDebug(EmailConfig.isDebugMode());
                }
            }
        }
        return emailSession;
    }
    
    /**
     * 이메일 인증 메일 발송 (토큰과 6자리 코드 포함)
     * @param toEmail 수신자 이메일 주소
     * @param verificationToken 인증 토큰
     * @param verificationCode 6자리 인증 코드
     * @param baseUrl 웹사이트 기본 URL (예: http://localhost:8081/DateGenie)
     * @return 발송 성공 여부
     */
    public static boolean sendVerificationEmail(String toEmail, String verificationToken, String verificationCode, String baseUrl) {
        
        // 발송 제한 체크 제거 - 무제한 발송 허용
        
        try {
            Session session = getEmailSession();
            MimeMessage message = new MimeMessage(session);
            
            // 한글 인코딩을 위한 시스템 속성 설정
            System.setProperty("mail.mime.charset", "UTF-8");
            
            // 발송자 설정 (UTF-8 직접 설정)
            message.setFrom(new InternetAddress(EmailConfig.getFromAddress(), EmailConfig.getFromName(), "UTF-8"));
            
            // 수신자 설정
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            
            // 제목 설정 (영문으로 하드코딩하여 인코딩 문제 완전 해결)
            message.setSubject("DateGenie Email Verification", "UTF-8");
            
            // 인증 링크 생성
            String verificationUrl = baseUrl + "/verify-email?token=" + verificationToken;
            
            // 이메일 본문 생성 (HTML)
            String htmlContent = createVerificationEmailContent(verificationUrl, verificationToken, verificationCode);
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            
            // 추가 헤더 설정 (한글 처리를 위함)
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setHeader("Content-Transfer-Encoding", "quoted-printable");
            
            // 이메일 발송
            Transport.send(message);
            
            // 발송 카운터 기능 제거됨
            
            System.out.println("이메일 인증 메일 발송 완료: " + toEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("이메일 발송 실패: " + toEmail + ", 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 인증 이메일 HTML 내용 생성
     * @param verificationUrl 인증 링크 URL
     * @param token 인증 토큰
     * @param verificationCode 6자리 인증 코드
     * @return HTML 형식의 이메일 내용
     */
    private static String createVerificationEmailContent(String verificationUrl, String token, String verificationCode) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"ko\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>DateGenie Email Verification</title>");
        html.append("<style>");
        html.append("body { font-family: 'Malgun Gothic', Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }");
        html.append(".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }");
        html.append(".button { display: inline-block; background: #667eea; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }");
        html.append(".button:hover { background: #5a6fd8; }");
        html.append(".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }");
        html.append(".warning { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        html.append(".verification-code { background: #e3f2fd; border: 2px solid #2196F3; padding: 20px; border-radius: 10px; margin: 20px 0; text-align: center; }");
        html.append(".code-number { font-size: 28px; font-weight: bold; color: #1976D2; letter-spacing: 4px; font-family: monospace; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class=\"container\">");
        html.append("<div class=\"header\">");
        html.append("<h1>🎯 DateGenie</h1>");
        html.append("<p>이메일 인증을 완료해주세요</p>");
        html.append("</div>");
        
        html.append("<div class=\"content\">");
        html.append("<h2>안녕하세요!</h2>");
        html.append("<p>DateGenie 서비스에 가입해주셔서 감사합니다.</p>");
        html.append("<p>계정 보안을 위해 이메일 인증이 필요합니다. 아래 인증 코드를 사용하거나 버튼을 클릭하여 인증을 완료해주세요.</p>");
        
        html.append("<div class=\"verification-code\">");
        html.append("<h3 style=\"margin-top: 0; color: #1976D2;\">📱 인증 코드</h3>");
        html.append("<div class=\"code-number\">").append(verificationCode).append("</div>");
        html.append("<p style=\"margin-bottom: 0; color: #666; font-size: 14px;\">위 6자리 숫자를 회원가입 페이지에 입력해주세요</p>");
        html.append("</div>");
        
        html.append("<div style=\"text-align: center;\">");
        html.append("<a href=\"").append(verificationUrl).append("\" class=\"button\">");
        html.append("✅ 이메일 인증 완료하기");
        html.append("</a>");
        html.append("</div>");
        
        html.append("<div class=\"warning\">");
        html.append("<strong>⚠️ 중요:</strong>");
        html.append("<ul>");
        html.append("<li>이 링크는 ").append(EmailConfig.getVerificationExpiryHours()).append("시간 후 만료됩니다</li>");
        html.append("<li>인증을 완료하지 않으면 일부 서비스 이용에 제한이 있을 수 있습니다</li>");
        html.append("<li>본인이 가입하지 않았다면 이 이메일을 무시해주세요</li>");
        html.append("</ul>");
        html.append("</div>");
        
        html.append("<p>링크가 작동하지 않는 경우, 아래 URL을 복사하여 브라우저에 직접 입력해주세요:</p>");
        html.append("<p style=\"word-break: break-all; background: #e9ecef; padding: 10px; border-radius: 5px; font-family: monospace;\">");
        html.append(verificationUrl);
        html.append("</p>");
        
        html.append("<p>문의사항이 있으시면 언제든지 연락해주세요.</p>");
        html.append("<p>감사합니다.<br><strong>DateGenie 팀</strong></p>");
        html.append("</div>");
        
        html.append("<div class=\"footer\">");
        html.append("<p>이 이메일은 자동으로 발송된 메일입니다. 회신하지 마세요.</p>");
        html.append("<p>인증 토큰: ").append(token.substring(0, 8)).append("...</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    // 이메일 발송 제한 기능이 완전히 제거됨
    // 이제 모든 이메일이 제한 없이 발송됩니다.
    
    /**
     * 테스트용 이메일 발송 (개발 시에만 사용)
     * @param toEmail 수신자 이메일
     * @param subject 제목
     * @param content 내용
     * @return 발송 성공 여부
     */
    public static boolean sendTestEmail(String toEmail, String subject, String content) {
        try {
            Session session = getEmailSession();
            MimeMessage message = new MimeMessage(session);
            
            // 한글 인코딩을 위한 시스템 속성 설정
            System.setProperty("mail.mime.charset", "UTF-8");
            
            // 발송자 설정 (한글 이름 인코딩 처리)
            message.setFrom(new InternetAddress(EmailConfig.getFromAddress(), EmailConfig.getFromName(), "UTF-8"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject, "UTF-8");
            message.setText(content, "UTF-8");
            
            // 헤더 설정 추가
            message.setHeader("Content-Type", "text/plain; charset=UTF-8");
            message.setHeader("Content-Transfer-Encoding", "base64");
            
            Transport.send(message);
            
            System.out.println("테스트 이메일 발송 완료: " + toEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("테스트 이메일 발송 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 이메일 설정 테스트
     * @return 설정이 올바른지 여부
     */
    public static boolean testEmailConfiguration() {
        try {
            // 기본 설정 확인
            if (EmailConfig.getUsername() == null || EmailConfig.getUsername().isEmpty()) {
                System.err.println("이메일 사용자명이 설정되지 않았습니다");
                return false;
            }
            
            if (EmailConfig.getPassword() == null || EmailConfig.getPassword().isEmpty()) {
                System.err.println("이메일 패스워드가 설정되지 않았습니다");
                return false;
            }
            
            // 세션 생성 테스트
            Session session = getEmailSession();
            if (session == null) {
                System.err.println("이메일 세션 생성 실패");
                return false;
            }
            
            System.out.println("이메일 설정 테스트 통과");
            return true;
            
        } catch (Exception e) {
            System.err.println("이메일 설정 테스트 실패: " + e.getMessage());
            return false;
        }
    }
}