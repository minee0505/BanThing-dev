// FeedbackModal.jsx
import React, { useState } from 'react';
import { FaTimes } from 'react-icons/fa';
import styles from './FeedbackModal.module.scss';
import { postFeedback } from '../../services/meetingDetailApi.js';


const FeedbackModal = ({ isOpen, onClose, targetUser, meetingId }) => {
    if (!isOpen || !targetUser) {
        return null;
    }
    console.log("targetUser : ", targetUser);
    // eslint-disable-next-line react-hooks/rules-of-hooks
    const [feedbackScore, setFeedbackScore] = useState(null); // 'GOOD' 또는 'BAD'

    const handleFeedbackChange = (e) => {
        setFeedbackScore(e.target.value);
    };

    const handleConfirm = async () => {
        if (!feedbackScore) {
            alert("피드백 항목을 선택해주세요.");
            return;
        }

        try {
            // API 호출을 위해 피드백 점수(5점 또는 -5점)를 설정합니다.
            const scoreToSend = feedbackScore === 'GOOD' ? 5 : -5;
            const result = await postFeedback(meetingId, targetUser.userId, scoreToSend);

            if (result.success) {
                alert('피드백이 성공적으로 제출되었습니다!');
                onClose();
            } else {
                alert(result.message || '피드백 제출에 실패했습니다.');
            }
        } catch (error) {
            console.error("피드백 제출 중 오류 발생:", error);
            alert('피드백 제출 중 오류가 발생했습니다.');
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
                        />
                        싫었습니다. 😠
                    </label>
                </div>

                <div className={styles.buttonGroup}>
                    <button onClick={handleConfirm} className={styles.confirmButton}>확인</button>
                    <button onClick={onClose} className={styles.cancelButton}>취소</button>
                </div>
            </div>
        </div>
    );
};

export default FeedbackModal;