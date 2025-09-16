import React from 'react';
import MeetingCard from "./MeetingCard.jsx";
import MeetingCardSkeleton from "./MeetingCardSkeleton.jsx";

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
                <p>표시할 모임이 없습니다.</p>
            )}
        </div>
    );
};

export default MeetingList;