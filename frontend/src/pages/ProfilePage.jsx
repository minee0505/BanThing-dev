import React, {useEffect} from 'react';
import MyInfo from '../components/Profile/MyInfo.jsx';
import MyParticipatingGroups from '../components/Profile/MyParticipatingGroups.jsx';
import MyPendingGroups from '../components/Profile/MyPendingGroups.jsx';
import {useAuthStore} from '../stores/authStore.js';
import {useNavigate} from 'react-router-dom';

const ProfilePage = () => {

  const { isAuthenticated, user } = useAuthStore();
  const navigate = useNavigate();

  // 로그인 버튼 활성화 여부 검증을 위한 useEffect
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {})

  return (
    <>
      <div>
        <h1>프로필 페이지</h1>
        <MyInfo user={user}/>
        <MyParticipatingGroups />
        <MyPendingGroups />
      </div>
    </>
  );
};

export default ProfilePage;