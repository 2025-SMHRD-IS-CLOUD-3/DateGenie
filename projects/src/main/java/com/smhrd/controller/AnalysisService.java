package com.smhrd.controller;

import java.io.BufferedReader;
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
import com.google.gson.GsonBuilder;
import com.smhrd.model.AnalysisResult;
import com.smhrd.model.AnalysisResultDAO;
import com.smhrd.model.UserInfo;
import com.smhrd.service.GeminiAnalysisService;

/**
 * 대화 분석 요청을 처리하는 Controller
 * 
 * 주요 기능:
 * 1. 클라이언트로부터 대화 데이터 수신
 * 2. Gemini API를 통한 분석 수행
 * 3. 분석 결과를 데이터베이스에 저장
 * 4. Frontend에 결과 반환
 * 5. 에러 처리 및 상태 관리
 */
@WebServlet("/AnalysisService")
public class AnalysisService extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final GeminiAnalysisService geminiService;
    private final AnalysisResultDAO analysisDAO;
    private final Gson gson;
    
    public AnalysisService() {
        this.geminiService = new GeminiAnalysisService();
        this.analysisDAO = new AnalysisResultDAO();
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
    }
    
    /**
     * POST 요청 처리: 새로운 대화 분석 요청
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 요청/응답 설정
            request.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            
            // 세션에서 사용자 정보 확인
            HttpSession session = request.getSession();
            UserInfo loginMember = (UserInfo) session.getAttribute("loginMember");
            
            if (loginMember == null) {
                sendErrorResponse(response, "로그인이 필요합니다.", 401);
                return;
            }
            
            // JSON 요청 데이터 읽기
            String requestBody = readRequestBody(request);
            
            // 요청 데이터 파싱
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = gson.fromJson(requestBody, Map.class);
            
            String conversationData = (String) requestData.get("conversationData");
            String partnerName = (String) requestData.get("partnerName");
            
            // 입력 데이터 검증
            if (conversationData == null || conversationData.trim().isEmpty()) {
                sendErrorResponse(response, "대화 데이터가 필요합니다.", 400);
                return;
            }
            
            if (partnerName == null || partnerName.trim().isEmpty()) {
                partnerName = "상대방"; // 기본값
            }
            
            // Gemini API를 통한 분석 수행
            System.out.println("=== 대화 분석 시작 ===");
            System.out.println("사용자: " + loginMember.getEmail());
            System.out.println("상대방: " + partnerName);
            System.out.println("대화 데이터 길이: " + conversationData.length());
            
            AnalysisResult analysisResult = geminiService.analyzeConversation(
                conversationData, 
                loginMember.getEmail(), 
                partnerName
            );
            
            System.out.println("분석 완료. 세션 ID: " + analysisResult.getSessionId());
            
            // 데이터베이스에 결과 저장
            boolean saved = analysisDAO.saveAnalysisResult(analysisResult);
            
            if (!saved) {
                System.err.println("데이터베이스 저장 실패");
                sendErrorResponse(response, "분석 결과 저장에 실패했습니다.", 500);
                return;
            }
            
            System.out.println("데이터베이스 저장 완료");
            
            // Frontend용 응답 데이터 생성
            Map<String, Object> frontendData = analysisDAO.getAnalysisResultForFrontend(
                analysisResult.getSessionId()
            );
            
            // 성공 응답
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "분석이 완료되었습니다!");
            responseData.put("sessionId", analysisResult.getSessionId());
            responseData.put("analysisData", frontendData);
            
            // 응답 전송
            PrintWriter out = response.getWriter();
            out.print(gson.toJson(responseData));
            out.flush();
            
            System.out.println("=== 대화 분석 완료 ===");
            
        } catch (GeminiAnalysisService.AnalysisException e) {
            System.err.println("분석 오류: " + e.getMessage());
            sendErrorResponse(response, "분석 처리 중 오류가 발생했습니다: " + e.getMessage(), 500);
            
        } catch (Exception e) {
            System.err.println("예상치 못한 오류: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, "서버 내부 오류가 발생했습니다.", 500);
        }
    }
    
    /**
     * GET 요청 처리: 저장된 분석 결과 조회
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            request.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            
            // 세션에서 사용자 정보 확인
            HttpSession session = request.getSession();
            UserInfo loginMember = (UserInfo) session.getAttribute("loginMember");
            
            if (loginMember == null) {
                sendErrorResponse(response, "로그인이 필요합니다.", 401);
                return;
            }
            
            String action = request.getParameter("action");
            
            if ("getResult".equals(action)) {
                // 특정 분석 결과 조회
                String sessionId = request.getParameter("sessionId");
                
                if (sessionId == null || sessionId.trim().isEmpty()) {
                    sendErrorResponse(response, "세션 ID가 필요합니다.", 400);
                    return;
                }
                
                Map<String, Object> analysisData = analysisDAO.getAnalysisResultForFrontend(sessionId);
                
                if (analysisData.isEmpty()) {
                    sendErrorResponse(response, "분석 결과를 찾을 수 없습니다.", 404);
                    return;
                }
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("analysisData", analysisData);
                
                PrintWriter out = response.getWriter();
                out.print(gson.toJson(responseData));
                out.flush();
                
            } else if ("getHistory".equals(action)) {
                // 사용자 분석 히스토리 조회
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("history", analysisDAO.getUserAnalysisHistory(loginMember.getEmail()));
                
                PrintWriter out = response.getWriter();
                out.print(gson.toJson(responseData));
                out.flush();
                
            } else {
                sendErrorResponse(response, "지원하지 않는 액션입니다.", 400);
            }
            
        } catch (Exception e) {
            System.err.println("조회 오류: " + e.getMessage());
            sendErrorResponse(response, "조회 중 오류가 발생했습니다.", 500);
        }
    }
    
    /**
     * 요청 본문 읽기
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }
        
        return requestBody.toString();
    }
    
    /**
     * 에러 응답 전송
     */
    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) {
        try {
            response.setStatus(statusCode);
            response.setContentType("application/json; charset=UTF-8");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", message);
            errorResponse.put("statusCode", statusCode);
            
            PrintWriter out = response.getWriter();
            out.print(gson.toJson(errorResponse));
            out.flush();
            
        } catch (IOException e) {
            System.err.println("에러 응답 전송 실패: " + e.getMessage());
        }
    }
}