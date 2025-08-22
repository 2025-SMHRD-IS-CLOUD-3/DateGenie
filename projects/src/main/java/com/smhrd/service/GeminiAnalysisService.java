package com.smhrd.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smhrd.model.AnalysisResult;
import com.smhrd.util.GeminiConfig;

/**
 * Gemini API를 활용한 대화 분석 서비스
 * 
 * 주요 기능:
 * 1. 구조화된 프롬프트로 Gemini API 호출
 * 2. JSON 응답 파싱 및 검증
 * 3. 데이터베이스 저장 준비
 * 4. 에러 처리 및 재시도 로직
 */
public class GeminiAnalysisService {
    
    private static final String PROMPT_TEMPLATE_PATH = "src/main/resources/prompt_templates/analysis_prompt_template.txt";
    
    private final Gson gson;
    
    public GeminiAnalysisService() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create();
    }
    
    /**
     * 대화 내용을 분석하여 결과 반환
     * 
     * @param conversationData 분석할 대화 데이터
     * @param userId 사용자 ID
     * @param partnerName 상대방 이름
     * @return 분석 결과 객체
     * @throws AnalysisException 분석 실패 시
     */
    public AnalysisResult analyzeConversation(String conversationData, String userId, String partnerName) 
            throws AnalysisException {
        
        try {
            // 1. 프롬프트 생성
            String prompt = buildAnalysisPrompt(conversationData, partnerName);
            
            // 2. Gemini API 호출
            String apiResponse = callGeminiAPI(prompt);
            
            // 3. 응답 파싱 및 검증
            AnalysisResult result = parseAndValidateResponse(apiResponse, userId, partnerName);
            
            // 4. 세션 ID 생성
            result.setSessionId(generateSessionId());
            
            return result;
            
        } catch (Exception e) {
            throw new AnalysisException("대화 분석 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 프롬프트 템플릿과 대화 데이터를 결합하여 완성된 프롬프트 생성
     */
    private String buildAnalysisPrompt(String conversationData, String partnerName) throws Exception {
        
        // 프롬프트 템플릿 로드
        String template = loadPromptTemplate();
        
        // 대화 데이터를 JSON 형태로 정리
        String formattedConversation = formatConversationData(conversationData);
        
        // 프롬프트 완성
        String fullPrompt = template + "\n\n## 분석 대상 대화\n" + 
                           "상대방 이름: " + partnerName + "\n" +
                           "대화 내용:\n" + formattedConversation + 
                           "\n\n위 대화를 분석하여 정확한 JSON 형식으로 응답해주세요.";
        
        return fullPrompt;
    }
    
    /**
     * 프롬프트 템플릿 파일 로드
     */
    private String loadPromptTemplate() throws Exception {
        try {
            return new String(Files.readAllBytes(Paths.get(PROMPT_TEMPLATE_PATH)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 파일이 없는 경우 기본 프롬프트 사용
            return getDefaultPrompt();
        }
    }
    
    /**
     * 대화 데이터를 분석에 적합한 형태로 포맷팅
     */
    private String formatConversationData(String conversationData) {
        // 대화 데이터가 JSON이면 파싱해서 정리
        // 아니면 텍스트 그대로 사용
        try {
            // JSON 파싱 시도
            Map<?, ?> parsed = gson.fromJson(conversationData, Map.class);
            return gson.toJson(parsed);
        } catch (Exception e) {
            // JSON이 아니면 텍스트로 처리
            return conversationData;
        }
    }
    
    /**
     * Gemini API 호출
     */
    private String callGeminiAPI(String prompt) throws Exception {
        
        URL url = new URL(GeminiConfig.getApiUrl() + "?key=" + GeminiConfig.getApiKey());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            // 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(GeminiConfig.getConnectTimeout());
            conn.setReadTimeout(GeminiConfig.getReadTimeout());
            
            // 요청 본문 생성
            String requestBody = createGeminiRequestBody(prompt);
            
            // 요청 전송
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(requestBody);
                writer.flush();
            }
            
            // 응답 읽기
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                return readResponse(conn.getInputStream());
            } else {
                String errorResponse = readResponse(conn.getErrorStream());
                throw new Exception("Gemini API 호출 실패 (코드: " + responseCode + "): " + errorResponse);
            }
            
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * Gemini API 요청 본문 생성
     */
    private String createGeminiRequestBody(String prompt) {
        
        // Gemini API 요청 형식에 맞게 구성 (설정 파일 기반)
        String requestJson = "{\n" +
            "  \"contents\": [{\n" +
            "    \"parts\": [{\n" +
            "      \"text\": " + gson.toJson(prompt) + "\n" +
            "    }]\n" +
            "  }],\n" +
            "  \"generationConfig\": {\n" +
            "    \"temperature\": " + GeminiConfig.getTemperature() + ",\n" +
            "    \"topK\": " + GeminiConfig.getTopK() + ",\n" +
            "    \"topP\": " + GeminiConfig.getTopP() + ",\n" +
            "    \"maxOutputTokens\": " + GeminiConfig.getMaxOutputTokens() + ",\n" +
            "    \"candidateCount\": 1\n" +
            "  }\n" +
            "}";
        
        return requestJson;
    }
    
    /**
     * HTTP 응답 스트림에서 텍스트 읽기
     */
    private String readResponse(java.io.InputStream inputStream) throws Exception {
        
        StringBuilder response = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }
        
        return response.toString();
    }
    
    /**
     * Gemini API 응답에서 실제 분석 결과 추출 및 검증
     */
    private AnalysisResult parseAndValidateResponse(String apiResponse, String userId, String partnerName) 
            throws Exception {
        
        try {
            // Gemini API 응답 파싱
            Map<?, ?> responseJson = gson.fromJson(apiResponse, Map.class);
            
            // candidates[0].content.parts[0].text에서 실제 응답 추출
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> candidates = 
                (java.util.List<Map<String, Object>>) responseJson.get("candidates");
            
            if (candidates == null || candidates.isEmpty()) {
                throw new Exception("Gemini API 응답에서 candidates를 찾을 수 없습니다");
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> parts = 
                (java.util.List<Map<String, Object>>) content.get("parts");
            
            String analysisText = (String) parts.get(0).get("text");
            
            // JSON 응답에서 실제 분석 결과 추출
            String cleanJsonText = extractJsonFromResponse(analysisText);
            
            // 분석 결과 객체로 변환
            AnalysisResult result = gson.fromJson(cleanJsonText, AnalysisResult.class);
            
            // 기본 정보 설정
            result.setUserId(userId);
            result.setPartnerName(partnerName);
            
            // 데이터 검증
            validateAnalysisResult(result);
            
            return result;
            
        } catch (Exception e) {
            throw new Exception("Gemini API 응답 파싱 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 응답 텍스트에서 JSON 부분만 추출
     */
    private String extractJsonFromResponse(String responseText) {
        
        // ```json ... ``` 형태로 감싸져 있는 경우 처리
        String jsonPattern = "```json\\s*\\n([\\s\\S]*?)\\n\\s*```";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(jsonPattern);
        java.util.regex.Matcher matcher = pattern.matcher(responseText);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // { ... } 형태의 JSON 찾기
        int startIndex = responseText.indexOf("{");
        int lastIndex = responseText.lastIndexOf("}");
        
        if (startIndex != -1 && lastIndex != -1 && startIndex < lastIndex) {
            return responseText.substring(startIndex, lastIndex + 1);
        }
        
        throw new RuntimeException("응답에서 유효한 JSON을 찾을 수 없습니다: " + responseText);
    }
    
    /**
     * 분석 결과 데이터 검증
     */
    private void validateAnalysisResult(AnalysisResult result) throws Exception {
        
        // 필수 필드 검증
        if (result.getMainResults() == null) {
            throw new Exception("mainResults가 없습니다");
        }
        
        // 수치 범위 검증
        double successRate = result.getMainResults().getSuccessRate();
        if (successRate < 0 || successRate > 100) {
            throw new Exception("successRate는 0-100 범위여야 합니다: " + successRate);
        }
        
        // 감정 분석 합계 검증
        if (result.getEmotionAnalysis() != null) {
            double total = result.getEmotionAnalysis().getPositive() + 
                          result.getEmotionAnalysis().getNeutral() + 
                          result.getEmotionAnalysis().getNegative();
            
            if (Math.abs(total - 100.0) > 0.1) {
                throw new Exception("감정 분석 비율 합계가 100%가 아닙니다: " + total);
            }
        }
        
        // 기타 필수 검증 로직...
    }
    
    /**
     * 세션 ID 생성
     */
    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
    
    /**
     * 기본 프롬프트 (템플릿 파일이 없는 경우)
     */
    private String getDefaultPrompt() {
        return "당신은 썸 관계 전문 분석가입니다. 제공된 대화를 분석하여 JSON 형식으로 결과를 제공해주세요.";
    }
    
    /**
     * 분석 예외 클래스
     */
    public static class AnalysisException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public AnalysisException(String message) {
            super(message);
        }
        
        public AnalysisException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}