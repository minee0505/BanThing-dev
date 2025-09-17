import React, {useEffect, useRef, useState} from 'react';

const LoginPage = () => {

  // 로딩중인지 확인할 변수
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  // useRef로 인터벌 ID를 저장합니다.
  const intervalRef = useRef(null);
  // 카카오 로그인 백엔드 엔드포인트
  const KAKAO_AUTH_URL = 'http://localhost:9000/oauth2/authorization/kakao';

  // 컴포넌트가 언마운트될 때 인터벌을 정리합니다.
  useEffect(() => {
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, []);

  const handleKakaoLogin = () => {

    // 팝업 창이 열리기 전에 로딩 상태를 true로 설정
    setIsLoading(true);
    setError('');

    // 팝업창 열기
    const popup = window.open(KAKAO_AUTH_URL, 'KakaoLoginPopup', 'width=400,height=600,left=100,top=100');

    // 팝업이 열리지 않았을 경우 (팝업 차단 등)
    if (!popup || popup.closed || typeof popup.closed === 'undefined') {
      setIsLoading(false);
      setError('팝업 차단을 해제하고 다시 시도해 주세요.');
      return;
    }

    // 기존 인터벌이 있다면 정리합니다.
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
    }

    // 팝업 창이 강제로 닫혔는지 확인하는 로직
    intervalRef.current = setInterval(() => {
      // 팝업이 닫혔을 때
      if (popup.closed) {
        setIsLoading(false);
        setError('로그인 창이 닫혔습니다. 다시 시도해 주세요.');
        clearInterval(intervalRef.current); // 현재 인터벌도 정리합니다.
      }
    }, 500);

  };


  return (
    <>
      <p>로그인 페이지입니다.</p>
      {error && <p>{error}</p> }
      {isLoading ? (
        <p>로그인 진행 중입니다...</p>
      ) : (
      <button onClick={handleKakaoLogin}>Kakao로 로그인</button>
      )}
    </>
  );
};

export default LoginPage;