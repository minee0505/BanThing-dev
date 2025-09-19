// MeetingDetailPage.jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import { getMeetingDetail, joinMeeting, leaveMeeting, getParticipants } from '../services/meetingDetailApi';
import { FaMapMarkerAlt, FaCalendarAlt, FaUsers, FaClock, FaEdit, FaTrash } from 'react-icons/fa';
import Chatbot from '../components/Chatbot/Chatbot';
import styles from './MeetingDetailPage.module.scss';
import ParticipantsTab from '../components/Meeting/ParticipantsTab';
import {approveParticipant, rejectParticipant } from '../services/participantApi';

const MeetingDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { isAuthenticated, user } = useAuthStore();

    const [meeting, setMeeting] = useState(null);
    const [participants, setParticipants] = useState({ approved: [], pending: [] });
    const [activeTab, setActiveTab] = useState('participants');
    const [isLoading, setIsLoading] = useState(true);
    const [isJoining, setIsJoining] = useState(false);
    const [error, setError] = useState(null);
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState('');

    useEffect(() => {
        // meeting state가 성공적으로 로드되거나 변경될 때마다 실행됩니다.
        if (meeting) {
            console.log('meeting 객체가 업데이트되었습니다:', meeting);
            console.log('그 안의 hostInfo 객체:', meeting.hostInfo);
        }
    }, [meeting]);

    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }

        fetchMeetingDetail();
        fetchParticipants();
    }, [id, isAuthenticated, navigate]);

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

    const handleCommentSubmit = (e) => {
        e.preventDefault();
        if (!newComment.trim()) return;

        // 임시 댓글 추가 (실제로는 API 호출)
        const comment = {
            id: Date.now(),
            author: user?.nickname || '나',
            content: newComment,
            createdAt: new Date().toISOString()
        };

        setComments(prev => [...prev, comment]);
        setNewComment('');
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

    //  참여 신청을 승인/거절하는 핸들러 함수를 추가
    const handleApprove = async (participant) => {
        if (!window.confirm(`${participant.nickname}님의 참여를 승인하시겠습니까?`)) return;
        try {
            const result = await approveParticipant(id, participant.participantId);
            if (result.success) {
                alert('참여를 승인했습니다.');

                // 서버에 다시 요청하는 대신, 현재 상태(state)를 직접 업데이트합니다.
                setParticipants(currentParticipants => {
                    // 1. 방금 승인된 사용자를 '대기중' 목록에서 제거합니다.
                    const newPending = currentParticipants.pending.filter(
                        p => p.participantId !== participant.participantId
                    );

                    // 2. '확정' 목록에 방금 승인된 사용자를 추가합니다.
                    const newApproved = [...currentParticipants.approved, participant];

                    // 3. 새로 만든 두 목록으로 상태를 업데이트하여 리렌더링을 발생시킵니다.
                    return { ...currentParticipants, approved: newApproved, pending: newPending };
                });

            } else {
                alert(result.message || '승인 처리에 실패했습니다.');
            }
        } catch (err) {
            console.error('참여 승인 실패:', err);
            alert('승인 처리 중 오류가 발생했습니다.');
        }
    };

    const handleReject = async (participant) => {
        if (!window.confirm(`${participant.nickname}님의 참여를 거절하시겠습니까?`)) return;
        try {
            const result = await rejectParticipant(id, participant.participantId);
            if (result.success) {
                alert('참여를 거절했습니다.');

                // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ 여기도 수정 ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
                // 거절의 경우, '대기중' 목록에서 제거하기만 하면 됩니다.
                setParticipants(currentParticipants => {
                    const newPending = currentParticipants.pending.filter(
                        p => p.participantId !== participant.participantId
                    );
                    return { ...currentParticipants, pending: newPending };
                });
                // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

            } else {
                alert(result.message || '거절 처리에 실패했습니다.');
            }
        } catch (err) {
            console.error('참여 거절 실패:', err);
            alert('거절 처리 중 오류가 발생했습니다.');
        }
    };


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
                                <button className={styles.deleteButton}>
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
                   {/* {activeTab === 'participants' && (
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
                    )}*/}

                        {/* 탭 콘텐츠 */}
                            {activeTab === 'participants' && (() => {

                                // 1. meeting 데이터나 hostInfo가 아직 로드되지 않았을 경우를 대비
                                if (!meeting || !meeting.hostInfo) {
                                    return null;
                                }

                                // 2. 호스트 정보를 참여자 객체와 동일한 형태
                                const hostAsParticipant = {
                                    ...meeting.hostInfo,
                                    // hostInfo에는 userId가 없으므로, 고유값인 nickname을 key로 사용하도록 전달합니다.
                                    userId: meeting.hostInfo.nickname,
                                    participantType: 'HOST',
                                    TrusterScore: meeting.hostInfo.TrusterScore || 450
                                };

                                // 3. 기존 확정 목록에서 혹시라도 중복될 수 있는 호스트를 제거하고,
                                //    새로 만든 호스트 객체를 배열의 맨 앞에 추가
                                const approvedWithHost = [
                                    hostAsParticipant,
                                    ...participants.approved.filter(p => p.nickname !== meeting.hostInfo.nickname)
                                ];

                                // 4. 새로 조합한 확정 참여자 목록을 props로 전달
                                return (
                                    <ParticipantsTab
                                        participants={{
                                            ...participants,
                                            approved: approvedWithHost // 수정된 배열을 전달
                                        }}
                                        isHost={isHost()}
                                        onApprove={handleApprove}
                                        onReject={handleReject}
                                        styles={styles}
                                    />
                                );
                            })()}



                            {activeTab === 'comments' && (
                        <div className={styles.commentsTab}>
                            <div className={styles.commentsList}>
                                {comments.length === 0 ? (
                                    <div className={styles.noComments}>
                                        <p>아직 댓글이 없습니다.</p>
                                        <p>첫 번째 댓글을 남겨보세요!</p>
                                    </div>
                                ) : (
                                    comments.map(comment => (
                                        <div key={comment.id} className={styles.commentItem}>
                                            <div className={styles.commentAvatar}>
                                                {comment.author.charAt(0)}
                                            </div>
                                            <div className={styles.commentContent}>
                                                <div className={styles.commentAuthor}>
                                                    {comment.author}
                                                    <span className={styles.commentTime}>
                                                        {new Date(comment.createdAt).toLocaleString()}
                                                    </span>
                                                </div>
                                                <div className={styles.commentText}>
                                                    {comment.content}
                                                </div>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>

                            {(isHost() || isParticipating()) && (
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
                                            disabled={!newComment.trim()}
                                            className={styles.commentSubmit}
                                        >
                                            등록
                                        </button>
                                    </div>
                                </form>
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
        </div>
    );
};

export default MeetingDetailPage;