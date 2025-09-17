import React from 'react';
import styles from './MeetingCard.module.scss';
import { FaMapMarkerAlt, FaUsers, FaCalendarAlt } from 'react-icons/fa';

const MeetingCard = ({ meeting }) => {
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
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

    const getFullImageUrl = (thumbnailUrl) => {
        // 1. 기본 이미지 URL 설정
        const placeholder = 'https://via.placeholder.com/150';
        if (!thumbnailUrl) {
            return placeholder;
        }

        // 2. 이미 완전한 URL(http로 시작)이면 그대로 반환
        if (thumbnailUrl.startsWith('http')) {
            return thumbnailUrl;
        }

        // 3. .env 파일의 VITE_API_URL ('http://localhost:9000/api')에서
        //    뒤의 '/api' 부분을 제거하여 백엔드 기본 주소('http://localhost:9000')를 만듭니다.
        const backendUrl = import.meta.env.VITE_API_URL.replace('/api', '');

        // 4. 백엔드 기본 주소와 DB에 저장된 이미지 경로를 합쳐 완전한 URL을 반환합니다.
        return `${backendUrl}${thumbnailUrl}`;
    };

    return (
        <div className={styles.card}>
            <span className={`${styles.badge} ${getStatusClass(meeting.status)}`}>
                {statusToKorean[meeting.status] || meeting.status}
            </span>

            <div className={styles.thumbnail}>
                {/* [수정] 위에서 만든 함수를 사용하여 최종 이미지 URL을 가져옵니다. */}
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