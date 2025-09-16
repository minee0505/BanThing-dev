package com.nathing.banthing.entity;

import lombok.Getter;

/**
 @author 송민재
 @since 25. 9. 15. */
@Getter
public enum TrustGrade {
    GOOD,   // 500점 이상
    BASIC,  // 101점 ~ 499점
    WARNING // 100점 이하
}
