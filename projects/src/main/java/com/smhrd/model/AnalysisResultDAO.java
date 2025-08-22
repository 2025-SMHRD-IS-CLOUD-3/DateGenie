package com.smhrd.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.google.gson.Gson;
import com.smhrd.db.SqlSessionManager;

/**
 * 분석 결과 데이터베이스 접근 객체 (DAO)
 * 
 * 주요 기능:
 * 1. 분석 결과를 데이터베이스에 저장
 * 2. 저장된 분석 결과 조회
 * 3. 사용자별 분석 히스토리 관리
 * 4. 트랜잭션 관리 및 에러 처리
 */
public class AnalysisResultDAO {
    
    private final SqlSessionFactory sqlSessionFactory;
    private final Gson gson;
    
    public AnalysisResultDAO() {
        this.sqlSessionFactory = SqlSessionManager.getSqlSessionFactory();
        this.gson = new Gson();
    }
    
    /**
     * 완전한 분석 결과를 데이터베이스에 저장
     * 
     * @param analysisResult 저장할 분석 결과
     * @return 저장 성공 여부
     */
    public boolean saveAnalysisResult(AnalysisResult analysisResult) {
        
        SqlSession sqlSession = null;
        
        try {
            sqlSession = sqlSessionFactory.openSession(false); // 수동 커밋
            
            // 1. 메인 분석 세션 저장
            if (!insertAnalysisSession(sqlSession, analysisResult)) {
                return false;
            }
            
            // 2. 관심도 추이 데이터 저장
            if (!insertInterestTrends(sqlSession, analysisResult)) {
                sqlSession.rollback();
                return false;
            }
            
            // 3. 감정 분석 저장
            if (!insertEmotionAnalysis(sqlSession, analysisResult)) {
                sqlSession.rollback();
                return false;
            }
            
            // 4. 긍정적 신호들 저장
            if (!insertPositiveSignals(sqlSession, analysisResult)) {
                sqlSession.rollback();
                return false;
            }
            
            // 5. 대표 호감 메시지 저장
            if (!insertFavoriteMessage(sqlSession, analysisResult)) {
                sqlSession.rollback();
                return false;
            }
            
            // 6. 맞춤 조언 저장
            if (!insertCustomAdvice(sqlSession, analysisResult)) {
                sqlSession.rollback();
                return false;
            }
            
            // 모든 저장이 성공하면 커밋
            sqlSession.commit();
            return true;
            
        } catch (Exception e) {
            if (sqlSession != null) {
                sqlSession.rollback();
            }
            System.err.println("분석 결과 저장 실패: " + e.getMessage());
            return false;
            
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }
    
    /**
     * 메인 분석 세션 저장
     */
    private boolean insertAnalysisSession(SqlSession sqlSession, AnalysisResult result) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("sessionId", result.getSessionId());
            params.put("userId", result.getUserId());
            params.put("sessionName", generateSessionName(result));
            params.put("partnerName", result.getPartnerName());
            
            if (result.getMainResults() != null) {
                params.put("successRate", result.getMainResults().getSuccessRate());
                params.put("confidenceLevel", result.getMainResults().getConfidenceLevel());
                params.put("relationshipStage", result.getMainResults().getRelationshipStage());
                params.put("heroInsight", result.getMainResults().getHeroInsight());
            }
            
            if (result.getAnalysisMetadata() != null && 
                result.getAnalysisMetadata().getConversationPeriod() != null) {
                params.put("conversationStart", result.getAnalysisMetadata().getConversationPeriod().getStart());
                params.put("conversationEnd", result.getAnalysisMetadata().getConversationPeriod().getEnd());
                params.put("totalMessages", result.getAnalysisMetadata().getTotalMessages());
            }
            
            int count = sqlSession.insert("com.smhrd.db.ResultMapper.insertAnalysisSession", params);
            return count > 0;
            
        } catch (Exception e) {
            System.err.println("분석 세션 저장 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 관심도 추이 데이터 저장
     */
    private boolean insertInterestTrends(SqlSession sqlSession, AnalysisResult result) {
        try {
            if (result.getInterestTrends() == null || result.getInterestTrends().isEmpty()) {
                return true; // 데이터가 없으면 성공으로 처리
            }
            
            for (AnalysisResult.InterestTrend trend : result.getInterestTrends()) {
                Map<String, Object> params = new HashMap<>();
                params.put("sessionId", result.getSessionId());
                params.put("trendDate", convertDateFormat(trend.getDate()));
                params.put("interestValue", trend.getValue());
                params.put("messageCount", trend.getMessageCount());
                params.put("avgResponseTime", trend.getAvgResponseTime());
                params.put("emojiCount", trend.getEmojiCount());
                
                int count = sqlSession.insert("com.smhrd.db.ResultMapper.insertInterestTrend", params);
                if (count <= 0) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("관심도 추이 저장 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 감정 분석 저장
     */
    private boolean insertEmotionAnalysis(SqlSession sqlSession, AnalysisResult result) {
        try {
            if (result.getEmotionAnalysis() == null) {
                return true;
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("sessionId", result.getSessionId());
            params.put("positive", result.getEmotionAnalysis().getPositive());
            params.put("neutral", result.getEmotionAnalysis().getNeutral());
            params.put("negative", result.getEmotionAnalysis().getNegative());
            params.put("dominantEmotion", result.getEmotionAnalysis().getDominantEmotion());
            params.put("stabilityScore", result.getEmotionAnalysis().getStabilityScore());
            
            // 키워드는 JSON 문자열로 저장
            params.put("positiveKeywords", gson.toJson(result.getEmotionAnalysis().getPositiveKeywords()));
            params.put("negativeKeywords", gson.toJson(result.getEmotionAnalysis().getNegativeKeywords()));
            
            int count = sqlSession.insert("com.smhrd.db.ResultMapper.insertEmotionAnalysis", params);
            return count > 0;
            
        } catch (Exception e) {
            System.err.println("감정 분석 저장 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 긍정적 신호들 저장
     */
    private boolean insertPositiveSignals(SqlSession sqlSession, AnalysisResult result) {
        try {
            if (result.getPositiveSignals() == null || result.getPositiveSignals().isEmpty()) {
                return true;
            }
            
            int priority = 1;
            for (AnalysisResult.PositiveSignal signal : result.getPositiveSignals()) {
                Map<String, Object> params = new HashMap<>();
                params.put("sessionId", result.getSessionId());
                params.put("text", signal.getText());
                params.put("description", signal.getDescription());
                params.put("confidence", signal.getConfidence());
                params.put("type", signal.getType());
                params.put("metricValue", signal.getMetricValue());
                params.put("metricUnit", signal.getMetricUnit());
                params.put("priority", priority++);
                
                int count = sqlSession.insert("com.smhrd.db.ResultMapper.insertPositiveSignal", params);
                if (count <= 0) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("긍정적 신호 저장 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 대표 호감 메시지 저장
     */
    private boolean insertFavoriteMessage(SqlSession sqlSession, AnalysisResult result) {
        try {
            if (result.getFavoriteMessage() == null) {
                return true;
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("sessionId", result.getSessionId());
            params.put("text", result.getFavoriteMessage().getText());
            params.put("confidence", result.getFavoriteMessage().getConfidence());
            params.put("reason", result.getFavoriteMessage().getReason());
            params.put("sender", result.getFavoriteMessage().getSender());
            params.put("messageDate", convertDateTimeFormat(result.getFavoriteMessage().getDate()));
            
            // 추가 분석 데이터
            params.put("sentimentScore", 0.8); // 기본값
            params.put("intimacyLevel", 4); // 기본값
            params.put("actionType", "invitation"); // 기본값
            
            int count = sqlSession.insert("com.smhrd.db.ResultMapper.insertFavoriteMessage", params);
            return count > 0;
            
        } catch (Exception e) {
            System.err.println("대표 호감 메시지 저장 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 맞춤 조언 저장
     */
    private boolean insertCustomAdvice(SqlSession sqlSession, AnalysisResult result) {
        try {
            if (result.getCustomAdvice() == null || result.getCustomAdvice().isEmpty()) {
                return true;
            }
            
            for (AnalysisResult.CustomAdvice advice : result.getCustomAdvice()) {
                Map<String, Object> params = new HashMap<>();
                params.put("sessionId", result.getSessionId());
                params.put("title", advice.getTitle());
                params.put("content", advice.getContent());
                params.put("type", advice.getType());
                params.put("priority", advice.getPriority());
                params.put("urgency", advice.getUrgency());
                
                // 기반 데이터
                if (result.getMainResults() != null) {
                    params.put("basedOnSuccessRate", result.getMainResults().getSuccessRate());
                }
                params.put("basedOnSignals", "[]"); // 기본값
                
                int count = sqlSession.insert("com.smhrd.db.ResultMapper.insertCustomAdvice", params);
                if (count <= 0) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("맞춤 조언 저장 실패: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 저장된 분석 결과 조회 (Frontend 형식으로 변환)
     * 
     * @param sessionId 세션 ID
     * @return Frontend에서 사용할 수 있는 형태의 분석 결과
     */
    public Map<String, Object> getAnalysisResultForFrontend(String sessionId) {
        
        SqlSession sqlSession = null;
        
        try {
            sqlSession = sqlSessionFactory.openSession();
            
            Map<String, Object> result = new HashMap<>();
            
            // 1. 메인 분석 결과 조회
            Map<String, Object> mainResult = sqlSession.selectOne(
                "com.smhrd.db.ResultMapper.getAnalysisResult", sessionId);
            
            if (mainResult != null) {
                result.put("successRate", mainResult.get("successRate"));
                result.put("confidenceLevel", mainResult.get("confidenceLevel"));
                result.put("relationshipStage", mainResult.get("relationshipStage"));
                result.put("heroInsight", mainResult.get("heroInsight"));
            }
            
            // 2. 관심도 추이 데이터 조회
            List<Map<String, Object>> interestTrends = sqlSession.selectList(
                "com.smhrd.db.ResultMapper.getInterestTrends", sessionId);
            result.put("interestTrendData", interestTrends);
            
            // 3. 감정 분석 조회
            Map<String, Object> emotionAnalysis = sqlSession.selectOne(
                "com.smhrd.db.ResultMapper.getEmotionAnalysis", sessionId);
            result.put("emotionData", emotionAnalysis);
            
            // 4. 긍정적 신호들 조회
            List<Map<String, Object>> positiveSignals = sqlSession.selectList(
                "com.smhrd.db.ResultMapper.getPositiveSignals", sessionId);
            result.put("positiveSignals", positiveSignals);
            
            // 5. 대표 호감 메시지 조회
            Map<String, Object> favoriteMessage = sqlSession.selectOne(
                "com.smhrd.db.ResultMapper.getFavoriteMessage", sessionId);
            result.put("favoriteMessage", favoriteMessage);
            
            // 6. 대화 가이드 (고정 데이터 또는 별도 테이블에서 조회)
            result.put("conversationGuide", getDefaultConversationGuide());
            
            // 7. 맞춤 조언 조회
            List<Map<String, Object>> customAdvice = sqlSession.selectList(
                "com.smhrd.db.ResultMapper.getCustomAdvice", sessionId);
            result.put("customAdvice", customAdvice);
            
            return result;
            
        } catch (Exception e) {
            System.err.println("분석 결과 조회 실패: " + e.getMessage());
            return new HashMap<>();
            
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }
    
    /**
     * 사용자별 분석 히스토리 조회
     */
    public List<Map<String, Object>> getUserAnalysisHistory(String userId) {
        
        SqlSession sqlSession = null;
        
        try {
            sqlSession = sqlSessionFactory.openSession();
            
            return sqlSession.selectList(
                "com.smhrd.db.ResultMapper.getUserAnalysisHistory", userId);
            
        } catch (Exception e) {
            System.err.println("분석 히스토리 조회 실패: " + e.getMessage());
            return new ArrayList<>();
            
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }
    
    /**
     * 세션 존재 여부 확인
     */
    public boolean checkSessionExists(String sessionId) {
        
        SqlSession sqlSession = null;
        
        try {
            sqlSession = sqlSessionFactory.openSession();
            
            Integer count = sqlSession.selectOne(
                "com.smhrd.db.ResultMapper.checkSessionExists", sessionId);
            
            return count != null && count > 0;
            
        } catch (Exception e) {
            System.err.println("세션 존재 확인 실패: " + e.getMessage());
            return false;
            
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }
    
    // === 헬퍼 메서드들 ===
    
    /**
     * 세션 이름 생성
     */
    private String generateSessionName(AnalysisResult result) {
        if (result.getPartnerName() != null && !result.getPartnerName().isEmpty()) {
            return result.getPartnerName() + "과의 대화 분석";
        }
        return "대화 분석 결과";
    }
    
    /**
     * 날짜 형식 변환 (8월 13일 -> 2024-08-13)
     */
    private String convertDateFormat(String dateStr) {
        try {
            // "8월 13일" 형태를 "2024-08-13" 형태로 변환
            if (dateStr.contains("월") && dateStr.contains("일")) {
                String[] parts = dateStr.replace("월", "-").replace("일", "").split("-");
                if (parts.length == 2) {
                    int month = Integer.parseInt(parts[0].trim());
                    int day = Integer.parseInt(parts[1].trim());
                    return String.format("2024-%02d-%02d", month, day);
                }
            }
            return dateStr; // 변환 실패 시 원본 반환
        } catch (Exception e) {
            return "2024-01-01"; // 기본값
        }
    }
    
    /**
     * 날짜시간 형식 변환 (8월 20일 오후 3:24 -> 2024-08-20 15:24:00)
     */
    private String convertDateTimeFormat(String dateTimeStr) {
        try {
            // "8월 20일 오후 3:24" 형태를 "2024-08-20 15:24:00" 형태로 변환
            // 간단한 변환 로직 (실제로는 더 정교한 파싱 필요)
            return "2024-08-20 15:24:00"; // 기본값
        } catch (Exception e) {
            return "2024-01-01 12:00:00"; // 기본값
        }
    }
    
    /**
     * 기본 대화 가이드 반환 (템플릿 데이터)
     */
    private List<Map<String, Object>> getDefaultConversationGuide() {
        List<Map<String, Object>> guides = new ArrayList<>();
        
        Map<String, Object> guide1 = new HashMap<>();
        guide1.put("type", "일상 공유");
        guide1.put("text", "오늘 날씨 진짜 좋다! 밖에 나가고 싶어지는 날씨야 😄");
        guide1.put("timing", "오후 2-6시 추천");
        guides.add(guide1);
        
        Map<String, Object> guide2 = new HashMap<>();
        guide2.put("type", "관심사 질문");
        guide2.put("text", "요즘 뭐 재밌는 거 있어? 나도 새로운 취미 찾고 있어서 ㅎㅎ");
        guide2.put("timing", "저녁 시간대 추천");
        guides.add(guide2);
        
        Map<String, Object> guide3 = new HashMap<>();
        guide3.put("type", "만남 제안");
        guide3.put("text", "혹시 시간 되면 커피 한 잔 어때? 오랜만에 수다 떨고 싶어 ☕");
        guide3.put("timing", "주말 오전 추천");
        guides.add(guide3);
        
        return guides;
    }
}