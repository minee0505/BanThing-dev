import React from 'react';
import styles from './MeetingCardSkeleton.module.scss';

const MeetingCardSkeleton = () => {
    return (
        <div className={styles.card}>
            {/* 1. 썸네일 영역 */}
            <div className={styles.thumbnail} />

            {/* 2. 콘텐츠 영역 */}
            <div className={styles.content}>
                {/* 제목 */}
                <div className={`${styles.line} ${styles.title}`} />

                {/* 정보 (위치, 날짜) */}
                <div className={styles.info}>
                    <div className={`${styles.line} ${styles.infoLine}`} />
                    <div className={`${styles.line} ${styles.infoLine}`} style={{ width: '70%' }} />
                </div>

                {/* 하단 (참여자) */}
                <div className={styles.footer}>
                    <div className={`${styles.line} ${styles.participants}`} />
                </div>
            </div>
        </div>
    );
};

export default MeetingCardSkeleton;