import React, { useEffect, useState } from 'react';
import {Link, useParams} from 'react-router-dom';
import axios from 'axios';

const MeetingDetailPage = () => {
    // 1. URL에서 meetingId 추출
    const { meetingId } = useParams();
    const [comments, setComments] = useState([]);
    const token =
        'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI0NDQ5Nzg3ODkwIiwiaWF0IjoxNzU4MDIxMzEwLCJleHAiOjE3NTgwMjIyMTB9.TgB1w2LPIIvDV2z9cWtyM4Mbk--uPrrYlLLEb9yt3VY'; // TODO: 실제 인증 토큰으로 교체하세요

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
            <h2>댓글 ({comments.length})</h2>
            <Link to={`/meetings/${meetingId}/comments`} className="comment-link-area">
                {comments.length > 0 ? (
                    // 댓글이 있을 때 첫 번째 댓글과 그 작성자를 보여줌
                    <div>
                        <p><strong>{comments[0].nickname}:</strong> {comments[0].content}</p>
                    </div>
                ) : (
                    // 댓글이 없을 때
                    <div>
                        <p>등록된 댓글이 없습니다.</p>
                    </div>
                )}
            </Link>
        </div>
    );
};

export default MeetingDetailPage;