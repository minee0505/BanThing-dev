import {useEffect} from 'react';
import {useAuthStore} from "../../stores/authStore.js";

// 앱 최초 진입 또는 새로고침 시 사용자 인증 상태를 확인하고 불러오는 컴포넌트
const LoadMeRoute = ({ children }) => {
  const { hasCheckedAuth, fetchMe } = useAuthStore();

  // 최초 진입 시 인증 체크 1회
  useEffect(() => {
    if (!hasCheckedAuth) {
      fetchMe();
    }
  }, [hasCheckedAuth, fetchMe]);

  return children;
};

export default LoadMeRoute;