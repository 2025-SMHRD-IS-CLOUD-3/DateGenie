// com/smhrd/model/ResultDTO.java
package com.smhrd.model;

import lombok.Data;

@Data // Lombok 사용. 없다면 Getter/Setter/ToString 등 직접 생성
public class ResultDTO {
    
    private Long resultNum; // RESULT_NUM (자료형은 Long 또는 int)
    private String email; // EMAIL
    private int emotionScore; // EMOTION_SCORE
    private String emotionSentence; // EMOTION_SENTENCE
    private String personality; // PERSONALITY
    private String advice; // ADVICE
    private String targetName; // TARGET_NAME
    private int displayOrder; // DISPLAY_ORDER
    private String firstTalk; // FIRST_TALK
    private int emotionCount; // EMOTION_COUNT
    private String talkBalance; // TALK_BALANCE (비율 등이 문자열로 표현될 수 있어 String으로 가정)
    private String talkSpeed; // TALK_SPEED
    private int talkCount; // TALK_COUNT
    
    // createdAt과 같은 날짜 컬럼이 있다면 추가
}