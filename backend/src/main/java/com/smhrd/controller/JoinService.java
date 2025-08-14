package com.smhrd.controller;


import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.smhrd.model.UserInfo;
import com.smhrd.model.MemberDAO;

@WebServlet("/JoinService")
public class JoinService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 1. Join.html을 통해서 회원가입 정보를 받는다.
		// 2. form 태그를 통해서 JoinService로 데이터들을 전송한다.
		// 3. join.html에서 입력 받은 데이터들을 받아온다!
		//	*post방식으로 보낸 데이터들을 받아오면 된다!
		// 		-> 데이터 받을때마다 인코딩 필수
		request.setCharacterEncoding("UTF-8");
		
		String email = request.getParameter("email");
		String pw = request.getParameter("pw");
		String nickname = request.getParameter("nickname");
		// 4. 받아온 데이터를 DB에 저장하는 작업
		//  - 4개의 데이터를 하나로 묶어주기(MavenMember)
		UserInfo joinMember = new UserInfo(email, pw, nickname);
		// 5. DB 연결할 수 있도록 MemberDAO의 join메서드 호출
		//	->join메서드를 사용하기 위해서 MemberDAO 객체 생성
		MemberDAO dao = new MemberDAO();
		int cnt = dao.join(joinMember);
		// 6. 결과값 처리
		if(cnt>0) {
			//insert구문 실행시, 영향 받은 행의 개수 >0
			// -> 성공
			// 회원가입 성공 -> 회원가입한 email 데이터를 가지고 페이지 이동
			request.setAttribute("email", email);
			// forward 방식으로 이동
			RequestDispatcher rd = request.getRequestDispatcher("join_success.jsp");
			rd.forward(request, response);
//			response.sendRedirect("join_success.jsp");
		}else {
			// 영향 받은 행의 개수 = 0, <0
			response.sendRedirect("main.jsp");
		}
	}

}
