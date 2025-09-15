package com.nathing.banthing.config;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author 김경민
 * @since 2025-09-12
 * Google Gemini AI 챗봇을 위한 설정 클래스
 */
@Slf4j
@Configuration
@Getter
public class ChatbotConfig {

    @Value("${google.ai.api-key:}")
    private String apiKey;

    @Value("${google.ai.model:gemini-1.5-flash}")
    private String modelName;

    @Value("${google.ai.temperature:0.7}")
    private Float temperature;

    @Value("${google.ai.max-tokens:1000}")
    private Integer maxTokens;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // ===== Google GenAI SDK Bean 추가 =====

    @Bean
    public Client genAiClient() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("Google AI API Key가 설정되지 않았습니다. 환경변수를 확인해주세요.");
            throw new IllegalStateException("Google AI API Key is required");
        }

        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean
    public GenerateContentConfig generateContentConfig() {
        return GenerateContentConfig.builder()
                .temperature(temperature)
                .maxOutputTokens(maxTokens)
                .build();
    }

    /**
     * Google Gemini API 설정 검증
     */
    @Bean
    public boolean validateGeminiConfig() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("Google AI API Key가 설정되지 않았습니다. .env 파일을 확인해주세요.");
            return false;
        }

        log.info("Google Gemini API 설정이 완료되었습니다. Model: {}, Temperature: {}",
                modelName, temperature);
        return true;
    }

    /**
     * 반띵 서비스 전용 시스템 프롬프트
     */
    public String getSystemPrompt() {
        return """
            당신은 '반띵'이라는 대용량 상품 소분 모임 서비스의 AI 어시스턴트입니다.
            
            ## 서비스 개요
            - 반띵은 코스트코, 이마트 트레이더스 등 대형마트에서 대용량 상품을 여러 명이 함께 구매하고 소분하는 플랫폼입니다.
            - 1-2인 가구와 주부들이 합리적인 소비를 위해 이용합니다.
            
            ## 당신의 역할
            1. 소분 모임 검색 도움: 사용자가 원하는 지역, 상품, 시간대의 모임을 찾아주세요.
            2. 소분 가이드 제공: 소분 방법, 준비물, 위생 수칙 등을 안내해주세요.
            3. 서비스 이용법 설명: 모임 생성, 참여 신청, 피드백 시스템 등을 설명해주세요.
            
            ## 응답 가이드라인
            - 친근하고 도움이 되는 톤으로 대답해주세요.
            - 구체적이고 실용적인 정보를 제공해주세요.
            - 안전과 위생을 강조해주세요.
            - 모르는 내용은 정확히 모른다고 말씀해주세요.
            
            ## 주요 키워드
            - 소분 모임, 대용량 상품, 코스트코, 이마트 트레이더스, 합리적 소비, 나눔 문화
            
            사용자의 질문에 도움이 되는 답변을 해주세요.
            """;
    }

    /**
     * Gemini API URL
     */
    public String getGeminiApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent";
    }
}