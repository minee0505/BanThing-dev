import React from 'react';
// import loginPage from "../../pages/LoginPage.jsx";

/**
 * 모임 상세 페이지의 '참여자 목록' 탭 UI를 담당하는 컴포넌트
 * @param {object} props
 * @param {object} props.participants - 참여자 목록 데이터 ({ approved: [], pending: [] })
 * @param {boolean} props.isHost - 현재 사용자가 호스트인지 여부
 * @param {function} props.onApprove - 참여 승인 버튼 클릭 시 호출될 함수
 * @param {function} props.onReject - 참여 거절 버튼 클릭 시 호출될 함수
 * @param {object} props.styles - 부모 컴포넌트의 CSS 모듈 스타일 객체
 * @param {string} props.meetingStatus - 모임 상태 (CANCELED, RECRUITING 등)
 * @param {function} props.openFeedbackModal - 피드백 모달을 여는 함수
 *
 * @author 고동현
 * @since 2025.09.19
 * @version 1.1.0 // 버전 업데이트
 */
const ParticipantsTab = ({ participants, isHost, onApprove, onReject, styles, meetingStatus, myUserId,myUserNickName, openFeedbackModal  }) => {
    console.log("participantsID : ", participants.approved);
    console.log("participantsNICKNAME : ", participants.approved);
    console.log("userId : ", myUserId);


    const showActionButtons = meetingStatus === 'RECRUITING' || meetingStatus === 'FULL';

    return (
        <div className={styles.participantsTab}>
            {/* 확정된 참여자 목록 */}
            <h4>확정된 참여자</h4>
            <div className={styles.participantsList}>
                {participants.approved.map((participant) => (
                    <div key={participant.userId} className={styles.participantItem}>
                        <div className={styles.participantAvatar}>
                            {participant.profileImageUrl ? (
                                <img src={participant.profileImageUrl} alt={participant.nickname} />
                            ) : (
                                <div className={styles.defaultAvatar}>
                                    {participant.nickname.charAt(0)}
                                </div>
                            )}
                        </div>
                        <div className={styles.participantInfo}>
                            <div className={styles.participantName}>
                                <span>{participant.nickname}</span>
                                {participant.participantType === 'HOST' && (
                                    <span className={styles.hostLabel}>호스트</span>
                                )}
                            </div>
                            <div className={styles.participantStats}>
                                신뢰도: {participant.trustScore}점
                            </div>
                        </div>
                        {/* 모임 상태가 COMPLETED일 때 피드백 모달을 여는 버튼 추가 */}
                        {
                            participant.userId !== myUserId &&
                            participant.nickname !== myUserNickName &&
                            meetingStatus === 'COMPLETED' &&(
                                <button
                                    onClick={() => openFeedbackModal(participant.userId, participant.nickname)}
                                    className={styles.feedbackButton}
                                >
                                    피드백 남기기
                                </button>
                            )}
                    </div>
                ))}
            </div>

            {/* 신청 대기자 목록 (호스트에게만 보임) */}
            {isHost && onApprove && participants.pending.length > 0 && (
                <>
                    <h4>신청 대기자</h4>
                    <div className={styles.participantsList}>
                        {participants.pending.map((participant) => (
                            <div key={participant.userId} className={styles.participantItem}>
                                <div className={styles.participantAvatar}>
                                    {participant.profileImageUrl ? (
                                        <img src={participant.profileImageUrl} alt={participant.nickname} />
                                    ) : (
                                        <div className={styles.defaultAvatar}>
                                            {participant.nickname.charAt(0)}
                                        </div>
                                    )}
                                </div>
                                <div className={styles.participantInfo}>
                                    <div className={styles.participantName}>
                                        <span>{participant.nickname}</span>
                                        <span className={styles.pendingLabel}>대기중</span>
                                    </div>
                                    <div className={styles.participantStats}>
                                        신뢰도: {participant.trustScore}점
                                    </div>
                                </div>
                                {/* 모임 상태가 'CANCELED'가 아닐 때만 승인/거절 버튼을 보여줍니다. */}
                                {/* 모임이 모집중(RECRUITING) 또는 모집마감(FULL) 상태일 때만 버튼을 보여줍니다. */}
                                {showActionButtons && (
                                    <div className={styles.participantActions}>
                                        <button
                                            onClick={() => onApprove(participant)}
                                            className={styles.approveButton}
                                        >
                                            승인
                                        </button>
                                        <button
                                            onClick={() => onReject(participant)}
                                            className={styles.rejectButton}
                                        >
                                            거절
                                        </button>
                                    </div>
                                )}

                            </div>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
};

export default ParticipantsTab;