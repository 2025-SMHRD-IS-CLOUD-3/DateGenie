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

@WebServlet("/UpdateService")
public class UpdateService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// main.jsp -> update.jsp -> UpdateService.java
		// -> MemberDAO -> MemberMapper.xml
		// 1. update.jsp에서 받아오는 데이터 인코딩
		request.setCharacterEncoding("UTF-8");
		
		// 2. update.jsp에서 name값을 기준으로 데이터 받아오기
		String email = request.getParameter("email");
		String pw = request.getParameter("pw");
		String tel = request.getParameter("tel");
		String address = request.getParameter("address");
		
		// 3. MemberDAO로 데이터 전송 전, 데이터 하나로 묶어주기
		UserInfo updateMem = new UserInfo(email, pw, tel, address);
		// 4. 회원정보 수정을 할 수 있는 MemberDAO의 update메서드 호출
		MemberDAO dao = new MemberDAO();
		int cnt = dao.update(updateMem);
		// 5. update메서드 호출 시의 리턴값 판별
		if(cnt>0) {
			HttpSession session = request.getSession();
			session.setAttribute("sMember", updateMem);
			
			response.sendRedirect("main.jsp");
		}else {
			response.sendRedirect("main.jsp");
		}
	}

}
