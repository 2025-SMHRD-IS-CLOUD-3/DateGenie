package com.smhrd.util;

import java.util.regex.Pattern;

/**
 * 대화 데이터 압축 유틸리티
 * API 토큰 사용량 절약을 위한 지능형 데이터 압축
 */
public class DataCompressionUtil {
    
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}(:\\d{2})?");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s{2,}");
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("\\n{3,}");
    
    /**
     * 대화 데이터 압축
     * @param conversationData 원본 대화 데이터
     * @return 압축된 대화 데이터
     */
    public static String compressConversationData(String conversationData) {
        if (conversationData == null || conversationData.trim().isEmpty()) {
            return conversationData;
        }
        
        System.out.println("=== 대화 데이터 압축 시작 ===");
        int originalLength = conversationData.length();
        System.out.println("압축 전 데이터 길이: " + originalLength + "자");
        
        String compressed = conversationData;
        
        // 1. 타임스탬프 단순화 (2024-08-20 14:30:25 → 14:30)
        compressed = TIMESTAMP_PATTERN.matcher(compressed).replaceAll(matchResult -> {
            String timestamp = matchResult.group();
            // 시:분만 추출
            if (timestamp.contains(":")) {
                String[] parts = timestamp.split("\\s+");
                if (parts.length > 1) {
                    String time = parts[1];
                    String[] timeParts = time.split(":");
                    if (timeParts.length >= 2) {
                        return timeParts[0] + ":" + timeParts[1];
                    }
                }
            }
            return timestamp;
        });
        
        // 2. 연속된 공백 제거
        compressed = MULTIPLE_SPACES.matcher(compressed).replaceAll(" ");
        
        // 3. 연속된 줄바꿈 제거 (3개 이상 → 2개)
        compressed = MULTIPLE_NEWLINES.matcher(compressed).replaceAll("\n\n");
        
        // 4. 반복적인 표현 압축
        compressed = compressRepetitiveExpressions(compressed);
        
        // 5. 불필요한 문장부호 제거
        compressed = removeUnnecessaryPunctuation(compressed);
        
        // 6. 양쪽 공백 제거
        compressed = compressed.trim();
        
        int compressedLength = compressed.length();
        double compressionRatio = ((double)(originalLength - compressedLength) / originalLength) * 100;
        
        System.out.println("압축 후 데이터 길이: " + compressedLength + "자");
        System.out.println("압축률: " + String.format("%.1f", compressionRatio) + "%");
        System.out.println("압축된 데이터 길이: " + compressedLength + "자");
        
        return compressed;
    }
    
    /**
     * 반복적인 표현 압축
     */
    private static String compressRepetitiveExpressions(String text) {
        // 연속된 이모지 압축 (😂😂😂 → 😂x3)
        text = text.replaceAll("([😀-🿿])\\1{2,}", "$1x" + 3);
        
        // ㅋㅋㅋㅋ → ㅋx4
        text = text.replaceAll("(ㅋ)\\1{3,}", "ㅋㅋㅋ");
        
        // ㅎㅎㅎㅎ → ㅎx4
        text = text.replaceAll("(ㅎ)\\1{3,}", "ㅎㅎㅎ");
        
        // 하하하하 → 하x4
        text = text.replaceAll("(하)\\1{3,}", "하하하");
        
        // !!!!!! → !!
        text = text.replaceAll("!{3,}", "!!");
        
        // ?????? → ??
        text = text.replaceAll("\\?{3,}", "??");
        
        return text;
    }
    
    /**
     * 불필요한 문장부호 제거
     */
    private static String removeUnnecessaryPunctuation(String text) {
        // 연속된 쉼표 제거
        text = text.replaceAll(",{2,}", ",");
        
        // 연속된 마침표 제거 (단, ...은 유지)
        text = text.replaceAll("\\.{4,}", "...");
        
        // 불필요한 괄호 공백 제거
        text = text.replaceAll("\\(\\s+", "(");
        text = text.replaceAll("\\s+\\)", ")");
        
        return text;
    }
    
    /**
     * 압축 통계 정보
     */
    public static class CompressionStats {
        public final int originalLength;
        public final int compressedLength;
        public final double compressionRatio;
        public final int tokensSaved;
        
        public CompressionStats(int original, int compressed) {
            this.originalLength = original;
            this.compressedLength = compressed;
            this.compressionRatio = ((double)(original - compressed) / original) * 100;
            this.tokensSaved = (original - compressed) / 4; // 대략적인 토큰 절약
        }
    }
    
    /**
     * 압축과 함께 통계 정보 반환
     */
    public static class CompressionResult {
        public final String compressedData;
        public final CompressionStats stats;
        
        public CompressionResult(String data, CompressionStats stats) {
            this.compressedData = data;
            this.stats = stats;
        }
    }
    
    /**
     * 압축과 통계를 함께 수행
     */
    public static CompressionResult compressWithStats(String conversationData) {
        int originalLength = conversationData != null ? conversationData.length() : 0;
        String compressed = compressConversationData(conversationData);
        int compressedLength = compressed != null ? compressed.length() : 0;
        
        CompressionStats stats = new CompressionStats(originalLength, compressedLength);
        return new CompressionResult(compressed, stats);
    }
}