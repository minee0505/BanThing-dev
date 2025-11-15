import React from 'react';
import styles from '../../pages/MeetingDetailPage.module.scss';

const CommentForm = ({
                         newComment,
                         setNewComment,
                         handleCommentSubmit,
                         isSubmittingComment,
                         isEditing,
                         editedCommentContent, // ✨✨✨ 수정된 내용 상태 받음 ✨✨✨
                         setEditedCommentContent, // ✨✨✨ 수정된 내용 핸들러 받음 ✨✨✨
                         onCancelEdit
}) => {
    // 폼의 value와 onChange 핸들러를 조건부로 설정
    const value = isEditing ? editedCommentContent : newComment;
    const onChangeHandler = isEditing ? setEditedCommentContent : setNewComment;

    return (
        <form onSubmit={handleCommentSubmit} className={styles.commentForm}>
            <div className={styles.commentInputWrapper}>
                <textarea
                    value={value} // 조건부 값 사용
                    onChange={(e) => onChangeHandler(e.target.value)} // 조건부 핸들러 사용
                    placeholder={isEditing ? "댓글을 수정하세요..." : "댓글을 입력하세요..."}
                    className={styles.commentInput}
                    rows="3"
                />
                <button
                    type="submit"
                    disabled={isSubmittingComment || !value.trim()}
                    className={styles.commentSubmit}
                >
                    {isSubmittingComment ? '처리 중...' : (isEditing ? '수정' : '등록')}
                </button>
                {isEditing && (
                    <button
                        type="button"
                        onClick={onCancelEdit}
                        className={styles.commentCancel}
                    >
                        취소
                    </button>
                )}
            </div>
        </form>
    );
};

export default CommentForm;