import React from 'react';
import Logo from '../Others/Logo.jsx';
import {Link, useNavigate} from 'react-router-dom';
import {useAuthStore} from "../../stores/authStore.js";
import styles from './AppHeader.module.scss';
import { RiLogoutBoxLine, RiLoginBoxLine } from "react-icons/ri";

const AppHeader = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuthStore();
  const nickname = user?.nickname || '사용자';
  const profileImageUrl = user?.profileImageUrl || '/images/defaultProfile.png';

  const handleLogout = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  const handleLogin = () => {
    navigate('/login');
  };

  const handleProfile = () => {
    navigate('/profile');
  };

  return (
    <header className={`${styles.header} ${styles.container}`}>
      <Logo />
      {isAuthenticated ? (
        <div onClick={handleProfile} className={styles.userSection}>
          <div className={styles.profileSection}>
            <img
              src={profileImageUrl}
              alt="프로필 이미지"
              className={styles.profileImage}
            />
            <p className={styles.nickname}>{nickname}</p>
          </div>
          <RiLogoutBoxLine
            onClick={handleLogout}
            className={styles.actionButton}
          />
        </div>
      ) : (
        <RiLoginBoxLine
          onClick={handleLogin}
          className={styles.actionButton}
        />
      )
      }
    </header>
  );
};

export default AppHeader;