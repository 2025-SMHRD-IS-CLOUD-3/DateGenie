package com.smhrd.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gemini API 설정 관리 클래스
 * 
 * 설정 파일에서 API 키와 기타 설정을 로드하고 관리합니다.
 * 보안상 API 키는 별도 파일로 관리하며, 환경변수 우선순위를 적용합니다.
 */
public class GeminiConfig {
    
    private static final String CONFIG_FILE = "/config/gemini.properties";
    private static Properties properties = new Properties();
    
    // 싱글톤 패턴으로 설정 로드
    static {
        loadConfig();
    }
    
    /**
     * 설정 파일 로드
     */
    private static void loadConfig() {
        try (InputStream inputStream = GeminiConfig.class.getResourceAsStream(CONFIG_FILE)) {
            
            if (inputStream != null) {
                properties.load(inputStream);
                System.out.println("Gemini 설정 파일 로드 완료: " + CONFIG_FILE);
            } else {
                System.err.println("Gemini 설정 파일을 찾을 수 없습니다: " + CONFIG_FILE);
                loadDefaultConfig();
            }
            
        } catch (IOException e) {
            System.err.println("Gemini 설정 파일 로드 실패: " + e.getMessage());
            loadDefaultConfig();
        }
    }
    
    /**
     * 기본 설정 로드 (설정 파일이 없는 경우)
     */
    private static void loadDefaultConfig() {
        properties.setProperty("gemini.api.key", "AIzaSyBhLDHAb5guEGF0fXvyQRX7t6gHs7XOYqM");
        properties.setProperty("gemini.api.url", "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent");
        properties.setProperty("gemini.temperature", "0.1");
        properties.setProperty("gemini.top_k", "1");
        properties.setProperty("gemini.top_p", "0.8");
        properties.setProperty("gemini.max_output_tokens", "8192");
        properties.setProperty("gemini.retry.max_attempts", "3");
        properties.setProperty("gemini.retry.delay_ms", "1000");
        properties.setProperty("gemini.timeout.connect", "10000");
        properties.setProperty("gemini.timeout.read", "30000");
    }
    
    /**
     * API 키 가져오기 (환경변수 우선)
     */
    public static String getApiKey() {
        // 1. 환경변수에서 확인
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            return envKey;
        }
        
        // 2. 시스템 프로퍼티에서 확인
        String sysKey = System.getProperty("GEMINI_API_KEY");
        if (sysKey != null && !sysKey.isEmpty()) {
            return sysKey;
        }
        
        // 3. 설정 파일에서 확인
        return properties.getProperty("gemini.api.key");
    }
    
    /**
     * API URL 가져오기
     */
    public static String getApiUrl() {
        return properties.getProperty("gemini.api.url");
    }
    
    /**
     * Temperature 설정 가져오기
     */
    public static double getTemperature() {
        return Double.parseDouble(properties.getProperty("gemini.temperature", "0.1"));
    }
    
    /**
     * Top K 설정 가져오기
     */
    public static int getTopK() {
        return Integer.parseInt(properties.getProperty("gemini.top_k", "1"));
    }
    
    /**
     * Top P 설정 가져오기
     */
    public static double getTopP() {
        return Double.parseDouble(properties.getProperty("gemini.top_p", "0.8"));
    }
    
    /**
     * 최대 출력 토큰 수 가져오기
     */
    public static int getMaxOutputTokens() {
        return Integer.parseInt(properties.getProperty("gemini.max_output_tokens", "8192"));
    }
    
    /**
     * 최대 재시도 횟수 가져오기
     */
    public static int getMaxRetryAttempts() {
        return Integer.parseInt(properties.getProperty("gemini.retry.max_attempts", "3"));
    }
    
    /**
     * 재시도 지연 시간 가져오기 (밀리초)
     */
    public static long getRetryDelayMs() {
        return Long.parseLong(properties.getProperty("gemini.retry.delay_ms", "1000"));
    }
    
    /**
     * 연결 타임아웃 가져오기 (밀리초)
     */
    public static int getConnectTimeout() {
        return Integer.parseInt(properties.getProperty("gemini.timeout.connect", "10000"));
    }
    
    /**
     * 읽기 타임아웃 가져오기 (밀리초)
     */
    public static int getReadTimeout() {
        return Integer.parseInt(properties.getProperty("gemini.timeout.read", "30000"));
    }
    
    /**
     * 설정 확인 및 출력 (디버그용)
     */
    public static void printConfig() {
        System.out.println("=== Gemini API 설정 ===");
        System.out.println("API URL: " + getApiUrl());
        System.out.println("API Key: " + (getApiKey() != null ? "설정됨 (***)" : "설정되지 않음"));
        System.out.println("Temperature: " + getTemperature());
        System.out.println("Top K: " + getTopK());
        System.out.println("Top P: " + getTopP());
        System.out.println("Max Output Tokens: " + getMaxOutputTokens());
        System.out.println("Max Retry Attempts: " + getMaxRetryAttempts());
        System.out.println("Retry Delay: " + getRetryDelayMs() + "ms");
        System.out.println("Connect Timeout: " + getConnectTimeout() + "ms");
        System.out.println("Read Timeout: " + getReadTimeout() + "ms");
        System.out.println("========================");
    }
}