import React from 'react';
import { FaMapMarkerAlt, FaUsers, FaClock, FaStore } from 'react-icons/fa';
import { HiOutlineExternalLink } from 'react-icons/hi';
import { MdOutlineLogin } from 'react-icons/md';
import styles from './MeetingCard.module.scss';

const MeetingCard = ({ meeting, isAuthenticated, onCardClick, onLoginRequired }) => {
    const handleCardClick = () => {
        if (isAuthenticated) {
            onCardClick(meeting.meetingId);
        } else {
            onLoginRequired(meeting);
        }
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const month = date.getMonth() + 1;
        const day = date.getDate();
        const hours = date.getHours();
        const minutes = date.getMinutes();
        return `${month}/${day} ${hours}:${minutes.toString().padStart(2, '0')}`;
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'RECRUITING': return 'recruiting';
            case 'FULL': return 'full';
            case 'ONGOING': return 'ongoing';
            default: return 'recruiting';
        }
    };

    const getStatusText = (status) => {
        switch (status) {
            case 'RECRUITING': return '모집중';
            case 'FULL': return '마감';
            case 'ONGOING': return '진행중';
            default: return '모집중';
        }
    };

    return (
        <div className={styles.card} onClick={handleCardClick}>
            <div className={styles.header}>
                <div className={styles.titleArea}>
                    <h4 className={styles.title}>{meeting.title}</h4>
                    <span className={`${styles.status} ${styles[getStatusColor(meeting.status)]}`}>
                        {getStatusText(meeting.status)}
                    </span>
                </div>
                <div className={styles.suggestion}>
                    <span className={styles.suggestionText}>{meeting.suggestionReason}</span>
                </div>
            </div>

            <div className={styles.info}>
                <div className={styles.infoItem}>
                    <FaStore className={styles.icon} />
                    <span>{meeting.martName}</span>
                </div>

                <div className={styles.infoItem}>
                    <FaClock className={styles.icon} />
                    <span>{formatDate(meeting.meetingDate)}</span>
                </div>

                <div className={styles.infoItem}>
                    <FaUsers className={styles.icon} />
                    <span>{meeting.currentParticipants}/{meeting.maxParticipants}명</span>
                </div>

                <div className={styles.infoItem}>
                    <FaMapMarkerAlt className={styles.icon} />
                    <span className={styles.address}>{meeting.martAddress}</span>
                </div>
            </div>

            <div className={styles.footer}>
                <div className={styles.action}>
                    {isAuthenticated ? (
                        <div className={styles.viewDetail}>
                            <HiOutlineExternalLink className={styles.actionIcon} />
                            <span>자세히 보기</span>
                        </div>
                    ) : (
                        <div className={styles.loginRequired}>
                            <MdOutlineLogin className={styles.actionIcon} />
                            <span>로그인하여 자세히 보기</span>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default MeetingCard;