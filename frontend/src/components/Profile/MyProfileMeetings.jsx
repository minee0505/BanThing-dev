import React from 'react';
import MeetingCard from '../Meeting/MeetingCard.jsx';
import {FaRegSadTear} from 'react-icons/fa';
import styles from './MyProfileMeetings.module.scss';

const MyProfileMeetings = ({ meetingList, condition }) => {

  return (
    <>
      {
        meetingList?.length > 0 ? (
          // 모임이 있는 경우
          meetingList?.map(meeting => (
            <MeetingCard key={meeting.meetingId} meeting={meeting} />
          ))
        ) : (
          // 모임이 없을 경우
          <div className={`${styles.emptyState} ${styles.slideUp}`}>
            <FaRegSadTear className={styles.emptyIcon} />
            <div className={styles.emptyMessage}>
              {
                condition === 'APPROVED' ? ( // 참가중인 모임을 클릭한 경우
                  <>
                    <p>앗, 참가중인 모임이 없네요.</p>
                    <p>새로운 모임에 참가해보는 건 어떠세요?</p>
                  </>
                ) : ( // 참가 대기중인 모임을 클릭한 경우
                  <>
                    <p>앗, 참가 대기중인 모임이 없네요.</p>
                    <p>새로운 모임에 참가해보는 건 어떠세요?</p>
                  </>
                )
              }
            </div>
          </div>
        )
      }
    </>
  );
};

export default MyProfileMeetings;