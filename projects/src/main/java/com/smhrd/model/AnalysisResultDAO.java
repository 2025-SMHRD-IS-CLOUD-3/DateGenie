package com.smhrd.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.smhrd.db.SqlSessionManager;

/**
 * 분석 결과 데이터베이스 접근 클래스
 * 분석 결과의 저장, 조회, 업데이트를 담당
 */
public class AnalysisResultDAO {
    
    private SqlSessionFactory sqlSessionFactory;
    
    public AnalysisResultDAO() {
        this.sqlSessionFactory = SqlSessionManager.getSqlSessionFactory();
    }
    
    /**
     * 분석 결과를 데이터베이스에 저장
     * @param analysisResult 저장할 분석 결과
     * @return 저장 성공 여부
     */
    public boolean saveAnalysisResult(AnalysisResult analysisResult) {
        SqlSession session = null;
        boolean success = false;
        
        try {
            session = sqlSessionFactory.openSession();
            
            // 기존 결과가 있는지 확인
            AnalysisResult existing = session.selectOne("ResultMapper.getBySessionId", analysisResult.getSessionId());
            
            if (existing != null) {
                // 업데이트
                int result = session.update("ResultMapper.updateAnalysisResult", analysisResult);
                success = (result > 0);
            } else {
                // 새로 삽입
                int result = session.insert("ResultMapper.insertAnalysisResult", analysisResult);
                success = (result > 0);
            }
            
            if (success) {
                session.commit();
                System.out.println("분석 결과 저장 성공 - sessionId: " + analysisResult.getSessionId());
            } else {
                session.rollback();
                System.err.println("분석 결과 저장 실패 - sessionId: " + analysisResult.getSessionId());
            }
            
        } catch (Exception e) {
            System.err.println("분석 결과 저장 중 오류: " + e.getMessage());
            e.printStackTrace();
            if (session != null) {
                session.rollback();
            }
            success = false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        
        return success;
    }
    
    /**
     * 세션 ID로 분석 결과 조회
     * @param sessionId 세션 ID
     * @return 분석 결과 또는 null
     */
    public AnalysisResult getAnalysisResult(String sessionId) {
        SqlSession session = null;
        AnalysisResult result = null;
        
        try {
            session = sqlSessionFactory.openSession();
            result = session.selectOne("ResultMapper.getBySessionId", sessionId);
            
            if (result != null) {
                System.out.println("분석 결과 조회 성공 - sessionId: " + sessionId);
            } else {
                System.out.println("분석 결과 없음 - sessionId: " + sessionId);
            }
            
        } catch (Exception e) {
            System.err.println("분석 결과 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        
        return result;
    }
    
    /**
     * 프론트엔드용 분석 결과 데이터 조회 (포괄적 - 모든 상세 데이터 포함)
     * @param sessionId 세션 ID
     * @return 프론트엔드 형식의 포괄적 분석 데이터
     */
    public Map<String, Object> getAnalysisResultForFrontend(String sessionId) {
        SqlSession session = null;
        Map<String, Object> frontendData = new HashMap<>();
        
        try {
            session = sqlSessionFactory.openSession();
            
            // 1. 메인 분석 결과 조회
            Map<String, Object> mainResult = session.selectOne("ResultMapper.getAnalysisResult", sessionId);
            if (mainResult == null) {
                System.out.println("분석 결과 없음 - sessionId: " + sessionId);
                return new HashMap<>();
            }
            
            // 2. 메인 결과 정보
            Map<String, Object> mainResults = new HashMap<>();
            mainResults.put("successRate", mainResult.get("successRate"));
            mainResults.put("confidenceLevel", mainResult.get("confidenceLevel"));  
            mainResults.put("relationshipStage", mainResult.get("relationshipStage"));
            mainResults.put("heroInsight", mainResult.get("heroInsight"));
            frontendData.put("mainResults", mainResults);
            
            // 3. 관심도 추이 데이터 조회
            List<Map<String, Object>> interestTrends = session.selectList("ResultMapper.getInterestTrends", sessionId);
            if (interestTrends != null && !interestTrends.isEmpty()) {
                frontendData.put("interestTrends", interestTrends);
                System.out.println("관심도 추이 데이터 조회됨: " + interestTrends.size() + "건");
            } else {
                // 기본 추이 데이터 생성
                frontendData.put("interestTrends", generateDefaultTrendData());
            }
            
            // 4. 감정 분석 조회
            Map<String, Object> emotionAnalysis = session.selectOne("ResultMapper.getEmotionAnalysis", sessionId);
            if (emotionAnalysis != null) {
                frontendData.put("emotionAnalysis", emotionAnalysis);
                System.out.println("감정 분석 데이터 조회됨");
            } else {
                // 기본 감정 데이터
                Map<String, Object> defaultEmotion = new HashMap<>();
                defaultEmotion.put("positive", 60.0);
                defaultEmotion.put("neutral", 25.0);
                defaultEmotion.put("negative", 15.0);
                frontendData.put("emotionAnalysis", defaultEmotion);
            }
            
            // 5. 긍정적 신호 조회
            List<Map<String, Object>> positiveSignals = session.selectList("ResultMapper.getPositiveSignals", sessionId);
            if (positiveSignals != null && !positiveSignals.isEmpty()) {
                frontendData.put("positiveSignals", positiveSignals);
                System.out.println("긍정 신호 조회됨: " + positiveSignals.size() + "건");
            } else {
                frontendData.put("positiveSignals", new ArrayList<>());
            }
            
            // 6. 대표 호감 메시지 조회
            Map<String, Object> favoriteMessage = session.selectOne("ResultMapper.getFavoriteMessage", sessionId);
            if (favoriteMessage != null) {
                // 날짜 형식 개선
                String dateStr = favoriteMessage.get("messageDate") != null ? 
                    favoriteMessage.get("messageDate").toString() : "날짜 정보 없음";
                favoriteMessage.put("date", dateStr);
                
                frontendData.put("favoriteMessage", favoriteMessage);
                System.out.println("대표 메시지 조회됨");
            } else {
                // 기본 메시지
                Map<String, Object> defaultMessage = new HashMap<>();
                defaultMessage.put("text", "분석된 메시지가 없습니다.");
                defaultMessage.put("confidence", 0);
                defaultMessage.put("date", "날짜 없음");
                defaultMessage.put("reason", "메시지 분석 중");
                frontendData.put("favoriteMessage", defaultMessage);
            }
            
            // 7. 맞춤 조언 조회
            List<Map<String, Object>> customAdvice = session.selectList("ResultMapper.getCustomAdvice", sessionId);
            if (customAdvice != null && !customAdvice.isEmpty()) {
                frontendData.put("customAdvice", customAdvice);
                System.out.println("맞춤 조언 조회됨: " + customAdvice.size() + "건");
            } else {
                frontendData.put("customAdvice", generateDefaultAdvice());
            }
            
            // 8. 실전 대화 가이드 생성 (DB에 없는 경우 기본값)
            frontendData.put("conversationGuides", generateDefaultConversationGuide());
            
            System.out.println("포괄적 프론트엔드 데이터 조회 완료 - sessionId: " + sessionId);
            return frontendData;
            
        } catch (Exception e) {
            System.err.println("포괄적 데이터 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * 기본 추이 데이터 생성
     */
    private List<Map<String, Object>> generateDefaultTrendData() {
        List<Map<String, Object>> trendData = new ArrayList<>();
        String[] dates = {"8월 13일", "8월 14일", "8월 15일", "8월 16일", "8월 17일", 
                         "8월 18일", "8월 19일", "8월 20일", "8월 21일", "8월 22일"};
        double[] values = {45, 52, 48, 58, 62, 59, 67, 71, 69, 76};
        
        for (int i = 0; i < dates.length; i++) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("date", dates[i]);
            trend.put("value", values[i]);
            trend.put("messageCount", 5 + (int)(Math.random() * 10));
            trend.put("avgResponseTime", 10 + (Math.random() * 30));
            trend.put("emojiCount", (int)(Math.random() * 5));
            trendData.add(trend);
        }
        return trendData;
    }
    
    /**
     * 기본 조언 생성
     */
    private List<Map<String, Object>> generateDefaultAdvice() {
        List<Map<String, Object>> advice = new ArrayList<>();
        
        Map<String, Object> advice1 = new HashMap<>();
        advice1.put("title", "현재 단계에서는");
        advice1.put("content", "자연스럽고 부담스럽지 않은 대화를 이어가는 것이 좋습니다.");
        advice1.put("type", "current_stage");
        advice.add(advice1);
        
        Map<String, Object> advice2 = new HashMap<>();
        advice2.put("title", "다음 단계 준비");
        advice2.put("content", "공통 관심사를 찾아 가벼운 활동을 함께 할 수 있는 기회를 만들어보세요.");
        advice2.put("type", "next_step");
        advice.add(advice2);
        
        Map<String, Object> advice3 = new HashMap<>();
        advice3.put("title", "주의사항");
        advice3.put("content", "너무 급하게 관계를 발전시키려 하지 말고, 상대방의 페이스를 존중하세요.");
        advice3.put("type", "caution");
        advice.add(advice3);
        
        return advice;
    }
    
    /**
     * 기본 대화 가이드 생성
     */
    private List<Map<String, Object>> generateDefaultConversationGuide() {
        List<Map<String, Object>> guides = new ArrayList<>();
        
        Map<String, Object> guide1 = new HashMap<>();
        guide1.put("type", "일상 공유");
        guide1.put("text", "오늘 하루 어때? 나는 좋은 일이 있어서 기분이 좋아 ㅎㅎ");
        guide1.put("timing", "오후 6-9시 추천");
        guides.add(guide1);
        
        Map<String, Object> guide2 = new HashMap<>();
        guide2.put("type", "관심사 질문");
        guide2.put("text", "요즘 뭐 재밌는 거 있어? 나도 새로운 거 찾고 있어서~");
        guide2.put("timing", "저녁 시간대");
        guides.add(guide2);
        
        Map<String, Object> guide3 = new HashMap<>();
        guide3.put("type", "가벼운 제안");
        guide3.put("text", "날씨 좋은 날 산책이라도 한번 해볼까? 부담없이~");
        guide3.put("timing", "주말 오전");
        guides.add(guide3);
        
        return guides;
    }
    
    /**
     * 사용자의 분석 히스토리 조회
     * @param userEmail 사용자 이메일
     * @return 분석 히스토리 목록
     */
    public List<Map<String, Object>> getUserAnalysisHistory(String userEmail) {
        SqlSession session = null;
        List<Map<String, Object>> history = null;
        
        try {
            session = sqlSessionFactory.openSession();
            
            Map<String, Object> params = new HashMap<>();
            params.put("userEmail", userEmail);
            params.put("limit", 20); // 최근 20개만
            
            history = session.selectList("ResultMapper.getUserHistory", params);
            
            System.out.println("사용자 히스토리 조회 - 사용자: " + userEmail + ", 건수: " + (history != null ? history.size() : 0));
            
        } catch (Exception e) {
            System.err.println("사용자 히스토리 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        
        return history;
    }
    
    /**
     * 분석 결과 삭제
     * @param sessionId 세션 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteAnalysisResult(String sessionId) {
        SqlSession session = null;
        boolean success = false;
        
        try {
            session = sqlSessionFactory.openSession();
            int result = session.delete("ResultMapper.deleteBySessionId", sessionId);
            
            if (result > 0) {
                session.commit();
                success = true;
                System.out.println("분석 결과 삭제 성공 - sessionId: " + sessionId);
            } else {
                System.out.println("삭제할 분석 결과 없음 - sessionId: " + sessionId);
            }
            
        } catch (Exception e) {
            System.err.println("분석 결과 삭제 중 오류: " + e.getMessage());
            e.printStackTrace();
            if (session != null) {
                session.rollback();
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
        
        return success;
    }
    
    /**
     * 분석 통계 조회 (대시보드용)
     * @param userEmail 사용자 이메일
     * @return 분석 통계
     */
    public Map<String, Object> getAnalysisStats(String userEmail) {
        SqlSession session = null;
        Map<String, Object> stats = new HashMap<>();
        
        try {
            session = sqlSessionFactory.openSession();
            
            // 총 분석 횟수
            Integer totalCount = session.selectOne("ResultMapper.getTotalAnalysisCount", userEmail);
            stats.put("totalAnalysis", totalCount != null ? totalCount : 0);
            
            // 최근 분석 일시
            Timestamp lastAnalysis = session.selectOne("ResultMapper.getLastAnalysisTime", userEmail);
            if (lastAnalysis != null) {
                stats.put("lastAnalysis", lastAnalysis);
                stats.put("lastAnalysisFormatted", lastAnalysis.toString());
            } else {
                stats.put("lastAnalysis", null);
                stats.put("lastAnalysisFormatted", "-");
            }
            
            // 평균 성공률
            Double avgSuccessRate = session.selectOne("ResultMapper.getAverageSuccessRate", userEmail);
            stats.put("averageScore", avgSuccessRate != null ? Math.round(avgSuccessRate) : 0);
            
            System.out.println("분석 통계 조회 - 사용자: " + userEmail + ", 총 분석: " + stats.get("totalAnalysis"));
            
        } catch (Exception e) {
            System.err.println("분석 통계 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            // 기본값 설정
            stats.put("totalAnalysis", 0);
            stats.put("lastAnalysisFormatted", "-");
            stats.put("averageScore", 0);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        
        return stats;
    }
}