// src/components/CommentModal.jsx
import React from 'react';
import styles from './CommentModal.module.scss';
const CommentModal = ({
                          isOpen,
                          onClose,
                          comment, // comment 객체 전체를 받음
                          onUpdate,  // 부모로부터 받은 수정 시작 핸들러
                          onDelete, // 삭제 핸들러
                          modalPosition
                      }) => {
    if (!isOpen || !comment) {
        return null;
    }

    // 수정 버튼 클릭 핸들러
    const handleUpdateClick = () => {
        onUpdate(comment); // comment 객체 전체를 전달
        onClose();
    };

    // 삭제 버튼 클릭 핸들러
    const handleDeleteClick = () => {
        onDelete(comment.commentId);
        onClose(); // 삭제 후 모달 닫기
    };

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div
                className={styles.modalContent}
                style={{
                    position: 'absolute',
                    top: `${modalPosition.y + 150}px`,
                    left: `${modalPosition.x -10}px`,
                    // 모달의 위치를 버튼의 우측 하단에 오도록 설정
                    transform: 'translate(10px, 10px)',
                }}
                onClick={(e) => e.stopPropagation()}
            >
                <button className={styles.modalCloseBtn} onClick={onClose}>&times;</button>
                <button onClick={handleUpdateClick}>수정</button>
                <button onClick={handleDeleteClick}>삭제</button>
            </div>
        </div>
    );
};

export default CommentModal;