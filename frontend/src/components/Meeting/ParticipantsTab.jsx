import React from 'react';

/**
 * 모임 상세 페이지의 '참여자 목록' 탭 UI를 담당하는 컴포넌트
 * @param {object} props
 * @param {object} props.participants - 참여자 목록 데이터 ({ approved: [], pending: [] })
 * @param {boolean} props.isHost - 현재 사용자가 호스트인지 여부
 * @param {function} props.onApprove - 참여 승인 버튼 클릭 시 호출될 함수
 * @param {function} props.onReject - 참여 거절 버튼 클릭 시 호출될 함수
 * @param {object} props.styles - 부모 컴포넌트의 CSS 모듈 스타일 객체
 */
const ParticipantsTab = ({ participants, isHost, onApprove, onReject, styles }) => {
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
                                신뢰도: {participant.TrusterScore}점
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* 신청 대기자 목록 (호스트에게만 보임) */}
            {isHost && participants.pending.length > 0 && (
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
                                        신뢰도: {participant.TrusterScore}점
                                    </div>
                                </div>
                                {/* 승인/거절 버튼에 핸들러 함수를 연결합니다. */}
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
                            </div>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
};

export default ParticipantsTab;