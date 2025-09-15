-- ============================================
-- 사용자 데이터 삽입
-- ============================================
INSERT INTO users (nickname, provider, provider_id, trust_score, trust_grade, agree, created_at, updated_at) VALUES
                                                                                                                 ('김코스트', 'kakao', 'kakao_1', 300, 'BASIC', TRUE, NOW(), NOW()),
                                                                                                                 ('이소분', 'kakao', 'kakao_2', 350, 'BASIC', TRUE, NOW(), NOW()),
                                                                                                                 ('박나눔', 'kakao', 'kakao_3', 280, 'BASIC', TRUE, NOW(), NOW()),
                                                                                                                 ('최절약', 'kakao', 'kakao_4', 520, 'GOOD', TRUE, NOW(), NOW()),
                                                                                                                 ('정합리', 'kakao', 'kakao_5', 450, 'BASIC', TRUE, NOW(), NOW()),
                                                                                                                 ('한경제', 'kakao', 'kakao_6', 80, 'WARNING', TRUE, NOW(), NOW()),
                                                                                                                 ('윤공유', 'kakao', 'kakao_7', 380, 'BASIC', TRUE, NOW(), NOW()),
                                                                                                                 ('강커뮤', 'kakao', 'kakao_8', 420, 'BASIC', TRUE, NOW(), NOW());

-- ============================================
-- 모임 데이터 삽입
-- ============================================
-- schema.sql에서 생성된 mart_id (1~8)를 참조하도록 수정했습니다.
INSERT INTO meetings (host_user_id, mart_id, title, description, meeting_date, max_participants, status, created_at, updated_at) VALUES
-- 양재 코스트코(mart_id=2) 모임들
(1, 2, '코스트코 견과류 소분해요!', '아몬드, 호두 등 견과류를 4명이서 나눠 가져요. 개인 용기 꼭 가져오세요!', '2025-09-20 14:00:00', 4, 'RECRUITING', NOW(), NOW()),
(2, 2, '세제 대용량 소분 모임', '다우니 4L를 3명이서 나누어 가져가실 분!', '2025-09-21 16:00:00', 3, 'RECRUITING', NOW(), NOW()),

-- 상봉 코스트코(mart_id=3) 모임들
(3, 3, '상봉 코스트코 냉동식품 소분', '냉동만두와 냉동과일을 함께 소분해요. 아이스박스 준비됩니다.', '2025-09-23 11:00:00', 4, 'RECRUITING', NOW(), NOW()),
(5, 3, '육류 소분 모임', '소고기, 돼지고기 대용량 소분합니다', '2025-09-24 15:00:00', 5, 'RECRUITING', NOW(), NOW()),

-- 트레이더스 월계점(mart_id=5) 및 마곡점(mart_id=6) 모임들
(6, 5, '월계 트레이더스 생활용품 소분', '화장지, 세제 등 생활용품 함께 구매해요', '2025-09-25 13:00:00', 4, 'RECRUITING', NOW(), NOW()),
(7, 6, '마곡 트레이더스 와인 소분', '신규 오픈한 마곡점에서 와인 함께 사실 분 구합니다.', '2025-09-26 16:00:00', 3, 'RECRUITING', NOW(), NOW()),

-- 롯데마트 맥스 영등포점(mart_id=8) 모임
(4, 8, '영등포 롯데맥스 캠핑용품 사실 분', '캠핑용품 같이 구매하실 분 찾아요.', '2025-09-22 10:00:00', 5, 'RECRUITING', NOW(), NOW()),

-- 종료된 모임 (피드백 테스트용, 양평 코스트코 mart_id=1)
(1, 1, '완료된 모임 - 쌀 소분', '20kg 쌀을 5명이서 나눠가졌습니다', '2025-09-10 14:00:00', 5, 'RECRUITING', NOW(), NOW());

-- 마지막 모임 상태를 COMPLETED로 변경
UPDATE meetings SET status = 'COMPLETED' WHERE meeting_id = 8;

