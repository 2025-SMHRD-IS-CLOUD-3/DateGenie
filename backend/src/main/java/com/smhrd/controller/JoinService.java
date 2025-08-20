package com.smhrd.controller;


import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.smhrd.model.UserInfo;
import com.smhrd.model.EmailDAO;
import com.smhrd.model.MemberDAO;

@WebServlet("/JoinService")
public class JoinService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("=====joinservice 호출됨=====");
		
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		
		String email = request.getParameter("email");
		String pw = request.getParameter("pw");
		String checkPW = request.getParameter("checkPW");
		String nickname = request.getParameter("nickname");
		
		// 비밀번호 일치 여부 확인(서버용)
		if (!pw.equals(checkPW)) {
			// 비밀번호가 일치하지 않을 경우
			 response.setContentType("text/html; charset=UTF-8");
			    response.getWriter().println("<script>alert('잘못된 요청입니다.'); history.back();</script>");
			    return;
		}
		
		// 이메일 인증 확인 (추가된 부분)
		EmailDAO emailDAO = new EmailDAO();
		boolean isEmailVerified = emailDAO.isEmailVerified(email);
		System.out.println("이메일 인증 상태 확인: " + isEmailVerified);
		
		if (!isEmailVerified) {
			System.out.println("ERROR: 이메일 인증 미완료");
			response.getWriter().println("<script>alert('이메일 인증을 완료해주세요. 이메일을 확인하시거나 인증메일을 다시 발송해주세요.'); history.back();</script>");
			return;
		}
		
		System.out.println("SUCCESS: 이메일 인증 완료됨. 회원가입 진행");
		
		
		UserInfo joinMember = new UserInfo(email, pw, nickname);
		
		
		try {
		    System.out.println("=== DB 저장 시작 ===");
		    MemberDAO dao = new MemberDAO();
		    int cnt = dao.join(joinMember);
		    System.out.println("dao.join() 결과: " + cnt);
		    
		    if(cnt > 0) {
		        System.out.println("=== 성공 처리 시작 ===");
		        
		        // 세션 처리
		        HttpSession session = request.getSession();
		        session.setAttribute("email", email);
		        System.out.println("세션 설정 완료");
		        
		        // 응답 처리
		        response.setContentType("text/html; charset=UTF-8");
		        response.setCharacterEncoding("UTF-8");
		        System.out.println("응답 설정 완료");
		        
		        response.sendRedirect("login.html");
		        System.out.println("응답 전송 완료");
		        
		    } else {
		        System.out.println("DB 저장 실패: cnt = " + cnt);
		    }
		    
		} catch (Exception e) {
		    System.out.println("=== 예외 발생 위치 확인 ===");
		    System.out.println("예외 메시지: " + e.getMessage());
		    e.printStackTrace();
		}
		
		
	}

}
