// src/pages/CommentPage.jsx
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import CommentList from '../components/Comment/CommentList.jsx';
import CommentModal from '../components/Comment/CommentModal'

const CommentPage = () => {
    const { meetingId } = useParams();
    const [comments, setComments] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false); // 모달 상태
    const [selectedComment, setSelectedComment] = useState(null); // 선택된 댓글 정보
    const token =
        'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0NDQ5Nzg3ODkwIiwiaWF0IjoxNzU4MDIxMzEwLCJleHAiOjE3NTgwMjIyMTB9.TgB1w2LPIIvDV2z9cWtyM4Mbk--uPrrYlLLEb9yt3VY'; // TODO: 실제 인증 토큰으로 교체하세요

    useEffect(() => {
        if (meetingId) {
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
            fetchComments();
        }
    }, [meetingId, token]);

    // 모달을 여는 함수
    const handleOpenModal = (comment) => {
        setSelectedComment(comment);
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
                <form className="comment-form" onSubmit={() => {}}>
                    <textarea placeholder="새로운 댓글을 입력하세요."></textarea>
                    <button type="submit">등록</button>
                </form>
            </section>

            <CommentModal isOpen={isModalOpen} onClose={handleCloseModal} comment={selectedComment} />
        </>
    );
};

export default CommentPage;