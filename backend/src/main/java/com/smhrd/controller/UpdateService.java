package com.smhrd.controller;

import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson; // GSON 라이브러리 import
import com.smhrd.model.MemberDAO;
import com.smhrd.model.UserInfo;

@WebServlet("/UpdateService")
public class UpdateService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 1. 클라이언트가 보낸 JSON 데이터를 문자열로 읽어옵니다.
		String jsonData = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		
		// 2. GSON 라이브러리를 사용해 JSON 문자열을 UserInfo 객체로 변환합니다.
		Gson gson = new Gson();
		UserInfo updateMem = gson.fromJson(jsonData, UserInfo.class);
		
		// 3. 변환된 데이터로 로그를 출력하여 올바른 값이 들어왔는지 확인합니다.
		System.out.println("회원정보 수정 요청: " + updateMem.getEmail() + ", " + updateMem.getPw() + ", " + updateMem.getNickname());
		
		// 4. 회원정보 수정을 할 수 있는 MemberDAO의 update메서드 호출
		MemberDAO dao = new MemberDAO();
		int cnt = dao.update(updateMem);
		
		// 5. update메서드 호출 시의 리턴값에 따라 JSON 응답을 보냅니다.
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    
	    String jsonResponse;
	    
	    if(cnt > 0) {
	        System.out.println("정보 수정 성공");
	        HttpSession session = request.getSession();
	        session.setAttribute("loginMember", updateMem);

	        jsonResponse = "{\"success\": true, \"message\": \"회원정보 수정 성공!\"}";
	    } else {
	        System.out.println("정보 수정 실패");
	        jsonResponse = "{\"success\": false, \"message\": \"회원정보 수정 실패\"}";
	    }

	    response.getWriter().write(jsonResponse);
	}
}
