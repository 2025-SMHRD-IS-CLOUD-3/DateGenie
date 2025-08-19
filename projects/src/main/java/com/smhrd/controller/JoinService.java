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
        
        System.out.println("===== JoinService called =====");
        
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
                System.out.println("Processing multipart/form-data");
                
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
                    System.out.println("Multipart parsing error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // Handle application/x-www-form-urlencoded
                System.out.println("Processing application/x-www-form-urlencoded");
                email = request.getParameter("email");
                password = request.getParameter("pw");
                nickname = request.getParameter("nickname");
            }
            
            // 디버깅 로그 추가
            System.out.println("=== 파라미터 수신 디버깅 ===");
            System.out.println("Request Content-Type: " + request.getContentType());
            System.out.println("Request Method: " + request.getMethod());
            System.out.println("Email: [" + email + "]");
            System.out.println("Password: [" + (password != null ? "***(" + password.length() + "자)" : "null") + "]");
            System.out.println("Nickname: [" + nickname + "]");
            System.out.println("===========================");
            
            System.out.println("Signup attempt - Email: " + email + ", Nickname: " + nickname);
            
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
            MemberDAO dao = new MemberDAO();
                
                // Check if email already exists
                UserInfo existingUser = dao.checkEmailExists(email);
                
                if (existingUser != null) {
                    // Email already exists
                    System.out.println("Signup failed: email already exists - " + email);
                    
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "이미 사용 중인 이메일입니다.");
                    
                    PrintWriter out = response.getWriter();
                    Gson gson = new Gson();
                    out.print(gson.toJson(errorResponse));
                    out.flush();
                    
                } else {
                    // Email is available, proceed with registration
                    UserInfo newMember = new UserInfo(email, password, nickname);
                    int result = dao.join(newMember);
                    
                    if (result > 0) {
                        // Signup success
                        HttpSession session = request.getSession();
                        session.setAttribute("email", email);
                        
                        System.out.println("Signup success: " + email);
                        
                        // 환경별 리다이렉션 URL 설정
                        String baseURL = request.getRequestURL().toString();
                        String redirectUrl;
                        String referer = request.getHeader("Referer");
                        
                        if (baseURL.contains("localhost") || baseURL.contains("127.0.0.1")) {
                            // 로컬 환경 - Live Server (포트 5500)
                            redirectUrl = "http://localhost:5500/projects/login.html";
                        } else if (referer != null && referer.contains("github.io")) {
                            // GitHub Pages 환경 - webapp 폴더 경로
                            String githubPagesBase = referer.substring(0, referer.lastIndexOf("/") + 1);
                            redirectUrl = githubPagesBase + "projects/src/main/webapp/login.html";
                        } else {
                            // 기타 프로덕션 환경
                            redirectUrl = "login.html";
                        }
                        
                        Map<String, Object> successResponse = new HashMap<>();
                        successResponse.put("success", true);
                        successResponse.put("message", "회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.");
                        successResponse.put("redirectUrl", redirectUrl);
                        
                        PrintWriter out = response.getWriter();
                        Gson gson = new Gson();
                        out.print(gson.toJson(successResponse));
                        out.flush();
                        
                    } else {
                        // DB insert failed
                        System.out.println("Signup failed: DB insert failed for " + email);
                        
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
            System.out.println("JoinService error: " + e.getMessage());
            e.printStackTrace();
            
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
                System.out.println("Error response 전송 중 추가 오류: " + ex.getMessage());
            }
        }
        
        System.out.println("===== JoinService finished =====");
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