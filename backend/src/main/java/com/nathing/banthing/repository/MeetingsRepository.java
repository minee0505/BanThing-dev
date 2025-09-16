package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingsRepository extends JpaRepository<Meeting, Long> {

    // 특정 상태이면서 모임 시간이 지난 모임들 조회
    List<Meeting> findByStatusAndMeetingDateBefore(Meeting.MeetingStatus status, LocalDateTime dateTime);

    /**
     * 상태별 모임 조회 (삭제되지 않은 모임만)
     */
    List<Meeting> findByStatusAndDeletedAtIsNull(Meeting.MeetingStatus status);

    /**
     * 제목이나 설명에 키워드가 포함된 모임 검색
     */
    @Query("SELECT m FROM Meeting m WHERE " +
            "(LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "m.status = 'RECRUITING' AND m.deletedAt IS NULL")
    List<Meeting> findByKeywordAndRecruiting(@Param("keyword") String keyword);

    /**
     * 마트 지점별 모집중인 모임 조회
     */
    List<Meeting> findByMartMartIdAndStatusAndDeletedAtIsNull(
            Long martId, Meeting.MeetingStatus status);

    /**
     * 지역(마트 주소) 기반 모임 검색
     */
    @Query("SELECT m FROM Meeting m JOIN m.mart mart WHERE " +
            "LOWER(mart.address) LIKE LOWER(CONCAT('%', :location, '%')) AND " +
            "m.status = 'RECRUITING' AND m.deletedAt IS NULL")
    List<Meeting> findByLocationAndRecruiting(@Param("location") String location);


    /**
     * 모든 모임을 생성 시간(createdAt) 내림차순으로 정렬하여 조회합니다. (최신순)
     * (@Where 어노테이션에 의해 삭제된 모임은 자동으로 제외됩니다.)
     * @return 정렬된 모임 목록
     */
    List<Meeting> findAllByOrderByCreatedAtDesc();
}