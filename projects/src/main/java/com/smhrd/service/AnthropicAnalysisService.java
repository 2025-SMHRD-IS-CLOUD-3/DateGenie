package com.smhrd.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smhrd.model.AnalysisResult;
import com.smhrd.util.AnthropicConfig;

/**
 * Anthropic Claude API를 사용한 대화 분석 서비스
 */
public class AnthropicAnalysisService {
    
    private final Gson gson;
    private final AnthropicConfig config;
    
    public AnthropicAnalysisService() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .disableHtmlEscaping()
            .create();
        this.config = new AnthropicConfig();
    }
    
    /**
     * 대화 분석 수행
     */
    public AnalysisResult analyzeConversation(String conversationData, String userId, String partnerName) throws Exception {
        System.out.println("=== Anthropic 대화 분석 시작 ===");
        
        // 대화 데이터 압축 (토큰 제한 고려)
        String compressedData = compressConversationData(conversationData);
        System.out.println("압축된 데이터 길이: " + compressedData.length() + "자");
        
        // 프롬프트 템플릿 로드 및 생성
        String prompt = createAnalysisPrompt(compressedData);
        
        // Anthropic API 호출
        Map<String, Object> apiResponse = callAnthropicAPI(prompt);
        
        // 응답을 AnalysisResult로 변환
        return parseToAnalysisResult(apiResponse, userId, partnerName);
    }
    
    /**
     * 분석 프롬프트 생성
     */
    private String createAnalysisPrompt(String conversationData) throws Exception {
        try {
            String template = new String(
                getClass().getClassLoader().getResourceAsStream("prompt_templates/anthropic_analysis_prompt.txt").readAllBytes(),
                StandardCharsets.UTF_8
            );
            return template.replace("{{CONVERSATION_DATA}}", conversationData);
        } catch (Exception e) {
            System.err.println("프롬프트 템플릿 로드 실패");
            throw new Exception("프롬프트 템플릿을 로드할 수 없습니다.", e);
        }
    }
    
    /**
     * Anthropic API 호출
     */
    private Map<String, Object> callAnthropicAPI(String prompt) throws Exception {
        URL url = new URL(config.getApiUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("x-api-key", config.getApiKey());
        connection.setRequestProperty("anthropic-version", config.getApiVersion());
        connection.setDoOutput(true);
        connection.setConnectTimeout(config.getConnectTimeout());
        connection.setReadTimeout(config.getReadTimeout());
        
        // 요청 본문 생성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("max_tokens", config.getMaxTokens());
        
        // 메시지 배열 생성
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        requestBody.put("messages", Arrays.asList(userMessage));
        
        String requestJson = gson.toJson(requestBody);
        System.out.println("API 요청 크기: " + requestJson.length() + " bytes");
        
        // UTF-8 인코딩으로 요청 전송
        byte[] jsonBytes = requestJson.getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Length", String.valueOf(jsonBytes.length));
        
        try (java.io.OutputStream os = connection.getOutputStream()) {
            os.write(jsonBytes);
            os.flush();
        }
        
        // 응답 읽기
        int responseCode = connection.getResponseCode();
        System.out.println("응답 코드: " + responseCode);
        
        if (responseCode != 200) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new Exception("Anthropic API 호출 실패 (" + responseCode + "): " + errorResponse.toString());
            }
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            String responseBody = response.toString();
            System.out.println("응답 크기: " + responseBody.length() + " bytes");
            
            // 응답 파싱
            return parseAnthropicResponse(responseBody);
        }
    }
    
    /**
     * Anthropic API 응답 파싱
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAnthropicResponse(String responseBody) throws Exception {
        Map<String, Object> responseJson = gson.fromJson(responseBody, Map.class);
        
        // 사용량 정보 출력
        Map<String, Object> usage = (Map<String, Object>) responseJson.get("usage");
        if (usage != null) {
            System.out.println("토큰 사용량:");
            System.out.println("  입력 토큰: " + usage.get("input_tokens"));
            System.out.println("  출력 토큰: " + usage.get("output_tokens"));
        }
        
        // 응답 내용 추출
        List<Map<String, Object>> content = (List<Map<String, Object>>) responseJson.get("content");
        if (content == null || content.isEmpty()) {
            throw new Exception("응답에 content가 없습니다.");
        }
        
        Map<String, Object> firstContent = content.get(0);
        String responseText = (String) firstContent.get("text");
        
        if (responseText == null) {
            throw new Exception("응답 텍스트를 찾을 수 없습니다.");
        }
        
        System.out.println("분석 응답 받음: " + responseText.length() + "자");
        
        // JSON 블록 추출
        String jsonText = extractJsonFromText(responseText);
        
        // JSON 파싱하여 반환
        return gson.fromJson(jsonText, Map.class);
    }
    
    /**
     * 텍스트에서 JSON 블록 추출
     */
    private String extractJsonFromText(String text) throws Exception {
        // ```json 블록 찾기
        int jsonStart = text.indexOf("```json");
        int jsonEnd = text.indexOf("```", jsonStart + 7);
        
        if (jsonStart != -1 && jsonEnd != -1) {
            String jsonBlock = text.substring(jsonStart + 7, jsonEnd).trim();
            System.out.println("JSON 블록 추출 성공");
            return jsonBlock;
        }
        
        // 일반 JSON 블록 찾기
        jsonStart = text.indexOf("{");
        jsonEnd = text.lastIndexOf("}");
        
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            String jsonBlock = text.substring(jsonStart, jsonEnd + 1).trim();
            System.out.println("일반 JSON 추출 성공");
            return jsonBlock;
        }
        
        throw new Exception("응답에서 유효한 JSON을 찾을 수 없습니다.");
    }
    
    /**
     * API 응답을 AnalysisResult로 변환
     */
    @SuppressWarnings("unchecked")
    private AnalysisResult parseToAnalysisResult(Map<String, Object> apiResponse, String userId, String partnerName) {
        AnalysisResult result = new AnalysisResult();
        result.setSessionId(UUID.randomUUID().toString());
        result.setUserId(userId);
        result.setPartnerName(partnerName);
        
        // 메인 결과 파싱
        if (apiResponse.containsKey("mainResults")) {
            Map<?, ?> mainData = (Map<?, ?>) apiResponse.get("mainResults");
            AnalysisResult.MainResults main = new AnalysisResult.MainResults();
            main.setSuccessRate(((Number) mainData.get("successRate")).doubleValue());
            main.setConfidenceLevel(mainData.containsKey("confidenceLevel") ? 
                ((Number) mainData.get("confidenceLevel")).doubleValue() : main.getSuccessRate() * 0.8);
            main.setRelationshipStage((String) mainData.get("relationshipStage"));
            main.setSummary((String) mainData.get("summary"));
            main.setHeroInsight((String) mainData.get("heroInsight"));
            result.setMainResults(main);
        }
        
        // 감정 분석 파싱
        if (apiResponse.containsKey("emotionAnalysis")) {
            Map<?, ?> emotionData = (Map<?, ?>) apiResponse.get("emotionAnalysis");
            AnalysisResult.EmotionAnalysis emotion = new AnalysisResult.EmotionAnalysis();
            emotion.setPositive(((Number) emotionData.get("positive")).doubleValue());
            emotion.setNeutral(((Number) emotionData.get("neutral")).doubleValue());
            emotion.setNegative(((Number) emotionData.get("negative")).doubleValue());
            result.setEmotionAnalysis(emotion);
        }
        
        // 관심도 추이 파싱
        if (apiResponse.containsKey("interestTrends")) {
            List<?> trendList = (List<?>) apiResponse.get("interestTrends");
            List<AnalysisResult.InterestTrend> interestTrends = new ArrayList<>();
            
            for (Object trendObj : trendList) {
                Map<?, ?> trendData = (Map<?, ?>) trendObj;
                AnalysisResult.InterestTrend trend = new AnalysisResult.InterestTrend();
                trend.setDate((String) trendData.get("date"));
                trend.setValue(((Number) trendData.get("value")).doubleValue());
                trend.setMessageCount(trendData.containsKey("messageCount") ? 
                    ((Number) trendData.get("messageCount")).intValue() : 0);
                trend.setAvgResponseTime(trendData.containsKey("avgResponseTime") ? 
                    ((Number) trendData.get("avgResponseTime")).doubleValue() : 0);
                interestTrends.add(trend);
            }
            result.setInterestTrends(interestTrends);
        }
        
        // 긍정적 신호 파싱
        if (apiResponse.containsKey("positiveSignals")) {
            List<?> signalList = (List<?>) apiResponse.get("positiveSignals");
            List<AnalysisResult.PositiveSignal> positiveSignals = new ArrayList<>();
            
            for (Object signalObj : signalList) {
                Map<?, ?> signalData = (Map<?, ?>) signalObj;
                AnalysisResult.PositiveSignal signal = new AnalysisResult.PositiveSignal();
                signal.setText((String) signalData.get("text"));
                signal.setDescription((String) signalData.get("description"));
                signal.setConfidence(signalData.containsKey("confidence") ? 
                    ((Number) signalData.get("confidence")).intValue() : 3);
                positiveSignals.add(signal);
            }
            result.setPositiveSignals(positiveSignals);
        }
        
        // 대표 호감 메시지 파싱
        if (apiResponse.containsKey("favoriteMessage")) {
            Map<?, ?> messageData = (Map<?, ?>) apiResponse.get("favoriteMessage");
            AnalysisResult.FavoriteMessage message = new AnalysisResult.FavoriteMessage();
            message.setText((String) messageData.get("text"));
            message.setConfidence(messageData.containsKey("confidence") ? 
                ((Number) messageData.get("confidence")).intValue() : 85);
            message.setDate((String) messageData.get("date"));
            message.setReason((String) messageData.get("reason"));
            result.setFavoriteMessage(message);
        }
        
        // 실전 대화 가이드 파싱
        if (apiResponse.containsKey("conversationGuides")) {
            List<?> guideList = (List<?>) apiResponse.get("conversationGuides");
            List<AnalysisResult.ConversationGuide> conversationGuides = new ArrayList<>();
            
            for (Object guideObj : guideList) {
                Map<?, ?> guideData = (Map<?, ?>) guideObj;
                AnalysisResult.ConversationGuide guide = new AnalysisResult.ConversationGuide();
                guide.setType((String) guideData.get("type"));
                guide.setText((String) guideData.get("text"));
                guide.setTiming((String) guideData.get("timing"));
                guide.setContext((String) guideData.get("context"));
                conversationGuides.add(guide);
            }
            result.setConversationGuides(conversationGuides);
        }
        
        // 조언 파싱
        if (apiResponse.containsKey("customAdvice")) {
            List<?> adviceList = (List<?>) apiResponse.get("customAdvice");
            List<AnalysisResult.CustomAdvice> customAdviceList = new ArrayList<>();
            
            for (Object adviceObj : adviceList) {
                Map<?, ?> adviceData = (Map<?, ?>) adviceObj;
                AnalysisResult.CustomAdvice advice = new AnalysisResult.CustomAdvice();
                advice.setTitle((String) adviceData.get("title"));
                advice.setContent((String) adviceData.get("content"));
                customAdviceList.add(advice);
            }
            result.setCustomAdvice(customAdviceList);
        }
        
        System.out.println("AnalysisResult 변환 완료 - 모든 필드 포함");
        return result;
    }
    
    /**
     * 대화 데이터 압축
     */
    private String compressConversationData(String data) {
        if (data.length() > 3000) {
            return data.substring(0, 3000) + "...[대화가 길어 일부만 분석에 사용됩니다]";
        }
        return data;
    }
}