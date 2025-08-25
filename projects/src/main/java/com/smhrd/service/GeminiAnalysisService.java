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
            .disableHtmlEscaping()
            .setLenient()
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
            System.out.println("=== GeminiAnalysisService.analyzeConversation 시작 ===");
            System.out.println("conversationData 길이: " + (conversationData != null ? conversationData.length() : 0));
            System.out.println("userId: " + userId);
            System.out.println("partnerName: " + partnerName);
            
            // 입력 데이터 검증
            if (conversationData == null || conversationData.trim().isEmpty()) {
                throw new AnalysisException("대화 데이터가 없습니다");
            }
            if (userId == null || userId.trim().isEmpty()) {
                throw new AnalysisException("사용자 ID가 없습니다");
            }
            
            // 1. 프롬프트 생성
            System.out.println("1. 프롬프트 생성 시작");
            String prompt = buildAnalysisPrompt(conversationData, partnerName);
            System.out.println("1-1. 프롬프트 생성 완료 - 길이: " + prompt.length());
            
            // 2. Gemini API 호출
            System.out.println("2. Gemini API 호출 시작");
            String apiResponse = callGeminiAPI(prompt);
            System.out.println("2-1. Gemini API 호출 완료 - 응답 길이: " + (apiResponse != null ? apiResponse.length() : 0));
            
            // 3. 응답 파싱 및 검증
            System.out.println("3. 응답 파싱 및 검증 시작");
            AnalysisResult result = parseAndValidateResponse(apiResponse, userId, partnerName);
            System.out.println("3-1. 응답 파싱 및 검증 완료");
            
            // 4. 세션 ID 생성
            System.out.println("4. 세션 ID 생성");
            result.setSessionId(generateSessionId());
            System.out.println("4-1. 세션 ID 생성 완료: " + result.getSessionId());
            
            System.out.println("=== GeminiAnalysisService.analyzeConversation 완료 ===");
            return result;
            
        } catch (Exception e) {
            System.err.println("=== GeminiAnalysisService 에러 발생 ===");
            System.err.println("에러 타입: " + e.getClass().getSimpleName());
            System.err.println("에러 메시지: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("원인: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
            System.err.println("스택 트레이스:");
            e.printStackTrace();
            
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
            System.out.println("=== 프롬프트 템플릿 로드 시도 ===");
            System.out.println("템플릿 경로: " + PROMPT_TEMPLATE_PATH);
            
            // 클래스패스에서 리소스 로드 시도
            java.io.InputStream inputStream = getClass().getClassLoader().getResourceAsStream("prompt_templates/analysis_prompt_template.txt");
            if (inputStream != null) {
                String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                inputStream.close();
                System.out.println("클래스패스에서 템플릿 로드 성공 - 길이: " + template.length());
                return template;
            }
            
            // 파일 시스템에서 로드 시도 (fallback)
            String template = new String(Files.readAllBytes(Paths.get(PROMPT_TEMPLATE_PATH)), StandardCharsets.UTF_8);
            System.out.println("파일시스템에서 템플릿 로드 성공 - 길이: " + template.length());
            return template;
            
        } catch (Exception e) {
            System.err.println("프롬프트 템플릿 로드 실패: " + e.getMessage());
            System.err.println("기본 프롬프트 사용");
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
            
            // 디버그 로깅
            System.out.println("=== GEMINI API 응답 디버그 ===");
            System.out.println("원본 응답 텍스트 (처음 500자):");
            System.out.println(analysisText.length() > 500 ? analysisText.substring(0, 500) + "..." : analysisText);
            System.out.println("\n추출된 JSON 텍스트:");
            System.out.println(cleanJsonText);
            System.out.println("=== 디버그 끝 ===");
            
            // 분석 결과 객체로 변환
            System.out.println("=== JSON 파싱 시도 ===");
            System.out.println("파싱할 JSON 길이: " + cleanJsonText.length());
            System.out.println("파싱할 JSON 미리보기 (처음 300자): " + 
                (cleanJsonText.length() > 300 ? cleanJsonText.substring(0, 300) + "..." : cleanJsonText));
            
            AnalysisResult result;
            try {
                result = gson.fromJson(cleanJsonText, AnalysisResult.class);
                System.out.println("JSON 파싱 성공!");
            } catch (com.google.gson.JsonSyntaxException jsonError) {
                System.err.println("JSON 구문 오류: " + jsonError.getMessage());
                System.err.println("문제가 된 JSON: " + cleanJsonText);
                throw new Exception("JSON 구문이 올바르지 않습니다: " + jsonError.getMessage(), jsonError);
            }
            
            // 기본 정보 설정
            result.setUserId(userId);
            result.setPartnerName(partnerName);
            
            // 데이터 검증
            validateAnalysisResult(result);
            
            return result;
            
        } catch (Exception e) {
            System.err.println("=== GEMINI API 파싱 에러 상세 정보 ===");
            System.err.println("에러 타입: " + e.getClass().getSimpleName());
            System.err.println("에러 메시지: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("원인: " + e.getCause().getMessage());
            }
            System.err.println("=== 파싱 에러 정보 끝 ===");
            throw new Exception("Gemini API 응답 파싱 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 응답 텍스트에서 JSON 부분만 추출
     */
    private String extractJsonFromResponse(String responseText) {
        System.out.println("=== JSON 추출 시작 ===");
        System.out.println("응답 텍스트 길이: " + responseText.length());
        System.out.println("응답 미리보기 (처음 200자): " + 
            (responseText.length() > 200 ? responseText.substring(0, 200) + "..." : responseText));
        
        // 1. ```json ... ``` 형태 처리 (여러 패턴 지원)
        String[] jsonPatterns = {
            "```json\\s*\\n([\\s\\S]*?)\\n\\s*```",  // ```json\n...\n```
            "```json\\s*([\\s\\S]*?)```",           // ```json...```
            "```\\s*\\n?([\\s\\S]*?)\\n\\s*```"     // ```\n...\n```
        };
        
        for (String jsonPattern : jsonPatterns) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(jsonPattern, java.util.regex.Pattern.MULTILINE | java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher matcher = pattern.matcher(responseText);
            
            if (matcher.find()) {
                String extracted = matcher.group(1).trim();
                System.out.println("패턴으로 JSON 추출 성공: " + jsonPattern);
                System.out.println("추출된 JSON 길이: " + extracted.length());
                return extracted;
            }
        }
        
        // 2. 중첩된 중괄호를 고려한 JSON 찾기
        int startIndex = -1;
        int endIndex = -1;
        int braceCount = 0;
        
        for (int i = 0; i < responseText.length(); i++) {
            char c = responseText.charAt(i);
            
            if (c == '{') {
                if (startIndex == -1) {
                    startIndex = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && startIndex != -1) {
                    endIndex = i;
                    break;
                }
            }
        }
        
        if (startIndex != -1 && endIndex != -1) {
            String extracted = responseText.substring(startIndex, endIndex + 1);
            System.out.println("중괄호 매칭으로 JSON 추출 성공");
            System.out.println("추출된 JSON 길이: " + extracted.length());
            return extracted;
        }
        
        // 3. 마지막 수단: 간단한 { ... } 찾기
        startIndex = responseText.indexOf("{");
        int lastIndex = responseText.lastIndexOf("}");
        
        if (startIndex != -1 && lastIndex != -1 && startIndex < lastIndex) {
            String extracted = responseText.substring(startIndex, lastIndex + 1);
            System.out.println("기본 방식으로 JSON 추출 시도");
            System.out.println("추출된 JSON 길이: " + extracted.length());
            return extracted;
        }
        
        System.err.println("모든 JSON 추출 방법 실패");
        System.err.println("응답 전문: " + responseText);
        throw new RuntimeException("응답에서 유효한 JSON을 찾을 수 없습니다. 응답 길이: " + responseText.length());
    }
    
    /**
     * 분석 결과 데이터 검증
     */
    private void validateAnalysisResult(AnalysisResult result) throws Exception {
        
        // 결과 객체 자체 검증
        if (result == null) {
            throw new Exception("분석 결과 객체가 null입니다");
        }
        
        // 디버그 로깅
        System.out.println("=== 분석 결과 검증 디버그 ===");
        System.out.println("result 객체 타입: " + result.getClass().getSimpleName());
        System.out.println("mainResults: " + result.getMainResults());
        System.out.println("emotionAnalysis: " + result.getEmotionAnalysis());
        System.out.println("communicationPatterns: " + result.getCommunicationPatterns());
        System.out.println("=== 검증 디버그 끝 ===");
        
        // 필수 필드 검증 및 기본값 설정
        if (result.getMainResults() == null) {
            System.err.println("WARNING: mainResults가 없어서 기본값으로 설정합니다");
            // 완전한 기본값 생성
            AnalysisResult.MainResults defaultMainResults = new AnalysisResult.MainResults();
            defaultMainResults.setSuccessRate(50.0);
            defaultMainResults.setConfidenceLevel(50.0);
            defaultMainResults.setRelationshipStage("분석 진행 중");
            defaultMainResults.setHeroInsight("대화 분석을 진행하고 있습니다.");
            defaultMainResults.setSummary("분석 결과를 가져오는데 문제가 있어 기본값을 사용합니다.");
            result.setMainResults(defaultMainResults);
            System.out.println("완전한 mainResults 기본값 설정 완료");
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
        
        // 분석 메타데이터 기본값 설정
        if (result.getAnalysisMetadata() == null) {
            System.err.println("WARNING: analysisMetadata가 없어서 기본값으로 설정합니다");
            AnalysisResult.AnalysisMetadata defaultMetadata = new AnalysisResult.AnalysisMetadata();
            defaultMetadata.setAnalysisDate(java.time.LocalDateTime.now().toString());
            defaultMetadata.setTotalMessages(0);
            defaultMetadata.setAnalysisVersion("1.0");
            
            // 대화 기간 기본값
            AnalysisResult.ConversationPeriod defaultPeriod = new AnalysisResult.ConversationPeriod();
            String today = java.time.LocalDate.now().toString();
            defaultPeriod.setStart(today);
            defaultPeriod.setEnd(today);
            defaultMetadata.setConversationPeriod(defaultPeriod);
            
            result.setAnalysisMetadata(defaultMetadata);
            System.out.println("analysisMetadata 기본값 설정 완료");
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