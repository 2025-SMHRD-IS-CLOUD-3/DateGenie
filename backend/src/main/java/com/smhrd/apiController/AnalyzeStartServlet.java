package com.smhrd.apiController;

import com.google.gson.Gson;
import com.smhrd.model.UserInfo; // UserInfo DTO를 import 합니다.
import com.smhrd.gemini.AnalysisJobManager;
import com.smhrd.util.Jsons;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; // HttpSession을 import 합니다.
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/analyze")
public class AnalyzeStartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L; // 경고 메시지 방지를 위해 serialVersionUID 추가

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        
        try {
            // --- 1. 요청에서 파일 내용 목록 파싱 ---
            Gson gson = new Gson();
            Map<String, Object> requestData = gson.fromJson(req.getReader(), Map.class);
            List<Map<String, Object>> fileContents = (List<Map<String, Object>>) requestData.get("fileContents");

            if (fileContents == null || fileContents.isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write(Jsons.error("파일 내용이 비었습니다."));
                return;
            }

            // 파일 내용들을 하나의 문자열로 합치기
            StringBuilder combinedContent = new StringBuilder();
            for (Map<String, Object> fileInfo : fileContents) {
                String originalName = (String) fileInfo.get("originalName");
                String content = (String) fileInfo.get("content");
                
                combinedContent.append("=== 파일: ").append(originalName).append(" ===\n");
                combinedContent.append(content).append("\n\n");
            }

            // --- 2. 세션에서 로그인 정보 가져오기 ---
            HttpSession session = req.getSession();
            UserInfo loginMember = (UserInfo) session.getAttribute("loginMember");

            if (loginMember == null) {
                resp.setStatus(401);
                resp.getWriter().write(Jsons.error("분석을 시작하려면 로그인이 필요합니다."));
                return;
            }

            // --- 3. 분석 시작 (파일 내용 직접 전달) ---
            String jobId = AnalysisJobManager.start(combinedContent.toString(), loginMember.getEmail());
            
            // --- 4. 성공 응답 생성 ---
            Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("jobId", jobId);

            resp.getWriter().write(Jsons.ok(responseData));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(Jsons.error("분석 시작 실패: " + e.getMessage()));
        }    
    }
}