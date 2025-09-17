// src/components/CommentModal.jsx
import React from 'react';

const CommentModal = ({
                          isOpen,
                          onClose,
                          comment,
                          editedContent, // 수정할 댓글 내용
                          onEditChange, // 수정 내용 변경 핸들러
                          onUpdate, // 수정 완료 핸들러
                          onDelete // 삭제 핸들러 프롭
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
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="modal-close-btn" onClick={onClose}>&times;</button>
                <p>댓글 수정</p>
                <textarea
                    value={editedContent}
                    onChange={(e) => onEditChange(e.target.value)}
                    rows="4"
                    cols="50"
                ></textarea>
                <button onClick={handleUpdateClick}>수정</button>
                <button onClick={handleDeleteClick}>삭제</button>
            </div>
        </div>
    );
};

export default CommentModal;