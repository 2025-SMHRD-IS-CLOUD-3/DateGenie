package com.smhrd.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Anthropic API 설정을 관리하는 클래스
 */
public class AnthropicConfig {
    
    private Properties properties;
    
    public AnthropicConfig() {
        loadProperties();
    }
    
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config/anthropic.properties")) {
            if (input == null) {
                throw new RuntimeException("anthropic.properties 파일을 찾을 수 없습니다.");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("anthropic.properties 로드 실패", e);
        }
    }
    
    public String getApiKey() {
        return properties.getProperty("anthropic.api.key");
    }
    
    public String getApiUrl() {
        return properties.getProperty("anthropic.api.url");
    }
    
    public String getApiVersion() {
        return properties.getProperty("anthropic.api.version");
    }
    
    public String getModel() {
        return properties.getProperty("anthropic.model");
    }
    
    public int getMaxTokens() {
        return Integer.parseInt(properties.getProperty("anthropic.max_tokens", "4096"));
    }
    
    public int getConnectTimeout() {
        return Integer.parseInt(properties.getProperty("anthropic.timeout.connect", "15000"));
    }
    
    public int getReadTimeout() {
        return Integer.parseInt(properties.getProperty("anthropic.timeout.read", "60000"));
    }
}