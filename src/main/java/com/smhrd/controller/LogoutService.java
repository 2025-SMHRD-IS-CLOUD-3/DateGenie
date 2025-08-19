package com.smhrd.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl;

@WebServlet("/LogoutService")
public class LogoutService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// 로그아웃 : 세션의 데이터 모두 지우기
		// 1. session 꺼내오기
		HttpSession session = request.getSession();
		// 2. session 처리하기 => 세션을 무효화 시킨다.
		session.invalidate();
		// 3. redirect방식으로 main.jsp로 이동
		response.sendRedirect("main.jsp");
	}

}
