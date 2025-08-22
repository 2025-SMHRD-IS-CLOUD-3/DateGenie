package com.smhrd.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.smhrd.service.EmailVerificationService;
import com.smhrd.service.EmailVerificationService.VerificationResult;
import com.smhrd.model.MemberDAO;
import com.smhrd.util.SecurityUtils;

import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;

@WebServlet("/VerifyEmailService")
public class VerifyEmailService extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        try {
            // CORS and basic settings
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type");
            
            // Handle OPTIONS request
            if ("OPTIONS".equals(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
            
            request.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            
            // Get parameters
            String email = request.getParameter("email");
            String verificationCode = request.getParameter("verificationCode");
            
            // Validate required parameters
            if (email == null || email.trim().isEmpty()) {
                sendErrorResponse(response, "이메일을 입력해주세요.");
                return;
            }
            
            if (verificationCode == null || verificationCode.trim().isEmpty()) {
                sendErrorResponse(response, "인증 코드를 입력해주세요.");
                return;
            }
            
            // Sanitize input
            email = SecurityUtils.sanitizeInput(email.trim());
            verificationCode = SecurityUtils.sanitizeInput(verificationCode.trim());
            
            // Validate email format
            if (!SecurityUtils.isEmailValid(email)) {
                sendErrorResponse(response, "올바른 이메일 형식을 입력해주세요.");
                return;
            }
            
            // Validate verification code format (6 digits)
            if (!verificationCode.matches("^[0-9]{6}$")) {
                sendErrorResponse(response, "인증 코드는 6자리 숫자여야 합니다.");
                return;
            }
            
            // Verify the email verification code
            System.out.println("=== DEBUG: 이메일 인증 시작 ===");
            System.out.println("Email: " + email);
            System.out.println("Verification Code: " + verificationCode);
            System.out.println("==============================");
            
            EmailVerificationService emailVerificationService = new EmailVerificationService();
            VerificationResult result = emailVerificationService.verifyEmailCode(email, verificationCode);
            
            System.out.println("=== DEBUG: 인증 결과 ===");
            System.out.println("Success: " + result.isSuccess());
            System.out.println("Message: " + result.getMessage());
            System.out.println("========================");
            
            Map<String, Object> jsonResponse = new HashMap<>();
            
            if (result.isSuccess()) {
                // Verification successful - update member status
                MemberDAO dao = new MemberDAO();
                boolean updateResult = dao.updateEmailVerificationStatus(email, true);
                
                if (updateResult) {
                    // Success
                    jsonResponse.put("success", true);
                    jsonResponse.put("message", "이메일 인증이 완료되었습니다!");
                    jsonResponse.put("redirectUrl", "/DateGenie/login.html");
                } else {
                    // Database update failed
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "인증 상태 업데이트에 실패했습니다. 다시 시도해주세요.");
                }
            } else {
                // Verification failed
                jsonResponse.put("success", false);
                jsonResponse.put("message", result.getMessage());
            }
            
            // Send response
            PrintWriter out = response.getWriter();
            Gson gson = new Gson();
            out.print(gson.toJson(jsonResponse));
            out.flush();
            
        } catch (Exception e) {
            System.err.println("=== VerifyEmailService 오류 ===");
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("============================");
            
            try {
                response.setContentType("application/json; charset=UTF-8");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "서버 오류가 발생했습니다. 다시 시도해주세요.");
                errorResponse.put("error", e.getMessage()); // 디버그용 추가
                
                PrintWriter out = response.getWriter();
                Gson gson = new Gson();
                out.print(gson.toJson(errorResponse));
                out.flush();
            } catch (Exception ex) {
                System.err.println("응답 전송 중 오류: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Send error response
     * @param response HttpServletResponse
     * @param message Error message to display
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        out.print(gson.toJson(errorResponse));
        out.flush();
    }
}