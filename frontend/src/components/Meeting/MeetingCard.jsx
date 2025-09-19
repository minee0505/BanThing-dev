import React from 'react';
// [추가] dev 브랜치의 useNavigate 기능을 가져옵니다.
import { useNavigate } from 'react-router-dom';
import styles from './MeetingCard.module.scss';
import { FaMapMarkerAlt, FaUsers, FaCalendarAlt } from 'react-icons/fa';

const MeetingCard = ({ meeting }) => {
    // [추가] dev 브랜치의 상세 페이지 이동 기능을 위한 navigate 함수입니다.
    const navigate = useNavigate();

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };

    // [추가] dev 브랜치의 카드 클릭 핸들러 함수입니다.
    const handleCardClick = () => {
        navigate(`/meetings/${meeting.meetingId || meeting.meeting_id || meeting.id}`);
    };

    const getStatusClass = (status) => {
        if (!status) return '';
        return styles[status.toLowerCase()] || '';
    };

    const statusToKorean = {
        RECRUITING: '모집중',
        FULL: '모집완료',
        ONGOING: '진행중',
        COMPLETED: '모임종료',
        CANCELLED: '모임취소',
    };

    // [추가] feature 브랜치의 이미지 전체 URL을 만들어주는 함수입니다.
    const getFullImageUrl = (thumbnailUrl) => {
        const placeholder = 'https://via.placeholder.com/150';
        if (!thumbnailUrl) {
            return placeholder;
        }
        if (thumbnailUrl.startsWith('http')) {
            return thumbnailUrl;
        }
        const backendUrl = import.meta.env.VITE_API_URL.replace('/api', '');
        return `${backendUrl}${thumbnailUrl}`;
    };

    return (
        // [수정] dev 브랜치의 onClick, style 속성을 추가하여 카드 전체를 클릭할 수 있게 합니다.
        <div
            className={styles.card}
            onClick={handleCardClick}
            style={{ cursor: 'pointer' }}
        >
            <span className={`${styles.badge} ${getStatusClass(meeting.status)}`}>
                {statusToKorean[meeting.status] || meeting.status}
            </span>

            <div className={styles.thumbnail}>
                {/* [수정] feature 브랜치의 getFullImageUrl 함수를 사용하여 최종 이미지 URL을 가져옵니다. */}
                <img src={getFullImageUrl(meeting.thumbnailImageUrl)} alt={meeting.title} />
            </div>

            <div className={styles.content}>
                <h3 className={styles.title}>{meeting.title}</h3>
                <div className={styles.info}>
                    <span><FaMapMarkerAlt /> {meeting.martName}</span>
                    <span><FaCalendarAlt /> {formatDate(meeting.meetingDate)}</span>
                </div>
                <div className={styles.footer}>
                    <span className={styles.participants}>
                        <FaUsers /> {meeting.currentParticipants} / {meeting.maxParticipants}명
                    </span>
                </div>
            </div>
        </div>
    );
};

export default MeetingCard;