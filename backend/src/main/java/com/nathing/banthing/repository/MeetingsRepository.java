package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query("SELECT m FROM Meeting m JOIN m.mart mart WHERE " +
                  "(LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                  "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                  "LOWER(mart.martName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                  "m.deletedAt IS NULL")
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
     * "m.mart"를 함께 조회(JOIN FETCH)하여 N+1 문제를 방지하고
     * Mart 정보가 누락되지 않도록 보장합니다.
     */
    @Query("SELECT m FROM Meeting m JOIN FETCH m.mart ORDER BY m.createdAt DESC")
    List<Meeting> findAllWithMartByOrderByCreatedAtDesc();

    /**
     * 주어진 사용자 ID에 대해 승인된 모임 목록을 반환하는 메서드입니다. (최신순)
     *
     * 네이티브 쿼리로 JOIN을 통해
     * Meeting과 Mart, 모임 참가자 정보를 모두 가져오고 있기 때문에
     * N+1 문제는 발생하지 않음
     *
     * JPA가 Pageable 정보를 쿼리에 자동으로 반영해서 LIMIT와 OFFSET을 추가,
     * countQuery를 통해 전체 개수도 별도로 가져와서
     * Page 객체에 필요한 모든 정보를 한 번에 얻을 수 있음
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보를 포함하는 Pageable 객체
     * @return 승인된 모임 목록을 담고 있는 Page 객체
     */
    @Query(value = """
        SELECT m.*
        FROM meetings m
        JOIN meeting_participants mp ON m.meeting_id = mp.meeting_id
        JOIN marts ma ON m.mart_id = ma.mart_id
        WHERE mp.user_id = :userId AND mp.application_status = 'APPROVED'
            AND m.deleted_at IS NULL
        ORDER BY m.created_at DESC
    """,
    countQuery = """
        SELECT count(m.meeting_id)
        FROM meetings m
        JOIN meeting_participants mp ON m.meeting_id = mp.meeting_id
        WHERE mp.user_id = :userId AND mp.application_status = 'APPROVED'
            AND m.deleted_at IS NULL
    """,
    nativeQuery = true)
    Page<Meeting> findApprovedMeetingsWithMartByUserId(@Param("userId") Long userId, Pageable pageable);
}