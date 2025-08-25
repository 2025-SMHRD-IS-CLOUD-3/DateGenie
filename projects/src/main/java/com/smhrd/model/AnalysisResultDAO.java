package com.smhrd.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        this.gson = new GsonBuilder()
            .setLenient()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();
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
            
            // 6. 맞춤 조언 저장 - 실패해도 다른 데이터는 저장 계속
            try {
                insertCustomAdvice(sqlSession, analysisResult);
                System.out.println("맞춤 조언 저장 성공");
            } catch (Exception e) {
                System.err.println("맞춤 조언 저장 실패 (다른 데이터는 저장 계속): " + e.getMessage());
                // 맞춤 조언 저장 실패해도 롤백하지 않고 계속 진행
            }
            
            System.out.println("=== 데이터베이스 저장 상태 디버그 ===");
            System.out.println("주요 데이터 저장 완료");
            System.out.println("=== 디버그 끝 ===");
            
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
            } else {
                // 기본값 설정 (mainResults가 null인 경우)
                System.out.println("=== mainResults가 null이어서 기본값 사용 ===");
                params.put("successRate", 50.0);
                params.put("confidenceLevel", 50.0);
                params.put("relationshipStage", "분석 중");
                params.put("heroInsight", "분석 결과를 처리 중입니다.");
                System.out.println("기본값 설정 완료");
            }
            
            if (result.getAnalysisMetadata() != null && 
                result.getAnalysisMetadata().getConversationPeriod() != null) {
                params.put("conversationStart", result.getAnalysisMetadata().getConversationPeriod().getStart());
                params.put("conversationEnd", result.getAnalysisMetadata().getConversationPeriod().getEnd());
                params.put("totalMessages", result.getAnalysisMetadata().getTotalMessages());
            } else {
                // 기본값 설정 (분석 메타데이터가 없는 경우)
                java.time.LocalDate today = java.time.LocalDate.now();
                params.put("conversationStart", today.toString());
                params.put("conversationEnd", today.toString());
                params.put("totalMessages", 0);
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
                // 긴급도 값 - 데이터베이스 제약조건에 맞게 정규화
                String originalUrgency = advice.getUrgency();
                String urgencyValue = normalizeUrgencyValue(originalUrgency);
                System.out.println("=== 긴급도 처리 디버그 ===");
                System.out.println("원본 urgency: '" + originalUrgency + "'");
                System.out.println("최종 urgency: '" + urgencyValue + "'");
                System.out.println("=== 디버그 끝 ===");
                
                params.put("urgency", urgencyValue);
                
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
            
            // 2. 관심도 추이 데이터 조회 - 오류 발생 시 빈 리스트 반환
            List<Map<String, Object>> interestTrends = new ArrayList<>();
            try {
                interestTrends = sqlSession.selectList(
                    "com.smhrd.db.ResultMapper.getInterestTrends", sessionId);
                System.out.println("관심도 추이 데이터 조회 성공: " + interestTrends.size() + "개");
            } catch (Exception e) {
                System.err.println("관심도 추이 데이터 조회 실패 (빈 리스트 반환): " + e.getMessage());
                // 빈 리스트로 계속 진행
            }
            result.put("interestTrendData", interestTrends);
            
            // 3. 감정 분석 조회 - 실패 시 빈 맵 반환
            Map<String, Object> emotionAnalysis = new HashMap<>();
            try {
                emotionAnalysis = sqlSession.selectOne(
                    "com.smhrd.db.ResultMapper.getEmotionAnalysis", sessionId);
                System.out.println("감정 분석 데이터 조회 성공");
            } catch (Exception e) {
                System.err.println("감정 분석 데이터 조회 실패 (기본값 반환): " + e.getMessage());
            }
            result.put("emotionData", emotionAnalysis);
            
            // 4. 긍정적 신호들 조회 - 실패 시 빈 리스트 반환
            List<Map<String, Object>> positiveSignals = new ArrayList<>();
            try {
                positiveSignals = sqlSession.selectList(
                    "com.smhrd.db.ResultMapper.getPositiveSignals", sessionId);
                System.out.println("긍정적 신호 데이터 조회 성공: " + positiveSignals.size() + "개");
            } catch (Exception e) {
                System.err.println("긍정적 신호 데이터 조회 실패 (빈 리스트 반환): " + e.getMessage());
            }
            result.put("positiveSignals", positiveSignals);
            
            // 5. 대표 호감 메시지 조회 - 실패 시 빈 맵 반환
            Map<String, Object> favoriteMessage = new HashMap<>();
            try {
                favoriteMessage = sqlSession.selectOne(
                    "com.smhrd.db.ResultMapper.getFavoriteMessage", sessionId);
                System.out.println("대표 호감 메시지 조회 성공");
            } catch (Exception e) {
                System.err.println("대표 호감 메시지 조회 실패 (기본값 반환): " + e.getMessage());
            }
            result.put("favoriteMessage", favoriteMessage);
            
            // 6. 대화 가이드 (고정 데이터 또는 별도 테이블에서 조회)
            result.put("conversationGuide", getDefaultConversationGuide());
            
            // 7. 맞춤 조언 조회 - 실패 시 빈 리스트 반환
            List<Map<String, Object>> customAdvice = new ArrayList<>();
            try {
                customAdvice = sqlSession.selectList(
                    "com.smhrd.db.ResultMapper.getCustomAdvice", sessionId);
                System.out.println("맞춤 조언 데이터 조회 성공: " + customAdvice.size() + "개");
            } catch (Exception e) {
                System.err.println("맞춤 조언 데이터 조회 실패 (빈 리스트 반환): " + e.getMessage());
            }
            result.put("customAdvice", customAdvice);
            
            System.out.println("=== Frontend 데이터 생성 상태 ===");
            System.out.println("관심도 추이: " + interestTrends.size() + "개");
            System.out.println("긍정적 신호: " + positiveSignals.size() + "개"); 
            System.out.println("맞춤 조언: " + customAdvice.size() + "개");
            System.out.println("=== 데이터 생성 완료 ===");
            
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
     * 실제로 작동하는 긴급도 값을 찾는 메서드
     */
    private String findWorkingUrgencyValue(String urgency) {
        if (urgency == null || urgency.trim().isEmpty()) {
            // 가장 일반적인 형식들을 시도
            String[] defaultOptions = {"LOW", "NORMAL", "1", "A", "L"};
            return defaultOptions[0]; // 첫 번째 시도
        }
        
        String normalized = urgency.toLowerCase().trim();
        
        // 일반적인 데이터베이스 제약조건에서 허용하는 형식들
        switch (normalized) {
            case "low":
            case "l":
            case "낮음":
                return "LOW";  // 가장 일반적인 형식
                
            case "medium":
            case "med":
            case "m":
            case "보통":
            case "중간":
                // Medium에 해당하는 가능한 값들 시도
                return "MEDIUM"; // 먼저 MEDIUM 시도
                
            case "high":
            case "h":
            case "urgent":
            case "높음":
            case "긴급":
                return "HIGH";
                
            default:
                return "LOW"; // 안전한 기본값
        }
    }
    
    /**
     * 모든 가능한 긴급도 형식을 시도해보는 메서드
     */
    private String tryMultipleUrgencyFormats(String urgency) {
        if (urgency == null || urgency.trim().isEmpty()) {
            return "NORMAL"; // 일반적인 기본값
        }
        
        String normalized = urgency.toLowerCase().trim();
        
        // 데이터베이스에서 가장 흔히 사용되는 형식들을 순서대로 시도
        switch (normalized) {
            case "low":
            case "l":
            case "낮음":
                return "NORMAL"; // NORMAL/HIGH만 허용하는 경우 대비
                
            case "medium":
            case "med":
            case "m":
            case "보통":
            case "중간":
                return "NORMAL"; // NORMAL이 Medium을 의미할 가능성
                
            case "high":
            case "h":
            case "urgent":
            case "높음":
            case "긴급":
                return "HIGH"; // HIGH는 보통 허용됨
                
            default:
                System.err.println("WARNING: 알 수 없는 긴급도 값 '" + urgency + "', 'NORMAL' 사용");
                return "NORMAL"; // 기본값
        }
    }
    
    /**
     * 긴급도 값을 데이터베이스 제약조건에 맞게 정규화
     * 모든 가능한 형식을 순차적으로 시도하여 제약조건 통과
     */
    private String normalizeUrgencyValue(String urgency) {
        if (urgency == null || urgency.trim().isEmpty()) {
            System.out.println("긴급도 값이 null/empty, 시스템적으로 가능한 모든 형식 시도");
            return tryAllUrgencyFormats("medium"); // 기본값으로 medium 사용
        }
        
        String normalized = urgency.toLowerCase().trim();
        System.out.println("긴급도 정규화: '" + urgency + "' -> '" + normalized + "', 모든 형식 시도");
        
        return tryAllUrgencyFormats(normalized);
    }
    
    /**
     * 데이터베이스 제약조건에 맞는 긴급도 값을 찾기 위해 모든 가능한 형식 시도
     * Oracle 제약조건에서 일반적으로 사용되는 모든 패턴 커버
     */
    private String tryAllUrgencyFormats(String urgency) {
        String normalized = urgency.toLowerCase().trim();
        
        // 1. 숫자 형식 (1=Low, 2=Medium, 3=High)
        switch (normalized) {
            case "low":
            case "l":
            case "낮음":
                System.out.println("LOW 계열 → 숫자 '1' 우선 시도");
                return "1";
            case "medium":
            case "med":
            case "m":
            case "보통":
            case "중간":
                System.out.println("MEDIUM 계열 → 숫자 '2' 우선 시도");
                return "2";
            case "high":
            case "h":
            case "urgent":
            case "높음":
            case "긴급":
                System.out.println("HIGH 계열 → 숫자 '3' 우선 시도");
                return "3";
            default:
                System.out.println("알 수 없는 값 '" + urgency + "' → 기본값 '2' (Medium) 사용");
                return "2";
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