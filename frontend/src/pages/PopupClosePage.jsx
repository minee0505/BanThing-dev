import React, { useEffect } from "react";
import {Link, useSearchParams} from 'react-router-dom';

const PopupClosePage = () => {
  const [searchParams] = useSearchParams();
  const errorMessage = searchParams.get('error');

  // 성공 시 팝업을 바로 닫는 로직
  // 에러 메시지가 없다면 (즉, 성공 시) 이 useEffect가 실행됩니다.
  useEffect(() => {
    if (!errorMessage) {
      if (window.opener) {
        // 부모 창을 메인 페이지로 이동시키고
        window.opener.location.href = "http://localhost:5173/";
        // 현재 팝업 닫기
        window.close();
      } else {
        // 팝업이 아닐 경우 그냥 현재창에서 이동
        window.location.href = "http://localhost:5173/";
      }
    }
  }, [errorMessage]);


  return (
    <>
      { errorMessage && (
        <>
          <p>오류가 발생했습니다.</p>
          <Link to='/'>홈으로</Link>
        </>
      )}
      { errorMessage === true ? <p>로그인 처리중입니다... 잠시만 기다려주세요</p> : null }
    </>
  );
};

export default PopupClosePage;