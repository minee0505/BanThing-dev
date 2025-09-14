package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.Meeting;
import com.nathing.banthing.entity.MeetingParticipant;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MeetingDetailResponse {

    private final Long meetingId;
    private final String title;
    private final String description;
    private final String martName;
    private final LocalDateTime meetingDate;
    private final Integer currentParticipants;
    private final Integer maxParticipants;
    private final String status;
    private final String thumbnailImageUrl;
    private final HostInfo hostInfo;
    private final List<ParticipantInfo> participants;
    /**
     * MeetingDetailResponse 생성자는 전달받은 Meeting 객체를 기반으로 미팅 세부 정보를 초기화합니다.
     *
     * @param meeting 미팅 정보 객체로, MeetingDetailResponse의 모든 필드를 초기화하는 데 사용됩니다.
     *                이 객체는 미팅의 기본 정보, 호스트, 참여자 및 상태 관련 정보를 포함합니다.
     *
     * @author - 고동현
     * @since - 2025-09-15
     */
    public MeetingDetailResponse(Meeting meeting) {
        this.meetingId = meeting.getMeetingId();
        this.title = meeting.getTitle();
        this.description = meeting.getDescription();
        this.martName = meeting.getMart().getMartName();
        this.meetingDate = meeting.getMeetingDate();
        this.currentParticipants = meeting.getCurrentParticipants();
        this.maxParticipants = meeting.getMaxParticipants();
        this.status = meeting.getStatus().name();
        this.thumbnailImageUrl = meeting.getThumbnailImageUrl();

        // Host 정보와 Participant 목록을 스트림을 사용해 분리 및 변환
        this.hostInfo = new HostInfo(meeting.getHostUser());
        this.participants = meeting.getParticipants().stream()
                .filter(p -> p.getApplicationStatus() == MeetingParticipant.ApplicationStatus.APPROVED)
                .map(ParticipantInfo::new)
                .collect(Collectors.toList());
    }

    // 호스트 정보를 담을 내부 클래스
    @Getter
    private static class HostInfo {
        private final String nickname;
        private final String profileImageUrl;

        public HostInfo(com.nathing.banthing.entity.User host) {
            this.nickname = host.getNickname();
            this.profileImageUrl = host.getProfileImageUrl();
        }
    }

    // 참여자 정보를 담을 내부 클래스
    @Getter
    private static class ParticipantInfo {
        private final String nickname;
        private final String profileImageUrl;
        private final String participantType;

        public ParticipantInfo(MeetingParticipant participant) {
            this.nickname = participant.getUser().getNickname();
            this.profileImageUrl = participant.getUser().getProfileImageUrl();
            this.participantType = participant.getParticipantType().name();
        }
    }
}