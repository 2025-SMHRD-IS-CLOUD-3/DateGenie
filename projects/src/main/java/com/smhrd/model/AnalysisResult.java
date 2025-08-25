package com.smhrd.model;

import java.util.List;
import java.util.Map;

/**
 * Gemini API 분석 결과를 담는 종합적인 데이터 모델
 * UI 표시 및 DB 저장에 필요한 모든 필드를 포함
 */
public class AnalysisResult {
    
    private String sessionId;
    private String userId;
    private String partnerName;
    
    private MainResults mainResults;
    private EmotionAnalysis emotionAnalysis;
    private List<CustomAdvice> customAdvice;
    
    // 추가 필드들 - UI 표시용
    private List<InterestTrend> interestTrends;
    private List<PositiveSignal> positiveSignals;
    private FavoriteMessage favoriteMessage;
    private List<ConversationGuide> conversationGuides;
    
    // === Inner Classes ===
    
    /**
     * 메인 분석 결과
     */
    public static class MainResults {
        private double successRate;
        private double confidenceLevel;
        private String relationshipStage;
        private String summary;
        private String heroInsight;
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public double getConfidenceLevel() { return confidenceLevel; }
        public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
        
        public String getRelationshipStage() { return relationshipStage; }
        public void setRelationshipStage(String relationshipStage) { this.relationshipStage = relationshipStage; }
        
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        
        public String getHeroInsight() { return heroInsight; }
        public void setHeroInsight(String heroInsight) { this.heroInsight = heroInsight; }
    }
    
    /**
     * 감정 분석 결과 (간소화됨)
     */
    public static class EmotionAnalysis {
        private double positive;
        private double neutral;
        private double negative;
        
        public double getPositive() { return positive; }
        public void setPositive(double positive) { this.positive = positive; }
        
        public double getNeutral() { return neutral; }
        public void setNeutral(double neutral) { this.neutral = neutral; }
        
        public double getNegative() { return negative; }
        public void setNegative(double negative) { this.negative = negative; }
    }
    
    /**
     * 맞춤 조언
     */
    public static class CustomAdvice {
        private String title;
        private String content;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    /**
     * 관심도 추이 데이터
     */
    public static class InterestTrend {
        private String date;
        private double value;
        private int messageCount;
        private double avgResponseTime;
        private int emojiCount;
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        
        public int getMessageCount() { return messageCount; }
        public void setMessageCount(int messageCount) { this.messageCount = messageCount; }
        
        public double getAvgResponseTime() { return avgResponseTime; }
        public void setAvgResponseTime(double avgResponseTime) { this.avgResponseTime = avgResponseTime; }
        
        public int getEmojiCount() { return emojiCount; }
        public void setEmojiCount(int emojiCount) { this.emojiCount = emojiCount; }
    }
    
    /**
     * 긍정적 신호
     */
    public static class PositiveSignal {
        private String text;
        private String description;
        private int confidence;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public int getConfidence() { return confidence; }
        public void setConfidence(int confidence) { this.confidence = confidence; }
    }
    
