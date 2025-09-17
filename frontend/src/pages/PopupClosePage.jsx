import React, { useEffect } from "react";
import {Link, useNavigate, useSearchParams} from 'react-router-dom';

const PopupClosePage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const errorMessage = searchParams.get('error');

  // 팝업 창의 상태(성공 또는 실패)를 확인하고 부모 창을 처리하는 로직
  useEffect(() => {
    if (!errorMessage) { // 에러가 없을때 = 로그인 성공
      if (window.opener) {
        // 부모 창을 메인 페이지로 이동시키고
        window.opener.location.href = "http://localhost:5173/";
        // 현재 팝업 닫기
        window.close();
      } else {
        // 팝업이 아닐 경우 그냥 현재창에서 이동
        navigate('/');
      }
    } else { // 에러가 있을때 = 로그인 실패
      if (window.opener) {
        // 부모 창을 현재 팝업 창의 URL로 이동
        window.opener.location.href = window.location.href;
        window.close();
      } else {
        // 팝업이 아닐 경우 그대로 있음
      }
    }
  }, [errorMessage]);

  return (
    <>
      { errorMessage ? (
        <>
          <p>오류가 발생했습니다.</p>
          <Link to='/login'>로그인 페이지로 이동</Link>
        </>
      ) : (
        <p>로그인 처리중입니다... 잠시만 기다려주세요</p>
      )}
    </>
  );
};

export default PopupClosePage;