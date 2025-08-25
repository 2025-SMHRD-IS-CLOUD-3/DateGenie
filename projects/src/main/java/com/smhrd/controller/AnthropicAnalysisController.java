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
import com.google.gson.GsonBuilder;
import com.smhrd.model.AnalysisResult;
import com.smhrd.model.AnalysisResultDAO;
import com.smhrd.model.UserInfo;
import com.smhrd.service.AnthropicAnalysisService;

/**
 * Anthropic API를 사용한 대화 분석 컨트롤러
 * 전체 워크플로우: API 호출 → 데이터 처리 → DB 저장 → 결과 반환
 */
@WebServlet("/AnthropicAnalysisController")
public class AnthropicAnalysisController extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private final Gson gson;
    private final AnthropicAnalysisService analysisService;
    private final AnalysisResultDAO resultDAO;
    
    public AnthropicAnalysisController() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .disableHtmlEscaping()
            .create();
        this.analysisService = new AnthropicAnalysisService();
        this.resultDAO = new AnalysisResultDAO();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 인코딩 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // CORS 헤더 설정
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        PrintWriter out = response.getWriter();
        Map<String, Object> responseData = new HashMap<>();
        
        try {
            System.out.println("=== Anthropic 분석 요청 시작 ===");
            
            // 사용자 인증 확인
            HttpSession session = request.getSession(false);
            if (session == null) {
                System.err.println("세션이 없습니다.");
                responseData.put("success", false);
                responseData.put("message", "로그인이 필요합니다.");
                out.print(gson.toJson(responseData));
                return;
            }
            
            // 세션 정보 디버그
            System.out.println("세션 ID: " + session.getId());
            System.out.println("세션 생성 시간: " + new java.util.Date(session.getCreationTime()));
            java.util.Enumeration<String> attributeNames = session.getAttributeNames();
            System.out.println("세션에 저장된 속성들:");
            while (attributeNames.hasMoreElements()) {
                String attrName = attributeNames.nextElement();
                Object attrValue = session.getAttribute(attrName);
                System.out.println("  " + attrName + " = " + attrValue);
            }
            
            UserInfo user = (UserInfo) session.getAttribute("loginMember");
            if (user == null) {
                System.err.println("로그인된 사용자 정보가 없습니다. 세션에서 'loginMember' 속성을 찾을 수 없음");
                responseData.put("success", false);
                responseData.put("message", "로그인이 필요합니다.");
                out.print(gson.toJson(responseData));
                return;
            }
            
            // 요청 파라미터 추출
            String action = request.getParameter("action");
            System.out.println("요청된 액션: " + action);
            
            if ("analyze".equals(action)) {
                handleAnalysisRequest(request, response, user, responseData, out);
            } else if ("getResult".equals(action)) {
                handleGetResult(request, response, user, responseData, out);
            } else if ("getHistory".equals(action)) {
                handleGetHistory(request, response, user, responseData, out);
            } else if ("checkSession".equals(action)) {
                handleCheckSession(request, response, user, responseData, out);
            } else {
                responseData.put("success", false);
                responseData.put("message", "지원되지 않는 액션입니다: " + action);
                out.print(gson.toJson(responseData));
            }
            
        } catch (Exception e) {
            System.err.println("컨트롤러 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            
            responseData.put("success", false);
            responseData.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            out.print(gson.toJson(responseData));
        } finally {
            out.flush();
            out.close();
        }
    }
    
    /**
     * 대화 분석 요청 처리
     */
    private void handleAnalysisRequest(HttpServletRequest request, HttpServletResponse response, 
            UserInfo user, Map<String, Object> responseData, PrintWriter out) throws Exception {
        
        System.out.println("분석 요청 처리 시작 - 사용자: " + user.getEmail());
        
        // 대화 데이터 추출
        String conversationData = request.getParameter("conversationData");
        String partnerName = request.getParameter("partnerName");
        
        if (conversationData == null || conversationData.trim().isEmpty()) {
            responseData.put("success", false);
            responseData.put("message", "분석할 대화 데이터가 없습니다.");
            out.print(gson.toJson(responseData));
            return;
        }
        
        if (partnerName == null || partnerName.trim().isEmpty()) {
            partnerName = "상대방";
        }
        
        System.out.println("대화 데이터 길이: " + conversationData.length() + "자");
        System.out.println("상대방 이름: " + partnerName);
        
        try {
            // Anthropic API를 사용하여 분석 수행
            AnalysisResult analysisResult = analysisService.analyzeConversation(
                conversationData, user.getEmail(), partnerName);
            
            if (analysisResult == null) {
                throw new Exception("분석 결과가 null입니다.");
            }
            
            System.out.println("API 분석 완료 - sessionId: " + analysisResult.getSessionId());
            
            // 데이터베이스에 저장
            boolean dbSaved = saveToDatabase(analysisResult);
            
            if (!dbSaved) {
                System.err.println("데이터베이스 저장 실패, 하지만 분석 결과는 반환");
                // DB 저장 실패해도 분석 결과는 반환 (사용자 경험 최우선)
            }
            
            // 응답 데이터 구성
            responseData.put("success", true);
            responseData.put("message", "분석이 완료되었습니다.");
            responseData.put("sessionId", analysisResult.getSessionId());
            responseData.put("analysisData", analysisResult.toFrontendFormat());
            
            // 세션에 저장 (백업)
            HttpSession httpSession = request.getSession();
            httpSession.setAttribute("lastAnalysisResult", analysisResult);
            httpSession.setAttribute("lastAnalysisSessionId", analysisResult.getSessionId());
            
            System.out.println("분석 완료 및 응답 전송");
            out.print(gson.toJson(responseData));
            
        } catch (Exception e) {
            System.err.println("분석 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            
            responseData.put("success", false);
            responseData.put("message", "분석 중 오류가 발생했습니다: " + e.getMessage());
            responseData.put("errorDetail", e.getClass().getSimpleName());
            out.print(gson.toJson(responseData));
        }
    }
    
    /**
     * 분석 결과 조회 처리
     */
    private void handleGetResult(HttpServletRequest request, HttpServletResponse response, 
            UserInfo user, Map<String, Object> responseData, PrintWriter out) throws Exception {
        
        String sessionId = request.getParameter("sessionId");
        System.out.println("분석 결과 조회 - sessionId: " + sessionId + ", 사용자: " + user.getEmail());
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            responseData.put("success", false);
            responseData.put("message", "세션 ID가 필요합니다.");
            out.print(gson.toJson(responseData));
            return;
        }
        
        try {
            // 데이터베이스에서 조회
            Map<String, Object> analysisData = resultDAO.getAnalysisResultForFrontend(sessionId);
            
            if (analysisData.isEmpty()) {
                // DB에 없으면 세션에서 확인
                HttpSession httpSession = request.getSession();
                String lastSessionId = (String) httpSession.getAttribute("lastAnalysisSessionId");
                AnalysisResult lastResult = (AnalysisResult) httpSession.getAttribute("lastAnalysisResult");
                
                if (sessionId.equals(lastSessionId) && lastResult != null) {
                    System.out.println("세션에서 분석 결과 복원");
                    analysisData = lastResult.toFrontendFormat();
                } else {
                    responseData.put("success", false);
                    responseData.put("message", "분석 결과를 찾을 수 없습니다.");
                    out.print(gson.toJson(responseData));
                    return;
                }
            }
            
            responseData.put("success", true);
            responseData.put("analysisData", analysisData);
            
            System.out.println("분석 결과 조회 완료");
            out.print(gson.toJson(responseData));
            
        } catch (Exception e) {
            System.err.println("결과 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            
            responseData.put("success", false);
            responseData.put("message", "결과 조회 중 오류가 발생했습니다.");
            out.print(gson.toJson(responseData));
        }
    }
    
    /**
     * 분석 히스토리 조회 처리
     */
    private void handleGetHistory(HttpServletRequest request, HttpServletResponse response, 
            UserInfo user, Map<String, Object> responseData, PrintWriter out) throws Exception {
        
        System.out.println("분석 히스토리 조회 - 사용자: " + user.getEmail());
        
        try {
            // 사용자별 분석 히스토리 조회
            Map<String, Object> historyData = new HashMap<>();
            historyData.put("analysisHistory", resultDAO.getUserAnalysisHistory(user.getEmail()));
            historyData.put("analysisStats", resultDAO.getAnalysisStats(user.getEmail()));
            
            responseData.put("success", true);
            responseData.put("historyData", historyData);
            
            System.out.println("히스토리 조회 완료");
            out.print(gson.toJson(responseData));
            
        } catch (Exception e) {
            System.err.println("히스토리 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            
            responseData.put("success", false);
            responseData.put("message", "히스토리 조회 중 오류가 발생했습니다.");
            out.print(gson.toJson(responseData));
        }
    }
    
    /**
     * 분석 결과를 데이터베이스에 저장 (포괄적)
     */
    private boolean saveToDatabase(AnalysisResult analysisResult) {
        try {
            System.out.println("데이터베이스 저장 시작 - sessionId: " + analysisResult.getSessionId());
            
            // 메인 분석 결과 저장
            boolean mainSaved = resultDAO.saveAnalysisResult(analysisResult);
            
            if (!mainSaved) {
                System.err.println("메인 분석 결과 저장 실패");
                return false;
            }
            
            // 상세 데이터 저장 (Anthropic API 응답의 모든 데이터 저장)
            saveDetailedAnalysisData(analysisResult);
            
            System.out.println("데이터베이스 저장 완료 (메인 + 상세 데이터)");
            return true;
            
        } catch (Exception e) {
            System.err.println("데이터베이스 저장 중 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 상세 분석 데이터 저장 (관심도 추이, 긍정 신호, 대표 메시지, 조언 등)
     */
    private void saveDetailedAnalysisData(AnalysisResult analysisResult) {
        String sessionId = analysisResult.getSessionId();
        
        try {
            // 관심도 추이 데이터 저장
            if (analysisResult.getInterestTrends() != null && !analysisResult.getInterestTrends().isEmpty()) {
                System.out.println("관심도 추이 데이터 저장 중...");
                for (AnalysisResult.InterestTrend trend : analysisResult.getInterestTrends()) {
                    Map<String, Object> trendData = new HashMap<>();
                    trendData.put("sessionId", sessionId);
                    trendData.put("trendDate", trend.getDate());
                    trendData.put("interestValue", trend.getValue());
                    trendData.put("messageCount", trend.getMessageCount());
                    trendData.put("avgResponseTime", trend.getAvgResponseTime());
                    trendData.put("emojiCount", trend.getEmojiCount());
                    
                    // ResultMapper의 insertInterestTrend 사용
                    insertDataToDatabase("ResultMapper.insertInterestTrend", trendData);
                }
                System.out.println("관심도 추이 저장 완료: " + analysisResult.getInterestTrends().size() + "건");
            }
            
            // 감정 분석 결과 저장
            if (analysisResult.getEmotionAnalysis() != null) {
                System.out.println("감정 분석 데이터 저장 중...");
                Map<String, Object> emotionData = new HashMap<>();
                emotionData.put("sessionId", sessionId);
                emotionData.put("positive", analysisResult.getEmotionAnalysis().getPositive());
                emotionData.put("neutral", analysisResult.getEmotionAnalysis().getNeutral());
                emotionData.put("negative", analysisResult.getEmotionAnalysis().getNegative());
                
                // 주요 감정과 안정성 점수 계산
                double maxEmotion = Math.max(Math.max(
                    analysisResult.getEmotionAnalysis().getPositive(),
                    analysisResult.getEmotionAnalysis().getNeutral()),
                    analysisResult.getEmotionAnalysis().getNegative());
                
                String dominantEmotion = "neutral";
                if (maxEmotion == analysisResult.getEmotionAnalysis().getPositive()) {
                    dominantEmotion = "positive";
                } else if (maxEmotion == analysisResult.getEmotionAnalysis().getNegative()) {
                    dominantEmotion = "negative";
                }
                
                emotionData.put("dominantEmotion", dominantEmotion);
                emotionData.put("stabilityScore", maxEmotion); // 간단한 안정성 지수
                emotionData.put("positiveKeywords", "긍정적 키워드 추출 필요"); // AI에서 추출 필요
                emotionData.put("negativeKeywords", "부정적 키워드 추출 필요"); // AI에서 추출 필요
                
                insertDataToDatabase("ResultMapper.insertEmotionAnalysis", emotionData);
                System.out.println("감정 분석 저장 완료");
            }
            
            // 긍정적 신호 저장
            if (analysisResult.getPositiveSignals() != null && !analysisResult.getPositiveSignals().isEmpty()) {
                System.out.println("긍정 신호 저장 중...");
                int priority = 1;
                for (AnalysisResult.PositiveSignal signal : analysisResult.getPositiveSignals()) {
                    Map<String, Object> signalData = new HashMap<>();
                    signalData.put("sessionId", sessionId);
                    signalData.put("text", signal.getText());
                    signalData.put("description", signal.getDescription());
                    signalData.put("confidence", signal.getConfidence());
                    signalData.put("type", "positive_indicator");
                    signalData.put("metricValue", 0); // 필요시 추가 메트릭
                    signalData.put("metricUnit", "");
                    signalData.put("priority", priority++);
                    
                    insertDataToDatabase("ResultMapper.insertPositiveSignal", signalData);
                }
                System.out.println("긍정 신호 저장 완료: " + analysisResult.getPositiveSignals().size() + "건");
            }
            
            // 대표 호감 메시지 저장
            if (analysisResult.getFavoriteMessage() != null) {
                System.out.println("대표 메시지 저장 중...");
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("sessionId", sessionId);
                messageData.put("text", analysisResult.getFavoriteMessage().getText());
                messageData.put("confidence", analysisResult.getFavoriteMessage().getConfidence());
                messageData.put("reason", analysisResult.getFavoriteMessage().getReason());
                messageData.put("messageDate", analysisResult.getFavoriteMessage().getDate());
                messageData.put("sender", "partner"); // 기본값
                messageData.put("sentimentScore", analysisResult.getFavoriteMessage().getConfidence()); 
                messageData.put("intimacyLevel", Math.min(5, analysisResult.getFavoriteMessage().getConfidence() / 20));
                messageData.put("actionType", "affection_expression");
                
                insertDataToDatabase("ResultMapper.insertFavoriteMessage", messageData);
                System.out.println("대표 메시지 저장 완료");
            }
            
            // 맞춤 조언 저장
            if (analysisResult.getCustomAdvice() != null && !analysisResult.getCustomAdvice().isEmpty()) {
                System.out.println("맞춤 조언 저장 중...");
                int priority = 1;
                for (AnalysisResult.CustomAdvice advice : analysisResult.getCustomAdvice()) {
                    Map<String, Object> adviceData = new HashMap<>();
                    adviceData.put("sessionId", sessionId);
                    adviceData.put("title", advice.getTitle());
                    adviceData.put("content", advice.getContent());
                    adviceData.put("type", "general_advice");
                    adviceData.put("priority", priority++);
                    adviceData.put("urgency", "medium");
                    adviceData.put("basedOnSuccessRate", analysisResult.getMainResults() != null ? 
                        analysisResult.getMainResults().getSuccessRate() : 0);
                    adviceData.put("basedOnSignals", analysisResult.getPositiveSignals() != null ? 
                        analysisResult.getPositiveSignals().size() : 0);
                    
                    insertDataToDatabase("ResultMapper.insertCustomAdvice", adviceData);
                }
                System.out.println("맞춤 조언 저장 완료: " + analysisResult.getCustomAdvice().size() + "건");
            }
            
        } catch (Exception e) {
            System.err.println("상세 데이터 저장 중 오류: " + e.getMessage());
            e.printStackTrace();
            // 상세 데이터 저장 실패해도 메인 분석은 저장된 상태이므로 계속 진행
        }
    }
    
    /**
     * 데이터베이스 삽입 헬퍼 메소드
     */
    private void insertDataToDatabase(String mapperId, Map<String, Object> data) {
        try {
            org.apache.ibatis.session.SqlSession session = 
                com.smhrd.db.SqlSessionManager.getSqlSessionFactory().openSession();
            try {
                int result = session.insert(mapperId, data);
                if (result > 0) {
                    session.commit();
                } else {
                    session.rollback();
                    System.err.println("데이터 삽입 실패: " + mapperId);
                }
            } finally {
                session.close();
            }
        } catch (Exception e) {
            System.err.println("데이터베이스 삽입 오류 (" + mapperId + "): " + e.getMessage());
            // 개별 데이터 실패는 전체 프로세스를 중단하지 않음
        }
    }
    
    /**
     * 세션 확인 처리
     */
    private void handleCheckSession(HttpServletRequest request, HttpServletResponse response, 
            UserInfo user, Map<String, Object> responseData, PrintWriter out) {
        try {
            System.out.println("세션 확인 요청 - 사용자: " + user.getEmail());
            
            responseData.put("success", true);
            responseData.put("message", "로그인 상태 정상");
            responseData.put("userEmail", user.getEmail());
            
            out.print(gson.toJson(responseData));
            
        } catch (Exception e) {
            System.err.println("세션 확인 중 오류: " + e.getMessage());
            e.printStackTrace();
            
            responseData.put("success", false);
            responseData.put("message", "세션 확인 실패: " + e.getMessage());
            out.print(gson.toJson(responseData));
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // GET 요청도 처리 (결과 조회용)
        doPost(request, response);
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // CORS preflight 요청 처리
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}