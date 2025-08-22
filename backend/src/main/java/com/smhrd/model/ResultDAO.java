package com.smhrd.model;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.smhrd.db.SqlSessionManager;


public class ResultDAO {

	// DB에 접근할 수 있도록 해주는 SqlSessionFactory
	SqlSessionFactory sqlSessionFactory = SqlSessionManager.getSqlSessionFactory();
	
    /**
     * 분석 결과를 DB에 저장하는 메서드
     * @param resultDTO - 저장할 분석 결과 데이터
     * @return int - 저장 성공시 1, 실패시 0
     */
    public int insertResult(ResultDTO resultDTO) {
        // 1. SqlSession 생성 (auto commit 활성화)
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        
        try {
            // 2. insert 쿼리 실행
            // mapper의 namespace + id = "com.smhrd.db.ResultMapper.insertResult"
            int cnt = sqlSession.insert("com.smhrd.db.ResultMapper.insertResult", resultDTO);
            
            System.out.println("분석 결과 DB 저장 결과: " + cnt + "행 영향받음");
            
            return cnt;
            
        } catch (Exception e) {
            System.err.println("ResultDAO.insertResult 오류: " + e.getMessage());
            e.printStackTrace();
            return 0;
            
        } finally {
            // 3. SqlSession 닫기
            sqlSession.close();
        }
    }
    
    /**
     * 특정 이메일의 분석 결과 조회
     * @param email - 조회할 사용자 이메일
     * @return ResultDTO - 분석 결과, 없으면 null
     */
    public ResultDTO selectResultByEmail(String email) {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        
        try {
            ResultDTO result = sqlSession.selectOne("com.smhrd.db.ResultMapper.selectResultByEmail", email);
            return result;
            
        } catch (Exception e) {
            System.err.println("ResultDAO.selectResultByEmail 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
            
        } finally {
            sqlSession.close();
        }
    }
    
    // 분석 횟수 조회
    public int getAnalysisCountByEmail(String email) {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        try {
            Integer count = sqlSession.selectOne("com.smhrd.db.ResultMapper.getAnalysisCountByEmail", email);
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            sqlSession.close();
        }
    }
	
	
	
}
