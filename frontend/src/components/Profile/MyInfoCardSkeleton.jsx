import React from 'react';
import styles from './MyInfoCardSkeleton.module.scss';

/**
 * MyInfoCardSkeleton 컴포넌트
 *
 * 사용자 정보 카드의 스켈레톤 UI를 렌더링하는 컴포넌트입니다.
 * 데이터를 로딩하는 동안 뼈대 형태의 UI를 표시하기 위해 사용됩니다.
 *
 * 구조:
 * - 섹션 태그로 감싸진 사용자 정보 카드 뼈대
 * - 프로필 헤더 영역:
 *   - 프로필 이미지의 스켈레톤
 *   - 닉네임 표시 영역의 스켈레톤
 * - 사용자 통계 리스트:
 *   - 각 스탯 항목은 레이블 및 값의 스켈레톤으로 구성
 * @author 강관주
 * @since 2025.09.20
 */
const MyInfoCardSkeleton = () => {
  return (
    <section className={styles.myInfoCard}>
      <div className={styles.profileHeader}>
        <div className={styles.profileImageSkeleton}></div>
        <div className={styles.profileInfo}>
          <div className={styles.nicknameSkeleton}></div>
        </div>
      </div>

      <ul className={styles.userStats}>
        <li className={styles.statItem}>
          <div className={styles.statLabelSkeleton}></div>
          <div className={styles.statValueSkeleton}></div>
        </li>
        <li className={styles.statItem}>
          <div className={styles.statLabelSkeleton}></div>
          <div className={styles.statValueSkeleton}></div>
        </li>
        <li className={styles.statItem}>
          <div className={styles.statLabelSkeleton}></div>
          <div className={styles.statValueSkeleton}></div>
        </li>
      </ul>
    </section>
  );
};

export default MyInfoCardSkeleton;