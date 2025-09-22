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
import com.nathing.banthing.entity.Mart;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 김경민
 * @since 2025-09-16
 * 반띵 AI 챗봇 서비스 구현체
 *
 * 핵심 기능:
 * 1. 게스트 사용자를 위한 기본 챗봇 서비스 (회원가입 유도)
 * 2. 로그인 사용자를 위한 개인화된 챗봇 서비스 (대화 기록 저장)
 * 3. AI API 장애 시 대체 로직으로 서비스 연속성 보장
 * 4. 실시간 모임 정보를 활용한 맞춤형 답변 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatbotServiceImpl implements ChatbotService {

    // Google Gemini AI 관련 빈들
    private final Client genAiClient;
    private final GenerateContentConfig genConfig;
    private final ChatbotConfig chatbotConfig;

    // 데이터베이스 레포지토리들
    private final UsersRepository usersRepository;
    private final ChatbotConversationsRepository conversationRepository;
    private final ChatbotMeetingsSuggestionRepository suggestionRepository;
    private final MeetingsRepository meetingsRepository;

    /**
     * Google Gemini AI API 상태 확인
     * API 키 유효성과 서비스 연결 상태를 점검합니다.
     */
    @Override
    public boolean healthCheck() {
        try {
            // API 키가 설정되어 있는지 확인
            if (chatbotConfig.getApiKey() == null || chatbotConfig.getApiKey().trim().isEmpty()) {
                log.warn("Google AI API Key가 설정되지 않음");
                return false;
            }

            // 간단한 ping 요청으로 API 연결 테스트
            String model = chatbotConfig.getModelName();
            GenerateContentConfig pingConfig = GenerateContentConfig.builder()
                    .maxOutputTokens(1)
                    .temperature(0.0f)
                    .build();

            GenerateContentResponse response = genAiClient.models.generateContent(model, "ping", pingConfig);
            return response != null && response.text() != null;

        } catch (Exception e) {
            log.warn("Google AI API 헬스체크 실패", e);
            return false;
        }
    }

    /**
     * 로그인한 사용자의 챗봇 메시지 처리
     * 개인화된 응답 생성 + 대화 기록 저장 + 모임 추천
     */
    @Override
    @Transactional
    public ChatbotMessageResponse processAuthenticatedMessage(String providerId, String userMessage) {
        try {
            // 1. 사용자 정보 조회
            User user = usersRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 2. 현재 모집중인 모임 목록 조회 (실시간 데이터)
            List<Meeting> activeMeetings = meetingsRepository.findByStatusAndDeletedAtIsNull(Meeting.MeetingStatus.RECRUITING);
            log.info("현재 활성 모임 수: {}", activeMeetings.size());

            // 3. AI 응답 생성 시도
            String botResponse = generateAuthenticatedResponse(user, userMessage, activeMeetings);

            // 4. 대화 의도 파악
            ChatbotConversation.IntentType intentType = determineIntentType(userMessage);

            // 5. 대화 기록 저장
            ChatbotConversation savedConversation = saveConversation(user, userMessage, botResponse, intentType);
            log.info("대화 기록 저장 완료 - conversationId: {}", savedConversation.getConversationId());

            // 6. 모임 추천 생성 (모임 검색 의도인 경우에만)
            List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestedMeetings = new ArrayList<>();
            if (intentType == ChatbotConversation.IntentType.MEETING_SEARCH) {
                suggestedMeetings = generateMeetingSuggestions(savedConversation, userMessage, activeMeetings);
                log.info("모임 추천 생성 완료 - 추천 수: {}", suggestedMeetings.size());
            }

            // 7. 응답 객체 생성
            return ChatbotMessageResponse.builder()
                    .response(botResponse)
                    .suggestedMeetings(suggestedMeetings)
                    .intentType(intentType)
                    .conversationId(savedConversation.getConversationId())
                    .build();

        } catch (BusinessException e) {
            log.error("비즈니스 로직 오류: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("로그인 사용자 챗봇 처리 중 예상치 못한 오류", e);
            throw new RuntimeException("챗봇 서비스 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 게스트(비로그인) 사용자의 챗봇 메시지 처리
     * 기본 응답 생성 + 회원가입 유도
     */
    @Override
    @Transactional(readOnly = true)
    public ChatbotMessageResponse processGuestMessage(String userMessage) {
        try {
            log.info("=== 게스트 사용자 챗봇 처리 시작 ===");
            log.info("메시지: {}", userMessage);

            // 1. 현재 모집중인 모임 목록 조회
            List<Meeting> activeMeetings = meetingsRepository.findByStatusAndDeletedAtIsNull(Meeting.MeetingStatus.RECRUITING);
            log.info("현재 활성 모임 수: {}", activeMeetings.size());

            // 2. 의도 분류 (추가!)
            ChatbotConversation.IntentType intentType = determineIntentType(userMessage);

            // 3. 게스트 응답 생성
            String botResponse = generateGuestResponse(userMessage, activeMeetings);

            // 4. 키워드 기반 모임 추천 (추가!)
            List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestedMeetings = new ArrayList<>();
            if (intentType == ChatbotConversation.IntentType.MEETING_SEARCH && !activeMeetings.isEmpty()) {
                // 키워드 기반으로 관련 모임 찾기
                List<String> keywords = extractKeywords(userMessage);
                List<Meeting> relevantMeetings = findRelevantMeetings(keywords, activeMeetings);

                // 관련 모임이 없으면 최신 모임 3개 추천
                if (relevantMeetings.isEmpty()) {
                    relevantMeetings = activeMeetings.stream()
                            .limit(3)
                            .collect(Collectors.toList());
                }

                // 게스트용 모임 추천 DTO 생성 (저장하지 않음)
                for (Meeting meeting : relevantMeetings) {
                    String suggestionReason = generateSuggestionReason(userMessage, meeting, keywords);

                    suggestedMeetings.add(ChatbotMessageResponse.MeetingSuggestionResponse.builder()
                            .meetingId(meeting.getMeetingId())
                            .title(meeting.getTitle())
                            .martName(meeting.getMart().getMartName())
                            .meetingDate(meeting.getMeetingDate())
                            .suggestionReason(suggestionReason)
                            .currentParticipants(meeting.getCurrentParticipants())
                            .maxParticipants(meeting.getMaxParticipants())
                            .status(meeting.getStatus().toString())
                            .martAddress(meeting.getMart().getAddress())
                            .build());
                }
            }

            // 5. 응답 객체 생성
            return ChatbotMessageResponse.builder()
                    .response(botResponse)
                    .suggestedMeetings(suggestedMeetings) // ← 실제 추천 모임!
                    .intentType(intentType) // ← 실제 의도!
                    .conversationId(null)
                    .build();

        } catch (Exception e) {
            log.error("게스트 사용자 챗봇 처리 중 오류", e);

            // 오류 발생 시에도 기본 응답 제공
            String errorResponse = """
            현재 AI 서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요! 😊
            
            더 정확한 답변을 원하시면 아래 카카오로 시작하기 버튼을 이용해주세요!
            """;

            return ChatbotMessageResponse.builder()
                    .response(errorResponse)
                    .suggestedMeetings(new ArrayList<>())
                    .intentType(ChatbotConversation.IntentType.GENERAL)
                    .conversationId(null)
                    .build();
        }
    }

    /**
     * 로그인 사용자의 챗봇 대화 기록 조회
     * 최근 10개 대화를 최신순으로 반환
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChatbotConversationHistoryResponse> getChatHistory(String providerId) {
        try {
            log.info("=== 대화 기록 조회 시작 ===");
            log.info("providerId: {}", providerId);

            // 1. 사용자 정보 조회
            User user = usersRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 2. 최근 10개 대화 조회 (페이징 처리)
            PageRequest pageRequest = PageRequest.of(0, 10);
            List<ChatbotConversation> conversations = conversationRepository
                    .findByUser_UserIdOrderByCreatedAtDesc(user.getUserId(), pageRequest);

            log.info("조회된 대화 기록 수: {}", conversations.size());

            // 3. DTO로 변환하여 반환
            return conversations.stream()
                    .map(this::convertToHistoryResponse)
                    .collect(Collectors.toList());

        } catch (BusinessException e) {
            log.error("사용자 조회 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("대화 기록 조회 중 오류", e);
            throw new RuntimeException("대화 기록 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 로그인한 사용자를 위한 개인화된 AI 응답 생성
     * AI API 호출 실패 시 대체 로직으로 전환
     */
    private String generateAuthenticatedResponse(User user, String userMessage, List<Meeting> activeMeetings) {
        try {
            // 1. AI API 키 확인
            if (chatbotConfig.getApiKey() == null || chatbotConfig.getApiKey().trim().isEmpty()) {
                log.warn("AI API 키 없음 - 대체 로직 사용");
                return generateFallbackResponse(userMessage, activeMeetings, true);
            }

            // 2. 개인화된 시스템 프롬프트 생성
            String systemPrompt = buildPersonalizedPrompt(user, activeMeetings);

            // 3. AI 모델 호출
            String model = chatbotConfig.getModelName();
            String fullPrompt = systemPrompt + "\n\n사용자 질문: " + userMessage;

            GenerateContentResponse response = genAiClient.models.generateContent(model, fullPrompt, genConfig);

            if (response != null && response.text() != null && !response.text().trim().isEmpty()) {
                log.info("AI 응답 생성 성공");
                return response.text().trim();
            } else {
                log.warn("AI 응답이 비어있음 - 대체 로직 사용");
                return generateFallbackResponse(userMessage, activeMeetings, true);
            }

        } catch (Exception e) {
            log.error("AI 응답 생성 실패 - 대체 로직으로 전환", e);
            return generateFallbackResponse(userMessage, activeMeetings, true);
        }
    }

    /**
     * 게스트 사용자를 위한 기본 AI 응답 생성
     * AI API 호출 실패 시 대체 로직으로 전환
     */
    private String generateGuestResponse(String userMessage, List<Meeting> activeMeetings) {
        try {
            // 1. AI API 키 확인
            if (chatbotConfig.getApiKey() == null || chatbotConfig.getApiKey().trim().isEmpty()) {
                log.warn("AI API 키 없음 - 대체 로직 사용");
                return generateFallbackResponse(userMessage, activeMeetings, false);
            }

            // 2. 게스트용 시스템 프롬프트 생성
            String systemPrompt = buildGuestPrompt(activeMeetings);

            // 3. AI 모델 호출
            String model = chatbotConfig.getModelName();
            String fullPrompt = systemPrompt + "\n\n사용자 질문: " + userMessage;

            GenerateContentResponse response = genAiClient.models.generateContent(model, fullPrompt, genConfig);

            if (response != null && response.text() != null && !response.text().trim().isEmpty()) {
                log.info("게스트 AI 응답 생성 성공");
                return response.text().trim();
            } else {
                log.warn("AI 응답이 비어있음 - 대체 로직 사용");
                return generateFallbackResponse(userMessage, activeMeetings, false);
            }

        } catch (Exception e) {
            log.error("게스트 AI 응답 생성 실패 - 대체 로직으로 전환", e);
            return generateFallbackResponse(userMessage, activeMeetings, false);
        }
    }

    /**
     * 개인화된 시스템 프롬프트 생성
     * 사용자 정보 + 실시간 모임 정보를 포함한 상세 프롬프트
     */
    private String buildPersonalizedPrompt(User user, List<Meeting> activeMeetings) {
        StringBuilder prompt = new StringBuilder();

        // 1. 기본 시스템 프롬프트 추가
        prompt.append(chatbotConfig.getSystemPrompt()).append("\n\n");

        // 시스템 프롬프트에 추가 지침
        prompt.append("# 절대 금지사항 (CRITICAL)\n");
        prompt.append("- 절대로 '[여기에 링크 삽입]', '[링크]', 'URL', 'https://' 등의 표현을 사용하지 마세요.\n");
        prompt.append("- '링크를 클릭해주세요', '아래 링크에서' 같은 표현도 금지입니다.\n");
        prompt.append("- 회원가입 안내 시에는 오직 '아래 카카오로 시작하기 버튼을 이용해주세요'라고만 말하세요.\n");
        prompt.append("- 프론트엔드에서 자동으로 버튼이 표시되므로 링크나 URL 관련 언급은 절대 금지입니다.\n");
        prompt.append("- 이 규칙을 위반하면 사용자에게 혼란을 줍니다. 반드시 준수하세요.\n\n");

        // 2. 사용자 개인화 정보 추가
        prompt.append("# 현재 대화 상대 사용자 정보\n");
        prompt.append(String.format("- 닉네임: %s\n", user.getNickname()));
        prompt.append(String.format("- 신뢰도 점수: %d점\n", user.getTrustScore()));
        prompt.append(String.format("- 신뢰도 등급: %s\n", user.getTrustGrade()));
        prompt.append(String.format("- 노쇼 횟수: %d회\n\n", user.getNoShowCount()));

        // 3. 실시간 모임 정보 추가
        prompt.append("# 현재 진행 중인 모임 정보\n");
        if (activeMeetings.isEmpty()) {
            prompt.append("현재 모집중인 모임이 없습니다.\n");
        } else {
            prompt.append(String.format("총 %d개의 모임이 모집 중입니다:\n\n", activeMeetings.size()));
            for (int i = 0; i < activeMeetings.size(); i++) {
                Meeting meeting = activeMeetings.get(i);
                prompt.append(String.format("%d. [%s] %s\n", i + 1, meeting.getMart().getMartName(), meeting.getTitle()));
                prompt.append(String.format("   - 일시: %s\n", meeting.getMeetingDate()));
                prompt.append(String.format("   - 참여인원: %d/%d명\n", meeting.getCurrentParticipants(), meeting.getMaxParticipants()));
                prompt.append(String.format("   - 장소: %s\n\n", meeting.getMart().getAddress()));
            }
        }

        return prompt.toString();
    }

    /**
     * 게스트용 시스템 프롬프트 생성
     * 서비스 소개 + 회원가입 유도 + 실시간 모임 정보
     */
    private String buildGuestPrompt(List<Meeting> activeMeetings) {
        StringBuilder prompt = new StringBuilder();

        // 1. 게스트용 기본 프롬프트 추가
        prompt.append(chatbotConfig.getGuestSystemPrompt()).append("\n\n");

        prompt.append("# 로그인/회원가입 질문 처리 규칙\n");
        prompt.append("사용자가 '로그인', '회원가입', '가입' 관련 질문을 하면:\n");
        prompt.append("'아래 카카오로 시작하기 버튼을 이용해주세요!'라고만 간단히 답변하세요.\n");
        prompt.append("다른 설명이나 추가 안내는 하지 마세요.\n\n");

        // 2. 실시간 모임 정보 추가 (간략버전)
        prompt.append("# 현재 진행 중인 모임 정보 (예시)\n");
        if (activeMeetings.isEmpty()) {
            prompt.append("현재 새로운 모임이 준비 중입니다.\n");
        } else {
            prompt.append(String.format("현재 %d개의 모임이 모집 중입니다:\n\n", Math.min(3, activeMeetings.size())));
            for (int i = 0; i < Math.min(3, activeMeetings.size()); i++) {
                Meeting meeting = activeMeetings.get(i);
                prompt.append(String.format("- [%s] %s (참여: %d/%d명)\n",
                        meeting.getMart().getMartName(), meeting.getTitle(),
                        meeting.getCurrentParticipants(), meeting.getMaxParticipants()));
            }
        }

        return prompt.toString();
    }

    /**
     * AI API 실패 시 사용하는 대체 응답 생성 로직
     * 키워드 기반 매칭으로 관련 모임 찾기
     */
    private String generateFallbackResponse(String userMessage, List<Meeting> activeMeetings, boolean isAuthenticated) {
        StringBuilder response = new StringBuilder();

        if (isAuthenticated) {
            response.append("안녕하세요! 😊 반띵 AI 도우미입니다.\n\n");
        } else {
            response.append("안녕하세요! 😊 반띵에 오신 것을 환영합니다!\n\n");
        }

        // 로그인/회원가입 관련 질문인지 먼저 확인 (게스트만)
        if (!isAuthenticated) {
            String lowerMessage = userMessage.toLowerCase();
            if (lowerMessage.contains("로그인") || lowerMessage.contains("회원가입") ||
                    lowerMessage.contains("가입") || lowerMessage.contains("회원")) {
                return "아래 카카오로 시작하기 버튼을 이용해주세요!";
            }
        }

        // 키워드 기반 모임 검색
        List<String> keywords = extractKeywords(userMessage);
        List<Meeting> relevantMeetings = findRelevantMeetings(keywords, activeMeetings);

        if (!relevantMeetings.isEmpty()) {
            response.append("요청하신 내용과 관련된 모임을 찾았습니다:\n\n");
            for (int i = 0; i < Math.min(3, relevantMeetings.size()); i++) {
                Meeting meeting = relevantMeetings.get(i);
                response.append(String.format("🛒 [%s] %s\n", meeting.getMart().getMartName(), meeting.getTitle()));
                response.append(String.format("📅 일시: %s\n", meeting.getMeetingDate()));
                response.append(String.format("👥 참여: %d/%d명\n\n", meeting.getCurrentParticipants(), meeting.getMaxParticipants()));
            }
        } else if (!activeMeetings.isEmpty()) {
            response.append("현재 이런 모임들이 진행 중입니다:\n\n");
            for (int i = 0; i < Math.min(3, activeMeetings.size()); i++) {
                Meeting meeting = activeMeetings.get(i);
                response.append(String.format("🛒 [%s] %s\n", meeting.getMart().getMartName(), meeting.getTitle()));
                response.append(String.format("📅 일시: %s\n", meeting.getMeetingDate()));
                response.append(String.format("👥 참여: %d/%d명\n\n", meeting.getCurrentParticipants(), meeting.getMaxParticipants()));
            }
        } else {
            response.append("현재 새로운 모임이 준비 중입니다. 잠시 후 다시 확인해주세요!\n\n");
            response.append("반띵은 서울 지역 8개 마트에서 다양한 소분 모임을 제공합니다:\n");
            response.append("- 코스트코 4곳 (양평점, 양재점, 상봉점, 고척점)\n");
            response.append("- 이마트 트레이더스 2곳 (월계점, 마곡점)\n");
            response.append("- 롯데마트 맥스 2곳 (금천점, 영등포점)\n\n");
        }

        if (!isAuthenticated) {
            response.append("더 정확한 정보와 개인 맞춤 추천을 원하시면 아래 카카오로 시작하기 버튼을 이용해주세요!");
        }

        return response.toString();
    }

    /**
     * 사용자 메시지에서 키워드 추출
     */
    private List<String> extractKeywords(String userMessage) {
        String[] commonKeywords = {
                "양재", "양평", "상봉", "고척", "월계", "마곡", "금천", "영등포",
                "코스트코", "이마트", "트레이더스", "롯데마트",
                "견과류", "아몬드", "호두", "세제", "다우니", "베이커리", "머핀", "베이글",
                "냉동식품", "만두", "과일", "육류", "삼겹살", "닭가슴살", "간식", "과자",
                "조미료", "올리브오일", "소스"
        };

        return Arrays.stream(commonKeywords)
                .filter(keyword -> userMessage.toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * 키워드와 관련된 모임 찾기
     */
    private List<Meeting> findRelevantMeetings(List<String> keywords, List<Meeting> activeMeetings) {
        if (keywords.isEmpty()) {
            return new ArrayList<>();
        }

        return activeMeetings.stream()
                .filter(meeting -> {
                    String searchText = (meeting.getTitle() + " " + meeting.getDescription() + " " + meeting.getMart().getMartName()).toLowerCase();
                    return keywords.stream().anyMatch(keyword -> searchText.contains(keyword.toLowerCase()));
                })
                .limit(3)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 질문의 의도 분류
     */
    private ChatbotConversation.IntentType determineIntentType(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        // 모임 검색 관련 키워드
        String[] searchKeywords = {"찾", "검색", "추천", "모임", "소분", "참여", "신청", "있나", "어디"};
        if (Arrays.stream(searchKeywords).anyMatch(lowerMessage::contains)) {
            return ChatbotConversation.IntentType.MEETING_SEARCH;
        }

        // 서비스 가이드 관련 키워드
        String[] guideKeywords = {"방법", "어떻게", "가이드", "이용", "사용", "회원가입", "가입", "시작"};
        if (Arrays.stream(guideKeywords).anyMatch(lowerMessage::contains)) {
            return ChatbotConversation.IntentType.SERVICE_GUIDE;
        }

        return ChatbotConversation.IntentType.GENERAL;
    }

    /**
     * 모임 추천 생성 (로그인 사용자 전용)
     */
    private List<ChatbotMessageResponse.MeetingSuggestionResponse> generateMeetingSuggestions(
            ChatbotConversation conversation, String userMessage, List<Meeting> activeMeetings) {

        List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestions = new ArrayList<>();

        try {
            // 키워드 기반으로 관련 모임 찾기
            List<String> keywords = extractKeywords(userMessage);
            List<Meeting> relevantMeetings = findRelevantMeetings(keywords, activeMeetings);

            // 관련 모임이 없으면 최신 모임 3개 추천
            if (relevantMeetings.isEmpty() && !activeMeetings.isEmpty()) {
                relevantMeetings = activeMeetings.stream()
                        .limit(3)
                        .collect(Collectors.toList());
            }

            // 추천 모임 저장 및 응답 DTO 생성
            for (Meeting meeting : relevantMeetings) {
                String suggestionReason = generateSuggestionReason(userMessage, meeting, keywords);

                // 추천 기록 저장
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
                        .status(meeting.getStatus().toString())
                        .martAddress(meeting.getMart().getAddress())
                        .build());
            }

        } catch (Exception e) {
            log.error("모임 추천 생성 중 오류 발생", e);
        }

        return suggestions;
    }

    /**
     * 모임 추천 이유 생성
     */
    private String generateSuggestionReason(String userMessage, Meeting meeting, List<String> keywords) {
        if (!keywords.isEmpty()) {
            String keyword = keywords.get(0);
            return String.format("'%s' 관련 요청에 적합한 모임입니다.", keyword);
        }
        return "현재 참여 가능한 인기 모임입니다.";
    }

    /**
     * 대화 기록 저장
     */
    private ChatbotConversation saveConversation(User user, String userMessage, String botResponse, ChatbotConversation.IntentType intentType) {
        ChatbotConversation conversation = newConversationInstance();
        conversation.setUser(user);
        conversation.setUserMessage(userMessage);
        conversation.setBotResponse(botResponse);
        conversation.setIntentType(intentType);
        return conversationRepository.save(conversation);
    }

    /**
     * 대화 기록을 히스토리 응답 DTO로 변환
     */
    private ChatbotConversationHistoryResponse convertToHistoryResponse(ChatbotConversation conversation) {
        List<ChatbotConversationHistoryResponse.MeetingSuggestionInfo> suggestionInfos =
                conversation.getMeetingSuggestions().stream()
                        .map(suggestion -> ChatbotConversationHistoryResponse.MeetingSuggestionInfo.builder()
                                .meetingId(suggestion.getMeeting().getMeetingId())
                                .title(suggestion.getMeeting().getTitle())
                                .suggestionReason(suggestion.getSuggestionReason())
                                .martName(suggestion.getMeeting().getMart().getMartName())
                                .meetingDate(suggestion.getMeeting().getMeetingDate())
                                .status(suggestion.getMeeting().getStatus().toString())
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

    /**
     * ChatbotConversation 인스턴스 생성 (Reflection 사용)
     * protected 생성자를 우회하여 인스턴스를 생성합니다.
     */
    private ChatbotConversation newConversationInstance() {
        try {
            Constructor<ChatbotConversation> constructor = ChatbotConversation.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("ChatbotConversation 인스턴스 생성 실패", e);
        }
    }

    /**
     * ChatbotMeetingSuggestion 인스턴스 생성 (Reflection 사용)
     * protected 생성자를 우회하여 인스턴스를 생성합니다.
     */
    private ChatbotMeetingSuggestion newMeetingSuggestionInstance() {
        try {
            Constructor<ChatbotMeetingSuggestion> constructor = ChatbotMeetingSuggestion.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("ChatbotMeetingSuggestion 인스턴스 생성 실패", e);
        }
    }
}