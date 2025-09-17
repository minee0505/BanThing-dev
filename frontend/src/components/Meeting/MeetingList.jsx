import React from 'react';
import MeetingCard from "./MeetingCard.jsx";
import MeetingCardSkeleton from "./MeetingCardSkeleton.jsx";
import styles from "../../pages/MeetingListPage.module.scss";
import { FaRegSadTear } from 'react-icons/fa';

// ✅ 1. skeletonCount prop을 받고, 기본값을 3으로 설정합니다.
const MeetingList = ({ meetings, isLoading, skeletonCount = 3 }) => {
    if (isLoading) {
        return (
            <div>
                {/* ✅ 2. skeletonCount만큼 스켈레톤 UI를 반복 렌더링합니다. */}
                {[...Array(skeletonCount)].map((_, index) => (
                    <MeetingCardSkeleton key={index} />
                ))}
            </div>
        );
    }

    return (
        <div>
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