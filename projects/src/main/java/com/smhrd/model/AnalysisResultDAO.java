package com.smhrd.model;

import java.sql.Timestamp;
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
     * 프론트엔드용 분석 결과 데이터 조회
     * @param sessionId 세션 ID
     * @return 프론트엔드 형식의 분석 데이터
     */
    public Map<String, Object> getAnalysisResultForFrontend(String sessionId) {
        AnalysisResult result = getAnalysisResult(sessionId);
        
        if (result == null) {
            System.out.println("프론트엔드 데이터 변환 실패 - 분석 결과 없음: " + sessionId);
            return new HashMap<>();
        }
        
        try {
            Map<String, Object> frontendData = result.toFrontendFormat();
            System.out.println("프론트엔드 데이터 변환 성공 - sessionId: " + sessionId + ", 데이터 크기: " + frontendData.size());
            return frontendData;
            
        } catch (Exception e) {
            System.err.println("프론트엔드 데이터 변환 중 오류: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
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