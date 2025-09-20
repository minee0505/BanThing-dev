import React, {useEffect, useState} from 'react';
import MyInfo from '../components/Profile/MyInfo.jsx';
import MyProfileMeetings from '../components/Profile/MyProfileMeetings.jsx';
import {useAuthStore} from '../stores/authStore.js';
import {useNavigate} from 'react-router-dom';
import {profileMeetings} from '../services/profileApi.js';
import Pagination from "../components/Meeting/Pagination.jsx";

const ProfilePage = () => {

  const { isAuthenticated, user } = useAuthStore();
  const [condition, setCondition] = useState('APPROVED');
  const [page, setPage] = useState(0); // 0번 인덱스 기반
  const [totalPages, setTotalPages] = useState(0); // 전체 페이지 수를 위한 상태
  const [isLoading, setIsLoading] = useState(false);
  const [meetingList, setMeetingList] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();
  const meetingsPerPage = 4;

  // 로그인 활성화 여부 검증을 위한 useEffect
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  // async 함수를 만들어서 await를 쓸 수 있게 합니다.
  const fetchMeetings = async () => {
    setIsLoading(true); // 로딩 상태 시작
    setErrorMessage(''); // 에러 메시지 없애고 시작

    try {
      // await를 사용하면 프로미스가 완료될 때까지 기다려줍니다.
      const result = await profileMeetings(page, condition);

      // await 덕분에 원하는 { success, data, ... } 객체가 담겨있음
      if (result.success) {
        setMeetingList(result.data.content);
        setTotalPages(Math.ceil(result.data.totalElements / meetingsPerPage));
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

  // 페이지 변경을 위한 함수
  const paginate = (pageNumber) => {
    setPage(pageNumber - 1);
  }

  return (
    <>
      <div>
        <h2>프로필 페이지</h2>

        <MyInfo user={user}/>

        {/* 버튼 클릭시 조건 변경, 페이지 0으로 재설정 */}
        <button
          onClick={() => { setCondition('APPROVED'); setPage(0); }}
          disabled={condition === 'APPROVED'}
        >참가중인 모임</button>
        <button
          onClick={() => {setCondition('PENDING'); setPage(0);}}
          disabled={condition === 'PENDING'}
        >참가 대기중인 모임</button>

        <MyProfileMeetings meetingList={meetingList} condition={condition} />
        {meetingList && (
          <Pagination
            currentPage={page + 1}
            totalPages={totalPages}
            paginate={paginate}
          />
        )}
      </div>
    </>
  );
};

export default ProfilePage;