-- ============================================
-- 모임 참여자 데이터 삽입
-- ============================================
INSERT INTO meeting_participants (meeting_id, user_id, participant_type, application_status, joined_at, updated_at) VALUES
                                                                                                                        (1, 1, 'HOST', 'APPROVED', NOW(), NOW()), (2, 2, 'HOST', 'APPROVED', NOW(), NOW()), (3, 4, 'HOST', 'APPROVED', NOW(), NOW()),
                                                                                                                        (4, 3, 'HOST', 'APPROVED', NOW(), NOW()), (5, 5, 'HOST', 'APPROVED', NOW(), NOW()), (6, 6, 'HOST', 'APPROVED', NOW(), NOW()),
                                                                                                                        (7, 7, 'HOST', 'APPROVED', NOW(), NOW()), (8, 1, 'HOST', 'APPROVED', NOW(), NOW());

INSERT INTO meeting_participants (meeting_id, user_id, participant_type, application_status, joined_at, updated_at) VALUES
                                                                                                                        (1, 2, 'PARTICIPANT', 'APPROVED', NOW(), NOW()), (1, 3, 'PARTICIPANT', 'PENDING', NOW(), NOW()),
                                                                                                                        (2, 1, 'PARTICIPANT', 'APPROVED', NOW(), NOW()), (2, 4, 'PARTICIPANT', 'PENDING', NOW(), NOW()),
                                                                                                                        (3, 5, 'PARTICIPANT', 'APPROVED', NOW(), NOW()), (3, 6, 'PARTICIPANT', 'APPROVED', NOW(), NOW()),
                                                                                                                        (8, 2, 'PARTICIPANT', 'APPROVED', NOW(), NOW()), (8, 3, 'PARTICIPANT', 'APPROVED', NOW(), NOW()),
                                                                                                                        (8, 4, 'PARTICIPANT', 'APPROVED', NOW(), NOW()), (8, 5, 'PARTICIPANT', 'APPROVED', NOW(), NOW());

-- ============================================
-- 챗봇 대화 이력 데이터 삽입
-- ============================================
INSERT INTO chatbot_conversations (user_id, user_message, bot_response, intent_type, created_at, updated_at) VALUES
                                                                                                                 (1, '양재 코스트코 근처에 견과류 소분 모임 있나요?', '네, 현재 양재 코스트코에서 견과류 소분 모임이 진행중입니다. 참여하실 수 있습니다.', 'MEETING_SEARCH', NOW(), NOW()),
                                                                                                                 (2, '소분 모임은 어떻게 참여하나요?', '원하는 모임을 선택하고 참여 신청을 하시면, 호스트의 승인 후 참여가 확정됩니다.', 'SERVICE_GUIDE', NOW(), NOW());

-- ============================================
-- 챗봇 모임 추천 데이터 삽입
-- ============================================
INSERT INTO chatbot_meeting_suggestions (conversation_id, meeting_id, suggestion_reason, created_at, updated_at) VALUES
    (1, 1, '사용자가 찾던 양재 코스트코 견과류 소분 모임', NOW(), NOW());

-- ============================================
-- 피드백 데이터 삽입 (완료된 모임 기준)
-- ============================================
INSERT INTO feedbacks (giver_user_id, receiver_user_id, meeting_id, is_positive, created_at, updated_at) VALUES
                                                                                                             (1, 2, 8, TRUE, NOW(), NOW()), (1, 3, 8, TRUE, NOW(), NOW()), (1, 4, 8, FALSE, NOW(), NOW()),
                                                                                                             (2, 1, 8, TRUE, NOW(), NOW()), (3, 1, 8, TRUE, NOW(), NOW()), (2, 3, 8, TRUE, NOW(), NOW());

-- ============================================
-- 현재 참여자 수 업데이트
-- ============================================
UPDATE meetings SET current_participants = (
    SELECT COUNT(*) FROM meeting_participants
    WHERE meeting_participants.meeting_id = meetings.meeting_id AND application_status = 'APPROVED'
);

COMMIT;