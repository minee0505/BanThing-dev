import React from 'react';
import Logo from '../Others/Logo.jsx';
import {Link, useNavigate} from 'react-router-dom';
import {useAuthStore} from "../../stores/authStore.js";

const AppHeader = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuthStore();
  const nickname = user?.nickname || '사용자';

  const handleLogout = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  return (
    <>
      <Logo />
      {isAuthenticated ? (
        <>
          <p>{nickname}</p>
          <button type='button' onClick={handleLogout}>로그아웃</button>
        </>
      ) : (
        <Link to='/login'>로그인</Link>
      )
      }
    </>
  );
};

export default AppHeader;