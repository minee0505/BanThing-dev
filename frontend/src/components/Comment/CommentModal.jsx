// src/components/CommentModal.jsx
import React from 'react';
import styles from './CommentModal.module.scss';
const CommentModal = ({
                          isOpen,
                          onClose,
                          comment,
                          // editedContent, // 수정할 댓글 내용
                          // onEditChange, // 수정 내용 변경 핸들러
                          onUpdate, // 수정 완료 핸들러
                          onDelete, // 삭제 핸들러 프롭
                          modalPosition
                      }) => {
    if (!isOpen || !comment) {
        return null;
    }

    // 💡 추가: 수정 버튼 클릭 핸들러
    const handleUpdateClick = () => {
        onUpdate(comment.commentId);
    };

    // 💡 추가: 삭제 버튼 클릭 핸들러
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
                    top: `${modalPosition.y}px`,
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