import React from 'react';
import styles from './MeetingCard.module.scss';
import {FaMapMarkerAlt, FaUsers, FaCalendarAlt} from 'react-icons/fa';

const MeetingCard = ({meeting}) => {
    // 날짜 형식을 'YYYY-MM-DD HH:mm'으로 예쁘게 바꿔주는 함수
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    };

    //  모임 상태(영문 대문자)를 CSS 클래스(영문 소문자)로 바꿔주는 함수
    const getStatusClass = (status) => {
        if (!status) return '';
        return styles[status.toLowerCase()] || '';
    };

    //  모임 상태에 따라 표시할 한글 텍스트를 정하는 객체
    const statusToKorean = {
        RECRUITING: '모집중',
        FULL: '모집완료',
        ONGOING: '진행중',
        COMPLETED: '모임종료',
        CANCELLED: '모임취소',
    };

    return (
        <div className={styles.card}>
            {/*  className에 동적으로 상태 클래스를 추가합니다. */}
            <span className={`${styles.badge} ${getStatusClass(meeting.status)}`}>
                    {statusToKorean[meeting.status] || meeting.status}
                </span>

            <div className={styles.thumbnail}>
                <img src={meeting.thumbnailImageUrl || 'https://via.placeholder.com/150'} alt={meeting.title}/>
                {/* 썸네일 안에 있던 배지는 삭제 */}
            </div>

            <div className={styles.content}>
                <h3 className={styles.title}>{meeting.title}</h3>
                <div className={styles.info}>
                    <span><FaMapMarkerAlt/> {meeting.martName}</span>
                    <span><FaCalendarAlt/> {formatDate(meeting.meetingDate)}</span>
                </div>
                <div className={styles.footer}>
                    <span className={styles.participants}>
                        <FaUsers/> {meeting.currentParticipants} / {meeting.maxParticipants}명
                    </span>
                </div>

            </div>
        </div>
    );
};

export default MeetingCard;