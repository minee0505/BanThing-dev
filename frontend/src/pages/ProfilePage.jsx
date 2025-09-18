import React from 'react';
import MyInfo from '../components/Profile/MyInfo.jsx';
import MyParticipatingGroups from '../components/Profile/MyParticipatingGroups.jsx';
import MyPendingGroups from '../components/Profile/MyPendingGroups.jsx';

const ProfilePage = () => {
  return (
    <>
      <div>
        <h1>프로필 페이지</h1>
        <MyInfo />
        <MyParticipatingGroups />
        <MyPendingGroups />
      </div>
    </>
  );
};

export default ProfilePage;