package com.nathing.banthing.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

import com.nathing.banthing.config.ChatbotConfig;
import com.nathing.banthing.dto.request.ChatbotMessageRequest;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @PersistenceContext
    private EntityManager em;

    // ===== 기존 메서드들 (레거시 호환성) =====

    @Override
    public ChatbotMessageResponse processMessage(ChatbotMessageRequest request, Long userId) {
        try {
            User user = usersRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            String aiResponse = generateAIResponse(chatbotConfig.getSystemPrompt() + "\n\n사용자 질문: " + request.getMessage());
            ChatbotConversation.IntentType intentType = determineIntentType(request.getMessage());
            ChatbotConversation conversation = saveConversation(user, request.getMessage(), aiResponse, intentType);

            List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestions = new ArrayList<>();
            if (intentType == ChatbotConversation.IntentType.MEETING_SEARCH) {
                suggestions = createMeetingSuggestions(conversation, request.getMessage());
            }

            return ChatbotMessageResponse.builder()
                    .response(aiResponse)
                    .intentType(intentType)
                    .suggestedMeetings(suggestions)
                    .build();

        } catch (Exception e) {
            log.error("챗봇 메시지 처리 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatbotConversationHistoryResponse> getConversationHistory(Long userId, int limit) {
        if (!usersRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        var pageable = PageRequest.of(0, Math.max(limit, 0));
        List<ChatbotConversation> conversations = conversationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);

        return conversations.stream()
                .map(this::convertToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void clearConversation(Long userId) {
        em.createQuery("delete from ChatbotConversation c where c.user.userId = :uid")
                .setParameter("uid", userId)
                .executeUpdate();
    }

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

    // ===== 새로운 메서드들 (로그인 선택적) =====

    @Override
    @Transactional
    public ChatbotMessageResponse processAuthenticatedMessage(String providerId, String userMessage) {
        try {
            User user = usersRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            String systemPrompt = buildPersonalizedPrompt(user);
            String aiResponse = generateAIResponse(systemPrompt + "\n\n사용자 질문: " + userMessage);
            ChatbotConversation.IntentType intentType = determineIntentType(userMessage);
            ChatbotConversation conversation = saveConversation(user, userMessage, aiResponse, intentType);

            List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestions = new ArrayList<>();
            if (intentType == ChatbotConversation.IntentType.MEETING_SEARCH) {
                suggestions = createMeetingSuggestions(conversation, userMessage);
            }

            return ChatbotMessageResponse.builder()
                    .response(aiResponse)
                    .suggestedMeetings(suggestions)
                    .intentType(intentType)
                    .build();

        } catch (Exception e) {
            log.error("로그인 사용자 메시지 처리 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ChatbotMessageResponse processGuestMessage(String userMessage) {
        try {
            String systemPrompt = chatbotConfig.getGuestSystemPrompt();
            String aiResponse = generateAIResponse(systemPrompt + "\n\n사용자 질문: " + userMessage);
            ChatbotConversation.IntentType intentType = determineIntentType(userMessage);

            return ChatbotMessageResponse.builder()
                    .response(aiResponse)
                    .suggestedMeetings(new ArrayList<>()) // 게스트는 모임 추천 안함
                    .intentType(intentType)
                    .build();

        } catch (Exception e) {
            log.error("게스트 사용자 메시지 처리 중 오류 발생", e);
            return ChatbotMessageResponse.builder()
                    .response(getDefaultGuestResponse())
                    .suggestedMeetings(new ArrayList<>())
                    .intentType(ChatbotConversation.IntentType.GENERAL)
                    .build();
        }
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

    private String getDefaultGuestResponse() {
        return """
                안녕하세요! 반띵 AI 도우미입니다.
                
                현재 AI 서버와 연결이 불안정하지만, 기본 정보를 안내해드릴게요:
                
                반띵 서비스는 대용량 상품을 여러 명이 나눠 구매하는 플랫폼이에요.
                
                서울 지역 8개 마트에서 다양한 소분 모임이 활발히 진행되고 있어요!
                
                로그인하시면 더 정확한 정보와 개인 맞춤 추천을 받으실 수 있어요.
                
                궁금한 점이 있으시면 언제든 말씀해주세요!
                """;
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