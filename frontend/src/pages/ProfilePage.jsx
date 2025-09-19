import React, {useEffect, useState} from 'react';
import MyInfo from '../components/Profile/MyInfo.jsx';
import MyProfileMeetings from '../components/Profile/MyProfileMeetings.jsx';
import {useAuthStore} from '../stores/authStore.js';
import {useNavigate} from 'react-router-dom';
import {profileMeetings} from '../services/profileApi.js';

const ProfilePage = () => {

  const { isAuthenticated, user } = useAuthStore();
  const [condition, setCondition] = useState('APPROVED');
  const [page, setPage] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [meetingList, setMeetingList] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  // 로그인 활성화 여부 검증을 위한 useEffect
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  // async 함수를 만들어서 await를 쓸 수 있게 합니다.
  const fetchMeetings = async () => {
    setIsLoading(true); // 로딩 상태 시작

    try {
      // await를 사용하면 프로미스가 완료될 때까지 기다려줍니다.
      const result = await profileMeetings(page, condition);

      // await 덕분에 원하는 { success, data, ... } 객체가 담겨있음
      if (result.success) {
        setMeetingList(result.data);
      } else {
        console.error('실패:', result.message);
        setErrorMessage(result.message);
      }
    } catch (error) {
      console.error('API 호출 중 에러 발생:', error);
      setErrorMessage('서버와 통신 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false); // 로딩 상태 끝
    }
  };

  // 모임 목록을 불러오는 useEffect (최초, 조건 변경, 페이지 변경)
  useEffect(() => {
    fetchMeetings(); // 만든 함수를 호출합니다.
  }, [condition, page]);

  return (
    <>
      <div>
        <h1>프로필 페이지</h1>
        <MyInfo user={user}/>
        <button onClick={() => {setCondition('APPROVED')}}>참가중인 모임</button>
        <button onClick={() => {setCondition('PENDING')}}>참가 대기중인 모임</button>
        <MyProfileMeetings meetingList={meetingList} condition={condition} />
      </div>
    </>
  );
};

export default ProfilePage;