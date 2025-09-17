
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import apiClient from '../services/apiClient';
import { AuthService } from '../services/authService';

import CommentList from '../components/Comment/CommentList.jsx';
import CommentModal from '../components/Comment/CommentModal'

const CommentPage = () => {
    const { meetingId } = useParams();
    const [comments, setComments] = useState([]);
    const [newCommentContent, setNewCommentContent] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false); // 모달 상태
    const [selectedComment, setSelectedComment] = useState(null); // 선택된 댓글 정보
    const [editedCommentContent, setEditedCommentContent] = useState(''); // 수정할 댓글 내용
    const [isLoggedIn, setIsLoggedIn] = useState(false); // 로그인 상태 관리
    const [currentUserId, setCurrentUserId] = useState(null); // 현재 로그인한 사용자 ID
    const [isParticipant, setIsParticipant] = useState(false); // 모임 참여자 상태 관리


    useEffect(() => {
        const checkLoginStatus = async () => {
            try {
                const user = await AuthService.me();
                setIsLoggedIn(true);
                setCurrentUserId(user.data.providerId);

                // 모임 참여자인지 확인하는 API 호출 (백엔드에 해당 API가 있다고 가정)
                const participationResponse = await apiClient.get(
                    `/meetings/${meetingId}/participants/check?providerId=${providerId}`
                );
                setIsParticipant(participationResponse.data.isParticipant);
            } catch {
                // 로그인하지 않았거나 토큰이 유효하지 않은 경우
                setIsLoggedIn(false);
                setCurrentUserId(null);
            }
        };

        checkLoginStatus();
    }, []);

    // 댓글 목록 불러오기 함수
    const fetchComments = async () => {
        try {
            // apiClient 사용: 별도의 토큰 설정 없이 자동으로 쿠키를 포함합니다.
            const response = await apiClient.get(
                `/meetings/${meetingId}/comments`
            );
            const sortedComments = response.data.comments.slice().reverse();
            setComments(sortedComments);
        } catch (error) {
            console.error("댓글을 불러오는 데 실패했습니다.", error);
        }
    };

    useEffect(() => {
        if (meetingId) {
            fetchComments();
        }
    }, [meetingId]);



    /**
     * 댓글 작성 핸들러
     * @param e
     * @returns {Promise<void>}
     */
    const handleCommentSubmit = async (e) => {
        e.preventDefault(); // 기본 폼 제출 동작 방지

        if (!newCommentContent.trim()) {
            alert("댓글 내용을 입력해주세요.");
            return;
        }

        if (!isLoggedIn) {
            alert("댓글을 작성하려면 로그인이 필요합니다.");
            return;
        }

        if (!isParticipant) {
            alert("댓글을 작성하려면 모임에 참여해야 합니다.");
            return;
        }

        try {
            // apiClient 사용: 별도의 토큰 설정 없이 자동으로 쿠키를 포함합니다.
            await apiClient.post(
                `/meetings/${meetingId}/comments`,
                { content: newCommentContent },
                {
                    headers: { 'Content-Type': 'application/json' },
                }
            );

            fetchComments();
            setNewCommentContent('');
        } catch (error) {
            console.error("댓글 작성 실패:", error.response ? error.response.data : error.message);
            alert("댓글 작성에 실패했습니다.");
        }
    };

    /**
     * 댓글 수정 핸들러
     * @param commentId 수정할 댓글 ID
     * @returns {Promise<void>}
     */
    const handleCommentUpdate = async (commentId) => {
        if (!editedCommentContent.trim()) {
            alert("수정할 댓글 내용을 입력해주세요.");
            return;
        }

        if (!isLoggedIn) {
            // 댓글을 수정하려면 로그인이 필요함을 명시
            alert("댓글을 수정하려면 로그인이 필요합니다.");
            return;
        }

        if (!isParticipant) {
            alert("댓글을 수정하려면 모임에 참여해야 합니다.");
            return;
        }

        try {
            // apiClient 사용
            await apiClient.put(
                `/meetings/${meetingId}/comments/${commentId}`,
                { content: editedCommentContent },
                { headers: { 'Content-Type': 'application/json' } }
            );

            fetchComments();
            handleCloseModal();
        } catch (error) {
            console.error("댓글 수정 실패:", error.response ? error.response.data : error.message);
            alert("댓글 수정에 실패했습니다.");
        }
    };

    // 댓글 삭제 함수
    const handleCommentDelete = async (commentId) => {
        if (!isLoggedIn) {
            alert("댓글을 삭제하려면 로그인이 필요합니다.");
            return;
        }

        if (!isParticipant) {
            alert("댓글을 삭제하려면 모임에 참여해야 합니다.");
            return;
        }

        if (window.confirm('정말 이 댓글을 삭제하시겠습니까?')) {
            try {
                // apiClient 사용
                await apiClient.delete(
                    `/meetings/${meetingId}/comments/${commentId}`
                );
                fetchComments();
            } catch (error) {
                console.error('댓글 삭제 실패:', error);
                alert("댓글 삭제에 실패했습니다.");
            }
        }
    };

    // 모달을 여는 함수
    const handleOpenModal = (comment) => {
        if (!isLoggedIn) {
            alert("댓글 수정 및 삭제 기능은 로그인 후 이용할 수 있습니다.");
            return;
        }

        if (!isParticipant) {
            alert("댓글을 작성하려면 모임에 참여해야 합니다.");
            return;
        }

        setSelectedComment(comment);
        setEditedCommentContent(comment.content);
        setIsModalOpen(true);
    };

    // 모달을 닫는 함수
    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedComment(null);
    };

    return (
        <>
            <section>
                <CommentList
                    comments={comments}
                    onOpenModal={handleOpenModal}
                    isLoggedIn={isLoggedIn}
                    currentUserId={currentUserId}
                />
            </section>

            {isLoggedIn && isParticipant ? (
                <section>
                    <h3>댓글 작성 폼</h3>
                    <form className="comment-form" onSubmit={handleCommentSubmit}>
                        <textarea
                            placeholder="새로운 댓글을 입력하세요."
                            value={newCommentContent}
                            onChange={(e) => setNewCommentContent(e.target.value)}
                        ></textarea>
                        <button type="submit">작성</button>
                    </form>
                </section>
            ) : (
                <p>댓글 작성은 모임에 참여한 사용자만 가능합니다.</p>
            )}

            <CommentModal
                isOpen={isModalOpen}
                onClose={handleCloseModal}
                comment={selectedComment}
                editedContent={editedCommentContent}
                onEditChange={setEditedCommentContent}
                onUpdate={handleCommentUpdate}
                onDelete={handleCommentDelete}
            />
        </>
    );
};

export default CommentPage;