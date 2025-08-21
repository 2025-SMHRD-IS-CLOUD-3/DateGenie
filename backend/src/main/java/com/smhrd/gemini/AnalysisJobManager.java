package com.smhrd.gemini;

import com.google.gson.Gson;

import com.smhrd.model.ResultDAO;
import com.smhrd.model.ResultDTO;
import com.smhrd.gemini.GeminiService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalysisJobManager {

    /**
     * 개별 분석 작업을 관리하는 내부 클래스
     */
    public static class Job {
        public final String id;
        public final String fileContent;  // 파일 ID 대신 파일 내용 저장
        public volatile String status = "running";
        public volatile int percent = 0;
        public volatile String message = "started";
        public volatile Map<String, Object> result;

        Job(String id, String fileContent) {
            this.id = id;
            this.fileContent = fileContent;
        }
    }

    // 진행 중인 분석 작업을 메모리에 저장하는 맵
    private static final Map<String, Job> JOBS = new ConcurrentHashMap<>();

    // 데몬 스레드 팩토리
    private static final ThreadFactory DAEMON_THREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "analysis-job-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    };
    private static final ScheduledExecutorService EXEC = Executors.newScheduledThreadPool(2, DAEMON_THREAD_FACTORY);

    /**
     * 분석 프롬프트 생성
     */
    private static String buildAnalysisPrompt(String fileContent) {
        return "다음 대화 내용을 분석해서 **반드시 JSON 형태로만** 결과를 제공해주세요. " +
                "다른 설명 없이 오직 JSON만 반환해주세요.\n\n" +
                
                "JSON 형식:\n" +
                "{\n" +
                "  \"emotionScore\": 숫자 (0-100),\n" +
                "  \"emotionSentence\": \"감정 분석 한 줄 요약\",\n" +
                "  \"personality\": \"성격 분석\",\n" +
                "  \"advice\": \"관계 발전을 위한 조언\",\n" +
                "  \"targetName\": \"상대방 이름 (파악되지 않으면 '상대방')\",\n" +
                "  \"firstTalk\": \"먼저 대화를 시작한 사람\",\n" +
                "  \"emotionCount\": 숫자 (총 감정 표현 횟수),\n" +
                "  \"talkBalance\": \"대화 비율 (예: '45:55')\",\n" +
                "  \"talkSpeed\": \"평균 응답 속도\",\n" +
                "  \"talkCount\": 숫자 (총 대화 횟수)\n" +
                "}\n\n" +
                
                "대화 내용:\n" + fileContent + "\n\n" +
                
                "중요: 위 JSON 형식을 정확히 지켜서 응답해주세요. " +
                "JSON 외의 다른 텍스트는 포함하지 마세요.";
    }

    /**
     * Gemini API 호출
     */
    private static String callGeminiAPI(String fileContent) {
        try {
            String prompt = buildAnalysisPrompt(fileContent);
            
             
          // 실제 Gemini 서비스 호출
             GeminiService geminiService = new GeminiService();
             String geminiResponse = geminiService.generateContent(prompt);
             
             System.out.println("=== Gemini 원본 응답 ===");
             System.out.println(geminiResponse);
             System.out.println("=====================");
             
             return geminiResponse;            
            
//            // 임시 더미 응답
//            return "{\n" +
//                "  \"emotionScore\": 85,\n" +
//                "  \"emotionSentence\": \"대화에서 긍정적인 감정 표현이 많이 발견됩니다.\",\n" +
//                "  \"personality\": \"상대방을 배려하고 이해하려는 성향이 강합니다.\",\n" +
//                "  \"advice\": \"현재의 소통 방식을 유지하며 더욱 깊은 대화를 나누어보세요.\",\n" +
//                "  \"targetName\": \"분석대상\",\n" +
//                "  \"firstTalk\": \"사용자\",\n" +
//                "  \"emotionCount\": 120,\n" +
//                "  \"talkBalance\": \"48:52\",\n" +
//                "  \"talkSpeed\": \"평균 3분 이내\",\n" +
//                "  \"talkCount\": 380\n" +
//                "}";
            
        } catch (Exception e) {
            System.err.println("Gemini API 호출 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Gemini 분석 실패", e);
        }
    }

    /**
     * Gemini 응답 파싱
     */
    private static Map<String, Object> parseGeminiResponse(String geminiResponse) {
        Map<String, Object> result = new HashMap<>();
        
        try {
        	String cleanJson = extractJsonFromResponse(geminiResponse);
        	
            Gson gson = new Gson();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsedResult = gson.fromJson(cleanJson, Map.class);
            
            result.put("emotionScore", getIntValue(parsedResult, "emotionScore", 0));
            result.put("emotionSentence", getStringValue(parsedResult, "emotionSentence", "분석 결과 없음"));
            result.put("personality", getStringValue(parsedResult, "personality", "성격 분석 불가"));
            result.put("advice", getStringValue(parsedResult, "advice", "조언을 제공할 수 없습니다"));
            result.put("targetName", getStringValue(parsedResult, "targetName", "상대방"));
            result.put("displayOrder", 1);
            result.put("firstTalk", getStringValue(parsedResult, "firstTalk", "알 수 없음"));
            result.put("emotionCount", getIntValue(parsedResult, "emotionCount", 0));
            result.put("talkBalance", getStringValue(parsedResult, "talkBalance", "50:50"));
            result.put("talkSpeed", getStringValue(parsedResult, "talkSpeed", "알 수 없음"));
            result.put("talkCount", getIntValue(parsedResult, "talkCount", 0));
            
        } catch (Exception e) {
            System.err.println("Gemini 응답 파싱 오류: " + e.getMessage());
            e.printStackTrace();
            
            // 기본값으로 채우기
            result.put("emotionScore", 0);
            result.put("emotionSentence", "JSON 파싱 실패");
            result.put("personality", "분석할 수 없음");
            result.put("advice", "다시 시도해주세요");
            result.put("targetName", "알 수 없음");
            result.put("displayOrder", 1);
            result.put("firstTalk", "알 수 없음");
            result.put("emotionCount", 0);
            result.put("talkBalance", "50:50");
            result.put("talkSpeed", "알 수 없음");
            result.put("talkCount", 0);
        }
        
        return result;
    }

    /**
     * 헬퍼 메서드들
     */
    private static String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return (value != null) ? value.toString() : defaultValue;
    }

    private static Integer getIntValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 응답에서 JSON 부분만 추출하는 헬퍼 메서드
     */
    private static String extractJsonFromResponse(String response) {
        int startIndex = response.indexOf('{');
        int endIndex = response.lastIndexOf('}');
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }
        
        throw new RuntimeException("JSON 형태를 찾을 수 없습니다");
    }
    

    /**
     * 분석 Job 시작 (파일 내용 직접 받기)
     */
    public static String start(String fileContent, String email) {
        String id = UUID.randomUUID().toString();
        Job job = new Job(id, fileContent);
        JOBS.put(id, job);

        EXEC.submit(() -> {
            try {
                // 1. 파일 내용 검증
                job.percent = 10;
                job.message = "파일 내용 확인 중...";
                
                if (fileContent.trim().isEmpty()) {
                    throw new Exception("분석할 파일 내용이 없습니다.");
                }
                
                // 2. Gemini API 호출
                job.percent = 30;
                job.message = "AI 분석 중...";
                String geminiResponse = callGeminiAPI(fileContent);
                
                // 3. 응답 파싱
                job.percent = 80;
                job.message = "결과 처리 중...";
                Map<String, Object> analysisResult = parseGeminiResponse(geminiResponse);
                job.result = analysisResult;

                // 4. DB 저장
                job.percent = 90;
                job.message = "결과 정리 중...";
                
                // 임시 저장 성공 처리
                System.out.println("분석 완료! (DB 저장 건너뜀) Email: " + email);
                System.out.println("Gemini 분석 결과: " + analysisResult);
                
                // 실제 저장 하는 부분
				/*
				 * ResultDTO resultDTO = new ResultDTO(); resultDTO.setEmail(email);
				 * resultDTO.setEmotionScore((Integer) analysisResult.get("emotionScore"));
				 * resultDTO.setEmotionSentence((String) analysisResult.get("emotionSentence"));
				 * resultDTO.setPersonality((String) analysisResult.get("personality"));
				 * resultDTO.setAdvice((String) analysisResult.get("advice"));
				 * resultDTO.setTargetName((String) analysisResult.get("targetName"));
				 * resultDTO.setDisplayOrder((Integer) analysisResult.get("displayOrder"));
				 * resultDTO.setFirstTalk((String) analysisResult.get("firstTalk"));
				 * resultDTO.setEmotionCount((Integer) analysisResult.get("emotionCount"));
				 * resultDTO.setTalkBalance((String) analysisResult.get("talkBalance"));
				 * resultDTO.setTalkSpeed((String) analysisResult.get("talkSpeed"));
				 * resultDTO.setTalkCount((Integer) analysisResult.get("talkCount"));
				 * 
				 * 
				 * ResultDAO dao = new ResultDAO(); int cnt = dao.insertResult(resultDTO);
				 */
				 
                
                
				/*
				 * if (cnt > 0) { System.out.println("분석 결과 DB 저장 성공! Email: " + email); } else
				 * { throw new Exception("DB 저장 실패"); }
				 */

                // 5. 완료
                job.percent = 100;
                job.status = "done";
                job.message = "complete";

            } catch (Exception e) {
                e.printStackTrace();
                job.status = "error";
                job.message = e.getMessage();
                System.err.println("분석 작업 실패 - JobId: " + id + ", Error: " + e.getMessage());
            }
        });

        return id;
    }

    /**
     * Job 정보 가져오기
     */
    public static Job get(String id) {
        return JOBS.get(id);
    }
	
}