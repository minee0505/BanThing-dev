import React, {useEffect, useState} from 'react';
import MyInfoCard from '../components/Profile/MyInfoCard.jsx';
import MyProfileMeetings from '../components/Profile/MyProfileMeetings.jsx';
import {useAuthStore} from '../stores/authStore.js';
import {useLoaderData, useNavigate} from 'react-router-dom';
import {profileMeetings} from '../services/profileApi.js';
import Pagination from "../components/Meeting/Pagination.jsx";
import MeetingCardSkeleton from "../components/Meeting/MeetingCardSkeleton.jsx";

const ProfilePage = () => {

  const initialSkeletonCount = useLoaderData(); // 로더에서 전달된 첫 페이지 개수
  const { isAuthenticated, user } = useAuthStore();

  const [condition, setCondition] = useState('APPROVED');
  const [page, setPage] = useState(0); // 0번 인덱스 기반
  const [totalPages, setTotalPages] = useState(0); // 전체 페이지 수를 위한 상태
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(false); // 모임 로딩 여부
  const [isProfileLoading, setIsProfileLoading] = useState(false); // 프로필 로딩 여부
  const [meetingList, setMeetingList] = useState([]);
  const [errorMessage, setErrorMessage] = useState('');

  const navigate = useNavigate();
  const meetingsPerPage = 4;

  // 로그인 활성화 여부 검증을 위한 useEffect
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
    setIsProfileLoading(true);
    setTimeout(() => setIsProfileLoading(false), 300);
  }, [isAuthenticated, navigate]);

  // async 함수를 만들어서 await를 쓸 수 있게 합니다.
  const fetchMeetings = async () => {
    setIsLoading(true); // 로딩 상태 시작
    setErrorMessage(''); // 에러 메시지 없애고 시작

    // 최소 로딩 시간 을 주기 위한 딜레이
    const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

    try {

      // await를 사용하면 프로미스가 완료될 때까지 기다려줍니다.
      const result = await profileMeetings(page, condition);

      // await 덕분에 원하는 { success, data, ... } 객체가 담겨있음
      if (result.success) {
        setMeetingList(result.data.content);
        setTotalElements(result.data.totalElements);
        setTotalPages(Math.ceil(result.data.totalElements / meetingsPerPage));
      } else {
        console.error('실패:', result.message);
        setErrorMessage(result.message);
        setMeetingList([]);
      }
    } catch (error) {
      console.error('API 호출 중 에러 발생:', error);
      setErrorMessage('서버와 통신 중 오류가 발생했습니다.');
      setMeetingList([]);
    } finally {
      // 선 fetch -> 후 딜레이
      await delay(300);
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

  // 현재 페이지에 들어올 개수 계산
  const expectedCount =
    page === 0 && condition === 'APPROVED'
      ? initialSkeletonCount // 첫 페이지는 로더에서 가져온 값 사용
      : Math.min(meetingsPerPage, Math.max(0, totalElements - page * meetingsPerPage));

  return (
    <>
      <div>
        <h2>프로필 페이지</h2>

        { isProfileLoading ? (
          <p>프로필 로딩중</p>
        ) : (
          <MyInfoCard user={user}/>
        )}


        {/* 버튼 클릭시 조건 변경, 페이지 0으로 재설정 */}
        <button
          onClick={() => { setCondition('APPROVED'); setPage(0); }}
          disabled={condition === 'APPROVED'}
        >
          참가중인 모임
        </button>
        <button
          onClick={() => {
            setCondition('PENDING');
            setPage(0);
            setTotalElements(0);
          }}
          disabled={condition === 'PENDING'}
        >
          참가 대기중인 모임
        </button>

        { isLoading ? ( // 로딩중인 경우
          <div>
            {Array.from({ length: expectedCount }).map((_, index) => (
              <MeetingCardSkeleton key={index} />
            ))}
          </div>
        ) : errorMessage ? ( // 로딩이 끝났지만 에러가 있는 경우
          <p>{errorMessage}</p>
        ) : ( // 로딩이 끝나고, 에러가 없는 경우 -> 미팅 렌더링
          <MyProfileMeetings meetingList={meetingList} condition={condition} />
        )}

        { meetingList && totalPages > 1 && !isLoading && (
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