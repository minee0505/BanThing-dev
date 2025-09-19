// MeetingDetailPage.jsx
import React, {useState, useEffect} from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import {
    getMeetingDetail,
    joinMeeting,
    leaveMeeting,
    getParticipants,
    deleteMeeting,
    getComments,
    postComment,
    updateComments
    // deleteComments
} from '../services/meetingDetailApi';
// import { getMeetingDetail, joinMeeting, leaveMeeting, getParticipants,  } from '../services/meetingDetailApi';
import { FaMapMarkerAlt, FaCalendarAlt, FaUsers, FaClock, FaEdit, FaTrash } from 'react-icons/fa';
import Chatbot from '../components/Chatbot/Chatbot';
import styles from './MeetingDetailPage.module.scss';
import {AuthService} from "../services/authService.js";
import CommentModal from "../components/Comment/CommentModal.jsx";
import CommentList from "../components/Comment/CommentList.jsx";
import CommentForm from "../components/Comment/CommentForm.jsx";

const MeetingDetailPage = () => {
    const { id } = useParams(); // 미팅id
    const navigate = useNavigate();
    const { isAuthenticated, user } = useAuthStore(); // 로그인 여부

    const [meeting, setMeeting] = useState(null);
    const [participants, setParticipants] = useState({ approved: [], pending: [] });
    const [activeTab, setActiveTab] = useState('participants');
    const [isLoading, setIsLoading] = useState(true);
    const [isJoining, setIsJoining] = useState(false);
    const [error, setError] = useState(null);
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState('');
    const [isSubmittingComment, setIsSubmittingComment] = useState(false); // 댓글 전송 상태
    const [isModalOpen, setIsModalOpen] = useState(false); // 모달 상태
    const [selectedComment, setSelectedComment] = useState(null); // 선택된 댓글 정보
    const [modalPosition, setModalPosition] = useState({ x: 0, y: 0 });
    // 댓글 수정
    const [isEditing, setIsEditing] = useState(false); // 수정 모드 여부
    const [editingCommentId, setEditingCommentId] = useState(null); // 수정할 댓글 ID
    const [editedCommentContent, setEditedCommentContent] = useState(''); // 수정할 댓글 내용 상태




    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }

        fetchMeetingDetail();
        fetchParticipants();
        fetchComments();
    }, [id, isAuthenticated, navigate]);


    // 댓글 목록 불러오기 함수-송민재
    const fetchComments = async () => {
        // console.log("user", user);

        try {
            const result = await getComments(id); // API 호출 함수 (새로 구현 필요)
            if (result.success) {
                setComments(result.data.reverse());
            } else {
                setError(result.message || '댓글 정보를 불러올 수 없습니다.')
            }
        } catch (error) {
            console.error('댓글을 불러오는 데 실패했습니다.', error);
        }
    };

    const fetchMeetingDetail = async () => {
        try {
            const result = await getMeetingDetail(id);
            if (result.success) {
                setMeeting(result.data);
            } else {
                setError(result.message || '모임 정보를 불러올 수 없습니다.');
            }
        } catch (error) {
            console.error('모임 상세 조회 실패:', error);
            setError('모임 정보를 불러오는 중 오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    const fetchParticipants = async () => {
        try {
            const result = await getParticipants(id);
            if (result.success) {
                setParticipants(result.data);
            }
        } catch (error) {
            console.error('참여자 목록 조회 실패:', error);
        }
    };

    const handleJoinMeeting = async () => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }

        setIsJoining(true);
        try {
            const result = await joinMeeting(id);
            if (result.success) {
                await fetchMeetingDetail();
                await fetchParticipants();
                alert('모임 참여 신청이 완료되었습니다!');
            } else {
                alert(result.message || '참여 신청에 실패했습니다.');
            }
        } catch (error) {
            console.error('모임 참여 실패:', error);
            alert('참여 신청 중 오류가 발생했습니다.');
        } finally {
            setIsJoining(false);
        }
    };

    const handleLeaveMeeting = async () => {
        if (!confirm('정말 모임에서 탈퇴하시겠습니까?')) return;

        try {
            const result = await leaveMeeting(id);
            if (result.success) {
                await fetchMeetingDetail();
                await fetchParticipants();
                alert('모임에서 탈퇴했습니다.');
            } else {
                alert(result.message || '탈퇴에 실패했습니다.');
            }
        } catch (error) {
            console.error('모임 탈퇴 실패:', error);
            alert('탈퇴 중 오류가 발생했습니다.');
        }
    };


    // 모달의 수정 버튼을 눌렀을 때 실행될 함수
    // 이 함수가 부모 컴포넌트의 상태를 변경하여 수정 폼을 활성화
    const startEditing = (comment) => {
        // 폼의 textarea에 기존 댓글 내용 채우기
        setEditedCommentContent(comment.content);
        setIsEditing(true); // 수정 모드 시작
        setEditingCommentId(comment.commentId); // 수정할 댓글 ID 저장
        setIsModalOpen(false); // 모달 닫기
    };

    // 수정 모드 취소 핸들러
    const handleCancelEdit = () => {
        setIsEditing(false);
        setEditingCommentId(null);
        setEditedCommentContent(''); // textarea 비우기
    };

    /**
     * 댓글 작성 핸들러
     * @param {Event} e - 폼 제출 이벤트 객체
     * @returns {Promise<void>}
     */
    const handleCommentSubmit = async (e) => {
        e.preventDefault(); // 기본 폼 제출 동작 방지

        if (isEditing && !editedCommentContent.trim()) {
            alert("수정할 내용을 입력해주세요.");
            return;
        } else if (!isEditing && !newComment.trim()) {
            alert("댓글 내용을 입력해주세요.");
            return;
        }

        // 2. 댓글 전송 중 상태로 변경하여 중복 제출 방지
        setIsSubmittingComment(true);

        try {
            if (isEditing) {
                // 수정 로직: 백엔드 API 경로와 요청 바디에 맞게 수정
                await updateComments(id, editingCommentId, { content: editedCommentContent });
                alert("댓글이 수정되었습니다.");
                handleCancelEdit(); // 수정 모드 종료 및 폼 초기화
            } else {
                // 댓글 작성 로직
                const result = await postComment(id, { content: newComment });
                if (result.success) {
                    setNewComment('');
                } else {
                    alert(result.message || "댓글 작성에 실패했습니다.");
                }
            }
            fetchComments(); // 작성 또는 수정 후 댓글 목록 갱신
        } catch (error) {
            console.error("댓글 작성/수정 실패:", error.response ? error.response.data : error.message);
            alert("댓글 작성/수정 중 오류가 발생했습니다.");
        } finally {
            setIsSubmittingComment(false);
        }
    };

    // ✨✨✨ 삭제 핸들러
    /*const handleCommentDelete = async (commentId) => {
        const isConfirmed = window.confirm('정말로 댓글을 삭제하시겠습니까?');
        if (!isConfirmed) return;

        try {
            const res = await deleteComment(commentId);
            if (res.success) {
                alert('댓글이 삭제되었습니다.');
                setComments(comments.filter(c => c.commentId !== commentId));
            } else {
                alert(res.message);
            }
        } catch (err) {
            console.error('댓글 삭제 오류:', err);
            alert('댓글 삭제 중 오류가 발생했습니다.');
        }
    };*/


    // 삭제 핸들러 추가
    const handleDeleteMeeting = async () => {
        if (!confirm('정말 모임을 삭제하시겠습니까? 삭제된 모임은 복구할 수 없습니다.')) return;

        try {
            const result = await deleteMeeting(id);
            if (result.success) {
                alert('모임이 삭제되었습니다.');
                // 메인 페이지로 리다이렉트
                navigate('/', { replace: true });
            } else {
                alert(result.message || '모임 삭제에 실패했습니다.');
            }
        } catch (error) {
            console.error('모임 삭제 실패:', error);
            alert('모임 삭제 중 오류가 발생했습니다.');
        }
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };

    const getStatusColor = (status) => {
        const statusColors = {
            'RECRUITING': styles.recruiting,
            'FULL': styles.full,
            'ONGOING': styles.ongoing,
            'COMPLETED': styles.completed,
            'CANCELLED': styles.cancelled
        };
        return statusColors[status] || '';
    };

    const getStatusText = (status) => {
        const statusTexts = {
            'RECRUITING': '모집중',
            'FULL': '모집완료',
            'ONGOING': '진행중',
            'COMPLETED': '모임종료',
            'CANCELLED': '모임취소'
        };
        return statusTexts[status] || status;
    };

    const getTrustBadgeClass = (score) => {
        if (score >= 500) return styles.trustGood;
        if (score <= 100) return styles.trustWarning;
        return styles.trustBasic;
    };

    const isHost = () => {
        return meeting?.hostInfo?.nickname === user?.nickname;
    };

    const isParticipating = () => {
        return participants.approved.some(p => p.nickname === user?.nickname) ||
            participants.pending.some(p => p.nickname === user?.nickname);
    };

    if (isLoading) {
        return (
            <div className={styles.container}>
                <div className={styles.loading}>
                    <div className={styles.loadingSpinner}></div>
                    <p>모임 정보를 불러오는 중...</p>
                </div>
                <Chatbot />
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.container}>
                <div className={styles.error}>
                    <h2>오류가 발생했습니다</h2>
                    <p>{error}</p>
                    <button onClick={() => navigate('/')} className={styles.backButton}>
                        목록으로 돌아가기
                    </button>
                </div>
                <Chatbot />
            </div>
        );
    }

    if (!meeting) {
        return (
            <div className={styles.container}>
                <div className={styles.notFound}>
                    <h2>모임을 찾을 수 없습니다</h2>
                    <button onClick={() => navigate('/')} className={styles.backButton}>
                        목록으로 돌아가기
                    </button>
                </div>
                <Chatbot />
            </div>
        );
    }



    // 댓글 삭제 함수
    /*const handleCommentDelete = async (commentId) => {
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
    };*/

    // 모달을 여는 함수
    const handleOpenModal = (comment, e) => {
        // console.log("선택된 댓글:", comment);
        const rect = e.target.getBoundingClientRect(); // 버튼의 위치와 크기 정보를 가져옴
        setModalPosition({
            x: rect.left,
            y: rect.top,
        });
        setSelectedComment(comment);
        setIsModalOpen(true);
        // console.log("isModalOpen 상태 변경:", isModalOpen); // 이 로그는 변경 전 값을 표시할 수 있음
    };

    // 모달을 닫는 함수
    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedComment(null);
    };


    return (
        <div className={styles.container}>
            <div className={styles.detailCard}>
                {/* 모임 기본 정보 */}
                <div className={styles.meetingHeader}>
                    <div className={styles.headerTop}>
                        <h1 className={styles.title}>{meeting.title}</h1>
                        <span className={`${styles.statusBadge} ${getStatusColor(meeting.status)}`}>
                            {getStatusText(meeting.status)}
                        </span>
                    </div>

                    <div className={styles.meetingMeta}>
                        <div className={styles.metaItem}>
                            <FaMapMarkerAlt />
                            <span>{meeting.martName}</span>
                        </div>
                        <div className={styles.metaItem}>
                            <FaCalendarAlt />
                            <span>{formatDate(meeting.meetingDate)}</span>
                        </div>
                        <div className={styles.metaItem}>
                            <FaUsers />
                            <span>{meeting.currentParticipants} / {meeting.maxParticipants}명</span>
                        </div>
                    </div>

                    {/* 호스트 정보 */}
                    <div className={styles.hostInfo}>
                        <div className={styles.hostAvatar}>
                            {meeting.hostInfo.profileImageUrl ? (
                                <img src={meeting.hostInfo.profileImageUrl} alt="호스트" />
                            ) : (
                                <div className={styles.defaultAvatar}>
                                    {meeting.hostInfo.nickname.charAt(0)}
                                </div>
                            )}
                        </div>
                        <div className={styles.hostDetails}>
                            <div className={styles.hostName}>
                                <span>{meeting.hostInfo.nickname} (호스트)</span>
                                <span className={getTrustBadgeClass(450)}>좋음</span>
                            </div>
                            <div className={styles.hostStats}>신뢰도 점수: 450점</div>
                        </div>
                        {isHost() && (
                            <div className={styles.hostActions}>
                                <button className={styles.editButton}>
                                    <FaEdit /> 수정
                                </button>
                                <button
                                    className={styles.deleteButton}
                                    onClick={handleDeleteMeeting}
                                >
                                    <FaTrash /> 삭제
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                {/* 모임 상세 내용 */}
                <div className={styles.meetingContent}>
                    <h3>모임 상세 정보</h3>
                    <div className={styles.description}>
                        {meeting.description.split('\n').map((line, index) => (
                            <p key={index}>{line}</p>
                        ))}
                    </div>
                </div>

                {/* 탭 메뉴 */}
                <div className={styles.tabMenu}>
                    <button
                        className={`${styles.tabButton} ${activeTab === 'participants' ? styles.active : ''}`}
                        onClick={() => setActiveTab('participants')}
                    >
                        참여자 목록 ({participants.approved.length}/{meeting.maxParticipants})
                    </button>
                    <button
                        className={`${styles.tabButton} ${activeTab === 'comments' ? styles.active : ''}`}
                        onClick={() => setActiveTab('comments')}
                    >
                        댓글 ({comments.length})
                    </button>
                </div>

                {/* 탭 콘텐츠 */}
                <div className={styles.tabContent}>
                    {activeTab === 'participants' && (
                        <div className={styles.participantsTab}>
                            <h4>확정된 참여자</h4>
                            <div className={styles.participantsList}>
                                {participants.approved.map((participant, index) => (
                                    <div key={index} className={styles.participantItem}>
                                        <div className={styles.participantAvatar}>
                                            {participant.profileImageUrl ? (
                                                <img src={participant.profileImageUrl} alt={participant.nickname} />
                                            ) : (
                                                <div className={styles.defaultAvatar}>
                                                    {participant.nickname.charAt(0)}
                                                </div>
                                            )}
                                        </div>
                                        <div className={styles.participantInfo}>
                                            <div className={styles.participantName}>
                                                <span>{participant.nickname}</span>
                                                {participant.participantType === 'HOST' && (
                                                    <span className={styles.hostLabel}>호스트</span>
                                                )}
                                            </div>
                                            <div className={styles.participantStats}>
                                                신뢰도: {participant.TrusterScore}점
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            {isHost() && participants.pending.length > 0 && (
                                <>
                                    <h4>신청 대기자</h4>
                                    <div className={styles.participantsList}>
                                        {participants.pending.map((participant, index) => (
                                            <div key={index} className={styles.participantItem}>
                                                <div className={styles.participantAvatar}>
                                                    {participant.profileImageUrl ? (
                                                        <img src={participant.profileImageUrl} alt={participant.nickname} />
                                                    ) : (
                                                        <div className={styles.defaultAvatar}>
                                                            {participant.nickname.charAt(0)}
                                                        </div>
                                                    )}
                                                </div>
                                                <div className={styles.participantInfo}>
                                                    <div className={styles.participantName}>
                                                        <span>{participant.nickname}</span>
                                                        <span className={styles.pendingLabel}>대기중</span>
                                                    </div>
                                                    <div className={styles.participantStats}>
                                                        신뢰도: {participant.TrusterScore}점
                                                    </div>
                                                </div>
                                                <div className={styles.participantActions}>
                                                    <button className={styles.approveButton}>승인</button>
                                                    <button className={styles.rejectButton}>거절</button>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </>
                            )}
                        </div>
                    )}

                    {activeTab === 'comments' && (
                        <div className={styles.commentsTab}>
                            {/*<div className={styles.commentsList}>
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
                                                { user.userId === comment.userId && (
                                                    <button
                                                        className={styles.commentMoreButton}
                                                        onClick={(e) => handleOpenModal(comment, e)} // 모달 열기 함수 호출
                                                    >
                                                        ...
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>*/}
                            <CommentList
                                comments={comments}
                                user={user}
                                handleOpenModal={handleOpenModal}
                            />

                            {(isHost() || isParticipating()) && (
                                /*<form onSubmit={handleCommentSubmit} className={styles.commentForm}>
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
                                </form>*/

                                <CommentForm
                                    newComment={newComment} // 작성 로직을 위한 상태
                                    setNewComment={setNewComment} // 작성 로직을 위한 핸들러
                                    handleCommentSubmit={handleCommentSubmit}
                                    isSubmittingComment={isSubmittingComment}
                                    isEditing={isEditing}
                                    editedCommentContent={editedCommentContent} // 수정된 내용 상태 전달
                                    setEditedCommentContent={setEditedCommentContent} // 수정된 내용 핸들러 전달
                                    onCancelEdit={handleCancelEdit}
                                />
                            )}
                        </div>
                    )}
                </div>

                {/* 액션 버튼 */}
                <div className={styles.actionButtons}>
                    {!isHost() && !isParticipating() && meeting.status === 'RECRUITING' && (
                        <button
                            onClick={handleJoinMeeting}
                            disabled={isJoining}
                            className={styles.joinButton}
                        >
                            {isJoining ? '참여 신청 중...' : '참여 신청하기'}
                        </button>
                    )}

                    {!isHost() && isParticipating() && (
                        <button
                            onClick={handleLeaveMeeting}
                            className={styles.leaveButton}
                        >
                            모임 탈퇴하기
                        </button>
                    )}

                    <button
                        onClick={() => navigate('/')}
                        className={styles.backButton}
                    >
                        목록으로
                    </button>
                </div>
            </div>

            <Chatbot />
            {isModalOpen && (
                <CommentModal
                    isOpen={isModalOpen}
                    onClose={handleCloseModal}
                    comment={selectedComment} // 선택된 댓글 정보 그대로 전달
                    modalPosition={modalPosition}
                    onUpdate={startEditing} // `startEditing` 함수를 props로 전달
                    // onDelete={handleCommentDelete} // 삭제 핸들러 전달
                />
            )}
        </div>
    );
};

export default MeetingDetailPage;