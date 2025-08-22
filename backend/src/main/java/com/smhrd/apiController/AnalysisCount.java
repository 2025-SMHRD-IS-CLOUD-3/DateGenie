package com.smhrd.apiController;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.smhrd.model.ResultDAO;


@WebServlet("/AnalysisCount")
public class AnalysisCount extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		System.out.println("=== AnalysisCount 서블릿 호출됨 ===");
		 // 요청 파라미터에서 이메일 가져오기
        String email = request.getParameter("email");
        
        // 이메일이 없으면 에러 응답
        if (email == null || email.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\":\"이메일이 필요합니다.\"}");
            return;
        }
        
        try {
            // DAO를 통해 분석 횟수 조회
            ResultDAO dao = new ResultDAO();
            int count = dao.getAnalysisCountByEmail(email);
            
            // JSON 응답 설정
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"count\":" + count + "}");
            
            System.out.println("분석 횟수 조회 성공 - Email: " + email + ", Count: " + count);
            
        } catch (Exception e) {
            // 에러 발생 시 처리
            System.err.println("분석 횟수 조회 실패: " + e.getMessage());
            e.printStackTrace();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\":\"서버 오류가 발생했습니다.\"}");
        }	
		
		
		
		
		
		
		
		
	}

}
