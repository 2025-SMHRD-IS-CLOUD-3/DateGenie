package com.smhrd.model;

import java.util.List;
import java.util.Map;

/**
 * Gemini API 분석 결과를 담는 메인 클래스
 * Database 저장과 Frontend 응답에 모두 사용됨
 */
public class AnalysisResult {
    
    private String sessionId;
    private String userId;
    private String partnerName;
    
    private AnalysisMetadata analysisMetadata;
    private MainResults mainResults;
    private List<InterestTrend> interestTrends;
    private EmotionAnalysis emotionAnalysis;
    private List<PositiveSignal> positiveSignals;
    private FavoriteMessage favoriteMessage;
    private List<ConversationGuide> conversationGuides;
    private List<CustomAdvice> customAdvice;
    
    // === Inner Classes ===
    
    /**
     * 분석 메타데이터
     */
    public static class AnalysisMetadata {
        private String analysisDate;
        private ConversationPeriod conversationPeriod;
        private int totalMessages;
        private String analysisVersion;
        
        // Getters and Setters
        public String getAnalysisDate() { return analysisDate; }
        public void setAnalysisDate(String analysisDate) { this.analysisDate = analysisDate; }
        
        public ConversationPeriod getConversationPeriod() { return conversationPeriod; }
        public void setConversationPeriod(ConversationPeriod conversationPeriod) { this.conversationPeriod = conversationPeriod; }
        
        public int getTotalMessages() { return totalMessages; }
        public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }
        
