package com.smhrd.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.smhrd.model.MemberDAO;
import com.smhrd.model.UserInfo;
import com.smhrd.util.SecurityUtils;
import com.smhrd.service.EmailVerificationService;
import com.smhrd.service.EmailVerificationService.VerificationResult;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;

@WebServlet("/JoinService")
public class JoinService extends HttpServlet {
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
            
            // Get form parameters - handle both URLencoded and multipart data
            String email = null;
            String password = null; 
            String nickname = null;
            
            String contentType = request.getContentType();
            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                // Handle multipart/form-data
                DiskFileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setHeaderEncoding("UTF-8");
                
                try {
                    List<FileItem> formItems = upload.parseRequest(request);
                    for (FileItem item : formItems) {
                        if (item.isFormField()) {
                            String fieldName = item.getFieldName();
                            String fieldValue = item.getString("UTF-8");
                            
                            switch (fieldName) {
                                case "email":
                                    email = fieldValue;
                                    break;
                                case "pw":
                                    password = fieldValue;
                                    break;
                                case "nickname":
                                    nickname = fieldValue;
                                    break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Silent handling - avoid logging
                }
            } else {
                // Handle application/x-www-form-urlencoded
                email = request.getParameter("email");
                password = request.getParameter("pw");
                nickname = request.getParameter("nickname");
            }
            
            // Comprehensive server-side validation
            if (email == null || password == null || nickname == null) {
                sendErrorResponse(response, "모든 필수 항목을 입력해주세요.");
                return;
            }
            
            // Sanitize input
            email = SecurityUtils.sanitizeInput(email.trim());
            nickname = SecurityUtils.sanitizeInput(nickname.trim());
            
            // Validate email format
            if (!SecurityUtils.isEmailValid(email)) {
                sendErrorResponse(response, "올바른 이메일 형식을 입력해주세요.");
                return;
            }
            
            // Validate nickname format  
            if (!SecurityUtils.isNicknameValid(nickname)) {
                sendErrorResponse(response, "닉네임은 2~20자, 한글/영문/숫자/언더스코어만 가능합니다.");
                return;
            }
            
            // Validate password strength
            if (!SecurityUtils.isPasswordValid(password)) {
                sendErrorResponse(response, "비밀번호는 8~64자, 대/소문자·숫자·특수문자 중 3종 이상 포함해야 합니다.");
                return;
            }
            
            // All validations passed, proceed with registration
            System.out.println("=== DEBUG: Starting registration process ===");
            System.out.println("Email: " + email);
            System.out.println("Nickname: " + nickname);
            System.out.println("Password length: " + password.length());
            
            MemberDAO dao = new MemberDAO();
                
                // Check if email already exists
                System.out.println("=== DEBUG: Checking if email exists ===");
                UserInfo existingUser = dao.checkEmailExists(email);
                System.out.println("Existing user result: " + (existingUser != null ? "EXISTS" : "NOT_EXISTS"));
                
                if (existingUser != null) {
                    // Email already exists
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "이미 사용 중인 이메일입니다.");
                    
                    PrintWriter out = response.getWriter();
                    Gson gson = new Gson();
                    out.print(gson.toJson(errorResponse));
                    out.flush();
                    
                } else {
                    // Email is available, proceed with registration
                    System.out.println("=== DEBUG: Creating new member ===");
                    UserInfo newMember = new UserInfo(email, password, nickname);
                    System.out.println("Member created, attempting database insert...");
                    
                    int result = 0;
                    try {
                        result = dao.join(newMember);
                        System.out.println("=== DEBUG: Database insert result: " + result + " ===");
                    } catch (Exception dbEx) {
                        System.err.println("=== Database Insert Error ===");
                        System.err.println("Error: " + dbEx.getMessage());
                        dbEx.printStackTrace();
                        System.err.println("============================");
                        
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
                        errorResponse.put("debug", "Database error: " + dbEx.getMessage());
                        
                        PrintWriter out = response.getWriter();
                        Gson gson = new Gson();
                        out.print(gson.toJson(errorResponse));
                        out.flush();
                        return;
                    }
                    
                    if (result > 0) {
                        // Signup success - 회원가입 성공 후 이메일 인증 메일 발송
                        HttpSession session = request.getSession();
                        session.setAttribute("email", email);
                        
                        // 베이스 URL 생성
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
                        
                        // 이메일 인증 서비스 호출
                        System.out.println("=== DEBUG: Starting email verification ===");
                        System.out.println("Email: " + email);
                        System.out.println("Base URL: " + baseUrl.toString());
                        
                        EmailVerificationService emailVerificationService = new EmailVerificationService();
                        VerificationResult emailResult = null;
                        
                        try {
                            emailResult = emailVerificationService.createAndSendVerification(email, baseUrl.toString());
                            System.out.println("Email verification result: " + (emailResult != null ? emailResult.isSuccess() : "null"));
                            if (emailResult != null) {
                                System.out.println("Email result message: " + emailResult.getMessage());
                            }
                        } catch (Exception emailEx) {
                            System.err.println("=== Email Verification Error ===");
                            System.err.println("Error: " + emailEx.getMessage());
                            emailEx.printStackTrace();
                            System.err.println("================================");
                            emailResult = new VerificationResult(false, "Email service error: " + emailEx.getMessage(), null);
                        }
                        
                        Map<String, Object> successResponse = new HashMap<>();
                        successResponse.put("success", true);
                        
                        if (emailResult.isSuccess()) {
                            // 이메일 인증 메일 발송 성공
                            successResponse.put("message", "회원가입이 완료되었습니다! 이메일 인증을 위해 " + email + "로 발송된 메일을 확인해주세요.");
                            successResponse.put("emailSent", true);
                        } else {
                            // 이메일 발송 실패 시에도 회원가입은 완료된 상태이므로 성공 처리
                            successResponse.put("message", "회원가입이 완료되었습니다! 이메일 인증 메일 발송에 실패했지만, 이메일 인증 섹션에서 재발송할 수 있습니다.");
                            successResponse.put("emailSent", false);
                            successResponse.put("emailError", emailResult.getMessage());
                        }
                        
                        PrintWriter out = response.getWriter();
                        Gson gson = new Gson();
                        out.print(gson.toJson(successResponse));
                        out.flush();
                        
                    } else {
                        // DB insert failed
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
                        
                        PrintWriter out = response.getWriter();
                        Gson gson = new Gson();
                        out.print(gson.toJson(errorResponse));
                        out.flush();
                    }
                }
            
        } catch (Exception e) {
            // Log the actual error for debugging
            System.err.println("=== JoinService Error ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================");
            
            try {
                response.setContentType("application/json; charset=UTF-8");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "서버 오류가 발생했습니다. 다시 시도해주세요.");
                errorResponse.put("debug", e.getMessage()); // Add debug info
                
                PrintWriter out = response.getWriter();
                Gson gson = new Gson();
                out.print(gson.toJson(errorResponse));
                out.flush();
            } catch (Exception ex) {
                // Silent handling - avoid additional logging
            }
        }
    }
    
    /**
     * Send error response with alert and history back
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