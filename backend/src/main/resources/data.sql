
-- ============================================
-- 마트 데이터 삽입
-- ============================================

INSERT INTO marts (mart_name, mart_brand, address, latitude, longitude, created_at, updated_at) VALUES
-- 코스트코
('코스트코 양재점', 'COSTCO', '서울특별시 서초구 양재대로 275', 37.4833917, 127.0341544, NOW(), NOW()),
('코스트코 상봉점', 'COSTCO', '서울특별시 중랑구 망우로 354', 37.5955556, 127.0952778, NOW(), NOW()),
('코스트코 송도점', 'COSTCO', '인천광역시 연수구 센트럴로 123', 37.3838889, 126.6597222, NOW(), NOW()),
('코스트코 광명점', 'COSTCO', '경기도 광명시 일직로 17', 37.4167778, 126.8597222, NOW(), NOW()),
('코스트코 의정부점', 'COSTCO', '경기도 의정부시 평화로 525', 37.7361111, 127.0469444, NOW(), NOW()),

-- 이마트 트레이더스
('이마트 트레이더스 월계점', 'TRADERS', '서울특별시 노원구 덕릉로 515', 37.6127778, 127.0580556, NOW(), NOW()),
('이마트 트레이더스 킨텍스점', 'TRADERS', '경기도 고양시 일산서구 킨텍스로 217-6', 37.6686111, 126.7430556, NOW(), NOW()),
('이마트 트레이더스 영등포점', 'TRADERS', '서울특별시 영등포구 영중로 15', 37.5147222, 126.9075000, NOW(), NOW()),
('이마트 트레이더스 하남점', 'TRADERS', '경기도 하남시 미사대로 750', 37.5444444, 127.2225000, NOW(), NOW()),

-- 롯데마트
('롯데마트 서울역점', 'LOTTE_MART', '서울특별시 중구 한강대로 405', 37.5547222, 126.9705556, NOW(), NOW());

-- ============================================
-- 사용자 데이터 삽입
-- ============================================

INSERT INTO users (nickname, provider, provider_id, trust_score, trust_grade, agree, created_at, updated_at) VALUES
                                                                                                                 ('김코스트', 'kakao', 'kakao_123456', 300, 'BASIC', TRUE, NOW(), NOW()),
                                                                                                                 ('이소분', 'kakao', 'kakao_789012', 350, 'BASIC', TRUE, NOW(), NOW()),
                                                                                                                 ('박나눔', 'kakao', 'kakao_345678', 280, 'BASIC', TRUE, NOW(), NOW()),
                                                                                                                 ('최절약', 'kakao', 'kakao_111222', 520, 'GOOD', TRUE, NOW(), NOW()),
                                                                                                                 ('정합리', 'kakao', 'kakao_333444', 450, 'BASIC', TRUE, NOW(), NOW()),
                                                                                                                 ('한경제', 'kakao', 'kakao_555666', 80, 'WARNING', TRUE, NOW(), NOW()),
                                                                                                                 ('윤공유', 'kakao', 'kakao_777888', 380, 'BASIC', TRUE, NOW(), NOW()),
                                                                                                                 ('강커뮤', 'kakao', 'kakao_999000', 420, 'BASIC', TRUE, NOW(), NOW());

-- ============================================
-- 모임 데이터 삽입
-- ============================================

-- status 컬럼 추가 및 기본값 'RECRUITING' 설정
INSERT INTO meetings (host_user_id, mart_id, title, description, meeting_date, max_participants, status, created_at, updated_at) VALUES
-- 양재 코스트코 모임들
(1, 1, '코스트코 견과류 소분해요!', '아몬드, 호두 등 견과류를 4명이서 나눠 가져요. 개인 용기 꼭 가져오세요!', '2025-09-15 14:00:00', 4, 'RECRUITING', NOW(), NOW()),
(2, 1, '세제 대용량 소분 모임', '다우니 4L를 3명이서 나누어 가져가실 분!', '2025-09-16 16:00:00', 3, 'RECRUITING', NOW(), NOW()),
(4, 1, '베이커리 빵 소분', '코스트코 머핀과 베이글을 함께 나눠요', '2025-09-18 10:00:00', 5, 'RECRUITING', NOW(), NOW()),

