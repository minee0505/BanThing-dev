import React from 'react';
import MeetingCard from "./MeetingCard.jsx";
import MeetingCardSkeleton from "./MeetingCardSkeleton.jsx";
import styles from "../../pages/MeetingListPage.module.scss";
import { FaRegSadTear } from 'react-icons/fa';


/**
 * 모임 목록을 표시하는 컴포넌트입니다.
 * @param {Array} meetings - 표시할 모임 목록 데이터
 * @param {boolean} isLoading - 데이터 로딩 상태를 나타내는 플래그
 * @param {number} skeletonCount - 로딩 중 표시할 스켈레톤 UI의 개수 (기본값: 3)
 * @returns {Element} 모임 목록 또는 스켈레톤 UI를 포함하는 React 엘리먼트
 *
 * @author 고동현
 * @since 2025.09.19
 */
const MeetingList = ({ meetings, isLoading, skeletonCount = 3 }) => {
    if (isLoading) {
        return (
            <div>
                {/* 데이터 로딩 중일 때 지정된 개수만큼 스켈레톤 UI를 표시합니다 */}
                {[...Array(skeletonCount)].map((_, index) => (
                    <MeetingCardSkeleton key={index} />
                ))}
            </div>
        );
    }

    return (
        <div>
            {/* 모임 데이터가 있으면 목록을 표시하고, 없으면 '결과 없음' 메시지를 표시합니다 */}
            {meetings.length > 0 ? (
                meetings.map(meeting => (
                    <MeetingCard key={meeting.meetingId} meeting={meeting} />
                ))
            ) : (
                <div className={styles['no-results']}>
                    <FaRegSadTear size="3rem" className={styles['no-results__icon']} />
                    <p className={styles['no-results__text']}>
                        앗, 찾으시는 모임이 없네요.<br />
                        새로운 모임을 만들어보는 건 어떠세요?
                    </p>
                </div>
            )}
        </div>
    );
};

export default MeetingList;