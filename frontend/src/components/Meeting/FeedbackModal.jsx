// FeedbackModal.jsx
import React, { useState } from 'react';
import { FaTimes } from 'react-icons/fa';
import styles from './FeedbackModal.module.scss';
import { postFeedback } from '../../services/meetingDetailApi.js';
import { useAuthStore } from '../../stores/authStore';

const FeedbackModal = ({ isOpen, onClose, targetUser, meetingId }) => {
    const { user } = useAuthStore(); // 현재 로그인한 사용자 정보 가져오기

    if (!isOpen || !targetUser) {
        return null;
    }

    console.log("targetUser : ", targetUser);
    console.log("현재 사용자 : ", user);

    // eslint-disable-next-line react-hooks/rules-of-hooks
    const [feedbackScore, setFeedbackScore] = useState(null); // 'GOOD' 또는 'BAD'
    // eslint-disable-next-line react-hooks/rules-of-hooks
    const [isSubmitting, setIsSubmitting] = useState(false); // 제출 중 상태

    const handleFeedbackChange = (e) => {
        setFeedbackScore(e.target.value);
    };

    const handleConfirm = async () => {
        if (!feedbackScore) {
            alert("피드백 항목을 선택해주세요.");
            return;
        }

        if (!user || !user.userId) {
            alert("로그인 정보를 확인할 수 없습니다.");
            return;
        }

        setIsSubmitting(true);

        try {
            // 피드백 타입 매핑: 'GOOD' -> 'POSITIVE', 'BAD' -> 'NEGATIVE'
            const feedbackType = feedbackScore === 'GOOD' ? 'POSITIVE' : 'NEGATIVE';

            // 백엔드가 userId 또는 nickname을 모두 받을 수 있도록 수정
            const targetIdentifier = targetUser.userId || targetUser.nickname;
            const userIdentifier = user.userId || user.nickname;

            const result = await postFeedback(
                meetingId,           // 모임 ID
                targetIdentifier,   // 피드백을 받을 사용자 ID
                userIdentifier,         // 피드백을 주는 사용자 ID (현재 로그인한 사용자)
                feedbackType         // 피드백 타입
            );

            if (result.success) {
                alert('피드백이 성공적으로 제출되었습니다!');
                onClose();
            } else {
                alert(result.message || '피드백 제출에 실패했습니다.');
            }
        } catch (error) {
            console.error("피드백 제출 중 오류 발생:", error);
            alert('피드백 제출 중 오류가 발생했습니다.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className={styles.modalOverlay}>
            <div className={styles.modalContent}>
                <button className={styles.closeButton} onClick={onClose}>
                    <FaTimes />
                </button>
                <h3>{targetUser.nickname}님에 대한 피드백</h3>
                <p className={styles.description}>
                    모임 경험이 어떠셨나요?
                </p>

                <div className={styles.voteContainer}>
                    <label className={styles.voteOption}>
                        <input
                            type="radio"
                            name="feedback"
                            value="GOOD"
                            checked={feedbackScore === 'GOOD'}
                            onChange={handleFeedbackChange}
                            disabled={isSubmitting}
                        />
                        좋았습니다. 😊
                    </label>
                    <label className={styles.voteOption}>
                        <input
                            type="radio"
                            name="feedback"
                            value="BAD"
                            checked={feedbackScore === 'BAD'}
                            onChange={handleFeedbackChange}
                            disabled={isSubmitting}
                        />
                        싫었습니다. 😠
                    </label>
                </div>

                <div className={styles.buttonGroup}>
                    <button
                        onClick={handleConfirm}
                        className={styles.confirmButton}
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? '제출 중...' : '확인'}
                    </button>
                    <button
                        onClick={onClose}
                        className={styles.cancelButton}
                        disabled={isSubmitting}
                    >
                        취소
                    </button>
                </div>
            </div>
        </div>
    );
};

export default FeedbackModal;