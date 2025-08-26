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
import com.google.gson.JsonObject;

/**
 * 계정 삭제 처리 서블릿
 * 사용자의 모든 데이터를 영구 삭제
 */
@WebServlet("/DeleteAccountService")
public class DeleteAccountService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=UTF-8");
		
		PrintWriter out = response.getWriter();
		JsonObject jsonResponse = new JsonObject();
		
		try {
			// 세션에서 사용자 정보 확인
			HttpSession session = request.getSession();
			UserInfo loginUser = (UserInfo) session.getAttribute("user");
			
			if (loginUser == null) {
				jsonResponse.addProperty("success", false);
				jsonResponse.addProperty("message", "로그인이 필요합니다.");
				out.print(jsonResponse.toString());
				return;
			}
			
			// 확인을 위한 이메일 검증
			String confirmEmail = request.getParameter("confirmEmail");
			if (confirmEmail == null || !confirmEmail.equals(loginUser.getEmail())) {
				jsonResponse.addProperty("success", false);
				jsonResponse.addProperty("message", "이메일 확인이 일치하지 않습니다.");
				out.print(jsonResponse.toString());
				return;
			}
			
			// 계정 삭제 실행
			MemberDAO memberDAO = new MemberDAO();
			boolean deleted = memberDAO.deleteAccount(loginUser.getEmail());
			
			if (deleted) {
				// 세션 무효화
				session.invalidate();
				
				jsonResponse.addProperty("success", true);
				jsonResponse.addProperty("message", "계정이 완전히 삭제되었습니다.");
				
				System.out.println("계정 삭제 완료: " + loginUser.getEmail());
				
			} else {
				jsonResponse.addProperty("success", false);
				jsonResponse.addProperty("message", "계정 삭제 중 오류가 발생했습니다. 다시 시도해주세요.");
			}
			
		} catch (Exception e) {
			System.err.println("DeleteAccountService 오류: " + e.getMessage());
			e.printStackTrace();
			
			jsonResponse.addProperty("success", false);
			jsonResponse.addProperty("message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		}
		
		out.print(jsonResponse.toString());
		out.flush();
	}
}