-- 상봉 코스트코 모임들
(3, 2, '상봉 코스트코 냉동식품 소분', '냉동만두와 냉동과일을 함께 소분해요. 아이스박스 준비됩니다.', '2025-09-17 11:00:00', 4, 'RECRUITING', NOW(), NOW()),
(5, 2, '육류 소분 모임', '소고기, 돼지고기 대용량 소분합니다', '2025-09-19 15:00:00', 5, 'RECRUITING', NOW(), NOW()),

-- 트레이더스 모임들
(6, 6, '트레이더스 생활용품 소분', '화장지, 세제 등 생활용품 함께 구매해요', '2025-09-20 13:00:00', 4, 'RECRUITING', NOW(), NOW()),
(7, 7, '킨텍스 트레이더스 과일 소분', '사과, 배 등 과일 박스 소분', '2025-09-21 16:00:00', 3, 'RECRUITING', NOW(), NOW()),

-- 종료된 모임 (피드백 테스트용)
(1, 1, '완료된 모임 - 쌀 소분', '20kg 쌀을 5명이서 나눠가졌습니다', '2025-09-10 14:00:00', 5, 'RECRUITING', NOW(), NOW());

-- 마지막 모임 상태를 COMPLETED로 변경
UPDATE meetings SET status = 'COMPLETED' WHERE meeting_id = 8;

-- ============================================
-- 모임 참여자 데이터 삽입
-- ============================================

-- 호스트들 (자동으로 APPROVED 상태)
INSERT INTO meeting_participants (meeting_id, user_id, participant_type, application_status, joined_at, updated_at) VALUES
                                                                                                                        (1, 1, 'HOST', 'APPROVED', NOW(), NOW()),
                                                                                                                        (2, 2, 'HOST', 'APPROVED', NOW(), NOW()),
                                                                                                                        (3, 3, 'HOST', 'APPROVED', NOW(), NOW()),
                                                                                                                        (4, 4, 'HOST', 'APPROVED', NOW(), NOW()),
                                                                                                                        (5, 5, 'HOST', 'APPROVED', NOW(), NOW()),
                                                                                                                        (6, 6, 'HOST', 'APPROVED', NOW(), NOW()),
                                                                                                                        (7, 7, 'HOST', 'APPROVED', NOW(), NOW()),
                                                                                                                        (8, 1, 'HOST', 'APPROVED', NOW(), NOW());

-- 일반 참여자들
INSERT INTO meeting_participants (meeting_id, user_id, participant_type, application_status, joined_at, updated_at) VALUES
-- 모임 1 참여자들
(1, 2, 'PARTICIPANT', 'APPROVED', NOW(), NOW()),
(1, 3, 'PARTICIPANT', 'PENDING', NOW(), NOW()),

-- 모임 2 참여자들
(2, 1, 'PARTICIPANT', 'APPROVED', NOW(), NOW()),
(2, 4, 'PARTICIPANT', 'PENDING', NOW(), NOW()),

-- 모임 3 참여자들
(3, 5, 'PARTICIPANT', 'APPROVED', NOW(), NOW()),
(3, 6, 'PARTICIPANT', 'APPROVED', NOW(), NOW()),

-- 완료된 모임 8의 참여자들 (피드백 테스트용)
(8, 2, 'PARTICIPANT', 'APPROVED', NOW(), NOW()),
(8, 3, 'PARTICIPANT', 'APPROVED', NOW(), NOW()),
(8, 4, 'PARTICIPANT', 'APPROVED', NOW(), NOW()),
(8, 5, 'PARTICIPANT', 'APPROVED', NOW(), NOW());

