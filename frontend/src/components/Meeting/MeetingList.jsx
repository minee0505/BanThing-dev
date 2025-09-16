import React from 'react';
import MeetingCard from "./MeetingCard.jsx";
import MeetingCardSkeleton from "./MeetingCardSkeleton.jsx";


const MeetingList = ({ meetings, isLoading }) => {
    // 로딩 중일 때 스켈레톤 UI 렌더링
    if (isLoading) {
        return (
            <div>
                {/* 스켈레톤 3개 랜더링 */}
                {[...Array(3)].map((_, index) => (
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