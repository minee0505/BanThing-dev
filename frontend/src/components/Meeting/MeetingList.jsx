
import React from 'react';
import MeetingCard from "./MeetingCard.jsx";

// ✅ props로 meetings 배열을 받도록 수정
const MeetingList = ({ meetings }) => {
    return (
        <div>
            {/* meetings 배열을 map으로 순회하며 MeetingCard를 렌더링 */}
            {meetings.length > 0 ? (
                meetings.map(meeting => (
                    <MeetingCard key={meeting.meetingId} meeting={meeting} />
                ))
            ) : (
                // 모임이 없을 경우 표시할 메시지
                <p>표시할 모임이 없습니다.</p>
            )}
        </div>
    );
};

export default MeetingList;