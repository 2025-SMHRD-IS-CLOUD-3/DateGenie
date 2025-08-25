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
            System.out.println("=== AnalysisService doPost 시작 ===");
            
            // 요청/응답 설정
            request.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            System.out.println("1. 요청/응답 설정 완료");
            
            // 세션에서 사용자 정보 확인
            HttpSession session = request.getSession();
            UserInfo loginMember = (UserInfo) session.getAttribute("loginMember");
            System.out.println("2. 세션 정보 확인 - loginMember: " + (loginMember != null ? loginMember.getEmail() : "null"));
            
            if (loginMember == null) {
                System.out.println("3. 로그인 필요 - 401 에러 반환");
                sendErrorResponse(response, "로그인이 필요합니다.", 401);
                return;
            }
            
            // JSON 요청 데이터 읽기
            System.out.println("4. 요청 본문 읽기 시작");
            String requestBody = readRequestBody(request);
            System.out.println("5. 요청 본문 읽기 완료 - 길이: " + (requestBody != null ? requestBody.length() : 0));
            
            // 요청 데이터 파싱
            System.out.println("6. JSON 파싱 시작");
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = gson.fromJson(requestBody, Map.class);
            System.out.println("7. JSON 파싱 완료");
            
            String conversationData = (String) requestData.get("conversationData");
            String partnerName = (String) requestData.get("partnerName");
            System.out.println("8. 요청 데이터 추출 완료 - conversationData: " + 
                (conversationData != null ? conversationData.length() + "자" : "null") + 
                ", partnerName: " + partnerName);
            
            // 입력 데이터 검증
            if (conversationData == null || conversationData.trim().isEmpty()) {
                System.out.println("9. 대화 데이터 없음 - 400 에러 반환");
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
            
            System.out.println("10. Gemini API 호출 시작");
            AnalysisResult analysisResult = geminiService.analyzeConversation(
                conversationData, 
                loginMember.getEmail(), 
                partnerName
            );
            System.out.println("11. Gemini API 호출 완료");
            
            System.out.println("분석 완료. 세션 ID: " + analysisResult.getSessionId());
            
            // 데이터베이스에 결과 저장
            System.out.println("12. 데이터베이스 저장 시작");
            boolean saved = analysisDAO.saveAnalysisResult(analysisResult);
            
            if (!saved) {
                System.err.println("13. 데이터베이스 저장 실패");
                sendErrorResponse(response, "분석 결과 저장에 실패했습니다.", 500);
                return;
            }
            
            System.out.println("13. 데이터베이스 저장 완료");
            
            // Frontend용 응답 데이터 생성
            System.out.println("14. Frontend 데이터 생성 시작");
            Map<String, Object> frontendData = analysisDAO.getAnalysisResultForFrontend(
                analysisResult.getSessionId()
            );
            System.out.println("15. Frontend 데이터 생성 완료 - 데이터 크기: " + frontendData.size());
            
            // 성공 응답
            System.out.println("16. 응답 데이터 생성 시작");
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "분석이 완료되었습니다!");
            responseData.put("sessionId", analysisResult.getSessionId());
            responseData.put("analysisData", frontendData);
            
            // 응답 전송 - Java 17 호환 방식
            System.out.println("17. 클라이언트로 응답 전송 시작");
            PrintWriter out = response.getWriter();
            
            try {
                out.print(gson.toJson(responseData));
                System.out.println("JSON 직렬화 성공");
            } catch (Exception e) {
                System.err.println("JSON 직렬화 실패, 완전한 수동 JSON 생성: " + e.getMessage());
                // Java 17 AtomicReference 문제 시 완전한 수동 JSON 생성
                String manualJson = buildManualJsonResponse(responseData);
                out.print(manualJson);
                System.out.println("수동 JSON 생성 완료 - 길이: " + manualJson.length());
            }
            
            out.flush();
            System.out.println("18. 응답 전송 완료");
            
            System.out.println("=== 대화 분석 완료 ===");
            
        } catch (GeminiAnalysisService.AnalysisException e) {
            System.err.println("=== AnalysisException 발생 ===");
            System.err.println("에러 메시지: " + e.getMessage());
            System.err.println("스택 트레이스:");
            e.printStackTrace();
            sendErrorResponse(response, "분석 처리 중 오류가 발생했습니다: " + e.getMessage(), 500);
            
        } catch (Exception e) {
            System.err.println("=== 예상치 못한 Exception 발생 ===");
            System.err.println("에러 타입: " + e.getClass().getSimpleName());
            System.err.println("에러 메시지: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("원인: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
            System.err.println("전체 스택 트레이스:");
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
            // 응답이 이미 커밋되었는지 확인
            if (response.isCommitted()) {
                System.err.println("응답이 이미 커밋되어 JSON 에러 응답을 보낼 수 없습니다: " + message);
                return;
            }
            
            // 기존 응답 내용 클리어
            response.reset();
            
            // JSON 응답 설정
            response.setStatus(statusCode);
            response.setContentType("application/json; charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", message);
            errorResponse.put("statusCode", statusCode);
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            PrintWriter out = response.getWriter();
            String jsonResponse = gson.toJson(errorResponse);
            out.print(jsonResponse);
            out.flush();
            out.close();
            
            System.out.println("JSON 에러 응답 전송: " + jsonResponse);
            
        } catch (Exception e) {
            System.err.println("에러 응답 전송 실패: " + e.getMessage());
            e.printStackTrace();
            
            // 최후의 수단으로 간단한 에러 응답 시도
            try {
                if (!response.isCommitted()) {
                    response.reset();
                    response.setStatus(500);
                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().print("{\"success\":false,\"message\":\"서버 오류\"}");
                    response.getWriter().flush();
                }
            } catch (Exception finalException) {
                System.err.println("최종 에러 응답도 실패: " + finalException.getMessage());
            }
        }
    }
    
    /**
     * Java 17 AtomicReference 문제 해결을 위한 수동 JSON 생성
     * 모든 데이터를 안전하게 직렬화하여 완전한 분석 결과 제공
     */
    @SuppressWarnings("unchecked")
    private String buildManualJsonResponse(Map<String, Object> responseData) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // 기본 성공 정보
        json.append("\"success\":true,");
        json.append("\"message\":\"분석이 완료되었습니다.\",");
        
        // 세션 ID
        Object sessionId = responseData.get("sessionId");
        if (sessionId != null) {
            json.append("\"sessionId\":\"").append(escapeJson(sessionId.toString())).append("\",");
        }
        
        // 분석 데이터
        json.append("\"analysisData\":{");
        
        try {
            Map<String, Object> analysisData = (Map<String, Object>) responseData.get("analysisData");
            if (analysisData != null) {
                boolean first = true;
                
                // 주요 결과 (successRate, relationshipStage 등)
                for (String key : new String[]{"successRate", "confidenceLevel", "relationshipStage", "heroInsight"}) {
                    Object value = analysisData.get(key);
                    if (value != null) {
                        if (!first) json.append(",");
                        json.append("\"").append(key).append("\":");
                        if (value instanceof String) {
                            json.append("\"").append(escapeJson(value.toString())).append("\"");
                        } else {
                            json.append(value.toString());
                        }
                        first = false;
                    }
                }
                
                // 감정 분석 데이터
                Object emotionData = analysisData.get("emotionData");
                if (emotionData != null && emotionData instanceof Map) {
                    if (!first) json.append(",");
                    json.append("\"emotionData\":{");
                    Map<String, Object> emotion = (Map<String, Object>) emotionData;
                    boolean emotionFirst = true;
                    for (String key : new String[]{"positive", "neutral", "negative", "dominantEmotion"}) {
                        Object value = emotion.get(key);
                        if (value != null) {
                            if (!emotionFirst) json.append(",");
                            json.append("\"").append(key).append("\":");
                            if (value instanceof String) {
                                json.append("\"").append(escapeJson(value.toString())).append("\"");
                            } else {
                                json.append(value.toString());
                            }
                            emotionFirst = false;
                        }
                    }
                    json.append("}");
                    first = false;
                }
                
                // 긍정적 신호들 (리스트)
                Object positiveSignals = analysisData.get("positiveSignals");
                if (positiveSignals != null && positiveSignals instanceof java.util.List) {
                    if (!first) json.append(",");
                    json.append("\"positiveSignals\":[");
                    java.util.List<?> signals = (java.util.List<?>) positiveSignals;
                    for (int i = 0; i < signals.size(); i++) {
                        if (i > 0) json.append(",");
                        Object signal = signals.get(i);
                        if (signal instanceof Map) {
                            Map<String, Object> signalMap = (Map<String, Object>) signal;
                            json.append("{");
                            boolean signalFirst = true;
                            for (String key : new String[]{"text", "description", "confidence", "type"}) {
                                Object value = signalMap.get(key);
                                if (value != null) {
                                    if (!signalFirst) json.append(",");
                                    json.append("\"").append(key).append("\":");
                                    if (value instanceof String) {
                                        json.append("\"").append(escapeJson(value.toString())).append("\"");
                                    } else {
                                        json.append(value.toString());
                                    }
                                    signalFirst = false;
                                }
                            }
                            json.append("}");
                        }
                    }
                    json.append("]");
                    first = false;
                }
                
                // 대화 가이드 (기본값)
                if (!first) json.append(",");
                json.append("\"conversationGuide\":[]");
            }
        } catch (Exception e) {
            System.err.println("수동 JSON 생성 중 오류 (기본값 사용): " + e.getMessage());
            json.append("\"error\":\"JSON 생성 중 일부 데이터 누락\"");
        }
        
        json.append("}"); // analysisData 종료
        json.append("}"); // 전체 응답 종료
        
        return json.toString();
    }
    
    /**
     * JSON 문자열 이스케이프 처리
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}