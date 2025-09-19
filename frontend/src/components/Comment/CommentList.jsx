import React from 'react';
import styles from '../../pages/MeetingDetailPage.module.scss';

const CommentList = ({ comments, user, handleOpenModal }) => {
    return (
        <div className={styles.commentsList}>
            {comments.length === 0 ? (
                <div className={styles.noComments}>
                    <p>아직 댓글이 없습니다.</p>
                    <p>첫 번째 댓글을 남겨보세요!</p>
                </div>
            ) : (
                comments.map(comment => (
                    <div key={comment.commentId} className={styles.commentItem}>
                        <div className={styles.commentAvatar}>
                            {comment.profileImageUrl ? (
                                <img src={comment.profileImageUrl} alt={comment.nickname} />
                            ) : (
                                comment.nickname.charAt(0)
                            )}
                        </div>
                        <div className={styles.commentContent}>
                            <div className={styles.commentAuthor}>
                                {comment.nickname}
                                <span className={styles.commentTime}>
                                    {new Date(comment.createdAt).toLocaleString()}
                                </span>
                            </div>
                            <div className={styles.commentText}>
                                {comment.content}
                            </div>
                            {user.userId === comment.userId && (
                                <button
                                    className={styles.commentMoreButton}
                                    onClick={(e) => handleOpenModal(comment, e)}
                                >
                                    ...
                                </button>
                            )}
                        </div>
                    </div>
                ))
            )}
        </div>
    );
};

export default CommentList;