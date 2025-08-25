package com.smhrd.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.smhrd.service.EmailVerificationService;
import com.smhrd.service.EmailVerificationService.VerificationResult;
import com.smhrd.util.SecurityUtils;

/**
 * 6자리 인증 코드로 이메일 인증을 처리하는 서비스
 * POST /VerifyEmailCodeService - 6자리 인증 코드 확인
 */
@WebServlet("/VerifyEmailCodeService")
public class VerifyEmailCodeService extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final EmailVerificationService emailVerificationService;
    
    public VerifyEmailCodeService() {
        this.emailVerificationService = new EmailVerificationService();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // CORS 및 기본 설정
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type");
            
            request.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            
            // 요청 파라미터 추출
            String email = request.getParameter("email");
            String verificationCode = request.getParameter("code");
            
            // 입력 검증
            if (email == null || email.trim().isEmpty()) {
                sendErrorResponse(response, "이메일을 입력해주세요.");
                return;
            }
            
            if (verificationCode == null || verificationCode.trim().isEmpty()) {
                sendErrorResponse(response, "인증 코드를 입력해주세요.");
                return;
            }
            
            // 입력 정리 및 검증
            email = SecurityUtils.sanitizeInput(email.trim());
            verificationCode = SecurityUtils.sanitizeInput(verificationCode.trim());
            
            if (!SecurityUtils.isEmailValid(email)) {
                sendErrorResponse(response, "올바른 이메일 형식을 입력해주세요.");
                return;
            }
            
            // 6자리 숫자 코드 형식 검증
            if (!verificationCode.matches("^[0-9]{6}$")) {
                sendErrorResponse(response, "인증 코드는 6자리 숫자여야 합니다.");
                return;
            }
            
            // 이메일 코드 인증 서비스 호출
            VerificationResult result = emailVerificationService.verifyEmailCode(email, verificationCode);
            
            // 결과 응답
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("success", result.isSuccess());
            jsonResponse.put("message", result.getMessage());
            
            if (result.isSuccess()) {
                jsonResponse.put("email", email);
                jsonResponse.put("verified", true);
                jsonResponse.put("action", "verified");
            }
            
            sendJsonResponse(response, jsonResponse);
            
        } catch (Exception e) {
            System.err.println("이메일 코드 인증 컨트롤러 오류: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, "서버 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // CORS preflight 요청 처리
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * JSON 응답 전송
     * @param response HTTP 응답
     * @param data 전송할 데이터
     * @throws IOException
     */
    private void sendJsonResponse(HttpServletResponse response, Map<String, Object> data) throws IOException {
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        out.print(gson.toJson(data));
        out.flush();
    }
    
    /**
     * 오류 응답 전송
     * @param response HTTP 응답
     * @param message 오류 메시지
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        
        sendJsonResponse(response, errorResponse);
    }
}