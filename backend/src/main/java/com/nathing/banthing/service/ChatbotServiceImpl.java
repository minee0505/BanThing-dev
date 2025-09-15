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
import com.nathing.banthing.repository.ChatbotMeetingSuggestionRepository;
import com.nathing.banthing.repository.MeetingsRepository;
import com.nathing.banthing.repository.UsersRepository;
import com.nathing.banthing.service.ChatbotService;

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
    private final ChatbotMeetingSuggestionRepository suggestionRepository;
    private final MeetingsRepository meetingsRepository;

    @PersistenceContext
    private EntityManager em;

    // ===== Public API =====

    @Override
    public ChatbotMessageResponse processMessage(ChatbotMessageRequest request, Long userId) {
        try {
            // 1) 사용자 검증
            User user = usersRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 2) AI 응답 생성
            String aiResponse = generateAIResponse(request.getMessage());

            // 3) 의도 분석(레거시 문자열 기반)
            ChatbotConversation.IntentType intentType = determineIntentType(request.getMessage());

            // 4) 대화 저장 (엔티티 생성자 protected → 리플렉션으로 생성)
            ChatbotConversation conversation =
                    saveConversation(user, request.getMessage(), aiResponse, intentType);

            // 5) 모임 추천 (필요 시)
            List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestions = new ArrayList<>();
            if (intentType == ChatbotConversation.IntentType.MEETING_SEARCH) {
                suggestions = findAndSuggestMeetings(conversation, request.getMessage());
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

        // Pageable 기반 최신 N개
        var pageable = PageRequest.of(0, Math.max(limit, 0));
        List<ChatbotConversation> conversations =
                conversationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);

        return conversations.stream()
                .map(this::convertToHistoryResponse)
                .collect(Collectors.toList());
    }

    // (V2) Enum 기반 의도 분석 구현
    @Override
    public IntentResult analyzeIntentV2(String userMessage) {
        if (userMessage == null) return new IntentResult(IntentType.UNKNOWN, 0.0);
        String msg = userMessage.toLowerCase();

        if (containsAny(msg, "모임", "찾아", "검색", "소분", "함께", "나눔", "지역", "근처", "마트")) {
            return new IntentResult(IntentType.FIND_GROUPS, 0.7);
        }
        if (containsAny(msg, "모임 생성", "개설", "주최", "호스트")) {
            return new IntentResult(IntentType.CREATE_GROUP, 0.7);
        }
        if (containsAny(msg, "위생", "준비물", "보관", "소분 방법", "용기")) {
            return new IntentResult(IntentType.HYGIENE_GUIDE, 0.7);
        }
        if (containsAny(msg, "이용법", "결제", "환불", "정원", "계정", "로그인", "회원", "가입")) {
            return new IntentResult(IntentType.HOW_TO_USE, 0.7);
        }
        if (containsAny(msg, "안녕", "고마워", "뭐해", "잡담")) {
            return new IntentResult(IntentType.SMALL_TALK, 0.6);
        }
        return new IntentResult(IntentType.UNKNOWN, 0.3);
    }

    // 레거시 문자열 시그니처 – 내부적으로 V2 매핑 (default 추가로 모든 enum 커버)
    @Override
    public String analyzeIntent(String userMessage) {
        IntentResult r = analyzeIntentV2(userMessage);
        return switch (r.type()) {
            case FIND_GROUPS -> "MEETING_SEARCH";
            case HYGIENE_GUIDE, HOW_TO_USE -> "SERVICE_GUIDE";
            // ChatbotConversation.IntentType에는 CREATE_GROUP이 없으므로 GENERAL로 흡수
            case CREATE_GROUP -> "GENERAL";
            case SMALL_TALK, UNKNOWN -> "GENERAL";
            case FEEDBACK -> "GENERAL";
            default -> "GENERAL";
        };
    }

    // 대화 비우기 (JPQL delete)
    @Override
    public void clearConversation(Long userId) {
        em.createQuery("delete from ChatbotConversation c where c.user.userId = :uid")
                .setParameter("uid", userId)
                .executeUpdate();
    }

    // 헬스 체크 (최소 토큰 호출)
    @Override
    public boolean healthCheck() {
        try {
            String model = chatbotConfig.getModelName();
            GenerateContentConfig pingCfg = GenerateContentConfig.builder()
                    .maxOutputTokens(1)
                    .temperature(0.0f)
                    .build();
            GenerateContentResponse res =
                    genAiClient.models.generateContent(model, "ping", pingCfg);
            return res != null && res.text() != null;
        } catch (Exception e) {
            log.warn("GenAI healthCheck 실패", e);
            return false;
        }
    }

    // ===== 내부 헬퍼 =====

    private String generateAIResponse(String userMessage) {
        try {
            String modelName = chatbotConfig.getModelName();
            String prompt = chatbotConfig.getSystemPrompt() + "\n\n사용자 질문: " + userMessage;

            GenerateContentResponse response =
                    genAiClient.models.generateContent(modelName, prompt, genConfig);

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
        String legacy = analyzeIntent(message);
        return switch (legacy) {
            case "MEETING_SEARCH" -> ChatbotConversation.IntentType.MEETING_SEARCH;
            case "SERVICE_GUIDE"  -> ChatbotConversation.IntentType.SERVICE_GUIDE;
            // CREATE_GROUP은 엔티티 Enum에 없으므로 제거
            default               -> ChatbotConversation.IntentType.GENERAL;
        };
    }

    // 엔티티 생성자 protected → 리플렉션으로 안전 생성 (엔티티 수정 없이 사용)
    private ChatbotConversation newConversationInstance() {
        try {
            Constructor<ChatbotConversation> ctor =
                    ChatbotConversation.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("ChatbotConversation 인스턴스 생성 실패", e);
        }
    }

    // ChatbotMeetingSuggestion 역시 protected 생성자 → 리플렉션 팩토리
    private ChatbotMeetingSuggestion newMeetingSuggestionInstance() {
        try {
            Constructor<ChatbotMeetingSuggestion> ctor =
                    ChatbotMeetingSuggestion.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("ChatbotMeetingSuggestion 인스턴스 생성 실패", e);
        }
    }

    private ChatbotConversation saveConversation(
            User user, String userMessage, String botResponse, ChatbotConversation.IntentType intentType) {

        ChatbotConversation conversation = newConversationInstance();
        conversation.setUser(user);
        conversation.setUserMessage(userMessage);
        conversation.setBotResponse(botResponse);
        conversation.setIntentType(intentType);
        return conversationRepository.save(conversation);
    }

    private List<ChatbotMessageResponse.MeetingSuggestionResponse> findAndSuggestMeetings(
            ChatbotConversation conversation, String userMessage) {

        List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestions = new ArrayList<>();
        try {
            List<Meeting> recommendedMeetings = searchMeetingsByKeywords(userMessage);

            int size = Math.min(3, recommendedMeetings.size());
            for (int i = 0; i < size; i++) {
                Meeting meeting = recommendedMeetings.get(i);

                String suggestionReason = generateSuggestionReason(meeting, userMessage);

                saveMeetingSuggestion(conversation, meeting, suggestionReason);

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

    private List<Meeting> searchMeetingsByKeywords(String userMessage) {
        // 기본적으로 모든 RECRUITING 상태 모임 조회 (팀원 코드에 맞춰서 수정)
        List<Meeting> allMeetings = meetingsRepository.findAll().stream()
                .filter(meeting -> meeting.getStatus() == Meeting.MeetingStatus.RECRUITING)
                .filter(meeting -> !meeting.isDeleted())
                .collect(Collectors.toList());

        String lowerMessage = userMessage.toLowerCase();
        List<Meeting> filteredMeetings = new ArrayList<>();

        for (Meeting meeting : allMeetings) {
            int score = 0;
            String title = meeting.getTitle().toLowerCase();
            String description = meeting.getDescription() != null ? meeting.getDescription().toLowerCase() : "";
            String martName = meeting.getMart().getMartName().toLowerCase();

            if (containsKeywords(title, lowerMessage)) score += 3;
            if (containsKeywords(description, lowerMessage)) score += 2;
            if (containsKeywords(martName, lowerMessage)) score += 1;
            if (containsLocationKeywords(lowerMessage, meeting.getMart().getAddress())) score += 2;

            if (score > 0) filteredMeetings.add(meeting);
        }

        return filteredMeetings.stream().limit(5).collect(Collectors.toList());
    }

    private boolean containsKeywords(String text, String userMessage) {
        String[] commonKeywords = {"세제", "견과류", "과일", "고기", "쌀", "빵", "냉동", "유제품"};
        for (String keyword : commonKeywords) {
            if (userMessage.contains(keyword) && text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsLocationKeywords(String userMessage, String martAddress) {
        String[] locationKeywords = {"양재", "상봉", "송도", "광명", "의정부", "월계", "킨텍스", "영등포", "하남", "서울역"};
        for (String location : locationKeywords) {
            if (userMessage.contains(location) && martAddress.contains(location)) {
                return true;
            }
        }
        return false;
    }

    private String generateSuggestionReason(Meeting meeting, String userMessage) {
        String reason = "사용자 요청과 유사한 모임";
        if (userMessage.toLowerCase().contains(meeting.getMart().getMartName().toLowerCase())) {
            reason = "요청하신 " + meeting.getMart().getMartName() + "에서 진행되는 모임";
        } else if (containsKeywords(meeting.getTitle().toLowerCase(), userMessage.toLowerCase())) {
            reason = "요청하신 상품과 유사한 소분 모임";
        }
        return reason;
    }

    private void saveMeetingSuggestion(ChatbotConversation conversation, Meeting meeting, String reason) {
        ChatbotMeetingSuggestion suggestion = newMeetingSuggestionInstance();
        suggestion.setConversation(conversation);
        suggestion.setMeeting(meeting);
        suggestion.setSuggestionReason(reason);
        suggestionRepository.save(suggestion);
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