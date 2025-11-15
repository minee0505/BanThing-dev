import React from 'react';
import { FaEdit, FaTrash } from 'react-icons/fa';
import styles from '../../pages/MeetingDetailPage.module.scss';
import TrustBadge from "./TrustBadge.jsx";

const HostInfo = ({ host, isHost, onDeleteMeeting }) => {
    return (
        <div className={styles.hostInfo}>
            <div className={styles.hostAvatar}>
                {host.profileImageUrl ? (
                    <img src={host.profileImageUrl} alt="호스트" />
                ) : (
                    <div className={styles.defaultAvatar}>
                        {host.nickname.charAt(0)}
                    </div>
                )}
            </div>
            <div className={styles.hostDetails}>
                <div className={styles.hostName}>
                    <span>{host.nickname} (호스트)</span>
                    <TrustBadge trustScore={host.trustScore} /> {/*TrustBadge 컴포넌트로 교체*/}
                </div>
                <div className={styles.hostStats}>신뢰도 점수: {host.trustScore}점</div>
            </div>
            {isHost && (
                <div className={styles.hostActions}>
                    <button className={styles.editButton}>
                        <FaEdit /> 수정
                    </button>
                    <button
                        className={styles.deleteButton}
                        onClick={onDeleteMeeting}
                    >
                        <FaTrash /> 삭제
                    </button>
                </div>
            )}
        </div>
    );
};

export default HostInfo;