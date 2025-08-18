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
		
		
		UserInfo joinMember = new UserInfo(email, pw, nickname);
		
		
		// 5. DB 연결할 수 있도록 MemberDAO의 join메서드 호출
		//	->join메서드를 사용하기 위해서 MemberDAO 객체 생성
//		try {
//			MemberDAO dao = new MemberDAO();
//			int cnt = dao.join(joinMember);
//			// 6. 결과값 처리
//			if(cnt > 0) {
//				// 회원가입 성공 시, 세션에 이메일 정보 저장
//				HttpSession session = request.getSession();
//				session.setAttribute("email", email);
//				
//				// 로그인 페이지로 리다이렉트
//				response.getWriter().println("<script>alert('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.'); location.href='login.html';</script>");
//			} else {
//				// 회원가입 실패 시
//				response.getWriter().println("<script>alert('회원가입에 실패하였습니다. 다시 시도해주세요.'); history.back();</script>");
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("회원가입 오류발생!");
//		}
		
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
		        
		        response.getWriter().println("<script>alert('회원가입이 성공했습니다. 로그인창으로 이동합니다.'); location.href='login.html';</script>");
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
