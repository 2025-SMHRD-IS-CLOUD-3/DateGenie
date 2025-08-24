package com.smhrd.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.smhrd.model.MemberDAO;
import com.smhrd.model.UserInfo;

@WebServlet("/LoginService")
public class LoginService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

try {
            // JSON 요청/응답 설정
            request.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            
            // JSON 데이터 읽기
            BufferedReader reader = request.getReader();
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            
            // JSON 파싱
            Gson gson = new Gson();
            Map<String, String> loginData = gson.fromJson(jsonString.toString(), Map.class);
            
            String email = loginData.get("email");
            String pw = loginData.get("password");
            
            // UserInfo 객체 생성 (로그인 검증용)
            UserInfo loginMember = new UserInfo(email, pw);
            
            // DAO 생성 및 로그인 검증
            MemberDAO dao = new MemberDAO();
            UserInfo result = dao.login(loginMember);
            
            // 응답 데이터 생성
            Map<String, Object> responseData = new HashMap<>();
            
            if (result != null) {
                // 이메일 인증 상태 확인 - 필수 검증
                if (!result.isEmailVerified()) {
                    // 이메일 미인증 사용자 - 로그인 차단
                    responseData.put("success", false);
                    responseData.put("message", "이메일 인증을 완료한 후 로그인해주세요. 이메일을 확인하거나 인증 메일을 재발송해주세요.");
                    responseData.put("requiresVerification", true);
                    responseData.put("email", result.getEmail());
                    responseData.put("canResend", true);
                } else {
                    // 이메일 인증 완료 사용자 - 로그인 허용
                    HttpSession session = request.getSession();
                    session.setAttribute("loginMember", result);
                    
                    responseData.put("success", true);
                    responseData.put("message", "로그인에 성공했습니다!");
                    responseData.put("userInfo", result);
                    
                    // 환경별 리다이렉션 URL 설정
                    String baseURL = request.getRequestURL().toString();
                    String referer = request.getHeader("Referer");
                    String redirectUrl;
                    
                    // 환경별 리다이렉트 URL 설정
                    
                    // 서비스가 실행 중인 환경에 따라 결정 (referer가 아닌 실제 서버 위치 기준)
                    // 모든 환경에서 localhost:8081/DateGenie로 리다이렉트
                    redirectUrl = "http://localhost:8081/DateGenie/upload.html";
                    System.out.println("=== 로그인 성공 디버깅 ===");
                    System.out.println("로그인 결과: " + result);
                    System.out.println("세션 설정 사용자 ID: " + (result != null ? result.getEmail() : "null"));
                    System.out.println("최종 결정된 redirectUrl: " + redirectUrl);
                    responseData.put("redirectUrl", redirectUrl);
                }
                
            } else {
                responseData.put("success", false);
                responseData.put("message", "이메일 또는 비밀번호가 일치하지 않습니다.");
            }
            
            // JSON 응답 전송
            PrintWriter out = response.getWriter();
            String jsonResponse = gson.toJson(responseData);
            
            out.print(jsonResponse);
            out.flush();
            
        } catch (Exception e) {
            try {
                // 오류 응답
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인 처리 중 오류가 발생했습니다.");
                
                response.setContentType("application/json; charset=UTF-8");
                PrintWriter out = response.getWriter();
                Gson gson = new Gson();
                out.print(gson.toJson(errorResponse));
                out.flush();
                
            } catch (Exception ex) {
                // Silent handling - avoid additional logging
            }
        }	
		
		
		
		
		
		
	}

}
