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
            response.setContentType("text/html; charset=UTF-8");
            
            // Get form parameters
            String email = request.getParameter("email");
            String password = request.getParameter("pw");
            String nickname = request.getParameter("nickname");
            
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
            if (true) {
                
                MemberDAO dao = new MemberDAO();
                
                // Check if email already exists
                UserInfo existingUser = dao.checkEmailExists(email);
                
                if (existingUser != null) {
                    // Email already exists
                    System.out.println("Signup failed: email already exists - " + email);
                    
                    PrintWriter out = response.getWriter();
                    out.println("<script>");
                    out.println("alert('이미 사용 중인 이메일입니다.');");
                    out.println("history.back();");
                    out.println("</script>");
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
                        
                        PrintWriter out = response.getWriter();
                        out.println("<script>");
                        out.println("alert('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.');");
                        out.println("location.href='login.html';");
                        out.println("</script>");
                        out.flush();
                        
                    } else {
                        // DB insert failed
                        System.out.println("Signup failed: DB insert failed for " + email);
                        
                        PrintWriter out = response.getWriter();
                        out.println("<script>");
                        out.println("alert('회원가입 중 오류가 발생했습니다. 다시 시도해주세요.');");
                        out.println("history.back();");
                        out.println("</script>");
                        out.flush();
                    }
                }
            
        } catch (Exception e) {
            System.out.println("JoinService error: " + e.getMessage());
            e.printStackTrace();
            
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>");
            out.println("alert('Server error occurred. Please try again.');");
            out.println("history.back();");
            out.println("</script>");
            out.flush();
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
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script>");
        out.println("alert('" + message + "');");
        out.println("history.back();");
        out.println("</script>");
        out.flush();
    }
}