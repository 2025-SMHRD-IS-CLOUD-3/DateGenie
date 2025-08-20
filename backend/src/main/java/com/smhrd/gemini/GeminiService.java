package com.smhrd.gemini;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiService {
	//Gemini API와의 모든 통신을 전담하는 클래스

    // Client 객체는 한 번만 생성해서 재활용합니다.
    private Client client;
    
    public GeminiService() {
        // Client가 환경 변수 GEMINI_API_KEY를 자동으로 읽어옵니다.
        this.client = new Client();
    }
    
    /**
     * Gemini API를 호출하여 텍스트를 생성하는 메서드입니다.
     * 이 코드는 com.google.genai 라이브러리를 사용합니다.
     *
     * @param prompt Gemini에게 보낼 질문이나 명령어
     * @return Gemini가 생성한 텍스트 응답
     */
    public String generateContent(String prompt) {
        try {
            
            GenerateContentResponse response = client.models.generateContent(
                "gemini-2.0-flash",
                prompt,
                null
            );

            // 응답에서 텍스트를 반환합니다.
           
            return response.text();
            
        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return "오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
        }
    }
}