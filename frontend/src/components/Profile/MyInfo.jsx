import React from 'react';

const MyInfo = ({ user }) => {
  
  const nickname = user?.nickname || '사용자';
  const profileImageUrl = user?.profileImageUrl || '/images/defaultProfile.png';
  // const selfIntroduction = user?.selfIntroduction || '없음';
  const trustGrade = user?.trustGrade;
  const trustScore = user?.trustScore;
  const noShowCount = user?.noShowCount;

  return (
    <section>
      <h2>내 정보</h2>
      <p>이름: {nickname}</p>
      <p>프로필 이미지: </p>
      <img
        src={profileImageUrl}
        alt="프로필 이미지"
        style={{width: '30px', height: '30px'}}
      />
      <p>등급: {trustGrade}</p>
      <p>점수: {trustScore}</p>
      <p>노쇼 카운트: {noShowCount}</p>
    </section>
  );
};

export default MyInfo;