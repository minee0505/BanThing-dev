import React, {useEffect, useState} from 'react';
import {useAuthStore} from "../stores/authStore.js";
import {useNavigate} from "react-router-dom";
import {AuthService} from "../services/authService.js";
import styles from './AgreementPage.module.scss';

// 서비스 이용약관 컴포넌트
const ServiceTerms = () => (
  <div className={styles.termsContent}>
    <h3>반띵(BanThing) 서비스 이용약관</h3>
    <div className={styles.scrollableBox}>
      <h4>제1조 (목적)</h4>
      <p>본 약관은 반띵(이하 "회사")이 제공하는 대형마트 소분 모임 서비스(이하 "서비스")의 이용에 관하여 회사와 이용자 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.</p>

      <h4>제2조 (서비스의 성격 및 목적)</h4>
      <p>1. 본 서비스는 <strong>개인 소비 목적의 공동구매</strong>를 중개하는 플랫폼입니다.</p>
      <p>2. 모든 공동구매는 <strong>영리 목적이 아닌 비용 분담</strong>을 목적으로 합니다.</p>
      <p>3. 이용자는 구매한 상품을 <strong>본인이 직접 소비</strong>할 목적으로만 참여할 수 있습니다.</p>

      <h4>제3조 (이용자의 의무)</h4>
      <div className={styles.highlight}>
        <p><strong>기본 준수사항:</strong></p>
        <p>• 개인 소비 목적 준수: 구매한 상품은 본인의 개인적 소비 목적으로만 사용</p>
        <p>• 영리 행위 금지: 소분받은 상품을 재판매하거나 영리 목적으로 이용 금지</p>
        <p>• 현장 소분 원칙: 상품은 구매 장소에서 즉시 소분</p>
      </div>

      <div className={styles.highlight}>
        <p><strong>위생 수칙:</strong></p>
        <p>• 개인 용기 지참 및 위생장갑 착용 권장</p>
        <p>• 냉장/냉동 식품의 경우 적절한 보냉 용기 준비</p>
      </div>

      <h4>제4조 (금지행위)</h4>
      <p>• 소분받은 상품의 재판매나 영리적 이용</p>
      <p>• 식품위생법 등 관련 법령을 위반하는 행위</p>
      <p>• 허위 정보 제공 또는 타인의 개인정보 도용</p>

      <h4>제5조 (책임의 한계)</h4>
      <p>회사는 이용자 간의 거래에서 발생하는 분쟁에 대해 책임을 지지 않으며, 상품의 품질, 안전성에 대해 보증하지 않습니다.</p>

      <div className={styles.warningBox}>
        <p>※ 본 서비스는 학습용 프로젝트로 운영됩니다.</p>
      </div>
    </div>
  </div>
);

// 개인정보 처리방침 컴포넌트
const PrivacyPolicy = () => (
  <div className={styles.termsContent}>
    <h3>개인정보 처리방침</h3>
    <div className={styles.scrollableBox}>
      <h4>제1조 (개인정보 처리 목적)</h4>
      <p>회사는 다음의 목적을 위하여 개인정보를 처리합니다:</p>
      <p>• 카카오톡 소셜로그인을 통한 회원 가입 및 본인 인증</p>
      <p>• 대형마트 소분 모임 매칭 서비스 제공</p>
      <p>• 모임 참여 현황 관리 및 커뮤니티 서비스</p>

      <h4>제2조 (처리하는 개인정보 항목)</h4>
      <div className={styles.highlight}>
        <p><strong>카카오톡 소셜로그인을 통한 수집:</strong></p>
        <p>• 카카오톡 닉네임: 서비스 내 사용자 식별</p>
        <p>• 카카오톡 프로필 사진: 사용자 프로필 표시</p>
      </div>

      <div className={styles.highlight}>
        <p><strong>자동 수집:</strong></p>
        <p>• IP주소, 서비스 접속 기록, 브라우저 정보</p>
      </div>

      <h4>제3조 (보유 및 이용기간)</h4>
      <p>• 회원정보: 회원탈퇴 시까지</p>
      <p>• 서비스 이용기록: 1년</p>
      <p>• 카카오톡 연동정보: 연동 해제 시 즉시 삭제</p>

      <h4>제4조 (개인정보 보호)</h4>
      <p>회사는 개인정보를 제3자에게 제공하지 않으며, 기본적인 보안 조치를 취하고 있습니다.</p>

      <div className={styles.warningBox}>
        <p><strong>⚠️ 중요 고지사항:</strong></p>
        <p>본 서비스는 학습용 프로젝트로서 상용 서비스 수준의 보안 조치가 완전히 구현되지 않았을 수 있습니다. 민감한 개인정보의 입력은 권장하지 않습니다.</p>
      </div>

      <h4>제5조 (연락처)</h4>
      <p>개인정보 보호 관련 문의사항이 있으시면 아래로 연락주시기 바랍니다.</p>
      <p>• 개인정보 보호책임자: 강관주</p>
    </div>
  </div>
);

