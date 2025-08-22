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

/**
 * 이메일 인증 토큰 검증을 처리하는 컨트롤러
 * GET /verify-email?token=... - 이메일 인증 링크 처리 (브라우저에서 클릭)
 * POST /VerifyEmailController - Ajax를 통한 토큰 검증
 */
@WebServlet({"/verify-email", "/VerifyEmailController"})
public class VerifyEmailController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final EmailVerificationService emailVerificationService;
    
    public VerifyEmailController() {
        this.emailVerificationService = new EmailVerificationService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            request.setCharacterEncoding("UTF-8");
            
            // 토큰 파라미터 추출
            String token = request.getParameter("token");
            
            if (token == null || token.trim().isEmpty()) {
                // 토큰이 없는 경우 - 회원가입 페이지로 리다이렉트
                response.sendRedirect("signup.html?verification_error=missing_token");
                return;
            }
            
            // 토큰 검증
            VerificationResult result = emailVerificationService.verifyEmail(token.trim());
            
            if (result.isSuccess()) {
                // 인증 성공 - 회원가입 페이지로 리다이렉트
                String redirectUrl = "signup.html?verification_success=true&email=" + 
                    java.net.URLEncoder.encode(result.getEmail(), "UTF-8");
                response.sendRedirect(redirectUrl);
                
            } else {
                // 인증 실패 - 회원가입 페이지로 리다이렉트
                String errorMessage = java.net.URLEncoder.encode(result.getMessage(), "UTF-8");
                response.sendRedirect("signup.html?verification_error=verification_failed&message=" + errorMessage);
            }
            
        } catch (Exception e) {
            System.err.println("이메일 인증 GET 처리 오류: " + e.getMessage());
            e.printStackTrace();
            
            try {
                response.sendRedirect("signup.html?verification_error=server_error");
            } catch (Exception ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
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
            
            // 토큰 파라미터 추출
            String token = request.getParameter("token");
            
            if (token == null || token.trim().isEmpty()) {
                sendErrorResponse(response, "인증 토큰을 입력해주세요.");
                return;
            }
            
            // 토큰 검증
            VerificationResult result = emailVerificationService.verifyEmail(token.trim());
            
            // 결과 응답
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("success", result.isSuccess());
            jsonResponse.put("message", result.getMessage());
            
            if (result.isSuccess()) {
                jsonResponse.put("email", result.getEmail());
                jsonResponse.put("verified", true);
            }
            
            sendJsonResponse(response, jsonResponse);
            
        } catch (Exception e) {
            System.err.println("이메일 인증 POST 처리 오류: " + e.getMessage());
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