        public String getAnalysisVersion() { return analysisVersion; }
        public void setAnalysisVersion(String analysisVersion) { this.analysisVersion = analysisVersion; }
    }
    
    /**
     * 대화 기간 정보
     */
    public static class ConversationPeriod {
        private String start;
        private String end;
        
        public String getStart() { return start; }
        public void setStart(String start) { this.start = start; }
        
        public String getEnd() { return end; }
        public void setEnd(String end) { this.end = end; }
    }
    
    /**
     * 메인 분석 결과
     */
    public static class MainResults {
        private double successRate;
        private double confidenceLevel;
        private String relationshipStage;
        private String heroInsight;
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public double getConfidenceLevel() { return confidenceLevel; }
        public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
        
        public String getRelationshipStage() { return relationshipStage; }
        public void setRelationshipStage(String relationshipStage) { this.relationshipStage = relationshipStage; }
        
        public String getHeroInsight() { return heroInsight; }
        public void setHeroInsight(String heroInsight) { this.heroInsight = heroInsight; }
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
     * 감정 분석 결과
     */
    public static class EmotionAnalysis {
        private double positive;
        private double neutral;
        private double negative;
        private String dominantEmotion;
        private double stabilityScore;
        private List<String> positiveKeywords;
        private List<String> negativeKeywords;
        
        public double getPositive() { return positive; }
        public void setPositive(double positive) { this.positive = positive; }
        
        public double getNeutral() { return neutral; }
        public void setNeutral(double neutral) { this.neutral = neutral; }
        
        public double getNegative() { return negative; }
        public void setNegative(double negative) { this.negative = negative; }
        
        public String getDominantEmotion() { return dominantEmotion; }
        public void setDominantEmotion(String dominantEmotion) { this.dominantEmotion = dominantEmotion; }
        
        public double getStabilityScore() { return stabilityScore; }
        public void setStabilityScore(double stabilityScore) { this.stabilityScore = stabilityScore; }
        
        public List<String> getPositiveKeywords() { return positiveKeywords; }
        public void setPositiveKeywords(List<String> positiveKeywords) { this.positiveKeywords = positiveKeywords; }
        
        public List<String> getNegativeKeywords() { return negativeKeywords; }
        public void setNegativeKeywords(List<String> negativeKeywords) { this.negativeKeywords = negativeKeywords; }
    }
    
    /**
     * 긍정적 신호
     */
    public static class PositiveSignal {
        private String text;
        private String description;
        private int confidence;
        private String type;
        private double metricValue;
        private String metricUnit;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public int getConfidence() { return confidence; }
        public void setConfidence(int confidence) { this.confidence = confidence; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public double getMetricValue() { return metricValue; }
        public void setMetricValue(double metricValue) { this.metricValue = metricValue; }
        
        public String getMetricUnit() { return metricUnit; }
        public void setMetricUnit(String metricUnit) { this.metricUnit = metricUnit; }
    }
    
    /**
     * 대표 호감 메시지
     */
    public static class FavoriteMessage {
        private String text;
        private double confidence;
        private String date;
        private String reason;
        private String sender;
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
    }
    
    /**
     * 대화 가이드
     */
    public static class ConversationGuide {
        private String type;
        private String text;
        private String timing;
        private String category;
        private int difficultyLevel;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getTiming() { return timing; }
        public void setTiming(String timing) { this.timing = timing; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public int getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    }
    
    /**
     * 맞춤 조언
     */
    public static class CustomAdvice {
        private String title;
        private String content;
        private String type;
        private int priority;
        private String urgency;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        
        public String getUrgency() { return urgency; }
        public void setUrgency(String urgency) { this.urgency = urgency; }
    }
    
    // === Main Class Getters and Setters ===
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    
    public AnalysisMetadata getAnalysisMetadata() { return analysisMetadata; }
    public void setAnalysisMetadata(AnalysisMetadata analysisMetadata) { this.analysisMetadata = analysisMetadata; }
    
    public MainResults getMainResults() { return mainResults; }
    public void setMainResults(MainResults mainResults) { this.mainResults = mainResults; }
    
    public List<InterestTrend> getInterestTrends() { return interestTrends; }
    public void setInterestTrends(List<InterestTrend> interestTrends) { this.interestTrends = interestTrends; }
    
    public EmotionAnalysis getEmotionAnalysis() { return emotionAnalysis; }
    public void setEmotionAnalysis(EmotionAnalysis emotionAnalysis) { this.emotionAnalysis = emotionAnalysis; }
    
    public List<PositiveSignal> getPositiveSignals() { return positiveSignals; }
    public void setPositiveSignals(List<PositiveSignal> positiveSignals) { this.positiveSignals = positiveSignals; }
    
    public FavoriteMessage getFavoriteMessage() { return favoriteMessage; }
    public void setFavoriteMessage(FavoriteMessage favoriteMessage) { this.favoriteMessage = favoriteMessage; }
    
    public List<ConversationGuide> getConversationGuides() { return conversationGuides; }
    public void setConversationGuides(List<ConversationGuide> conversationGuides) { this.conversationGuides = conversationGuides; }
    
    public List<CustomAdvice> getCustomAdvice() { return customAdvice; }
    public void setCustomAdvice(List<CustomAdvice> customAdvice) { this.customAdvice = customAdvice; }
    
    /**
     * Frontend에서 사용할 수 있는 형태로 변환
     */
    public Map<String, Object> toFrontendFormat() {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // 메인 분석 결과
        if (mainResults != null) {
            result.put("successRate", mainResults.getSuccessRate());
            result.put("confidenceLevel", mainResults.getConfidenceLevel());
            result.put("relationshipStage", mainResults.getRelationshipStage());
            result.put("heroInsight", mainResults.getHeroInsight());
        }
        
        // 관심도 추이 데이터 변환
        if (interestTrends != null) {
            List<Map<String, Object>> trendData = new java.util.ArrayList<>();
            for (InterestTrend trend : interestTrends) {
                Map<String, Object> trendMap = new java.util.HashMap<>();
                trendMap.put("date", trend.getDate());
                trendMap.put("value", trend.getValue());
                trendData.add(trendMap);
            }
            result.put("interestTrendData", trendData);
        }
        
        // 감정 분석 데이터
        if (emotionAnalysis != null) {
            Map<String, Object> emotionData = new java.util.HashMap<>();
            emotionData.put("positive", emotionAnalysis.getPositive());
            emotionData.put("neutral", emotionAnalysis.getNeutral());
            emotionData.put("negative", emotionAnalysis.getNegative());
            result.put("emotionData", emotionData);
        }
        
        // 긍정적 신호들
        if (positiveSignals != null) {
            List<Map<String, Object>> signals = new java.util.ArrayList<>();
            for (PositiveSignal signal : positiveSignals) {
                Map<String, Object> signalMap = new java.util.HashMap<>();
                signalMap.put("text", signal.getText());
                signalMap.put("description", signal.getDescription());
                signalMap.put("confidence", signal.getConfidence());
                signals.add(signalMap);
            }
            result.put("positiveSignals", signals);
        }
        
        // 대표 호감 메시지
        if (favoriteMessage != null) {
            Map<String, Object> favMsg = new java.util.HashMap<>();
            favMsg.put("text", favoriteMessage.getText());
            favMsg.put("confidence", favoriteMessage.getConfidence());
            favMsg.put("date", favoriteMessage.getDate());
            favMsg.put("reason", favoriteMessage.getReason());
            result.put("favoriteMessage", favMsg);
        }
        
        // 대화 가이드
        if (conversationGuides != null) {
            List<Map<String, Object>> guides = new java.util.ArrayList<>();
            for (ConversationGuide guide : conversationGuides) {
                Map<String, Object> guideMap = new java.util.HashMap<>();
                guideMap.put("type", guide.getType());
                guideMap.put("text", guide.getText());
                guideMap.put("timing", guide.getTiming());
                guides.add(guideMap);
            }
            result.put("conversationGuide", guides);
        }
        
        // 맞춤 조언
        if (customAdvice != null) {
            List<Map<String, Object>> advice = new java.util.ArrayList<>();
            for (CustomAdvice adv : customAdvice) {
                Map<String, Object> adviceMap = new java.util.HashMap<>();
                adviceMap.put("title", adv.getTitle());
                adviceMap.put("content", adv.getContent());
                advice.add(adviceMap);
            }
            result.put("customAdvice", advice);
        }
        
        return result;
    }
}