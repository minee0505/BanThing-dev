package com.nathing.banthing.repository;

import com.nathing.banthing.entity.Meeting;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SpringBootTest
@Transactional
class MeetingsRepositoryTest {
    // 필요한 Repository 주입
    @Autowired
    private MeetingsRepository meetingsRepository;

    @Test
    @DisplayName("특정 유저가 승인된 모임 목록을 조회하면 해당 모임들이 반환되어야 한다")
    void findApprovedMeetingsWithMartByUserId_ReturnsApprovedMeetings() {
        // given
        // data.sql에서 데이터 준비 완료
        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<Meeting> approvedMeetingsWithMartByUserId = meetingsRepository.findApprovedMeetingsWithMartByUserId(1L, pageable);

        // then
        System.out.println("\n\n\n\n\n\n\n\n\n\n");

        for (Meeting meeting : approvedMeetingsWithMartByUserId) {
            System.out.println(meeting.toString());
            System.out.println("\n");
        }

    }
}