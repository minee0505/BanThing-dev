import React from 'react';
import styles from './MeetingCardSkeleton.module.scss';

const MeetingCardSkeleton = () => {
    return (
        <div className={styles.card}>
            <div className={styles.thumbnail} />
            <div className={styles.content}>
                <div className={`${styles.line} ${styles.title}`} />
                <div className={`${styles.line} ${styles.info}`} />
                <div className={`${styles.line} ${styles.info}`} style={{ width: '60%' }} />
                <div className={styles.footer}>
                    <div className={`${styles.line} ${styles.participants}`} />
                </div>
            </div>
        </div>
    );
};

export default MeetingCardSkeleton;