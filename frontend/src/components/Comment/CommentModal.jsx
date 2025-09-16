// src/components/CommentModal.jsx
import React from 'react';

const CommentModal = ({ isOpen, onClose, comment }) => {
    if (!isOpen || !comment) {
        return null;
    }

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <button className="modal-close-btn" onClick={onClose}>&times;</button>
                <p>선택된 댓글: {comment.content}</p>
                {/* 여기에 수정, 삭제 등 모달에 들어갈 내용을 추가 */}
                <button>수정</button>
                <button>삭제</button>
            </div>
        </div>
    );
};

export default CommentModal;