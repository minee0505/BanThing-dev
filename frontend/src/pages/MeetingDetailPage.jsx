import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

const MeetingDetailPage = () => {
    // 1. URL에서 meetingId 추출
    const { meetingId } = useParams();
    const [comments, setComments] = useState([]);
    const token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0NDQ5Nzg3ODkwIiwiaWF0IjoxNzU4MDE0NTI3LCJleHAiOjE3NTgwMTU0Mjd9.EdA_KFKGU9gPmstc3twaX6jOQnLTiTaCZeDDLQbiEeI'; // TODO: 실제 인증 토큰으로 교체하세요

    useEffect(() => {
        // 2. meetingId가 유효할 때만 API 호출
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
                    setComments(response.data.comments);
                } catch (error) {
                    console.error("댓글을 불러오는 데 실패했습니다.", error);
                }
            };

            fetchComments();
        }
    }, [meetingId, token]);

    return (
        <div>
            <h2>댓글 목록</h2>
            {comments.length > 0 ? (
                <ul>
                    {comments.map((comment) => (
                        <li key={comment.commentId}>
                            <strong>{comment.nickname}:</strong> {comment.content}
                        </li>
                    ))}
                </ul>
            ) : (
                <p>댓글이 없습니다.</p>
            )}
        </div>
    );
};

export default MeetingDetailPage;