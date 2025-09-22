import React from 'react';
import { RiVerifiedBadgeFill } from "react-icons/ri";
import styles from './TrustBadge.module.scss';

const TrustBadge = ({ trustScore }) => {
    let trustClassName = '';

    if (trustScore >= 500) { //500 정 이상 파랑 뱃지
        trustClassName = styles.trustGood;
    } else if (trustScore >= 100) {// 499 ~100 사이 초록 뱃지
        trustClassName = styles.trustBasic;
    } else {                        // 99이하 빨강 뱃지
        trustClassName = styles.trustWarning;
    }

    return (
        <RiVerifiedBadgeFill className={trustClassName}/>
    );
};

export default TrustBadge;