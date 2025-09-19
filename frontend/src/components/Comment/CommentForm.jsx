import React from 'react';
import styles from '../../pages/MeetingDetailPage.module.scss'; // 경로가 동일하다면 그대로 사용

const CommentForm = ({ newComment, setNewComment, handleCommentSubmit, isSubmittingComment }) => {
    return (
        <form onSubmit={handleCommentSubmit} className={styles.commentForm}>
            <div className={styles.commentInputWrapper}>
                <textarea
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    placeholder="댓글을 입력하세요..."
                    className={styles.commentInput}
                    rows="3"
                />
                <button
                    type="submit"
                    disabled={isSubmittingComment || !newComment.trim()}
                    className={styles.commentSubmit}
                >
                    {isSubmittingComment ? '등록 중...' : '등록'}
                </button>
            </div>
        </form>
    );
};

export default CommentForm;