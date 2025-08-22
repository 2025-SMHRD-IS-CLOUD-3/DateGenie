package com.smhrd.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.smhrd.model.EmailDAO;
import com.smhrd.model.MemberDAO;


@WebServlet("/SendVerificationEmail")
public class SendVerificationEmail extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// SendGrid 설정
    private static final String SENDGRID_API_KEY = System.getenv("SendGrid_API_KEY");
    private static final String FROM_EMAIL = "rkskek1101@gmail.com"; // 본인 이메일
    private static final String FROM_NAME = "DateGenie";
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
System.out.println("=== SendVerificationEmail 서블릿 호출됨 ===");
        
        // 인코딩 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        try {
            // 1. 이메일 파라미터 받기
            String email = request.getParameter("email");
            System.out.println("요청된 이메일: " + email);
            
            // 이메일 유효성 검사
            if (email == null || email.trim().isEmpty()) {
                System.out.println("ERROR: 이메일이 비어있음");
                sendErrorResponse(response, "이메일이 필요합니다.");
                return;
            }
            
            // 2. 이메일 중복 체크 (추가된 부분)
            System.out.println("=== 이메일 중복 체크 시작 ===");
            
            // 실제 회원 테이블 체크
            MemberDAO memberDAO = new MemberDAO();
            System.out.println("MemberDAO 객체 생성 완료");
            System.out.println("회원 테이블 중복 체크 시도...");
            boolean isAlreadyMember = memberDAO.isEmailExists(email);
            System.out.println("회원 테이블 중복 체크 완료");
            System.out.println("회원 테이블 중복 체크 결과: " + isAlreadyMember);

            if (isAlreadyMember) {
                System.out.println("=== 이미 가입된 회원 감지 ===");
                System.out.println("이미 가입된 이메일: " + email);
                sendErrorResponse(response, "이미 가입된 이메일입니다.");
                return;
            }
            
            
            // 인증 진행 중인지 체크
            EmailDAO emailDAO = new EmailDAO();
            System.out.println("EmailDAO 객체 생성 완료");
            
            System.out.println("isEmailVerified 메서드 호출 시도...");
            boolean isAlreadyRegistered = emailDAO.isEmailVerified(email);
            System.out.println("isEmailVerified 메서드 호출 완료");
            System.out.println("중복 체크 결과: " + isAlreadyRegistered);
            
            if (isAlreadyRegistered) {
                System.out.println("=== 중복된 이메일 감지 ===");
                System.out.println("이미 가입된 이메일: " + email);
                sendErrorResponse(response, "이미 가입된 이메일입니다.");
                return;
            }
            
            System.out.println("=== 중복 체크 통과 - 인증 메일 발송 진행 ===");
            
            // 2. 고유 토큰 생성
            String verificationToken = UUID.randomUUID().toString();
            System.out.println("생성된 토큰: " + verificationToken);
            
            // 3. 만료시간 설정 (1시간 후)
            long currentTime = System.currentTimeMillis();
            Timestamp expiresAt = new Timestamp(currentTime + (60 * 60 * 1000)); // 1시간
            System.out.println("토큰 만료시간: " + expiresAt);
            
            // 4. DB에 토큰 저장
            int saveResult = emailDAO.saveVerificationToken(email, verificationToken, expiresAt);
            System.out.println("DB 저장 결과: " + saveResult);
            
            if (saveResult <= 0) {
                System.out.println("ERROR: DB 저장 실패");
                sendErrorResponse(response, "데이터베이스 저장에 실패했습니다.");
                return;
            }
            
            // 5. 인증 링크 생성
            String verificationLink = request.getScheme() + "://" + 
                                    request.getServerName() + ":" + 
                                    request.getServerPort() + 
                                    request.getContextPath() + 
                                    "/EmailVerification?token=" + verificationToken;
            System.out.println("인증 링크: " + verificationLink);
            
            // 6. 이메일 내용 생성
            String emailContent = createEmailContent(verificationLink);
            System.out.println("이메일 내용 생성 완료");
            
            // 7. SendGrid로 이메일 발송
            boolean emailSent = sendEmail(email, emailContent);
            System.out.println("이메일 발송 결과: " + emailSent);
            
            if (emailSent) {
                System.out.println("SUCCESS: 인증메일 발송 성공");
                sendSuccessResponse(response, "인증메일이 발송되었습니다.");
            } else {
                System.out.println("ERROR: 이메일 발송 실패");
                sendErrorResponse(response, "이메일 발송에 실패했습니다.");
            }
            
        } catch (Exception e) {
            System.out.println("=== EXCEPTION 발생 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, "서버 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 이메일 내용 생성
    private String createEmailContent(String verificationLink) {
        return "<html><body>" +
               "<h2>Date Genie 이메일 인증</h2>" +
               "<p>안녕하세요! Date Genie 회원가입을 위한 이메일 인증입니다.</p>" +
               "<p>아래 버 튼을 클릭하여 이메일 인증을 완료해주세요:</p>" +
               "<a href='" + verificationLink + "' style='background-color: #ec4899; color: white; padding: 12px 24px; text-decoration: none; border-radius: 8px; display: inline-block;'>이메일 인증하기</a>" +
               "<p>또는 다음 링크를 복사하여 브라우저에 붙여넣으세요:</p>" +
               "<p>" + verificationLink + "</p>" +
               "<p>이 링크는 1시간 후에 만료됩니다.</p>" +
               "<p>감사합니다.<br>Date Genie 팀</p>" +
               "</body></html>";
    }
    
    // SendGrid 이메일 발송
    private boolean sendEmail(String toEmail, String emailContent) {
        try {
            System.out.println("SendGrid 이메일 발송 시작...");
            
            Email from = new Email(FROM_EMAIL, FROM_NAME);
            Email to = new Email(toEmail);
            String subject = "[Date Genie] 이메일 인증을 완료해주세요";
            Content content = new Content("text/html", emailContent);
            
            Mail mail = new Mail(from, subject, to, content);
            
            SendGrid sg = new SendGrid(SENDGRID_API_KEY);
            Request mailRequest = new Request();
            
            mailRequest.setMethod(Method.POST);
            mailRequest.setEndpoint("mail/send");
            mailRequest.setBody(mail.build());
            
            Response sendGridResponse = sg.api(mailRequest);
            
            System.out.println("SendGrid 응답 코드: " + sendGridResponse.getStatusCode());
            System.out.println("SendGrid 응답 본문: " + sendGridResponse.getBody());
            
            // 2xx 응답이면 성공
            return sendGridResponse.getStatusCode() >= 200 && sendGridResponse.getStatusCode() < 300;
            
        } catch (Exception e) {
            System.out.println("SendGrid 이메일 발송 중 오류:");
            e.printStackTrace();
            return false;
        }
    }
    
    // 성공 응답 전송
    private void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        String jsonResponse = "{\"success\": true, \"message\": \"" + message + "\"}";
        response.getWriter().write(jsonResponse);
        System.out.println("SUCCESS 응답 전송: " + jsonResponse);
    }
    
    // 에러 응답 전송
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        String jsonResponse = "{\"success\": false, \"message\": \"" + message + "\"}";
        response.getWriter().write(jsonResponse);
        System.out.println("ERROR 응답 전송: " + jsonResponse);
		
	}

}