const AgreementPage = () => {
  const { user, isAuthenticated, logout, refreshMeSilent } = useAuthStore();
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState('');

  // 개별 약관 동의 상태
  const [agreements, setAgreements] = useState({
    serviceTerms: false,        // 서비스 이용약관 (필수)
    privacyPolicy: false,       // 개인정보 처리방침 (필수)
    allAgreed: false           // 전체 동의
  });

  // 약관 동의 상태를 검사
  useEffect(() => {
    // 1. 사용자가 로그인 상태가 아니거나
    // 2. 약관에 동의했다면 메인으로 이동
    if (!isAuthenticated || user.agree) {
      navigate('/');
    }
  }, [user, isAuthenticated, navigate]);

  // 전체 동의 체크박스 핸들러
  const handleAllAgree = (checked) => {
    setAgreements({
      serviceTerms: checked,
      privacyPolicy: checked,
      allAgreed: checked
    });
  };

  // 개별 약관 동의 핸들러
  const handleIndividualAgree = (type, checked) => {
    const newAgreements = {
      ...agreements,
      [type]: checked
    };

    // 모든 필수 항목이 체크되었는지 확인
    const allRequired = newAgreements.serviceTerms && newAgreements.privacyPolicy;
    newAgreements.allAgreed = allRequired;

    setAgreements(newAgreements);
  };

  // 로그아웃 핸들러
  const handleLogout = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  // 약관 동의 핸들러
  const handleFinalAgree = async () => {
    if (!agreements.serviceTerms || !agreements.privacyPolicy) {
      setErrorMessage('필수 약관에 모두 동의해주세요.');
      return;
    }

    try {
      const res = await AuthService.updateAgreement();
      if (res.data.success) {
        // 동의가 성공하면 유저 정보 갱신
        await refreshMeSilent();
        // 스크롤을 맨 위로 이동
        window.scrollTo(0, 0);
        // 메인 페이지로 리디렉션
        navigate('/', { replace: true });
      }
    } catch (error) {
      console.error("약관 동의 실패:", error);
      setErrorMessage('서버와 통신 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className={styles.agreementContainer}>
      <div className={styles.header}>
        <h1>반띵 서비스 약관 동의</h1>
        <p className={styles.subtitle}>안전하고 건전한 서비스 이용을 위해 약관 동의가 필요합니다.</p>
        <p className={styles.projectNotice}>
          ※ 본 서비스는 학습용 프로젝트입니다
        </p>
      </div>

      {/* 전체 동의 */}
      <div className={styles.allAgreeBox}>
        <label className={styles.checkboxLabel}>
          <input
            type="checkbox"
            checked={agreements.allAgreed}
            onChange={(e) => handleAllAgree(e.target.checked)}
          />
          전체 동의하기
        </label>
        <p className={styles.description}>
          서비스 이용약관 및 개인정보 처리방침에 모두 동의합니다.
        </p>
      </div>

      {/* 서비스 이용약관 */}
      <div className={styles.individualAgreeBox}>
        <label className={styles.checkboxLabel}>
          <input
            type="checkbox"
            checked={agreements.serviceTerms}
            onChange={(e) => handleIndividualAgree('serviceTerms', e.target.checked)}
          />
          서비스 이용약관 동의 <span className={styles.required}>(필수)</span>
        </label>
        <ServiceTerms />
      </div>

      {/* 개인정보 처리방침 */}
      <div className={styles.individualAgreeBox}>
        <label className={styles.checkboxLabel}>
          <input
            type="checkbox"
            checked={agreements.privacyPolicy}
            onChange={(e) => handleIndividualAgree('privacyPolicy', e.target.checked)}
          />
          개인정보 처리방침 동의 <span className={styles.required}>(필수)</span>
        </label>
        <PrivacyPolicy />
      </div>

      {/* 에러 메시지 */}
      {errorMessage && (
        <div className={styles.errorMessage}>
          {errorMessage}
        </div>
      )}

      {/* 버튼들 */}
      <div className={styles.buttonContainer}>
        <button
          className={`${styles.agreeButton} ${
            agreements.serviceTerms && agreements.privacyPolicy
              ? styles.enabled
              : styles.disabled
          }`}
          onClick={handleFinalAgree}
          disabled={!agreements.serviceTerms || !agreements.privacyPolicy}
        >
          동의하고 시작하기
        </button>
        <button
          className={styles.logoutButton}
          onClick={handleLogout}
        >
          로그아웃
        </button>
      </div>
    </div>
  );
};

export default AgreementPage;