-- ============================================
-- 챗봇 대화 이력 데이터 삽입
-- ============================================

INSERT INTO chatbot_conversations (user_id, user_message, bot_response, intent_type, created_at, updated_at) VALUES
                                                                                                                 (1, '양재 코스트코 근처에 견과류 소분 모임 있나요?', '네, 현재 양재 코스트코에서 견과류 소분 모임이 진행중입니다. 9월 15일 오후 2시에 진행되는 모임에 참여하실 수 있습니다.', 'MEETING_SEARCH', NOW(), NOW()),
                                                                                                                 (2, '소분 모임은 어떻게 참여하나요?', '소분 모임 참여는 간단합니다. 원하는 모임을 선택하고 참여 신청을 하시면, 호스트의 승인 후 참여가 확정됩니다.', 'SERVICE_GUIDE', NOW(), NOW()),
                                                                                                                 (3, '냉동식품 소분할 때 주의사항이 있나요?', '냉동식품 소분 시에는 아이스박스나 보냉백을 꼭 준비하시고, 소분 후 빠른 시간 내에 냉동보관하시기 바랍니다.', 'SERVICE_GUIDE', NOW(), NOW()),
                                                                                                                 (4, '안녕하세요', '안녕하세요! 반띵 챗봇입니다. 소분 모임에 대해 궁금한 것이 있으시면 언제든 물어보세요.', 'GENERAL', NOW(), NOW());

-- ============================================
-- 챗봇 모임 추천 데이터 삽입
-- ============================================

INSERT INTO chatbot_meeting_suggestions (conversation_id, meeting_id, suggestion_reason, created_at, updated_at) VALUES
                                                                                                                     (1, 1, '사용자가 찾던 양재 코스트코 견과류 소분 모임', NOW(), NOW()),
                                                                                                                     (3, 3, '냉동식품 관련 질문에 대한 관련 모임 추천', NOW(), NOW());

-- ============================================
-- 피드백 데이터 삽입 (완료된 모임 기준)
-- ============================================

INSERT INTO feedbacks (giver_user_id, receiver_user_id, meeting_id, is_positive, created_at, updated_at) VALUES
-- 모임 8에서의 상호 피드백들 (호스트: 1번, 참여자: 2,3,4,5번)
(1, 2, 8, TRUE, NOW(), NOW()),   -- 호스트 -> 참여자2 긍정
(1, 3, 8, TRUE, NOW(), NOW()),   -- 호스트 -> 참여자3 긍정
(1, 4, 8, FALSE, NOW(), NOW()),  -- 호스트 -> 참여자4 부정 (노쇼)
(1, 5, 8, TRUE, NOW(), NOW()),   -- 호스트 -> 참여자5 긍정

(2, 1, 8, TRUE, NOW(), NOW()),   -- 참여자2 -> 호스트 긍정
(3, 1, 8, TRUE, NOW(), NOW()),   -- 참여자3 -> 호스트 긍정
(5, 1, 8, TRUE, NOW(), NOW()),   -- 참여자5 -> 호스트 긍정

-- 참여자들 간 상호 피드백
(2, 3, 8, TRUE, NOW(), NOW()),   -- 참여자2 -> 참여자3 긍정
(3, 2, 8, TRUE, NOW(), NOW()),   -- 참여자3 -> 참여자2 긍정
(2, 5, 8, TRUE, NOW(), NOW()),   -- 참여자2 -> 참여자5 긍정
(5, 2, 8, TRUE, NOW(), NOW());   -- 참여자5 -> 참여자2 긍정

-- ============================================
-- 현재 참여자 수 업데이트
-- ============================================

UPDATE meetings SET current_participants = (
    SELECT COUNT(*)
    FROM meeting_participants
    WHERE meeting_participants.meeting_id = meetings.meeting_id
      AND application_status = 'APPROVED'
);

-- ============================================
-- 더미 데이터 삽입 완료
-- ============================================
commit;