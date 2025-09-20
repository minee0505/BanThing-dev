import React from 'react';
import MeetingCard from '../Meeting/MeetingCard.jsx';
import {FaRegSadTear} from 'react-icons/fa';

const MyProfileMeetings = ({ meetingList, condition }) => {

  return (
    <section>
      {
        condition === 'APPROVED' ? (
          <h2>내가 참여한 모임</h2>
        ) : (
          <h2>참가 대기 중인 모임</h2>
        )
      }
      {
        meetingList?.length > 0 ? (
          // 모임이 있는 경우
          meetingList?.map(meeting => (
            <MeetingCard key={meeting.meetingId} meeting={meeting} />
          ))
        ) : (
          // 모임이 없을 경우
          <div>
            <FaRegSadTear size="3rem" />
            {
              condition === 'APPROVED' ? ( // 참가중인 모임을 클릭한 경우
                <p>
                  앗, 참가중인 모임이 없네요.<br />
                  새로운 모임에 참가해보는 건 어떠세요?
                </p>
              ) : ( // 참가 대기중인 모임을 클릭한 경우
                <p>
                  앗, 참가 대기중인 모임이 없네요.<br />
                  새로운 모임에 참가해보는 건 어떠세요?
                </p>
              )
            }
          </div>
        )
      }
    </section>
  );
};

export default MyProfileMeetings;