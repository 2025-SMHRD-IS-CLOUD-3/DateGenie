package com.smhrd.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.smhrd.model.EmailDAO;


@WebServlet("/CheckVerificationStatus")
public class CheckVerificationStatus extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
        System.out.println("=== CheckVerificationStatus 서블릿 호출됨 ===");
        
        // 인코딩 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        
        try {
            // 1. 이메일 파라미터 받기
            String email = request.getParameter("email");
            System.out.println("인증 상태 확인 요청 이메일: " + email);
            
            // 이메일 유효성 검사
            if (email == null || email.trim().isEmpty()) {
                System.out.println("ERROR: 이메일이 비어있음");
                sendErrorResponse(response, "이메일이 필요합니다.");
                return;
            }
            
            // 2. EmailDAO로 인증 상태 확인
            EmailDAO emailDAO = new EmailDAO();
            boolean isVerified = emailDAO.isEmailVerified(email);
            System.out.println("이메일 인증 상태: " + isVerified);
            
            // 3. JSON 응답 전송
            if (isVerified) {
                System.out.println("SUCCESS: 이메일 인증 완료됨");
                sendVerifiedResponse(response);
            } else {
                System.out.println("INFO: 이메일 인증 아직 안됨");
                sendNotVerifiedResponse(response);
            }
            
        } catch (Exception e) {
            System.out.println("=== EXCEPTION 발생 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, "서버 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 인증 완료 응답
    private void sendVerifiedResponse(HttpServletResponse response) throws IOException {
        String jsonResponse = "{\"verified\": true, \"message\": \"이메일 인증이 완료되었습니다.\"}";
        response.getWriter().write(jsonResponse);
        System.out.println("VERIFIED 응답 전송: " + jsonResponse);
    }
    
    // 인증 미완료 응답
    private void sendNotVerifiedResponse(HttpServletResponse response) throws IOException {
        String jsonResponse = "{\"verified\": false, \"message\": \"이메일 인증이 아직 완료되지 않았습니다.\"}";
        response.getWriter().write(jsonResponse);
        System.out.println("NOT VERIFIED 응답 전송: " + jsonResponse);
    }
    
    // 에러 응답
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        String jsonResponse = "{\"verified\": false, \"error\": true, \"message\": \"" + message + "\"}";
        response.getWriter().write(jsonResponse);
        System.out.println("ERROR 응답 전송: " + jsonResponse);
		
	}

}
