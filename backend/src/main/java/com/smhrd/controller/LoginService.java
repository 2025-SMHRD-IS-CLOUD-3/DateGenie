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

System.out.println("===== LoginService 호출됨 =====");
        
        try {
            // JSON 요청/응답 설정
            System.out.println("=== 1단계: 응답 설정 시작 ===");
            request.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            System.out.println("응답 설정 완료");
            
            // JSON 데이터 읽기
            System.out.println("=== 2단계: JSON 데이터 읽기 시작 ===");
            BufferedReader reader = request.getReader();
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            System.out.println("받은 JSON 데이터: " + jsonString.toString());
            
            // JSON 파싱
            System.out.println("=== 3단계: JSON 파싱 시작 ===");
            Gson gson = new Gson();
            Map<String, String> loginData = gson.fromJson(jsonString.toString(), Map.class);
            System.out.println("JSON 파싱 완료");
            
            String email = loginData.get("email");
            String pw = loginData.get("password");
            
            System.out.println("파싱된 이메일: " + email);
            System.out.println("파싱된 비밀번호: " + pw);
            
            // UserInfo 객체 생성
            System.out.println("=== 4단계: UserInfo 객체 생성 ===");
            UserInfo loginMember = new UserInfo(email, pw);
            System.out.println("UserInfo 객체 생성 완료: " + loginMember);
            
            // DAO 생성 및 로그인 검증
            System.out.println("=== 5단계: DB 로그인 검증 시작 ===");
            MemberDAO dao = new MemberDAO();
            System.out.println("MemberDAO 객체 생성 완료");
            
            UserInfo result = dao.login(loginMember);
            System.out.println("dao.login() 실행 완료");
            System.out.println("로그인 결과: " + result);
            
            // 응답 데이터 생성
            System.out.println("=== 6단계: 응답 데이터 생성 ===");
            Map<String, Object> responseData = new HashMap<>();
            
            if (result != null) {
                System.out.println("=== 로그인 성공 처리 ===");
                
                // 세션 설정
                HttpSession session = request.getSession();
                session.setAttribute("loginMember", result);
                System.out.println("세션 설정 완료");
                
                responseData.put("success", true);
                responseData.put("message", "로그인에 성공했습니다!");
                responseData.put("userInfo", result);
                responseData.put("redirectUrl", "main.html");
                
                System.out.println("성공 응답 데이터 생성 완료");
                System.out.println("로그인 성공 사용자: " + result.getEmail());
                
            } else {
                System.out.println("=== 로그인 실패 처리 ===");
                
                responseData.put("success", false);
                responseData.put("message", "이메일 또는 비밀번호가 일치하지 않습니다.");
                
                System.out.println("실패 응답 데이터 생성 완료");
            }
            
            // JSON 응답 전송
            System.out.println("=== 7단계: JSON 응답 전송 ===");
            PrintWriter out = response.getWriter();
            String jsonResponse = gson.toJson(responseData);
            System.out.println("전송할 JSON: " + jsonResponse);
            
            out.print(jsonResponse);
            out.flush();
            System.out.println("JSON 응답 전송 완료");
            
        } catch (Exception e) {
            System.out.println("=== ❌ 예외 발생 ===");
            System.out.println("예외 위치: LoginService");
            System.out.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
            
            try {
                // 오류 응답
                System.out.println("=== 오류 응답 생성 ===");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인 처리 중 오류가 발생했습니다.");
                
                response.setContentType("application/json; charset=UTF-8");
                PrintWriter out = response.getWriter();
                Gson gson = new Gson();
                out.print(gson.toJson(errorResponse));
                out.flush();
                System.out.println("오류 응답 전송 완료");
                
            } catch (Exception ex) {
                System.out.println("오류 응답 전송 중 추가 예외 발생: " + ex.getMessage());
            }
        }
        
        System.out.println("===== LoginService 종료 =====");	
		
		
		
		
		
		
	}

}
