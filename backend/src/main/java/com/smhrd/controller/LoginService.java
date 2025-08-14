package com.smhrd.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.smhrd.model.MavenMember;
import com.smhrd.model.MemberDAO;

@WebServlet("/LoginService")
public class LoginService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// 1. main.jsp에서 데이터를 받기 전, 인코딩 작업
		request.setCharacterEncoding("UTF-8");
		// 2. main.jsp의 form태그에서 name값을 기준으로 데이터 가져오기
		String email = request.getParameter("email");
		String pw = request.getParameter("pw");
		
		// 3. DB에 접근할 수 있는 DAO로 데이터 보내기 전, 데이터 처리 작업
		//	-->
		MavenMember loginMember = new MavenMember(email,pw);
		// 4. DAO의 login메서드 호출 & 데이터 전송하기
		MemberDAO dao = new MemberDAO();
		// login 기능 실행
		// -> 현재 입력된 email, pw와 DB에 있는 email,pw가 동일한지 비교 필요
		MavenMember sMember = dao.login(loginMember);
		// 5. 결과 값 처리하기
		if(sMember != null) {
			// sMember가 null값이 아니다 == DB에서 일치하는 값을 가져왔다.
			// 성공 --> session영역에 sMember를 저장!
			// jsp에서는 내장 객체 session이 지원되지만 servlet에서는 session을 선언해줘야 한다.
			HttpSession session = request.getSession();
			session.setAttribute("sMember", sMember);
		}
			// sMember == null 실패 --> redirect 방식으로 main.jsp 이동
			response.sendRedirect("main.jsp");
		
		
		
		
		
		
	}

}
