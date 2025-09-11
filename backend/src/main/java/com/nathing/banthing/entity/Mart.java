package com.nathing.banthing.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 1. 마트 정보 엔터티
@Entity
@Table(name = "marts")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mart_id")
    private Long martId;

    @Column(name = "mart_name", nullable = false, length = 100)
    private String martName;

    @Enumerated(EnumType.STRING)
    @Column(name = "mart_brand", nullable = false)
    private MartBrand martBrand;

    @Column(name = "address", nullable = false, length = 200)
    private String address;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 연관관계 매핑
    @OneToMany(mappedBy = "mart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meeting> meetings = new ArrayList<>();

    public enum MartBrand {
        COSTCO, TRADERS, LOTTE_MART
    }
}