import React from 'react';
import { RiVerifiedBadgeFill } from "react-icons/ri";
import styles from './TrustBadge.module.scss';

const TrustBadge = ({ trustScore }) => {
    let trustClassName = '';

    if (trustScore >= 500) {
        trustClassName = styles.trustGood;
    } else if (trustScore >= 300) {
        trustClassName = styles.trustBasic;
    } else {
        trustClassName = styles.trustWarning;
    }

    return (
        <RiVerifiedBadgeFill className={trustClassName}/>
    );
};

export default TrustBadge;