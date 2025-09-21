package com.nathing.banthing.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.nathing.banthing.config.ChatbotConfig;
import com.nathing.banthing.dto.response.ChatbotConversationHistoryResponse;
import com.nathing.banthing.dto.response.ChatbotMessageResponse;
import com.nathing.banthing.entity.ChatbotConversation;
import com.nathing.banthing.entity.ChatbotMeetingSuggestion;
import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.User;
import com.nathing.banthing.exception.BusinessException;
import com.nathing.banthing.exception.ErrorCode;
import com.nathing.banthing.repository.ChatbotConversationsRepository;
import com.nathing.banthing.repository.ChatbotMeetingsSuggestionRepository;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 김경민
 * @since 2025-09-16
 * 반띵 AI 챗봇 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatbotServiceImpl implements ChatbotService {

    private final Client genAiClient;
    private final GenerateContentConfig genConfig;
    private final ChatbotConfig chatbotConfig;
    private final UsersRepository usersRepository;
    private final ChatbotConversationsRepository conversationRepository;
    private final ChatbotMeetingsSuggestionRepository suggestionRepository;
    private final MeetingsRepository meetingsRepository;

    @Override
    public boolean healthCheck() {
        try {
            String model = chatbotConfig.getModelName();
            GenerateContentConfig pingCfg = GenerateContentConfig.builder()
                    .maxOutputTokens(1)
                    .temperature(0.0f)
                    .build();
            GenerateContentResponse res = genAiClient.models.generateContent(model, "ping", pingCfg);
            return res != null && res.text() != null;
        } catch (Exception e) {
            log.warn("GenAI healthCheck 실패", e);
            return false;
        }
    }

    @Override
    @Transactional
    public ChatbotMessageResponse processAuthenticatedMessage(String providerId, String userMessage) {
        try {
            User user = usersRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 개인화된 프롬프트 생성 (사용자 정보 + 실시간 모임 정보 포함)
            String systemPrompt = buildPersonalizedPrompt(user);
            String aiResponse = generateAIResponse(systemPrompt + "\n\n사용자 질문: " + userMessage);

            ChatbotConversation.IntentType intentType = determineIntentType(userMessage);
            ChatbotConversation conversation = saveConversation(user, userMessage, aiResponse, intentType);

            // 모임 검색 의도일 때만 추천 모임 생성
            List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestions = new ArrayList<>();
            if (intentType == ChatbotConversation.IntentType.MEETING_SEARCH) {
                suggestions = createMeetingSuggestions(conversation, userMessage);
            }

            return ChatbotMessageResponse.builder()
                    .response(aiResponse)
                    .suggestedMeetings(suggestions)
                    .intentType(intentType)
                    .conversationId(conversation.getConversationId())
                    .build();

        } catch (Exception e) {
            log.error("로그인 사용자 메시지 처리 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ChatbotMessageResponse processGuestMessage(String userMessage) {
        log.info("=== 게스트 메시지 처리 시작 ===");
        log.info("입력 메시지: {}", userMessage);

        try {
            // 1. API 키 상태 확인
            String apiKey = chatbotConfig.getApiKey();
            boolean hasApiKey = apiKey != null && !apiKey.trim().isEmpty();
            log.info("Google AI API 키 상태: {}", hasApiKey ? "설정됨" : "누락됨");

            // 2. 데이터베이스에서 실제 모임 정보 조회
            List<Meeting> activeMeetings = null;
            int meetingCount = 0;
            try {
                log.info("데이터베이스 조회 시작...");
                activeMeetings = meetingsRepository.findByStatusAndDeletedAtIsNull(Meeting.MeetingStatus.RECRUITING);
                meetingCount = activeMeetings != null ? activeMeetings.size() : 0;
                log.info("✅ 데이터베이스 조회 성공: {}개의 활성 모임 발견", meetingCount);

                // 모임 정보 상세 로그
                if (activeMeetings != null && !activeMeetings.isEmpty()) {
                    log.info("모임 목록:");
                    activeMeetings.forEach(meeting -> {
                        log.info("- {}: {} ({}명/{}) at {}",
                                meeting.getTitle(),
                                meeting.getMart().getMartName(),
                                meeting.getCurrentParticipants(),
                                meeting.getMaxParticipants(),
                                meeting.getMeetingDate().toLocalDate());
                    });
                }
            } catch (Exception dbError) {
                log.error("❌ 데이터베이스 조회 실패", dbError);
                activeMeetings = new ArrayList<>();
            }

            // 3. 사용자 질문 분석
            String lowerMessage = userMessage.toLowerCase();
            boolean isSpecificQuery = lowerMessage.contains("양재") || lowerMessage.contains("상봉") ||
                    lowerMessage.contains("견과류") || lowerMessage.contains("냉동") ||
                    lowerMessage.contains("세제") || lowerMessage.contains("육류");

            log.info("특정 질문 여부: {}", isSpecificQuery);

            // 4. AI API 사용 가능 여부에 따른 응답 전략
            String response;

            if (hasApiKey && meetingCount > 0) {
                // AI API + 실제 데이터로 응답 시도
                log.info("AI API + 실제 데이터로 응답 생성 시도");
                try {
                    String systemPrompt = buildEnhancedSystemPrompt(activeMeetings);
                    String fullPrompt = systemPrompt + "\n\n사용자 질문: " + userMessage;
                    log.info("프롬프트 길이: {}", fullPrompt.length());

                    response = generateAIResponse(fullPrompt);
                    log.info("✅ AI 응답 생성 성공 (길이: {})", response.length());
                } catch (Exception aiError) {
                    log.error("❌ AI API 호출 실패, 데이터베이스 기반 응답으로 전환", aiError);
                    response = buildDatabaseBasedResponse(userMessage, activeMeetings, lowerMessage);
                }
            } else {
                // 데이터베이스 기반 직접 응답
                log.info("데이터베이스 기반 직접 응답 생성 (API 키: {}, 모임 수: {})", hasApiKey, meetingCount);
                response = buildDatabaseBasedResponse(userMessage, activeMeetings, lowerMessage);
            }

            ChatbotConversation.IntentType intentType = determineIntentType(userMessage);
            log.info("의도 타입: {}", intentType);
            log.info("=== 게스트 메시지 처리 완료 ===");

            return ChatbotMessageResponse.builder()
                    .response(response)
                    .suggestedMeetings(new ArrayList<>())
                    .intentType(intentType)
                    .build();

        } catch (Exception e) {
            log.error("=== 게스트 메시지 처리 중 최종 오류 ===", e);
            return ChatbotMessageResponse.builder()
                    .response(getEmergencyFallbackResponse(userMessage))
                    .suggestedMeetings(new ArrayList<>())
                    .intentType(ChatbotConversation.IntentType.GENERAL)
                    .build();
        }
    }

    /**
     * 긴급 상황용 기본 응답
     */
    private String getEmergencyFallbackResponse(String userMessage) {
        return String.format("""
            안녕하세요! 반띵 AI 도우미입니다.
            
            현재 시스템에 일시적인 문제가 발생했지만, 기본 정보를 안내해드릴게요.
            
            문의하신 내용: "%s"
            
            반띵은 대용량 상품을 여러 명이 함께 구매하고 소분하는 서비스예요.
            서울 지역 8개 마트(코스트코, 이마트 트레이더스, 롯데마트)에서 
            다양한 소분 모임이 진행되고 있습니다.
            
            로그인 후 정확한 모임 정보를 확인하실 수 있어요!
            """, userMessage);
    }

    /**
     * 데이터베이스 정보를 포함한 강화된 시스템 프롬프트
     */
    private String buildEnhancedSystemPrompt(List<Meeting> activeMeetings) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(chatbotConfig.getGuestSystemPrompt());

        if (activeMeetings != null && !activeMeetings.isEmpty()) {
            prompt.append("\n\n# 현재 실제 진행 중인 모임 정보\n");

            activeMeetings.forEach(meeting -> {
                prompt.append("모임명: ").append(meeting.getTitle()).append("\n");
                prompt.append("위치: ").append(meeting.getMart().getMartName())
                        .append(" (").append(meeting.getMart().getAddress()).append(")\n");
                prompt.append("일시: ").append(meeting.getMeetingDate().toLocalDate()).append("\n");
                prompt.append("참여현황: ").append(meeting.getCurrentParticipants())
                        .append("/").append(meeting.getMaxParticipants()).append("명\n");
                prompt.append("설명: ").append(meeting.getDescription()).append("\n");
                prompt.append("---\n");
            });

            prompt.append("\n위의 실제 모임 정보를 바탕으로 사용자의 질문에 구체적이고 정확하게 답변해주세요.");
        }

        return prompt.toString();
    }

    /**
     * 데이터베이스 기반 직접 응답 생성
     */
    private String buildDatabaseBasedResponse(String userMessage, List<Meeting> activeMeetings, String lowerMessage) {
        StringBuilder response = new StringBuilder();
        response.append("안녕하세요! 반띵 AI 도우미입니다.\n\n");

        if (activeMeetings != null && !activeMeetings.isEmpty()) {
            response.append("현재 서울 지역에서 총 ").append(activeMeetings.size()).append("개의 소분 모임이 진행 중이에요!\n\n");

            // 사용자 질문에 맞는 특정 모임 찾기
            List<Meeting> matchedMeetings = activeMeetings.stream()
                    .filter(meeting -> {
                        String title = meeting.getTitle().toLowerCase();
                        String martName = meeting.getMart().getMartName().toLowerCase();

                        if (lowerMessage.contains("양재")) return martName.contains("양재");
                        if (lowerMessage.contains("상봉")) return martName.contains("상봉");
                        if (lowerMessage.contains("견과류")) return title.contains("견과") || title.contains("아몬드") || title.contains("호두");
                        if (lowerMessage.contains("냉동")) return title.contains("냉동");
                        if (lowerMessage.contains("세제")) return title.contains("세제") || title.contains("다우니");
                        if (lowerMessage.contains("육류")) return title.contains("육류") || title.contains("고기") || title.contains("삼겹살");

                        return false;
                    })
                    .limit(3)
                    .collect(Collectors.toList());

            if (!matchedMeetings.isEmpty()) {
                response.append("문의하신 내용과 관련된 모임을 찾았어요:\n\n");
                matchedMeetings.forEach(meeting -> {
                    response.append("📋 ").append(meeting.getTitle()).append("\n");
                    response.append("   위치: ").append(meeting.getMart().getMartName()).append("\n");
                    response.append("   일시: ").append(meeting.getMeetingDate().toLocalDate()).append("\n");
                    response.append("   참여현황: ").append(meeting.getCurrentParticipants())
                            .append("/").append(meeting.getMaxParticipants()).append("명\n");
                    response.append("   설명: ").append(meeting.getDescription()).append("\n\n");
                });
            } else {
                response.append("현재 진행 중인 모임 예시:\n\n");
                activeMeetings.stream().limit(3).forEach(meeting -> {
                    response.append("📋 ").append(meeting.getTitle())
                            .append(" (").append(meeting.getMart().getMartName()).append(")\n");
                    response.append("   일시: ").append(meeting.getMeetingDate().toLocalDate())
                            .append(" (").append(meeting.getCurrentParticipants())
                            .append("/").append(meeting.getMaxParticipants()).append("명)\n\n");
                });

                if (lowerMessage.contains("양재") || lowerMessage.contains("견과류")) {
                    response.append("양재 코스트코에서의 견과류 소분 모임은 평소 인기가 높은 모임이에요! ");
                } else if (lowerMessage.contains("상봉") || lowerMessage.contains("냉동")) {
                    response.append("상봉 코스트코는 냉동식품 소분이 활발한 지점이에요! ");
                }
            }
        } else {
            response.append("현재 새로운 모임이 준비 중입니다.\n\n");
            response.append("평소에는 서울 지역 8개 마트에서 다양한 소분 모임이 활발히 진행돼요:\n\n");
        }

        response.append("🏪 이용 가능한 마트:\n");
        response.append("• 코스트코 (양평점, 양재점, 상봉점, 고척점)\n");
        response.append("• 이마트 트레이더스 (월계점, 마곡점)\n");
        response.append("• 롯데마트 맥스 (금천점, 영등포점)\n\n");
        response.append("로그인하시면 더 자세한 정보와 참여 신청이 가능해요!");

        return response.toString();
    }


    @Override
    @Transactional(readOnly = true)
    public List<ChatbotConversationHistoryResponse> getChatHistory(String providerId) {
        try {
            User user = usersRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            var pageable = PageRequest.of(0, 10);
            List<ChatbotConversation> conversations = conversationRepository
                    .findByUser_UserIdOrderByCreatedAtDesc(user.getUserId(), pageable);

            return conversations.stream()
                    .map(this::convertToHistoryResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("대화 기록 조회 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== Private 헬퍼 메서드들 =====

    private String generateAIResponse(String fullPrompt) {
        try {
            GenerateContentResponse response = genAiClient.models.generateContent(
                    chatbotConfig.getModelName(), fullPrompt, genConfig);

            String text = response.text();
            if (text == null || text.isBlank()) {
                log.warn("GenAI로부터 빈 응답을 받았습니다.");
                return "죄송합니다. 현재 답변을 생성할 수 없습니다. 다시 시도해주세요.";
            }
            return text.trim();

        } catch (Exception e) {
            log.error("GenAI API 호출 중 오류 발생", e);
            return "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private ChatbotConversation.IntentType determineIntentType(String message) {
        String msg = message.toLowerCase();

        if (containsAny(msg, "모임", "찾", "검색", "소분", "함께", "나눔", "지역", "근처", "마트", "추천")) {
            return ChatbotConversation.IntentType.MEETING_SEARCH;
        }
        if (containsAny(msg, "이용", "방법", "가이드", "가입", "시작", "어떻게", "준비", "위생", "안전")) {
            return ChatbotConversation.IntentType.SERVICE_GUIDE;
        }
        return ChatbotConversation.IntentType.GENERAL;
    }

    private String buildPersonalizedPrompt(User user) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(chatbotConfig.getSystemPrompt());

        // 시스템 프롬프트에 추가 지침
        promptBuilder.append("\n\n# 절대 금지사항 (CRITICAL)\n");
        promptBuilder.append("- 절대로 '[여기에 링크 삽입]', '[링크]', 'URL', 'https://' 등의 표현을 사용하지 마세요.\n");
        promptBuilder.append("- '링크를 클릭해주세요', '아래 링크에서' 같은 표현도 금지입니다.\n");
        promptBuilder.append("- 회원가입 안내 시에는 오직 '아래 카카오로 시작하기 버튼을 이용해주세요'라고만 말하세요.\n");
        promptBuilder.append("- 프론트엔드에서 자동으로 버튼이 표시되므로 링크나 URL 관련 언급은 절대 금지입니다.\n");
        promptBuilder.append("- 이 규칙을 위반하면 사용자에게 혼란을 줍니다. 반드시 준수하세요.\n");

        promptBuilder.append("\n\n# 현재 사용자 정보\n");
        promptBuilder.append("- 닉네임: ").append(user.getNickname()).append("\n");
        promptBuilder.append("- 신뢰도: ").append(user.getTrustScore()).append("점 (")
                .append(user.getTrustGrade().name()).append(" 등급)\n");

        if (user.getNoShowCount() > 0) {
            promptBuilder.append("- 노쇼 이력: ").append(user.getNoShowCount()).append("회\n");
        }

        // 현재 활성 모임 정보 추가
        List<Meeting> activeMeetings = meetingsRepository.findByStatusAndDeletedAtIsNull(Meeting.MeetingStatus.RECRUITING);
        if (!activeMeetings.isEmpty()) {
            promptBuilder.append("\n# 현재 모집 중인 모임 (최신 5개)\n");
            activeMeetings.stream().limit(5).forEach(meeting -> {
                promptBuilder.append("- ").append(meeting.getTitle())
                        .append(" (").append(meeting.getMart().getMartName()).append(", ")
                        .append(meeting.getMeetingDate().toLocalDate()).append(")\n");
            });
        }

        return promptBuilder.toString();
    }

    private List<ChatbotMessageResponse.MeetingSuggestionResponse> createMeetingSuggestions(
            ChatbotConversation conversation, String userMessage) {

        List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestions = new ArrayList<>();
        try {
            List<Meeting> activeMeetings = meetingsRepository.findByStatusAndDeletedAtIsNull(Meeting.MeetingStatus.RECRUITING);
            List<Meeting> recommendedMeetings = activeMeetings.stream().limit(3).collect(Collectors.toList());

            for (Meeting meeting : recommendedMeetings) {
                String suggestionReason = "사용자 질문과 관련된 " + meeting.getMart().getMartName() + " 모임입니다.";

                // 추천 저장
                ChatbotMeetingSuggestion suggestion = newMeetingSuggestionInstance();
                suggestion.setConversation(conversation);
                suggestion.setMeeting(meeting);
                suggestion.setSuggestionReason(suggestionReason);
                suggestionRepository.save(suggestion);

                // 응답 DTO 생성
                suggestions.add(ChatbotMessageResponse.MeetingSuggestionResponse.builder()
                        .meetingId(meeting.getMeetingId())
                        .title(meeting.getTitle())
                        .martName(meeting.getMart().getMartName())
                        .meetingDate(meeting.getMeetingDate())
                        .suggestionReason(suggestionReason)
                        .currentParticipants(meeting.getCurrentParticipants())
                        .maxParticipants(meeting.getMaxParticipants())
                        .build());
            }

        } catch (Exception e) {
            log.error("모임 추천 중 오류 발생", e);
        }
        return suggestions;
    }

    private ChatbotConversation saveConversation(User user, String userMessage, String botResponse, ChatbotConversation.IntentType intentType) {
        ChatbotConversation conversation = newConversationInstance();
        conversation.setUser(user);
        conversation.setUserMessage(userMessage);
        conversation.setBotResponse(botResponse);
        conversation.setIntentType(intentType);
        return conversationRepository.save(conversation);
    }

    private ChatbotConversation newConversationInstance() {
        try {
            Constructor<ChatbotConversation> ctor = ChatbotConversation.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("ChatbotConversation 인스턴스 생성 실패", e);
        }
    }

    private ChatbotMeetingSuggestion newMeetingSuggestionInstance() {
        try {
            Constructor<ChatbotMeetingSuggestion> ctor = ChatbotMeetingSuggestion.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("ChatbotMeetingSuggestion 인스턴스 생성 실패", e);
        }
    }

    private ChatbotConversationHistoryResponse convertToHistoryResponse(ChatbotConversation conversation) {
        List<ChatbotConversationHistoryResponse.MeetingSuggestionInfo> suggestionInfos =
                conversation.getMeetingSuggestions().stream()
                        .map(s -> ChatbotConversationHistoryResponse.MeetingSuggestionInfo.builder()
                                .meetingId(s.getMeeting().getMeetingId())
                                .title(s.getMeeting().getTitle())
                                .suggestionReason(s.getSuggestionReason())
                                .build())
                        .collect(Collectors.toList());

        return ChatbotConversationHistoryResponse.builder()
                .conversationId(conversation.getConversationId())
                .userMessage(conversation.getUserMessage())
                .botResponse(conversation.getBotResponse())
                .intentType(conversation.getIntentType())
                .createdAt(conversation.getCreatedAt())
                .suggestedMeetings(suggestionInfos)
                .build();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}