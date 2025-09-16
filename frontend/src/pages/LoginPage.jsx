import React from 'react';

const LoginPage = () => {

  // 카카오 로그인 백엔드 엔드포인트
  const KAKAO_AUTH_URL = 'http://localhost:9000/oauth2/authorization/kakao';

  const handleKakaoLogin = () => {

    // 팝업창 열기
    window.open(KAKAO_AUTH_URL, 'KakaoLoginPopup', 'width=400,height=600,left=100,top=100');
  };

  return (
    <>
      <p>로그인 페이지입니다.</p>
      <button onClick={handleKakaoLogin}>Kakao로 로그인</button>
    </>
  );
};

export default LoginPage;