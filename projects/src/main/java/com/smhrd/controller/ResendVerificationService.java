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
import com.smhrd.model.UserInfo;
import com.smhrd.util.SecurityUtils;

import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;

@WebServlet("/ResendVerificationService")
public class ResendVerificationService extends HttpServlet {
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
            
            // Validate required parameters
            if (email == null || email.trim().isEmpty()) {
                sendErrorResponse(response, "이메일을 입력해주세요.");
                return;
            }
            
            // Sanitize input
            email = SecurityUtils.sanitizeInput(email.trim());
            
            // Validate email format
            if (!SecurityUtils.isEmailValid(email)) {
                sendErrorResponse(response, "올바른 이메일 형식을 입력해주세요.");
                return;
            }
            
            // Check if email exists in database
            MemberDAO dao = new MemberDAO();
            UserInfo user = dao.checkEmailExists(email);
            
            if (user == null) {
                sendErrorResponse(response, "등록되지 않은 이메일입니다.");
                return;
            }
            
            // Check if already verified
            if (user.isEmailVerified()) {
                sendErrorResponse(response, "이미 인증된 이메일입니다.");
                return;
            }
            
            // Generate base URL for email links
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            
            StringBuilder baseUrl = new StringBuilder();
            baseUrl.append(scheme).append("://").append(serverName);
            if ((scheme.equals("http") && serverPort != 80) || 
                (scheme.equals("https") && serverPort != 443)) {
                baseUrl.append(":").append(serverPort);
            }
            baseUrl.append(contextPath);
            
            // Resend verification email
            EmailVerificationService emailVerificationService = new EmailVerificationService();
            VerificationResult result = emailVerificationService.resendVerificationEmail(email, baseUrl.toString());
            
            Map<String, Object> jsonResponse = new HashMap<>();
            
            if (result.isSuccess()) {
                // Resend successful
                jsonResponse.put("success", true);
                jsonResponse.put("message", "인증 이메일을 다시 발송했습니다. 메일함을 확인해주세요.");
            } else {
                // Resend failed
                jsonResponse.put("success", false);
                jsonResponse.put("message", result.getMessage());
            }
            
            // Send response
            PrintWriter out = response.getWriter();
            Gson gson = new Gson();
            out.print(gson.toJson(jsonResponse));
            out.flush();
            
        } catch (Exception e) {
            try {
                response.setContentType("application/json; charset=UTF-8");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "서버 오류가 발생했습니다. 다시 시도해주세요.");
                
                PrintWriter out = response.getWriter();
                Gson gson = new Gson();
                out.print(gson.toJson(errorResponse));
                out.flush();
            } catch (Exception ex) {
                // Silent handling
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