package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.Meeting;
import lombok.Getter;

@Getter
public class MeetingUpdateResponse {

    private final Long updatedMeetingId;

    public MeetingUpdateResponse(Meeting meeting) {
        this.updatedMeetingId = meeting.getMeetingId();
    }
}