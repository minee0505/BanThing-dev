import React, {useEffect, useState} from 'react';
import {useAuthStore} from "../stores/authStore.js";
import {useNavigate} from "react-router-dom";
import {AuthService} from "../services/authService.js";

const AgreementPage = () => {
  const { user, isAuthenticated, logout, refreshMeSilent } = useAuthStore();
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState('');

  // 약관 동의 상태를 검사
  useEffect(() => {
    // 1. 사용자가 로그인 상태가 아니거나
    // 2. 약관에 동의했다면 메인으로 이동
    if (!isAuthenticated || user.agree) {
      navigate('/');
    }
  }, [user, isAuthenticated, navigate]);

  // 로그아웃 핸들러 함수
  const handleLogout = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  // 약관 동의 핸들러 함수
  const handleAgree = async () => {
    try {
      const res = await AuthService.updateAgreement();
      if (res.data.success) {
        // 동의가 성공하면 유저 정보 갱신
        await refreshMeSilent();
        // 메인 페이지로 리디렉션
        navigate('/', { replace: true });
      }
    } catch (error) {
      console.error("약관 동의 실패:", error);
      setErrorMessage('서버와 통신 중 오류가 발생했습니다.');
    }
  };

  return (
    <>
      <p>약관동의 페이지입니다.</p>
      <button onClick={handleAgree}>동의</button>
      <button onClick={handleLogout}>로그아웃</button>
      { errorMessage && <p>{errorMessage}</p> }
    </>
  );
};

export default AgreementPage;