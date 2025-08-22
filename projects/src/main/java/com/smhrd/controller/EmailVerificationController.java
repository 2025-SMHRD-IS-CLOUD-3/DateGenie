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
 * 이메일 인증 메일 발송/재발송을 처리하는 컨트롤러
 * POST /EmailVerificationController - 인증 메일 발송
 * POST /EmailVerificationController?action=resend - 인증 메일 재발송
 */
@WebServlet("/EmailVerificationController")
public class EmailVerificationController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final EmailVerificationService emailVerificationService;
    
    public EmailVerificationController() {
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
            
            // 액션 타입 확인 (기본 발송 또는 재발송)
            String action = request.getParameter("action");
            boolean isResend = "resend".equals(action);
            
            // 요청 파라미터 추출
            String email = request.getParameter("email");
            
            // 입력 검증
            if (email == null || email.trim().isEmpty()) {
                sendErrorResponse(response, "이메일 주소를 입력해주세요.");
                return;
            }
            
            // 입력 정리 및 검증
            email = SecurityUtils.sanitizeInput(email.trim());
            
            if (!SecurityUtils.isEmailValid(email)) {
                sendErrorResponse(response, "올바른 이메일 형식을 입력해주세요.");
                return;
            }
            
            // 베이스 URL 생성
            String baseUrl = getBaseUrl(request);
            
            // 이메일 인증 서비스 호출
            VerificationResult result;
            if (isResend) {
                result = emailVerificationService.resendVerification(email, baseUrl);
            } else {
                result = emailVerificationService.createAndSendVerification(email, baseUrl);
            }
            
            // 결과 응답
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("success", result.isSuccess());
            jsonResponse.put("message", result.getMessage());
            
            if (result.isSuccess()) {
                jsonResponse.put("email", email);
                jsonResponse.put("action", isResend ? "resent" : "sent");
            }
            
            sendJsonResponse(response, jsonResponse);
            
        } catch (Exception e) {
            System.err.println("이메일 인증 컨트롤러 오류: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, "서버 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // GET 요청으로 상태 확인 또는 시스템 정보 제공
        String action = request.getParameter("action");
        
        try {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setContentType("application/json; charset=UTF-8");
            
            Map<String, Object> jsonResponse = new HashMap<>();
            
            if ("status".equals(action)) {
                // 시스템 상태 확인
                String systemStatus = emailVerificationService.getSystemStatus();
                jsonResponse.put("success", true);
                jsonResponse.put("status", systemStatus);
                
            } else if ("check".equals(action)) {
                // 특정 이메일의 인증 상태 확인
                String email = request.getParameter("email");
                
                if (email != null && !email.trim().isEmpty()) {
                    email = SecurityUtils.sanitizeInput(email.trim());
                    
                    if (SecurityUtils.isEmailValid(email)) {
                        boolean isVerified = emailVerificationService.isEmailVerified(email);
                        jsonResponse.put("success", true);
                        jsonResponse.put("email", email);
                        jsonResponse.put("verified", isVerified);
                    } else {
                        jsonResponse.put("success", false);
                        jsonResponse.put("message", "올바른 이메일 형식을 입력해주세요.");
                    }
                } else {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "이메일 주소를 입력해주세요.");
                }
                
            } else {
                // 기본 정보 제공
                jsonResponse.put("success", true);
                jsonResponse.put("service", "EmailVerificationController");
                jsonResponse.put("version", "1.0");
                jsonResponse.put("methods", new String[]{"POST - 인증 메일 발송", "GET?action=status - 시스템 상태", "GET?action=check&email=... - 인증 상태 확인"});
            }
            
            sendJsonResponse(response, jsonResponse);
            
        } catch (Exception e) {
            System.err.println("이메일 인증 상태 확인 오류: " + e.getMessage());
            sendErrorResponse(response, "상태 확인 중 오류가 발생했습니다.");
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
     * 요청에서 베이스 URL 추출
     * @param request HTTP 요청
     * @return 베이스 URL (예: http://localhost:8081/DateGenie)
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);
        
        // 기본 포트가 아닌 경우에만 포트 추가
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }
        
        baseUrl.append(contextPath);
        
        return baseUrl.toString();
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