    /**
     * 대표 호감 메시지
     */
    public static class FavoriteMessage {
        private String text;
        private int confidence;
        private String date;
        private String reason;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public int getConfidence() { return confidence; }
        public void setConfidence(int confidence) { this.confidence = confidence; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    /**
     * 실전 대화 가이드
     */
    public static class ConversationGuide {
        private String type;
        private String text;
        private String timing;
        private String context;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getTiming() { return timing; }
        public void setTiming(String timing) { this.timing = timing; }
        
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }
    
    // === Main Class Getters and Setters ===
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    
    public MainResults getMainResults() { return mainResults; }
    public void setMainResults(MainResults mainResults) { this.mainResults = mainResults; }
    
    public EmotionAnalysis getEmotionAnalysis() { return emotionAnalysis; }
    public void setEmotionAnalysis(EmotionAnalysis emotionAnalysis) { this.emotionAnalysis = emotionAnalysis; }
    
    public List<CustomAdvice> getCustomAdvice() { return customAdvice; }
    public void setCustomAdvice(List<CustomAdvice> customAdvice) { this.customAdvice = customAdvice; }
    
    public List<InterestTrend> getInterestTrends() { return interestTrends; }
    public void setInterestTrends(List<InterestTrend> interestTrends) { this.interestTrends = interestTrends; }
    
    public List<PositiveSignal> getPositiveSignals() { return positiveSignals; }
    public void setPositiveSignals(List<PositiveSignal> positiveSignals) { this.positiveSignals = positiveSignals; }
    
    public FavoriteMessage getFavoriteMessage() { return favoriteMessage; }
    public void setFavoriteMessage(FavoriteMessage favoriteMessage) { this.favoriteMessage = favoriteMessage; }
    
    public List<ConversationGuide> getConversationGuides() { return conversationGuides; }
    public void setConversationGuides(List<ConversationGuide> conversationGuides) { this.conversationGuides = conversationGuides; }
    
    /**
     * Frontend에서 사용할 수 있는 형태로 변환
     */
    public Map<String, Object> toFrontendFormat() {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // 메인 분석 결과
        if (mainResults != null) {
            Map<String, Object> mainData = new java.util.HashMap<>();
            mainData.put("successRate", mainResults.getSuccessRate());
            mainData.put("confidenceLevel", mainResults.getConfidenceLevel());
            mainData.put("relationshipStage", mainResults.getRelationshipStage());
            mainData.put("summary", mainResults.getSummary());
            mainData.put("heroInsight", mainResults.getHeroInsight());
            result.put("mainResults", mainData);
        }
        
        // 감정 분석 데이터
        if (emotionAnalysis != null) {
            Map<String, Object> emotionData = new java.util.HashMap<>();
            emotionData.put("positive", emotionAnalysis.getPositive());
            emotionData.put("neutral", emotionAnalysis.getNeutral());
            emotionData.put("negative", emotionAnalysis.getNegative());
            result.put("emotionAnalysis", emotionData);
        }
        
        // 관심도 추이
        if (interestTrends != null) {
            List<Map<String, Object>> trendList = new java.util.ArrayList<>();
            for (InterestTrend trend : interestTrends) {
                Map<String, Object> trendMap = new java.util.HashMap<>();
                trendMap.put("date", trend.getDate());
                trendMap.put("value", trend.getValue());
                trendMap.put("messageCount", trend.getMessageCount());
                trendMap.put("avgResponseTime", trend.getAvgResponseTime());
                trendMap.put("emojiCount", trend.getEmojiCount());
                trendList.add(trendMap);
            }
            result.put("interestTrends", trendList);
        }
        
        // 긍정적 신호
        if (positiveSignals != null) {
            List<Map<String, Object>> signalList = new java.util.ArrayList<>();
            for (PositiveSignal signal : positiveSignals) {
                Map<String, Object> signalMap = new java.util.HashMap<>();
                signalMap.put("text", signal.getText());
                signalMap.put("description", signal.getDescription());
                signalMap.put("confidence", signal.getConfidence());
                signalList.add(signalMap);
            }
            result.put("positiveSignals", signalList);
        }
        
        // 대표 호감 메시지
        if (favoriteMessage != null) {
            Map<String, Object> messageData = new java.util.HashMap<>();
            messageData.put("text", favoriteMessage.getText());
            messageData.put("confidence", favoriteMessage.getConfidence());
            messageData.put("date", favoriteMessage.getDate());
            messageData.put("reason", favoriteMessage.getReason());
            result.put("favoriteMessage", messageData);
        }
        
        // 실전 대화 가이드
        if (conversationGuides != null) {
            List<Map<String, Object>> guideList = new java.util.ArrayList<>();
            for (ConversationGuide guide : conversationGuides) {
                Map<String, Object> guideMap = new java.util.HashMap<>();
                guideMap.put("type", guide.getType());
                guideMap.put("text", guide.getText());
                guideMap.put("timing", guide.getTiming());
                guideMap.put("context", guide.getContext());
                guideList.add(guideMap);
            }
            result.put("conversationGuides", guideList);
        }
        
        // 맞춤 조언
        if (customAdvice != null) {
            List<Map<String, Object>> adviceList = new java.util.ArrayList<>();
            for (CustomAdvice advice : customAdvice) {
                Map<String, Object> adviceMap = new java.util.HashMap<>();
                adviceMap.put("title", advice.getTitle());
                adviceMap.put("content", advice.getContent());
                adviceList.add(adviceMap);
            }
            result.put("customAdvice", adviceList);
        }
        
        return result;
    }
}