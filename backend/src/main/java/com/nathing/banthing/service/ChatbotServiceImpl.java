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
 *
 * 챗봇 서비스의 핵심 기능을 담당하는 클래스입니다:
 * - Google GenAI 모델과의 연동을 통한 AI 응답 생성
 * - 로그인/비로그인 사용자 구분 처리 및 개인화된 응답 제공
 * - 실시간 모임 정보 조회 및 지능적 모임 추천
 * - 대화 내역 저장 및 관리
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

    /**
     * 챗봇 서비스의 상태를 확인하는 헬스체크 메서드
     * Google GenAI API에 간단한 요청("ping")을 보내 정상적으로 응답하는지 확인합니다.
     *
     * @return API와 통신이 정상이면 true, 아니면 false를 반환합니다.
     */
    @Override
    public boolean healthCheck() {
        try {
            String model = chatbotConfig.getModelName();
            // 헬스체크용으로 최소한의 리소스를 사용하도록 설정 객체를 별도로 생성
            GenerateContentConfig pingCfg = GenerateContentConfig.builder()
                    .maxOutputTokens(1) // 응답 토큰 1개
                    .temperature(0.0f)  // 0.0f로 두면, 같은 입력("ping")을 주면 항상 똑같은 응답을 돌려줌
                    .build();
            GenerateContentResponse res = genAiClient.models.generateContent(model, "ping", pingCfg);
            return res != null && res.text() != null;
        } catch (Exception e) {
            log.warn("GenAI healthCheck 실패", e);
            return false;
        }
    }

    /**
     * 로그인한 사용자의 챗봇 메시지를 처리합니다.
     * 사용자 정보를 기반으로 개인화된 응답을 생성하고, 대화 내용을 DB에 저장합니다.
     *
     * @param providerId  사용자를 식별하는 소셜 로그인 ID
     * @param userMessage 사용자가 입력한 메시지
     * @return 개인화된 답변과 추천 모임 목록이 포함된 챗봇 응답 DTO
     */
    @Override
    @Transactional
    public ChatbotMessageResponse processAuthenticatedMessage(String providerId, String userMessage) {
        try {
            // 1. 사용자 정보 조회
            User user = usersRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 2. 사용자 정보와 실시간 모임 정보를 포함한 개인화된 시스템 프롬프트 생성
            String systemPrompt = buildPersonalizedPrompt(user);

            // 3. AI 모델을 통해 답변 생성
            String aiResponse = generateAIResponse(systemPrompt + "\n\n사용자 질문: " + userMessage);

            // 4. 사용자의 질문 의도 파악 (확장된 키워드 기반)
            ChatbotConversation.IntentType intentType = determineIntentType(userMessage);

            // 5. 대화 내용 DB에 저장
            ChatbotConversation conversation = saveConversation(user, userMessage, aiResponse, intentType);

            // 6. 의도가 '모임 검색'일 경우, 관련 모임을 추천하고 DB에 기록
            List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestions = new ArrayList<>();
            if (intentType == ChatbotConversation.IntentType.MEETING_SEARCH) {
                suggestions = createMeetingSuggestions(conversation, userMessage);
            }

            // 7. 최종 응답 DTO를 빌드하여 반환
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

    /**
     * 게스트(비로그인) 사용자의 챗봇 메시지를 처리합니다.
     * 실시간 모임 정보를 조회하여 기본적인 안내와 함께 답변을 생성합니다.
     * 대화 내용은 저장하지 않고, 개인화 정보 없이 일반적인 답변을 제공합니다.
     *
     * @param userMessage 사용자가 입력한 메시지
     * @return 기본적인 답변이 포함된 챗봇 응답 DTO
     */
    @Override
    public ChatbotMessageResponse processGuestMessage(String userMessage) {
        log.info("=== 게스트 메시지 처리 시작: '{}' ===", userMessage);

        try {
            // 1. API 키 상태 확인
            String apiKey = chatbotConfig.getApiKey();
            boolean hasApiKey = apiKey != null && !apiKey.trim().isEmpty();
            log.info("Google AI API 키 상태: {}", hasApiKey ? "설정됨" : "누락됨");

            // 2. 데이터베이스에서 실시간 모임 정보 조회 (실시간 업데이트 문제 해결)
            List<Meeting> activeMeetings = null;
            int meetingCount = 0;
            try {
                log.info("데이터베이스 조회 시작...");
                activeMeetings = meetingsRepository.findByStatusAndDeletedAtIsNull(Meeting.MeetingStatus.RECRUITING);
                meetingCount = activeMeetings != null ? activeMeetings.size() : 0;
                log.info("데이터베이스 조회 성공: {}개의 활성 모임 발견", meetingCount);

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
                log.error("데이터베이스 조회 실패", dbError);
            }

            // 3. 응답 생성 전략 결정
            String response;

            if (hasApiKey && meetingCount > 0) {
                // AI API + 실제 데이터로 응답 시도
                log.info("AI API + 실제 데이터로 응답 생성 시도");
                try {
                    String systemPrompt = buildEnhancedSystemPrompt(activeMeetings);
                    String fullPrompt = systemPrompt + "\n\n사용자 질문: " + userMessage;
                    log.info("프롬프트 길이: {}", fullPrompt.length());

                    response = generateAIResponse(fullPrompt);
                    log.info("AI 응답 생성 성공 (길이: {})", response.length());
                } catch (Exception aiError) {
                    log.error("AI API 호출 실패, 데이터베이스 기반 응답으로 전환", aiError);
                    response = buildDatabaseBasedResponse(userMessage, activeMeetings);
                }
            } else {
                // 데이터베이스 기반 직접 응답
                log.info("데이터베이스 기반 직접 응답 생성 (API 키: {}, 모임 수: {})", hasApiKey, meetingCount);
                response = buildDatabaseBasedResponse(userMessage, activeMeetings);
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
     * 사용자의 대화 기록을 조회합니다.
     * 최근 10개의 대화 내용을 반환합니다.
     *
     * @param providerId 사용자의 소셜 로그인 ID
     * @return 대화 기록 리스트 (최신순)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChatbotConversationHistoryResponse> getChatHistory(String providerId) {
        try {
            // 사용자 존재 여부 확인
            User user = usersRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 최근 10개 대화 조회 (페이징 처리)
            var pageable = PageRequest.of(0, 10);
            List<ChatbotConversation> conversations = conversationRepository
                    .findByUser_UserIdOrderByCreatedAtDesc(user.getUserId(), pageable);

            // Entity → DTO 변환
            return conversations.stream()
                    .map(this::convertToHistoryResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("대화 기록 조회 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== Private 헬퍼 메서드들 =====

    /**
     * Google GenAI API를 통해 응답을 생성합니다.
     * API 호출 실패 시 적절한 기본 응답을 반환합니다.
     */
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

    /**
     * 사용자 메시지의 의도를 분석합니다. (키워드 기반 패턴 매칭)
     * 모임 검색, 서비스 가이드, 일반 질문으로 구분합니다.
     * 의도 종류:
     * - MEETING_SEARCH: 모임 찾기 (지역, 마트, 상품명 포함)
     * - SERVICE_GUIDE: 서비스 이용법 (가입, 사용법, 규칙 등)
     * - GENERAL: 일반 질문 (기타 모든 경우)
     *
     * @param message 사용자가 입력한 메시지
     * @return 분석된 의도 타입
     */
    private ChatbotConversation.IntentType determineIntentType(String message) {
        String msg = message.toLowerCase();

        // 모임 검색 관련 키워드
        if (containsAny(msg,
                // 기본 검색 키워드
                "모임", "찾", "검색", "소분", "함께", "나눔", "지역", "근처", "마트", "추천",
                // 지역명 키워드 (서울 8개 지점)
                "양재", "상봉", "마곡", "월계", "영등포", "금천", "고척", "양평",
                // 마트 브랜드 키워드
                "코스트코", "트레이더스", "롯데마트", "맥스",
                // 상품 카테고리 키워드 (자주 소분되는 상품들)
                "견과류", "아몬드", "호두", "캐슈넛", "마카다미아", "피스타치오",
                "세제", "다우니", "섬유유연제", "세탁세제", "주방세제",
                "냉동", "냉동식품", "만두", "냉동과일", "아이스크림",
                "육류", "고기", "삼겹살", "소고기", "돼지고기", "닭고기", "등심",
                "와인", "양주", "맥주", "음료", "주류",
                "생활용품", "화장지", "휴지", "키친타올", "물티슈",
                "캠핑용품", "아웃도어", "텐트", "의자",
                "과일", "사과", "배", "포도", "딸기", "바나나",
                "유제품", "우유", "치즈", "요거트", "버터",
                "쌀", "곡물", "견과", "올리브오일", "식용유",
                "간식", "과자", "초콜릿", "사탕", "젤리")) {
            return ChatbotConversation.IntentType.MEETING_SEARCH;
        }

        // 서비스 가이드 관련 키워드
        if (containsAny(msg,
                // 기본 가이드 키워드
                "이용", "방법", "가이드", "가입", "시작", "어떻게", "준비", "위생", "안전",
                // 회원 관련 키워드
                "회원가입", "로그인", "탈퇴", "프로필", "정보수정",
                // 사용법 관련 키워드
                "사용법", "이용방법", "가입방법", "참여방법", "신청방법",
                // 준비물 관련 키워드
                "준비물", "용기", "아이스박스", "계량", "포장", "봉지",
                // 규칙/정책 관련 키워드
                "수칙", "매너", "규칙", "정책", "약관", "취소", "환불",
                // 평가/신뢰도 관련 키워드
                "신뢰도", "평가", "피드백", "후기", "리뷰", "별점",
                // 문제해결 관련 키워드
                "신고", "문의", "고객센터", "도움말", "FAQ", "질문")) {
            return ChatbotConversation.IntentType.SERVICE_GUIDE;
        }

        return ChatbotConversation.IntentType.GENERAL;
    }

    /**
     * 로그인한 사용자를 위한 개인화된 프롬프트를 생성합니다.
     * 사용자의 닉네임과 실시간 모임 정보를 포함합니다.
     */
    private String buildPersonalizedPrompt(User user) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(chatbotConfig.getSystemPrompt());

        // 사용자 기본 정보
        promptBuilder.append("\n\n# 현재 사용자 정보\n");
        promptBuilder.append("- 닉네임: ").append(user.getNickname()).append("\n");

        // 현재 활성 모임 정보 추가 (실시간 데이터)
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

    /**
     * 실시간 모임 정보를 포함한 강화된 시스템 프롬프트를 생성합니다.
     * 게스트 사용자용 프롬프트에 실제 모임 데이터를 추가합니다.
     *
     * @param activeMeetings 현재 모집 중인 모임 리스트
     * @return 모임 정보가 포함된 강화된 프롬프트
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
     * 데이터베이스 기반 직접 응답을 생성합니다.
     * AI API 사용이 불가능하거나 실패했을 때 사용하는 대안 응답입니다.
     * 하드코딩된 키워드 매칭 대신 지능적인 모임 매칭을 수행합니다.
     *
     * @param userMessage 사용자 메시지 (매칭 키워드 추출용)
     * @param activeMeetings 현재 활성 모임 리스트
     * @return 데이터베이스 정보 기반 구조화된 응답
     */
    private String buildDatabaseBasedResponse(String userMessage, List<Meeting> activeMeetings) {
        StringBuilder response = new StringBuilder();
        response.append("안녕하세요! 반띵 AI 도우미입니다.\n\n");

        if (activeMeetings != null && !activeMeetings.isEmpty()) {
            response.append("현재 서울 지역에서 총 ").append(activeMeetings.size()).append("개의 소분 모임이 진행 중이에요!\n\n");

            // 사용자 질문과 관련된 모임 찾기
            List<Meeting> matchedMeetings = findRelevantMeetings(userMessage, activeMeetings);

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

    /**
     * 사용자 질문과 관련성이 높은 모임을 지능적으로 찾습니다.
     *
     * 매칭 전략:
     *   - 지역명 매칭: 사용자가 언급한 지역과 마트 위치 비교
     *   - 마트 브랜드 매칭: 코스트코, 트레이더스 등 브랜드명 비교
     *   - 상품 키워드 매칭: 모임 제목/설명에서 상품명 검색
     * 결과: 최대 3개 모임 반환 (관련도 높은 순)
     */
    private List<Meeting> findRelevantMeetings(String userMessage, List<Meeting> activeMeetings) {
        String lowerMessage = userMessage.toLowerCase();

        return activeMeetings.stream()
                .filter(meeting -> {
                    String title = meeting.getTitle().toLowerCase();
                    String martName = meeting.getMart().getMartName().toLowerCase();
                    String description = meeting.getDescription() != null ?
                            meeting.getDescription().toLowerCase() : "";

                    // 제목, 마트명, 설명에서 사용자 메시지의 키워드와 일치하는 것이 있는지 확인
                    return containsRelevantKeywords(lowerMessage, title, martName, description);
                })
                .limit(3)   // 최대 3개만
                .collect(Collectors.toList());
    }

    /**
     * 사용자 메시지와 모임 정보 간의 관련성을 판단합니다.
     *
     *  판단 기준:
     *   1. 지역명 매칭 (양재, 상봉 등)
     *   2. 마트 브랜드 매칭 (코스트코, 트레이더스 등)
     *   3. 상품명 매칭 (견과, 세제, 냉동 등)
     *  반환: 하나라도 매칭되면 true
     */
    private boolean containsRelevantKeywords(String userMessage, String title, String martName, String description) {
        // 지역명 매칭
        String[] locations = {"양재", "상봉", "마곡", "월계", "영등포", "금천", "고척", "양평"};
        for (String location : locations) {
            if (userMessage.contains(location) && martName.contains(location)) {
                return true;
            }
        }

        // 마트 브랜드 매칭
        String[] marts = {"코스트코", "트레이더스", "롯데"};
        for (String mart : marts) {
            if (userMessage.contains(mart) && martName.contains(mart)) {
                return true;
            }
        }

        // 상품명 매칭 (제목 또는 설명에서)
        String[] products = {"견과", "아몬드", "호두", "세제", "다우니", "냉동", "육류", "고기", "와인", "생활용품", "화장지", "캠핑"};
        for (String product : products) {
            if (userMessage.contains(product) && (title.contains(product) || description.contains(product))) {
                return true;
            }
        }

        return false;
    }

    /**
     * 긴급 상황용 기본 응답을 생성합니다.
     * 모든 시스템이 실패했을 때 최후의 수단으로 사용됩니다.
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
     * 로그인한 사용자를 위한 모임 추천을 생성합니다.
     * 실시간 모임 데이터를 기반으로 지능적인 추천을 수행합니다.
     */
    private List<ChatbotMessageResponse.MeetingSuggestionResponse> createMeetingSuggestions(
            ChatbotConversation conversation, String userMessage) {

        List<ChatbotMessageResponse.MeetingSuggestionResponse> suggestions = new ArrayList<>();
        try {
            // 실시간 활성 모임 조회
            List<Meeting> activeMeetings = meetingsRepository.findByStatusAndDeletedAtIsNull(Meeting.MeetingStatus.RECRUITING);

            // 사용자 질문과 관련된 모임을 지능적으로 선별
            List<Meeting> relevantMeetings = findRelevantMeetings(userMessage, activeMeetings);

            // 관련 모임이 없으면 최근 생성된 모임 3개 선택
            List<Meeting> recommendedMeetings = relevantMeetings.isEmpty() ?
                    activeMeetings.stream().limit(3).collect(Collectors.toList()) :
                    relevantMeetings;

            for (Meeting meeting : recommendedMeetings) {
                String suggestionReason = generateSuggestionReason(meeting, userMessage);

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

    /**
     * 모임 추천 이유를 동적으로 생성합니다.
     * 사용자 질문과 모임 정보의 매칭 결과에 따라 적절한 이유를 제공합니다.
     */
    private String generateSuggestionReason(Meeting meeting, String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        String title = meeting.getTitle().toLowerCase();
        String martName = meeting.getMart().getMartName();

        // 지역 매칭
        String[] locations = {"양재", "상봉", "마곡", "월계", "영등포", "금천"};
        for (String location : locations) {
            if (lowerMessage.contains(location) && martName.toLowerCase().contains(location)) {
                return "요청하신 " + location + " 지역의 " + martName + " 모임입니다.";
            }
        }

        // 상품 매칭
        String[] products = {"견과", "세제", "냉동", "육류", "와인", "생활용품", "캠핑"};
        for (String product : products) {
            if (lowerMessage.contains(product) && title.contains(product)) {
                return "문의하신 " + product + " 관련 모임입니다.";
            }
        }

        // 기본 추천 이유
        return "현재 인기 있는 " + martName + " 모임입니다.";
    }

    /**
     * 대화 내용을 데이터베이스에 저장합니다.
     * 로그인한 사용자의 대화 이력을 관리하기 위해 사용됩니다.
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
     * 리플렉션을 사용하여 ChatbotConversation 인스턴스를 생성합니다.
     * 기존 코드의 패턴을 유지합니다.
     */
    private ChatbotConversation newConversationInstance() {
        try {
            Constructor<ChatbotConversation> ctor = ChatbotConversation.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("ChatbotConversation 인스턴스 생성 실패", e);
        }
    }

    /**
     * 리플렉션을 사용하여 ChatbotMeetingSuggestion 인스턴스를 생성합니다.
     * 기존 코드의 패턴을 유지합니다.
     */
    private ChatbotMeetingSuggestion newMeetingSuggestionInstance() {
        try {
            Constructor<ChatbotMeetingSuggestion> ctor = ChatbotMeetingSuggestion.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("ChatbotMeetingSuggestion 인스턴스 생성 실패", e);
        }
    }

    /**
     * 대화 기록을 히스토리 응답 DTO로 변환합니다.
     * 클라이언트에서 필요한 형태로 데이터를 가공합니다.
     */
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

    /**
     * 문자열에 지정된 키워드들 중 하나라도 포함되어 있는지 확인합니다.
     * 의도 분석에서 사용되는 유틸리티 메서드입니다.
     */
    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}