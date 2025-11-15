import React from 'react';
import styles from './MeetingCardSkeleton.module.scss';

/**
 * 모임 카드의 로딩 상태를 표시하는 스켈레톤 컴포넌트입니다.
 * 실제 데이터가 로드되기 전에 표시되는 플레이스홀더 역할을 합니다.
 *
 * @returns {Element} 스켈레톤 UI를 표시하는 React 엘리먼트
 *
 * @author 고동현
 * @since 2025.09.17
 */
const MeetingCardSkeleton = () => {
    return (
        <div className={styles.card}>
            {/* 모임 대표 이미지가 표시될 썸네일 영역 */}
            <div className={styles.thumbnail}/>

            {/* 모임 정보가 표시되는 주요 콘텐츠 영역 */}
            <div className={styles.content}>
                {/* 모임 제목이 표시될 영역 */}
                <div className={`${styles.line} ${styles.title}`}/>

                {/* 모임 장소와 일시 정보가 표시될 영역 */}
                <div className={styles.info}>
                    <div className={`${styles.line} ${styles.infoLine}`}/>
                    <div className={`${styles.line} ${styles.infoLine}`} style={{width: '70%'}}/>
                </div>

                {/* 참여자 정보가 표시될 하단 영역 */}
                <div className={styles.footer}>
                    <div className={`${styles.line} ${styles.participants}`} />
                </div>
            </div>
        </div>
    );
};

export default MeetingCardSkeleton;