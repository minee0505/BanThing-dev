
import React from 'react';

const CommentList = ({ comments, onOpenModal }) => {
    return (
        <div className="comment-list-container">
            <h3>댓글 ({comments.length}개)</h3>
            {comments.length > 0 ? (
                comments.map((comment) => (
                    <div className="comment" key={comment.commentId}>
                        <div>
                            <p><strong>{comment.nickname}</strong></p>
                            <p>{comment.content}</p>
                        </div>
                        {/* 💡 onClick 이벤트 추가 */}
                        <button className="comment-options-btn" onClick={() => onOpenModal(comment)}>
                            ...
                        </button>
                    </div>
                ))
            ) : (
                <p>댓글이 없습니다.</p>
            )}
        </div>
    );
};

export default CommentList;