import React from 'react';

const MyInfoCard = ({ user }) => {
  
  const nickname = user?.nickname || '사용자';
  const profileImageUrl = user?.profileImageUrl || '/images/defaultProfile.png';
  // const selfIntroduction = user?.selfIntroduction || '없음';
  const trustGrade = user?.trustGrade;
  const trustScore = user?.trustScore;
  const noShowCount = user?.noShowCount;

  return (
    <section>
      <h2>프로필</h2>
      <ul>
        <li>
          <strong>이름:</strong> {nickname}
        </li>
        <li>
          <div>
            <img
              src={profileImageUrl}
              alt="프로필 이미지"
              style={{ width: '40px', height: '40px' }}
            />
          </div>
        </li>
        <li>
          <strong>등급:</strong> {trustGrade}
        </li>
        <li>
          <strong>점수:</strong> {trustScore}
        </li>
        <li>
          <strong>노쇼 카운트:</strong> {noShowCount}
        </li>
      </ul>
    </section>
  );
};

export default MyInfoCard;