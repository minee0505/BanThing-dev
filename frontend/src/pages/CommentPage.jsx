// src/pages/CommentPage.jsx
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import CommentList from '../components/Comment/CommentList.jsx';
import CommentModal from '../components/Comment/CommentModal'

const CommentPage = () => {
    const { meetingId } = useParams();
    const [comments, setComments] = useState([]);
    const [newCommentContent, setNewCommentContent] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false); // 모달 상태
    const [selectedComment, setSelectedComment] = useState(null); // 선택된 댓글 정보
    const [editedCommentContent, setEditedCommentContent] = useState(''); // 수정할 댓글 내용
    const token =
        'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0NDQ5Nzg3ODkwIiwiaWF0IjoxNzU4MDc5NjI2LCJleHAiOjE3NTgwODA1MjZ9.ltXrN6w-XZaLEXqt5Fog14jjWecc_iP1tTg8WFfITtk'; // TODO: 실제 인증 토큰으로 교체하세요

    // 댓글 목록 불러오기 함수
    const fetchComments = async () => {
        try {
            const response = await axios.get(
                `http://localhost:9000/api/meetings/${meetingId}/comments`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
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
    }, [meetingId, token]);

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

        try {
            // CommentCreateRequest DTO에 맞게 JSON 데이터 전송
            const response = await axios.post(
                `http://localhost:9000/api/meetings/${meetingId}/comments`,
                {
                    content: newCommentContent,
                    // DTO에 giverId가 포함되어 있으나, 백엔드에서 @AuthenticationPrincipal을 사용하므로 생략 가능
                    // 백엔드 컨트롤러 (@AuthenticationPrincipal String providerId)가 giverId를 자동으로 처리합니다.
                    // 만약 DTO로 giverId를 전달해야 한다면, 여기에 추가해야 합니다.
                },
                {
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            // 성공적으로 댓글이 생성되면, 댓글 목록을 다시 불러와 화면을 업데이트합니다.
            fetchComments();
            // 입력 필드 초기화
            setNewCommentContent('');
            console.log("댓글이 성공적으로 작성되었습니다:", response.data);

        } catch (error) {
            console.error("댓글 작성 실패:", error.response ? error.response.data : error.message);
            alert("댓글 작성에 실패했습니다.");
        }
    };

    /**
     * 💡 추가: 댓글 수정 핸들러
     * @param commentId 수정할 댓글 ID
     * @returns {Promise<void>}
     */
    const handleCommentUpdate = async (commentId) => {
        if (!editedCommentContent.trim()) {
            alert("수정할 댓글 내용을 입력해주세요.");
            return;
        }

        try {
            await axios.put(
                `http://localhost:9000/api/meetings/${meetingId}/comments/${commentId}`,
                { content: editedCommentContent },
                { headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } }
            );

            fetchComments(); // 수정 후 댓글 목록 새로고침
            handleCloseModal(); // 모달 닫기
            console.log(`댓글 ${commentId}이(가) 성공적으로 수정되었습니다.`);
        } catch (error) {
            console.error("댓글 수정 실패:", error.response ? error.response.data : error.message);
            alert("댓글 수정에 실패했습니다.");
        }
    };

    // 💡 추가: 댓글 삭제 함수
    const handleCommentDelete = async (commentId) => {
        if (window.confirm('정말 이 댓글을 삭제하시겠습니까?')) {
            try {
                await axios.delete(
                    `http://localhost:9000/api/meetings/${meetingId}/comments/${commentId}`,
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );
                // 댓글 목록을 다시 불러와 UI 업데이트
                fetchComments();
            } catch (error) {
                console.error('댓글 삭제 실패:', error);
                alert("댓글 삭제에 실패했습니다.");
            }
        }
    };

    // 모달을 여는 함수
    const handleOpenModal = (comment) => {
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
                {/* CommentList 컴포넌트에 onOpenModal 프롭 전달 */}
                <CommentList comments={comments} onOpenModal={handleOpenModal} />
            </section>

            <section>
                <h3>댓글 작성 폼</h3>
                <form className="comment-form" onSubmit={handleCommentSubmit}>
                    <textarea
                        placeholder="새로운 댓글을 입력하세요."
                        value={newCommentContent} // 상태와 입력 값 연결
                        onChange={(e) => setNewCommentContent(e.target.value)} // 입력 값 변경 핸들러
                    ></textarea>
                    <button type="submit">작성</button>
                </form>
            </section>

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