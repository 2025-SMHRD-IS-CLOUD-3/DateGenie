package com.smhrd.controller;

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

@WebServlet("/DeleteService")
public class DeleteService extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("===== DeleteService 호출됨 =====");

		// JSON 응답을 위한 설정
		response.setContentType("application/json; charset=UTF-8");
		PrintWriter out = response.getWriter();
		Gson gson = new Gson();
		Map<String, Object> responseData = new HashMap<>();

		try {
			// 클라이언트가 보낸 JSON 데이터를 읽어옵니다.
			StringBuilder sb = new StringBuilder();
			try (java.io.BufferedReader reader = request.getReader()) {
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			}

			String requestBody = sb.toString();
			String userEmail = null; // 이메일을 저장할 변수

			if (!requestBody.isEmpty()) {
				// JSON을 파싱하여 이메일 데이터를 추출합니다.
				Map<String, String> data = gson.fromJson(requestBody, new HashMap<String, String>().getClass());
				userEmail = data.get("email");
			}
			
			// 이메일이 정상적으로 추출되었는지 확인합니다.
			if (userEmail == null || userEmail.isEmpty()) {
				responseData.put("success", false);
				responseData.put("message", "사용자 인증에 실패했습니다.");
				out.print(gson.toJson(responseData));
				out.flush();
				return;
			}
			
			// MemberDAO를 사용하여 DB에서 회원 삭제
			// 클라이언트가 보낸 body 데이터에서 추출한 이메일을 사용합니다.
			MemberDAO dao = new MemberDAO();
			int cnt = dao.delete(userEmail);
			
			if (cnt > 0) {
				// 삭제 성공
				System.out.println(userEmail + " 계정 삭제 성공");
				responseData.put("success", true);
				responseData.put("message", "계정이 성공적으로 삭제되었습니다.");
				
				// 세션 무효화
				HttpSession session = request.getSession(false);
				if (session != null) {
					session.invalidate();
				}
				
			} else {
				// 삭제 실패 (해당 이메일의 계정 없음)
				System.out.println(userEmail + " 계정 삭제 실패");
				responseData.put("success", false);
				responseData.put("message", "계정 삭제에 실패했습니다. 사용자를 찾을 수 없습니다.");
			}

		} catch (Exception e) {
			System.out.println("=== ❌ 예외 발생 ===");
			e.printStackTrace();
			responseData.put("success", false);
			responseData.put("message", "서버 오류가 발생했습니다. 다시 시도해주세요.");
		}
		
		out.print(gson.toJson(responseData));
		out.flush();
	}
}