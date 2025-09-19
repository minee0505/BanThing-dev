import React from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {useAuthStore} from "../../stores/authStore.js";
import styles from './AppHeader.module.scss';

const AppHeader = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuthStore();
  const nickname = user?.nickname || '사용자';
  const profileImageUrl = user?.profileImageUrl || '/images/defaultProfile.png';

  const handleLogout = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  return (
    <header className={`${styles.header} ${styles.container}`}>
      <Link to="/" className={styles.logoLink}>
        <div className={styles.logo}>
          <div className={styles.logoIcon}>
            <div className={styles.splitCircle}></div>
          </div>
          <div className={styles.logoText}>반띵</div>
        </div>
      </Link>
      {isAuthenticated ? (
        <div className={styles.userSection}>
          <img
            src={profileImageUrl}
            alt="프로필 이미지"
            className={styles.profileImage}
          />
          <p className={styles.nickname}>{nickname}</p>
          <button
            type='button'
            onClick={handleLogout}
            className={`${styles.logoutButton} ${styles.actionButton}`}
          >
            로그아웃
          </button>
        </div>
      ) : (
        <Link to='/login' className={styles.actionButton}>
          로그인
        </Link>
      )
      }
    </header>
  );
};

export default AppHeader;